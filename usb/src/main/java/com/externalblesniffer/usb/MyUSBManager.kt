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

@OptIn(ExperimentalUnsignedTypes::class)
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
    private var joinRspReq:Boolean = true

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
        usbDevices.setIsOn(true)
        scanResults.clearUSBResults()
        write(ubyteArrayOf(0x01.toUByte()))
    }

    fun stopScan() {
        usbDevices.setIsOn(false)
        write(ubyteArrayOf(0x00.toUByte()))
    }

    private fun write(data: UByteArray) {
        Log.d("MyUSBManager", "write${data.contentToString()}")
        currentPort?.write(data.toByteArray(), 1000)

    }

    private suspend fun read() {
        yield()
        val dataToRead = ByteArray(64)
        val readBytes = currentPort?.read(dataToRead, 1000)
        if (readBytes != 0 && readBytes != null) {
            if(usbDevices.connectedBoardType.value) incompleteData = processData(
                try {
                    incompleteData + dataToRead.sliceArray(0 until (readBytes))
                } catch (e: Exception) {
                    ByteArray(0)
                }
            ) else {
                Log.d("MyUSBManager", "read: ${dataToRead.contentToString()}")
                val boardState = dataToRead[0].toInt()
                usbDevices.setIsOn(boardState == 1)
            }
        }
    }

    private fun processData(data: ByteArray): ByteArray {
        val length = data[0].toInt()
        if (data.size < length + 1) return data
        val message = data.sliceArray(0 until length + 1)
        val rssi = message[1].toInt()
        val advType = message[2].toInt()
        val addrType = message[3].toInt()
        val addr = message.sliceArray(4 until 10).reversedArray()
        val manData = message.sliceArray(10 until length + 1)
        if (!(rssi < rssiThreshold || (rssi == 127 && rssiThreshold == -100))) {
            var newResult: BLEScanResult? = BLEScanResult(rssi, advType, addrType, addr, manData, "USB")
            if (joinRspReq) {
                if (previousResult != null) {
                    if (newResult?.adv_type == 4 && previousResult!!.adv_type in intArrayOf(0, 2) &&
                        previousResult!!.addr.contentEquals(newResult.addr)
                    ) {
                        previousResult = newResult.copy(
                            adv_type = 4,
                            data = previousResult!!.data + newResult.data
                        )
                        newResult = null
                    }
                }
                previousResult?.let { scanResults.registerUSBScanResult(it) }
                previousResult = newResult
            } else {
                newResult?.let { scanResults.registerUSBScanResult(it) }
            }
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

    fun changeJoinRspReq(joinRspReq: Boolean) {
        this.joinRspReq = joinRspReq
    }

    fun changeScanTypePassive(scanTypePassive: Boolean) {
        if (scanTypePassive) write(byteArrayOf(0x03).toUByteArray())
        else write(byteArrayOf(0x02).toUByteArray())
    }

    fun changeScanWindowValue(scanWindowValue: Float) {
        val increments = (scanWindowValue / 0.625).toInt()
        val data = UByteArray(3)
        // convert increments in a size-2 bytearray
        data[0] = 0x04.toUByte()
        data[2] = (increments and 0xFF).toUByte()
        data[1] = ((increments shr 8) and 0xFF).toUByte()
        write(data)
    }

    fun changeScanIntervalValue(scanIntervalValue: Float) {
        val increments = (scanIntervalValue / 0.625).toInt()
        val data = UByteArray(3)
        data[0] = 0x05.toUByte()
        data[2] = (increments and 0xFF).toUByte()
        data[1] = ((increments shr 8) and 0xFF).toUByte()
        write(data)
    }

    fun changeAdvertisingMinInterval(advertisingMinInterval: Float) {
        val increments = (advertisingMinInterval / 0.625).toInt()
        val data = UByteArray(3)
        data[0] = 0x04.toUByte()
        data[2] = (increments and 0xFF).toUByte()
        data[1] = ((increments shr 8) and 0xFF).toUByte()
        write(data)
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    fun changeAdvertisingMaxInterval(advertisingMaxInterval: Float) {
        val increments = (advertisingMaxInterval / 0.625).toInt()
        val data = UByteArray(3)
        data[0] = 0x03.toUByte()
        data[2] = (increments and 0xFF).toUByte()
        data[1] = ((increments shr 8) and 0xFF).toUByte()
        write(data)
    }

    fun changeAdvTimeoutValue(advTimeoutValue: Int) {
        val data = ByteArray(2)
        data[0] = 0x02
        data[1] = advTimeoutValue.toByte()
        write(data.toUByteArray())
    }

    fun startAdvertising() {
        write(byteArrayOf(0x01).toUByteArray())
    }

    fun stopAdvertising() {
        write(byteArrayOf(0x00).toUByteArray())
    }

    fun changeBoard(isScanner: Boolean) {
        usbDevices.setConnectedBoardType(isScanner)
    }

}