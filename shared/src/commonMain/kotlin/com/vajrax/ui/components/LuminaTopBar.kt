package com.vajrax.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vajrax.ui.theme.LocalThemeModeController
import com.vajrax.ui.theme.LuminaTheme
import com.vajrax.ui.theme.ThemeMode

/**
 * Clean, solid executive app header for Lumina Life OS.
 * Designed with editorial restraint, clear page hierarchy, and quiet action controls.
 */
@Composable
fun LuminaTopBar(
    currentRoute: String,
    onProfileClick: () -> Unit = {},
    onAiSageClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = LuminaTheme.colors
    val themeController = LocalThemeModeController.current

    val (title, subtitle) = when (currentRoute) {
        "home" -> "Today" to "Saturday, September 5"
        "calendar" -> "Weekly Habits" to "Habit & Practice Matrix"
        "report" -> "Overview" to "Progress & Evidence"
        "discover" -> "Life Systems" to "Operating Frameworks"
        "profile" -> "Settings" to "Preferences & Identity"
        else -> "Today" to "Executive OS"
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.surface)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Clean Page Title & Context
            Column(
                modifier = Modifier.weight(1f)
            ) {
                AnimatedContent(
                    targetState = title to subtitle,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "topBarTitle"
                ) { (currTitle, currSub) ->
                    Column {
                        Text(
                            text = currTitle,
                            color = colors.onSurface,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.3).sp
                        )
                        Text(
                            text = currSub,
                            color = colors.onSurfaceVariant,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                            letterSpacing = 0.1.sp
                        )
                    }
                }
            }

            // Right Actions: AI Sage Companion Button, Theme Switcher & User Avatar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // AI Sage Life Companion Button (Quiet, elegant)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.primaryContainer)
                        .border(
                            width = 1.dp,
                            color = colors.primary.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onAiSageClick
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Text(text = "✨", fontSize = 12.sp)
                        Text(
                            text = "Sage",
                            color = colors.primary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Light / Dark Theme Mode Toggle
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.surfaceContainerLow)
                        .border(1.dp, colors.outlineVariant, RoundedCornerShape(8.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            themeController.value = if (colors.isDark) ThemeMode.LIGHT else ThemeMode.DARK
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (colors.isDark) "🌙" else "☀️",
                        fontSize = 13.sp
                    )
                }

                // Profile Avatar Initials
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(colors.surfaceContainerHigh)
                        .border(1.dp, colors.outlineVariant, CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onProfileClick
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "AV",
                        color = colors.onSurface,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Restrained Hairline bottom divider
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(0.6.dp)
                .background(colors.outlineVariant)
        )
    }
}

