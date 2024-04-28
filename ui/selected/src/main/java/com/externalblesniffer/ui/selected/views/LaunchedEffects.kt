package com.externalblesniffer.ui.selected.views

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.externalblesniffer.ui.selected.datamodel.UIEvents

@Composable
fun LaunchedEffects(
    rssiFinal: Int,
    scanTimeoutFinal: Int,
    joinRspReq: Boolean,
    onUIEvent: (UIEvents) -> Unit,
) {
    LaunchedEffect(rssiFinal) {
        onUIEvent(UIEvents.OnRSSIChange(rssiFinal))
    }

    LaunchedEffect(joinRspReq) {
        onUIEvent(UIEvents.OnJoinRspReqChange(joinRspReq))
    }

    LaunchedEffect(scanTimeoutFinal) {
        onUIEvent(UIEvents.OnScanTimeoutChange(scanTimeoutFinal))
    }

}