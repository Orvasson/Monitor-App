package com.pentechnologies.wareader2.ble


import android.annotation.SuppressLint
import android.bluetooth.*
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.pentechnologies.wareader2.Initializer
import com.pentechnologies.wareader2.MainActivity.Companion.ToastThis
import com.pentechnologies.wareader2.MainActivity.Companion.checkAnkle
import com.pentechnologies.wareader2.MainActivity.Companion.connectToDevices2
import com.pentechnologies.wareader2.MainActivity.Companion.flagAnkle
import com.pentechnologies.wareader2.MainActivity.Companion.flagWrist
import com.pentechnologies.wareader2.MainActivity.Companion.gattWrist
import com.punchthrough.blestarterappandroid.ble.*
import java.util.*


private const val HEART_RATE_SERVICE_UUID_STRING = "0000180d-0000-1000-8000-00805f9b34fb"
private const val HEART_RATE_MEASUREMENT_UUID_STRING = "00002a37-0000-1000-8000-00805f9b34fb"
private const val R_R_UUID_STRING = "00000201-0000-1000-8000-00805f9b34fb"
private const val BODY_SENSOR_LOCATION_UUID_STRING = "00002a38-0000-1000-8000-00805f9b34fb"

private const val TEMPERATURE_SERVICE_UUID_STRING = "00001809-0000-1000-8000-00805f9b34fb"
private const val TEMPERATURE_UUID_STRING = "00002a6e-0000-1000-8000-00805f9b34fb"

private const val SPO2_SERVICE_UUID_STRING = "00001822-0000-1000-8000-00805f9b34fb"
private const val SPO2_UUID_STRING = "00002a5e-0000-1000-8000-00805f9b34fb"

private const val ECG_SERVICE_UUID_STRING = "00002d0d-0000-1000-8000-00805f9b34fb"

//private const val ECG_UUID_STRING = "00002d37-0000-1000-8000-00805f9b34fb"
private const val ECG_POSITIVE_STRING = "22220020-1c35-402b-b938-af832d35a1c3"
private const val ECG_NEGATIVE_STRING = "33330020-1c35-402b-b938-af832d35a1c3"

//for ecg 10 sec
var EcgSec: Int = 0
var ecgTest: Boolean = false
class WristGattCallback private constructor() :

    BluetoothGattCallback() {
    //variables of the Live Data saved on MainActivityModel of type String
    val heartRateMeasurement: MutableLiveData<String> = MutableLiveData<String>()
    val rr: MutableLiveData<String> = MutableLiveData<String>()
    val bodySensorLocation: MutableLiveData<String> = MutableLiveData<String>()
    val temperature: MutableLiveData<String> = MutableLiveData<String>()
    val sp02: MutableLiveData<String> = MutableLiveData<String>()
    val ecg: MutableLiveData<String> = MutableLiveData<String>()
    val ecgNeg: MutableLiveData<String> = MutableLiveData<String>()
    private lateinit var ankleInitializer: AnkleGattCallback //we need it to call the checkAnkle function


    @SuppressLint("MissingPermission")
    override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
        val deviceAddress = gatt.device.address
        gattWrist = gatt
        if (status == BluetoothGatt.GATT_SUCCESS) {
            //if we are connected to the wrist device
            if (newState == BluetoothProfile.STATE_CONNECTED) {
               flagWrist = true
                Log.i("WristGattCallback", "onConnectionStateChange: connected to $deviceAddress")
                ConnectionManager.deviceGattMap[gatt.device] = gatt
                Handler(Looper.getMainLooper()).post {
                    gatt.discoverServices()     //start function discoverServices
                }
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                //if we disconnected from wrist device
                Log.i("WristGattCallback","onConnectionStateChange: disconnected from $deviceAddress")
                flagWrist = false
                //ask Object ConnectionManager to terminate connection
                ConnectionManager.teardownConnection(gatt.device)
            }
        } else {
            //if we can not aqcuire BluetoothGatt
            Log.i("WristGattCallBack","onConnectionStateChange: status $status encountered for $deviceAddress!")
            if (ConnectionManager.pendingOperation is Connect) {
                ConnectionManager.signalEndOfOperation()
            }
            ConnectionManager.teardownConnection(gatt.device)
        }
    }
    //Discover the services of the device
    override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
       // ankleInitializer = AnkleGattCallback.initInstance2()
        if(flagAnkle) {
            ankleInitializer = AnkleGattCallback.getInstance2()
        }
        if (status == BluetoothGatt.GATT_SUCCESS) {

            Log.i("WristGattCallback", "Discovered ${gatt.services.size} services for ${gatt.device.address}.")
            gatt.printGattTable()

            // the following code get all characterstic from the deivce
            val wristHeartMeasurement = getCharacteristic(gatt, HEART_RATE_SERVICE_UUID_STRING, HEART_RATE_MEASUREMENT_UUID_STRING) // get char
            val wristRr = getCharacteristic(gatt, HEART_RATE_SERVICE_UUID_STRING, R_R_UUID_STRING)
            val bodySensorLocation = getCharacteristic(gatt, HEART_RATE_SERVICE_UUID_STRING, BODY_SENSOR_LOCATION_UUID_STRING)
            val wristTemperature = getCharacteristic(gatt, TEMPERATURE_SERVICE_UUID_STRING, TEMPERATURE_UUID_STRING)
            val wristSpo2 = getCharacteristic(gatt, SPO2_SERVICE_UUID_STRING, SPO2_UUID_STRING)
            val wristECGPositive = getCharacteristic(gatt, ECG_SERVICE_UUID_STRING, ECG_POSITIVE_STRING)
            val wristECGNegative = getCharacteristic(gatt, ECG_SERVICE_UUID_STRING, ECG_NEGATIVE_STRING)
            //if characteristics have any value, then read the value and enable notifications
            if (wristHeartMeasurement != null) {
                ConnectionManager.readCharacteristic(gatt.device, wristHeartMeasurement) // read it
                ConnectionManager.enableNotification(gatt.device, wristHeartMeasurement) // enable its notification
            } // if service got
            if ( wristRr != null) {
                ConnectionManager.readCharacteristic(gatt.device, wristRr)
                ConnectionManager.enableNotification(gatt.device, wristRr)
            }
            if (bodySensorLocation != null) {
                ConnectionManager.readCharacteristic(gatt.device, bodySensorLocation)
                ConnectionManager.enableNotification(gatt.device, bodySensorLocation)
            }
            if (wristTemperature != null) {
                ConnectionManager.readCharacteristic(gatt.device, wristTemperature)
                ConnectionManager.enableNotification(gatt.device, wristTemperature)
            }
            if (wristSpo2 != null) {
                ConnectionManager.readCharacteristic(gatt.device, wristSpo2)
                ConnectionManager.enableNotification(gatt.device, wristSpo2)
            }
            if( wristECGNegative != null){
                ConnectionManager.readCharacteristic(gatt.device, wristECGNegative)
                ConnectionManager.enableNotification(gatt.device, wristECGNegative)
            }
            if( wristECGPositive != null){
                ConnectionManager.readCharacteristic(gatt.device, wristECGPositive)
                ConnectionManager.enableNotification(gatt.device, wristECGPositive)
            }

        } else {
            Log.i("WristGattCallback", "Service discovery failed due to status $status")
            ConnectionManager.teardownConnection(gatt.device)
        }

        if (ConnectionManager.pendingOperation is Connect) {

            ConnectionManager.signalEndOfOperation()
        }
    }
    // First Read of the characteristics from the services
    override fun onCharacteristicRead(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        status: Int
    ) {
        with(characteristic) {

            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    Log.i("WristGattCallback","Read characteristic $uuid | value: ${value.toHexString()}")
                    Initializer.getInstance().mainActivity.runOnUiThread {
                        when (characteristic.uuid) { // check and identify characteristic each time it is called
                            //Max30101 sensor measurement charcteristics
                            UUID.fromString(HEART_RATE_MEASUREMENT_UUID_STRING) -> heartRateMeasurement.value =
                                characteristic.value?.toHexString().toString().toHeartRate()
                            UUID.fromString(R_R_UUID_STRING) -> rr.value =
                                characteristic.value?.toHexString().toString().toRr()
                            UUID.fromString(BODY_SENSOR_LOCATION_UUID_STRING) -> bodySensorLocation.value =
                                characteristic.value?.toHexString().toString().toSensorLocation()
                            //TMP117 sensor measurement characteristic
                            UUID.fromString(TEMPERATURE_UUID_STRING) -> temperature.value =
                                characteristic.value?.toHexString().toString().toTemperature()
                            //MAX30101 sensor measurement SP02
                            UUID.fromString(SPO2_UUID_STRING) -> sp02.value =
                                characteristic.value?.toHexString().toString().toSpo2()
                            //MAX30003 sensor measurement ECG
                            UUID.fromString(ECG_POSITIVE_STRING) -> ecg.value =
                                characteristic.value?.toHexString().toString().toEcg()
                            UUID.fromString(ECG_NEGATIVE_STRING)-> ecgNeg.value =
                                characteristic.value?.toHexString().toString().toNegEcg()
                        }
                    }
                }
                BluetoothGatt.GATT_READ_NOT_PERMITTED -> {
                    Log.i("WristGattCallback","Read not permitted for $uuid!")
                }
                else -> {
                    Log.i("WristGattCallback","Characteristic read failed for $uuid, error: $status")
                }
            }
        }

        if (ConnectionManager.pendingOperation is CharacteristicRead) {
            ConnectionManager.signalEndOfOperation()
        }
    }
    //If characteristic changes value, read it again
    override fun onCharacteristicChanged(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic
    ) {
        with(characteristic) {
            characteristicWrist = characteristic
            Log.i("WristGattCallback","Characteristic $uuid changed | value: ${value.toHexString()}")
            Initializer.getInstance().mainActivity.runOnUiThread {
//                if((characteristic.uuid == UUID.fromString(ECG_NEGATIVE_STRING)) && !ecgTest){
//                    EcgSec = 1
//                }
                when (characteristic.uuid) { // check and identfity characterstic each time it is caleld
                    UUID.fromString(HEART_RATE_MEASUREMENT_UUID_STRING) -> heartRateMeasurement.value =
                        characteristic.value?.toHexString().toString().toHeartRate()
                    UUID.fromString(R_R_UUID_STRING) -> rr.value =
                        characteristic.value?.toHexString().toString().toRr()
                    UUID.fromString(BODY_SENSOR_LOCATION_UUID_STRING) -> bodySensorLocation.value =
                        characteristic.value?.toHexString().toString().toSensorLocation()
                    UUID.fromString(TEMPERATURE_UUID_STRING) -> temperature.value =
                        characteristic.value?.toHexString().toString().toTemperature()
                    UUID.fromString(SPO2_UUID_STRING) -> sp02.value =
                        characteristic.value?.toHexString().toString().toSpo2()
                    UUID.fromString(ECG_POSITIVE_STRING) -> ecg.value =
                        characteristic.value?.toHexString().toString().toEcg()
                    UUID.fromString(ECG_NEGATIVE_STRING)-> ecgNeg.value =
                        characteristic.value?.toHexString().toString().toNegEcg()
                }
            }

        }
        if(flagAnkle){
            checkAnkle()
        }else{
            connectToDevices2()
        }

    }

    override fun onDescriptorWrite(
        gatt: BluetoothGatt,
        descriptor: BluetoothGattDescriptor,
        status: Int
    ) {
        with(descriptor) {
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    Log.i("WristGattCallback","Wrote to descriptor $uuid | value: ${value.toHexString()}")

                    if (isCccd()) {
                        onCccdWrite(gatt, value, characteristic)
                    } else {
                        return
                    }
                }
                BluetoothGatt.GATT_WRITE_NOT_PERMITTED -> {
                    Log.i("WristGattCallback","Write not permitted for $uuid!")
                }
                else -> {
                    Log.i("WristGattCallback","Descriptor write failed for $uuid, error: $status")
                }
            }
        }

        if (descriptor.isCccd() &&
            (ConnectionManager.pendingOperation is EnableNotifications || ConnectionManager.pendingOperation is DisableNotifications)
        ) {
            ConnectionManager.signalEndOfOperation()
        } else if (!descriptor.isCccd() && ConnectionManager.pendingOperation is DescriptorWrite) {
            ConnectionManager.signalEndOfOperation()
        }
    }

    private fun onCccdWrite(
        gatt: BluetoothGatt,
        value: ByteArray,
        characteristic: BluetoothGattCharacteristic
    ) {
        val charUuid = characteristic.uuid
        val notificationsEnabled =
            value.contentEquals(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE) ||
                    value.contentEquals(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE)
        val notificationsDisabled =
            value.contentEquals(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE)

        when {
            notificationsEnabled -> {
                Log.i("WristGattCallback","Notifications or indications ENABLED on $charUuid")
            }
            notificationsDisabled -> {
                Log.i("WristGattCallback","Notifications or indications DISABLED on $charUuid")
            }
            else -> {
                Log.i("WristGattCallback", "Unexpected value ${value.toHexString()} on CCCD of $charUuid")
            }
        }
    }

    private fun getCharacteristic( // we pass service and char UUID to this function each when they are discovered to get the value
        gatt: BluetoothGatt,
        serviceUUIDString: String,
        characteristicUUIDString: String
    ): BluetoothGattCharacteristic? {
        return gatt.getService(UUID.fromString(serviceUUIDString))
            ?.getCharacteristic(UUID.fromString(characteristicUUIDString))
    }

    private fun String.toHeartRate(): String {

        val v1 = this.drop(2)
        val i = v1.indexOfFirst { it == ' ' }
        val v2 = v1.substring(i + 1 until i + 3)
        EcgSec = 0

        return Integer.parseInt(v2, 16).toString()
    }

    private fun String.toRr(): String {
        val v1 = this.drop(2)
        val v2 = v1.split(' ').reversed()
        var v3 = ""
        v2.forEach {
            v3 += it
        }
        return Integer.parseInt(v3, 16).toString()
    }

    private fun String.toSensorLocation(): String = Integer.parseInt(this.drop(2), 16).toString()

    private fun String.toTemperature(): String {
        val v1 = this.drop(2)
        val v2 = v1.split(' ').reversed()
        var v3 = ""
        v2.forEach {
            v3 += it
        }
        return (Integer.parseInt(v3, 16) / 100.0).toString()
    }


    private fun String.toSpo2(): String {
        val v1 = this.drop(2)
        val v2 = v1.split(' ')
        println(v2)
        var v3 = v2[0]
        return Integer.parseInt(v3, 16).toString()
    }

    private fun String.toEcg(): String {
        val v1 = this.drop(2)
        val v2 = v1.split(' ').reversed()
        var v3 = ""
        v2.forEach {
            v3 += it
        }
        return Integer.parseInt(v3, 16).inv().toString()
    }

    private fun String.toNegEcg(): String {
//        if(EcgSec ==1){
//            Toast.makeText(ToastThis, "Wait 15 seconds", Toast.LENGTH_LONG).show()
//            ecgTest = true
//        }
//        EcgSec =0
        val v1 = this.drop(2)
        val v2 = v1.split(' ').reversed()
        var v3 = ""
        v2.forEach{
            v3 += it
        }
        var negecg: Int = ((Integer.parseInt(v3, 16)) * (-1))

        return negecg.toString()

    }


    companion object {
        private var INSTANCE: WristGattCallback? = null
        private lateinit var characteristicWrist: BluetoothGattCharacteristic


        fun initInstance() {
            if (INSTANCE == null) {
                INSTANCE = WristGattCallback()
            }
        }

        fun getInstance(): WristGattCallback {
            return INSTANCE ?: throw IllegalAccessException("WristGattCallback must be initialized")
        }
    }
}