package com.externalblesniffer.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.externalblesniffer.ui.devices.screens.DevicesScreen
import no.nordicsemi.android.common.navigation.defineDestination

val DevicesDestination = defineDestination(Devices) {
    DevicesScreen(
        modifier = Modifier.fillMaxSize(),
    )
}