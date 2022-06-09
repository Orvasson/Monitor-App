package com.pentechnologies.wareader2.drive

import android.content.Context
import android.os.Environment
import org.library.worksheet.storage.EMedia
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

object FilesFunctions {

    fun isTodayFileExists(context:Context): Boolean{
        val excelFileName =
            "HearRate" + getDateFromLong(System.currentTimeMillis()) + ".xls"
        val storageDir = File(Environment.getExternalStoragePublicDirectory(EMedia.DEFAULT_EXTERNAL_FILE_DIRECTORY).absolutePath)
        //val storageDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "WAReader")

        //val storageDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "WAReader")
        if (!storageDir.exists()) {
            storageDir.mkdir()
        }
        val excel = File(storageDir, excelFileName)
        return excel.exists()
    }

    fun todayFile(context: Context): File {
        val excelFileName =
            "HearRate" + getDateFromLong(System.currentTimeMillis()) + ".xls"
        val storageDir = File(Environment.getExternalStoragePublicDirectory(EMedia.DEFAULT_EXTERNAL_FILE_DIRECTORY).absolutePath)
        return File(storageDir, excelFileName)
    }

    fun createNewFile(context : Context): File {
        val excelFileName = "HearRate" + getDateFromLong(System.currentTimeMillis())
        val storageDir = File(Environment.getExternalStoragePublicDirectory(EMedia.DEFAULT_EXTERNAL_FILE_DIRECTORY).absolutePath)
        if (!storageDir.exists()) {
            storageDir.mkdir()
        }
        var excel : File ?= null
        try {
            excel = File(storageDir, "$excelFileName.xls")
            excel.createNewFile()
        } catch (e: IOException) {
            e.printStackTrace()
            try {
                excel = File.createTempFile(
                    excelFileName,  /* prefix */
                    ".xls",  /* suffix */
                    storageDir /* directory */
                )
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return excel!!
    }

    fun isTodayFileExistsAnkle(context:Context): Boolean{
        val excelFileName =
            "Ankle" + getDateFromLong(System.currentTimeMillis()) + ".xls"
        val storageDir = File(Environment.getExternalStoragePublicDirectory(EMedia.DEFAULT_EXTERNAL_FILE_DIRECTORY).absolutePath)
        //val storageDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "WAReader")

        //val storageDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "WAReader")
        if (!storageDir.exists()) {
            storageDir.mkdir()
        }
        val excel = File(storageDir, excelFileName)
        return excel.exists()
    }

    fun todayFileAnkle(context: Context): File {
        val excelFileName =
            "Ankle" + getDateFromLong(System.currentTimeMillis()) + ".xls"
        val storageDir = File(Environment.getExternalStoragePublicDirectory(EMedia.DEFAULT_EXTERNAL_FILE_DIRECTORY).absolutePath)
        return File(storageDir, excelFileName)
    }

    fun createNewFileAnkle(context : Context): File {
        val excelFileName = "Ankle" + getDateFromLong(System.currentTimeMillis())
        val storageDir = File(Environment.getExternalStoragePublicDirectory(EMedia.DEFAULT_EXTERNAL_FILE_DIRECTORY).absolutePath)
        if (!storageDir.exists()) {
            storageDir.mkdir()
        }
        var excel : File ?= null
        try {
            excel = File(storageDir, "$excelFileName.xls")
            excel.createNewFile()
        } catch (e: IOException) {
            e.printStackTrace()
            try {
                excel = File.createTempFile(
                    excelFileName,  /* prefix */
                    ".xls",  /* suffix */
                    storageDir /* directory */
                )
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return excel!!
    }

    private fun getDateFromLong1(longDate: Long): String {
        val date = Date(longDate)
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
        return dateFormat.format(date)
    }

    private fun getDateFromLong(longDate: Long): String {
        val date = Date(longDate)
        val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.ENGLISH)
        return dateFormat.format(date)
    }
}