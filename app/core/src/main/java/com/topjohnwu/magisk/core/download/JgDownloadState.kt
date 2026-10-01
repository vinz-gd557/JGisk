package com.topjohnwu.magisk.core.download

import kotlinx.coroutines.flow.MutableStateFlow

data class JgDlState(
    val active: Boolean = false,
    val failed: Boolean = false,
    val title: String = "",
    val doneMb: Float = 0f,
    val totalMb: Float = 0f,
)

object JgDownloadState {
    val state = MutableStateFlow(JgDlState())
}
