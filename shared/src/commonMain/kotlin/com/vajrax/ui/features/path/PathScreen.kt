package com.vajrax.ui.features.path

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import com.vajrax.ui.theme.LuminaTheme

/**
 * Figma-Faithful Discover Screen for Lumina Life OS.
 * Features clean framework cards, category pills, active system highlights, and practice switches.
 */
@Composable
fun PathScreen(
    state: PathUiState,
    onIntent: (PathIntent) -> Unit
) {
    val colors = LuminaTheme.colors
    var selectedCategory by remember { mutableStateOf("All") }

    val categories = listOf("All", "Performance", "Philosophy", "Health", "Mindfulness")

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
            text = "Discover",
            color = colors.onSurface,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.5).sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "Operating frameworks & daily life protocols",
            color = colors.onSurfaceVariant,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Filter Categories Row (Horizontally Scrollable)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { cat ->
                val isSelected = cat == selectedCategory
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) colors.primary else colors.surface)
                        .border(
                            width = 1.dp,
                            color = if (isSelected) colors.primary else colors.outlineVariant,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { selectedCategory = cat }
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = cat,
                        color = if (isSelected) Color.White else colors.onSurfaceVariant,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ==========================================
        // 2. ACTIVE SYSTEM ELEVATED CARD
        // ==========================================
        val activePath = state.activePath ?: state.availablePaths.firstOrNull()

        if (activePath != null) {
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
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(colors.primaryContainer)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "ACTIVE FRAMEWORK",
                                color = colors.primary,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.6.sp
                            )
                        }

                        Text(
                            text = "${state.practices.size} practices active",
                            color = colors.onSurfaceVariant,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = activePath.name,
                        color = colors.onSurface,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.3).sp
                    )

                    if (!activePath.description.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = activePath.description,
                            color = colors.onSurfaceVariant,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 3 Mini Stat Highlights
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.surfaceContainerLow)
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("4.5h", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = colors.onSurface)
                                Text("Deep Work", fontSize = 10.5.sp, color = colors.onSurfaceVariant)
                            }
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.surfaceContainerLow)
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("100%", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = colors.onSurface)
                                Text("Adherence", fontSize = 10.5.sp, color = colors.onSurfaceVariant)
                            }
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.surfaceContainerLow)
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("14 Days", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = colors.onSurface)
                                Text("Streak", fontSize = 10.5.sp, color = colors.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ==========================================
        // 3. AVAILABLE SYSTEMS DIRECTORY
        // ==========================================
        Text(
            text = "AVAILABLE FRAMEWORKS",
            color = colors.onSurfaceVariant,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp
        )
        Spacer(modifier = Modifier.height(12.dp))

        val filteredPaths = remember(state.availablePaths, selectedCategory) {
            if (selectedCategory == "All") {
                state.availablePaths
            } else {
                state.availablePaths.filter { path ->
                    when (selectedCategory) {
                        "Performance" -> path.id in listOf("high_performance", "wealth_builder", "creator")
                        "Philosophy" -> path.id in listOf("self_mastery", "scholar", "purpose_driven")
                        "Health" -> path.id in listOf("high_performance", "balanced_life")
                        "Mindfulness" -> path.id in listOf("mindful_life", "balanced_life", "self_mastery")
                        else -> true
                    }
                }
            }
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            filteredPaths.forEach { path ->
                val isActive = path.id == activePath?.id

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(colors.surface)
                        .border(
                            width = 1.dp,
                            color = if (isActive) colors.primary.copy(alpha = 0.5f) else colors.outlineVariant.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onIntent(PathIntent.SelectPath(path.id)) }
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = path.name,
                                color = if (isActive) colors.primary else colors.onSurface,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (!path.description.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = path.description,
                                    color = colors.onSurfaceVariant,
                                    fontSize = 12.5.sp,
                                    lineHeight = 16.sp,
                                    maxLines = 2
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        if (isActive) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(colors.primary)
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "Active",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(colors.primaryContainer)
                                    .clickable { onIntent(PathIntent.SelectPath(path.id)) }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "Adopt",
                                    color = colors.primary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // ==========================================
        // 4. SYSTEM PRACTICES & TOGGLES
        // ==========================================
        if (state.practices.isNotEmpty()) {
            Text(
                text = "SYSTEM PRACTICES",
                color = colors.onSurfaceVariant,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                state.practices.forEach { practice ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(colors.surface)
                            .border(1.dp, colors.outlineVariant.copy(alpha = 0.7f), RoundedCornerShape(18.dp))
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = practice.title,
                                    color = colors.onSurface,
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${practice.targetDurationMinutes}m target · ${practice.minimumDurationMinutes}m min",
                                    color = colors.onSurfaceVariant,
                                    fontSize = 12.sp
                                )
                            }
                            Switch(
                                checked = practice.isActive,
                                onCheckedChange = { onIntent(PathIntent.TogglePractice(practice.id, it)) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = colors.primary,
                                    uncheckedThumbColor = colors.outline,
                                    uncheckedTrackColor = colors.surfaceContainerHigh
                                )
                            )
                        }
                    }
                }
            }
        }

        // Safe clearance for bottom navigation bar
        Spacer(modifier = Modifier.navigationBarsPadding().height(96.dp))
    }
}


