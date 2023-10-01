package com.externalblesniffer.usb

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbEndpoint
import android.hardware.usb.UsbManager
import android.hardware.usb.UsbRequest
import android.util.Log
import androidx.core.content.ContextCompat
import com.externalblesniffer.repo.CurrentConnection
import com.externalblesniffer.repo.USBDevices
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialPort.PARITY_NONE
import com.hoho.android.usbserial.driver.UsbSerialPort.STOPBITS_1
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.nio.ByteBuffer
import com.hoho.android.usbserial.driver.UsbSerialProber
import javax.inject.Inject
import javax.inject.Singleton

const val USB_SERVICE = "usb_request_permission"

@Singleton
class MyUSBManager @Inject constructor(
    private val usbManager: UsbManager,
    private val usbDevices: USBDevices,
    private val currentConnection: CurrentConnection,
) {

    private var currentPort: UsbSerialPort? = null

    fun refresh() {
        val availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(usbManager)
        Log.d("MyUSBManager", "refresh")
        val devices = availableDrivers.map { Pair(it.ports, it.device) }
        usbDevices.refresh(devices)
    }

    fun requestPermission(deviceID: Int, mPendingIntent: PendingIntent) {
        Log.d("MyUSBManager", "requestPermission: $deviceID")
        val device = usbDevices.usbDevices.value?.find { it.second.deviceId == deviceID }?.second
        if (device != null) {
            usbManager.requestPermission(device, mPendingIntent)
        }
    }

    fun connect(deviceID: Int, scope: CoroutineScope): Boolean {
        Log.d("MyUSBManager", "connect: $deviceID")
        val connection = usbManager.openDevice(usbDevices.usbDevices.value?.find { it.second.deviceId == deviceID }?.second)
            ?: return false

        currentConnection.setConnection(connection)
        currentPort = usbDevices.usbDevices.value?.find { it.second.deviceId == deviceID }?.first?.firstOrNull()
            ?: return false

        currentPort!!.open(connection)
        currentPort!!.setParameters(115200, 8, STOPBITS_1, PARITY_NONE)
        currentPort!!.dtr = true
        return true
    }

    fun write(data: ByteArray) {
        Log.d("MyUSBManager", "write: $currentPort")
        currentPort?.write(data, 1000)
        Log.d("MyUSBManager", "write: ${data.contentToString()}")
        read()
    }

    private fun read() {
        val dataToRead = ByteArray(64)
        val bytesRead = currentPort?.read(dataToRead, 1000)
        Log.d("MyUSBManager", "read: $bytesRead bytes read")
        currentConnection.setLatestReceivedData(dataToRead)
    }

}