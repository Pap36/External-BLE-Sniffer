package com.externalblesniffer.viewmodels

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.hardware.usb.UsbManager
import android.net.Uri
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.RECEIVER_EXPORTED
import androidx.core.content.ContextCompat.RECEIVER_NOT_EXPORTED
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.externalblesniffer.blescanner.BLEManager
import com.externalblesniffer.export.ExportManager
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
    private val bleManager: BLEManager,
    private val exportManager: ExportManager,
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
    fun exportResults(uri: Uri) = exportManager.exportResults(uri)

    fun connect(deviceID: Int): Boolean {
        val res = usbManager.connect(deviceID, viewModelScope)
        if (!res) requestPermission(deviceID)
        return res
    }

    fun startScan() {
        bleManager.startScan()
        usbManager.startScan()
    }

    fun startAdv() = usbManager.startAdvertising()
    fun stopAdv() = usbManager.stopAdvertising()

    fun stopScan() {
        bleManager.stopScan()
        usbManager.stopScan()
    }
    fun changeRSSI(rssiValue: Int) {
        bleManager.changeRssi(rssiValue)
        usbManager.changeRssi(rssiValue)
    }

    fun changeJoinRspReq(joinRspReq: Boolean) {
        usbManager.changeJoinRspReq(joinRspReq)
    }

    fun changeScanTypePassive(scanTypePassive: Boolean) {
        usbManager.changeScanTypePassive(scanTypePassive)
    }

    fun changeScanWindowValue(scanWindowValue: Float) {
        usbManager.changeScanWindowValue(scanWindowValue)
    }

    fun changeScanIntervalValue(scanIntervalValue: Float) {
        usbManager.changeScanIntervalValue(scanIntervalValue)
    }

    fun changeAdvertisingMinInterval(advertisingMinInterval: Float) {
        Log.d("MainViewModel", "changeAdvertisingMinInterval: $advertisingMinInterval")
        val increments = (advertisingMinInterval / 0.625).toInt()
        val value = increments * 0.625f
        usbManager.changeAdvertisingMinInterval(value)
    }

    fun changeAdvertisingMaxInterval(advertisingMaxInterval: Float) {
        val increments = (advertisingMaxInterval / 0.625).toInt()
        val value = increments * 0.625f
        usbManager.changeAdvertisingMinInterval(value)
    }

    fun changeAdvTimeoutValue(advTimeoutValue: Int) {
        usbManager.changeAdvTimeoutValue(advTimeoutValue)
    }

    fun changeBoard(isScanner: Boolean) {
        usbManager.changeBoard(isScanner)
    }
    fun disconnect() = usbManager.disconnect()

}