package com.externalblesniffer.ui.selected.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import com.externalblesniffer.repo.Utils.encodeHex
import com.externalblesniffer.repo.Utils.toByteArray
import com.externalblesniffer.repo.datamodel.BoardParameters

@Composable
fun AdvertiserView(
    modifier: Modifier,
    isOn: Boolean,
    boardParameters: BoardParameters,
    setAdvParameters: (Float, Float, Int) -> Unit,
    readParameters: () -> Unit,
    startAdv: () -> Unit,
    stopAdv: () -> Unit
) {

    val hexValueRegex = Regex("[0-9A-Fa-f]{0,4}")
    var minIntervalValue by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue("00A0"))
    }
    var maxIntervalValue by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue("00A0"))
    }
    var advTimeOutValue by rememberSaveable { mutableIntStateOf(5) }
    var isMinIntervalError by rememberSaveable { mutableStateOf(false) }
    var isMaxIntervalError by rememberSaveable { mutableStateOf(false) }

    OutlinedTextField(
        modifier = modifier,
        value = minIntervalValue,
        onValueChange = {
            if (hexValueRegex.matches(it.text)) {
                minIntervalValue = it
                isMinIntervalError = false
            } else isMinIntervalError = true
        },
        prefix = { Text("Minimum Advertising Interval: 0x") },
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
        supportingText = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text( "Current value: ${
                        if(minIntervalValue.text.isNotEmpty())
                            minIntervalValue.text.toUInt(radix = 16).toFloat() * 0.625f
                        else 0
                    } ms")

                    Text(text = "Board value: ${boardParameters.advertisingMinInterval} ms")
                }
                Text(
                    modifier = Modifier.align(Alignment.End),
                    text = "(${(boardParameters.advertisingMinInterval / 0.625f)
                        .toUInt().toUShort().toByteArray().encodeHex(true)})"
                )
                Text(
                    if(!isMinIntervalError) "Range: 20 ms to 10240 ms (0x0020 -> 0x4000)"
                    else "Invalid value."
                )
            }
        },
        enabled = !isOn,
        isError = isMinIntervalError
    )

    OutlinedTextField(
        modifier = modifier,
        value = maxIntervalValue,
        onValueChange = {
            if (hexValueRegex.matches(it.text)) {
                maxIntervalValue = it
                isMaxIntervalError = false
            } else isMaxIntervalError = true
        },
        prefix = { Text("Maximum Advertising Interval: 0x") },
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
        supportingText = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text( "Current value: ${
                        if(maxIntervalValue.text.isNotEmpty())
                            maxIntervalValue.text.toUInt(radix = 16).toFloat() * 0.625f
                        else 0
                    } ms")

                    Text(text = "Board value: ${boardParameters.advertisingMaxInterval} ms")
                }
                Text(
                    modifier = Modifier.align(Alignment.End),
                    text = "(${(boardParameters.advertisingMaxInterval / 0.625f)
                        .toUInt().toUShort().toByteArray().encodeHex(true)})"
                )
                Text(
                    if(!isMaxIntervalError) "Range: 20 ms to 10240 ms (0x0020 -> 0x4000)"
                    else "Invalid value."
                )
            }
        },
        enabled = !isOn,
        isError = isMaxIntervalError
    )

    Slider(
        modifier = modifier,
        value = (advTimeOutValue / 5).toFloat(),
        onValueChange = {
            advTimeOutValue = it.toInt() * 5
        },
        valueRange = 1f..12f,
        steps = 12,
        enabled = !isOn
    )

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = "Advertising timeout: ${advTimeOutValue}s")
        Text(text = "Board value: ${boardParameters.advTimeout}s")
    }

    Column(
        modifier = Modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.SpaceEvenly,
    ) {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Button(
                onClick = {
                    setAdvParameters(
                        minIntervalValue.text.toUInt(radix = 16).toFloat() * 0.625f,
                        maxIntervalValue.text.toUInt(radix = 16).toFloat() * 0.625f,
                        advTimeOutValue
                    )
                },
                enabled = if (!isOn && !isMinIntervalError && !isMaxIntervalError) {
                    if (minIntervalValue.text.length == 4 && maxIntervalValue.text.length == 4) {
                        val minVal = minIntervalValue.text.toUInt(radix = 16).toFloat() * 0.625f
                        val maxVal = maxIntervalValue.text.toUInt(radix = 16).toFloat() * 0.625f
                        if (minVal in 20f..10240f && maxVal in 20f..10240f) {
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
                    startAdv()
                }
            ) {
                Text(text = "Start advertising")
            }

            Button(
                enabled = isOn,
                onClick = {
                    stopAdv()
                }
            ) {
                Text(text = "Stop advertising")
            }
        }
    }
}