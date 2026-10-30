# Angel - Celengan Digital & Pengingat Harian Cerdas

<p align="center">
  <b>Aplikasi Tabungan & Celengan Digital Standar Produksi Developer Google</b><br>
  <i>100% Offline-First • Material 3 Expressive • SQLite Lokal • WorkManager • Audio Notifikasi MP3 Kustom</i>
</p>

---

## Ringkasan Fitur Unggulan

Angel adalah aplikasi celengan digital modern yang dirancang untuk membangun kebiasaan menabung harian secara konsisten melalui sistem target impian, pelacakan *streak* menabung, dan pengingat otomatis berlatar belakang.

1. **Pengingat Harian Otomatis (Anti-Lupa Menabung):**
   - Didukung oleh `androidx.work:work-runtime-ktx` (`CoroutineWorker`).
   - Melakukan evaluasi otomatis setiap hari pada jam yang ditentukan pengguna (default 20:00 WIB).
   - Jika pengguna belum mencatat setoran tabungan pada hari tersebut, sistem secara otomatis mengirimkan notifikasi interaktif ke bilah status.

2. **Suara Notifikasi Kustom (Angel Chime MP3):**
   - Dilengkapi file audio MP3 kustom (`res/raw/angel_chime.mp3`) dengan nada lonceng harmoni surgawi (C6, E6, G6, B6, C7).
   - Terintegrasi langsung dengan `NotificationChannel` via `AudioAttributes.USAGE_NOTIFICATION`.
   - Fitur uji coba audio langsung di aplikasi dan simulasi notifikasi instan pada menu Pengaturan.

3. **Ikon Aplikasi Tematik "Angel":**
   - Menggunakan Adaptive Vector Icon murni Android (`ic_launcher_background.xml` dan `ic_launcher_foreground.xml`).
   - Simbol geometris elegan: Lingkaran Halo Emas, Sayap Malaikat Surgawi, dan Celengan dengan slot koin emas aktif.
   - Raster PNG beresolusi tinggi tersedia untuk seluruh densitas layar (`mdpi` hingga `xxxhdpi`).

4. **Penyimpanan Database Lokal (Offline-First):**
   - Menggunakan SQLite engine lokal terintegrasi via `SQLiteOpenHelper`.
   - Transaksi ACID atomik dengan integritas referensial Foreign Key (`PRAGMA foreign_keys = ON`).
   - Menyimpan seluruh target celengan (*goals*), riwayat transaksi (*transactions*), dan catatan harian (*daily savings log*).
   - Fitur Cadangkan (Ekspor JSON) dan Pulihkan (Impor JSON) secara mandiri tanpa ketergantungan server luar.

5. **UI/UX Standar Google Developer:**
   - Framework: **Jetpack Compose** dengan desain **Material 3 Expressive**.
   - Navigasi mulus dengan transisi arah horizontal dan fade (`AnimatedContent`).
   - Anti-Loading Spinner: Menggunakan Skeleton Screen ber-shimmer (`Modifier.shimmerEffect`).
   - Anti-Keyboard Overlap: Form input terisolasi dengan `Modifier.imePadding()` dan `windowSoftInputMode="adjustResize"`.
   - Gaya bahasa profesional, lugas, dan bebas kata alay/hiperbolis.

---

## Struktur Arsitektur Kode

```text
app/src/main/java/com/arbdev/angel/
├── MainActivity.kt                      # Entry activity, navigasi animasi, permission handler
├── data/
│   ├── model/
│   │   ├── Goal.kt                      # Model data target celengan & kalkulasi progres
│   │   ├── GoalCategory.kt              # Kategori celengan & palet warna
│   │   ├── TransactionRecord.kt         # Model catatan transaksi (Setor / Tarik)
│   │   └── SavingsStats.kt              # Metrik statistik tabungan & streak harian
│   ├── local/
│   │   └── AngelDatabaseHelper.kt       # Native SQLite database helper & queries
│   ├── preferences/
│   │   └── UserPreferencesManager.kt   # Jetpack DataStore Preferences (Pengaturan jam, notifikasi)
│   └── repository/
│       └── SavingsRepository.kt         # Single source of truth, StateFlow & Coroutines
├── worker/
│   ├── NotificationHelper.kt            # NotificationChannel kustom dengan audio MP3
│   ├── DailyReminderWorker.kt           # CoroutineWorker pengecekan tabungan harian
│   └── ReminderScheduler.kt             # WorkManager scheduler periodik
├── ui/
│   ├── theme/                           # Material 3 Color, Type, Theme
│   ├── components/                      # BottomSheet, Dialogs, StreakIndicator, BottomBar, Skeletons
│   └── screens/
│       ├── DashboardScreen.kt           # Total saldo, streak harian, target aktif, transaksi terkini
│       ├── GoalsScreen.kt               # Manajemen seluruh celengan, setor/tarik, filter
│       ├── HistoryScreen.kt             # Rekap bulanan, riwayat masuk vs keluar
│       └── SettingsScreen.kt            # Konfigurasi pengingat, uji suara MP3, backup/restore
```

---

## Lisensi & Standar Rekayasa

Dibuat dengan standar Google Android Architecture Guidelines:
- Clean Architecture (UI -> ViewModel/StateFlow -> Repository -> Local DB)
- Strictly Monotonic Versioning (`versionCode = 10000`, SemVer `1.0.0`)
- Proguard & Keystore Release Signing Otomatis via GitHub Actions CI/CD.
