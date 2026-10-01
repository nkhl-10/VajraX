package com.vajrax.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class ThemeMode {
    AUTO,
    LIGHT,
    DARK;

    companion object {
        fun of(raw: String?): ThemeMode = when (raw) {
            "LIGHT" -> LIGHT
            "DARK" -> DARK
            else -> AUTO
        }
    }
}

val LocalLuminaColors = staticCompositionLocalOf { LuminaLightColors }
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

/** Spacing scale (spec 05): 4, 8, 12, 16, 20, 24, 32. */
object VxSpace {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 20.dp
    val xxl = 24.dp
    val xxxl = 32.dp
    /** Horizontal screen gutter. */
    val gutter = 20.dp
    /** Bottom clearance so lists never hide behind the floating nav bar. */
    val navClearance = 112.dp
}

/** Semantic corner radii: small controls, medium cards, large sheets. */
object VxShape {
    val small = RoundedCornerShape(10.dp)
    /** Calendar cells, small tiles. */
    val tile = RoundedCornerShape(12.dp)
    /** Buttons, fields, list rows, snackbars. */
    val control = RoundedCornerShape(14.dp)
    val medium = RoundedCornerShape(16.dp)
    /** Cards (VxCard) and the pinned Home dashboard. */
    val card = RoundedCornerShape(20.dp)
    val large = RoundedCornerShape(24.dp)
    val pill = RoundedCornerShape(50)
}

/** Type roles (spec 05: no ad-hoc font sizes in screens). */
val VxTypography = Typography(
    displaySmall = TextStyle(fontSize = 30.sp, lineHeight = 36.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.6).sp),
    headlineMedium = TextStyle(fontSize = 26.sp, lineHeight = 32.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.5).sp),
    headlineSmall = TextStyle(fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.3).sp),
    titleLarge = TextStyle(fontSize = 18.sp, lineHeight = 24.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.2).sp),
    titleMedium = TextStyle(fontSize = 16.sp, lineHeight = 22.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.1).sp),
    titleSmall = TextStyle(fontSize = 15.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.Normal),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Normal),
    bodySmall = TextStyle(fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.Normal),
    labelLarge = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Bold),
    labelMedium = TextStyle(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.SemiBold),
    labelSmall = TextStyle(fontSize = 11.sp, lineHeight = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
)

/** Accent text/icon color to use on top of primaryContainer (keeps ≥4.5:1 in dark mode). */
val LuminaColors.accentOnContainer: Color get() = if (isDark) onPrimaryContainer else primary

/** Habit accent colors keyed by the stored color name. */
fun habitAccent(key: String?, dark: Boolean): Color {
    val base = when (key) {
        "violet" -> BrandViolet
        "sky" -> Color(0xFF0284C7)
        "teal" -> Color(0xFF0D9488)
        "emerald" -> Color(0xFF059669)
        "amber" -> Color(0xFFD97706)
        "orange" -> Color(0xFFEA580C)
        "rose" -> Color(0xFFE11D48)
        "slate" -> Color(0xFF475569)
        else -> Color(0xFF4F46E5)
    }
    return if (dark) lerpToWhite(base, 0.25f) else base
}

private fun lerpToWhite(c: Color, t: Float) = Color(
    red = c.red + (1f - c.red) * t,
    green = c.green + (1f - c.green) * t,
    blue = c.blue + (1f - c.blue) * t,
    alpha = c.alpha
)

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
    outlineVariant = outlineVariant,
    surfaceContainer = surfaceContainerLow,
    surfaceContainerHigh = surfaceContainer,
    surfaceContainerLow = surfaceDim
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
    outlineVariant = outlineVariant,
    surfaceContainer = Color(0xFFFFFFFF),
    surfaceContainerHigh = Color(0xFFFFFFFF),
    surfaceContainerLow = surfaceDim
)

@Composable
fun VajraTheme(
    themeMode: ThemeMode = ThemeMode.AUTO,
    content: @Composable () -> Unit
) {
    val systemInDark = isSystemInDarkTheme()
    val themeModeState = remember { mutableStateOf(themeMode) }
    LaunchedEffect(themeMode) { themeModeState.value = themeMode }

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
            typography = VxTypography,
            shapes = Shapes(
                extraSmall = RoundedCornerShape(8.dp),
                small = RoundedCornerShape(10.dp),
                medium = RoundedCornerShape(16.dp),
                large = RoundedCornerShape(24.dp),
                extraLarge = RoundedCornerShape(28.dp)
            ),
            content = content
        )
    }
}
