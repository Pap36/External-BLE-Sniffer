package com.externalblesniffer.blescanner

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.os.Build
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

    private val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            if (result != null) {

                val rssi = result.rssi
                val advType = result.scanRecord?.bytes?.get(0)?.toInt() ?: 0
                val addrType = result.device.type
                val addr = result.device.address.toByteArray()
                val data = result.scanRecord?.bytes ?: ByteArray(0)

                scanResults.registerBLESanResult(BLEScanResult(rssi, advType, addrType, addr, data))
            }
        }
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

}