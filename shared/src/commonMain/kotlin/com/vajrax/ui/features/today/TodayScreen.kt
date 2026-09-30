package com.vajrax.ui.features.today

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
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

    BoxWithConstraints(Modifier.fillMaxSize().background(colors.background)) {
        // On short (e.g. landscape) screens a pinned dashboard would leave no room for tasks,
        // so it scrolls with the list there instead.
        val pinDashboard = maxHeight >= 560.dp
        val density = LocalDensity.current
        var listHeightPx by remember { mutableStateOf(0) }
        val navClearancePx = with(density) { VxSpace.navClearance.toPx() }
        // Enough bottom room that the last habit can still sit in the middle of the visible area.
        val bottomPad = maxOf(VxSpace.navClearance, with(density) { (listHeightPx * 0.5f).toDp() } - 24.dp)

        val listKeys = buildList {
            if (!pinDashboard) add("dashboard")
            if (state.hasTracker) {
                add("header")
                if (state.items.isEmpty()) add(if (state.startsLabel != null) "starts_later" else "rest_day")
                state.items.forEach { add(it.id) }
            } else add("no_routine")
        }

        // Keep the active habit in the centre of the visible list area (between the dashboard and
        // the nav bar): on open and whenever focus moves on to the next habit.
        LaunchedEffect(state.nowId, state.isLoading, listHeightPx) {
            val nowId = state.nowId ?: return@LaunchedEffect
            if (state.isLoading || listHeightPx == 0) return@LaunchedEffect
            val index = listKeys.indexOf(nowId).takeIf { it >= 0 } ?: return@LaunchedEffect
            fun centreDelta(): Float? {
                val info = listState.layoutInfo
                val item = info.visibleItemsInfo.firstOrNull { it.key == nowId } ?: return null
                val visibleCentre = (info.viewportSize.height - navClearancePx) / 2f
                return item.offset + item.size / 2f - visibleCentre
            }
            var delta = centreDelta()
            if (delta == null) {
                listState.scrollToItem(index)
                delta = centreDelta()
            }
            if (delta != null && kotlin.math.abs(delta) > 8f) listState.animateScrollBy(delta)
        }

        Column(Modifier.fillMaxSize()) {
            // Opaque status-bar area: scrolled content never shows under the clock and icons.
            Spacer(Modifier.fillMaxWidth().windowInsetsTopHeight(WindowInsets.statusBars).background(colors.background))
            if (pinDashboard && !state.isLoading) {
                Box(Modifier.padding(start = VxSpace.gutter, end = VxSpace.gutter, top = VxSpace.lg, bottom = VxSpace.md)) {
                    DashboardCard(state, onWeeklyReview, onOpenReport)
                }
            }
            Box(
                Modifier.weight(1f).fillMaxWidth().clipToBounds()
                    .onSizeChanged { listHeightPx = it.height }
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = VxSpace.gutter,
                        end = VxSpace.gutter,
                        top = if (pinDashboard) VxSpace.sm else VxSpace.lg,
                        bottom = bottomPad
                    )
                ) {
                    if (state.isLoading) {
                        item { LoadingSkeleton() }
                        return@LazyColumn
                    }
                    if (!pinDashboard) {
                        item(key = "dashboard") {
                            DashboardCard(state, onWeeklyReview, onOpenReport)
                            Spacer(Modifier.height(VxSpace.xl))
                        }
                    }
                    if (!state.hasTracker) {
                        item(key = "no_routine") {
                            EmptyState(
                                icon = VxIcons.Compass,
                                title = "No routine yet",
                                message = "Pick a template to start.",
                                actionLabel = "Choose a template",
                                onAction = onOpenDiscover
                            )
                        }
                        return@LazyColumn
                    }
                    item(key = "header") {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(state.trackerName, style = MaterialTheme.typography.titleLarge, color = colors.onSurface)
                                val left = state.totalCount - state.doneCount
                                if (state.totalCount > 0) {
                                    Text(if (left > 0) "$left left" else "All done", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                                }
                            }
                            RoundIconButton(VxIcons.Pencil, "Edit routine", onClick = onOpenRoutine)
                        }
                        Spacer(Modifier.height(VxSpace.md))
                    }
                    if (state.items.isEmpty() && state.startsLabel != null) {
                        item(key = "starts_later") {
                            EmptyState(
                                icon = VxIcons.Sunrise,
                                title = "Starts ${state.startsLabel}",
                                message = state.firstUp?.let { "First up: $it" } ?: "Your routine is ready.",
                                actionLabel = "Start today",
                                onAction = { onIntent(TodayIntent.StartToday) }
                            )
                        }
                    } else if (state.items.isEmpty()) {
                        item(key = "rest_day") {
                            EmptyState(
                                icon = VxIcons.Moon,
                                title = "Rest day",
                                message = "Nothing scheduled today.",
                                actionLabel = "Edit routine",
                                onAction = onOpenRoutine
                            )
                        }
                    }
                    items(state.items, key = { it.id }) { item ->
                        Column(Modifier.animateItem()) {
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
                            Spacer(Modifier.height(VxSpace.sm))
                        }
                    }
                }
                // Soft edge under the pinned dashboard once the list is scrolled.
                if (pinDashboard && listState.canScrollBackward) {
                    Box(
                        Modifier.fillMaxWidth().height(14.dp)
                            .background(Brush.verticalGradient(listOf(colors.background, colors.background.copy(alpha = 0f))))
                    )
                }
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
private fun DashboardCard(state: TodayUiState, onWeeklyReview: () -> Unit, onOpenReport: () -> Unit) {
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
        // End-of-day and end-of-week prompts live in the pinned card, so auto-centring the NOW habit
        // can never scroll them out of sight.
        val wrap = state.dayWrap
        when {
            state.weeklyReviewDue -> PromptStrip(VxIcons.Pen, "Weekly review", "Look back on your week", onWeeklyReview)
            wrap != null -> PromptStrip(
                icon = if (wrap.allDone) VxIcons.Award else VxIcons.Moon,
                title = if (wrap.allDone) "Day complete" else "Wrapping up",
                body = buildString {
                    append("${wrap.done} of ${wrap.total} done")
                    if (wrap.skipped > 0) append(" · ${wrap.skipped} skipped")
                },
                onClick = onOpenReport
            )
        }
    }
}

@Composable
private fun PromptStrip(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, body: String, onClick: () -> Unit) {
    val colors = LuminaTheme.colors
    Spacer(Modifier.height(VxSpace.lg))
    Row(
        Modifier.fillMaxWidth().heightIn(min = 52.dp).clip(RoundedCornerShape(14.dp))
            .background(colors.primaryContainer)
            .clickable(role = Role.Button, onClickLabel = title, onClick = onClick)
            .padding(horizontal = VxSpace.md, vertical = VxSpace.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconBadge(icon, colors.primary, size = 32.dp, iconSize = 16.dp, background = colors.surface)
        Spacer(Modifier.width(VxSpace.md))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = colors.accentOnContainer)
            Text(body, style = MaterialTheme.typography.bodySmall, color = colors.onPrimaryContainer.copy(alpha = 0.8f))
        }
        Icon(VxIcons.ArrowRight, null, tint = colors.accentOnContainer, modifier = Modifier.size(18.dp))
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
        TodayPhase.OPEN_EARLIER -> item.progressLabel ?: "Still open"
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
