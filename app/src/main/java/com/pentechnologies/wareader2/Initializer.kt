package com.pentechnologies.wareader2

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.pentechnologies.wareader2.ble.ConnectionManager
import com.pentechnologies.wareader2.ble.ConnectionManagerAnkle
import com.pentechnologies.wareader2.drive.FilesFunctions
import jxl.Workbook
import jxl.write.*
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException


private const val WRIST_DEVICE_ADDRESS = "C2:67:90:AD:28:AC"
private const val ANKLE_DEVICE_ADDRESS = "AC:50:F1:CE:59:E6"


class Initializer private constructor(val mainActivity: MainActivity) {

    /*
    * This class handle all connection related works, when the device succesfuly connected
    * it transfer control to WristGattCallback class and AnkleGattCallback class which is used to discover services activate
    * notifications and read values.
    * THE ENTRY POINT TO THIS CLASS IS initConnection() method
    * */

    private val bluetoothEnableRequest =
        mainActivity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode != Activity.RESULT_OK) {
                promptBluetoothEnableRequest()
            }
        }
    private val requestLocationPermission =
        mainActivity.registerForActivityResult(ActivityResultContracts.RequestPermission()) {

        }
    private val bluetoothAdaptor: BluetoothAdapter by lazy {
        val bluetoothManager =
            mainActivity.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }
    private val bleScanner by lazy {
        bluetoothAdaptor.bluetoothLeScanner
    }
    private val scanSettings =
        ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()

    private val scanFilter = ScanFilter.Builder().setDeviceAddress(WRIST_DEVICE_ADDRESS).build()
    private val scanCallback = object : ScanCallback() {
        /*This object override onScanResult function from the ScanCallback that is called when a
        * device is found */
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            Log.i("ConInitializer", "Device found id: ${result?.device?.address}")
            /*When a device found check if it is the wrist device
            * then if it is a the wrist device with given MAC connect to that*/
            //if the result mac address is the Wrist device address
            if (result?.device?.address == WRIST_DEVICE_ADDRESS) {
                //save the device setting on the devWrist
                devWrist = result.device
                Logger.log(mainActivity, "Wrist device found", null)
                wristConnector = true
                //call connectWrist function to stop the scan and start the connection
                connectWrist(result.device) //connect to the wrist device we found
                waitIdle()
            }
            //If the result mac address is the Ankle device address
            if (result?.device?.address == ANKLE_DEVICE_ADDRESS) {
                //save the device settings on the devAnkle
                devAnkle = result.device
                Logger.log(mainActivity, "Ankle device found", null)
                ankleConnector = true
                //call connectAnkle function to stop the scan and start the connection
                connectAnkle(result.device)  // connect to the found ankle device
                waitIdle()

            }
        }
    }

    //Gets here from MainActivity , check for bluetooth and gps permissions
    fun start() {
        if (!bluetoothAdaptor.isEnabled) {
            promptBluetoothEnableRequest() // if bluetooth is not enabled, ask user to turn it on
        }
        if (ContextCompat.checkSelfPermission(
                mainActivity,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            promptRequestLocationPermission() // if location permission is not granted, ask it
        }
        // once bluetooth is on and connction is granted access start scan and find the device

        findWristDevice()  // find and connect the device

    }



    //Gets here from MainActivity , check for bluetooth and gps permissions
    fun start2() {
        if (!bluetoothAdaptor.isEnabled) {
            promptBluetoothEnableRequest() // if bluetooth is not enabled, ask user to turn it on
        }
        if (ContextCompat.checkSelfPermission(
                mainActivity,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            promptRequestLocationPermission() // if location permission is not granted, ask it
        }
        // once bluetooth is on and connction is granted access start scan and find the device

        findAnkleDevice()  // find and connect the device
    }

    private fun promptBluetoothEnableRequest() {
        bluetoothEnableRequest.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
    }

    private fun promptRequestLocationPermission() {
        requestLocationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    //Start the Scan to find the Wrist Device
    @SuppressLint("MissingPermission")
    private fun findWristDevice() {
        Logger.log(mainActivity, "finding wrist device...", null)

        bleScanner.startScan(null, scanSettings, scanCallback)  // this function start
        // bluetooth scan
        /*
        * To startScan function we pass scanSettings that define how scan will work
        * and we pass scanCallback object that is used to call a funtion when the a device found */
    }

    //Stop the Scan, and Connect to the Wrist device
    @SuppressLint("MissingPermission")
    private fun connectWrist(device: BluetoothDevice) {
        //This is called from the onScanResult when the result match the wrist device mac address
        bleScanner.stopScan(scanCallback) // first stop scanning

        ConnectionManager.connect(device, mainActivity)
        waitIdle()
    }

    //Start the Scan to find the Wrist Device
    @SuppressLint("MissingPermission")
    private fun findAnkleDevice() {

        bleScanner.startScan(null, scanSettings, scanCallback)    //this function start the bluetooth scan

    }

    //Stop the Scan, and Connect to the Wrist device
    @SuppressLint("MissingPermission")
    private fun connectAnkle(device: BluetoothDevice) {
        while(wristConnector) {
            bleScanner.stopScan(scanCallback)
            ConnectionManagerAnkle.connect(device, mainActivity)
        }

        waitIdle()
    }


    fun waitIdle() {

        try {
            Thread.sleep(13)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }


    }

    companion object {

        private var INSTANCE: Initializer? = null
        private var INSTANCE2: Initializer? = null
        private lateinit var devWrist: BluetoothDevice
        private lateinit var devAnkle: BluetoothDevice
        var wristConnector: Boolean = false
        var ankleConnector: Boolean = false

        fun initInstance(mainActivity: MainActivity) {
            if (INSTANCE == null) {
                INSTANCE = Initializer(mainActivity)
            }

        }

        fun initInstance2(mainActivity: MainActivity) {
            if (INSTANCE2 == null) {
                INSTANCE2 = Initializer(mainActivity)
            }
        }


        fun getInstance(): Initializer {
            return INSTANCE ?: throw IllegalAccessException("Connector must be initialized")
        }

        fun getInstance2(): Initializer {
            return INSTANCE2 ?: throw IllegalAccessException("Connector must be initialized")
        }
    }
}