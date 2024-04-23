package com.externalblesniffer.ui.selected.datamodel

import android.net.Uri

sealed class UIEvents {
    data object StartScan: UIEvents()
    data object StopScan: UIEvents()
    data class ExportResults(val uri: Uri): UIEvents()
    data class OnRSSIChange(val rssi: Int): UIEvents()
    data class OnJoinRspReqChange(val joinRspReq: Boolean): UIEvents()
}