package com.externalblesniffer.repo

import com.externalblesniffer.repo.datamodel.BLEScanResult
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.onEach
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
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

    private val _scannedUSBResults = MutableSharedFlow<BLEScanResult?>(
        extraBufferCapacity = 10000,
    )

    val scannedUSBResults = _scannedUSBResults.asSharedFlow()

    private val _scannedBLEResults = MutableSharedFlow<BLEScanResult?>(
        extraBufferCapacity = 10000,
    )
    val scannedBLEResults = _scannedBLEResults.asSharedFlow()

    fun addUSBResult(result: BLEScanResult) {
        usbResults.add(result)
        _usbResultsCount.value++
    }

    fun addBLEResult(result: BLEScanResult) {
        bleResults.add(result)
        _bleResultsCount.value++
    }

    fun registerUSBScanResult(result: BLEScanResult) {
        _scannedUSBResults.tryEmit(result)
    }

    fun registerBLESanResult(result: BLEScanResult) {
        _scannedBLEResults.tryEmit(result)
    }

    fun clearUSBResults() {
        usbResults.clear()
        _scannedUSBResults.tryEmit(null)
        _usbResultsCount.value = 0
    }

    fun clearBLEResults() {
        bleResults.clear()
        _scannedBLEResults.tryEmit(null)
        _bleResultsCount.value = 0
    }

}