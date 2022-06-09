package com.pentechnologies.wareader2


import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

import com.pentechnologies.wareader2.ble.WristGattCallback

class MainActivityModel : ViewModel() {

    /*
    * this model class get the live value from the WristCallBack and then it give the value to the UI to show
    * It is use Android LiveData to watch over changes in the data and report those changes to the model
    * and from model to the UI*/
    private val wristGattCallbackInstance: WristGattCallback = WristGattCallback.getInstance()

    //for wrist device
    var heartRateMeasurement: MutableLiveData<String> = wristGattCallbackInstance.heartRateMeasurement
    val rr: MutableLiveData<String> = wristGattCallbackInstance.rr
    val bodySensorLocation: MutableLiveData<String> = wristGattCallbackInstance.bodySensorLocation
    val temperature: MutableLiveData<String> = wristGattCallbackInstance.temperature
    val sp02: MutableLiveData<String> = wristGattCallbackInstance.sp02
    val ecg: MutableLiveData<String> = wristGattCallbackInstance.ecg
    val ecgNeg: MutableLiveData<String> = wristGattCallbackInstance.ecgNeg
}