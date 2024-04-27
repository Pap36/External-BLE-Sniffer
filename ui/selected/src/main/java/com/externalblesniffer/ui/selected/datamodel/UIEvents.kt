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
    data class OnScanTypePassiveChange(val scanTypePassive: Boolean): UIEvents()
    data class OnScanWindowValueChange(val scanWindowValue: Float): UIEvents()
    data class OnScanIntervalValueChange(val scanIntervalValue: Float): UIEvents()
    data class OnAdvertisingMinIntervalChange(val advertisingMinInterval: Float): UIEvents()
    data class OnAdvertisingMaxIntervalChange(val advertisingMaxInterval: Float): UIEvents()
    data class OnAdvTimeoutValueChange(val advTimeoutValue: Int): UIEvents()
    data class OnBoardChange(val isScanner: Boolean): UIEvents()
}