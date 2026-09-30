#!/usr/bin/env python3
import re, sys
from pathlib import Path

A = Path("app")
def rd(p): return Path(p).read_text()
def wr(p, s):
    Path(p).parent.mkdir(parents=True, exist_ok=True)
    Path(p).write_text(s)
def sub(p, old, new, count=1):
    s = rd(p)
    if old not in s:
        sys.exit(f"GAGAL: teks tidak ditemukan di {p}:\n{old[:120]}")
    wr(p, s.replace(old, new, count))
def add_import(p, imp):
    s = rd(p)
    line = f"import {imp}\n"
    if line in s: return
    i = s.index("\nimport ") + 1
    wr(p, s[:i] + line + s[i:])

CORE = A / "core/src/main/java/com/topjohnwu/magisk/core"
RES = A / "core/src/main/res"

sub(A / "shared/src/main/AndroidManifest.xml", 'android:label="Magisk"', 'android:label="JGisk"')

BODY = '''    <path android:fillColor="#B388FF" android:pathData="M360,90 L590,180 L590,360 C590,500 490,590 360,640 C230,590 130,500 130,360 L130,180 Z"/>
    <path android:fillColor="#0B0B12" android:fillAlpha="0.55" android:pathData="M360,132 L552,207 L552,360 C552,478 470,556 360,600 C250,556 168,478 168,360 L168,207 Z"/>
    <path android:strokeColor="#FFFFFF" android:strokeWidth="46" android:strokeLineCap="round" android:strokeLineJoin="round" android:pathData="M420,250 L420,400 C420,462 382,494 336,494 C302,494 276,476 262,448"/>
    <path android:fillColor="#B388FF" android:pathData="M316,250 a26,26 0 1,0 52,0 a26,26 0 1,0 -52,0"/>
'''
HEAD = '<vector xmlns:android="http://schemas.android.com/apk/res/android"\n    android:width="{d}dp" android:height="{d}dp"\n    android:viewportWidth="{v}" android:viewportHeight="{v}">\n'
wr(RES / "drawable/ic_magisk.xml", HEAD.format(d=48, v=720) + BODY + "</vector>\n")
wr(RES / "drawable/ic_magisk_padded.xml",
   HEAD.format(d=108, v=1080)
   + '    <group android:translateX="180" android:translateY="180" android:scaleX="1.3" android:scaleY="1.3" android:pivotX="360" android:pivotY="360">\n'
   + BODY + "    </group>\n</vector>\n")
wr(RES / "drawable/ic_magisk_outline.xml", HEAD.format(d=48, v=720) + '''    <path android:strokeColor="#FFFFFF" android:strokeWidth="30" android:strokeLineJoin="round" android:pathData="M360,100 L580,186 L580,360 C580,494 486,580 360,628 C234,580 140,494 140,360 L140,186 Z"/>
    <path android:strokeColor="#FFFFFF" android:strokeWidth="46" android:strokeLineCap="round" android:strokeLineJoin="round" android:pathData="M420,250 L420,400 C420,462 382,494 336,494 C302,494 276,476 262,448"/>
    <path android:fillColor="#FFFFFF" android:pathData="M316,250 a26,26 0 1,0 52,0 a26,26 0 1,0 -52,0"/>
</vector>
''')
sub(RES / "drawable/ic_logo.xml", "#00AF9C", "#0B0B12")
sub(RES / "values/colors.xml", '<color name="ic_launcher_background">#00AF9C</color>', '<color name="ic_launcher_background">#0B0B12</color>')

wr(CORE / "utils/JgUpdate.kt", '''package com.topjohnwu.magisk.core.utils

import com.topjohnwu.magisk.core.BuildConfig

/** Perbandingan versi JGisk: tag release "v1.2" vs versi app "JGisk-1.2". */
object JgUpdate {
    private fun nums(s: String): List<Int> =
        Regex("\\\\d+").findAll(s).mapNotNull { it.value.toIntOrNull() }.toList()

    fun code(tag: String): Int {
        val n = nums(tag)
        return n.getOrElse(0) { 0 } * 10000 + n.getOrElse(1) { 0 } * 100 + n.getOrElse(2) { 0 }
    }

    fun isNewer(remote: String): Boolean {
        val a = nums(remote)
        val b = nums(BuildConfig.APP_VERSION_NAME)
        for (i in 0 until maxOf(a.size, b.size)) {
            val x = a.getOrElse(i) { 0 }
            val y = b.getOrElse(i) { 0 }
            if (x != y) return x > y
        }
        return false
    }
}
''')

sub(CORE / "model/UpdateInfo.kt", '''    val versionCode: Int get() {
        return if (tag[0] == 'v') {
            (tag.drop(1).toFloat() * 1000).toInt()
        } else {
            tag.drop(7).toInt()
        }
    }''', '''    val versionCode: Int get() = com.topjohnwu.magisk.core.utils.JgUpdate.code(tag)''')

NS = CORE / "repository/NetworkService.kt"
s = rd(NS)
a = s.index("    suspend fun fetchUpdate() = safe {")
b = s.index("    suspend fun fetchUpdate(version: Int)")
s = s[:a] + '''    suspend fun fetchUpdate() = safe {
        val release = api.fetchLatestRelease()
        val apk = release.assets.firstOrNull {
            it.name.endsWith(".apk") && !it.name.contains("debug") && !it.name.contains("stub")
        }
        if (apk == null) {
            UpdateInfo()
        } else {
            UpdateInfo(
                version = release.tag.trimStart('v', 'V'),
                versionCode = JgUpdate.code(release.tag),
                link = apk.url,
                note = "## ${release.name}\\n\\n${release.body}"
            )
        }
    }

''' + s[b:]
wr(NS, s)
add_import(NS, "com.topjohnwu.magisk.core.utils.JgUpdate")

RI = CORE / "data/RetrofitInterfaces.kt"
s = rd(RI)
s = s.replace('@Path("owner") owner: String = "topjohnwu"', '@Path("owner") owner: String = "vinz-gd557"')
s = s.replace('@Path("repo") repo: String = "Magisk"', '@Path("repo") repo: String = "JGisk"')
wr(RI, s)

HVM = A / "apk/src/main/java/com/topjohnwu/magisk/ui/home/HomeViewModel.kt"
sub(HVM, "appState = if (BuildConfig.APP_VERSION_CODE < versionCode) State.OUTDATED else State.UP_TO_DATE,",
         "appState = if (JgUpdate.isNewer(version)) State.OUTDATED else State.UP_TO_DATE,")
add_import(HVM, "com.topjohnwu.magisk.core.utils.JgUpdate")

JS = CORE / "JobService.kt"
sub(JS, "Info.env.isActive && BuildConfig.APP_VERSION_CODE < it.versionCode", "Info.env.isActive && JgUpdate.isNewer(it.version)")
add_import(JS, "com.topjohnwu.magisk.core.utils.JgUpdate")

sub(CORE / "download/Subject.kt", '"Magisk-${json.version}(${json.versionCode})"', '"JGisk-${json.version}"')

MS = A / "apk/src/main/java/com/topjohnwu/magisk/ui/MainScreen.kt"
sub(MS, "Tab.MODULES -> Info.env.isActive && LocalModule.loaded()", "Tab.MODULES -> true")
sub(MS, "    val initialPage = visibleTabs.indexOf(",
        "    val modulesUnlocked = remember { Info.env.isActive && LocalModule.loaded() }\n    val initialPage = visibleTabs.indexOf(")
sub(MS, "icon = ImageVector.vectorResource(tab.iconRes),",
        "icon = if (tab == Tab.MODULES && !modulesUnlocked) Icons.Default.Lock\n                                else ImageVector.vectorResource(tab.iconRes),")
sub(MS, "                    Tab.MODULES -> {\n",
        '''                    Tab.MODULES -> if (!modulesUnlocked) {
                        JgLockedPage(onGoHome = {
                            scope.launch {
                                pagerState.animateScrollToPage(visibleTabs.indexOf(Tab.HOME).coerceAtLeast(0))
                            }
                        })
                    } else {
''')
s = rd(MS) + '''
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
            text = "Fitur ini aktif setelah HP kamu di-root dengan JGisk. Buka Beranda, pilih Pasang, patch boot image, lalu flash dan reboot.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(20.dp))
        Button(onClick = onGoHome) { Text("Ke Beranda") }
    }
}
'''
wr(MS, s)
for imp in ["androidx.compose.material.icons.filled.Lock", "androidx.compose.foundation.layout.Column",
            "androidx.compose.foundation.layout.Spacer", "androidx.compose.foundation.layout.height",
            "androidx.compose.material3.Button", "androidx.compose.ui.text.style.TextAlign",
            "androidx.compose.ui.text.font.FontWeight"]:
    add_import(MS, imp)

print("OK: semua patch berhasil diterapkan")
