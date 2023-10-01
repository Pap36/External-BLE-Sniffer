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

    fun requestPermissionAndConnect(deviceID: Int, mPendingIntent: PendingIntent, scope: CoroutineScope): Boolean {
        Log.d("MyUSBManager", "requestPermissionAndConnect: $deviceID")
        val device = usbDevices.usbDevices.value?.find { it.second.deviceId == deviceID }?.second
        return if (device != null) {
            usbManager.requestPermission(device, mPendingIntent)
            connect(deviceID, scope)
        } else false
    }

    fun connect(deviceID: Int, scope: CoroutineScope): Boolean {
        Log.d("MyUSBManager", "connect: $deviceID")
        val device = usbDevices.usbDevices.value?.find { it.second.deviceId == deviceID }?.second
        connectedDevice = device
        if (device != null) {
            val connection = usbManager.openDevice(device)
            val config = device.getConfiguration(0)
            return if (connection != null) {
                connection.claimInterface(device.getInterface(1), true)
                connection.setConfiguration(config)
                currentConnection.setConnection(connection)
                readEP = device.getInterface(1).getEndpoint(0)
                Log.d("MyUSBManager", "readEP: $readEP")
                Log.d("MyUSBManager", "readEP: ${readEP?.direction}")
                Log.d("MyUSBManager", "readEP: ${readEP?.attributes}")
                writeEP = device.getInterface(1).getEndpoint(1)

                /*scope.launch(Dispatchers.IO) {
                    while (true) {
                        try {
                            // give time to System resources
                            delay(100)
                            read()
                        } catch (e: Exception) {
                            // Log.d("MyUSBManager", "read: ${e.message}")
                            continue
                        }
                    }
                }*/

                true
            } else false
        }
        return false
    }

    suspend fun write(data: ByteArray) {
        Log.d("MyUSBManager", "write: ${data.contentToString()}")
        val length = currentConnection.currentConnection.value?.bulkTransfer(
            writeEP,
            data,
            data.size,
            1000
        )
        Log.d("MyUSBManager", "wrote a length of: $length")
        delay(100)
        read()
    }

    private fun read() {
        val data = ByteArray(64)
        val length = currentConnection.currentConnection.value?.bulkTransfer(
            readEP,
            data,
            64,
            1000
        )
        Log.d("MyUSBManager", "read a length of: $length")
        currentConnection.setLatestReceivedData(data)
    }

}