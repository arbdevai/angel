package com.arbdev.angel.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = HeavenlyBluePrimaryDark,
    onPrimary = HeavenlyBlueOnPrimaryDark,
    primaryContainer = HeavenlyBlueContainerDark,
    onPrimaryContainer = HeavenlyBlueOnContainerDark,
    secondary = AngelicGoldSecondaryDark,
    onSecondary = AngelicGoldOnSecondaryDark,
    secondaryContainer = AngelicGoldContainerDark,
    onSecondaryContainer = AngelicGoldOnContainerDark,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onBackground = DarkOnBackground,
    onSurface = DarkOnSurface,
    onSurfaceVariant = DarkOnSurfaceVariant
)

private val LightColorScheme = lightColorScheme(
    primary = HeavenlyBluePrimary,
    onPrimary = HeavenlyBlueOnPrimary,
    primaryContainer = HeavenlyBlueContainer,
    onPrimaryContainer = HeavenlyBlueOnContainer,
    secondary = AngelicGoldSecondary,
    onSecondary = AngelicGoldOnSecondary,
    secondaryContainer = AngelicGoldContainer,
    onSecondaryContainer = AngelicGoldOnContainer,
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = LightSurfaceVariant,
    onBackground = LightOnBackground,
    onSurface = LightOnSurface,
    onSurfaceVariant = LightOnSurfaceVariant
)

@Composable
fun AngelTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
