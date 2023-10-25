package com.externalblesniffer.usb

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MyBroadcastReceiver @Inject constructor(
    private val usbManager: MyUSBManager,
): BroadcastReceiver() {
    override fun onReceive(p0: Context?, p1: Intent?) {
        if (p1?.action == USB_SERVICE) { usbManager.refresh() }
    }
}