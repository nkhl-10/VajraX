package com.vajrax.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * Lumina Life OS Design System
 * Engineered for calm focus, optical clarity, restrained borders, and editorial typography.
 * Supports both Light and Dark themes.
 */

@Immutable
data class LuminaColors(
    val background: Color,
    val onBackground: Color,
    val surface: Color,
    val onSurface: Color,
    val surfaceDim: Color,
    val surfaceBright: Color,
    val surfaceContainerLowest: Color,
    val surfaceContainerLow: Color,
    val surfaceContainer: Color,
    val surfaceContainerHigh: Color,
    val surfaceContainerHighest: Color,
    val onSurfaceVariant: Color,
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val secondary: Color,
    val onSecondary: Color,
    val secondaryContainer: Color,
    val onSecondaryContainer: Color,
    val tertiary: Color,
    val onTertiary: Color,
    val outline: Color,
    val outlineVariant: Color,
    val glassSurface: Color,
    val glassBorder: Color,
    val floatingDock: Color,
    val floatingDockBorder: Color,
    val statusSuccess: Color,
    val statusWarning: Color,
    val statusError: Color,
    val isDark: Boolean,
    /** Non-essential hints only (captions next to stronger text); never for body copy. */
    val textTertiary: Color = Color(0xFF9CA3AF),
    val successContainer: Color = Color(0xFFDCFCE7),
    val onSuccessContainer: Color = Color(0xFF166534),
    val warningContainer: Color = Color(0xFFFEF3C7),
    val onWarningContainer: Color = Color(0xFF92400E)
)

// ==========================================
// 1. LIGHT PALETTE — Exact Figma Tokens
// ==========================================
val LuminaLightColors = LuminaColors(
    background = Color(0xFFF9FAFB),         // Figma bg: r=0.976, g=0.980, b=0.984
    onBackground = Color(0xFF111827),        // Figma text primary: r=0.067, g=0.094, b=0.153
    surface = Color(0xFFFFFFFF),             // Figma card surface: white
    onSurface = Color(0xFF111827),
    surfaceDim = Color(0xFFF3F4F6),         // Figma pill/toggle bg: r=0.953, g=0.957, b=0.965
    surfaceBright = Color(0xFFFFFFFF),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF3F4F6), // Figma section bg
    surfaceContainer = Color(0xFFEEF2FF),    // Figma primary container / tint
    surfaceContainerHigh = Color(0xFFE5E7EB),
    surfaceContainerHighest = Color(0xFFD1D5DB),
    onSurfaceVariant = Color(0xFF6B7280),    // Secondary text, 4.8:1 on white (Figma #9CA3AF is 2.5:1)
    primary = Color(0xFF4F46E5),             // Figma indigo: r=0.310, g=0.275, b=0.898
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFEEF2FF),    // Soft indigo tint
    onPrimaryContainer = Color(0xFF4338CA),
    secondary = Color(0xFF6366F1),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE0E7FF),
    onSecondaryContainer = Color(0xFF3730A3),
    tertiary = Color(0xFF374151),
    onTertiary = Color(0xFFFFFFFF),
    outline = Color(0xFF9CA3AF),
    outlineVariant = Color(0xFFE5E7EB),
    glassSurface = Color(0xFFFFFFFF),
    glassBorder = Color(0xFFE5E7EB),
    floatingDock = Color(0x33BEBEBE),        // Figma nav: rgba(0.749,0.749,0.749,0.20)
    floatingDockBorder = Color(0x1ABEBEBE),
    statusSuccess = Color(0xFF16A34A),
    statusWarning = Color(0xFFD97706),
    statusError = Color(0xFFDC2626),
    // 4.7:1 on white (the old #9CA3AF was 2.5:1): placeholders and future days stay readable.
    textTertiary = Color(0xFF6F7480),
    isDark = false
)

// ==========================================
// 2. DARK PALETTE (Deep Slate Obsidian)
// ==========================================
val LuminaDarkColors = LuminaColors(
    background = Color(0xFF0F1117),
    onBackground = Color(0xFFF9FAFB),
    surface = Color(0xFF181B22),
    onSurface = Color(0xFFF9FAFB),
    surfaceDim = Color(0xFF12141A),
    surfaceBright = Color(0xFF222632),
    surfaceContainerLowest = Color(0xFF12141A),
    surfaceContainerLow = Color(0xFF1A1D26),
    surfaceContainer = Color(0xFF222632),
    surfaceContainerHigh = Color(0xFF2C3140),
    surfaceContainerHighest = Color(0xFF373D4E),
    onSurfaceVariant = Color(0xFF94A3B8),
    primary = Color(0xFF6366F1),            // Accessible Bright Indigo
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF312E81),
    onPrimaryContainer = Color(0xFFE0E7FF),
    secondary = Color(0xFF818CF8),
    onSecondary = Color(0xFF0F172A),
    secondaryContainer = Color(0xFF4338CA),
    onSecondaryContainer = Color(0xFFEEF2FF),
    tertiary = Color(0xFFCBD5E1),
    onTertiary = Color(0xFF0F172A),
    outline = Color(0xFF64748B),
    outlineVariant = Color(0xFF262B38),     // Subtle hairline dark border
    glassSurface = Color(0xFF181B22),
    glassBorder = Color(0xFF262B38),
    floatingDock = Color(0xFF1E232E),
    floatingDockBorder = Color(0xFF2B3242),
    statusSuccess = Color(0xFF22C55E),
    statusWarning = Color(0xFFF59E0B),
    statusError = Color(0xFFEF4444),
    isDark = true,
    textTertiary = Color(0xFF7C8799),
    successContainer = Color(0xFF14532D),
    onSuccessContainer = Color(0xFFBBF7D0),
    warningContainer = Color(0xFF451A03),
    onWarningContainer = Color(0xFFFDE68A)
)

// Backward Compatibility Aliases for Vajra Tokens
val BackgroundObsidian = LuminaDarkColors.background
val SurfaceCharcoal = LuminaDarkColors.surface
val SurfaceElevated = LuminaDarkColors.surfaceContainer
val VajraGold = Color(0xFFF59E0B)
val VajraGoldContainer = Color(0xFFD97706)
val OnVajraGold = Color(0xFFFFFFFF)
val TextPrimary = LuminaDarkColors.onSurface
val TextSecondary = LuminaDarkColors.onSurfaceVariant
val TextMuted = Color(0xFF64748B)
val GlassSurface = LuminaDarkColors.glassSurface
val GlassBorder = LuminaDarkColors.glassBorder
val CalmGreen = LuminaDarkColors.statusSuccess
val StatusSuccess = LuminaDarkColors.statusSuccess
val StatusWarning = LuminaDarkColors.statusWarning
val StatusError = LuminaDarkColors.statusError
val OutlineVariant = LuminaDarkColors.outlineVariant
val SurfaceContainerLowest = LuminaDarkColors.surfaceContainerLowest
val SurfaceContainerLow = LuminaDarkColors.surfaceContainerLow
val SurfaceContainerHigh = LuminaDarkColors.surfaceContainerHigh
val SurfaceContainerHighest = LuminaDarkColors.surfaceContainerHighest

/** Brand gradient: the logo, the Profile header and the "violet" habit colour share it. */
val BrandIndigo = Color(0xFF4F46E5)
val BrandViolet = Color(0xFF7C3AED)
