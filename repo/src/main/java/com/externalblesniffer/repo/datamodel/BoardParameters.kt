package com.externalblesniffer.repo.datamodel

import javax.inject.Singleton

@Singleton
data class BoardParameters(
    val joinRspReq: Boolean = true,
    val scanTypePassive: Boolean = true,
    val scanWindowValue: Float = 0.0f,
    val scanIntervalValue: Float = 0.0f,
    val advertisingMinInterval: Float = 0.0f,
    val advertisingMaxInterval: Float = 0.0f,
    val advTimeout: Int = 5,
    val isScanner: Boolean = false,
)