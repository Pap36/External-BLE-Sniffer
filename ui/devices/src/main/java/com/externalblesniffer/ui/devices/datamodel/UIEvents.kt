package com.externalblesniffer.ui.devices.datamodel

sealed class UIEvents {
    data object Refresh: UIEvents()
    data class RequestPermission(val usbDeviceID: Int) : UIEvents()
    data class Connect(val usbDeviceID: Int) : UIEvents()
}