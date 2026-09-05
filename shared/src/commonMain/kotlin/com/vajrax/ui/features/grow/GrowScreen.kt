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
        // 5. MOST CONSISTENT SECTION
        // ==========================================
        Text(
            text = "MOST CONSISTENT",
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
                .padding(horizontal = 18.dp, vertical = 14.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Daily Review",
                        color = colors.onSurface,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "98%",
                        color = colors.primary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = colors.outlineVariant.copy(alpha = 0.5f), thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Deep Work",
                        color = colors.onSurface,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "89%",
                        color = colors.primary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Safe clearance for bottom navigation dock
        Spacer(modifier = Modifier.navigationBarsPadding().height(96.dp))
    }
}
