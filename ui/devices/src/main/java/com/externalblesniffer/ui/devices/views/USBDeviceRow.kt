package com.externalblesniffer.ui.devices.views

import android.hardware.usb.UsbDevice
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.externalblesniffer.ui.devices.datamodel.UIEvents
import no.nordicsemi.android.common.theme.nordicGreen

@Composable
fun USBDeviceRow(
    modifier: Modifier = Modifier,
    usbDevice: UsbDevice,
    permission: Boolean,
    onUIEvent: (UIEvents) -> Unit = {},
) {
    Row(
        modifier = modifier
            .clickable {
                if (!permission) onUIEvent(UIEvents.RequestPermissionAndConnect(usbDevice.deviceId))
                else onUIEvent(UIEvents.Connect(usbDevice.deviceId))
            },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = { if(!permission) onUIEvent(UIEvents.RequestPermission(usbDevice.deviceId)) }
        ) {
            Icon(
                imageVector = Icons.Outlined.Circle,
                tint = if(permission) MaterialTheme.colorScheme.nordicGreen else MaterialTheme.colorScheme.error,
                contentDescription = null
            )
        }

        Column(
            modifier = Modifier
                .padding(start = 16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.SpaceEvenly,
        ) {
            Text(text = usbDevice.productName ?: "Unknown Product")
            Text(text = usbDevice.manufacturerName ?: "Unknown Manufacturer")
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(text = "Vendor ID: 0x${usbDevice.vendorId.toString(16)}")
                Text(text = "Product ID: 0x${usbDevice.productId.toString(16)}")
            }
        }
    }
}