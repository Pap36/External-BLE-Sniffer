package com.externalblesniffer.blescanner

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.os.Build
import android.util.Log
import com.externalblesniffer.repo.ScanResults
import com.externalblesniffer.repo.datamodel.BLEScanResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BLEManager @Inject constructor(
    bleAdapter: BluetoothAdapter,
    private val scanResults: ScanResults
) {

    private val scanner = bleAdapter.bluetoothLeScanner

    private val scanSettings = android.bluetooth.le.ScanSettings.Builder()
        .setScanMode(android.bluetooth.le.ScanSettings.SCAN_MODE_LOW_LATENCY)
        .setReportDelay(0)
        .build()

    private var rssiThreshold: Int = 127

    private fun hexStringToByteArray(hexString: String): ByteArray {
        val result = ByteArray(hexString.length / 2)

        for (i in hexString.indices step 2) {
            val firstDigit = Character.digit(hexString[i], 16)
            val secondDigit = Character.digit(hexString[i + 1], 16)
            val byteValue = firstDigit shl 4 or secondDigit
            result[i / 2] = byteValue.toByte()
        }

        return result
    }

    private val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            if (result != null) {
                val rssi = result.rssi
                if (rssi < rssiThreshold || (rssi == 127 && rssiThreshold == -100)) return
                val advType = -1
                val addrType = result.device.type
                val addr = hexStringToByteArray(result.device.address.replace(":", ""))
                // addr is string of the form AA:BB:CC:DD:EE:FF
                val data = truncateData(result.scanRecord?.bytes ?: ByteArray(0))
                scanResults.registerBLESanResult(BLEScanResult(rssi, advType, addrType, addr, data, "BLE"))
            }
        }
    }

    private fun truncateData(data: ByteArray): ByteArray {
        var len = data[0].toInt()
        var offset = 0
        while (len != 0) {
            offset += len + 1
            len = data[offset].toInt()
        }
        return data.sliceArray(0 until offset)
    }

    @SuppressLint("MissingPermission")
    fun startScan() {
        scanResults.clearBLEResults()
        scanner.startScan(null, scanSettings, scanCallback)
    }

    @SuppressLint("MissingPermission")
    fun stopScan() {
        scanner.stopScan(scanCallback)
    }

    fun changeRssi(rssi: Int) {
        rssiThreshold = rssi
    }

}