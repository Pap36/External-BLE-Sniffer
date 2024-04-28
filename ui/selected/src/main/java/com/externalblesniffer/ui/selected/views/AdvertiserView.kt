package com.externalblesniffer.ui.selected.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Slider
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
import com.externalblesniffer.repo.Utils.encodeHex
import com.externalblesniffer.repo.Utils.toByteArray

@Composable
fun AdvertiserView(
    modifier: Modifier,
    advertisingMinInterval: Float,
    advertisingMaxInterval: Float,
    advTimeoutValue: Int,
    isOn: Boolean,
    changeAdvertisingMinInterval: (Float) -> Unit,
    changeAdvertisingMaxInterval: (Float) -> Unit,
    changeAdvTimeoutValue: (Int) -> Unit,
    startAdv: () -> Unit,
    stopAdv: () -> Unit
) {

    val hexValueRegex = Regex("[0-9A-Fa-f]{0,4}")
    var minIntervalValue by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(
            TextFieldValue((advertisingMinInterval / 0.625f).toUInt().toUShort()
                .toByteArray().encodeHex(prefixOx = false))
        )
    }
    var maxIntervalValue by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(
            TextFieldValue((advertisingMaxInterval / 0.625f).toUInt().toUShort()
                .toByteArray().encodeHex(prefixOx = false))
        )
    }
    var isMinIntervalError by rememberSaveable { mutableStateOf(false) }
    var isMaxIntervalError by rememberSaveable { mutableStateOf(false) }

    TextField(
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
            Column() {
                Text( "Current value: ${ 
                    if(minIntervalValue.text.isNotEmpty()) 
                        minIntervalValue.text.toUInt(radix = 16).toFloat() * 0.625f 
                    else 0
                } ms")
                Text(
                    if(!isMinIntervalError) "Range: 20 ms to 10240 ms (0x0020 -> 0x4000)"
                    else "Invalid value."
                )
            }
        },
        enabled = !isOn,
        isError = isMinIntervalError
    )

    TextField(
        modifier = modifier,
        value = maxIntervalValue,
        onValueChange = {
            if (hexValueRegex.matches(it.text)) {
                maxIntervalValue = it
                isMaxIntervalError = false
            } else isMaxIntervalError = true
        },
        prefix = { Text("Minimum Advertising Interval: 0x") },
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
        supportingText = {
            Column() {
                Text( "Current value: ${
                    if(maxIntervalValue.text.isNotEmpty())
                        maxIntervalValue.text.toUInt(radix = 16).toFloat() * 0.625f
                    else 0
                } ms")
                Text(
                    if(!isMaxIntervalError) "Range: 20 ms to 10240 ms (0x0020 -> 0x4000)"
                    else "Invalid value."
                )
            }
        },
        enabled = !isOn,
        isError = isMaxIntervalError
    )

    Button(
        onClick = {
            changeAdvertisingMinInterval(minIntervalValue.text.toUInt(radix = 16).toFloat() * 0.625f)
            changeAdvertisingMaxInterval(maxIntervalValue.text.toUInt(radix = 16).toFloat() * 0.625f)
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
        Text(text = "Set advertising intervals")
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = "Advertising timeout: ${advTimeoutValue}s")
        Slider(
            value = (advTimeoutValue / 5).toFloat(),
            onValueChange = {
                changeAdvTimeoutValue(it.toInt() * 5)
            },
            valueRange = 1f..12f,
            steps = 12,
            enabled = !isOn
        )
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Button(onClick = {
            startAdv()
        }) {
            Text(text = "Start advertising")
        }

        Button(onClick = {
            stopAdv()
        }) {
            Text(text = "Stop advertising")
        }
    }
}