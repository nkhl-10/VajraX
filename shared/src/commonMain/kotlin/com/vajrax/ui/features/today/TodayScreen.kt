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
import com.vajrax.ui.utils.gyroShadowCard
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.graphics.graphicsLayer
import kotlin.math.abs
import com.vajrax.ui.theme.LuminaTheme

/**
 * Human-Crafted Home Screen for Lumina Life OS.
 * Faithfully matches the reference design with Good Morning greeting, Task Completion card,
 * circular progress indicator, 3 stat pods, and clear Single-Core NOW / NEXT / LATER task flow.
 */
sealed class TodayTaskItem {
    abstract val id: String
    data class Completed(val title: String, val timeTag: String, override val id: String) : TodayTaskItem()
    data class Active(val title: String, val timeTag: String, override val id: String) : TodayTaskItem()
    data class Next(val title: String, val timeTag: String, override val id: String) : TodayTaskItem()
    data class Later(val title: String, val timeTag: String, override val id: String) : TodayTaskItem()
}

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

        val allTasks = remember(state.completedItems, state.currentFocus, state.nextItems, state.laterItems) {
        buildList {
            if (state.completedItems.isNotEmpty() || state.currentFocus != null || state.nextItems.isNotEmpty() || state.laterItems.isNotEmpty()) {
                state.completedItems.forEach { add(TodayTaskItem.Completed(it.title, it.scheduledTime ?: "Done", it.id)) }
                state.currentFocus?.let { add(TodayTaskItem.Active(it.title, it.scheduledTime ?: "Today", it.id)) }
                state.nextItems.forEach { add(TodayTaskItem.Next(it.title, it.scheduledTime ?: "Next", it.id)) }
                state.laterItems.forEach { add(TodayTaskItem.Later(it.title, it.scheduledTime ?: "Later", it.id)) }
            } else {
                add(TodayTaskItem.Completed("planning", "Tomorrow", "mc1"))
                add(TodayTaskItem.Completed("Weekly planning", "Tomorrow", "mc2"))
                add(TodayTaskItem.Active("Daily review", "Today", "ma1"))
                add(TodayTaskItem.Later("Notes", "2 days ago", "ml1"))
                add(TodayTaskItem.Later("Notes", "2 days ago", "ml2"))
            }
        }
    }

    val listState = rememberLazyListState()

    // 2. Main Screen Body
    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        item(key = "header") {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Good Morning.",
                color = colors.onSurface,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            )
            Spacer(modifier = Modifier.height(18.dp))
            
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
                                    text = "o",
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
                            val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)
                            val arcSize = Size(diameter, diameter)

                            drawArc(
                                color = trackColor,
                                startAngle = 0f,
                                sweepAngle = 360f,
                                useCenter = false,
                                topLeft = topLeft,
                                size = arcSize,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )

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
            
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (state.lastCompletedActionId != null) {
            item(key = "evidence_banner") {
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
                                text = "o",
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
                                text = "x",
                                color = colors.outline,
                                fontSize = 12.sp,
                                modifier = Modifier
                                    .clickable { onIntent(TodayIntent.DismissOptionalEvidencePrompt) }
                                    .padding(4.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(26.dp))
            }
        } else {
            item(key = "spacer_if_no_banner") {
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        itemsIndexed(allTasks, key = { _, task -> task.id }) { index, item ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 40.dp) // Provide space for maxTranslationX
                    .padding(vertical = 8.dp)
                    .graphicsLayer {
                        val layoutInfo = listState.layoutInfo
                        val viewportHeight = layoutInfo.viewportSize.height.toFloat()
                        val centerY = viewportHeight / 2f

                        val itemInfo = layoutInfo.visibleItemsInfo.find { it.key == item.id }
                        if (itemInfo != null) {
                            val itemY = itemInfo.offset.toFloat() + (itemInfo.size.toFloat() / 2f)
                            val distanceFromCenter = itemY - centerY
                            
                            val normalizedDist = (distanceFromCenter / (viewportHeight / 1.6f)).coerceIn(-1f, 1f)
                            
                            val maxTranslationX = 60.dp.toPx()
                            
                            // User provided parabolic true arc formula
                            translationX = maxTranslationX * (1f - (normalizedDist * normalizedDist))
                            
                            alpha = 1f - 0.7f * abs(normalizedDist)
                            
                            val scale = 1f - 0.15f * abs(normalizedDist)
                            scaleX = scale
                            scaleY = scale
                        }
                    }
            ) {
                when (item) {
                    is TodayTaskItem.Completed -> CompletedTaskItem(
                        title = item.title,
                        timeTag = item.timeTag,
                        onClick = { onIntent(TodayIntent.OpenEvidenceForAction(item.id)) }
                    )
                    is TodayTaskItem.Active -> ActiveFocusCard(
                        title = item.title,
                        timeTag = item.timeTag,
                        onComplete = { onIntent(TodayIntent.QuickCompletePractice(item.id)) },
                        onSaveNote = { note -> onIntent(TodayIntent.SubmitEvidence(item.id, note, null)) },
                        onClick = { onIntent(TodayIntent.StartPractice(item.id)) }
                    )
                    is TodayTaskItem.Next -> UpcomingTaskItem(
                        title = item.title,
                        timeTag = item.timeTag,
                        onClick = { onIntent(TodayIntent.StartPractice(item.id)) }
                    )
                    is TodayTaskItem.Later -> NoteHistoryItem(
                        title = item.title,
                        timeTag = item.timeTag
                    )
                }
            }
        }
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
    onSaveNote: (String) -> Unit,
    onClick: () -> Unit
) {
    val colors = LuminaTheme.colors
    var isNotesExpanded by remember { mutableStateOf(false) }
    var noteText by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .gyroShadowCard()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFFF3F4FB))
            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(20.dp))
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // Solid Blue Circle with white dot
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(colors.primary)
                            .clickable(onClick = onClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                        )
                    }

                    Text(
                        text = title,
                        color = colors.primary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = timeTag,
                    color = colors.primary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Mark Done Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFC7D2FE), RoundedCornerShape(20.dp))
                        .clickable(onClick = onComplete)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("✓", color = colors.primary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text("Mark done", color = colors.primary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }

                // Notes Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFC7D2FE), RoundedCornerShape(20.dp))
                        .clickable { isNotesExpanded = !isNotesExpanded }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("✏️", fontSize = 13.sp)
                    Text("Notes", color = colors.primary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            // Expanded Notes Area
            if (isNotesExpanded) {
                Spacer(modifier = Modifier.height(16.dp))
                
                androidx.compose.foundation.text.BasicTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = colors.onSurface,
                        fontSize = 15.sp
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFC7D2FE), RoundedCornerShape(12.dp))
                        .padding(14.dp),
                    decorationBox = { innerTextField ->
                        if (noteText.isEmpty()) {
                            Text("Add a quick note for this workout...", color = Color(0xFF9CA3AF), fontSize = 15.sp)
                        }
                        innerTextField()
                    }
                )
                
                Spacer(modifier = Modifier.height(12.dp))

                // Save Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(colors.primary)
                        .clickable { 
                            onSaveNote(noteText)
                            isNotesExpanded = false
                            noteText = ""
                        }
                        .padding(horizontal = 24.dp, vertical = 10.dp)
                ) {
                    Text("Save", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
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



