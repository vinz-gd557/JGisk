package com.topjohnwu.magisk.ui.install

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.net.URLEncoder

private fun enc(q: String): String = URLEncoder.encode(q, "UTF-8")

@Composable
fun JgBootFinderDialog(
    onDismiss: () -> Unit,
    onDownload: (Uri) -> Unit,
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    var build by rememberSaveable { mutableStateOf(Build.DISPLAY ?: "") }
    var link by rememberSaveable { mutableStateOf("") }
    var linkError by rememberSaveable { mutableStateOf(false) }

    fun google(q: String) = uriHandler.openUri("https://www.google.com/search?q=" + enc(q))
    val model = Build.MODEL ?: ""

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier
                    .statusBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Cari boot.img",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = onDismiss) { Text("Tutup") }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.medium)
                        .background(MaterialTheme.colorScheme.errorContainer)
                        .padding(14.dp)
                ) {
                    Text(
                        text = "Penting",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Boot image harus SAMA PERSIS dengan build number HP kamu. Image beda versi atau beda perangkat bisa bikin bootloop. File dari sumber tidak dikenal juga bisa berbahaya. Utamakan firmware resmi atau thread XDA yang tepercaya.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }

                OutlinedTextField(
                    value = build,
                    onValueChange = { build = it },
                    label = { Text("Build number") },
                    supportingText = { Text("Terisi otomatis dari HP ini. Bisa diedit.") },
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "Model: $model\nFingerprint: ${Build.FINGERPRINT}",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FilledTonalButton(
                    onClick = {
                        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        cm.setPrimaryClip(ClipData.newPlainText("build", build))
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Salin build number") }

                Text(
                    text = "Cari di web",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                FilledTonalButton(
                    onClick = { google("\"$build\" boot.img OR init_boot.img download") },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Google: boot.img / init_boot.img") }
                FilledTonalButton(
                    onClick = { google("site:xdaforums.com $model $build boot.img") },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("XDA Forums") }
                FilledTonalButton(
                    onClick = { google("site:github.com $model $build boot.img firmware") },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("GitHub") }
                FilledTonalButton(
                    onClick = { google("$model $build stock rom firmware download") },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Firmware stok lengkap") }

                Text(
                    text = "Sudah ketemu link-nya?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Tempel link langsung (https) ke file .img, atau firmware .zip yang berisi boot/init_boot. JGisk akan mengunduh lalu mem-patch otomatis. Hasilnya tetap harus kamu flash sendiri.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = link,
                    onValueChange = {
                        link = it
                        linkError = false
                    },
                    label = { Text("Link unduhan") },
                    isError = linkError,
                    supportingText = { if (linkError) Text("Link harus diawali https://") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = {
                        val u = Uri.parse(link.trim())
                        if (u.scheme.equals("https", ignoreCase = true) && u.host.isNullOrEmpty().not()) {
                            onDownload(u)
                        } else {
                            linkError = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Unduh & patch") }
            }
        }
    }
}
