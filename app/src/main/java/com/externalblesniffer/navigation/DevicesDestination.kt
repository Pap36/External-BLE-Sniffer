package com.externalblesniffer.navigation

import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.externalblesniffer.ui.devices.datamodel.UIEvents
import com.externalblesniffer.ui.devices.screens.DevicesScreen
import com.externalblesniffer.viewmodels.MainViewModel
import no.nordicsemi.android.common.navigation.defineDestination
import no.nordicsemi.android.common.navigation.viewmodel.SimpleNavigationViewModel

val DevicesDestination = defineDestination(Devices) {

    val mainViewModel = hiltViewModel<MainViewModel>()
    val navigation = hiltViewModel<SimpleNavigationViewModel>()

    DevicesScreen(
        modifier = Modifier.fillMaxSize(),
        onUIEvent = { uiEvent ->
            when (uiEvent) {
                is UIEvents.Refresh -> {
                    Log.d("DevicesDestination", "Refresh")
                    mainViewModel.refresh()
                }
                is UIEvents.RequestPermission -> {
                    Log.d("DevicesDestination", "RequestPermission")
                    mainViewModel.requestPermission(uiEvent.usbDeviceID)
                }
                is UIEvents.RequestPermissionAndConnect -> {
                    Log.d("DevicesDestination", "RequestPermissionAndConnect")
                    val success = mainViewModel.requestPermissionAndConnect(uiEvent.usbDeviceID)
                    if (success) navigation.navigateTo(Selected)
                    // TODO else display snackbar
                }
                is UIEvents.Connect -> {
                    Log.d("DevicesDestination", "Connect")
                    val success = mainViewModel.connect(uiEvent.usbDeviceID)
                    if (success) navigation.navigateTo(Selected)
                    // TODO else display snackbar
                }
            }
        }
    )
}