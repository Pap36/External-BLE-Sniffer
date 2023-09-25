package com.externalblesniffer.ui.devices.datamodel

sealed class UIEvents {
    data object Refresh: UIEvents()
}