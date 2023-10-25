package com.externalblesniffer.ui.selected.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.externalblesniffer.ui.selected.datamodel.UIEvents
import com.externalblesniffer.ui.selected.viewmodels.SelectedViewModel
import no.nordicsemi.android.common.theme.view.ProgressItem

@Composable
fun SelectedScreen(
    modifier: Modifier = Modifier,
    onUIEvent: (UIEvents) -> Unit = {  }
) {

    val viewModel = hiltViewModel<SelectedViewModel>()
    val maximumCount by viewModel.maximumCount.collectAsStateWithLifecycle()
    val usbResultsCount by viewModel.usbResultsCount.collectAsStateWithLifecycle()
    val bleResultsCount by viewModel.bleResultsCount.collectAsStateWithLifecycle()


    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly,
    ) {

        Text(
            modifier = Modifier.fillMaxWidth(),
            text = "USB: $usbResultsCount",
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = "USB Result Count:")
            LinearProgressIndicator(progress = usbResultsCount.toFloat() / maximumCount.toFloat())
        }

        Text(
            modifier = Modifier.fillMaxWidth(),
            text = "BLE / All % Result Count: ${bleResultsCount.toFloat() * 100 / maximumCount.toFloat()}",
            style = MaterialTheme.typography.titleLarge
        )

        Text(
            modifier = Modifier.fillMaxWidth(),
            text = "BLE: $bleResultsCount",
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = "BLE Result Count:")
            LinearProgressIndicator(progress = bleResultsCount.toFloat() / maximumCount.toFloat())
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Button(onClick = { onUIEvent(UIEvents.StartScan) }) {
                Text(text = "Start scanning")
            }

            Button(onClick = {
                onUIEvent(UIEvents.StopScan)
                viewModel.processResults()
            }) {
                Text(text = "Stop scanning")
            }
        }
    }

}