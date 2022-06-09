package com.pentechnologies.wareader2

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Toast
import com.github.mikephil.charting.charts.Chart
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.storage.UploadTask
import com.pentechnologies.wareader2.db.*
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

object Utils {

    public fun getHeartList(list: List<HeartRate>): List<HeartRate>{
        val l = arrayListOf<HeartRate>()
        if (list.isEmpty()){
            l.add(HeartRate(0,"","","",""))
        }else{
            l.addAll(list)
        }
        return l
    }

    fun getEcgList(list: List<Ecg>): List<Ecg>{
        val l = arrayListOf<Ecg>()
        if (list.isEmpty()){
            l.add(Ecg(0,"","",""))
        }else{
            l.addAll(list)
        }
        return l
    }

    fun getTempList(list: List<Temperature>): List<Temperature>{
        val l = arrayListOf<Temperature>()
        if (list.isEmpty()){
            l.add(Temperature(0,"","",""))
        }else{
            l.addAll(list)
        }
        return l
    }

    fun getSp02List(list: List<SpO2>): List<SpO2>{
        val l = arrayListOf<SpO2>()
        if (list.isEmpty()){
            l.add(SpO2(0,"","",""))
        }else{
            l.addAll(list)
        }
        return l
    }

    fun getAnkleList(list: List<Ankle>): List<Ankle>{
        val l = arrayListOf<Ankle>()
        if (list.isEmpty()){
            l.add(Ankle(0,"","",""))
        }else{
            l.addAll(list)
        }
        return l
    }

    fun getSteps(list: List<Steps>): List<Steps>{
        val l = arrayListOf<Steps>()
        if (list.isEmpty()){
            l.add(Steps(0,"","","",""))
        }else{
            l.addAll(list)
        }
        return l
    }

    fun getGPS(list: List<Gps>): List<Gps>{
        val l = arrayListOf<Gps>()
        if (list.isEmpty()){
            l.add(Gps(0,"0.0","0.0","",""))
        }else{
            l.addAll(list)
        }
        return l
    }

    fun getTodayDate(): String {
        val date = Date(System.currentTimeMillis())
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
        return dateFormat.format(date)
    }

    fun getTime(): String {
        val date = Date(System.currentTimeMillis())
        val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.ENGLISH)
        //val dateFormat = SimpleDateFormat("HH:mm:ss MM-dd-yy", Locale.ENGLISH)
        return dateFormat.format(date)
    }

    fun createSet(): LineDataSet? {
        /* val set = LineDataSet(null, "Dynamic Data")
         set.axisDependency = AxisDependency.LEFT
         set.color = ColorTemplate.getHoloBlue()
         set.setCircleColor(Color.WHITE)
         set.lineWidth = 2f
         set.circleRadius = 0f
         set.fillAlpha = 65
         set.fillColor = ColorTemplate.getHoloBlue()
         set.highLightColor = Color.rgb(244, 117, 117)
         set.valueTextColor = Color.WHITE
         set.valueTextSize = 6f
         set.setDrawValues(false)
         set.setDrawCircleHole(false)*/


        // create a dataset and give it a type
        val set1 = LineDataSet(null, "Ecg Graph")
        set1.axisDependency = YAxis.AxisDependency.LEFT
        set1.color = ColorTemplate.getHoloBlue()
        set1.valueTextColor = ColorTemplate.getHoloBlue()
        set1.lineWidth = 1.5f
        set1.setDrawCircles(false)
        set1.setDrawValues(false)
        set1.fillAlpha = 65
        set1.fillColor = ColorTemplate.getHoloBlue()
        set1.highLightColor = Color.rgb(244, 117, 117)
        set1.setDrawCircleHole(false)


        return set1
    }


    fun createHSet(): LineDataSet? {

        val set1 = LineDataSet(null, "Heart Rate Graph")
        set1.axisDependency = YAxis.AxisDependency.LEFT
        set1.color = ColorTemplate.getHoloBlue()
        set1.valueTextColor = ColorTemplate.getHoloBlue()
        set1.lineWidth = 1.2f
        set1.setDrawCircles(false)
        set1.setDrawValues(false)
        set1.fillAlpha = 65
        set1.fillColor = ColorTemplate.getHoloBlue()
        set1.highLightColor = Color.rgb(183, 63, 69)
        set1.setDrawCircleHole(false)


        return set1
    }

    fun getRand():String {
        val rand = (-3000000..1500000).random()
        return rand.toString()
    }

    fun getRandH():String {
        val rand = (30..200).random()
        return rand.toString()
    }



    fun getTimeStand(longDate: Long): String {
        val date = Date(longDate)
        val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.ENGLISH)
        //val dateFormat = SimpleDateFormat("HH:mm:ss MM-dd-yy", Locale.ENGLISH)
        return dateFormat.format(date)
    }

    fun getDateTimeStand(): String {
        val date = Date(System.currentTimeMillis())
        //val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.ENGLISH)
        val dateFormat = SimpleDateFormat("MM-dd-yy_HH:mm:ss", Locale.ENGLISH)
        return dateFormat.format(date)
    }

    fun saveToGallery(chart: Chart<*>, name: String, context: Context):String {
        var fName= ""
        val fileName = name+ "_"+getDateTimeStand()
        if (chart.saveToGallery(fileName, 70)){

            val extBaseDir = Environment.getExternalStorageDirectory()
            val file = File(extBaseDir.absolutePath + "/DCIM/" + fileName+".png")

            Log.e("file: ", file.absolutePath + " File: "+ file.exists().toString())
            fName = file.absolutePath

            return fName
        }

        return fName

    }

}