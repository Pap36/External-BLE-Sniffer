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
import javax.inject.Inject

@HiltViewModel
class SelectedViewModel @Inject constructor(
    private val scanResults: ScanResults,
    usbDevices: USBDevices,
): ViewModel() {
    val isScanner = usbDevices.connectedBoardType
    val isOn = usbDevices.isOn

    private val _rssiFilterValue = MutableStateFlow(-70)
    val rssiFilterValue = _rssiFilterValue.asStateFlow()

    private val _joinRspReq = MutableStateFlow(true)
    val joinRspReq = _joinRspReq.asStateFlow()

    private val _scanTypePassive = MutableStateFlow(true)
    val scanTypePassive = _scanTypePassive.asStateFlow()

    private val _scanWindowValue = MutableStateFlow(100f)
    val scanWindowValue = _scanWindowValue.asStateFlow()

    private val _scanIntervalValue = MutableStateFlow(100f)
    val scanIntervalValue = _scanIntervalValue.asStateFlow()

    private val _advertisingMinIntervalValue = MutableStateFlow(100f)
    val advertisingMinInterval = _advertisingMinIntervalValue.asStateFlow()

    private val _advertisingMaxIntervalValue = MutableStateFlow(100f)
    val advertisingMaxInterval = _advertisingMaxIntervalValue.asStateFlow()

    private val _advTimeoutValue = MutableStateFlow(5)
    val advTimeoutValue = _advTimeoutValue.asStateFlow()

    val rssiFinal = _rssiFilterValue
        .debounce(300)

    val advTimeoutFinal = _advTimeoutValue
        .debounce(300)

    val scanWindowFinal = _scanWindowValue
        .debounce(300)

    val scanIntervalFinal = _scanIntervalValue
        .debounce(300)

    val usbResultsCount = scanResults.usbResultsCount
    val bleResultsCount = scanResults.bleResultsCount

    private val _usbJobDone = MutableStateFlow(false)
    private val _bleJobDone = MutableStateFlow(false)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            isOn.combine(isScanner) { on, scanner ->
                if (on && scanner) startScan()
                else if (!on && !scanner) stopScan()
            }.collect()
        }
    }

    private var usbCollectJob: Job = viewModelScope.launch(Dispatchers.IO) {
        scanResults.scannedUSBResults
            .onEach {
                it?.let { scanResults.addUSBResult(it) }
            }
            .onCompletion {
                Log.d("SelectedViewModel", "usbCollectJob completed")
                _usbJobDone.value = true
            }
            .collect()
    }

    private var bleCollectJob: Job = viewModelScope.launch(Dispatchers.IO) {
        scanResults.scannedBLEResults
            .onEach {
                it?.let { scanResults.addBLEResult(it) }
            }
            .onCompletion {
                Log.d("SelectedViewModel", "bleCollectJob completed")
                _bleJobDone.value = true
            }
            .collect()
    }

    private var jobsDoneCollectJob: Job = viewModelScope.launch(Dispatchers.IO) {
        _usbJobDone.combine(_bleJobDone) { usb, ble ->
                Log.d("SelectedViewModel", "usb job: $usb, ble job: $ble")
                usb && ble
            }
            .flatMapLatest {
                if (it) { processResults() }
                flow{ emit(it) }
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

    fun changeAdvTimeoutValue(value: Int) {
        _advTimeoutValue.value = value
    }

    fun changeJoinRspReq(newVal: Boolean) {
        _joinRspReq.value = newVal
    }

    fun changeScanTypePassive(newVal: Boolean) {
        _scanTypePassive.value = newVal
    }

    fun changeScanWindowValue(value: Int) {
        Log.d("SelectedViewModel", "changeScanWindowValue: $value")
        // round value to 3 decimals
        val roundValue = value * 0.625f
        if (roundValue < _scanIntervalValue.value) _scanIntervalValue.value = roundValue
        _scanWindowValue.value = roundValue
    }

    fun changeScanIntervalValue(value: Int) {
        Log.d("SelectedViewModel", "changeScanWindowValue: $value")
        val roundValue = value * 0.625f
        if (roundValue > _scanWindowValue.value) _scanWindowValue.value = roundValue
        _scanIntervalValue.value = roundValue
    }

    fun changeAdvertisingMinIntervalValue(value: Float) {
        if (value > _advertisingMaxIntervalValue.value)
            _advertisingMaxIntervalValue.value = value
        _advertisingMinIntervalValue.value = value
    }

    fun changeAdvertisingMaxIntervalValue(value: Float) {
        if (value < _advertisingMinIntervalValue.value)
            _advertisingMinIntervalValue.value = value
        _advertisingMaxIntervalValue.value = value
    }

    fun formatTime3Digits(time: Float): String {
        return "%.3f".format(time)
    }

    fun startScan() {
        _usbJobDone.value = false
        _bleJobDone.value = false
        jobsDoneCollectJob.start()
        usbCollectJob.start()
        bleCollectJob.start()
    }

    fun stopScan() {
        usbCollectJob.cancel()
        bleCollectJob.cancel()
    }

    private suspend fun processResults() {
        yield()
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
        jobsDoneCollectJob.cancel()
    }

}