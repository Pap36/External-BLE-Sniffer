package com.externalblesniffer.viewmodels

import android.hardware.usb.UsbManager
import androidx.lifecycle.ViewModel
import com.externalblesniffer.usb.MyUSBManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val usbManager: MyUSBManager,
): ViewModel() {

    fun refresh() = usbManager.refresh()

}