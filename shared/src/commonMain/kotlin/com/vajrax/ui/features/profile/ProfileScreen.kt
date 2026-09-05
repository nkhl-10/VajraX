package com.vajrax.ui.features.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vajrax.ui.theme.LocalThemeModeController
import com.vajrax.ui.theme.LuminaTheme
import com.vajrax.ui.theme.ThemeMode

/**
 * Standard Settings & Identity Screen for Lumina Life OS.
 * Designed around "How do I configure my settings and profile?"
 * Clean grouped lists, AI persona protocol selection, and standard preferences.
 */
@Composable
fun ProfileScreen() {
    val colors = LuminaTheme.colors
    val themeController = LocalThemeModeController.current

    var selectedPersona by remember { mutableStateOf("Stoic & Vedic") }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var hapticsEnabled by remember { mutableStateOf(true) }

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
        // 1. PAGE HEADER (Breathes directly on page)
        // ==========================================
        Text(
            text = "Settings",
            color = colors.onSurface,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.5).sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "Profile identity, coaching tone, and system preferences",
            color = colors.onSurfaceVariant,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        // ==========================================
        // 2. USER IDENTITY CARD
        // ==========================================
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(colors.surface)
                .border(1.dp, colors.outlineVariant, RoundedCornerShape(12.dp))
                .padding(18.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(colors.primaryContainer)
                            .border(1.dp, colors.primary.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "AV",
                            color = colors.primary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Alexander Vance",
                                color = colors.onSurface,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "Active System: High Performance",
                            color = colors.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = colors.outlineVariant.copy(alpha = 0.6f), thickness = 0.6.dp)
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "IDENTITY STATEMENT",
                    color = colors.primary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "“Clarity. Deliberate Action. Quiet Strength.”",
                    color = colors.onSurface,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Executing daily imperatives with single-pointed focus and zero friction.",
                    color = colors.onSurfaceVariant,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ==========================================
        // 3. AI SAGE COACHING PERSONA
        // ==========================================
        Text(
            text = "AI COACHING PERSONA",
            color = colors.onSurfaceVariant,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp
        )
        Spacer(modifier = Modifier.height(10.dp))

        val personas = listOf(
            "Stoic & Vedic" to "Combines philosophical clarity and Vedic presence for steady accountability.",
            "High Performance" to "Direct, metrics-driven execution focus with minimal commentary.",
            "Gentle Mentor" to "Compassionate, friction-reducing support designed for habit recovery."
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            personas.forEach { (name, desc) ->
                val isSelected = selectedPersona == name

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) colors.surfaceContainerLow else colors.surface)
                        .border(
                            width = 1.dp,
                            color = if (isSelected) colors.primary.copy(alpha = 0.6f) else colors.outlineVariant,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable { selectedPersona = name }
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = name,
                                color = if (isSelected) colors.primary else colors.onSurface,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
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
        // 4. PREFERENCE SETTINGS GROUP
        // ==========================================
        Text(
            text = "PREFERENCES",
            color = colors.onSurfaceVariant,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp
        )
        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(colors.surface)
                .border(1.dp, colors.outlineVariant, RoundedCornerShape(10.dp))
        ) {
            Column {
                // Notifications Row
                SettingToggleRow(
                    title = "Daily Practice Reminders",
                    subtitle = "Notify before scheduled commitment windows",
                    checked = notificationsEnabled,
                    onCheckedChange = { notificationsEnabled = it }
                )

                HorizontalDivider(color = colors.outlineVariant.copy(alpha = 0.5f), thickness = 0.5.dp)

                // Haptics Row
                SettingToggleRow(
                    title = "Tactile Haptics",
                    subtitle = "Haptic feedback on practice completion",
                    checked = hapticsEnabled,
                    onCheckedChange = { hapticsEnabled = it }
                )

                HorizontalDivider(color = colors.outlineVariant.copy(alpha = 0.5f), thickness = 0.5.dp)

                // Theme Mode Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            themeController.value = if (colors.isDark) ThemeMode.LIGHT else ThemeMode.DARK
                        }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Appearance Theme",
                            color = colors.onSurface,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = if (colors.isDark) "Dark Slate mode active" else "Light Editorial mode active",
                            color = colors.onSurfaceVariant,
                            fontSize = 11.5.sp
                        )
                    }

                    Text(
                        text = if (colors.isDark) "Dark 🌙" else "Light ☀️",
                        color = colors.primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ==========================================
        // 5. DATA & ARCHITECTURE
        // ==========================================
        Text(
            text = "SYSTEM & DATA",
            color = colors.onSurfaceVariant,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp
        )
        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(colors.surface)
                .border(1.dp, colors.outlineVariant, RoundedCornerShape(10.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                    Text("Supabase Realtime (Active)", color = colors.statusSuccess, fontSize = 12.5.sp, fontWeight = FontWeight.Medium)
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
                    Text("Version", color = colors.onSurfaceVariant, fontSize = 12.5.sp)
                    Text("2.4.0 Production", color = colors.onSurfaceVariant, fontSize = 12.5.sp)
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
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = colors.onSurface,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                color = colors.onSurfaceVariant,
                fontSize = 11.5.sp
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = colors.onPrimary,
                checkedTrackColor = colors.primary,
                uncheckedThumbColor = colors.onSurfaceVariant,
                uncheckedTrackColor = colors.surfaceContainerHigh
            )
        )
    }
}

