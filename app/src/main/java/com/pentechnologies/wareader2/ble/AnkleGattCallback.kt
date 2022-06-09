package com.pentechnologies.wareader2.ble


import android.annotation.SuppressLint
import android.bluetooth.*
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.pentechnologies.wareader2.Initializer
import com.pentechnologies.wareader2.MainActivity
import com.pentechnologies.wareader2.MainActivity.Companion.checkWrist
import com.pentechnologies.wareader2.MainActivity.Companion.connectToDevices2
import com.pentechnologies.wareader2.MainActivity.Companion.flagAnkle
import com.pentechnologies.wareader2.MainActivity.Companion.flagWrist
import com.pentechnologies.wareader2.MainActivity.Companion.gattAnkle
import com.punchthrough.blestarterappandroid.ble.*
import java.util.*


//Temp sensor for ankle
private const val TEMPERATURE_SERVICE_UUID_STRING = "00001810-0000-1000-8000-00805f9b34fb"
private const val TEMPERATURE_UUID_STRING = "00002a66-0000-1000-8000-00805f9b34fb"
//Step service from IMU
private const val STEPS_SERVICE_UUID_STRING = "00001814-0000-1000-8000-00805f9b34fb"
private const val STEPS_UUID_STRING = "7abdb551-a0f0-444d-8fb0-96356e2d212b"
private const val METERS_UUID_STRING = "e1caf428-19b8-4f36-80e6-9d4b4524cc96"
//GPS Service
private const val GPS_SERVICE_UUID_STRING = "00001819-0000-1000-8000-00805f9b34fb"
private const val LATITUDE_UUID_STRING = "00002aae-0000-1000-8000-00805f9b34fb"
private const val LONGITUDE_UUID_STRING = "00002aaf-0000-1000-8000-00805f9b34fb"
private const val SIV_UUID_STRING = "f6ef9d90-1c28-4ddb-a31b-ad9680469e50"

//To calcuate the meters from the steps
var stepsIs: Int = 0
var forMeters: Float = 0.0F
var meterFlag: Boolean = false
var counterMeter: Int = 0

class AnkleGattCallback private constructor() :
   BluetoothGattCallback() {

    val ankleTemperatureMeas: MutableLiveData<String> = MutableLiveData<String>()
    val stepsMeas: MutableLiveData<String> = MutableLiveData<String>()
    val metersMeas: MutableLiveData<String> = MutableLiveData<String>()
    val latMeas: MutableLiveData<String> = MutableLiveData<String>()
    val longMeas: MutableLiveData<String> = MutableLiveData<String>()
    val sivMeas: MutableLiveData<String> = MutableLiveData<String>()


    @SuppressLint("MissingPermission")
    override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
        val deviceAddress = gatt.device.address
        gattAnkle = gatt
        if (status == BluetoothGatt.GATT_SUCCESS) {

            if (newState == BluetoothProfile.STATE_CONNECTED) {
               flagAnkle = true
                Log.i("AnkleGatt", "onConnectionStateChange: connected to $deviceAddress")

                ConnectionManagerAnkle.deviceGattMap[gatt.device] = gatt
                Handler(Looper.getMainLooper()).post {
                    gatt.discoverServices()
                }
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {

                Log.i("AnkleGatt", "onConnectionStateChange: disconnected from $deviceAddress")
                flagAnkle = false
                ConnectionManagerAnkle.teardownConnection(gatt.device)
            }
        } else {
//            Log.i(
//                "AnkleGattCallBack",
//                "onConnectionStateChange: status $status encountered for $deviceAddress!"
//            )
            if (ConnectionManagerAnkle.pendingOperation is Connect) {
                ConnectionManagerAnkle.signalEndOfOperation()
            }
            ConnectionManagerAnkle.teardownConnection(gatt.device)
        }
    }
    //get the services of the device
    override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
        if (status == BluetoothGatt.GATT_SUCCESS) {


            Log.i("AnkleGatt", "Discovered ${gatt.services.size} services for ${gatt.device.address}")
            gatt.printGattTable()

            // the following code get all characteristic from the device

            //temperature service characteristic
            val ankleTemp = getCharacteristic(
                gatt,
                TEMPERATURE_SERVICE_UUID_STRING,
                TEMPERATURE_UUID_STRING
            )
            //gps service characteristic
            val ankleLat = getCharacteristic(
                gatt,
                GPS_SERVICE_UUID_STRING,
                LATITUDE_UUID_STRING
            )
            val ankleLong = getCharacteristic(
                gatt,
                GPS_SERVICE_UUID_STRING,
                LONGITUDE_UUID_STRING
            )
            val ankleSiv = getCharacteristic(
                gatt,
                GPS_SERVICE_UUID_STRING,
                SIV_UUID_STRING
            )
            //steps services
            val ankleSteps = getCharacteristic(
                gatt,
                STEPS_SERVICE_UUID_STRING,
                STEPS_UUID_STRING
            )
            val ankleMeters = getCharacteristic(
                gatt,
                STEPS_SERVICE_UUID_STRING,
                METERS_UUID_STRING
            )


            //Temperature
            if(ankleTemp != null){
                ConnectionManagerAnkle.readCharacteristic(gatt.device, ankleTemp) // read it
                ConnectionManagerAnkle.enableNotification(gatt.device, ankleTemp) // enable its notification
            }

            //Gps Lat-Long-SIV
            if(ankleLat != null){
                ConnectionManagerAnkle.readCharacteristic(gatt.device, ankleLat) // read it
                ConnectionManagerAnkle.enableNotification(gatt.device, ankleLat) // enable its notification
            }
            if(ankleLong != null){
                ConnectionManagerAnkle.readCharacteristic(gatt.device, ankleLong) // read it
                ConnectionManagerAnkle.enableNotification(gatt.device, ankleLong) // enable its notification
            }
            if(ankleSiv != null){
                ConnectionManagerAnkle.readCharacteristic(gatt.device, ankleSiv) // read it
                ConnectionManagerAnkle.enableNotification(gatt.device, ankleSiv) // enable its notification
            }
            //Steps n meters
            if(ankleSteps != null){
                ConnectionManagerAnkle.readCharacteristic(gatt.device, ankleSteps) // read it
                ConnectionManagerAnkle.enableNotification(gatt.device, ankleSteps) // enable its notification
            }
            if(ankleMeters != null){
                ConnectionManagerAnkle.readCharacteristic(gatt.device, ankleMeters) // read it
                ConnectionManagerAnkle.enableNotification(gatt.device, ankleMeters) // enable its notification
            }



        } else {
            Log.i("AnkleGatt", "Service discovery failed due to status $status")
            ConnectionManagerAnkle.teardownConnection(gatt.device)

        }

        if (ConnectionManagerAnkle.pendingOperation is Connect) {
            ConnectionManagerAnkle.signalEndOfOperation()
        }
    }
    //read the characteristics

    override fun onCharacteristicRead(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        status: Int
    ) {
        with(characteristic) {
            when (status) {

                BluetoothGatt.GATT_SUCCESS -> {


                    Log.i("AnkleGatt", "Read characteristic $uuid | value: ${value.toHexString()}")
                    Initializer.getInstance2().mainActivity.runOnUiThread {
                        when (characteristic.uuid) { // check and identify characteristic each time it is called

                            UUID.fromString(TEMPERATURE_UUID_STRING) -> ankleTemperatureMeas.value =
                                characteristic.value?.toHexString().toString().toTemperature()
                            UUID.fromString(STEPS_UUID_STRING) -> stepsMeas.value =
                                characteristic.value?.toHexString().toString().toSteps()
                            UUID.fromString(METERS_UUID_STRING) -> metersMeas.value =
                                characteristic.value?.toHexString().toString().toMeters()
                            UUID.fromString(LATITUDE_UUID_STRING) -> latMeas.value =
                                characteristic.value?.toHexString().toString().toGPS()
                            UUID.fromString(LONGITUDE_UUID_STRING) -> longMeas.value =
                                characteristic.value?.toHexString().toString().toGPS()
                            UUID.fromString(SIV_UUID_STRING) -> sivMeas.value =
                                characteristic.value?.toHexString().toString().toSteps()

                        }
                    }
                }
                BluetoothGatt.GATT_READ_NOT_PERMITTED -> {

                    Log.i("AnkleGatt", "Read not permitted for $uuid")
                }
                else -> {
                   // Log.i("AnkleGattCallback","Characteristic read failed for $uuid, error: $status")
                }
            }
        }

        if (ConnectionManagerAnkle.pendingOperation is CharacteristicRead) {
            ConnectionManagerAnkle.signalEndOfOperation()
        }
    }
    //everytime a characteristic changes, read it again

    override fun onCharacteristicChanged(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic
    ) {
        with(characteristic) {
            characteristicAnkle = characteristic
            Log.i("AnkleGatt", "Characteristic $uuid changed | value:${value.toHexString()} ")

            Initializer.getInstance2().mainActivity.runOnUiThread {
                when (characteristic.uuid) { // check and identify characteristic each time it is called

                    UUID.fromString(TEMPERATURE_UUID_STRING) -> ankleTemperatureMeas.value =
                        characteristic.value?.toHexString().toString().toTemperature()
                    UUID.fromString(STEPS_UUID_STRING) -> stepsMeas.value =
                        characteristic.value?.toHexString().toString().toSteps()
                    UUID.fromString(METERS_UUID_STRING) -> metersMeas.value =
                        characteristic.value?.toHexString().toString().toMeters()
                    UUID.fromString(LATITUDE_UUID_STRING) -> latMeas.value =
                        characteristic.value?.toHexString().toString().toGPS()
                    UUID.fromString(LONGITUDE_UUID_STRING) -> longMeas.value =
                        characteristic.value?.toHexString().toString().toGPS()
                    UUID.fromString(SIV_UUID_STRING) -> sivMeas.value =
                        characteristic.value?.toHexString().toString().toSteps()
                }
            }
        }
     //checkWrist()

        if(flagWrist){
            checkWrist()
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
                    Log.i("AnkleGattCallback","Wrote to descriptor $uuid | value: ${value.toHexString()}")

                    if (isCccd()) {
                        onCccdWrite(gatt, value, characteristic)
                    } else {
                        return
                    }
                }
                BluetoothGatt.GATT_WRITE_NOT_PERMITTED -> {
                    Log.i("AnkleGattCallback","Write not permitted for $uuid!")
                }
                else -> {
                    Log.i("AnkleGattCallback","Descriptor write failed for $uuid, error: $status")
                }
            }
        }

        if (descriptor.isCccd() &&
            (ConnectionManagerAnkle.pendingOperation is EnableNotifications || ConnectionManager.pendingOperation is DisableNotifications)
        ) {
            ConnectionManagerAnkle.signalEndOfOperation()
        } else if (!descriptor.isCccd() && ConnectionManager.pendingOperation is DescriptorWrite) {
            ConnectionManagerAnkle.signalEndOfOperation()
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
                Log.i("AnkleGattCallback","Notifications or indications ENABLED on $charUuid")
            }
            notificationsDisabled -> {
                Log.i("AnkleGattCallback","Notifications or indications DISABLED on $charUuid")
            }
            else -> {
                Log.i("AnkleGattCallback", "Unexpected value ${value.toHexString()} on CCCD of $charUuid")
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

    private fun String.toTemperature(): String {
        val v1 = this.drop(2)
        val v2 = v1.split(' ').reversed()
        var v3 = ""
        v2.forEach {
            v3 += it
        }
        return (Integer.parseInt(v3, 16) / 100.0).toString()
    }
    private fun String.toGPS(): String {
        val v1 = this.drop(2)
        val v2 = v1.split(' ').reversed()
        var v3 = ""
        v2.forEach {
            v3 += it
        }
        return (Integer.parseInt(v3, 16)/ 10000000.0).toString()
    }

// stopped on 14.04.2022
 /*   private fun String.toMeters(): String {
        val v1 = this.drop(2)
        val v2 = v1.split(' ').reversed()
        var v3 = ""
        v2.forEach {
            v3 += it
        }
        return (Integer.parseInt(v3, 16)).inv().toString()
    }*/


    private fun String.toMeters(): String {
        val v1 = this.drop(2)
        val v2 = v1.split(' ').reversed()
        var v3 = ""
        v2.forEach {
            v3 += it
        }
        if(meterFlag){
            forMeters = (counterMeter * 7.62).toFloat()
        }
        // return (Integer.parseInt(v3, 16)).inv().toString()
        return forMeters.toString()
        // return (Integer.parseInt(v3, 16)).inv().toString()
    }


    private fun String.toSteps(): String {
        val v1 = this.drop(2)
        val v2 = v1.split(' ').reversed()
        var v3 = ""
        v2.forEach {
            v3 += it
        }
        stepsIs = Integer.parseInt(v3, 16)
        if(stepsIs >1) {
            if (stepsIs % 10 == 0) {
                meterFlag = true
                counterMeter = stepsIs / 10
            }else{
                meterFlag = false
            }
        }
        //  return (Integer.parseInt(v3, 16)).toString()
        return stepsIs.toString()
        // return (Integer.parseInt(v3, 16)).toString()

    }
   /* private fun String.toSteps(): String {
        val v1 = this.drop(2)
        val v2 = v1.split(' ').reversed()
        var v3 = ""
        v2.forEach {
            v3 += it
        }
        return (Integer.parseInt(v3, 16)).toString()

    }*/





    companion object {
        private var INSTANCE2: AnkleGattCallback? = null

      //  private var characteristicsAnkle = arrayOf(BluetoothGattCharacteristic)
      private lateinit var characteristicAnkle: BluetoothGattCharacteristic

        fun initInstance2() {
            if (INSTANCE2 == null) {
                INSTANCE2 = AnkleGattCallback()
            }
        }

        fun getInstance2(): AnkleGattCallback {
            return INSTANCE2?: throw IllegalAccessException("AnkleGattCallback must be initialized")
        }


    }
}