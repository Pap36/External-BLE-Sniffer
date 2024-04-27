package com.externalblesniffer.repo

import android.hardware.usb.UsbDevice
import android.util.Log
import com.hoho.android.usbserial.driver.UsbSerialPort
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class USBDevices @Inject constructor() {

    private val _usbDevices: MutableStateFlow<List<Pair<List<UsbSerialPort>, UsbDevice>>?> = MutableStateFlow(null)
    val usbDevices = _usbDevices.asStateFlow()

    private val _connectedBoardType: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val connectedBoardType = _connectedBoardType.asStateFlow()

    private val _isOn = MutableStateFlow(false)
    val isOn = _isOn.asStateFlow()

    fun refresh(devices: List<Pair<List<UsbSerialPort>, UsbDevice>>) {
        Log.d("USBDevices", "refresh: $devices")
        _usbDevices.value = devices
    }

    fun setConnectedBoardType(isScanner: Boolean) {
        _connectedBoardType.value = isScanner
    }

    fun setIsOn(isOn: Boolean) {
        _isOn.value = isOn
    }

}