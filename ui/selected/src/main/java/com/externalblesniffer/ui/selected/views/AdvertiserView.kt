package com.externalblesniffer.ui.selected.views

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue

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
    TextField(
        modifier = modifier,
        value = TextFieldValue(
            advertisingMinInterval.toString(),
            selection = TextRange(advertisingMinInterval.toString().length)
        ),
        onValueChange = {
            Log.d("AdvertiserView", "onValueChange: ${it.text}")
            changeAdvertisingMinInterval(it.text.toFloat())
        },
        prefix = { Text("Minimum Advertising Interval: ") },
        suffix = { Text("ms") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        supportingText = {
            Text( "Range: 20 ms to 10240 ms" )
        },
        enabled = !isOn
    )

    TextField(
        modifier = modifier,
        value = TextFieldValue(advertisingMaxInterval.toString()),
        onValueChange = {
            changeAdvertisingMaxInterval(it.text.toFloat())
        },
        prefix = { Text("Minimum Advertising Interval: ") },
        suffix = { Text("ms") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        supportingText = {
            Text( "Range: 20 ms to 10240 ms" )
        },
        enabled = !isOn
    )

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