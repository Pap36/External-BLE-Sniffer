package com.externalblesniffer.repo

import com.externalblesniffer.repo.datamodel.BLEScanResult
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.onEach
import java.util.Hashtable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScanResults @Inject constructor() {

    private val _usbResultsCount = MutableStateFlow(0)
    val usbResultsCount = _usbResultsCount.asStateFlow()

    private val _bleResultsCount = MutableStateFlow(0)
    val bleResultsCount = _bleResultsCount.asStateFlow()

    val usbResults: ArrayList<BLEScanResult> = arrayListOf()
    val bleResults: ArrayList<BLEScanResult> = arrayListOf()

    private val _scannedUSBResults = MutableStateFlow<BLEScanResult?>(null)
    val scannedUSBResults = _scannedUSBResults
        .onEach {
            it?.let {
                _usbResultsCount.value++
                usbResults.add(it)
            }
        }
        .buffer(10000, BufferOverflow.SUSPEND)

    private val _scannedBLEResults = MutableStateFlow<BLEScanResult?>(null)
    val scannedBLEResults = _scannedBLEResults
        .onEach {
            it?.let {
                _bleResultsCount.value++
                bleResults.add(it)
            }
        }
        .buffer(10000, BufferOverflow.SUSPEND)

    fun registerUSBScanResult(result: BLEScanResult) {
        _scannedUSBResults.value = result
    }

    fun registerBLESanResult(result: BLEScanResult) {
        _scannedBLEResults.value = result
    }

    fun clearUSBResults() {
        usbResults.clear()
        _scannedUSBResults.value = null
        _usbResultsCount.value = 0
    }

    fun clearBLEResults() {
        bleResults.clear()
        _scannedBLEResults.value = null
        _bleResultsCount.value = 0
    }

}