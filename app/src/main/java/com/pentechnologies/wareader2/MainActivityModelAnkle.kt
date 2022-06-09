package com.pentechnologies.wareader2

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.pentechnologies.wareader2.ble.AnkleGattCallback


class MainActivityModelAnkle : ViewModel(){

/*
 * this model class get the live value from the WristCallBack and then it give the value to the UI to show
 * It is use Android LiveData to watch over changes in the data and report those changes to the model
 * and from model to the UI*/

    private val ankleGattCallBackInstance: AnkleGattCallback = AnkleGattCallback.getInstance2()

    //for ankle device
    val ankleTemperatureMeas: MutableLiveData<String> = ankleGattCallBackInstance.ankleTemperatureMeas
    val stepsMeas: MutableLiveData<String> = ankleGattCallBackInstance.stepsMeas
    val metersMeas: MutableLiveData<String> = ankleGattCallBackInstance.metersMeas
    val latMeas: MutableLiveData<String> = ankleGattCallBackInstance.latMeas
    val longMeas: MutableLiveData<String> = ankleGattCallBackInstance.longMeas
    val sivMeas: MutableLiveData<String> = ankleGattCallBackInstance.sivMeas

}
