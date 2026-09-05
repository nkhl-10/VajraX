package com.vajrax.ui.features.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vajrax.ui.theme.LocalThemeModeController
import com.vajrax.ui.theme.LuminaTheme
import com.vajrax.ui.theme.ThemeMode

/**
 * 1:1 Figma-Faithful Profile Screen for Lumina Life OS.
 * Features centered user identity (John Doe, JD avatar, Edit Profile),
 * Current Template progress card (Morning Discipline, Day 12 of 30, 40%),
 * My Templates list, and Preferences.
 */
@Composable
fun ProfileScreen() {
    val colors = LuminaTheme.colors
    val themeController = LocalThemeModeController.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // ==========================================
        // 1. HEADER
        // ==========================================
        Text(
            text = "Profile",
            color = colors.onSurface,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.6).sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // ==========================================
        // 2. USER PROFILE HERO (Centered)
        // ==========================================
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // JD Avatar Circle
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(colors.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "JD",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "John Doe",
                color = colors.onSurface,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.3).sp
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = "john@example.com",
                color = Color(0xFF64748B),
                fontSize = 13.5.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Edit Profile",
                color = colors.primary,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.SemiBold,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { /* Edit profile */ }
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // ==========================================
        // 3. CURRENT TEMPLATE CARD
        // ==========================================
        Text(
            text = "CURRENT TEMPLATE",
            color = Color(0xFF8B95A5),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp
        )
        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(26.dp))
                .background(colors.surface)
                .border(1.dp, colors.outlineVariant.copy(alpha = 0.7f), RoundedCornerShape(26.dp))
                .padding(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Morning Discipline",
                    color = colors.onSurface,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.3).sp
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "Build a structured morning routine",
                    color = colors.onSurfaceVariant,
                    fontSize = 13.5.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Progress Info Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Day 12 of 30",
                        color = colors.primary,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "40% Complete",
                        color = Color(0xFF64748B),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Progress Bar (40%)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFFEEF2FF))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.40f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(3.dp))
                            .background(colors.primary)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons Row: [View Template] [Change Template]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Outlined "View Template" Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(colors.surface)
                            .border(1.5.dp, colors.primary, RoundedCornerShape(14.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { /* View Template action */ },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "View Template",
                            color = colors.primary,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Solid "Change Template" Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(colors.primary)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { /* Change Template action */ },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Change Template",
                            color = Color.White,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // ==========================================
        // 4. MY TEMPLATES SECTION
        // ==========================================
        Text(
            text = "MY TEMPLATES",
            color = Color(0xFF8B95A5),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp
        )
        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(colors.surface)
                .border(1.dp, colors.outlineVariant.copy(alpha = 0.7f), RoundedCornerShape(22.dp))
        ) {
            Column {
                ProfileChevronRow(
                    title = "Custom Evening Routine",
                    onClick = {}
                )
                HorizontalDivider(color = colors.outlineVariant.copy(alpha = 0.5f), thickness = 0.5.dp)

                ProfileChevronRow(
                    title = "Weekend Reset",
                    onClick = {}
                )
                HorizontalDivider(color = colors.outlineVariant.copy(alpha = 0.5f), thickness = 0.5.dp)

                // "+ Create New Template" Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { /* Create new template */ }
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "+",
                        color = colors.primary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Create New Template",
                        color = colors.primary,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // ==========================================
        // 5. PREFERENCES SECTION
        // ==========================================
        Text(
            text = "PREFERENCES",
            color = Color(0xFF8B95A5),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp
        )
        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(colors.surface)
                .border(1.dp, colors.outlineVariant.copy(alpha = 0.7f), RoundedCornerShape(22.dp))
        ) {
            Column {
                ProfileChevronRow(
                    title = "Notifications",
                    onClick = {}
                )
                HorizontalDivider(color = colors.outlineVariant.copy(alpha = 0.5f), thickness = 0.5.dp)

                // Theme Mode Switch Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            themeController.value = if (colors.isDark) ThemeMode.LIGHT else ThemeMode.DARK
                        }
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Appearance",
                        color = colors.onSurface,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFEEF2FF))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (colors.isDark) "Dark" else "Light",
                            color = colors.primary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                HorizontalDivider(color = colors.outlineVariant.copy(alpha = 0.5f), thickness = 0.5.dp)

                ProfileChevronRow(
                    title = "Account Settings",
                    onClick = {}
                )
            }
        }

        // Safe clearance for bottom navigation dock
        Spacer(modifier = Modifier.navigationBarsPadding().height(96.dp))
    }
}

@Composable
private fun ProfileChevronRow(
    title: String,
    onClick: () -> Unit
) {
    val colors = LuminaTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 18.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = colors.onSurface,
            fontSize = 14.5.sp,
            fontWeight = FontWeight.Medium
        )

        // Chevron Right Icon
        androidx.compose.foundation.Canvas(modifier = Modifier.size(14.dp)) {
            val stroke = 1.6.dp.toPx()
            drawLine(
                color = Color(0xFF94A3B8),
                start = androidx.compose.ui.geometry.Offset(size.width * 0.35f, size.height * 0.15f),
                end = androidx.compose.ui.geometry.Offset(size.width * 0.75f, size.height * 0.5f),
                strokeWidth = stroke,
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
            drawLine(
                color = Color(0xFF94A3B8),
                start = androidx.compose.ui.geometry.Offset(size.width * 0.75f, size.height * 0.5f),
                end = androidx.compose.ui.geometry.Offset(size.width * 0.35f, size.height * 0.85f),
                strokeWidth = stroke,
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        }
    }
}



