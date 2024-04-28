package com.externalblesniffer.ui.selected.views

import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.externalblesniffer.repo.Utils.encodeHex
import com.externalblesniffer.repo.Utils.toByteArray
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
    startScan: () -> Unit,
    stopScan: () -> Unit
) {

    val hexValueRegex = Regex("[0-9A-Fa-f]{0,4}")
    var scanInterval by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(
            TextFieldValue((scanIntervalValue / 0.625f).toUInt().toUShort()
                .toByteArray().encodeHex(prefixOx = false))
        )
    }
    var scanWindow by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(
            TextFieldValue((scanWindowValue / 0.625f).toUInt().toUShort()
                .toByteArray().encodeHex(prefixOx = false))
        )
    }
    var scanWindowError by rememberSaveable { mutableStateOf(false) }
    var scanIntervalError by rememberSaveable { mutableStateOf(false) }


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

    TextField(
        modifier = modifier,
        value = scanInterval,
        onValueChange = {
            if (hexValueRegex.matches(it.text)) {
                scanInterval = it
                scanIntervalError = false
            } else scanIntervalError = true
        },
        prefix = { Text("Scan Interval: 0x") },
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
        supportingText = {
            Column() {
                Text( "Current value: ${
                    if(scanInterval.text.isNotEmpty())
                        scanInterval.text.toUInt(radix = 16).toFloat() * 0.625f
                    else 0
                } ms")
                Text(
                    if(!scanIntervalError) "Range: 20 ms to 10240 ms (0x0020 -> 0x4000)"
                    else "Invalid value."
                )
            }
        },
        enabled = !isOn,
        isError = scanIntervalError
    )

    TextField(
        modifier = modifier,
        value = scanWindow,
        onValueChange = {
            if (hexValueRegex.matches(it.text)) {
                scanWindow = it
                scanWindowError = false
            } else scanWindowError = true
        },
        prefix = { Text("Scan Window: 0x") },
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
        supportingText = {
            Column() {
                Text( "Current value: ${
                    if(scanWindow.text.isNotEmpty())
                        scanWindow.text.toUInt(radix = 16).toFloat() * 0.625f
                    else 0
                } ms")
                Text(
                    if(!scanWindowError) "Range: 20 ms to 10240 ms (0x0020 -> 0x4000)"
                    else "Invalid value."
                )
            }
        },
        enabled = !isOn,
        isError = scanWindowError
    )

    Button(
        onClick = {
            changeScanIntervalValue(scanInterval.text.toUInt(radix = 16).toInt())
            changeScanWindowValue(scanWindow.text.toUInt(radix = 16).toInt())
        },
        enabled = if (!isOn && !scanIntervalError && !scanWindowError) {
            if (scanInterval.text.length == 4 && scanWindow.text.length == 4) {
                val minVal = scanInterval.text.toUInt(radix = 16).toFloat() * 0.625f
                val maxVal = scanWindow.text.toUInt(radix = 16).toFloat() * 0.625f
                if (minVal in 20f..10240f && maxVal in 20f..10240f) {
                    if (minVal <= maxVal) true
                    else false
                } else false
            } else false
        } else false
    ) {
        Text(text = "Set scan window and interval")
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
        text = "BLE / All % Result Count: ${bleResultsCount.toFloat() * 100 / maximumCount.toFloat()}",
        style = MaterialTheme.typography.titleLarge
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