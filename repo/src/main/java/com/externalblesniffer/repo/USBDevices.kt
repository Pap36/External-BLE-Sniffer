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

    fun refresh(devices: List<Pair<List<UsbSerialPort>, UsbDevice>>) {
        Log.d("USBDevices", "refresh: $devices")
        _usbDevices.value = devices
    }

}