package com.vajrax.ui.features.grow

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vajrax.domain.model.ReflectionRating
import com.vajrax.ui.theme.LuminaTheme

/**
 * Figma-Faithful Report Screen for Lumina Life OS.
 * Features weekly adherence bar graph, stat pods, behavioral patterns, and activity logs.
 */
@Composable
fun GrowScreen(
    state: GrowUiState,
    onIntent: (GrowIntent) -> Unit
) {
    val colors = LuminaTheme.colors
    var selectedPeriod by remember { mutableStateOf("Weekly") }

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
            text = "Report",
            color = colors.onSurface,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.5).sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "Weekly analytics & habit consistency",
            color = colors.onSurfaceVariant,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Segmented Period Selector
        val periodOptions = listOf("Weekly", "Monthly", "All-Time")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            periodOptions.forEach { period ->
                val isSelected = period == selectedPeriod
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
                        ) { selectedPeriod = period }
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = period,
                        color = if (isSelected) Color.White else colors.onSurfaceVariant,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ==========================================
        // 2. OVERVIEW ANALYTICS CARD WITH WEEKLY BAR CHART
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
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Consistency Overview",
                            color = colors.onSurface,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.3).sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "24 of 28 sessions fulfilled",
                            color = colors.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                    }

                    // 85% Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(colors.primaryContainer)
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("✓", color = colors.primary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("85%", color = colors.primary, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                // Weekly 7-Day Bar Chart
                val dailyData = listOf(
                    Triple("Mon", 0.75f, false),
                    Triple("Tue", 0.85f, false),
                    Triple("Wed", 1.00f, true),   // Active / Today
                    Triple("Thu", 0.60f, false),
                    Triple("Fri", 0.80f, false),
                    Triple("Sat", 0.50f, false),
                    Triple("Sun", 0.70f, false)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    dailyData.forEach { (dayName, fraction, isToday) ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(22.dp)
                                    .height((80 * fraction).dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        when {
                                            isToday -> colors.primary
                                            fraction >= 0.75f -> Color(0xFF818CF8)
                                            else -> colors.surfaceContainerHigh
                                        }
                                    )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = dayName,
                                fontSize = 11.sp,
                                color = if (isToday) colors.primary else colors.onSurfaceVariant,
                                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                // 3 Stat Pods
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ReportStatPod(
                        label = "Streak",
                        value = "14 Days",
                        modifier = Modifier.weight(1f)
                    )
                    ReportStatPod(
                        label = "Deep Work",
                        value = "${state.totalFocusedHours}h",
                        modifier = Modifier.weight(1f)
                    )
                    ReportStatPod(
                        label = "Sessions",
                        value = "112",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ==========================================
        // 3. BEHAVIORAL PATTERNS CARD
        // ==========================================
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(colors.surface)
                .border(1.dp, colors.outlineVariant.copy(alpha = 0.7f), RoundedCornerShape(20.dp))
                .padding(18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.primaryContainer)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "KEY INSIGHTS",
                            color = colors.primary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.6.sp
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .padding(top = 5.dp)
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(colors.primary)
                    )
                    Text(
                        text = "Highest consistency (94%) occurs on morning practices completed before 12:00 PM.",
                        color = colors.onSurface,
                        fontSize = 13.5.sp,
                        lineHeight = 18.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .padding(top = 5.dp)
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(colors.primary)
                    )
                    Text(
                        text = "Evening digital shutdown routines improve deep sleep recovery by an estimated 22%.",
                        color = colors.onSurface,
                        fontSize = 13.5.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // ==========================================
        // 4. RECENT ACTIVITY LOG
        // ==========================================
        Text(
            text = "ACTIVITY LOG",
            color = colors.onSurfaceVariant,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp
        )
        Spacer(modifier = Modifier.height(12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            val sampleLogs = listOf(
                EvidenceEntry("1", "Deep Work", "Today, 08:30 AM", "Unbroken focus on compiler optimizations.", ReflectionRating.EASY, 60),
                EvidenceEntry("2", "Movement Break", "Today, 12:30 PM", "Zone 2 brisk walk and diaphragmatic breathing.", ReflectionRating.OKAY, 30),
                EvidenceEntry("3", "Daily Reading", "Yesterday, 08:00 AM", "Meditations Book 4. Practiced pause before reacting.", ReflectionRating.EASY, 20),
                EvidenceEntry("4", "Morning Exercise", "Yesterday, 06:30 AM", "Full compound lifting session with tempo control.", ReflectionRating.HARD, 45)
            )

            sampleLogs.forEach { entry ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(colors.surface)
                        .border(1.dp, colors.outlineVariant.copy(alpha = 0.7f), RoundedCornerShape(18.dp))
                        .padding(16.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = entry.practiceTitle,
                                    color = colors.onSurface,
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                val (ratingText, ratingBg, ratingColor) = when (entry.rating) {
                                    ReflectionRating.EASY -> Triple("Easy", Color(0xFFDCFCE7), Color(0xFF16A34A))
                                    ReflectionRating.HARD -> Triple("Hard", Color(0xFFFEE2E2), Color(0xFFDC2626))
                                    else -> Triple("Solid", colors.primaryContainer, colors.primary)
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(ratingBg)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = ratingText,
                                        color = ratingColor,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Text(
                                text = "${entry.durationMinutes}m · ${entry.date}",
                                color = colors.onSurfaceVariant,
                                fontSize = 11.5.sp
                            )
                        }

                        if (entry.note.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = entry.note,
                                color = colors.onSurfaceVariant,
                                fontSize = 12.5.sp,
                                lineHeight = 16.sp
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

@Composable
private fun ReportStatPod(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    val colors = LuminaTheme.colors

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(colors.surfaceContainerLow)
            .border(1.dp, colors.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
            .padding(vertical = 12.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                color = colors.onSurfaceVariant,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                color = colors.onSurface,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.3).sp
            )
        }
    }
}


