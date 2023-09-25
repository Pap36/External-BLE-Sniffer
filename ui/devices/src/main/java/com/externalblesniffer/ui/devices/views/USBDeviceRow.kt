package com.externalblesniffer.ui.devices.views

import android.hardware.usb.UsbDevice
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun USBDeviceRow(
    modifier: Modifier = Modifier,
    usbDevice: UsbDevice,
) {
    Column(
        modifier = modifier,
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