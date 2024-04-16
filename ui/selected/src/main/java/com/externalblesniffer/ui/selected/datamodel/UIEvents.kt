package com.externalblesniffer.ui.selected.datamodel

import android.net.Uri

sealed class UIEvents {
    data object StartScan: UIEvents()
    data object StopScan: UIEvents()
    data class ExportResults(val uri: Uri): UIEvents()
    data class onRSSIChange(val rssi: Int): UIEvents()
}