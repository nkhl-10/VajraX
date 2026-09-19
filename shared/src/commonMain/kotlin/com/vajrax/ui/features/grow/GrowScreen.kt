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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.vajrax.ui.theme.LuminaTheme

/**
 * 1:1 Figma-Faithful Report Screen for Lumina Life OS.
 * Features Overall Completion score donut (78%), 7-day adherence bar chart,
 * side-by-side streak pods (Current/Best), and Most Consistent habit progress rows.
 */
@Composable
fun GrowScreen(
    state: GrowUiState,
    onIntent: (GrowIntent) -> Unit
) {
    val colors = LuminaTheme.colors
    var selectedTimePeriod by remember { mutableStateOf("This Week") }

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
        // 1. HEADER (Title + Segmented Pill Selector)
        // ==========================================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Report",
                color = colors.onSurface,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.6).sp
            )

            // Segmented Pill: [This Week] [This Month]
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFF1F5F9))
                    .padding(3.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    val periods = listOf("This Week", "This Month")
                    periods.forEach { period ->
                        val isSelected = period == selectedTimePeriod
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) Color.White else Color.Transparent)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { selectedTimePeriod = period }
                                .padding(horizontal = 14.dp, vertical = 7.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = period,
                                color = if (isSelected) colors.onSurface else Color(0xFF64748B),
                                fontSize = 12.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ==========================================
        // 2. OVERALL COMPLETION CARD
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
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = "Overall Completion",
                            color = colors.onSurface,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.3).sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Your productivity is increasing",
                            color = colors.onSurfaceVariant,
                            fontSize = 13.5.sp
                        )
                    }

                    // Green Pill Badge: ↑ 5%
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFDCFCE7))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "↑ 5%",
                            color = Color(0xFF16A34A),
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Centered Donut Gauge (78% SCORE)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val primaryColor = colors.primary
                    Canvas(modifier = Modifier.size(130.dp)) {
                        val stroke = 12.dp.toPx()
                        val arcPadding = stroke / 2f
                        val arcSize = Size(size.width - stroke, size.height - stroke)

                        // Background Ring
                        drawArc(
                            color = Color(0xFFEEF2FF),
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = Offset(arcPadding, arcPadding),
                            size = arcSize,
                            style = Stroke(width = stroke)
                        )

                        // Active Arc (78%)
                        drawArc(
                            color = primaryColor,
                            startAngle = -90f,
                            sweepAngle = 360f * 0.78f,
                            useCenter = false,
                            topLeft = Offset(arcPadding, arcPadding),
                            size = arcSize,
                            style = Stroke(width = stroke, cap = StrokeCap.Round)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "78%",
                            color = colors.onSurface,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            text = "SCORE",
                            color = Color(0xFF64748B),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom note
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEEF2FF))
                    )
                    Text(
                        text = "You achieved ",
                        color = Color(0xFF64748B),
                        fontSize = 13.sp
                    )
                    Text(
                        text = "6 more tasks",
                        color = colors.primary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = " compared to last week.",
                        color = Color(0xFF64748B),
                        fontSize = 13.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ==========================================
        // 3. THIS WEEK 7-DAY BAR CHART CARD
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
                Text(
                    text = "This Week",
                    color = colors.onSurface,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.3).sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Daily completions vs targeted workload",
                    color = colors.onSurfaceVariant,
                    fontSize = 13.5.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                val weeklyBars = listOf(
                    Triple("M", "6/8", 0.75f to false),
                    Triple("T", "7/8", 0.875f to false),
                    Triple("W", "5/8", 0.625f to false),
                    Triple("T", "8/8", 1.0f to false),
                    Triple("F", "7/8", 0.875f to false),
                    Triple("S", "4/8", 0.50f to false),
                    Triple("S", "6/8", 0.75f to true) // Active Sunday
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    weeklyBars.forEach { (dayLabel, ratioText, data) ->
                        val (fraction, isHighlight) = data

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = ratioText,
                                color = if (isHighlight) colors.primary else Color(0xFF64748B),
                                fontSize = 10.5.sp,
                                fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .width(18.dp)
                                    .height((90 * fraction).dp)
                                    .clip(RoundedCornerShape(9.dp))
                                    .background(if (isHighlight) colors.primary else Color(0xFFEEF2FF))
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            if (isHighlight) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(colors.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = dayLabel,
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else {
                                Text(
                                    text = dayLabel,
                                    color = Color(0xFF64748B),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ==========================================
        // 4. SIDE-BY-SIDE STREAK CARDS
        // ==========================================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Left Card: Current Streak
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(22.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.outlineVariant.copy(alpha = 0.7f), RoundedCornerShape(22.dp))
                    .padding(18.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Current Streak",
                            color = Color(0xFF64748B),
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                        // Flame icon in purple
                        Text("🔥", fontSize = 14.sp)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "12 days",
                        color = colors.onSurface,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.4).sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Keep it up!",
                        color = Color(0xFF64748B),
                        fontSize = 12.sp
                    )
                }
            }

            // Right Card: Best Streak
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(22.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.outlineVariant.copy(alpha = 0.7f), RoundedCornerShape(22.dp))
                    .padding(18.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Best Streak",
                            color = Color(0xFF64748B),
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                        // Medal / Ribbon icon in orange
                        Text("🏅", fontSize = 14.sp)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "21 days",
                        color = colors.onSurface,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.4).sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Achieved Jan 14",
                        color = Color(0xFF64748B),
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // ==========================================
        // 5. YOUR PRINCIPLES SECTION
        // ==========================================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "YOUR PRINCIPLES",
                color = Color(0xFF8B95A5),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Which part of your life system is becoming stronger?",
            color = colors.onSurfaceVariant,
            fontSize = 13.sp
        )
        Spacer(modifier = Modifier.height(14.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(colors.surface)
                .border(1.dp, colors.outlineVariant.copy(alpha = 0.7f), RoundedCornerShape(22.dp))
                .padding(horizontal = 18.dp, vertical = 16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                state.principles.forEachIndexed { index, principle ->
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
                                    text = principle.name,
                                    color = colors.onSurface,
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (principle.isStrongest) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFFDCFCE7))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "Strongest",
                                            color = Color(0xFF16A34A),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                } else if (principle.isNeedsAttention) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFFFEF3C7))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "Needs attention",
                                            color = Color(0xFFD97706),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Text(
                                text = "${principle.percentage}%",
                                color = if (principle.isStrongest) colors.primary else colors.onSurface,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Progress Bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color(0xFFEEF2FF))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(principle.percentage / 100f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(if (principle.isNeedsAttention) Color(0xFFF59E0B) else colors.primary)
                            )
                        }
                    }

                    if (index < state.principles.size - 1) {
                        HorizontalDivider(
                            color = colors.outlineVariant.copy(alpha = 0.4f),
                            thickness = 0.5.dp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(26.dp))

        // ==========================================
        // 6. PATTERN DISCOVERY LAYER
        // ==========================================
        Text(
            text = "PATTERN DISCOVERY",
            color = Color(0xFF8B95A5),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp
        )
        Spacer(modifier = Modifier.height(12.dp))

        val insight = state.activeInsight
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(colors.surface)
                .border(1.dp, colors.outlineVariant.copy(alpha = 0.7f), RoundedCornerShape(22.dp))
                .padding(18.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = insight.title,
                        color = colors.onSurface,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // Interactive "Why?" Pill Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.primaryContainer)
                            .clickable { onIntent(GrowIntent.OpenPatternWhyDialog) }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Why?",
                                color = colors.primary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "→",
                                color = colors.primary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = insight.interpretation,
                    color = colors.primary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = insight.rawDataSummary,
                        color = colors.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                    Text(
                        text = insight.confidenceScore,
                        color = Color(0xFF16A34A),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Safe clearance for bottom navigation dock
        Spacer(modifier = Modifier.navigationBarsPadding().height(96.dp))
    }

    // ==========================================
    // TRANSPARENT "WHY?" BREAKDOWN DIALOG
    // ==========================================
    // ==========================================
    // TRANSPARENT "WHY?" BREAKDOWN DIALOG
    // ==========================================
    if (state.showWhyDialog) {
        val insight = state.activeInsight
        Dialog(
            onDismissRequest = { onIntent(GrowIntent.DismissPatternWhyDialog) }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.outlineVariant.copy(alpha = 0.8f), RoundedCornerShape(24.dp))
                    .padding(22.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Why this pattern occurs",
                                color = colors.onSurface,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.3).sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Deterministic Behavioral Breakdown",
                                color = colors.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(colors.surfaceContainerLow)
                                .clickable { onIntent(GrowIntent.DismissPatternWhyDialog) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "✕",
                                color = colors.onSurfaceVariant,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text(
                        text = "Hourly Completion Breakdown:",
                        color = colors.onSurface,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    insight.whyBreakdown.forEach { point ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.surfaceContainerLow)
                                .padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = point.timeWindow,
                                        color = colors.onSurface,
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = point.sampleSize,
                                        color = colors.onSurfaceVariant,
                                        fontSize = 11.sp
                                    )
                                }
                                Text(
                                    text = point.completionRate,
                                    color = if (point.completionRate.startsWith("9")) Color(0xFF16A34A) else colors.onSurface,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Column {
                        Text(
                            text = "Root Cause:",
                            color = colors.onSurface,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = insight.rootCause,
                            color = colors.onSurfaceVariant,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                    }

                    Column {
                        Text(
                            text = "Recommendation:",
                            color = colors.primary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = insight.recommendation,
                            color = colors.onSurface,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Bottom Action Button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(colors.primary)
                            .clickable { onIntent(GrowIntent.DismissPatternWhyDialog) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Got it",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

