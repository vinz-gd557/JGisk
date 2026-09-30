# JGisk (fork Magisk) - GPLv3, kredit: topjohnwu/Magisk
1. Buat repo GitHub PRIVATE, upload isi folder ini (termasuk .github).
2. Actions > "Build JGisk" > Run workflow (cloud, HP tidak terbebani; arm64 saja).
3. Unduh artifact JGisk-apk, install.
4. Di app: Install > pilih file boot/init_boot stok > Patch > flash hasil patch via fastboot.
Backup boot/init_boot stok dulu.

## Struktur JGisk
- Dari Magisk (tidak diubah): native/ (daemon root, su, magiskboot, magiskinit), scripts/, app/core/.
- Ditulis sendiri: app/apk/ (Beranda JGisk dan dock navigasi di MainScreen.kt). Layar Superuser/Modul/Log/Pengaturan masih bawaan Magisk.
