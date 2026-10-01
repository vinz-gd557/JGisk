#!/usr/bin/env python3
import sys
from pathlib import Path

def rd(p): return Path(p).read_text()
def wr(p, s): Path(p).write_text(s)
def sub(p, old, new):
    s = rd(p)
    if old not in s:
        sys.exit(f"GAGAL: teks tidak ditemukan di {p}:\n{old[:140]}")
    wr(p, s.replace(old, new, 1))
def find(name):
    r = [p for p in Path(".").rglob(name)
         if ".git" not in p.parts and "build" not in p.parts and "out" not in p.parts]
    if not r:
        sys.exit(f"GAGAL: {name} tidak ada di repo ini")
    print("ketemu:", r[0])
    return r[0]

DE = find("DownloadEngine.kt")
s = rd(DE)
if "JgDownloadState" not in s:
    sub(DE, "                processor.handle(stream, subject)\n",
            "                processor.handle(stream, subject)\n                JgDownloadState.state.value = JgDlState()\n")
    sub(DE, "                Timber.e(e)\n                notifyFail(subject)\n",
            "                Timber.e(e)\n                JgDownloadState.state.value = JgDlState(failed = true, title = subject.title)\n                notifyFail(subject)\n")
    sub(DE, "            val progress = it.toFloat() / 1048576\n",
            "            val progress = it.toFloat() / 1048576\n            JgDownloadState.state.value = JgDlState(\n                active = true, title = subject.title,\n                doneMb = progress, totalMb = if (max > 0) total else 0f\n            )\n")

PI = find("ProgressInputStream.kt")
sub(PI, "if (cur - lastUpdate > 1000) {", "if (cur - lastUpdate > 300) {")

HS = find("HomeScreen.kt")
s = rd(HS)
if "JgDownloadBar()" not in s:
    sub(HS, "        JgCore(\n", "        JgDownloadBar()\n\n        JgCore(\n")
print("OK: progress bar download ditambahkan")
