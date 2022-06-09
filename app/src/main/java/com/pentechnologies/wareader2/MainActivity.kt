package com.pentechnologies.wareader2

import android.Manifest
import android.app.ProgressDialog
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothProfile
import android.bluetooth.BluetoothProfile.GATT
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend.LegendForm
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.OnProgressListener
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.pentechnologies.wareader2.Utils.createSet
import com.pentechnologies.wareader2.Utils.getRand
import com.pentechnologies.wareader2.Utils.getRandH
import com.pentechnologies.wareader2.Utils.getTime
import com.pentechnologies.wareader2.Utils.getTodayDate
import com.pentechnologies.wareader2.Utils.saveToGallery
import com.pentechnologies.wareader2.ble.AnkleGattCallback
import com.pentechnologies.wareader2.ble.ConnectionManager
import com.pentechnologies.wareader2.ble.ConnectionManagerAnkle
import com.pentechnologies.wareader2.ble.WristGattCallback
import com.pentechnologies.wareader2.db.Ecg
import com.pentechnologies.wareader2.db.HeartRate
import com.pentechnologies.wareader2.db.database.AppDatabase
import com.pentechnologies.wareader2.db.database.DatabaseBuilder
import com.pentechnologies.wareader2.drive.FilesFunctions
import kotlinx.android.synthetic.main.activity_main.*
import org.library.worksheet.cellstyles.CellEnum
import org.library.worksheet.cellstyles.WorkSheet
import java.io.File
import java.io.IOException
import java.util.*
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity() {
    private val STORAGE_PERMISSION_REQUEST_CODE: Int = 1212
    private lateinit var initializerW: Initializer //for wrist
    private lateinit var initializerA: Initializer //for ankle
    private lateinit var connectionAnkle: ConnectionManagerAnkle
    private lateinit var connectionWrist: ConnectionManager

    val mainViewModel: MainViewModel by viewModels()
    val mainActivityModel: MainActivityModel by viewModels()
    val mainActivityModelAnkle: MainActivityModelAnkle by viewModels()
    var chart: LineChart? = null
    var chartHeart: LineChart? = null
    var storage: FirebaseStorage? = null
    var storageReference: StorageReference? = null

    companion object {
        var flagWrist: Boolean = false
        var flagAnkle: Boolean = false
        lateinit var gattAnkle: BluetoothGatt
        lateinit var gattWrist: BluetoothGatt
        private lateinit var anklegatt: AnkleGattCallback
        private lateinit var wristgatt: WristGattCallback
        lateinit var characteristicWrist: BluetoothGattCharacteristic
        lateinit var characteristicAnkle: BluetoothGattCharacteristic
        lateinit var ToastThis: AppCompatActivity
        private lateinit var initializerWrist: Initializer //for wrist
        private lateinit var initializerAnkle: Initializer //for ankle
        lateinit var appDatabase: AppDatabase
        var testWrist: Boolean = false

        //If we finished onCharacteristicChanged from AnkleGattCallback check the characteristics of the Wrist
        fun checkWrist() {
            wristgatt.onCharacteristicChanged(gattWrist, characteristicWrist)
            try {
                Thread.sleep(2)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }


        }

        //if we finished onCharacteristicChanged from WristGattCallback check the characteristics of the Ankle
        fun checkAnkle() {
            anklegatt.onCharacteristicChanged(gattAnkle, characteristicAnkle)

            try {
                Thread.sleep(2)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }

        //check which device is not connected and get to Initializer
        fun connectToDevices2() {
            //if ankle device is not connected go to Initializer to start scan
            if (!flagAnkle) {

                initializerAnkle.start2()

            }
            // if wrist device is not connected go to Initializer to start scan
            if (!flagWrist) {

                initializerWrist.start()

            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        chart = findViewById(R.id.lc_chart)
        chartHeart = findViewById(R.id.lc_chart_heart)
        //initialize the graphs ( position, color, size etc)
        init()
      // init2()
        //get variables initializerW and initializerA for connection with devices
        Initializer.initInstance(this)
        Initializer.initInstance2(this)
        initializerW = Initializer.getInstance()
        initializerWrist = initializerW
        initializerA = Initializer.getInstance2()
        initializerAnkle = initializerA
        //variables for use of connectionManagers
        connectionAnkle = ConnectionManagerAnkle
        connectionWrist = ConnectionManager
        //initialize the firebase database
        storage = FirebaseStorage.getInstance()
        storageReference = storage?.reference
        appDatabase = DatabaseBuilder.getInstance(applicationContext)

        //check permission
        checkStoragePermission()
        //if button "Save" is pressed do the following
        ll_ecg.setOnClickListener {
            //save the picture to the DCIM of phone
            val file = saveToGallery(chart!!,"ECG",applicationContext)
            //If picture exists, upload it to Firebase via fileUpload() function
            if(!file.equals("")){
                val progressDialog = ProgressDialog(this)
                progressDialog.setTitle("Uploading...")
                progressDialog.show()

                fileUpload(File(file), progressDialog,"EcgImages")
              //  init()
            }else{
                Toast.makeText(applicationContext, "No Data available", Toast.LENGTH_LONG)
                    .show()
            }
        }
        //if button for upload is pressed
        viewSheetBT.setOnClickListener {

            // mainViewModel.fetchUsers(appDatabase)
            val progressDialog = ProgressDialog(this)
            progressDialog.setTitle("Uploading...")
            progressDialog.show()
            //get the data of wrist and ankle device saved on local database
            mainViewModel.fetchHeartRate(appDatabase)
            mainViewModel.fetchAnkle(appDatabase)
            //model with list of data for Wrist dev we got from observer
            mainViewModel.heart.observe(this, androidx.lifecycle.Observer {

                val list: List<Map<String, List<*>>> = it as List<Map<String, List<*>>>
                if (list.size > 0) { //if there is at least one value sent from wrist dev
                    // check through object FilesFunctions if excel was created for Wrist dev
                    val file = if (FilesFunctions.isTodayFileExists(this)) {
                        FilesFunctions.todayFile(this)
                    } else {
                        FilesFunctions.createNewFile(this)
                    }

                    try {

                        val data = WorkSheet.Builder(getApplicationContext(), file.name)
                            .title(CellEnum.TEAL_HEADER)
                            .header(CellEnum.FORMULA_1)
                            .cell(CellEnum.DEFAULT_CELL)
                            .setSheets(list)
                            .writeSheets()
                        //call the fileUload function to upload the wrist dev excels
                        fileUpload(file, progressDialog,"excels")
                        Log.i("file",data.getpath() + " : "+file.name)

                    } catch (e: IOException) {
                        e.printStackTrace();
                        cProgress.visibility = View.GONE
                    }
                } else { //if we got no values yet (impossible we have initial values, something wrong with connectivity)
                    progressDialog.dismiss()
                    cProgress.visibility = View.GONE
                    Toast.makeText(applicationContext, "No Data available", Toast.LENGTH_LONG)
                        .show()
                }
            })
            //model with list of data for ankle dev we got from observer
            mainViewModel.ankle.observe(this, androidx.lifecycle.Observer {
                val list: List<Map<String, List<*>>> = it as List<Map<String, List<*>>>
                if (list.size > 0) { //if there is at least one value sent from ankle dev
                    //check through FilesFunction object if excel was created for today
                    val file = if (FilesFunctions.isTodayFileExistsAnkle(this)) {
                        FilesFunctions.todayFileAnkle(this)
                    } else {
                        FilesFunctions.createNewFileAnkle(this)
                    }

                    try {
                        val data = WorkSheet.Builder(getApplicationContext(), file.name)
                            .title(CellEnum.TEAL_HEADER)
                            .header(CellEnum.FORMULA_1)
                            .cell(CellEnum.DEFAULT_CELL)
                            .setSheets(list)
                            .writeSheets()
                        //call fileUpload function to upload excel
                        fileUpload(file, progressDialog,"excels")
                        Log.i("file",data.getpath() + " : "+file.name)
                    } catch (e: IOException) {
                        e.printStackTrace();
                        cProgress.visibility = View.GONE
                    }
                } else { // if no value has been sent from ankle dev
                    progressDialog.dismiss()
                    cProgress.visibility = View.GONE
                    Toast.makeText(applicationContext, "No Data available", Toast.LENGTH_LONG)
                        .show()
                }
            })
        }
    }

    fun init() {

        chart!!.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry, h: Highlight) {
                Log.i("Entry selected", e.toString())
            }

            override fun onNothingSelected() {
                Log.i("Nothing selected", "Nothing selected.")
            }
        })
        chart!!.description.isEnabled = false
        chart!!.setTouchEnabled(false)
        chart!!.isDragEnabled = true
        chart!!.setScaleEnabled(true)
        chart!!.setDrawGridBackground(false)
        chart!!.setPinchZoom(true)
        chart!!.setBackgroundColor(Color.LTGRAY)

        val data = LineData()
        data.setValueTextColor(Color.WHITE)

        chart!!.data = data
        val l = chart!!.legend

        l.form = LegendForm.LINE
        l.textColor = Color.WHITE

        val xl = chart!!.xAxis
        xl.textColor = Color.WHITE
        xl.setDrawGridLines(false)
        xl.setAvoidFirstLastClipping(true)
        xl.isEnabled = true

        val leftAxis = chart!!.axisLeft
        //leftAxis.typeface = tfLight
        leftAxis.textColor = Color.WHITE
        leftAxis.axisMaximum = 100000f
        leftAxis.axisMinimum = -1550000f
        leftAxis.setDrawGridLines(true)

        val rightAxis = chart!!.axisRight
        rightAxis.isEnabled = false


        chartHeart!!.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry, h: Highlight) {
                Log.i("Entry selected", e.toString())
            }

            override fun onNothingSelected() {
                Log.i("Nothing selected", "Nothing selected.")
            }
        })
        chartHeart!!.description.isEnabled = false
        chartHeart!!.setTouchEnabled(false)
        chartHeart!!.isDragEnabled = true
        chartHeart!!.setScaleEnabled(true)
        chartHeart!!.setDrawGridBackground(false)
        chartHeart!!.setPinchZoom(true)
        chartHeart!!.setBackgroundColor(Color.LTGRAY)

        val data1 = LineData()
        data.setValueTextColor(Color.WHITE)
        chartHeart!!.data = data1
        val l1 = chartHeart!!.legend
        l1.form = LegendForm.LINE
        l1.textColor = Color.WHITE
        val xl1 = chartHeart!!.xAxis
        xl1.textColor = Color.WHITE
        xl1.setDrawGridLines(false)
        xl1.setAvoidFirstLastClipping(true)
        xl1.isEnabled = true

        val leftAxis1 = chartHeart!!.axisLeft
        //leftAxis.typeface = tfLight
        leftAxis1.textColor = Color.WHITE
        leftAxis1.axisMaximum = 170f
        leftAxis1.axisMinimum = 30f
        leftAxis1.setDrawGridLines(true)

        val rightAxis1 = chartHeart!!.axisRight
        rightAxis1.isEnabled = false
    }

    override fun onResume() {
        super.onResume()

        //check if any devices is connected to the app
        try {
            if (!(flagAnkle && flagWrist)) {
                connectToDevices()      //call connectToDevices function to start
            }
//            if(flagWrist){
//
//                    wristgatt.onConnectionStateChange(gattWrist, BluetoothGatt.GATT_SUCCESS, BluetoothProfile.STATE_DISCONNECTED)
//                Toast.makeText(this,"Wrist Device disconnected. Restart the App", Toast.LENGTH_LONG).show()
//            }


        } catch (e: Exception) {
            Logger.log(this, "$e", null)
            Log.i("ConnectionManager", "Exception == 0 ")
        }

    }
    //check which device is not connected and get to Initializer
    private fun connectToDevices() {

        // if wrist device is not connected go to Initializer to start scan
        if (!flagWrist) {
            Toast.makeText(this, "We are trying to find  Wrist Device", Toast.LENGTH_LONG).show()
            ToastThis = this
            initializerW.start()
            waitIdle()
        }

        //if ankle device is not connected go to Initializer to start scan
        if (!flagAnkle) {
            Toast.makeText(this, "We are trying to find  Ankle Device", Toast.LENGTH_LONG).show()
            ToastThis = this
            initializerA.start2()
            waitIdle()
        }
    }

    private fun waitIdle() {
        Log.i("ConnectionManager", "We are on the Main activity 10ms delay")
        try {
            Thread.sleep(10)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }
// get the permission for internal storage
    private fun checkStoragePermission() {
        Dexter.withActivity(Objects.requireNonNull(this@MainActivity)).withPermissions(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ).withListener(object : MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                if (report.areAllPermissionsGranted()) {

                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "Please set the Storage permission image file",
                        Toast.LENGTH_LONG
                    ).show()
                    
                }
            }

            override fun onPermissionRationaleShouldBeShown(
                permissions: List<PermissionRequest>,
                token: PermissionToken
            ) {
                token.continuePermissionRequest()
            }
        }).check()
    }

    fun getApp(): AppDatabase {
        return appDatabase
    }

    fun addEcgEntry(y: Float) {
        val data = chart!!.data
        if (data != null) {
            var set = data.getDataSetByIndex(0)
            if (set == null) {
                set = createSet()
                data.addDataSet(set)
            }
            data.addEntry(Entry(set.entryCount.toFloat(), y), 0)
            data.notifyDataChanged()

            chart!!.notifyDataSetChanged()

            // limit the number of visible entries
            chart!!.setVisibleXRangeMaximum(1500000f)
            //chart!!.setVisibleXRangeMinimum(-3000000f)
            // move to the latest entry
            chart!!.moveViewToX(data.entryCount.toFloat())

        }
    }

    fun addHeartEntry(y: Float) {
        val data = chartHeart!!.data
        if (data != null) {
            var set = data.getDataSetByIndex(0)
            if (set == null) {
                set = Utils.createHSet()
                data.addDataSet(set)
            }
            data.addEntry(Entry(set.entryCount.toFloat(), y), 0)
            data.notifyDataChanged()

            chartHeart!!.notifyDataSetChanged()

            // limit the number of visible entries
            chartHeart!!.setVisibleXRangeMaximum(200f)
            chartHeart!!.setVisibleXRangeMinimum(30f)
            chartHeart!!.moveViewToX(data.entryCount.toFloat())

        }
    }

    fun fileUpload(file: File, progressDialog: ProgressDialog,type: String) {
        val child = storageReference?.child(getTodayDate())
        val ref = child?.child(type+"/" + file.name)
        ref?.putFile(Uri.fromFile(file))
            ?.addOnSuccessListener(OnSuccessListener<UploadTask.TaskSnapshot?> { // Image uploaded successfully
            // Show message to user that File upload was successful
                Toast.makeText(this@MainActivity, "File Uploaded!!", Toast.LENGTH_SHORT).show()
                cProgress.visibility = View.GONE
                progressDialog.dismiss()
            })
            ?.addOnFailureListener(OnFailureListener { e -> // Error, Image not uploaded
                // progressDialog.dismiss()
                //Show message to user that File upload was not successful
                Toast
                    .makeText(this@MainActivity,"Failed " + e.message,Toast.LENGTH_SHORT
                    )
                    .show()
                cProgress.visibility = View.GONE
                progressDialog.dismiss()
            })
            ?.addOnProgressListener(
                OnProgressListener<UploadTask.TaskSnapshot> { taskSnapshot ->

                    val p = (100.0
                            * taskSnapshot.bytesTransferred
                            / taskSnapshot.totalByteCount)

                    cProgress.progress = p.roundToInt()

                    // progressDialog.setMessage("Uploaded " + p.toInt() + "%")
                })

    }

}




