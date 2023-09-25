package com.externalblesniffer.ui.devices.viewmodels

import androidx.lifecycle.ViewModel
import com.externalblesniffer.repo.USBDevices
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

@HiltViewModel
class DevicesViewModel @Inject constructor(
    usbDevices: USBDevices,
): ViewModel() {

    val usbDevicesFlow = usbDevices.usbDevices
        .flatMapLatest {
            flow {
                emit(it?.values?.toList())
            }
        }
        .flowOn(Dispatchers.IO)

}