package com.example.ubicompapplication

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DeviceViewModel: ViewModel() {
    private var _connectedDevices = MutableLiveData<MutableList<BluetoothDevice>>()
    var connectedDevices: LiveData<MutableList<BluetoothDevice>> = _connectedDevices


    fun addDevice(bluetoothDevice: BluetoothDevice){
        if(_connectedDevices.value == null)
            _connectedDevices.value = mutableListOf()
        val list = _connectedDevices.value
        val device: Int? = list?.indexOfFirst { d -> d.address == bluetoothDevice.address }
        if(device == null || device == -1)
        {
            list?.add(bluetoothDevice)
        }
        _connectedDevices.value = list
    }
}