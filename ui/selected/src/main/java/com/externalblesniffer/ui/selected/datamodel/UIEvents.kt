package com.externalblesniffer.ui.selected.datamodel

sealed class UIEvents {
    data object StartScan: UIEvents()
    data object StopScan: UIEvents()
}