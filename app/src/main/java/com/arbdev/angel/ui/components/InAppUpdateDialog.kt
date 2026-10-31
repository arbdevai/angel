package com.arbdev.angel.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.NewReleases
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.arbdev.angel.data.model.UpdateState

@Composable
fun InAppUpdateDialog(
    state: UpdateState,
    onDismiss: () -> Unit,
    onStartDownload: (downloadUrl: String, version: String) -> Unit,
    onInstallApk: (apkPath: String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(26.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (state) {
                            is UpdateState.Checking -> Icons.Rounded.SystemUpdate
                            is UpdateState.UpdateAvailable -> Icons.Rounded.NewReleases
                            is UpdateState.Downloading -> Icons.Rounded.Download
                            is UpdateState.ReadyToInstall -> Icons.Rounded.CheckCircle
                            is UpdateState.UpToDate -> Icons.Rounded.CheckCircle
                            is UpdateState.Error -> Icons.Rounded.ErrorOutline
                            else -> Icons.Rounded.SystemUpdate
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = when (state) {
                        is UpdateState.Checking -> "Memeriksa Pembaruan..."
                        is UpdateState.UpdateAvailable -> "Pembaruan Tersedia"
                        is UpdateState.Downloading -> "Mengunduh Pembaruan"
                        is UpdateState.ReadyToInstall -> "Siap Dipasang"
                        is UpdateState.UpToDate -> "Versi Terbaru"
                        is UpdateState.Error -> "Gagal Memperbarui"
                        else -> "Pembaruan Aplikasi"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                when (state) {
                    is UpdateState.Checking -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 12.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.5.dp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Menghubungkan ke repositori rilis resmi...",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    is UpdateState.UpToDate -> {
                        Text(
                            text = "Aplikasi Angel Anda sudah berada pada versi terbaru (v${state.currentVersion}). Tidak ada pembaruan yang diperlukan.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    is UpdateState.UpdateAvailable -> {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Angel v${state.latestVersion}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "Ukuran berkas: ${state.fileSizeFormatted}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primary
                                ) {
                                    Text(
                                        text = "RILIS BARU",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Catatan Rilis:",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = state.releaseNotes,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    is UpdateState.Downloading -> {
                        Column(modifier = Modifier.padding(vertical = 8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Mengunduh berkas APK...",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = "${state.progressPercent}%",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { state.progressPercent / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                            )
                        }
                    }
                    is UpdateState.ReadyToInstall -> {
                        Text(
                            text = "Berkas pembaruan Angel v${state.version} sudah selesai diunduh dan diverifikasi. Tekan 'Pasang Sekarang' untuk memperbarui aplikasi.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    is UpdateState.Error -> {
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    else -> {}
                }
            }
        },
        confirmButton = {
            when (state) {
                is UpdateState.UpdateAvailable -> {
                    Button(
                        onClick = {
                            if (state.isApkReadyLocally && state.localApkPath != null) {
                                onInstallApk(state.localApkPath)
                            } else {
                                onStartDownload(state.downloadUrl, state.latestVersion)
                            }
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (state.isApkReadyLocally) "Pasang Sekarang" else "Unduh Pembaruan",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                is UpdateState.ReadyToInstall -> {
                    Button(
                        onClick = { onInstallApk(state.localApkPath) },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Pasang Sekarang", fontWeight = FontWeight.Bold)
                    }
                }
                is UpdateState.UpToDate, is UpdateState.Error -> {
                    Button(onClick = onDismiss, shape = RoundedCornerShape(12.dp)) {
                        Text("Tutup")
                    }
                }
                else -> {}
            }
        },
        dismissButton = {
            if (state is UpdateState.UpdateAvailable || state is UpdateState.Downloading) {
                OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(12.dp)) {
                    Text("Nanti Saja")
                }
            }
        }
    )
}
