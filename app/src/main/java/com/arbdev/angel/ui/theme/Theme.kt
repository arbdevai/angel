package com.arbdev.angel.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val CyberFintechColorScheme = darkColorScheme(
    primary = CyberLime,
    onPrimary = OnCyberLime,
    primaryContainer = DarkElevated,
    onPrimaryContainer = CyberLime,
    secondary = CyberLime,
    onSecondary = OnCyberLime,
    secondaryContainer = DarkElevated,
    onSecondaryContainer = TextWhite,
    tertiary = StatusInfo,
    onTertiary = DarkCanvas,
    background = DarkCanvas,
    onBackground = TextWhite,
    surface = DarkSurface,
    onSurface = TextWhite,
    surfaceVariant = DarkElevated,
    onSurfaceVariant = TextMuted,
    outline = DarkBorder,
    outlineVariant = DarkBorderSubtle,
    error = StatusError,
    onError = TextWhite
)

@Composable
fun AngelTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // Selalu gunakan skema Cyber-Fintech Stealth Dark agar konsisten dan tidak ditimpa wallpaper dinamis
    MaterialTheme(
        colorScheme = CyberFintechColorScheme,
        typography = Typography,
        content = content
    )
}
