package com.topjohnwu.magisk.ui.home

import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.core.content.getSystemService
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.topjohnwu.magisk.R
import com.topjohnwu.magisk.core.BuildConfig
import com.topjohnwu.magisk.core.Config
import com.topjohnwu.magisk.core.Const
import com.topjohnwu.magisk.core.Info
import com.topjohnwu.magisk.core.download.DownloadEngine
import com.topjohnwu.magisk.core.download.Subject
import com.topjohnwu.magisk.core.ktx.reboot
import com.topjohnwu.magisk.core.ktx.toast
import com.topjohnwu.magisk.core.tasks.AppMigration
import com.topjohnwu.magisk.core.tasks.MagiskInstaller
import com.topjohnwu.magisk.ui.MainActivity
import com.topjohnwu.magisk.ui.component.MagiskDialog
import com.topjohnwu.magisk.ui.component.MarkdownTextAsync
import com.topjohnwu.magisk.ui.component.rememberLoadingDialog
import com.topjohnwu.magisk.ui.component.verticalScrollbar
import com.topjohnwu.magisk.ui.flash.FlashUtils
import com.topjohnwu.magisk.ui.install.InstallDialog
import com.topjohnwu.magisk.ui.install.InstallViewModel
import kotlinx.coroutines.launch
import java.io.File
import com.topjohnwu.magisk.core.R as CoreR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    installVm: InstallViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val scope = rememberCoroutineScope()
    val loadingDialog = rememberLoadingDialog()

    var showUninstallDialog by rememberSaveable { mutableStateOf(false) }
    var showManagerDialog by rememberSaveable { mutableStateOf(false) }
    var showEnvFixDialog by rememberSaveable { mutableStateOf(false) }
    var showHideDialog by rememberSaveable { mutableStateOf(false) }
    var showRestoreDialog by rememberSaveable { mutableStateOf(false) }
    var showInstallDialog by rememberSaveable { mutableStateOf(false) }
    var envFixCode by remember { mutableIntStateOf(0) }

    LaunchedEffect(uiState.showUninstall) {
        if (uiState.showUninstall) {
            showUninstallDialog = true
            viewModel.onUninstallConsumed()
        }
    }
    LaunchedEffect(uiState.showManagerInstall) {
        if (uiState.showManagerInstall) {
            showManagerDialog = true
            viewModel.onManagerInstallConsumed()
        }
    }
    LaunchedEffect(uiState.envFixCode) {
        if (uiState.envFixCode != 0) {
            envFixCode = uiState.envFixCode
            showEnvFixDialog = true
            viewModel.onEnvFixConsumed()
        }
    }
    LaunchedEffect(uiState.showHideRestore) {
        if (uiState.showHideRestore) {
            val hidden = context.packageName != BuildConfig.APP_PACKAGE_NAME
            if (hidden) showRestoreDialog = true else showHideDialog = true
            viewModel.onHideRestoreConsumed()
        }
    }

    if (showUninstallDialog) {
        UninstallComposableDialog(
            onDismiss = { showUninstallDialog = false },
            onCompleteUninstall = {
                showUninstallDialog = false
                val intent = Intent(context, context.javaClass).apply {
                    action = FlashUtils.INTENT_FLASH
                    putExtra(FlashUtils.EXTRA_FLASH_ACTION, Const.Value.UNINSTALL)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                }
                context.startActivity(intent)
            },
            onRestoreImage = {
                showUninstallDialog = false
                scope.launch {
                    val success = loadingDialog.withLoading {
                        MagiskInstaller.Restore().exec()
                    }
                    context.toast(
                        if (success) CoreR.string.restore_done else CoreR.string.restore_fail,
                        Toast.LENGTH_SHORT
                    )
                }
            }
        )
    }

    if (showManagerDialog) {
        ManagerInstallComposableDialog(
            cacheDir = context.cacheDir,
            onDismiss = { showManagerDialog = false },
            onInstall = {
                showManagerDialog = false
                (context as? MainActivity)?.let {
                    DownloadEngine.startWithActivity(it, Subject.App())
                }
            }
        )
    }

    if (showEnvFixDialog) {
        EnvFixComposableDialog(
            code = envFixCode,
            onDismiss = { showEnvFixDialog = false },
            onNavigateInstall = {
                showEnvFixDialog = false
                showInstallDialog = true
            },
            onFixEnv = {
                showEnvFixDialog = false
                scope.launch {
                    val success = loadingDialog.withLoading {
                        MagiskInstaller.FixEnv().exec()
                    }
                    context.toast(
                        if (success) CoreR.string.reboot_delay_toast else CoreR.string.setup_fail,
                        Toast.LENGTH_LONG
                    )
                    if (success) {
                        @Suppress("DEPRECATION")
                        Handler(Looper.getMainLooper())
                            .postDelayed({ reboot() }, 5000)
                    }
                }
            }
        )
    }

    if (showHideDialog) {
        HideAppDialog(
            onDismiss = { showHideDialog = false },
            onConfirm = { name ->
                showHideDialog = false
                scope.launch {
                    loadingDialog.withLoading {
                        AppMigration.patchAndHide(context, name)
                    }
                }
            }
        )
    }

    if (showRestoreDialog) {
        RestoreAppDialog(
            onDismiss = { showRestoreDialog = false },
            onConfirm = {
                showRestoreDialog = false
                scope.launch {
                    loadingDialog.withLoading {
                        AppMigration.restoreApp(context)
                    }
                }
            }
        )
    }

    val scrollState = rememberScrollState()
    var showRepo by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }
    val isHidden = context.packageName != BuildConfig.APP_PACKAGE_NAME

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        JgHeader(onDelete = viewModel::onDeletePressed)

        JgCore(
            state = uiState.magiskState,
            version = uiState.magiskInstalledVersion,
            onInstall = { showInstallDialog = true }
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            JgStat(
                label = "MANAGER",
                value = uiState.managerInstalledVersion,
                modifier = Modifier.weight(1f)
            )
            JgStat(
                label = "UPDATE",
                value = when (uiState.appState) {
                    HomeViewModel.State.LOADING -> "Mengecek..."
                    HomeViewModel.State.INVALID -> "Offline"
                    HomeViewModel.State.OUTDATED -> uiState.managerRemoteVersion
                    HomeViewModel.State.UP_TO_DATE -> "Terbaru"
                },
                modifier = Modifier.weight(1f)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FilledTonalButton(
                onClick = viewModel::onManagerPressed,
                modifier = Modifier.weight(1f)
            ) { Text("Cek update") }
            FilledTonalButton(
                onClick = viewModel::onHideRestorePressed,
                modifier = Modifier.weight(1f)
            ) { Text(if (isHidden) "Pulihkan app" else "Sembunyikan app") }
        }

        JgRootTest()
        JgDeviceCard()
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FilledTonalButton(onClick = { showRepo = true }, modifier = Modifier.weight(1f)) { Text("Repo module") }
            FilledTonalButton(onClick = { showAbout = true }, modifier = Modifier.weight(1f)) { Text("Tentang & tema") }
        }

        JgCredit(onClick = { viewModel.onLinkPressed("https://github.com/topjohnwu/Magisk") })
    }

    if (showRepo) JgModuleRepoDialog(onDismiss = { showRepo = false })
    if (showAbout) JgAboutDialog(onDismiss = { showAbout = false })

    InstallDialog(
        show = showInstallDialog,
        onDismiss = { showInstallDialog = false },
        installVm = installVm,
    )
}

@Composable
private fun RebootButton(
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var safeModeEnabled by remember { mutableIntStateOf(Config.bootloop) }

    val showUserspace = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
        context.getSystemService<PowerManager>()?.isRebootingUserspaceSupported == true
    val showSafeMode = Const.Version.atLeast_28_0()

    val items = buildList {
        add(RebootOption(CoreR.string.reboot, Icons.Default.RestartAlt) { reboot() })
        if (showUserspace) {
            add(RebootOption(CoreR.string.reboot_userspace, Icons.Default.Refresh) { reboot("userspace") })
        }
        add(RebootOption(CoreR.string.reboot_recovery, Icons.Default.Build) { reboot("recovery") })
        add(RebootOption(CoreR.string.reboot_bootloader, Icons.Default.Android) { reboot("bootloader") })
        add(RebootOption(CoreR.string.reboot_download, Icons.Default.Download) { reboot("download") })
        add(RebootOption(CoreR.string.reboot_edl, Icons.Default.Memory) { reboot("edl") })
        if (showSafeMode) {
            add(RebootOption(CoreR.string.reboot_safe_mode, Icons.Default.Security) {
                val newVal = if (safeModeEnabled >= 2) 0 else 2
                Config.bootloop = newVal
                safeModeEnabled = newVal
            })
        }
    }

    Box(modifier = modifier) {
        IconButton(
            onClick = { showMenu = true },
        ) {
            Icon(
                imageVector = Icons.Default.PowerSettingsNew,
                contentDescription = stringResource(CoreR.string.reboot),
            )
        }
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
            offset = DpOffset(x = (-8).dp, y = 0.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            items.forEach { item ->
                val isSafeMode = item.labelRes == CoreR.string.reboot_safe_mode
                if (isSafeMode) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                }
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(item.labelRes),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = if (isSafeMode && safeModeEnabled >= 2) {
                        {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    } else null,
                    onClick = {
                        item.action()
                        if (!isSafeMode) showMenu = false
                    }
                )
            }
        }
    }
}

private class RebootOption(val labelRes: Int, val icon: ImageVector, val action: () -> Unit)

@Composable
private fun JgHeader(onDelete: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "JG",
            color = MaterialTheme.colorScheme.primary,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Black,
            fontSize = 32.sp
        )
        Text(
            text = "isk",
            color = MaterialTheme.colorScheme.onSurface,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Light,
            fontSize = 32.sp
        )
        Spacer(Modifier.weight(1f))
        if (Info.env.isActive) {
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Uninstall")
            }
        }
        if (Info.isRooted) {
            RebootButton()
        }
    }
}

@Composable
private fun JgCore(
    state: HomeViewModel.State,
    version: String,
    onInstall: () -> Unit,
) {
    val active = state != HomeViewModel.State.INVALID
    val brush = if (active) {
        Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary))
    } else {
        Brush.linearGradient(listOf(MaterialTheme.colorScheme.error, MaterialTheme.colorScheme.errorContainer))
    }
    val fg = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onError
    val accent = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.extraLarge)
            .background(brush)
            .padding(24.dp)
    ) {
        Column {
            Text(
                text = if (active) "ROOT AKTIF" else "BELUM TERPASANG",
                color = fg.copy(alpha = 0.8f),
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                letterSpacing = 2.sp
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = if (active) version else "Patch boot image untuk mengaktifkan root",
                color = fg,
                style = MaterialTheme.typography.headlineSmall
            )
            if (state == HomeViewModel.State.OUTDATED) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Versi core lebih lama dari app ini. Upgrade disarankan.",
                    color = fg.copy(alpha = 0.85f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(Modifier.height(18.dp))
            Button(
                onClick = onInstall,
                colors = ButtonDefaults.buttonColors(containerColor = fg, contentColor = accent)
            ) {
                Text(if (active) "Upgrade / pasang ulang" else "Pasang")
            }
        }
    }
}

@Composable
private fun JgStat(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(16.dp)
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.primary,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            letterSpacing = 1.5.sp
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 2
        )
    }
}

@Composable
private fun JgCredit(onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(16.dp)
    ) {
        Text(
            text = "Berbasis Magisk",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Mesin root JGisk berasal dari Magisk karya topjohnwu dan kontributor (GPLv3). Ketuk untuk melihat proyek aslinya.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun UninstallComposableDialog(
    onDismiss: () -> Unit,
    onCompleteUninstall: () -> Unit,
    onRestoreImage: () -> Unit,
    modifier: Modifier = Modifier
) {
    MagiskDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = stringResource(CoreR.string.uninstall_magisk_title),
        confirmText = stringResource(CoreR.string.complete_uninstall),
        onConfirm = onCompleteUninstall,
        dismissText = stringResource(CoreR.string.restore_img),
        onDismiss = onRestoreImage,
    ) {
        Text(text = stringResource(CoreR.string.uninstall_magisk_msg))
    }
}

@Composable
private fun ManagerInstallComposableDialog(
    cacheDir: File,
    onDismiss: () -> Unit,
    onInstall: () -> Unit,
    modifier: Modifier = Modifier
) {
    MagiskDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = stringResource(CoreR.string.install),
        confirmText = stringResource(CoreR.string.install),
        onConfirm = onInstall,
        dismissText = stringResource(android.R.string.cancel),
        onDismiss = onDismiss,
        scrollable = true,
    ) {
        MarkdownTextAsync {
            val text = Info.update.note
            File(cacheDir, "${Info.update.versionCode}.md").writeText(text)
            text
        }
    }
}

@Composable
private fun EnvFixComposableDialog(
    code: Int,
    onDismiss: () -> Unit,
    onNavigateInstall: () -> Unit,
    onFixEnv: () -> Unit,
    modifier: Modifier = Modifier
) {
    val needsFullFix = code == 2 ||
        Info.env.versionCode != BuildConfig.APP_VERSION_CODE ||
        Info.env.versionString != BuildConfig.APP_VERSION_NAME

    MagiskDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = stringResource(CoreR.string.env_fix_title),
        confirmText = stringResource(android.R.string.ok),
        onConfirm = {
            if (needsFullFix) onNavigateInstall() else onFixEnv()
        },
        dismissText = stringResource(android.R.string.cancel),
        onDismiss = onDismiss,
    ) {
        Text(
            text = stringResource(
                if (needsFullFix) CoreR.string.env_full_fix_msg else CoreR.string.env_fix_msg
            )
        )
    }
}

@Composable
private fun HideAppDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val defaultName = stringResource(CoreR.string.settings)
    var appName by rememberSaveable { mutableStateOf(defaultName) }
    val isError = appName.length > AppMigration.MAX_LABEL_LENGTH || appName.isBlank()

    MagiskDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = stringResource(CoreR.string.settings_hide_app_title),
        confirmText = stringResource(android.R.string.ok),
        onConfirm = { if (!isError) onConfirm(appName) },
        confirmEnabled = !isError,
        dismissText = stringResource(android.R.string.cancel),
        onDismiss = onDismiss,
    ) {
        Column(modifier = Modifier.padding(top = 8.dp)) {
            OutlinedTextField(
                value = appName,
                onValueChange = { appName = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(CoreR.string.settings_app_name_hint)) },
                isError = isError
            )
        }
    }
}

@Composable
private fun RestoreAppDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    MagiskDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = stringResource(CoreR.string.settings_restore_app_title),
        confirmText = stringResource(android.R.string.ok),
        onConfirm = onConfirm,
        dismissText = stringResource(android.R.string.cancel),
        onDismiss = onDismiss,
    ) {
        Text(text = stringResource(CoreR.string.restore_app_confirmation))
    }
}
