package com.arbdev.angel.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

enum class AngelTab(val title: String, val icon: ImageVector) {
    DASHBOARD("Beranda", Icons.Rounded.Home),
    GOALS("Celengan", Icons.Rounded.Savings),
    HISTORY("Riwayat", Icons.Rounded.History),
    SETTINGS("Pengaturan", Icons.Rounded.Settings)
}

@Composable
fun AngelBottomBar(
    currentTab: AngelTab,
    onTabSelected: (AngelTab) -> Unit
) {
    NavigationBar {
        AngelTab.entries.forEach { tab ->
            val isSelected = currentTab == tab
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(imageVector = tab.icon, contentDescription = tab.title)
                },
                label = {
                    Text(text = tab.title)
                }
            )
        }
    }
}
