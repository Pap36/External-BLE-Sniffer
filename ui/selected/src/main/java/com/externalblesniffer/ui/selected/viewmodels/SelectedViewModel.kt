package com.externalblesniffer.ui.selected.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.externalblesniffer.repo.ScanResults
import com.externalblesniffer.repo.USBDevices
import com.externalblesniffer.repo.datamodel.BLEScanResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class SelectedViewModel @Inject constructor(
    private val scanResults: ScanResults,
    usbDevices: USBDevices,
): ViewModel() {
    val isScanner = usbDevices.connectedBoardType
    val isOn = usbDevices.isOn

    val boardParameters = usbDevices.boardParams

    private val _rssiFilterValue = MutableStateFlow(-70)
    val rssiFilterValue = _rssiFilterValue.asStateFlow()

    private val _scanTimeoutValue = MutableStateFlow(30)
    val scanTimeoutValue = _scanTimeoutValue.asStateFlow()

    private val _joinRspReq = MutableStateFlow(true)
    val joinRspReq = _joinRspReq.asStateFlow()

    val rssiFinal = _rssiFilterValue
        .debounce(300)

    val scanTimeoutFinal = _scanTimeoutValue
        .debounce(300)

    val usbResultsCount = scanResults.usbResultsCount
    val bleResultsCount = scanResults.bleResultsCount

    private var usbCollectJob: Job = viewModelScope.launch(Dispatchers.IO) {
        scanResults.scannedUSBResults
            .onEach {
                it?.let { scanResults.addUSBResult(it) }
            }
            .collect()
    }

    private var bleCollectJob: Job = viewModelScope.launch(Dispatchers.IO) {
        scanResults.scannedBLEResults
            .onEach {
                it?.let { scanResults.addBLEResult(it) }
            }
            .collect()
    }

    val maximumCount = usbResultsCount
        .combine(bleResultsCount) { usb, ble ->
            if (usb > ble && usb != 0) usb else if (ble != 0) ble else 1
        }
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 1)

    fun changeRSSI(value: Int) {
        _rssiFilterValue.value = value
    }

    fun changeScanTimeout(value: Int) {
        _scanTimeoutValue.value = value
    }

    fun changeJoinRspReq(newVal: Boolean) {
        _joinRspReq.value = newVal
    }

    fun startScan() {
        usbCollectJob.start()
        bleCollectJob.start()
    }

    fun stopScan() {
        processResults()
    }

    private fun processResults() {
        val usbResults = scanResults.usbResults
        val bleResults = scanResults.bleResults
        var dif = 0
        val processed = arrayListOf<BLEScanResult>()
        Log.d("SelectedViewModel", "bleResultsRaw: $bleResults")
        Log.d("SelectedViewModel", "usbResultsRaw: $usbResults")
        Log.d("SelectedViewModel", "bleResults: ${bleResults.map { res -> res.data.contentToString() }}")
        Log.d("SelectedViewModel", "usbResults: ${usbResults.map { res -> res.data.contentToString() }}")
        for(bleResult in bleResults) {
            if (processed.any { it.data.contentEquals(bleResult.data) }) continue
            processed.add(bleResult)
            Log.d("SelectedViewModel", "bleResult: ${bleResult.data.contentToString()}")
            val bleCount = bleResults.count { /*it.adv_type == bleResult.adv_type &&*/ it.data.contentEquals(bleResult.data) }
            val usbCount = usbResults.count { /*it.adv_type == bleResult.adv_type &&*/ it.data.contentEquals(bleResult.data) }
            Log.d("SelectedViewModel", "bleCount: $bleCount, usbCount: $usbCount")
            dif += if(bleCount >= usbCount) bleCount - usbCount else 0
        }
        Log.d("SelectedViewModel", "Total messages scanned by ble and not usb: $dif / ${bleResults.size}")
        dif = 0
        processed.clear()
        for(usbResult in usbResults) {
            if (processed.any { it.data.contentEquals(usbResult.data) }) continue
            processed.add(usbResult)
            Log.d("SelectedViewModel", "usbResult: ${usbResult.data.contentToString()}")
            val bleCount = bleResults.count { /*it.adv_type == usbResult.adv_type &&*/ it.data.contentEquals(usbResult.data) }
            val usbCount = usbResults.count { /*it.adv_type == usbResult.adv_type &&*/ it.data.contentEquals(usbResult.data) }
            Log.d("SelectedViewModel", "bleCount: $bleCount, usbCount: $usbCount")
            dif += if(usbCount >= bleCount) usbCount - bleCount else 0
        }
        Log.d("SelectedViewModel", "Total messages scanned by usb and not ble: $dif / ${usbResults.size}")
    }

}