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
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
 * Figma-Faithful Profile & Settings Screen for Lumina Life OS.
 * Features user identity hero card, coaching persona cards, grouped preference toggles,
 * and system sync & database status.
 */
@Composable
fun ProfileScreen() {
    val colors = LuminaTheme.colors
    val themeController = LocalThemeModeController.current

    var selectedPersona by remember { mutableStateOf("High Performance") }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var hapticsEnabled by remember { mutableStateOf(true) }
    var eveningReviewEnabled by remember { mutableStateOf(true) }

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
        // 1. PAGE HEADER
        // ==========================================
        Text(
            text = "Profile",
            color = colors.onSurface,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.5).sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "Identity, coaching tone, and system preferences",
            color = colors.onSurfaceVariant,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        // ==========================================
        // 2. USER IDENTITY HERO CARD
        // ==========================================
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(26.dp))
                .background(colors.surface)
                .border(1.dp, colors.outlineVariant.copy(alpha = 0.7f), RoundedCornerShape(26.dp))
                .padding(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(colors.primaryContainer)
                            .border(1.5.dp, colors.primary.copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "AV",
                            color = colors.primary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Alexander Vance",
                                color = colors.onSurface,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.2).sp
                            )

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(colors.primaryContainer)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "PRO",
                                    color = colors.primary,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = "alex.vance@lumina.io",
                            color = colors.onSurfaceVariant,
                            fontSize = 12.5.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Active: High Performance Architecture",
                            color = colors.primary,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = colors.outlineVariant.copy(alpha = 0.5f), thickness = 0.6.dp)
                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "OPERATING PRINCIPLE",
                    color = colors.primary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "“Clarity. Deliberate Action. Quiet Strength.”",
                    color = colors.onSurface,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Executing daily imperatives with single-pointed focus, intentional recovery, and zero friction.",
                    color = colors.onSurfaceVariant,
                    fontSize = 12.5.sp,
                    lineHeight = 17.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ==========================================
        // 3. AI COACHING PERSONA
        // ==========================================
        Text(
            text = "COACHING PERSONA",
            color = colors.onSurfaceVariant,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp
        )
        Spacer(modifier = Modifier.height(12.dp))

        val personas = listOf(
            "High Performance" to "Direct, metrics-driven execution focus with minimal commentary.",
            "Stoic & Vedic" to "Combines philosophical clarity and Vedic presence for steady accountability.",
            "Gentle Mentor" to "Compassionate, friction-reducing support designed for habit recovery."
        )

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            personas.forEach { (name, desc) ->
                val isSelected = selectedPersona == name

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(colors.surface)
                        .border(
                            width = 1.dp,
                            color = if (isSelected) colors.primary.copy(alpha = 0.6f) else colors.outlineVariant.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(18.dp)
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { selectedPersona = name }
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = name,
                                    color = if (isSelected) colors.primary else colors.onSurface,
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(colors.primaryContainer)
                                            .padding(horizontal = 6.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = "ACTIVE",
                                            color = colors.primary,
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = desc,
                                color = colors.onSurfaceVariant,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }

                        RadioButton(
                            selected = isSelected,
                            onClick = { selectedPersona = name },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = colors.primary,
                                unselectedColor = colors.outlineVariant
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ==========================================
        // 4. PREFERENCES GROUP
        // ==========================================
        Text(
            text = "PREFERENCES",
            color = colors.onSurfaceVariant,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp
        )
        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(colors.surface)
                .border(1.dp, colors.outlineVariant.copy(alpha = 0.7f), RoundedCornerShape(20.dp))
        ) {
            Column {
                SettingToggleRow(
                    title = "Daily Practice Reminders",
                    subtitle = "Notify before scheduled commitment windows",
                    checked = notificationsEnabled,
                    onCheckedChange = { notificationsEnabled = it }
                )

                HorizontalDivider(color = colors.outlineVariant.copy(alpha = 0.5f), thickness = 0.5.dp)

                SettingToggleRow(
                    title = "Tactile Haptics",
                    subtitle = "Haptic feedback on practice completion",
                    checked = hapticsEnabled,
                    onCheckedChange = { hapticsEnabled = it }
                )

                HorizontalDivider(color = colors.outlineVariant.copy(alpha = 0.5f), thickness = 0.5.dp)

                SettingToggleRow(
                    title = "Evening Reflection Prompts",
                    subtitle = "Summary prompt at 09:00 PM",
                    checked = eveningReviewEnabled,
                    onCheckedChange = { eveningReviewEnabled = it }
                )

                HorizontalDivider(color = colors.outlineVariant.copy(alpha = 0.5f), thickness = 0.5.dp)

                // Theme Mode Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            themeController.value = if (colors.isDark) ThemeMode.LIGHT else ThemeMode.DARK
                        }
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Appearance Theme",
                            color = colors.onSurface,
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (colors.isDark) "Dark Slate active" else "Light Editorial active",
                            color = colors.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.surfaceContainerLow)
                            .border(1.dp, colors.outlineVariant.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = if (colors.isDark) "Dark Slate" else "Light Editorial",
                            color = colors.primary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ==========================================
        // 5. SYSTEM & DATA
        // ==========================================
        Text(
            text = "SYSTEM & ARCHITECTURE",
            color = colors.onSurfaceVariant,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp
        )
        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(colors.surface)
                .border(1.dp, colors.outlineVariant.copy(alpha = 0.7f), RoundedCornerShape(20.dp))
                .padding(18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Local Database", color = colors.onSurfaceVariant, fontSize = 12.5.sp)
                    Text("SQLite (Room / SQLDelight)", color = colors.onSurface, fontSize = 12.5.sp, fontWeight = FontWeight.Medium)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Cloud Sync Engine", color = colors.onSurfaceVariant, fontSize = 12.5.sp)
                    Text("Supabase Realtime (Connected)", color = Color(0xFF16A34A), fontSize = 12.5.sp, fontWeight = FontWeight.Medium)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Architecture", color = colors.onSurfaceVariant, fontSize = 12.5.sp)
                    Text("Kotlin Multiplatform MVI", color = colors.onSurface, fontSize = 12.5.sp, fontWeight = FontWeight.Medium)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Build Version", color = colors.onSurfaceVariant, fontSize = 12.5.sp)
                    Text("2.4.0 Production (Build 48)", color = colors.onSurfaceVariant, fontSize = 12.5.sp)
                }
            }
        }

        // Safe clearance for bottom navigation bar
        Spacer(modifier = Modifier.navigationBarsPadding().height(96.dp))
    }
}

@Composable
private fun SettingToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val colors = LuminaTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = colors.onSurface,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = colors.onSurfaceVariant,
                fontSize = 12.sp
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = colors.primary,
                uncheckedThumbColor = colors.outline,
                uncheckedTrackColor = colors.surfaceContainerHigh
            )
        )
    }
}


