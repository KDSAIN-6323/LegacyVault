package com.legacyvault.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

private val DarkColorScheme = darkColorScheme(
    primary          = DarkAccent,
    onPrimary        = DarkOnAccent,
    primaryContainer = DarkSurfaceVariant,
    onPrimaryContainer = DarkOnSurface,
    secondary        = DarkAccentVariant,
    onSecondary      = DarkOnAccent,
    background       = DarkSurface,
    onBackground     = DarkOnSurface,
    surface          = DarkSurface,
    onSurface        = DarkOnSurface,
    surfaceVariant   = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceMuted,
    surfaceContainer = DarkCard,
    outline          = DarkBorder,
    error            = DarkError,
    onError          = DarkSurface,
)

private val LightColorScheme = lightColorScheme(
    primary          = LightAccent,
    onPrimary        = LightOnAccent,
    primaryContainer = LightSidebarVariant,
    onPrimaryContainer = LightOnAccent,
    secondary        = LightAccentVariant,
    onSecondary      = LightOnAccent,
    background       = LightSurface,
    onBackground     = LightOnSurface,
    surface          = LightSurface,
    onSurface        = LightOnSurface,
    surfaceVariant   = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceMuted,
    surfaceContainer = LightSurfaceVariant,
    outline          = LightSidebarVariant,
    error            = LightError,
    onError          = White,
)

@Composable
fun LegacyVaultTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic colour (Android 12+) — disabled by default to preserve brand colours
    dynamicColor: Boolean = false,
    fontScale: Float = 1f,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else      -> LightColorScheme
    }

    val density = LocalDensity.current
    CompositionLocalProvider(
        LocalDensity provides Density(
            density   = density.density,
            fontScale = fontScale
        )
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography  = LegacyVaultTypography,
            content     = content
        )
    }
}
