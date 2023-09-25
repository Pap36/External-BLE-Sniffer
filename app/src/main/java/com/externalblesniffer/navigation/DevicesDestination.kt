package com.externalblesniffer.navigation

import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.externalblesniffer.ui.devices.datamodel.UIEvents
import com.externalblesniffer.ui.devices.screens.DevicesScreen
import com.externalblesniffer.viewmodels.MainViewModel
import no.nordicsemi.android.common.navigation.defineDestination

val DevicesDestination = defineDestination(Devices) {

    val mainViewModel = hiltViewModel<MainViewModel>()

    DevicesScreen(
        modifier = Modifier.fillMaxSize(),
        onUIEvent = { uiEvent ->
            when (uiEvent) {
                is UIEvents.Refresh -> {
                    Log.d("DevicesDestination", "Refresh")
                    mainViewModel.refresh()
                }
            }
        }
    )
}