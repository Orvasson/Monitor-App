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
import com.pentechnologies.wareader2.MainActivity
import com.pentechnologies.wareader2.MainActivity.Companion.flagAnkle
import com.pentechnologies.wareader2.Utils.getTime
import com.pentechnologies.wareader2.Utils.getTodayDate
import com.pentechnologies.wareader2.db.Ankle
import com.pentechnologies.wareader2.db.Gps
import com.pentechnologies.wareader2.db.HeartRate
import com.pentechnologies.wareader2.db.Steps
import com.punchthrough.blestarterappandroid.ble.*
import jxl.write.DateTime
import jxl.write.Label
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

object ConnectionManagerAnkle {

    //show the values on the ui from the ankle device
    private val ankleGattCallback by lazy {
        AnkleGattCallback.let {
            it.initInstance2()
            val mainActivity = Initializer.getInstance2().mainActivity


            val temperatureObserver = Observer<String> { value ->
                Log.i("MainActivity", "new value added $value")
                mainActivity.ankle_temp.text = value

                mainActivity.mainViewModel.insertAnkle(mainActivity.getApp(),
                    Ankle(0,value,
                        getTime(),
                        getTodayDate()
                    )
                )

            }
            var steps = "0"
            val stepsObserver = Observer<String> { value ->
                Log.i("MainActivity", "new value added $value")
                mainActivity.ankle_steps.text = value
                steps = value
            }
            val metersObserver = Observer<String> { value ->
                Log.i("MainActivity", "new value added $value")
                mainActivity.ankle_meters.text = value


                mainActivity.mainViewModel.insertSteps(mainActivity.getApp(),
                    Steps(0,steps,value,
                        getTime(),
                        getTodayDate()
                    )
                )

            }

            var lat = "0.0"
            val latitudeObserver = Observer<String> { value ->
                Log.i("MainActivity", "new value added $value")
                mainActivity.ankle_lat.text = value
                lat = value

            }
            val longitudeObserver = Observer<String> { value ->
                Log.i("MainActivity", "new value added $value")
                mainActivity.ankle_long.text = value
                val lng = value
                mainActivity.mainViewModel.insertGps(mainActivity.getApp(),
                    Gps(0,lat,lng,
                        getTime(),
                        getTodayDate()
                    )
                )

            }
            val sivObserver = Observer<String> { value ->
                Log.i("MainActivity", "new value added $value")
                mainActivity.ankle_siv.text = value
            }

            mainActivity.mainActivityModelAnkle.ankleTemperatureMeas.observe(
                mainActivity,
                temperatureObserver
            )
            mainActivity.mainActivityModelAnkle.stepsMeas.observe(mainActivity, stepsObserver)
            mainActivity.mainActivityModelAnkle.metersMeas.observe(mainActivity, metersObserver)
            mainActivity.mainActivityModelAnkle.latMeas.observe(mainActivity, latitudeObserver)
            mainActivity.mainActivityModelAnkle.longMeas.observe(mainActivity, longitudeObserver)
            mainActivity.mainActivityModelAnkle.sivMeas.observe(mainActivity, sivObserver)

            it.getInstance2()
        }
    }


    val deviceGattMap = ConcurrentHashMap<BluetoothDevice, BluetoothGatt>()
    private val operationQueue = ConcurrentLinkedQueue<BleOperationType>()
    var pendingOperation: BleOperationType? = null

    fun connect(device: BluetoothDevice, context: Context) {

        if (device.isConnected()) {
            Log.i("ConnectionManager2", "Already connected to ${device.address}!")
            flagAnkle = true
        } else {
            Toast.makeText(MainActivity.ToastThis, "We are trying to connect to Ankle Device ", Toast.LENGTH_LONG).show()
            enqueueOperation(Connect(device, context.applicationContext))

        }
    }

    //This function reads the value of the characteristic
    fun readCharacteristic(device: BluetoothDevice, characteristic: BluetoothGattCharacteristic) {
        if (device.isConnected() && characteristic.isReadable()) {
            enqueueOperation(CharacteristicRead(device, characteristic.uuid))

        } else if (!characteristic.isReadable()) {
            Log.i(
                "ConnectionManager2",
                "Attempting to read ${characteristic.uuid} that isn't readable!"
            )
        } else if (!device.isConnected()) {
            Log.i(
                "ConnectionManager2",
                "Not connected to ${device.address}, cannot perform characteristic read"
            )
        }
    }

    //This function enables Notify on our device
    fun enableNotification(device: BluetoothDevice, characteristic: BluetoothGattCharacteristic) {
        if (device.isConnected() &&
            (characteristic.isIndicatable() || characteristic.isNotifiable())
        ) {
            enqueueOperation(EnableNotifications(device, characteristic.uuid))
        } else if (!device.isConnected()) {
            Log.i(
                "ConnectionManager2",
                "Not connected to ${device.address}, cannot enable notifications"
            )
        } else if (!characteristic.isIndicatable() && !characteristic.isNotifiable()) {
            Log.i(
                "ConnectionManager2",
                "Characteristic ${characteristic.uuid} doesn't support notifications/indications"
            )
        }
    }

    fun teardownConnection(device: BluetoothDevice) {
        if (device.isConnected()) {
            enqueueOperation(Disconnect(device))
        } else {
            Log.i(
                "ConnectionManager2",
                "Not connected to ${device.address}, cannot teardown connection!"
            )
        }
    }


    @SuppressLint("MissingPermission")
    private fun doNextOperation() {
        if (pendingOperation != null) {
            Log.i(
                "ConnectionManager2",
                "doNextOperation() called when an operation is pending! Aborting."
            )
            return
        }

        val operation = operationQueue.poll() ?: run {
            Log.i("ConnectionManager2", "Operation queue empty, returning")

            return
        }
        pendingOperation = operation

        // Handle Connect separately from other operations that require device to be connected

        if (operation is Connect) {

            with(operation) {
                Log.i("ConnectionManager2", "Connecting to ${device.address}")
                flagAnkle = true
                device.connectGatt(context, false, ankleGattCallback)
                waitIdle2()
            }
            return
        }

        val gatt = deviceGattMap[operation.device]
            ?: this@ConnectionManagerAnkle.run {
                Log.i(
                    "ConnectionManager2",
                    "Not connected to ${operation.device.address}! Aborting $operation operation."
                )
                signalEndOfOperation()
                return
            }

        when (operation) {
            is Disconnect -> with(operation) {
                Log.w("ConnectionManager2", "Disconnecting from ${device.address}")
                flagAnkle = false
                gatt.close()
                deviceGattMap.remove(device)
                signalEndOfOperation()
            }
            is CharacteristicRead -> with(operation) {

                gatt.findCharacteristic(characteristicUuid)?.let { characteristic ->
                    gatt.readCharacteristic(characteristic)
                } ?: this@ConnectionManagerAnkle.run {
                    Log.i("ConnectionManager2", "Cannot find $characteristicUuid to read from")
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
                            Log.i(
                                "ConnectionManager2",
                                "setCharacteristicNotification failed for ${characteristic.uuid}"
                            )
                            signalEndOfOperation()
                            return
                        }

                        cccDescriptor.value = payload
                        gatt.writeDescriptor(cccDescriptor)
                    } ?: this@ConnectionManagerAnkle.run {
                        Log.i(
                            "ConnectionManager2",
                            "${characteristic.uuid} doesn't contain the CCC descriptor!"
                        )
                        signalEndOfOperation()
                    }
                } ?: this@ConnectionManagerAnkle.run {
                    Log.i(
                        "ConnectionManager2",
                        "Cannot find $characteristicUuid! Failed to enable notifications."
                    )
                    signalEndOfOperation()
                }
            }
        }

    }


    private fun waitIdle2() {
    Log.i("ConnectionManager", "We are on the Ankle 20ms delay")
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
        Log.i("ConnectionManager2", "End of $pendingOperation")
        pendingOperation = null
        if (operationQueue.isNotEmpty()) {
            doNextOperation()
        }
    }

    private fun BluetoothDevice.isConnected() = deviceGattMap.containsKey(this)


}