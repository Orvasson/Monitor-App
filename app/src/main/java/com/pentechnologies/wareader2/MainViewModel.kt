package com.pentechnologies.wareader2


import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pentechnologies.wareader2.Utils.getAnkleList
import com.pentechnologies.wareader2.Utils.getEcgList
import com.pentechnologies.wareader2.Utils.getGPS
import com.pentechnologies.wareader2.Utils.getHeartList
import com.pentechnologies.wareader2.Utils.getSp02List
import com.pentechnologies.wareader2.Utils.getSteps
import com.pentechnologies.wareader2.Utils.getTempList
import com.pentechnologies.wareader2.Utils.getTodayDate
import com.pentechnologies.wareader2.db.*

import com.pentechnologies.wareader2.db.database.AppDatabase
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    //var heartRateMeasurement: MutableLiveData<Response<HeartRate>>
    val heartList = MutableLiveData<List<HeartRate>>()
    val heart = MutableLiveData<List<Map<String, List<*>>>>()
    val ankle = MutableLiveData<List<Map<String, List<*>>>>()


    fun fetchHeartRate(appDatabase: AppDatabase){
        val map: MutableList<Map<String, List<*>>> = ArrayList()
        val map1: MutableMap<String, List<*>> = HashMap()


        viewModelScope.launch {
            try {

           /*     val h = appDatabase.heartDao().getAll()
                 val s = appDatabase.stepDao().getAll()
                 if (s.isEmpty()){
                     steps.add(Steps(0,"","","",""))
                 }else{
                     steps.addAll(s)
                 }
                 if (h.isEmpty()){
                     usersFromDb.add(HeartRate(0,"","","",""))
                 }else{
                     usersFromDb.addAll(h)
                 }*/

                map1["HearRate"] = getHeartList(appDatabase.heartDao().getAll())
                map1["Temperature"] = getTempList(appDatabase.tempDao().getAll())
                map1["SpO2"] = getSp02List(appDatabase.spO2Dao().getAll())
                map1["ECG"] = getEcgList(appDatabase.ecgDao().getAll())

                map.add(map1)
                deleteHeartAll(appDatabase)
                heart.postValue(map)

            } catch (e: Exception) {
                // handler error
                heart.postValue(map)
            }

        }
    }

    fun fetchAnkle(appDatabase: AppDatabase){
        val map: MutableList<Map<String, List<*>>> = ArrayList()
        val map1: MutableMap<String, List<*>> = HashMap()


        viewModelScope.launch {
            try {

                map1["Ankle"] = getAnkleList(appDatabase.ankleDao().getAll())
                map1["Steps"] = getSteps(appDatabase.stepDao().getAll())
                map1["GPS"] = getGPS(appDatabase.gpsDao().getAll())

                map.add(map1)
                deleteAnkleAll(appDatabase)
                ankle.postValue(map)

            } catch (e: Exception) {
                // handler error
                ankle.postValue(map)
            }

        }
    }

    fun insertAnkle(appDatabase: AppDatabase, ankle: Ankle){
        viewModelScope.launch {
            try {
                appDatabase.ankleDao().insert(ankle)
            } catch (e: Exception) {
                // handler error
            }
        }

    }
    fun insertEcg(appDatabase: AppDatabase, ecg: Ecg){
        viewModelScope.launch {
            try {
                appDatabase.ecgDao().insert(ecg)
            } catch (e: Exception) {
                // handler error
            }
        }

    }
    fun insertGps(appDatabase: AppDatabase, gps: Gps){
        viewModelScope.launch {
            try {
                appDatabase.gpsDao().insert(gps)
            } catch (e: Exception) {
                // handler error
            }
        }

    }
    fun insertHeartRate(appDatabase: AppDatabase, heartRate: HeartRate){
        viewModelScope.launch {
            try {
                appDatabase.heartDao().insert(heartRate)
            } catch (e: Exception) {
                // handler error
            }
        }

    }
    fun insertSpO2(appDatabase: AppDatabase, spO2: SpO2){
        viewModelScope.launch {
            try {
                appDatabase.spO2Dao().insert(spO2)
            } catch (e: Exception) {
                // handler error
            }
        }

    }
    fun insertSteps(appDatabase: AppDatabase, steps: Steps){
        viewModelScope.launch {
            try {
                appDatabase.stepDao().insert(steps)
            } catch (e: Exception) {
                // handler error
            }
        }

    }
    fun insertTemperature(appDatabase: AppDatabase, temperature: Temperature){
        viewModelScope.launch {
            try {
                appDatabase.tempDao().insert(temperature)
            } catch (e: Exception) {
                // handler error
            }
        }

    }

    fun deleteHeartAll(appDatabase: AppDatabase){
        viewModelScope.launch {
            try {

                appDatabase.ecgDao().deleteAll()
                appDatabase.heartDao().deleteHeart()
                appDatabase.spO2Dao().deleteAll()
                appDatabase.tempDao().deleteAll()
            } catch (e: Exception) {
                // handler error
            }
        }

    }

    fun deleteAnkleAll(appDatabase: AppDatabase){
        viewModelScope.launch {
            try {
                appDatabase.ankleDao().deleteAll()
                appDatabase.gpsDao().deleteAll()
                appDatabase.stepDao().deleteAll()

            } catch (e: Exception) {
                // handler error
            }
        }

    }

    fun deleteAll(appDatabase: AppDatabase, date:String){
        viewModelScope.launch {
            try {
                appDatabase.ankleDao().deleteAll(date)
                appDatabase.ecgDao().deleteAll(date)
                appDatabase.gpsDao().deleteAll(date)
                appDatabase.heartDao().deleteHeart(date)
                appDatabase.spO2Dao().deleteAll(date)
                appDatabase.stepDao().deleteAll(date)
                appDatabase.tempDao().deleteAll(date)
            } catch (e: Exception) {
                // handler error
            }
        }

    }




}