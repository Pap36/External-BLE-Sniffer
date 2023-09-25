package com.externalblesniffer.usb

import android.hardware.usb.UsbManager
import android.util.Log
import com.externalblesniffer.repo.USBDevices
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MyUSBManager @Inject constructor(
    private val usbManager: UsbManager,
    private val usbDevices: USBDevices,
) {
    fun refresh() {
        Log.d("MyUSBManager", "refresh")
        val devices = usbManager.deviceList
        usbDevices.refresh(devices)
    }

}