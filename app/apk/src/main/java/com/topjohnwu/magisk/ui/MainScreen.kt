package com.topjohnwu.magisk.ui

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.Button
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.filled.Lock
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ShortNavigationBar
import androidx.compose.material3.ShortNavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.topjohnwu.magisk.R
import com.topjohnwu.magisk.arch.VMFactory
import com.topjohnwu.magisk.core.Info
import com.topjohnwu.magisk.core.model.module.LocalModule
import com.topjohnwu.magisk.ui.home.HomeScreen
import com.topjohnwu.magisk.ui.home.HomeViewModel
import com.topjohnwu.magisk.ui.install.InstallViewModel
import com.topjohnwu.magisk.ui.log.LogScreen
import com.topjohnwu.magisk.ui.log.LogViewModel
import com.topjohnwu.magisk.ui.module.ModuleScreen
import com.topjohnwu.magisk.ui.module.ModuleViewModel
import com.topjohnwu.magisk.ui.navigation.CollectNavEvents
import com.topjohnwu.magisk.ui.navigation.LocalNavigator
import com.topjohnwu.magisk.ui.settings.SettingsScreen
import com.topjohnwu.magisk.ui.settings.SettingsViewModel
import com.topjohnwu.magisk.ui.superuser.SuperuserScreen
import com.topjohnwu.magisk.ui.superuser.SuperuserViewModel
import kotlinx.coroutines.launch
import com.topjohnwu.magisk.core.R as CoreR

enum class Tab(val titleRes: Int, val iconRes: Int) {
    MODULES(CoreR.string.modules, R.drawable.ic_module),
    SUPERUSER(CoreR.string.superuser, CoreR.drawable.ic_superuser),
    HOME(CoreR.string.section_home, R.drawable.ic_home),
    LOG(CoreR.string.logs, R.drawable.ic_bug),
    SETTINGS(CoreR.string.settings, R.drawable.ic_settings);
}

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    initialTab: Int = Tab.HOME.ordinal,
    superuserViewModel: SuperuserViewModel? = null,
    onAuthenticate: ((onSuccess: () -> Unit) -> Unit)? = null,
) {
    val navigator = LocalNavigator.current
    val scope = rememberCoroutineScope()
    val visibleTabs = remember {
        Tab.entries.filter { tab ->
            when (tab) {
                Tab.SUPERUSER -> Info.showSuperUser
                Tab.MODULES -> true
                else -> true
            }
        }
    }
    val modulesUnlocked = remember { Info.env.isActive && LocalModule.loaded() }
    val initialPage = visibleTabs.indexOf(Tab.entries[initialTab]).coerceAtLeast(0)
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { visibleTabs.size })
    val fabFocusRequester = remember { FocusRequester() }
    val navModulesFocusRequester = remember { FocusRequester() }
    val moduleContentFocusRequester = remember { FocusRequester() }
    var moduleFabAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    val isModulesTab = visibleTabs.getOrNull(pagerState.currentPage) == Tab.MODULES

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    tonalElevation = 3.dp,
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier.padding(6.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        visibleTabs.forEachIndexed { index, tab ->
                            val isModulesItem = tab == Tab.MODULES
                            JgDockItem(
                                selected = pagerState.currentPage == index,
                                onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                                modifier = Modifier
                                    .then(
                                        if (isModulesItem) Modifier.focusRequester(navModulesFocusRequester)
                                        else Modifier
                                    )
                                    .focusProperties {
                                        if (isModulesTab && isModulesItem) {
                                            up = fabFocusRequester
                                        }
                                    },
                                icon = if (tab == Tab.MODULES && !modulesUnlocked) Icons.Default.Lock
                                else ImageVector.vectorResource(tab.iconRes),
                                label = stringResource(tab.titleRes),
                            )
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = isModulesTab && moduleFabAction != null,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut(),
            ) {
                FloatingActionButton(
                    onClick = { moduleFabAction?.invoke() },
                    modifier = Modifier
                        .focusRequester(fabFocusRequester)
                        .focusProperties {
                            up = moduleContentFocusRequester
                            down = navModulesFocusRequester
                            right = FocusRequester.Cancel
                        },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(CoreR.string.module_action_install_external),
                        modifier = Modifier.size(28.dp),
                    )
                }
            }
        }
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
            beyondViewportPageCount = 0,
            userScrollEnabled = true,
        ) { page ->
            val isCurrentPage = pagerState.currentPage == page
            val tab = visibleTabs[page]
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .focusProperties {
                        onEnter = {
                            if (!isCurrentPage) {
                                cancelFocusChange()
                            }
                        }
                        onExit = {
                            if (requestedFocusDirection == FocusDirection.Left ||
                                requestedFocusDirection == FocusDirection.Right ||
                                requestedFocusDirection == FocusDirection.Up
                            ) {
                                cancelFocusChange()
                            }
                        }
                        if (tab == Tab.MODULES) {
                            down = fabFocusRequester
                        }
                    }
                    .focusGroup()
            ) {
                when (visibleTabs[page]) {
                    Tab.HOME -> {
                        val vm: HomeViewModel = viewModel(factory = VMFactory)
                        val installVm: InstallViewModel = viewModel(factory = VMFactory)
                        LaunchedEffect(isCurrentPage) {
                            if (isCurrentPage) vm.startLoading()
                        }
                        CollectNavEvents(vm, navigator)
                        CollectNavEvents(installVm, navigator)
                        HomeScreen(vm, installVm)
                    }
                    Tab.SUPERUSER -> {
                        val activity = LocalActivity.current as? ComponentActivity
                        val vm: SuperuserViewModel = superuserViewModel
                            ?: if (activity != null) {
                                viewModel(viewModelStoreOwner = activity, factory = VMFactory)
                            } else {
                                viewModel(factory = VMFactory)
                            }
                        LaunchedEffect(onAuthenticate) {
                            if (onAuthenticate != null) {
                                vm.authenticate = onAuthenticate
                            }
                        }
                        LaunchedEffect(isCurrentPage) {
                            if (isCurrentPage) vm.startLoading()
                        }
                        SuperuserScreen(vm)
                    }
                    Tab.LOG -> {
                        val vm: LogViewModel = viewModel(factory = VMFactory)
                        LaunchedEffect(isCurrentPage) {
                            if (isCurrentPage) vm.startLoading()
                        }
                        LogScreen(vm)
                    }
                    Tab.MODULES -> if (!modulesUnlocked) {
                        JgLockedPage(onGoHome = {
                            scope.launch {
                                pagerState.animateScrollToPage(visibleTabs.indexOf(Tab.HOME).coerceAtLeast(0))
                            }
                        })
                    } else {
                        val vm: ModuleViewModel = viewModel(factory = VMFactory)
                        LaunchedEffect(isCurrentPage) {
                            if (isCurrentPage) vm.startLoading()
                        }
                        CollectNavEvents(vm, navigator)
                        ModuleScreen(
                            viewModel = vm,
                            onRegisterFab = { moduleFabAction = it },
                            contentFocusRequester = moduleContentFocusRequester,
                        )
                    }
                    Tab.SETTINGS -> {
                        val vm: SettingsViewModel = viewModel(factory = VMFactory)
                        LaunchedEffect(onAuthenticate) {
                            if (onAuthenticate != null) {
                                vm.authenticate = onAuthenticate
                            }
                        }
                        CollectNavEvents(vm, navigator)
                        SettingsScreen(vm)
                    }
                }
            }
        }
    }
}

@Composable
private fun JgDockItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
) {
    val bg = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
    val fg = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = if (selected) 16.dp else 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = label, tint = fg, modifier = Modifier.size(24.dp))
        AnimatedVisibility(visible = selected) {
            Text(
                text = label,
                color = fg,
                maxLines = 1,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Composable
private fun JgLockedPage(onGoHome: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(64.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Module terkunci",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Module aktif setelah JGisk terpasang di boot image.\n\n1. Beranda > Pasang > patch boot stok.\n2. Flash dari PC: adb reboot bootloader, lalu fastboot flash init_boot hasil-patch.img (atau boot).\n3. Reboot dan buka JGisk lagi.\n\nShizuku/ADB biasa saja tidak cukup, karena keduanya hanya punya akses shell, bukan root.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(20.dp))
        Button(onClick = onGoHome) { Text("Ke Beranda") }
    }
}
