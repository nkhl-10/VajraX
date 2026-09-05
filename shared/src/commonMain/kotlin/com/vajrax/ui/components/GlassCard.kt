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
 * Reusable Glassmorphism / Surface Card
 * Dynamically supports Light and Dark modes.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val colors = LuminaTheme.colors

    Box(
        modifier = modifier
            .shadow(
                elevation = if (colors.isDark) 0.dp else 4.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = Color(0x0A0F172A),
                spotColor = Color(0x0A4338CA)
            )
            .clip(RoundedCornerShape(20.dp))
            .background(colors.surface)
            .border(
                width = 1.dp,
                color = colors.outlineVariant,
                shape = RoundedCornerShape(20.dp)
            ),
        content = content
    )
}
