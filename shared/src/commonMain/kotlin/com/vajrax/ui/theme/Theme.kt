package com.vajrax.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*

enum class ThemeMode {
    AUTO,
    LIGHT,
    DARK
}

val LocalLuminaColors = staticCompositionLocalOf { LuminaDarkColors }
val LocalThemeModeController = compositionLocalOf { mutableStateOf(ThemeMode.AUTO) }

object LuminaTheme {
    val colors: LuminaColors
        @Composable
        get() = LocalLuminaColors.current

    val currentMode: ThemeMode
        @Composable
        get() = LocalThemeModeController.current.value

    @Composable
    fun toggleTheme() {
        val controller = LocalThemeModeController.current
        controller.value = when (controller.value) {
            ThemeMode.AUTO -> if (isSystemInDarkTheme()) ThemeMode.LIGHT else ThemeMode.DARK
            ThemeMode.LIGHT -> ThemeMode.DARK
            ThemeMode.DARK -> ThemeMode.LIGHT
        }
    }
}

private fun LuminaColors.toMaterialDarkColorScheme() = darkColorScheme(
    background = background,
    onBackground = onBackground,
    surface = surface,
    onSurface = onSurface,
    surfaceVariant = surfaceContainer,
    onSurfaceVariant = onSurfaceVariant,
    primary = primary,
    onPrimary = onPrimary,
    primaryContainer = primaryContainer,
    onPrimaryContainer = onPrimaryContainer,
    secondary = secondary,
    onSecondary = onSecondary,
    secondaryContainer = secondaryContainer,
    onSecondaryContainer = onSecondaryContainer,
    error = statusError,
    onError = onPrimary,
    outline = outline,
    outlineVariant = outlineVariant
)

private fun LuminaColors.toMaterialLightColorScheme() = lightColorScheme(
    background = background,
    onBackground = onBackground,
    surface = surface,
    onSurface = onSurface,
    surfaceVariant = surfaceContainer,
    onSurfaceVariant = onSurfaceVariant,
    primary = primary,
    onPrimary = onPrimary,
    primaryContainer = primaryContainer,
    onPrimaryContainer = onPrimaryContainer,
    secondary = secondary,
    onSecondary = onSecondary,
    secondaryContainer = secondaryContainer,
    onSecondaryContainer = onSecondaryContainer,
    error = statusError,
    onError = onPrimary,
    outline = outline,
    outlineVariant = outlineVariant
)

@Composable
fun VajraTheme(
    themeMode: ThemeMode = ThemeMode.LIGHT,
    content: @Composable () -> Unit
) {
    val systemInDark = isSystemInDarkTheme()
    val themeModeState = remember { mutableStateOf(themeMode) }

    val isDark = when (themeModeState.value) {
        ThemeMode.AUTO -> systemInDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val luminaColors = if (isDark) LuminaDarkColors else LuminaLightColors
    val materialColorScheme = if (isDark) {
        luminaColors.toMaterialDarkColorScheme()
    } else {
        luminaColors.toMaterialLightColorScheme()
    }

    CompositionLocalProvider(
        LocalLuminaColors provides luminaColors,
        LocalThemeModeController provides themeModeState
    ) {
        MaterialTheme(
            colorScheme = materialColorScheme,
            content = content
        )
    }
}
