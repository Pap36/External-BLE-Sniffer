package com.externalblesniffer.ui.selected.screens

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.externalblesniffer.ui.selected.datamodel.UIEvents
import com.externalblesniffer.ui.selected.viewmodels.SelectedViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
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
    val isScanning by viewModel.isScanning.collectAsStateWithLifecycle()
    val rssiFilterValue by viewModel.rssiFilterValue.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()
    val rssiFinal by viewModel.rssiFinal.collectAsStateWithLifecycle(initialValue = -70)

    val fileExporter = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument(mimeType = "application/json")
    ) { uri ->
        if(uri != null) coroutineScope.launch(Dispatchers.IO) {
            onUIEvent(UIEvents.ExportResults(uri))
        }
    }

    LaunchedEffect(rssiFinal) {
        onUIEvent(UIEvents.onRSSIChange(rssiFinal))
    }

    Column(
        modifier = modifier.padding(16.dp, 0.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly,
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = "RSSI: $rssiFilterValue dBm")
            Slider(
                value = -rssiFilterValue.toFloat(),
                onValueChange = {
                    viewModel.changeRSSI(-it.toInt())
                },
                valueRange = 30f..100f,
                steps = 71,
                enabled = !isScanning
            )
        }

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
            LinearProgressIndicator(
                modifier = Modifier.padding(start=16.dp),
                progress = { usbResultsCount.toFloat() / maximumCount.toFloat() },
            )
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
            LinearProgressIndicator(
                modifier = Modifier.padding(start=16.dp),
                progress = { bleResultsCount.toFloat() / maximumCount.toFloat() },
            )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Button(onClick = {
                onUIEvent(UIEvents.StartScan)
                viewModel.startScan()
            }) {
                Text(text = "Start scanning")
            }

            Button(onClick = {
                onUIEvent(UIEvents.StopScan)
                viewModel.stopScan()
            }) {
                Text(text = "Stop scanning")
            }
        }

        Button(onClick = {
            fileExporter.launch("sniffedResults")
        }) {
            Text(text = "Export Results to JSON")
        }
    }

}