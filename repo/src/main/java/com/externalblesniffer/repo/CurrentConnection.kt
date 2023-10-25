package com.externalblesniffer.repo

import android.hardware.usb.UsbDeviceConnection
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CurrentConnection @Inject constructor() {

    private val _currentConnection = MutableStateFlow<UsbDeviceConnection?>(null)
    val currentConnection = _currentConnection

    private val _latestReceivedData = MutableStateFlow<ByteArray?>(null)
    val latestReceivedData = _latestReceivedData

    fun setConnection(connection: UsbDeviceConnection?) {
        _currentConnection.value = connection
    }

    fun setLatestReceivedData(data: ByteArray?) {
        _latestReceivedData.value = data
    }

}