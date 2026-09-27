package com.vajrax.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.vajrax.ui.theme.LuminaTheme

/**
 * Reusable Surface Card — Exact Figma match.
 * Light: white bg, #E5E7EB border, 20dp radius, subtle shadow
 * Dark: dark surface bg, subtle border
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: androidx.compose.ui.unit.Dp = 20.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val colors = LuminaTheme.colors

    Box(
        modifier = modifier
            .shadow(
                elevation = if (colors.isDark) 0.dp else 2.dp,
                shape = RoundedCornerShape(cornerRadius),
                ambientColor = Color(0x0A111827),
                spotColor = Color(0x0A4F46E5)
            )
            .clip(RoundedCornerShape(cornerRadius))
            .background(colors.surface)   // Figma: white #FFFFFF
            .border(
                width = 1.dp,
                color = colors.outlineVariant,  // Figma: #E5E7EB
                shape = RoundedCornerShape(cornerRadius)
            ),
        content = content
    )
}
