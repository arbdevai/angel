package com.arbdev.angel.util

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import com.arbdev.angel.data.model.GitHubReleaseResponse
import com.arbdev.angel.data.model.UpdateState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.concurrent.TimeUnit

class UpdateManager(private val context: Context) {

    companion object {
        const val GITHUB_REPO_OWNER = "arbdevai"
        const val GITHUB_REPO_NAME = "angel"
        const val LATEST_RELEASE_URL = "https://api.github.com/repos/$GITHUB_REPO_OWNER/$GITHUB_REPO_NAME/releases/latest"

        fun isVersionNewer(remote: String, local: String): Boolean {
            try {
                val rClean = remote.removePrefix("v").trim()
                val lClean = local.removePrefix("v").trim()
                val rParts = rClean.split(".").map { it.toIntOrNull() ?: 0 }
                val lParts = lClean.split(".").map { it.toIntOrNull() ?: 0 }
                val maxLen = maxOf(rParts.size, lParts.size)

                for (i in 0 until maxLen) {
                    val r = rParts.getOrElse(i) { 0 }
                    val l = lParts.getOrElse(i) { 0 }
                    if (r > l) return true
                    if (r < l) return false
                }
            } catch (_: Exception) { }
            return false
        }

        fun cleanupOldApks(context: Context, keepVersion: String? = null) {
            try {
                val downloadDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                    ?: context.cacheDir
                val keepFileName = if (keepVersion != null) "angel-v$keepVersion.apk" else null

                downloadDir.listFiles { file -> file.isFile && file.name.endsWith(".apk") }?.forEach { file ->
                    if (keepFileName == null || file.name != keepFileName) {
                        file.delete()
                    }
                }
            } catch (_: Exception) { }
        }
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var downloadTrackingJob: Job? = null

    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    val currentAppVersion: String
        get() = try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            pInfo.versionName ?: "1.3.0"
        } catch (_: Exception) {
            "1.3.0"
        }

    init {
        // Bersihkan installer APK lama yang versinya sudah sama atau lebih lama dari versi saat ini
        cleanupOldApks(context, currentAppVersion)
    }

    fun checkForUpdates() {
        if (_updateState.value is UpdateState.Checking || _updateState.value is UpdateState.Downloading) return

        _updateState.value = UpdateState.Checking
        scope.launch {
            try {
                val request = Request.Builder()
                    .url(LATEST_RELEASE_URL)
                    .header("Accept", "application/vnd.github.v3+json")
                    .header("User-Agent", "Angel-Android-App")
                    .build()

                val response = okHttpClient.newCall(request).execute()
                val responseBody = response.body?.string()

                if (!response.isSuccessful || responseBody.isNullOrBlank()) {
                    _updateState.value = UpdateState.Error("Gagal memeriksa server pembaruan (Kode: ${response.code})")
                    return@launch
                }

                val release = json.decodeFromString<GitHubReleaseResponse>(responseBody)
                val remoteTag = release.tagName.removePrefix("v").trim()
                val currentVersion = currentAppVersion

                if (isVersionNewer(remoteTag, currentVersion)) {
                    val apkAsset = release.assets.firstOrNull { it.name.endsWith(".apk") }
                    val downloadUrl = apkAsset?.downloadUrl ?: ""
                    val sizeFormatted = if (apkAsset != null && apkAsset.sizeBytes > 0) {
                        String.format(java.util.Locale.US, "%.1f MB", apkAsset.sizeBytes / (1024.0 * 1024.0))
                    } else {
                        "~13 MB"
                    }

                    val downloadDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.cacheDir
                    val expectedApk = File(downloadDir, "angel-v$remoteTag.apk")
                    val isLocallyReady = expectedApk.exists() && expectedApk.length() > 5 * 1024 * 1024

                    _updateState.value = UpdateState.UpdateAvailable(
                        latestVersion = remoteTag,
                        releaseNotes = cleanChangelog(release.body),
                        downloadUrl = downloadUrl,
                        fileSizeFormatted = sizeFormatted,
                        isApkReadyLocally = isLocallyReady,
                        localApkPath = if (isLocallyReady) expectedApk.absolutePath else null
                    )
                } else {
                    _updateState.value = UpdateState.UpToDate(currentVersion)
                }
            } catch (e: Exception) {
                _updateState.value = UpdateState.Error("Koneksi gagal saat mengecek pembaruan: ${e.message}")
            }
        }
    }

    fun startDownload(downloadUrl: String, version: String) {
        if (downloadUrl.isBlank()) {
            _updateState.value = UpdateState.Error("URL unduhan tidak valid.")
            return
        }

        cleanupOldApks(context, version)

        val downloadDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.cacheDir
        val targetFile = File(downloadDir, "angel-v$version.apk")

        if (targetFile.exists() && targetFile.length() > 5 * 1024 * 1024) {
            _updateState.value = UpdateState.ReadyToInstall(targetFile.absolutePath, version)
            return
        }

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
        if (downloadManager == null) {
            _updateState.value = UpdateState.Error("Layanan DownloadManager tidak tersedia di perangkat ini.")
            return
        }

        try {
            val uri = Uri.parse(downloadUrl)
            val request = DownloadManager.Request(uri).apply {
                setTitle("Mengunduh Angel v$version")
                setDescription("Mengunduh pembaruan aplikasi Angel...")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, "angel-v$version.apk")
            }

            val downloadId = downloadManager.enqueue(request)
            _updateState.value = UpdateState.Downloading(0)

            downloadTrackingJob?.cancel()
            downloadTrackingJob = scope.launch {
                trackDownloadProgress(downloadManager, downloadId, targetFile, version)
            }
        } catch (e: Exception) {
            _updateState.value = UpdateState.Error("Gagal memulai unduhan: ${e.message}")
        }
    }

    private suspend fun trackDownloadProgress(
        downloadManager: DownloadManager,
        downloadId: Long,
        targetFile: File,
        version: String
    ) = withContext(Dispatchers.IO) {
        var isDownloading = true
        while (isDownloading && isActive) {
            val query = DownloadManager.Query().setFilterById(downloadId)
            val cursor = downloadManager.query(query)
            if (cursor != null && cursor.moveToFirst()) {
                val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                val status = if (statusIndex != -1) cursor.getInt(statusIndex) else -1

                val downloadedIndex = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                val totalIndex = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                val downloadedBytes = if (downloadedIndex != -1) cursor.getLong(downloadedIndex) else 0L
                val totalBytes = if (totalIndex != -1) cursor.getLong(totalIndex) else 0L

                val progress = if (totalBytes > 0) ((downloadedBytes * 100) / totalBytes).toInt() else 0

                when (status) {
                    DownloadManager.STATUS_RUNNING -> {
                        _updateState.value = UpdateState.Downloading(progress.coerceIn(0, 99))
                    }
                    DownloadManager.STATUS_SUCCESSFUL -> {
                        isDownloading = false
                        _updateState.value = UpdateState.ReadyToInstall(targetFile.absolutePath, version)
                    }
                    DownloadManager.STATUS_FAILED -> {
                        isDownloading = false
                        _updateState.value = UpdateState.Error("Pengunduhan berkas pembaruan gagal.")
                    }
                }
                cursor.close()
            }
            delay(1000)
        }
    }

    fun installApk(apkFilePath: String) {
        try {
            val apkFile = File(apkFilePath)
            if (!apkFile.exists()) {
                _updateState.value = UpdateState.Error("Berkas APK tidak ditemukan di penyimpanan.")
                return
            }

            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                apkFile
            )

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(contentUri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            context.startActivity(installIntent)
        } catch (e: Exception) {
            _updateState.value = UpdateState.Error("Gagal membuka penginstal APK: ${e.message}")
        }
    }

    fun resetState() {
        _updateState.value = UpdateState.Idle
    }

    private fun cleanChangelog(rawBody: String): String {
        if (rawBody.isBlank()) return "Pembaruan sistem, peningkatan performa, dan optimasi arsitektur."
        return rawBody
            .replace(Regex("### Download.*", RegexOption.DOT_MATCHES_ALL), "")
            .trim()
    }
}
