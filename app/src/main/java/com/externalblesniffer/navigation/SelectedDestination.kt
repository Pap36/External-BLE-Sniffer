package com.externalblesniffer.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.externalblesniffer.ui.selected.datamodel.UIEvents
import com.externalblesniffer.ui.selected.screens.SelectedScreen
import com.externalblesniffer.viewmodels.MainViewModel
import no.nordicsemi.android.common.navigation.defineDestination
import no.nordicsemi.android.common.permissions.ble.RequireBluetooth

val SelectedDestination = defineDestination(Selected) {

    val mainViewModel = hiltViewModel<MainViewModel>()
    RequireBluetooth {
        BackHandler {
            mainViewModel.disconnect()
        }

        SelectedScreen(
            modifier = Modifier.fillMaxSize(),
            onUIEvent = { uiEvent ->
                when (uiEvent) {
                    is UIEvents.StartScan -> {
                        mainViewModel.startScan()
                    }
                    is UIEvents.StopScan -> {
                        mainViewModel.stopScan()
                    }
                    is UIEvents.StartAdv -> {
                        mainViewModel.startAdv()
                    }
                    is UIEvents.StopAdv -> {
                        mainViewModel.stopAdv()
                    }
                    is UIEvents.ExportResults -> {
                        mainViewModel.exportResults(uiEvent.uri)
                    }
                    is UIEvents.OnRSSIChange -> {
                        mainViewModel.changeRSSI(uiEvent.rssi)
                    }
                    is UIEvents.OnScanTimeoutChange -> {
                        mainViewModel.changeScanTimeout(uiEvent.scanTimeout)
                    }
                    is UIEvents.OnJoinRspReqChange -> {
                        mainViewModel.changeJoinRspReq(uiEvent.joinRspReq)
                    }
                    is UIEvents.OnAdvertisingParamChange -> {
                        mainViewModel.changeAdvertisingParam(
                            uiEvent.advertisingMinInterval,
                            uiEvent.advertisingMaxInterval,
                            uiEvent.advTimeout
                        )
                    }
                    is UIEvents.OnScanningParamChange -> {
                        mainViewModel.changeScanningParam(
                            uiEvent.scanWindowValue,
                            uiEvent.scanIntervalValue,
                            uiEvent.scanTypePassive
                        )
                    }
                    is UIEvents.OnBoardChange -> {
                        mainViewModel.changeBoard(uiEvent.isScanner)
                    }
                    is UIEvents.ReadParams -> {
                        mainViewModel.readParameters()
                    }
                }
            }
        )
    }
}