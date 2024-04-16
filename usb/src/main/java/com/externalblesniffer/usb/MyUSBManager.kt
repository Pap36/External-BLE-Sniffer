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
import java.io.ByteArrayInputStream
import java.io.InputStream
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
        val dataToRead = ByteArray(73)
        val readBytes = currentPort?.read(dataToRead, 1000)
        Log.d("MyUSBManager", "read: ${dataToRead.contentToString()}")
        incompleteData = processData(
            try {
                incompleteData + dataToRead.sliceArray(0 until (readBytes ?: 0))
            } catch (e: Exception) {
                ByteArray(0)
            }
        )
    }

    private fun processData(data: ByteArray): ByteArray {
        var toProcess = data
        while (toProcess.isNotEmpty()) {
            try {
                val rssi = toProcess[0]
                val advType = toProcess[1]
                val addrType = toProcess[2]
                val mac = toProcess.sliceArray(3 until 9).reversedArray()
                var manufacturerData = toProcess.sliceArray(9 until toProcess.size)
                if (manufacturerData.isEmpty()) return toProcess
                while (manufacturerData.isNotEmpty()) {
                    val len = manufacturerData[0].toInt()
                    if (len <= 0) {
                        val manData = toProcess.sliceArray(
                            9 until toProcess.size - manufacturerData.size
                        )
                        val readData = toProcess.sliceArray(
                            0 until toProcess.size - manufacturerData.size
                        )
                        // Log.d("MyUSBManager", "read1: ${readData.contentToString()}")
                        scanResults.registerUSBScanResult(
                            BLEScanResult(
                                rssi = rssi.toInt(),
                                adv_type = advType.toInt(),
                                addr_type = addrType.toInt(),
                                addr = mac,
                                data = manData,
                            )
                        )
                        toProcess = toProcess.sliceArray(
                            toProcess.size - manufacturerData.size until toProcess.size
                        )
                        break
                    }
                    if (manufacturerData.size < len + 1) return toProcess
                    manufacturerData = manufacturerData.sliceArray(len + 1 until manufacturerData.size)
                }
                if (manufacturerData.isEmpty()) {
                    val manData = toProcess.sliceArray(9 until toProcess.size)
                    Log.d("MyUSBManager", "read2: ${toProcess.contentToString()}")
                    scanResults.registerUSBScanResult(
                        BLEScanResult(
                            rssi = rssi.toInt(),
                            adv_type = advType.toInt(),
                            addr_type = addrType.toInt(),
                            addr = mac,
                            data = manData,
                        )
                    )
                    toProcess = byteArrayOf()
                }
            } catch (e: Exception) {
                return toProcess
            }
        }
        return byteArrayOf()
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