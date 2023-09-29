package com.externalblesniffer.viewmodels

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.hardware.usb.UsbManager
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.RECEIVER_EXPORTED
import androidx.core.content.ContextCompat.RECEIVER_NOT_EXPORTED
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.externalblesniffer.usb.MyBroadcastReceiver
import com.externalblesniffer.usb.MyUSBManager
import com.externalblesniffer.usb.USB_SERVICE
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val usbManager: MyUSBManager,
    @ApplicationContext context: Context,
    broadcastReceiver: MyBroadcastReceiver,
): ViewModel() {

    private val mPendingIntent = PendingIntent.getBroadcast(
        context,
        0,
        android.content.Intent(USB_SERVICE),
        PendingIntent.FLAG_IMMUTABLE,
    )

    init {
        ContextCompat.registerReceiver(
            context,
            broadcastReceiver,
            IntentFilter(USB_SERVICE),
            RECEIVER_NOT_EXPORTED
        )
    }

    fun refresh() = usbManager.refresh()

    fun requestPermission(deviceID: Int) = usbManager.requestPermission(deviceID, mPendingIntent)

    fun requestPermissionAndConnect(deviceID: Int) = usbManager.requestPermissionAndConnect(deviceID, mPendingIntent, viewModelScope)

    fun connect(deviceID: Int) = usbManager.connect(deviceID, viewModelScope)

    fun send(data: ByteArray) {
        viewModelScope.launch(Dispatchers.IO) {
            usbManager.write(data)
        }
    }

}