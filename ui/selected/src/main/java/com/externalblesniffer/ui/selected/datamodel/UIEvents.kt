package com.externalblesniffer.ui.selected.datamodel

import android.net.Uri

sealed class UIEvents {
    data object StartScan: UIEvents()
    data object StopScan: UIEvents()
    data object StartAdv: UIEvents()
    data object StopAdv: UIEvents()
    data class ExportResults(val uri: Uri): UIEvents()
    data class OnRSSIChange(val rssi: Int): UIEvents()
    data class OnJoinRspReqChange(val joinRspReq: Boolean): UIEvents()
    data class OnAdvertisingParamChange(
        val advertisingMinInterval: Float,
        val advertisingMaxInterval: Float,
        val advTimeout: Int
    ): UIEvents()

    data class OnScanningParamChange(
        val scanWindowValue: Float,
        val scanIntervalValue: Float,
        val scanTypePassive: Boolean
    ): UIEvents()

    data object ReadParams: UIEvents()
    data class OnBoardChange(val isScanner: Boolean): UIEvents()

}