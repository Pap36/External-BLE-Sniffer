package com.externalblesniffer.ui.selected.views

import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.externalblesniffer.ui.selected.datamodel.UIEvents

@Composable
fun ScannerView(
    modifier: Modifier,
    rssiFilterValue: Int,
    scanWindowValue: Float,
    scanIntervalValue: Float,
    joinRspReq: Boolean,
    scanTypePassive: Boolean,
    usbResultsCount: Int,
    bleResultsCount: Int,
    maximumCount: Int,
    isOn: Boolean,
    onUIEvent: (UIEvents) -> Unit,
    fileExporter: ActivityResultLauncher<String>,
    changeRSSI: (Int) -> Unit,
    changeJoinRspReq: (Boolean) -> Unit,
    changeScanTypePassive: (Boolean) -> Unit,
    changeScanWindowValue: (Int) -> Unit,
    changeScanIntervalValue: (Int) -> Unit,
    formatTime3Digits: (Float) -> String,
    startScan: () -> Unit,
    stopScan: () -> Unit
) {

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = "RSSI: $rssiFilterValue dBm")
        Slider(
            value = -rssiFilterValue.toFloat(),
            onValueChange = {
                changeRSSI(-it.toInt())
            },
            valueRange = 30f..100f,
            steps = 71,
            enabled = !isOn
        )
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = "USB Scan Window: ${formatTime3Digits(scanWindowValue)} ms")
        Slider(
            value = scanWindowValue / 0.625f,
            onValueChange = {
                changeScanWindowValue(it.toInt())
            },
            valueRange = 160f..1600f,
            steps = 1441,
            enabled = !isOn
        )
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = "USB Scan Interval: ${formatTime3Digits(scanIntervalValue)} ms")
        Slider(
            value = scanIntervalValue / 0.625f,
            onValueChange = {
                changeScanIntervalValue(it.toInt())
            },
            valueRange = 160f..1600f,
            steps = 1441,
            enabled = !isOn
        )
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("Join Scan RSP with Initial ADV")
        Switch(
            checked = joinRspReq,
            onCheckedChange = {
                changeJoinRspReq(it)
            },
            enabled = !isOn
        )
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("nRF Scan Type: ${if (scanTypePassive) "Passive" else "Active"}")
        Switch(
            checked = scanTypePassive,
            onCheckedChange = {
                changeScanTypePassive(it)
            },
            enabled = !isOn
        )
    }

    Text(
        modifier = modifier,
        text = "USB: $usbResultsCount",
    )
    Row(
        modifier = modifier,
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
        modifier = modifier,
        text = "BLE / All % Result Count: ${bleResultsCount.toFloat() * 100 / maximumCount.toFloat()}",
        style = MaterialTheme.typography.titleLarge
    )

    Text(
        modifier = modifier,
        text = "BLE: $bleResultsCount",
    )
    Row(
        modifier = modifier,
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
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Button(onClick = {
            onUIEvent(UIEvents.StartScan)
            startScan()
        }) {
            Text(text = "Start scanning")
        }

        Button(onClick = {
            onUIEvent(UIEvents.StopScan)
            stopScan()
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