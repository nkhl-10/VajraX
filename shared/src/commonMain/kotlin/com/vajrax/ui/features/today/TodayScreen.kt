package com.vajrax.ui.features.today

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vajrax.domain.habit.HabitType
import com.vajrax.domain.model.TrackingMode
import com.vajrax.ui.designsystem.*
import com.vajrax.ui.theme.LuminaTheme
import com.vajrax.ui.theme.VxSpace
import com.vajrax.ui.theme.accentOnContainer
import com.vajrax.ui.theme.habitAccent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@Composable
fun TodayScreen(
    state: TodayUiState,
    effects: Flow<TodayEffect>,
    onIntent: (TodayIntent) -> Unit,
    onOpenDiscover: () -> Unit,
    onOpenRoutine: () -> Unit,
    onOpenHabit: (String) -> Unit,
    onOpenReport: () -> Unit,
    onWeeklyReview: () -> Unit
) {
    val colors = LuminaTheme.colors
    val snackbar = LocalVxSnackbar.current
    val listState = rememberLazyListState()
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    var sheetFor by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(effects) {
        effects.collect { effect ->
            when (effect) {
                is TodayEffect.ShowMessage -> scope.launch {
                    snackbar.currentSnackbarData?.dismiss()
                    val result = snackbar.showSnackbar(
                        message = effect.message,
                        actionLabel = if (effect.undo != null) "Undo" else null,
                        duration = SnackbarDuration.Short
                    )
                    if (result == SnackbarResult.ActionPerformed && effect.undo != null) {
                        onIntent(TodayIntent.Undo(effect.undo))
                    }
                }
            }
        }
    }

    val complete: (TodayItem) -> Unit = { item ->
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        onIntent(TodayIntent.Toggle(item.id))
    }

    // Keep NOW in view: on open and whenever focus moves to the next habit, scroll to it if hidden.
    val prefix = 3 + (if (state.weeklyReviewDue) 1 else 0) + (if (state.dayWrap != null) 1 else 0)
    LaunchedEffect(state.nowId, state.isLoading) {
        val nowIndex = state.items.indexOfFirst { it.id == state.nowId }
        if (state.isLoading || nowIndex < 0) return@LaunchedEffect
        val target = prefix + nowIndex
        val visible = listState.layoutInfo.visibleItemsInfo
        val fullyVisible = visible.any { it.index == target && it.offset >= 0 && it.offset + it.size <= listState.layoutInfo.viewportEndOffset - 250 }
        if (!fullyVisible) listState.animateScrollToItem((target - 1).coerceAtLeast(0))
    }

    Box(Modifier.fillMaxSize().background(colors.background)) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = VxSpace.gutter, end = VxSpace.gutter, bottom = VxSpace.navClearance)
        ) {
            item(key = "top") {
                Spacer(Modifier.statusBarsPadding().height(VxSpace.xl))
            }
            if (state.isLoading) {
                item { LoadingSkeleton() }
                return@LazyColumn
            }
            item(key = "dashboard") {
                DashboardCard(state)
                Spacer(Modifier.height(VxSpace.xxl))
            }
            if (!state.hasTracker) {
                item {
                    EmptyState(
                        icon = VxIcons.Compass,
                        title = "No routine yet",
                        message = "Pick a ready-made template or build your own tracker.",
                        actionLabel = "Choose a template",
                        onAction = onOpenDiscover
                    )
                }
                return@LazyColumn
            }
            if (state.weeklyReviewDue) {
                item(key = "weekly_review") {
                    PromptCard(
                        icon = VxIcons.Pen,
                        title = "Weekly review",
                        body = "Two minutes to look back: what worked, what got in the way, what to adjust.",
                        action = "Start review",
                        onAction = onWeeklyReview
                    )
                    Spacer(Modifier.height(VxSpace.lg))
                }
            }
            state.dayWrap?.let { wrap ->
                item(key = "day_wrap") {
                    PromptCard(
                        icon = if (wrap.allDone) VxIcons.Award else VxIcons.Moon,
                        title = if (wrap.allDone) "Day complete" else "Wrapping up the day",
                        body = buildString {
                            append("${wrap.done} of ${wrap.total} done")
                            if (wrap.skipped > 0) append(" · ${wrap.skipped} skipped")
                            append(if (wrap.allDone) ". Every commitment kept." else ". Anything left can still be checked in before midnight.")
                        },
                        action = "See progress",
                        onAction = onOpenReport
                    )
                    Spacer(Modifier.height(VxSpace.lg))
                }
            }
            item(key = "header") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(state.trackerName, style = MaterialTheme.typography.titleLarge, color = colors.onSurface)
                        Text("Complete daily routine", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                    }
                    RoundIconButton(VxIcons.Pencil, "Edit routine", onClick = onOpenRoutine)
                }
                Spacer(Modifier.height(VxSpace.lg))
            }
            if (state.items.isEmpty()) {
                item {
                    EmptyState(
                        icon = VxIcons.Moon,
                        title = "Rest day",
                        message = "Nothing is scheduled today. Rest days never count as missed.",
                        actionLabel = "Edit routine",
                        onAction = onOpenRoutine
                    )
                }
            }
            items(state.items, key = { it.id }) { item ->
                Box(Modifier.animateItem()) {
                    if (item.phase == TodayPhase.NOW) {
                        NowCard(
                            item = item,
                            onDone = {
                                when {
                                    item.habit.type == HabitType.COUNT -> onIntent(TodayIntent.Increment(item.id))
                                    item.habit.trackingMode == TrackingMode.TIMER -> onIntent(TodayIntent.StartTimer(item.id))
                                    item.habit.type != HabitType.BOOLEAN -> sheetFor = item.id
                                    else -> complete(item)
                                }
                            },
                            onNotes = { sheetFor = item.id },
                            onSnooze = { onIntent(TodayIntent.Snooze(item.id)) },
                            onSkip = { onIntent(TodayIntent.Skip(item.id, null)) },
                            onCheck = { complete(item) }
                        )
                    } else {
                        HabitRow(item, onCheck = { complete(item) }, onOpen = { sheetFor = item.id })
                    }
                }
                Spacer(Modifier.height(VxSpace.sm))
            }
        }

        state.timer?.let { timer ->
            FocusTimerOverlay(
                timer = timer,
                onPause = { onIntent(TodayIntent.PauseTimer) },
                onResume = { onIntent(TodayIntent.ResumeTimer) },
                onFinish = { onIntent(TodayIntent.FinishTimer) },
                onCancel = { onIntent(TodayIntent.CancelTimer) }
            )
        }
    }

    sheetFor?.let { id ->
        val item = state.items.firstOrNull { it.id == id }
        if (item == null) {
            LaunchedEffect(id) { sheetFor = null }
        } else {
            HabitActionSheet(
                item = item,
                onDismiss = { sheetFor = null },
                onIntent = { onIntent(it) },
                onOpenDetails = {
                    sheetFor = null
                    onOpenHabit(item.habit.id)
                }
            )
        }
    }
}

@Composable
private fun DashboardCard(state: TodayUiState) {
    val colors = LuminaTheme.colors
    VxCard(elevated = true, contentPadding = PaddingValues(VxSpace.xl)) {
        Text(state.dateLabel, style = MaterialTheme.typography.labelSmall, color = colors.primary)
        Spacer(Modifier.height(VxSpace.xs))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(state.greeting, style = MaterialTheme.typography.headlineMedium, color = colors.onSurface)
                Spacer(Modifier.height(VxSpace.md))
                Text("Task Completion", style = MaterialTheme.typography.titleMedium, color = colors.onSurface)
                Spacer(Modifier.height(2.dp))
                Text(state.statusLine, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            }
            Spacer(Modifier.width(VxSpace.md))
            ProgressRing(
                fraction = state.todayFraction,
                size = 72.dp,
                stroke = 7.dp,
                modifier = Modifier.semantics(mergeDescendants = true) {
                    contentDescription = "${state.doneCount} of ${state.totalCount} habits done today, ${state.todayPercent} percent"
                }
            ) {
                Text("${state.todayPercent}%", style = MaterialTheme.typography.labelLarge, color = colors.onSurface)
            }
        }
        Spacer(Modifier.height(VxSpace.xl))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatPod("Today", "${state.doneCount}/${state.totalCount}", Modifier.weight(1f))
            StatPod("Week", state.weekRate?.let { "$it%" } ?: "—", Modifier.weight(1f))
            StatPod("Consistency", state.consistencyRate?.let { "$it%" } ?: "—", Modifier.weight(1f))
        }
    }
}

@Composable
private fun PromptCard(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, body: String, action: String, onAction: () -> Unit) {
    val colors = LuminaTheme.colors
    VxCard(onClick = onAction) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(icon, colors.primary, size = 40.dp)
            Spacer(Modifier.width(VxSpace.md))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = colors.onSurface)
                Text(body, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            }
        }
        Spacer(Modifier.height(VxSpace.md))
        TonalAction(action, VxIcons.ArrowRight, onAction)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun NowCard(
    item: TodayItem,
    onDone: () -> Unit,
    onNotes: () -> Unit,
    onSnooze: () -> Unit,
    onSkip: () -> Unit,
    onCheck: () -> Unit
) {
    val colors = LuminaTheme.colors
    val shape = RoundedCornerShape(16.dp)
    Column(
        Modifier.fillMaxWidth().clip(shape).background(colors.primaryContainer)
            .border(1.dp, colors.primary.copy(alpha = 0.3f), shape)
            .padding(start = VxSpace.xs, end = VxSpace.lg, top = VxSpace.xs, bottom = VxSpace.lg)
            .animateContentSize()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CheckCircle(CheckState.ACTIVE, onClick = onCheck, label = "${item.habit.title}, now")
            Spacer(Modifier.width(VxSpace.xs))
            Column(Modifier.weight(1f)) {
                Text(
                    item.habit.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = colors.accentOnContainer,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    item.progressLabel ?: item.habit.subtitle(),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
            Text(item.timeLabel, style = MaterialTheme.typography.labelLarge, color = colors.accentOnContainer)
        }
        if (item.progressFraction != null) {
            LinearBar(item.progressFraction, Modifier.padding(start = 52.dp, top = VxSpace.xs), height = 6.dp)
        }
        Spacer(Modifier.height(VxSpace.md))
        FlowRow(
            Modifier.padding(start = 52.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val (label, icon) = when {
                item.habit.type == HabitType.COUNT -> "+1" to VxIcons.Plus
                item.habit.trackingMode == TrackingMode.TIMER -> "Start" to VxIcons.Play
                item.habit.type != HabitType.BOOLEAN -> "Log" to VxIcons.Pencil
                else -> "Mark done" to VxIcons.Check
            }
            PillAction(label, icon, onDone, filled = true)
            PillAction("Snooze", VxIcons.Alarm, onSnooze)
            PillAction("Skip", VxIcons.SkipForward, onSkip)
            PillAction("Notes", VxIcons.Pencil, onNotes)
        }
    }
}

@Composable
private fun HabitRow(item: TodayItem, onCheck: () -> Unit, onOpen: () -> Unit) {
    val colors = LuminaTheme.colors
    val state = when (item.phase) {
        TodayPhase.DONE -> CheckState.DONE
        TodayPhase.SKIPPED -> CheckState.SKIPPED
        TodayPhase.WEEKLY_MET -> CheckState.DONE_MUTED
        else -> CheckState.OPEN
    }
    val secondary = when (item.phase) {
        TodayPhase.SKIPPED -> "Skipped" + (item.occurrence.skipReason?.let { " · $it" } ?: "")
        TodayPhase.WEEKLY_MET -> "Weekly target met"
        TodayPhase.OPEN_EARLIER -> item.progressLabel ?: "Still open — there's time"
        else -> item.progressLabel
    }
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
            .clickable(role = Role.Button, onClick = onOpen)
            .padding(end = VxSpace.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CheckCircle(state, onClick = onCheck, label = item.habit.title)
        Spacer(Modifier.width(VxSpace.xs))
        Icon(
            VxIcons.forKey(item.habit.icon), null,
            tint = habitAccent(item.habit.color, colors.isDark).copy(alpha = if (state == CheckState.OPEN) 1f else 0.6f),
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(VxSpace.sm))
        Column(Modifier.weight(1f).padding(vertical = VxSpace.sm)) {
            Text(
                item.habit.title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = if (item.phase == TodayPhase.SKIPPED) colors.onSurfaceVariant else colors.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (secondary != null) {
                Text(secondary, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            }
        }
        Spacer(Modifier.width(VxSpace.sm))
        Text(item.timeLabel, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
    }
}
