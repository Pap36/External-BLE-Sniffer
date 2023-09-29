package com.externalblesniffer.ui.devices.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.externalblesniffer.ui.devices.datamodel.UIEvents
import com.externalblesniffer.ui.devices.viewmodels.DevicesViewModel
import com.externalblesniffer.ui.devices.views.USBDeviceRow

@Composable
fun DevicesScreen(
    modifier: Modifier = Modifier,
    onUIEvent: (UIEvents) -> Unit = {},
) {

    val viewModel = hiltViewModel<DevicesViewModel>()
    val usbDevices by viewModel.usbDevicesFlow.collectAsStateWithLifecycle(null)

    Column(
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp, 16.dp, 16.dp, 0.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Connected USB Devices",
                style = MaterialTheme.typography.titleLarge,
            )
            IconButton(
                onClick = { onUIEvent(UIEvents.Refresh) }
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                )
            }
        }

        Divider(modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp, 8.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            usbDevices?.let {
                items(it) {(permission, usbDevice) ->
                    USBDeviceRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        usbDevice,
                        permission,
                        onUIEvent = onUIEvent,
                    )
                    Divider(modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}