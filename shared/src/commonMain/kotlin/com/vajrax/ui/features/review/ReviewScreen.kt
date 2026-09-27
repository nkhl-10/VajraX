package com.vajrax.ui.features.review

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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vajrax.ui.theme.LuminaTheme

/**
 * Figma-Faithful Report Screen for Lumina Life OS.
 * Matches Figma: "Report" ExtraBold 28sp header, Period-Toggle pill,
 * Overall-Card (white) with progress ring, Trend-Card, Streaks-Row (2 cards),
 * Metrics-Super-Card (Consistent / Needs Attention).
 */
@Composable
fun ReviewScreen(
    state: ReviewUiState,
    onIntent: (ReviewIntent) -> Unit
) {
    val colors = LuminaTheme.colors
    var selectedPeriod by remember { mutableStateOf(0) }
    val periods = listOf("Week", "Month", "3 Months")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)   // Figma #F9FAFB
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // ==========================================
        // HEADER — Figma: "Report" ExtraBold 800, 28sp
        // ==========================================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Report",
                color = colors.onSurface,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.5).sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ==========================================
        // PERIOD TOGGLE — Figma: Period-Toggle pill, bg #F3F4F6
        // ==========================================
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF3F4F6))
                .padding(4.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(0.dp)) {
                periods.forEachIndexed { index, label ->
                    val isActive = selectedPeriod == index
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isActive) colors.surface else Color.Transparent)
                            .then(
                                if (isActive) Modifier.shadow(elevation = 1.dp, shape = RoundedCornerShape(10.dp))
                                else Modifier
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { selectedPeriod = index }
                            .padding(horizontal = 16.dp, vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = if (isActive) colors.onSurface else colors.onSurfaceVariant,
                            fontSize = 13.sp,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ==========================================
        // OVERALL CARD — Figma: white card, progress ring + stats
        // ==========================================
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(colors.surface)
                .border(1.dp, colors.outlineVariant, RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Card Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Overall Progress",
                            color = colors.onSurface,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${state.completedCount}/${state.plannedCount} practices completed",
                            color = colors.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.primaryContainer)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${state.weeklyConsistency}%",
                            color = colors.primary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Progress Ring Container — centered
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val progressFrac = (state.weeklyConsistency / 100f).coerceIn(0f, 1f)
                    val trackColor = colors.surfaceContainerHigh
                    val progressColor = colors.primary

                    androidx.compose.foundation.Canvas(modifier = Modifier.size(96.dp)) {
                        val strokeWidth = 9.dp.toPx()
                        val diameter = size.minDimension - strokeWidth
                        val topLeft = androidx.compose.ui.geometry.Offset(strokeWidth / 2f, strokeWidth / 2f)
                        val arcSize = androidx.compose.ui.geometry.Size(diameter, diameter)
                        drawArc(
                            color = trackColor,
                            startAngle = 0f, sweepAngle = 360f, useCenter = false,
                            topLeft = topLeft, size = arcSize,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Round)
                        )
                        drawArc(
                            color = progressColor,
                            startAngle = -90f,
                            sweepAngle = 360f * (if (progressFrac > 0f) progressFrac else 0.1f),
                            useCenter = false,
                            topLeft = topLeft, size = arcSize,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Round)
                        )
                    }
                    Text(
                        text = "${state.weeklyConsistency}%",
                        color = colors.onSurface,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Insights Footer — 3 mini-stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ReportStatPod("Completed", "${state.completedCount}", modifier = Modifier.weight(1f))
                    ReportStatPod("Planned", "${state.plannedCount}", modifier = Modifier.weight(1f))
                    ReportStatPod("Best Area", state.strongestArea.take(8), modifier = Modifier.weight(1f))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ==========================================
        // STREAKS ROW — 2 cards side by side
        // ==========================================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Current Streak Card
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.outlineVariant, RoundedCornerShape(18.dp))
                    .padding(18.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("🔥", fontSize = 24.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Current Streak",
                        color = colors.onSurfaceVariant,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "7 days",
                        color = colors.onSurface,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            // Best Streak Card
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.outlineVariant, RoundedCornerShape(18.dp))
                    .padding(18.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("⭐", fontSize = 24.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Best Streak",
                        color = colors.onSurfaceVariant,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "21 days",
                        color = colors.onSurface,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ==========================================
        // METRICS SUPER CARD — Consistent + Needs Attention
        // ==========================================
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(colors.surface)
                .border(1.dp, colors.outlineVariant, RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Consistent",
                    color = colors.onSurface,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("✅", fontSize = 16.sp)
                    Text(
                        text = state.strongestArea,
                        color = colors.onSurface,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "+12% vs last week",
                        color = colors.statusSuccess,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = colors.outlineVariant, thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Needs Attention",
                    color = colors.onSurface,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("⚠️", fontSize = 16.sp)
                    Text(
                        text = "Evening routine",
                        color = colors.onSurface,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "-8% vs last week",
                        color = colors.statusError,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Pattern Detected (if available)
        val pattern = state.patternInsight
        if (pattern != null) {
            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(colors.surfaceContainerLow)
                    .border(1.dp, colors.outlineVariant.copy(alpha = 0.6f), RoundedCornerShape(18.dp))
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(colors.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) { Text("💡", fontSize = 16.sp) }
                        Text(
                            text = pattern.title,
                            color = colors.primary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = pattern.description, color = colors.onSurface, fontSize = 13.sp, lineHeight = 19.sp)
                    Spacer(modifier = Modifier.height(14.dp))
                    if (!state.isRecommendationApplied) {
                        Button(
                            onClick = { onIntent(ReviewIntent.ApplyBhedaRecommendation) },
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = colors.onPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("⚡ Apply Recommendation", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.navigationBarsPadding().height(96.dp))
    }
}

@Composable
private fun ReportStatPod(label: String, value: String, modifier: Modifier = Modifier) {
    val colors = LuminaTheme.colors
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surfaceContainerLow)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = label, color = colors.onSurfaceVariant, fontSize = 10.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(3.dp))
            Text(text = value, color = colors.onSurface, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
    }
}
