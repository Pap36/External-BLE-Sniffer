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
import kotlinx.coroutines.Job
import kotlinx.coroutines.yield
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
    private var readingJob: Job? = null

    fun refresh() {
        val availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(usbManager)
        // Log.d("MyUSBManager", "refresh")
        val devices = availableDrivers.map { Pair(it.ports, it.device) }
        usbDevices.refresh(devices)
    }

    fun requestPermission(deviceID: Int, mPendingIntent: PendingIntent) {
        // Log.d("MyUSBManager", "requestPermission: $deviceID")
        val device = usbDevices.usbDevices.value?.find { it.second.deviceId == deviceID }?.second
        if (device != null) {
            usbManager.requestPermission(device, mPendingIntent)
        }
    }

    fun connect(deviceID: Int, scope: CoroutineScope): Boolean {
        // Log.d("MyUSBManager", "connect: $deviceID")
        val connection = usbManager.openDevice(usbDevices.usbDevices.value?.find { it.second.deviceId == deviceID }?.second)
            ?: return false

        currentConnection.setConnection(connection)
        currentPort = usbDevices.usbDevices.value?.find { it.second.deviceId == deviceID }?.first?.firstOrNull()
            ?: return false

        currentPort!!.open(connection)
        currentPort!!.setParameters(115200, 8, STOPBITS_1, PARITY_NONE)
        currentPort!!.dtr = true

        readingJob = scope.launch(Dispatchers.IO) {
            while (true) {
                yield()
                read()
            }
        }

        return true
    }

    fun startScan() {
        write(byteArrayOf(0x01))
    }

    fun stopScan() {
        write(byteArrayOf(0x00))
    }

    private fun write(data: ByteArray) {
        Log.d("MyUSBManager", "write${data.contentToString()}")
        currentPort?.write(data, 1000)
    }

    private suspend fun read() {
        yield()
        val dataToRead = ByteArray(64)
        currentPort?.read(dataToRead, 1000)
        Log.d("MyUSBManager", "read${dataToRead.contentToString()}")
        currentConnection.setLatestReceivedData(dataToRead)
    }

    fun disconnect() {
        try {
            readingJob?.cancel()
            currentPort?.close()
        } catch(e: Exception) {
            Log.d("MyUsbManager", e.printStackTrace().toString())
        }
        currentPort = null
    }

}