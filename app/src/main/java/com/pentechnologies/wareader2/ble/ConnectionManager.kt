
package com.pentechnologies.wareader2.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.Observer
import com.pentechnologies.wareader2.Initializer
import com.pentechnologies.wareader2.Initializer.Companion.wristConnector
import com.pentechnologies.wareader2.MainActivity.Companion.ToastThis
import com.pentechnologies.wareader2.MainActivity.Companion.flagWrist
import com.pentechnologies.wareader2.MainActivity.Companion.testWrist
import com.pentechnologies.wareader2.Utils.getTime
import com.pentechnologies.wareader2.Utils.getTodayDate
import com.pentechnologies.wareader2.db.Ecg
import com.pentechnologies.wareader2.db.HeartRate
import com.pentechnologies.wareader2.db.SpO2
import com.pentechnologies.wareader2.db.Temperature
import com.punchthrough.blestarterappandroid.ble.*
import jxl.write.DateTime
import jxl.write.Label
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

object ConnectionManager {

    //show the values on the ui from the wrist device and save them on the excel
    private val wristGattCallback by lazy {
        WristGattCallback.let { it ->
            it.initInstance()
            val mainActivity = Initializer.getInstance().mainActivity
            var rr: String = "0"
            //
            val rrObserver = Observer<String> { value ->
               //show the rr interval on the UI
                mainActivity.rr_interval.text = value
                rr = value
            }
            val heartRateObserver = Observer<String> {value ->
                Log.i("MainActivity", "new value added $value")
                //show the heart rate on the UI
                mainActivity.heart_rate.text = value
                //insert heart Rate on the Excel that is saved on the local database
                mainActivity.mainViewModel.insertHeartRate(mainActivity.getApp(),
                    HeartRate(0,value,rr,getTime(),getTodayDate())
                )
               //get the value on the heart rate graph
                mainActivity.addHeartEntry(value.toFloat())
            }
            val bodySensorLocationObserver = Observer<String> { value ->
                //show the value on the UI
                mainActivity.sensor_location.text = value
            }
            val temperatureObserver = Observer<String> { value ->
                //show the temperature on the UI
                mainActivity.temperature.text = value
                //insert temperature on the Excel that is saved on the local database
                mainActivity.mainViewModel.insertTemperature(mainActivity.getApp(),
                    Temperature(0,value,getTime(),getTodayDate())
                )
            }
            val spo2Observer = Observer<String> { value ->
                //show the sp02 on the UI
                mainActivity.spo2.text = value
                //insert sp02 on the Excel that is saved on the local database
                mainActivity.mainViewModel.insertSpO2(mainActivity.getApp(),
                    SpO2(0,value,getTime(),getTodayDate())
                )
            }
            val ecgObserver = Observer<String> { value ->
                //show the ecg positive on the UI
                mainActivity.ecg.text = value
                //insert ecg positive on the Excel that is saved on the local database
                mainActivity.mainViewModel.insertEcg(mainActivity.getApp(),
                    Ecg(0,value,getTime(),getTodayDate())
                )
                //get the value for the ecg graph
                mainActivity.addEcgEntry(value.toFloat())
            }
            val ecgNegObserver = Observer<String> {value ->
                //show the ecg negative on the UI
                mainActivity.ecg.text = value
                //insert ecg negative on the Excel that is saved on the local database
                mainActivity.mainViewModel.insertEcg(mainActivity.getApp(),
                    Ecg(0,value,getTime(),getTodayDate())
                )
                //get the value for the ecg graph
                mainActivity.addEcgEntry(value.toFloat())
            }
            //Call the observer for each characteristic as initialized on mainActivityModel class (live data)
            mainActivity.mainActivityModel.heartRateMeasurement.observe(mainActivity, heartRateObserver)
            mainActivity.mainActivityModel.rr.observe(mainActivity, rrObserver)
            mainActivity.mainActivityModel.bodySensorLocation.observe(mainActivity, bodySensorLocationObserver)
            mainActivity.mainActivityModel.temperature.observe(mainActivity, temperatureObserver)
            mainActivity.mainActivityModel.sp02.observe(mainActivity, spo2Observer)
            mainActivity.mainActivityModel.ecg.observe(mainActivity, ecgObserver)
            mainActivity.mainActivityModel.ecgNeg.observe(mainActivity, ecgNegObserver)

            it.getInstance()
        }
    }



    val deviceGattMap = ConcurrentHashMap<BluetoothDevice, BluetoothGatt>()
    private val operationQueue = ConcurrentLinkedQueue<BleOperationType>()
    var pendingOperation: BleOperationType? = null
   //we get here from Initializer class when wrist device MAC address is found
    fun connect(device: BluetoothDevice, context: Context) {
        //check if wrist device already connected
        if (device.isConnected()) {
            Log.i("ConnectionManager","Already connected to ${device.address}!")
            flagWrist = true
        } else {
            //call enqueueOperation function to initiate connection
            Toast.makeText(ToastThis, "We are trying to connect to Wrist Device ", Toast.LENGTH_LONG).show()
            enqueueOperation(Connect(device, context.applicationContext))
        }
    }

    fun readCharacteristic(device: BluetoothDevice, characteristic: BluetoothGattCharacteristic) {
        if (device.isConnected() && characteristic.isReadable()) {
            enqueueOperation(CharacteristicRead(device, characteristic.uuid))

        } else if (!characteristic.isReadable()) {

            Log.i("ConnectionManager","Attempting to read ${characteristic.uuid} that isn't readable!")
        } else if (!device.isConnected()) {
            Log.i("ConnectionManager","Not connected to ${device.address}, cannot perform characteristic read")
        }
    }
    fun enableNotification(device: BluetoothDevice, characteristic: BluetoothGattCharacteristic) {
        if (device.isConnected() &&
            (characteristic.isIndicatable() || characteristic.isNotifiable())
        ) {
            enqueueOperation(EnableNotifications(device, characteristic.uuid))
        } else if (!device.isConnected()) {
            Log.i("ConnectionManager","Not connected to ${device.address}, cannot enable notifications")
        } else if (!characteristic.isIndicatable() && !characteristic.isNotifiable()) {
            Log.i("ConnectionManager", "Characteristic ${characteristic.uuid} doesn't support notifications/indications")
        }
    }
    fun teardownConnection(device: BluetoothDevice) {
        if (device.isConnected()) {
            enqueueOperation(Disconnect(device))
        } else {
            Log.i("ConnectionManager","Not connected to ${device.address}, cannot teardown connection!")
        }
    }



    @SuppressLint("MissingPermission")
    private fun doNextOperation() {
        if (pendingOperation != null) {

            Log.i("ConnectionManager","doNextOperation() called when an operation is pending! Aborting.")
            return
        }

        val operation = operationQueue.poll() ?: run {
            Log.i("ConnectionManager","Operation queue empty, returning")
            return
        }
        pendingOperation = operation

        // Handle Connect separately from other operations that require device to be connected
        if (operation is Connect) {

            with(operation) {
                Log.i("ConnectionManager","Connecting to ${device.address}")
                flagWrist = true
                device.connectGatt(context, false, wristGattCallback)
                wristConnector = false
                waitIdle2()

            }
            return
        }


        val gatt = deviceGattMap[operation.device]
            ?: this@ConnectionManager.run {
                Log.i("ConnectionManager","Not connected to ${operation.device.address}! Aborting $operation operation.")

                signalEndOfOperation()
                return
            }

        when(operation) {
            is Disconnect -> with(operation) {
                Log.w("ConnectionManager","Disconnecting from ${device.address}")
             //   Toast.makeText(ToastThis, "Something happened, we are disconnecting from Wrist Device", Toast.LENGTH_LONG).show()
             //   Toast.makeText(ToastThis, "Restart the MonitorAPP", Toast.LENGTH_LONG).show()
             //   testWrist = true
                Toast.makeText(ToastThis, "Something happened, we are disconnecting from Wrist Device", Toast.LENGTH_LONG).show()
                flagWrist = false
                gatt.close()
                deviceGattMap.remove(device)
                signalEndOfOperation()
            }
            is CharacteristicRead -> with(operation) {

                gatt.findCharacteristic(characteristicUuid)?.let { characteristic ->
                    gatt.readCharacteristic(characteristic)
                } ?: this@ConnectionManager.run {
                    Log.i("ConnectionManager","Cannot find $characteristicUuid to read from")
                    signalEndOfOperation()
                }
            }
            is EnableNotifications -> with(operation) {
                gatt.findCharacteristic(characteristicUuid)?.let { characteristic ->
                    val cccdUuid = UUID.fromString(CCC_DESCRIPTOR_UUID)
                    val payload = when {
                        characteristic.isIndicatable() ->
                            BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
                        characteristic.isNotifiable() ->
                            BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                        else ->
                            error("${characteristic.uuid} doesn't support notifications/indications")
                    }

                    characteristic.getDescriptor(cccdUuid)?.let { cccDescriptor ->
                        if (!gatt.setCharacteristicNotification(characteristic, true)) {
                            Log.i("ConnectionManager", "setCharacteristicNotification failed for ${characteristic.uuid}")
                            signalEndOfOperation()
                            return
                        }

                        cccDescriptor.value = payload
                        gatt.writeDescriptor(cccDescriptor)
                    } ?: this@ConnectionManager.run {
                        Log.i("ConnectionManager", "${characteristic.uuid} doesn't contain the CCC descriptor!")
                        signalEndOfOperation()
                    }
                } ?: this@ConnectionManager.run {
                    Log.i("ConnectionManager","Cannot find $characteristicUuid! Failed to enable notifications.")
                    signalEndOfOperation()
                }
            }
        }

    }

    private fun waitIdle2() {

        Log.i("ConnectionManager", "We are on the Wrist 20ms delay")

            try {
                Thread.sleep(20)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }

    }

    @Synchronized
    private fun enqueueOperation(operation: BleOperationType) {
        operationQueue.add(operation)
        if (pendingOperation == null) {

            doNextOperation()
        }
    }

    @Synchronized
    fun signalEndOfOperation() {
        Log.i("ConnectionManager","End of $pendingOperation")
        pendingOperation = null
        if (operationQueue.isNotEmpty()) {

            doNextOperation()
        }
    }

    private fun BluetoothDevice.isConnected() = deviceGattMap.containsKey(this)


}