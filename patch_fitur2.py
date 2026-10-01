#!/usr/bin/env python3
import shutil, sys
from pathlib import Path

def rd(p): return Path(p).read_text()
def wr(p, s): Path(p).write_text(s)
def sub(p, old, new):
    s = rd(p)
    if old not in s:
        sys.exit(f"GAGAL: teks tidak ditemukan di {p}:\n{old[:140]}")
    wr(p, s.replace(old, new, 1))
def add_import(p, imp):
    s = rd(p); line = f"import {imp}\n"
    if line in s: return
    i = s.index("\nimport ") + 1
    wr(p, s[:i] + line + s[i:])
def find(name):
    r = [p for p in Path(".").rglob(name)
         if ".git" not in p.parts and "build" not in p.parts and "out" not in p.parts]
    if not r:
        sys.exit(f"GAGAL: {name} tidak ada di repo ini. Cek: ls; find . -name {name}")
    print("ketemu:", r[0])
    return r[0]

TH = find("MagiskTheme.kt")
s = rd(TH)
if "object JgTheme" not in s:
    s = s.replace("@Composable\nfun MagiskTheme(", '''object JgTheme {
    private val prefs get() = com.topjohnwu.magisk.core.AppContext.getSharedPreferences("jgisk_theme", 0)

    var accent by mutableStateOf(Color(prefs.getInt("accent", 0xFFB388FF.toInt())))
        private set
    var useMonet by mutableStateOf(prefs.getBoolean("monet", false))
        private set

    fun setAccent(c: Color) {
        accent = c
        useMonet = false
        prefs.edit().putInt("accent", c.toArgb()).putBoolean("monet", false).apply()
    }

    fun setMonet(on: Boolean) {
        useMonet = on
        prefs.edit().putBoolean("monet", on).apply()
    }
}

@Composable
fun MagiskTheme(''', 1)
    s = s.replace("dynamicColorScheme(MagiskAccentColor, isDark = true)", "dynamicColorScheme(JgTheme.accent, isDark = true)")
    s = s.replace("dynamicColorScheme(MagiskAccentColor, isDark = false)", "dynamicColorScheme(JgTheme.accent, isDark = false)")
    s = s.replace("val useDynamicColor = mode.isMonet && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S",
                  "val useDynamicColor = mode.isMonet && JgTheme.useMonet && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S")
    wr(TH, s)
    add_import(TH, "androidx.compose.runtime.mutableStateOf")

MI = find("MagiskInstaller.kt")
s = rd(MI)
if "backupStock" not in s:
    s = s.replace("    private fun patchBoot(): Boolean {\n", '''    private fun backupStock() {
        try {
            val stamp = java.text.SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(java.util.Date())
            val out = MediaStoreUtils.getFile("JGisk-stock-boot-$stamp.img")
            srcBoot.newInputStream().use { input ->
                out.uri.outputStream().use { os -> input.copyAll(os, 1024 * 1024) }
            }
            console.add("- Backup boot stok disimpan: $out")
        } catch (e: Exception) {
            console.add("! Backup boot stok gagal (patch tetap dilanjutkan)")
            Timber.e(e)
        }
    }

    private fun patchBoot(): Boolean {
        backupStock()
''', 1)
    wr(MI, s)

MS = find("MainScreen.kt")
sub(MS, 'text = "Fitur ini aktif setelah HP kamu di-root dengan JGisk. Buka Beranda, pilih Pasang, patch boot image, lalu flash dan reboot.",',
        'text = "Module aktif setelah JGisk terpasang di boot image.\\n\\n1. Beranda > Pasang > patch boot stok.\\n2. Flash dari PC: adb reboot bootloader, lalu fastboot flash init_boot hasil-patch.img (atau boot).\\n3. Reboot dan buka JGisk lagi.\\n\\nShizuku/ADB biasa saja tidak cukup, karena keduanya hanya punya akses shell, bukan root.",')

HS = find("HomeScreen.kt")

extras = find("JgExtras.kt")
if extras.parent != HS.parent:
    shutil.move(str(extras), str(HS.parent / "JgExtras.kt"))
    print("JgExtras.kt dipindah ke", HS.parent)
sub(HS, "    val scrollState = rememberScrollState()\n",
        "    val scrollState = rememberScrollState()\n    var showRepo by remember { mutableStateOf(false) }\n    var showAbout by remember { mutableStateOf(false) }\n")
sub(HS, "        JgCredit(onClick =", '''        JgRootTest()
        JgDeviceCard()
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FilledTonalButton(onClick = { showRepo = true }, modifier = Modifier.weight(1f)) { Text("Repo module") }
            FilledTonalButton(onClick = { showAbout = true }, modifier = Modifier.weight(1f)) { Text("Tentang & tema") }
        }

        JgCredit(onClick =''')
sub(HS, "    InstallDialog(\n        show = showInstallDialog",
        "    if (showRepo) JgModuleRepoDialog(onDismiss = { showRepo = false })\n    if (showAbout) JgAboutDialog(onDismiss = { showAbout = false })\n\n    InstallDialog(\n        show = showInstallDialog")
for imp in ["androidx.compose.runtime.mutableStateOf", "androidx.compose.runtime.getValue", "androidx.compose.runtime.setValue",
            "androidx.compose.runtime.remember", "androidx.compose.foundation.layout.Arrangement",
            "androidx.compose.foundation.layout.Row", "androidx.compose.material3.FilledTonalButton"]:
    add_import(HS, imp)

print("OK: semua fitur ditambahkan")
