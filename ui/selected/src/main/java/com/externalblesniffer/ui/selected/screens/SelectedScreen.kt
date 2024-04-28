package com.externalblesniffer.ui.selected.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.externalblesniffer.ui.selected.datamodel.UIEvents
import com.externalblesniffer.ui.selected.viewmodels.SelectedViewModel
import com.externalblesniffer.ui.selected.views.AdvertiserView
import com.externalblesniffer.ui.selected.views.LaunchedEffects
import com.externalblesniffer.ui.selected.views.ScannerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun SelectedScreen(
    modifier: Modifier = Modifier,
    onUIEvent: (UIEvents) -> Unit = {  }
) {

    val viewModel = hiltViewModel<SelectedViewModel>()
    val maximumCount by viewModel.maximumCount.collectAsStateWithLifecycle()
    val usbResultsCount by viewModel.usbResultsCount.collectAsStateWithLifecycle()
    val bleResultsCount by viewModel.bleResultsCount.collectAsStateWithLifecycle()
    val isOn by viewModel.isOn.collectAsStateWithLifecycle()
    val rssiFilterValue by viewModel.rssiFilterValue.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()
    val rssiFinal by viewModel.rssiFinal.collectAsStateWithLifecycle(initialValue = -70)
    val joinRspReq by viewModel.joinRspReq.collectAsStateWithLifecycle()
    val isScanner by viewModel.isScanner.collectAsStateWithLifecycle()

    // board params
    val boardParameters by viewModel.boardParameters.collectAsStateWithLifecycle()

    val fileExporter = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument(mimeType = "application/json")
    ) { uri ->
        if(uri != null) coroutineScope.launch(Dispatchers.IO) {
            onUIEvent(UIEvents.ExportResults(uri))
        }
    }

    val boardState = if (isOn) {
        if (isScanner) "Scanning" else "Advertising"
    } else {
        if (isScanner) "Not Scanning" else "Not Advertising"
    }

    LaunchedEffects(
        rssiFinal = rssiFinal,
        joinRspReq = joinRspReq,
        onUIEvent = onUIEvent,
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        // verticalArrangement = Arrangement.SpaceEvenly,
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp, 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Board Type: ${if (isScanner) "Scanner" else "Advertiser"}")
            Switch(
                checked = isScanner,
                onCheckedChange = {
                    onUIEvent(UIEvents.OnBoardChange(it))
                },
                enabled = !isOn
            )
        }

        Text(

            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp, top = 4.dp, bottom = 8.dp),
            text = "Board State: $boardState"
        )

        HorizontalDivider(modifier = Modifier.fillMaxWidth())

        if (isScanner) ScannerView(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp, 4.dp),
            rssiFilterValue = rssiFilterValue,
            joinRspReq = joinRspReq,
            usbResultsCount = usbResultsCount,
            bleResultsCount = bleResultsCount,
            maximumCount = maximumCount,
            isOn = isOn,
            boardParameters = boardParameters,
            onUIEvent = onUIEvent,
            fileExporter = fileExporter,
            changeRSSI = viewModel::changeRSSI,
            changeJoinRspReq = viewModel::changeJoinRspReq,
            startScan = {
                onUIEvent(UIEvents.StartScan)
                viewModel.startScan()
            },
            stopScan = {
                onUIEvent(UIEvents.StopScan)
                viewModel.stopScan()
            },
            setScanParameters = { window, interval, passive ->
                onUIEvent(UIEvents.OnScanningParamChange(window, interval, passive))
            },
            readParameters = { onUIEvent(UIEvents.ReadParams) }
        ) else AdvertiserView(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp, 4.dp),
            isOn = isOn,
            boardParameters = boardParameters,
            startAdv = { onUIEvent(UIEvents.StartAdv) },
            stopAdv = { onUIEvent(UIEvents.StopAdv) },
            setAdvParameters = { min, max, timeout ->
                onUIEvent(UIEvents.OnAdvertisingParamChange(min, max, timeout))
            },
            readParameters = { onUIEvent(UIEvents.ReadParams) }
        )
    }

}