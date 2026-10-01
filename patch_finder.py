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
         if ".git" not in p.parts and "build" not in p.parts and "out" not in p.parts
         and "apk-legacy" not in p.parts]
    if not r:
        sys.exit(f"GAGAL: {name} tidak ada di repo ini")
    print("ketemu:", r[0])
    return r[0]

VM = find("InstallViewModel.kt")
if "downloadFromUrl" not in rd(VM):
    sub(VM, "    fun install() {\n", '''    fun downloadFromUrl(uri: Uri) {
        _uiState.update { it.copy(method = Method.DOWNLOAD, patchUri = uri) }
        install()
    }

    fun install() {
''')

ID = find("InstallDialog.kt")
if "showFinder" not in rd(ID):
    sub(ID, "    var showDownloadDialog by rememberSaveable { mutableStateOf(false) }\n",
            "    var showDownloadDialog by rememberSaveable { mutableStateOf(false) }\n    var showFinder by rememberSaveable { mutableStateOf(false) }\n")
    sub(ID, "    if (showDownloadDialog) {\n", '''    if (showFinder) {
        JgBootFinderDialog(
            onDismiss = { showFinder = false },
            onDownload = { uri ->
                showFinder = false
                onDismiss()
                installVm.downloadFromUrl(uri)
            }
        )
    }

    if (showDownloadDialog) {
''')
    sub(ID, '''                        SettingsArrow(
                            title = stringResource(CoreR.string.select_patch_file),''', '''                        SettingsArrow(
                            title = "Cari boot.img sesuai build number",
                            onClick = { showFinder = true },
                        )

                        SettingsArrow(
                            title = stringResource(CoreR.string.select_patch_file),''')
print("OK: pencari boot.img ditambahkan")
