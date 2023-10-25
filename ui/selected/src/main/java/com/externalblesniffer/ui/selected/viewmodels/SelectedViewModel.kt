package com.externalblesniffer.ui.selected.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.externalblesniffer.repo.ScanResults
import com.externalblesniffer.repo.datamodel.BLEScanResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SelectedViewModel @Inject constructor(
    private val scanResults: ScanResults,
): ViewModel() {

    val usbResultsCount = scanResults.usbResultsCount
    val bleResultsCount = scanResults.bleResultsCount

    init {
        viewModelScope.launch(Dispatchers.IO) { scanResults.scannedUSBResults.collect() }
        viewModelScope.launch(Dispatchers.IO) { scanResults.scannedBLEResults.collect() }
    }

    val maximumCount = usbResultsCount
        .combine(bleResultsCount) { usb, ble ->
            if (usb > ble && usb != 0) usb else if (ble != 0) ble else 1
        }
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 1)

    fun processResults() {
        val usbResults = scanResults.usbResults
        val bleResults = scanResults.bleResults
        var dif = 0
        val processed = arrayListOf<BLEScanResult>()
        for(bleResult in bleResults) {
            if (processed.any { it.data.contentEquals(bleResult.data) }) continue
            processed.add(bleResult)
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
            val bleCount = bleResults.count { /*it.adv_type == usbResult.adv_type &&*/ it.data.contentEquals(usbResult.data) }
            val usbCount = usbResults.count { /*it.adv_type == usbResult.adv_type &&*/ it.data.contentEquals(usbResult.data) }
            dif += if(usbCount >= bleCount) usbCount - bleCount else 0
        }
        Log.d("SelectedViewModel", "Total messages scanned by usb and not ble: $dif / ${usbResults.size}")
    }

}