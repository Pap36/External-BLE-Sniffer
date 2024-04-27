package com.externalblesniffer.ui.selected.views

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.externalblesniffer.ui.selected.datamodel.UIEvents

@Composable
fun LaunchedEffects(
    rssiFinal: Int,
    joinRspReq: Boolean,
    scanTypePassive: Boolean,
    scanWindowFinal: Float,
    advertisingMinInterval: Float,
    advertisingMaxInterval: Float,
    advTimeoutFinal: Int,
    scanIntervalFinal: Float,
    onUIEvent: (UIEvents) -> Unit,
) {
    LaunchedEffect(rssiFinal) {
        onUIEvent(UIEvents.OnRSSIChange(rssiFinal))
    }

    LaunchedEffect(joinRspReq) {
        onUIEvent(UIEvents.OnJoinRspReqChange(joinRspReq))
    }

    LaunchedEffect(scanTypePassive) {
        onUIEvent(UIEvents.OnScanTypePassiveChange(scanTypePassive))
    }

    LaunchedEffect(advertisingMinInterval) {
        onUIEvent(UIEvents.OnAdvertisingMinIntervalChange(advertisingMinInterval))
    }

    LaunchedEffect(advertisingMaxInterval) {
        onUIEvent(UIEvents.OnAdvertisingMaxIntervalChange(advertisingMaxInterval))
    }

    LaunchedEffect(scanWindowFinal) {
        onUIEvent(UIEvents.OnScanWindowValueChange(scanWindowFinal))
    }

    LaunchedEffect(scanIntervalFinal) {
        onUIEvent(UIEvents.OnScanIntervalValueChange(scanIntervalFinal))
    }

    LaunchedEffect(advTimeoutFinal) {
        onUIEvent(UIEvents.OnAdvTimeoutValueChange(advTimeoutFinal))
    }
}