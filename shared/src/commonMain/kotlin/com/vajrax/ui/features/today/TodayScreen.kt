package com.vajrax.ui.features.today

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vajrax.ui.theme.LuminaTheme

/**
 * Home Screen — pixel-faithful match of the Figma/reference image.
 *
 * Layout (top → bottom):
 *  1. "Good Morning." ExtraBold heading
 *  2. White card: Task Completion title + ring top-right + stat pods
 *  3. Agenda section header: "30-Day Challenge" / "Complete daily routine"
 *  4. Flat task list:
 *       • Completed  → indigo filled circle ✓  + title + time
 *       • Active     → indigo circle (dot) card, #F5F3FF bg, Mark done + Notes
 *       • Upcoming   → gray circle + title + time
 *       • Later      → gray circle + title + time (muted)
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

    // Focus timer dialog overlay
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

    // Build flat task list from state (or use demo data)
    val allTasks = remember(state.completedItems, state.currentFocus, state.nextItems, state.laterItems) {
        buildList {
            val hasRealData = state.completedItems.isNotEmpty()
                || state.currentFocus != null
                || state.nextItems.isNotEmpty()
                || state.laterItems.isNotEmpty()

            if (hasRealData) {
                state.completedItems.forEach {
                    add(TodayTaskItem.Completed(it.title, it.scheduledTime ?: "Done", it.id))
                }
                state.currentFocus?.let {
                    add(TodayTaskItem.Active(it.title, it.scheduledTime ?: "Now", it.id))
                }
                state.nextItems.forEach {
                    add(TodayTaskItem.Next(it.title, it.scheduledTime ?: "Next", it.id))
                }
                state.laterItems.forEach {
                    add(TodayTaskItem.Later(it.title, it.scheduledTime ?: "Later", it.id))
                }
            } else {
                // Demo data matching the reference image
                add(TodayTaskItem.Completed("Meditation / breathing", "5:20 AM", "demo_c1"))
                add(TodayTaskItem.Completed("Light stretching / mobility", "5:40 AM", "demo_c2"))
                add(TodayTaskItem.Active("Workout", "6:00 AM", "demo_a1"))
                add(TodayTaskItem.Next("Cool down + shower", "7:00 AM", "demo_n1"))
                add(TodayTaskItem.Next("Healthy breakfast", "7:30 AM", "demo_n2"))
                add(TodayTaskItem.Later("5-10 min movement break", "10:30 AM", "demo_l1"))
            }
        }
    }

    // Completions fraction for ring
    val completedFraction = when {
        state.todayTotalCount > 0 ->
            (state.todayCompletedCount.toFloat() / state.todayTotalCount.toFloat()).coerceIn(0f, 1f)
        else -> 0.20f
    }
    val ringPercent = if (state.todayCompletedCount > 0)
        "${(completedFraction * 100).toInt()}%"
    else "20%"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)   // #F9FAFB
            .statusBarsPadding(),
        contentPadding = PaddingValues(bottom = 110.dp)
    ) {
        // ─────────────────────────────────────────
        // 1. HEADING
        // ─────────────────────────────────────────
        item(key = "heading") {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Good Morning.",
                color = colors.onSurface,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.5).sp,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // ─────────────────────────────────────────
        // 2. TASK COMPLETION CARD
        // ─────────────────────────────────────────
        item(key = "completion_card") {
            TaskCompletionCard(
                completedFraction = completedFraction,
                ringPercent = ringPercent,
                todayCompleted = state.todayCompletedCount,
                todayTotal = state.todayTotalCount,
                weekPercent = state.consistencyPercentage,
                consistencyPercent = state.pacePercentage,
                subtitle = "You're on track this Day"
            )
            Spacer(modifier = Modifier.height(20.dp))
        }

        // ─────────────────────────────────────────
        // 3. AGENDA SECTION HEADER
        // ─────────────────────────────────────────
        item(key = "agenda_header") {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                    text = "30-Day Challenge",
                    color = colors.onSurface,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.2).sp
                )
                Text(
                    text = "Complete daily routine",
                    color = colors.onSurfaceVariant,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // ─────────────────────────────────────────
        // 4. EVIDENCE BANNER (optional)
        // ─────────────────────────────────────────
        if (state.lastCompletedActionId != null) {
            item(key = "evidence_banner") {
                EvidenceBanner(
                    title = state.lastCompletedActionTitle ?: "Practice",
                    onAddEvidence = { onIntent(TodayIntent.OpenEvidenceForAction(state.lastCompletedActionId)) },
                    onDismiss = { onIntent(TodayIntent.DismissOptionalEvidencePrompt) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // ─────────────────────────────────────────
        // 5. FLAT TASK LIST — no arc/dial scroll
        // ─────────────────────────────────────────
        items(allTasks, key = { it.id }) { task ->
            when (task) {
                is TodayTaskItem.Completed -> CompletedRow(
                    title = task.title,
                    time = task.timeTag,
                    onClick = { onIntent(TodayIntent.OpenEvidenceForAction(task.id)) }
                )
                is TodayTaskItem.Active -> ActiveCard(
                    title = task.title,
                    time = task.timeTag,
                    onComplete = { onIntent(TodayIntent.QuickCompletePractice(task.id)) },
                    onSaveNote = { note -> onIntent(TodayIntent.SubmitEvidence(task.id, note, null)) },
                    onTap = { onIntent(TodayIntent.StartPractice(task.id)) }
                )
                is TodayTaskItem.Next -> UpcomingRow(
                    title = task.title,
                    time = task.timeTag,
                    onClick = { onIntent(TodayIntent.StartPractice(task.id)) }
                )
                is TodayTaskItem.Later -> LaterRow(
                    title = task.title,
                    time = task.timeTag
                )
            }
        }
    }

    // Evidence bottom sheet
    if (state.showEvidenceSheet && state.selectedActionIdForEvidence != null) {
        EvidenceBottomSheet(
            onDismissRequest = { onIntent(TodayIntent.DismissEvidenceSheet) },
            onSubmitEvidence = { note, rating ->
                onIntent(TodayIntent.SubmitEvidence(state.selectedActionIdForEvidence, note, rating))
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TASK COMPLETION CARD
// White card, title left + ring top-right, stat pods bottom
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun TaskCompletionCard(
    completedFraction: Float,
    ringPercent: String,
    todayCompleted: Int,
    todayTotal: Int,
    weekPercent: Int,
    consistencyPercent: Int,
    subtitle: String
) {
    val colors = LuminaTheme.colors

    Box(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(colors.surface)                   // white
            .border(1.dp, colors.outlineVariant, RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {

            // ── Title row with ring ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Task Completion",
                        color = colors.onSurface,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.3).sp
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = subtitle,
                        color = colors.onSurfaceVariant,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal
                    )
                }

                // Progress ring (top-right)
                ProgressRing(
                    fraction = completedFraction,
                    label = ringPercent,
                    size = 72
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Stat pods ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatPod(
                    label = "Today",
                    value = "$todayCompleted/$todayTotal",
                    modifier = Modifier.weight(1f)
                )
                StatPod(
                    label = "Week",
                    value = "$weekPercent%",
                    modifier = Modifier.weight(1f)
                )
                StatPod(
                    label = "Consistency",
                    value = "$consistencyPercent%",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// PROGRESS RING — circular arc, label in center
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ProgressRing(
    fraction: Float,
    label: String,
    size: Int = 72
) {
    val colors = LuminaTheme.colors
    val trackColor = Color(0xFFE5E7EB)     // light gray track
    val progressColor = colors.primary     // indigo arc

    Box(
        modifier = Modifier.size(size.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 7.dp.toPx()
            val inset = strokeWidth / 2f
            val arcSize = Size(this.size.width - strokeWidth, this.size.height - strokeWidth)
            val topLeft = Offset(inset, inset)

            // Track
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Progress arc
            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = 360f * fraction.coerceAtLeast(0.05f),
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        Text(
            text = label,
            color = colors.onSurface,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// STAT POD
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun StatPod(label: String, value: String, modifier: Modifier = Modifier) {
    val colors = LuminaTheme.colors
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(colors.surfaceContainerLow)
            .border(1.dp, colors.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
            .padding(vertical = 12.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                color = colors.onSurfaceVariant,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                color = colors.onSurface,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.3).sp
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// EVIDENCE BANNER
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun EvidenceBanner(
    title: String,
    onAddEvidence: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LuminaTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFF0FDF4))
            .border(1.dp, Color(0xFFBBF7D0), RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "✓  $title completed",
            color = Color(0xFF15803D),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.primaryContainer)
                    .clickable(onClick = onAddEvidence)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text("+ Add evidence", color = colors.primary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Text(
                text = "×",
                color = colors.onSurfaceVariant,
                fontSize = 14.sp,
                modifier = Modifier.clickable(onClick = onDismiss).padding(4.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// HOME TASK ROW — single unified layout for every list state.
// Icon (28dp fixed) + title (weight, ellipsis) + time (fixed 78dp, end-aligned).
// One layout for all states guarantees identical left alignment, so an icon
// can never go off-screen while another row's icon stays visible.
// ─────────────────────────────────────────────────────────────────────────────
private enum class HomeIconStyle { Completed, Upcoming }

@Composable
private fun HomeTaskRow(
    title: String,
    time: String,
    iconStyle: HomeIconStyle,
    onClick: (() -> Unit)? = null
) {
    val colors = LuminaTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                ) else Modifier
            )
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Fixed 28dp leading icon — always composed, never weighted away.
        when (iconStyle) {
            HomeIconStyle.Completed -> Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF4F46E5)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "✓",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            HomeIconStyle.Upcoming -> Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(colors.surfaceContainerHigh)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            color = colors.onSurface,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            softWrap = false,
            modifier = Modifier.weight(1f)
        )
        // Fixed-width time column: long times ("10:30 AM") can never push
        // the title/icon left, short times never misalign the column.
        Text(
            text = time,
            color = colors.onSurfaceVariant,
            fontSize = 13.sp,
            fontWeight = FontWeight.Normal,
            maxLines = 1,
            softWrap = false,
            textAlign = androidx.compose.ui.text.style.TextAlign.End,
            modifier = Modifier.width(78.dp)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// COMPLETED ROW — indigo filled circle ✓
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun CompletedRow(title: String, time: String, onClick: () -> Unit) {
    HomeTaskRow(
        title = title,
        time = time,
        iconStyle = HomeIconStyle.Completed,
        onClick = onClick
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// ACTIVE CARD — #F5F3FF bg, indigo circle-dot, bold title, Mark done + Notes
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ActiveCard(
    title: String,
    time: String,
    onComplete: () -> Unit,
    onSaveNote: (String) -> Unit,
    onTap: () -> Unit
) {
    val colors = LuminaTheme.colors
    var notesExpanded by remember { mutableStateOf(false) }
    var noteText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFFF5F3FF))              // light indigo tint
            .border(1.dp, Color(0xFFDDD6FE), RoundedCornerShape(18.dp))
            .padding(horizontal = 14.dp, vertical = 14.dp)
    ) {
        // Header row
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
                // Indigo circle with white dot (active indicator)
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(colors.primary)
                        .clickable(onClick = onTap),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    )
                }
                Text(
                    text = title,
                    color = colors.primary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = time,
                color = colors.primary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Mark done
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFC7D2FE), RoundedCornerShape(20.dp))
                    .clickable(onClick = onComplete)
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text("✓", color = colors.primary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text("Mark done", color = colors.primary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }

            // Notes
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFC7D2FE), RoundedCornerShape(20.dp))
                    .clickable { notesExpanded = !notesExpanded }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text("✏", color = colors.primary, fontSize = 12.sp)
                Text("Notes", color = colors.primary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        // Expandable notes field
        if (notesExpanded) {
            Spacer(modifier = Modifier.height(12.dp))
            BasicTextField(
                value = noteText,
                onValueChange = { noteText = it },
                textStyle = TextStyle(color = colors.onSurface, fontSize = 14.sp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFC7D2FE), RoundedCornerShape(12.dp))
                    .padding(12.dp),
                decorationBox = { inner ->
                    if (noteText.isEmpty()) {
                        Text("Add a quick note...", color = Color(0xFF9CA3AF), fontSize = 14.sp)
                    }
                    inner()
                }
            )
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.primary)
                    .clickable {
                        onSaveNote(noteText)
                        notesExpanded = false
                        noteText = ""
                    }
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Text("Save", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// UPCOMING ROW — gray circle, normal text (shared unified layout)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun UpcomingRow(title: String, time: String, onClick: () -> Unit) {
    HomeTaskRow(
        title = title,
        time = time,
        iconStyle = HomeIconStyle.Upcoming,
        onClick = onClick
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// LATER ROW — gray circle, muted text (shared unified layout)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun LaterRow(title: String, time: String) {
    HomeTaskRow(
        title = title,
        time = time,
        iconStyle = HomeIconStyle.Upcoming,
        onClick = null
    )
}
