package com.arbdev.angel.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.ui.graphics.vector.ImageVector

enum class AngelTab(val title: String, val icon: ImageVector) {
    DASHBOARD("Beranda", Icons.Rounded.Home),
    GOALS("Celengan", Icons.Rounded.Savings),
    SIMULATOR("Simulasi", Icons.Rounded.Calculate),
    HISTORY("Riwayat", Icons.Rounded.History),
    SETTINGS("Pengaturan", Icons.Rounded.Settings)
}
