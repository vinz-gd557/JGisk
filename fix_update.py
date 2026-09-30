#!/usr/bin/env python3
from pathlib import Path
import sys
p = Path("app/core/src/main/java/com/topjohnwu/magisk/core/repository/NetworkService.kt")
s = p.read_text()
a = s.index("    suspend fun fetchUpdate() = safe {")
b = s.index("    suspend fun fetchUpdate(version: Int)")
new = '''    suspend fun fetchUpdate() = safe {
        val url = "https://api.github.com/repos/vinz-gd557/JGisk/releases/latest"
        val json = org.json.JSONObject(raw.fetchString(url))
        fun org.json.JSONObject.str(k: String) = if (isNull(k)) "" else getString(k)
        val tag = json.str("tag_name")
        var link = ""
        val assets = json.optJSONArray("assets")
        if (assets != null) {
            for (i in 0 until assets.length()) {
                val asset = assets.getJSONObject(i)
                val n = asset.str("name")
                if (n.endsWith(".apk") && !n.contains("debug") && !n.contains("stub")) {
                    link = asset.str("browser_download_url")
                    break
                }
            }
        }
        if (tag.isEmpty() || link.isEmpty()) {
            UpdateInfo()
        } else {
            UpdateInfo(
                version = tag.trimStart('v', 'V'),
                versionCode = JgUpdate.code(tag),
                link = link,
                note = "## " + json.str("name") + "\\n\\n" + json.str("body")
            )
        }
    }

'''
p.write_text(s[:a] + new + s[b:])
print("OK: fungsi update diganti")
