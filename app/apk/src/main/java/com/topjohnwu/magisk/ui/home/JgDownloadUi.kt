package com.topjohnwu.magisk.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.topjohnwu.magisk.core.download.JgDlState
import com.topjohnwu.magisk.core.download.JgDownloadState

@Composable
fun JgDownloadBar() {
    val st by JgDownloadState.state.collectAsState()
    if (st.active || st.failed) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (st.failed) "UNDUHAN GAGAL" else "MENGUNDUH",
                    color = if (st.failed) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.weight(1f)
                )
                if (st.failed) {
                    TextButton(onClick = { JgDownloadState.state.value = JgDlState() }) { Text("Tutup") }
                }
            }
            if (st.title.isNotEmpty()) {
                Text(
                    text = st.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1
                )
            }
            if (st.active) {
                val known = st.totalMb > 0f
                val frac = if (known) (st.doneMb / st.totalMb).coerceIn(0f, 1f) else 0f
                val left = (st.totalMb - st.doneMb).coerceAtLeast(0f)
                Spacer(Modifier.height(10.dp))
                if (known) {
                    LinearProgressIndicator(
                        progress = { frac },
                        modifier = Modifier.fillMaxWidth().height(10.dp).clip(CircleShape)
                    )
                } else {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth().height(10.dp).clip(CircleShape)
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (known) "${(frac * 100).toInt()}%" else "...",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = if (known) "%.1f / %.1f MB".format(st.doneMb, st.totalMb) else "%.1f MB".format(st.doneMb),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = if (known) "sisa %.1f MB".format(left) else "ukuran tidak diketahui",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
