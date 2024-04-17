package com.externalblesniffer.usb

import android.app.PendingIntent
import android.hardware.usb.UsbManager
import android.util.Log
import com.externalblesniffer.repo.ScanResults
import com.externalblesniffer.repo.USBDevices
import com.externalblesniffer.repo.datamodel.BLEScanResult
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialPort.PARITY_NONE
import com.hoho.android.usbserial.driver.UsbSerialPort.STOPBITS_1
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
    private val scanResults: ScanResults,
) {

    private var currentPort: UsbSerialPort? = null
    private var readingJob: Job? = null

    private var incompleteData: ByteArray = byteArrayOf()

    private var rssiThreshold:Int = 127

    private var previousResult: BLEScanResult? = null

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
        scanResults.clearUSBResults()
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
        val readBytes = currentPort?.read(dataToRead, 1000)
        if (readBytes != 0 && readBytes != null) {
            incompleteData = processData(
                try {
                    incompleteData + dataToRead.sliceArray(0 until (readBytes))
                } catch (e: Exception) {
                    ByteArray(0)
                }
            )
        }
    }

    private fun processData(data: ByteArray): ByteArray {
        val toProcess = data
        val length = data[0].toInt()
        if (data.size < length + 1) return toProcess
        val message = data.sliceArray(0 until length + 1)
        val rssi = message[1].toInt()
        val advType = message[2].toInt()
        val addrType = message[3].toInt()
        val addr = message.sliceArray(4 until 10).reversedArray()
        val manData = message.sliceArray(10 until length + 1)
        if (!(rssi < rssiThreshold || (rssi == 127 && rssiThreshold == -100))) {
            val newResult = BLEScanResult(rssi, advType, addrType, addr, manData, "USB")
            if(previousResult != null) {
                if (newResult.adv_type == 4 && previousResult!!.adv_type in intArrayOf(0, 2) &&
                    previousResult!!.addr.contentEquals(newResult.addr)
                ) {
                    scanResults.registerUSBScanResult(
                        newResult.copy(
                            adv_type = 4,
                            data = previousResult!!.data + newResult.data
                        )
                    )
                } else scanResults.registerUSBScanResult(newResult)
            } else scanResults.registerUSBScanResult(newResult)
            previousResult = newResult
        }
        return data.sliceArray(length + 1 until data.size)
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

    fun changeRssi(rssi: Int) {
        rssiThreshold = rssi
    }

}