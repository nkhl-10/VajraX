package com.vajrax.ui.features.today

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
import androidx.compose.material3.*
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
 * Human-Crafted Home Screen for Lumina Life OS.
 * Faithfully matches the reference design with Good Morning greeting, Task Completion card,
 * circular progress indicator, 3 stat pods, and clear Single-Core NOW / NEXT / LATER task flow.
 */
@Composable
fun TodayScreen(
    state: TodayUiState,
    onIntent: (TodayIntent) -> Unit
) {
    val colors = LuminaTheme.colors

    // 1. Live Focus Timer Dialog (if active)
    val activeTimer = state.activeTimerItem
    if (activeTimer != null) {
        FocusTimerDialog(
            practiceTitle = activeTimer.title,
            targetDurationMinutes = activeTimer.targetDurationMinutes,
            minimumDurationMinutes = activeTimer.minimumDurationMinutes,
            onFinish = { elapsed -> onIntent(TodayIntent.FinishTimerSession(activeTimer.id, elapsed)) },
            onCancel = { onIntent(TodayIntent.CancelTimer) }
        )
        return
    }

    // 2. Main Screen Body
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Header: Good Morning.
        Text(
            text = "Good Morning.",
            color = colors.onSurface,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.5).sp
        )

        Spacer(modifier = Modifier.height(18.dp))

        // ==========================================
        // 1. TASK COMPLETION ELEVATED CARD
        // ==========================================
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(26.dp))
                .background(colors.surface)
                .border(1.dp, colors.outlineVariant.copy(alpha = 0.7f), RoundedCornerShape(26.dp))
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Top Row: Task Completion + Pill Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = "Task Completion",
                            color = colors.onSurface,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.3).sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "You're on track this week",
                            color = colors.onSurfaceVariant,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }

                    // 75% Badge with checkmark
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(colors.primaryContainer)
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "✓",
                                color = colors.primary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${state.targetPercentage}%",
                                color = colors.primary,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                // Circular Progress Ring (20%)
                val completedFraction = if (state.todayTotalCount > 0) {
                    (state.todayCompletedCount.toFloat() / state.todayTotalCount.toFloat()).coerceIn(0f, 1f)
                } else 0.20f
                val ringPercentText = "${(completedFraction * 100).toInt()}%"

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val trackColor = colors.surfaceContainerHigh
                    val progressColor = colors.primary

                    Canvas(modifier = Modifier.size(86.dp)) {
                        val strokeWidth = 8.dp.toPx()
                        val diameter = size.minDimension - strokeWidth
                        val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
                        val arcSize = Size(diameter, diameter)

                        // Background track circle
                        drawArc(
                            color = trackColor,
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )

                        // Foreground progress arc
                        drawArc(
                            color = progressColor,
                            startAngle = -90f,
                            sweepAngle = 360f * (if (completedFraction > 0f) completedFraction else 0.20f),
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }

                    Text(
                        text = if (state.todayCompletedCount > 0) ringPercentText else "20%",
                        color = colors.onSurface,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(22.dp))

                // Bottom Row of 3 Stat Pods
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HomeStatPod(
                        label = "Today",
                        value = "${state.todayCompletedCount}/${state.todayTotalCount}",
                        modifier = Modifier.weight(1f)
                    )
                    HomeStatPod(
                        label = "Week",
                        value = "${state.consistencyPercentage}%",
                        modifier = Modifier.weight(1f)
                    )
                    HomeStatPod(
                        label = "Consistency",
                        value = "${state.pacePercentage}%",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // ==========================================
        // OPTIONAL EVIDENCE PROMPT BANNER
        // ==========================================
        if (state.lastCompletedActionId != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.surfaceContainerLow)
                    .border(1.dp, colors.outlineVariant.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "✓",
                            color = Color(0xFF16A34A),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${state.lastCompletedActionTitle ?: "Practice"} completed",
                            color = colors.onSurface,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.primaryContainer)
                                .clickable {
                                    onIntent(TodayIntent.OpenEvidenceForAction(state.lastCompletedActionId))
                                }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "+ Add evidence",
                                color = colors.primary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "✕",
                            color = colors.outline,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .clickable { onIntent(TodayIntent.DismissOptionalEvidencePrompt) }
                                .padding(4.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(26.dp))

        // ==========================================
        // 2. SINGLE-CORE TASK SECTIONS (NOW / NEXT / LATER / COMPLETED)
        // ==========================================
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section A: COMPLETED ITEMS
            if (state.completedItems.isNotEmpty()) {
                state.completedItems.forEach { item ->
                    CompletedTaskItem(
                        title = item.title,
                        timeTag = item.scheduledTime ?: "Done",
                        onClick = { onIntent(TodayIntent.OpenEvidenceForAction(item.id)) }
                    )
                }
            } else {
                // Default reference mockup completed items
                CompletedTaskItem(
                    title = "planning",
                    timeTag = "Tomorrow",
                    onClick = { }
                )
                CompletedTaskItem(
                    title = "Weekly planning",
                    timeTag = "Tomorrow",
                    onClick = { }
                )
            }

            // Section B: NOW (Current Focus)
            val current = state.currentFocus
            ActiveFocusCard(
                title = current?.title ?: "Daily review",
                timeTag = current?.scheduledTime ?: "Today",
                onComplete = {
                    if (current != null) {
                        onIntent(TodayIntent.QuickCompletePractice(current.id))
                    }
                },
                onClick = {
                    if (current != null) {
                        onIntent(TodayIntent.StartPractice(current.id))
                    }
                }
            )

            // Section C: NEXT ITEMS
            if (state.nextItems.isNotEmpty()) {
                state.nextItems.forEach { item ->
                    UpcomingTaskItem(
                        title = item.title,
                        timeTag = item.scheduledTime ?: "Next",
                        onClick = { onIntent(TodayIntent.StartPractice(item.id)) }
                    )
                }
            }

            // Section D: LATER / NOTE HISTORY ITEMS
            if (state.laterItems.isNotEmpty()) {
                state.laterItems.forEach { item ->
                    NoteHistoryItem(
                        title = item.title,
                        timeTag = item.scheduledTime ?: "Later"
                    )
                }
            } else {
                // Default reference notes items
                NoteHistoryItem(
                    title = "Notes",
                    timeTag = "2 days ago"
                )
                NoteHistoryItem(
                    title = "Notes",
                    timeTag = "2 days ago"
                )
            }
        }

        // Safe clearance for bottom navigation bar
        Spacer(modifier = Modifier.navigationBarsPadding().height(96.dp))
    }

    // Evidence Sheet
    if (state.showEvidenceSheet && state.selectedActionIdForEvidence != null) {
        EvidenceBottomSheet(
            onDismissRequest = { onIntent(TodayIntent.DismissEvidenceSheet) },
            onSubmitEvidence = { note, rating ->
                onIntent(
                    TodayIntent.SubmitEvidence(
                        actionId = state.selectedActionIdForEvidence,
                        note = note,
                        rating = rating
                    )
                )
            }
        )
    }
}

@Composable
private fun HomeStatPod(
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

@Composable
private fun CompletedTaskItem(
    title: String,
    timeTag: String,
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
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.weight(1f)
        ) {
            // Filled Royal Indigo Check Circle
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(colors.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "✓",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = title,
                color = colors.onSurface,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            text = timeTag,
            color = colors.outline,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.Normal
        )
    }
}

@Composable
private fun ActiveFocusCard(
    title: String,
    timeTag: String,
    onComplete: () -> Unit,
    onClick: () -> Unit
) {
    val colors = LuminaTheme.colors

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.primaryContainer)
            .border(1.dp, Color(0xFFC7D2FE), RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Indigo Circle with inner dash/dot (Clickable to quick-complete)
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .border(2.dp, colors.primary, CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onComplete
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp, 2.dp)
                            .background(colors.primary, RoundedCornerShape(1.dp))
                    )
                }

                Text(
                    text = title,
                    color = colors.primary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = timeTag,
                    color = colors.primary,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun UpcomingTaskItem(
    title: String,
    timeTag: String,
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
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.weight(1f)
        ) {
            // Subtle open circle
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, colors.outlineVariant, CircleShape)
            )

            Text(
                text = title,
                color = colors.onSurface,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Text(
            text = timeTag,
            color = colors.outline,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.Normal
        )
    }
}

@Composable
private fun NoteHistoryItem(
    title: String,
    timeTag: String
) {
    val colors = LuminaTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.weight(1f)
        ) {
            // Gray Circle with 3 dots
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color.Transparent)
                    .border(1.2.dp, colors.outlineVariant, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(1.5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(2.5.dp).clip(CircleShape).background(colors.outline))
                    Box(modifier = Modifier.size(2.5.dp).clip(CircleShape).background(colors.outline))
                    Box(modifier = Modifier.size(2.5.dp).clip(CircleShape).background(colors.outline))
                }
            }

            Text(
                text = title,
                color = colors.onSurfaceVariant,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Text(
            text = timeTag,
            color = colors.outline,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.Normal
        )
    }
}



