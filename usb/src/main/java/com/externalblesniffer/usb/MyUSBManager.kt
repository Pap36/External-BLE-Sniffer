package com.externalblesniffer.usb

import android.app.PendingIntent
import android.hardware.usb.UsbManager
import android.util.Log
import com.externalblesniffer.repo.ScanResults
import com.externalblesniffer.repo.USBDevices
import com.externalblesniffer.repo.Utils.encodeHex
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

            if (!usbDevices.isOn.value && dataToRead[0].toInt() == 0x02) {
                if (usbDevices.connectedBoardType.value) {
                    // read parameters -- scanner
                    // 0x02 scan window scan interval scan type
                    val window = dataToRead.sliceArray(1 until 3)
                        .encodeHex().toUInt(radix = 16).toFloat() * 0.625f
                    val interval = dataToRead.sliceArray(3 until 5)
                        .encodeHex().toUInt(radix = 16).toFloat() * 0.625f
                    val scanType = dataToRead[5].toInt() == 0
                    usbDevices.setBoardParams(
                        usbDevices.boardParams.value.copy(
                            scanWindowValue = window,
                            scanIntervalValue = interval,
                            scanTypePassive = scanType
                        )
                    )
                } else {
                    // read parameters -- adv
                    // 0x02 adv min adv max timeout
                    val minInterval = dataToRead.sliceArray(1 until 3)
                        .encodeHex().toUInt(radix = 16).toFloat() * 0.625f
                    val maxInterval = dataToRead.sliceArray(3 until 5)
                        .encodeHex().toUInt(radix = 16).toFloat() * 0.625f
                    val timeout = dataToRead[5].toInt()
                    usbDevices.setBoardParams(
                        usbDevices.boardParams.value.copy(
                            advertisingMinInterval = minInterval.toFloat(),
                            advertisingMaxInterval = maxInterval.toFloat(),
                            advTimeout = timeout
                        )
                    )
                }
            }

            if(usbDevices.connectedBoardType.value && usbDevices.isOn.value) incompleteData = processData(
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

    fun changeAdvertisingParam(advertisingMinInterval: Float, advertisingMaxInterval: Float, advTimeout: Int) {
        val incrementsMin = (advertisingMinInterval / 0.625).toInt()
        val incrementsMax = (advertisingMaxInterval / 0.625).toInt()
        val data = UByteArray(6)
        data[0] = 0x03.toUByte()
        data[2] = (incrementsMin and 0xFF).toUByte()
        data[1] = ((incrementsMin shr 8) and 0xFF).toUByte()

        data[4] = (incrementsMax and 0xFF).toUByte()
        data[3] = ((incrementsMax shr 8) and 0xFF).toUByte()

        data[5] = advTimeout.toUByte()

        write(data)
    }

    fun changeScanningParam(scanWindowValue: Float, scanIntervalValue: Float, scanTypePassive: Boolean) {
        val incrementsWindow = (scanWindowValue / 0.625).toInt()
        val incrementsInterval = (scanIntervalValue / 0.625).toInt()
        val data = UByteArray(6)
        data[0] = 0x03.toUByte()
        data[2] = (incrementsWindow and 0xFF).toUByte()
        data[1] = ((incrementsWindow shr 8) and 0xFF).toUByte()

        data[4] = (incrementsInterval and 0xFF).toUByte()
        data[3] = ((incrementsInterval shr 8) and 0xFF).toUByte()

        data[5] = if (scanTypePassive) 0x00u else 0x01u

        write(data)
    }

    fun startAdvertising() {
        write(byteArrayOf(0x01).toUByteArray())
    }

    fun stopAdvertising() {
        write(byteArrayOf(0x00).toUByteArray())
    }

    fun readParameters() {
        write(byteArrayOf(0x02).toUByteArray())
    }

    fun changeBoard(isScanner: Boolean) {
        usbDevices.setConnectedBoardType(isScanner)
    }

}