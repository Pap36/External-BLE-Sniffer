package com.externalblesniffer.ui.devices.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DevicesScreen(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp, 16.dp, 16.dp, 0.dp),
        ) {
            Text(
                "Connected USB Devices:",
                style = MaterialTheme.typography.titleLarge,
            )
        }

        Divider(modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp, 8.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
        ) {

        }
    }
}