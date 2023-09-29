package com.externalblesniffer.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.externalblesniffer.ui.selected.datamodel.UIEvents
import com.externalblesniffer.ui.selected.screens.SelectedScreen
import com.externalblesniffer.viewmodels.MainViewModel
import no.nordicsemi.android.common.navigation.defineDestination

val SelectedDestination = defineDestination(Selected) {

    val mainViewModel = hiltViewModel<MainViewModel>()

    SelectedScreen(
        modifier = Modifier.fillMaxSize(),
        onUIEvent = { uiEvent ->
            when (uiEvent) {
                is UIEvents.Send -> {
                    mainViewModel.send(uiEvent.data)
                }
            }
        }
    )
}