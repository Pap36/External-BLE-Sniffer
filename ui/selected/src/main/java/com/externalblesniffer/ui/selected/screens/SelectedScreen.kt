package com.externalblesniffer.ui.selected.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.externalblesniffer.ui.selected.datamodel.UIEvents
import com.externalblesniffer.ui.selected.viewmodels.SelectedViewModel

@Composable
fun SelectedScreen(
    modifier: Modifier = Modifier,
    onUIEvent: (UIEvents) -> Unit = {  }
) {

    val viewModel = hiltViewModel<SelectedViewModel>()
    val latestReceivedData by viewModel.latestReceivedData.collectAsStateWithLifecycle()
    var toSend by remember {
        mutableStateOf(TextFieldValue())
    }


    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly,
    ) {
        Text("Received data: ${String(latestReceivedData ?: byteArrayOf(), Charsets.UTF_8)}")
        TextField(
            value = toSend,
            onValueChange = {
                toSend = it
            }
        )
        Button(onClick = { onUIEvent(UIEvents.Send(toSend.text.encodeToByteArray())) }) {
            Text("Send")
        }
    }

}