package com.externalblesniffer.ui.selected.views

import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.externalblesniffer.repo.datamodel.BoardParameters
import com.externalblesniffer.ui.selected.datamodel.UIEvents

@Composable
fun ScannerView(
    modifier: Modifier,
    rssiFilterValue: Int,
    scanTimeoutValue: Int,
    joinRspReq: Boolean,
    usbResultsCount: Int,
    bleResultsCount: Int,
    maximumCount: Int,
    isOn: Boolean,
    boardParameters: BoardParameters,
    onUIEvent: (UIEvents) -> Unit,
    fileExporter: ActivityResultLauncher<String>,
    changeRSSI: (Int) -> Unit,
    changeScanTimeout: (Int) -> Unit,
    changeJoinRspReq: (Boolean) -> Unit,
    setScanParameters: (Float, Float, Boolean) -> Unit,
    readParameters: () -> Unit,
    startScan: () -> Unit,
    stopScan: () -> Unit,
) {

    val hexValueRegex = Regex("[0-9A-Fa-f]{0,4}")
    var scanInterval by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue("00A0"))
    }
    var scanWindow by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue("00A0"))
    }
    var scanWindowError by rememberSaveable { mutableStateOf(false) }
    var scanIntervalError by rememberSaveable { mutableStateOf(false) }
    var scanTypePassive by rememberSaveable { mutableStateOf(true) }

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
        Text(text = "Scan Timeout: ${scanTimeoutValue}s")
        Slider(
            value = scanTimeoutValue.toFloat() / 30f,
            onValueChange = {
                changeScanTimeout(it.toInt() * 30)
            },
            valueRange = 1f..20f,
            steps = 21,
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

    OutlinedTextField(
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
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("Current value: ${
                        if (scanInterval.text.isNotEmpty())
                            scanInterval.text.toUInt(radix = 16).toFloat() * 0.625f
                        else 0
                    } ms")

                    Text(text = "Board value: ${boardParameters.scanIntervalValue} ms")
                }
                Text(
                    modifier = Modifier.align(Alignment.End),
                    text = "(${(boardParameters.scanIntervalValue / 0.625f)
                        .toUInt().toUShort().toByteArray().encodeHex(true)})"
                )
                Text(
                    if(!scanIntervalError) "Range: 2.5 ms to 10240 ms (0x0004 -> 0x4000)"
                    else "Invalid value."
                )
            }
        },
        enabled = !isOn,
        isError = scanIntervalError
    )

    OutlinedTextField(
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
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("Current value: ${
                        if (scanWindow.text.isNotEmpty())
                            scanWindow.text.toUInt(radix = 16).toFloat() * 0.625f
                        else 0
                    } ms")
                    Text(text = "Board value: ${boardParameters.scanWindowValue} ms")
                }
                Text(
                    modifier = Modifier.align(Alignment.End),
                    text = "(${(boardParameters.scanWindowValue / 0.625f)
                        .toUInt().toUShort().toByteArray().encodeHex(true)})"
                )
                Text(
                    if (!scanWindowError) "Range: 2.5 ms to 10240 ms (0x0004 -> 0x4000)"
                    else "Invalid value."
                )

            }
        },
        enabled = !isOn,
        isError = scanWindowError
    )

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("nRF Scan Type: ${if (scanTypePassive) "Passive" else "Active"}")
        Switch(
            checked = scanTypePassive,
            onCheckedChange = {scanTypePassive = it },
            enabled = !isOn
        )
    }

    Text(
        modifier = modifier,
        text = "Board value: ${if (boardParameters.scanTypePassive) "Passive" else "Active"}"
    )

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

    Column(
        modifier = Modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Button(
                onClick = {
                    setScanParameters(
                        scanInterval.text.toUInt(radix = 16).toFloat() * 0.625f,
                        scanWindow.text.toUInt(radix = 16).toFloat() * 0.625f,
                        scanTypePassive
                    )
                },
                enabled = if (!isOn && !scanIntervalError && !scanWindowError) {
                    if (scanInterval.text.length == 4 && scanWindow.text.length == 4) {
                        val minVal = scanInterval.text.toUInt(radix = 16).toFloat() * 0.625f
                        val maxVal = scanWindow.text.toUInt(radix = 16).toFloat() * 0.625f
                        if (minVal in 2.5f..10240f && maxVal in 2.5f..10240f) {
                            if (minVal <= maxVal) true
                            else false
                        } else false
                    } else false
                } else false
            ) {
                Text(text = "Set parameters")
            }

            Button(
                onClick = readParameters,
                enabled = !isOn
            ) {
                Text(text = "Read parameters")
            }
        }

        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Button(
                enabled = !isOn,
                onClick = {
                    onUIEvent(UIEvents.StartScan)
                    startScan()
                }
            ) {
                Text(text = "Start scanning")
            }

            Button(
                enabled = isOn,
                onClick = {
                    onUIEvent(UIEvents.StopScan)
                    stopScan()
                }
            ) {
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