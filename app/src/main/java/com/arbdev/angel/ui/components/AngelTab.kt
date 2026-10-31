package com.arbdev.angel.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.SpaceDashboard
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.ui.graphics.vector.ImageVector

enum class AngelTab(val title: String, val icon: ImageVector) {
    DASHBOARD("Beranda", Icons.Rounded.SpaceDashboard),
    GOALS("Celengan", Icons.Rounded.AccountBalanceWallet),
    SIMULATOR("Simulasi", Icons.Rounded.TrendingUp),
    HISTORY("Riwayat", Icons.Rounded.ReceiptLong),
    SETTINGS("Pengaturan", Icons.Rounded.Tune)
}
