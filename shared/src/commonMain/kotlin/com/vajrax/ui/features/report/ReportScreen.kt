package com.vajrax.ui.features.report

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.vajrax.domain.repository.GoalTargetType
import com.vajrax.resources.*
import com.vajrax.ui.designsystem.*
import com.vajrax.ui.theme.LuminaTheme
import com.vajrax.ui.theme.VxSpace
import com.vajrax.ui.theme.habitAccent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun ReportScreen(
    state: ReportUiState,
    effects: Flow<ReportEffect>,
    onIntent: (ReportIntent) -> Unit,
    onOpenDiscover: () -> Unit
) {
    val colors = LuminaTheme.colors
    val snackbar = LocalVxSnackbar.current
    val scope = rememberCoroutineScope()
    var breakdown by remember { mutableStateOf(false) }
    var why by remember { mutableStateOf(false) }
    var reflectionOpen by remember { mutableStateOf(false) }
    var goalOpen by remember { mutableStateOf(false) }
    var deleteGoalId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(effects) {
        effects.collect { e -> if (e is ReportEffect.ShowMessage) scope.launch { snackbar.showSnackbar(e.message) } }
    }
    LaunchedEffect(state.openReflection, state.isLoading, state.period) {
        if (state.openReflection && !state.isLoading && state.period == ReportPeriod.WEEK) {
            reflectionOpen = true
            onIntent(ReportIntent.ReflectionOpened)
        }
    }

    Column(Modifier.fillMaxSize().background(colors.background).statusBarsPadding()) {
        Spacer(Modifier.height(VxSpace.lg))
        Row(Modifier.fillMaxWidth().padding(horizontal = VxSpace.gutter), verticalAlignment = Alignment.CenterVertically) {
            ScreenTitle(stringResource(Res.string.report_report), Modifier.weight(1f))
            SegmentedToggle(
                listOf(stringResource(Res.string.report_this_week), stringResource(Res.string.report_this_month)),
                state.period.ordinal,
                onSelect = { onIntent(ReportIntent.SetPeriod(ReportPeriod.entries[it])) }
            )
        }
        Spacer(Modifier.height(VxSpace.lg))
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = VxSpace.lg)) {
            when {
                state.isLoading -> LoadingSkeleton()
                !state.hasData -> EmptyState(
                    VxIcons.Chart, stringResource(Res.string.report_your_report_builds_as_you),
                    stringResource(Res.string.report_check_in_to_see_your),
                    actionLabel = stringResource(Res.string.today_choose_a_template), onAction = onOpenDiscover
                )
                else -> ReportContent(
                    state,
                    onBreakdown = { breakdown = true },
                    onWhy = { why = true },
                    onReflection = { reflectionOpen = true },
                    onAddGoal = { goalOpen = true },
                    onDeleteGoal = { deleteGoalId = it }
                )
            }
            Spacer(Modifier.height(VxSpace.navClearance))
        }
    }

    deleteGoalId?.let { id ->
        ConfirmDialog(
            title = stringResource(Res.string.report_remove_this_goal),
            message = stringResource(Res.string.report_your_habits_and_their_history),
            confirmLabel = stringResource(Res.string.common_remove),
            destructive = true,
            onConfirm = {
                onIntent(ReportIntent.DeleteGoal(id))
                deleteGoalId = null
            },
            onDismiss = { deleteGoalId = null }
        )
    }
    if (breakdown) BreakdownSheet(state, onDismiss = { breakdown = false })
    if (why && state.insight != null) WhySheet(state.insight, onDismiss = { why = false })
    if (reflectionOpen) ReflectionSheet(state, onDismiss = { reflectionOpen = false }, onSave = { a, o, n ->
        onIntent(ReportIntent.SaveReflection(a, o, n))
        reflectionOpen = false
    })
    if (goalOpen) GoalSheet(state, onDismiss = { goalOpen = false }, onSave = { title, type, target, habitIds ->
        onIntent(ReportIntent.SaveGoal(null, title, type, target, null, habitIds))
        goalOpen = false
    })
}

@Composable
private fun ReportContent(
    state: ReportUiState,
    onBreakdown: () -> Unit,
    onWhy: () -> Unit,
    onReflection: () -> Unit,
    onAddGoal: () -> Unit,
    onDeleteGoal: (String) -> Unit
) {
    val colors = LuminaTheme.colors
    val periodWord = if (state.period == ReportPeriod.WEEK) "week" else "month"

    // Overall completion
    VxCard(elevated = true, onClick = onBreakdown, contentPadding = PaddingValues(VxSpace.xl)) {
        Row(verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text(stringResource(Res.string.report_overall_completion), style = MaterialTheme.typography.titleLarge, color = colors.onSurface)
                Text(state.headline, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            }
            DeltaChip(state.rateDelta)
        }
        Spacer(Modifier.height(VxSpace.lg))
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            ProgressRing(
                fraction = state.overall.rateFraction,
                size = 116.dp,
                stroke = 10.dp,
                modifier = Modifier.semantics(mergeDescendants = true) {
                    contentDescription = "Completion ${state.overall.rate ?: 0} percent: ${state.overall.completed} of ${state.overall.eligible} scheduled habits done"
                }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(state.overall.rate?.let { "$it%" } ?: "—", style = MaterialTheme.typography.headlineSmall, color = colors.onSurface)
                    Text(
                        stringResource(Res.string.report_done_word),
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.onSurfaceVariant
                    )
                }
            }
        }
        Spacer(Modifier.height(VxSpace.lg))
        HorizontalDivider(color = colors.outlineVariant.copy(alpha = 0.7f))
        Spacer(Modifier.height(VxSpace.md))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(VxIcons.Info, null, tint = colors.primary, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(VxSpace.sm))
            val d = state.completedDelta
            Text(
                buildAnnotatedString {
                    when {
                        state.previous.eligible == 0 -> append(stringResource(Res.string.report_how_calculated_hint))
                        d > 0 -> {
                            append(stringResource(Res.string.report_you_achieved))
                            append(" ")
                            withStyle(SpanStyle(color = colors.primary, fontWeight = FontWeight.Bold)) {
                                append(pluralStringResource(Res.plurals.report_more_check_ins, d, d))
                            }
                            append(" ")
                            append(stringResource(Res.string.report_compared_to_last_fmt, periodWord))
                        }
                        d < 0 -> {
                            append(stringResource(Res.string.report_fewer_than_last_fmt, -d, periodWord))
                            append(" ")
                            withStyle(SpanStyle(color = colors.primary, fontWeight = FontWeight.Bold)) {
                                append(stringResource(Res.string.report_every_check_in_counts))
                            }
                            append(".")
                        }
                        else -> append(stringResource(Res.string.report_same_as_last_fmt, periodWord))
                    }
                },
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant
            )
        }
    }
    Spacer(Modifier.height(VxSpace.lg))

    // Bars
    VxCard(elevated = true, contentPadding = PaddingValues(VxSpace.xl)) {
        Text(state.barsTitle, style = MaterialTheme.typography.titleLarge, color = colors.onSurface)
        Text(
            if (state.period == ReportPeriod.WEEK) stringResource(Res.string.report_done_vs_planned_by_day) else stringResource(Res.string.report_done_vs_planned_by_week),
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurfaceVariant
        )
        Spacer(Modifier.height(VxSpace.lg))
        BarChart(state.bars)
    }
    Spacer(Modifier.height(VxSpace.lg))

    // Streaks
    Row(horizontalArrangement = Arrangement.spacedBy(VxSpace.md)) {
        StreakCard(stringResource(Res.string.report_current_streak), state.currentStreak, VxIcons.Flame, stringResource(Res.string.report_days_with_done_fmt, com.vajrax.domain.BusinessRules.STREAK_DAY_PERCENT), Modifier.weight(1f))
        StreakCard(stringResource(Res.string.report_best_streak), state.bestStreak, VxIcons.Award, state.bestStreakEnd?.let { "Achieved $it" } ?: stringResource(Res.string.report_your_record_so_far), Modifier.weight(1f))
    }
    Spacer(Modifier.height(VxSpace.lg))

    if (state.mostConsistent.isNotEmpty()) {
        VxCard {
            SectionLabel(stringResource(Res.string.report_most_consistent))
            Spacer(Modifier.height(VxSpace.md))
            state.mostConsistent.forEach { row -> HabitRateRow(row) }
        }
        Spacer(Modifier.height(VxSpace.lg))
    }
    if (state.needsAttention.isNotEmpty()) {
        VxCard {
            SectionLabel(stringResource(Res.string.report_needs_attention))
            Spacer(Modifier.height(VxSpace.md))
            state.needsAttention.forEach { row -> HabitRateRow(row, showMissed = true) }
        }
        Spacer(Modifier.height(VxSpace.lg))
    }
    if (state.areas.isNotEmpty()) {
        VxCard {
            SectionLabel(stringResource(Res.string.report_your_areas))
            Spacer(Modifier.height(VxSpace.md))
            state.areas.forEach { area ->
                Column(Modifier.padding(vertical = 6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(area.category, style = MaterialTheme.typography.titleSmall, color = colors.onSurface, modifier = Modifier.weight(1f))
                        if (area.tag != null) {
                            Text(
                                area.tag,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (area.tag == stringResource(Res.string.report_strongest)) colors.onSuccessContainer else colors.onWarningContainer,
                                modifier = Modifier.clip(RoundedCornerShape(50)).background(if (area.tag == stringResource(Res.string.report_strongest)) colors.successContainer else colors.warningContainer)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                            Spacer(Modifier.width(VxSpace.sm))
                        }
                        Text(stringResource(Res.string.common_value_fmt_2, area.stats.rate ?: 0), style = MaterialTheme.typography.labelLarge, color = colors.onSurface)
                    }
                    Spacer(Modifier.height(4.dp))
                    LinearBar(area.stats.rateFraction, height = 6.dp)
                }
            }
        }
        Spacer(Modifier.height(VxSpace.lg))
    }

    VxCard {
        SectionLabel(stringResource(Res.string.report_pattern_discovery))
        Spacer(Modifier.height(VxSpace.sm))
        val insight = state.insight
        if (insight == null) {
            Text(
                stringResource(Res.string.report_appears_after_a_few_weeks),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant
            )
        } else {
            Text(insight.headline, style = MaterialTheme.typography.titleSmall, color = colors.onSurface)
            Spacer(Modifier.height(VxSpace.xs))
            Text(insight.rawData, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            Spacer(Modifier.height(VxSpace.sm))
            TonalAction(stringResource(Res.string.report_why), VxIcons.ArrowRight, onWhy)
        }
    }
    Spacer(Modifier.height(VxSpace.lg))

    VxCard {
        SectionLabel(if (state.period == ReportPeriod.WEEK) stringResource(Res.string.report_weekly_summary) else stringResource(Res.string.report_monthly_summary))
        Spacer(Modifier.height(VxSpace.md))
        Row(horizontalArrangement = Arrangement.spacedBy(VxSpace.sm)) {
            StatPod(stringResource(Res.string.report_habits), stringResource(Res.string.calendar_value_fmt, state.habitsTracked), Modifier.weight(1f))
            StatPod(stringResource(Res.string.report_scheduled), stringResource(Res.string.calendar_value_fmt, state.overall.eligible + state.overall.pending), Modifier.weight(1f))
            StatPod(stringResource(Res.string.report_completed), stringResource(Res.string.calendar_value_fmt, state.overall.completed), Modifier.weight(1f))
        }
        if (state.overall.skipped > 0) {
            Spacer(Modifier.height(VxSpace.sm))
            Text(stringResource(Res.string.report_skipped_not_counted_fmt, state.overall.skipped), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
        }
    }
    Spacer(Modifier.height(VxSpace.lg))

    VxCard(onClick = onReflection) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(VxIcons.Pen, colors.primary, size = 36.dp, iconSize = 18.dp)
            Spacer(Modifier.width(VxSpace.md))
            Column(Modifier.weight(1f)) {
                Text(if (state.period == ReportPeriod.WEEK) stringResource(Res.string.report_weekly_reflection) else stringResource(Res.string.report_monthly_reflection), style = MaterialTheme.typography.titleMedium, color = colors.onSurface)
                Text(
                    state.reflection?.achievements?.takeIf { it.isNotBlank() } ?: stringResource(Res.string.report_look_back_and_adjust),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(VxIcons.ChevronRight, null, tint = colors.onSurfaceVariant, modifier = Modifier.size(18.dp))
        }
    }
    Spacer(Modifier.height(VxSpace.lg))

    VxCard {
        SectionLabel(stringResource(Res.string.report_goals)) {
            TextButton(onClick = onAddGoal) { Text(stringResource(Res.string.report_add_goal)) }
        }
        if (state.goals.isEmpty()) {
            Text(stringResource(Res.string.report_link_habits_to_a_goal), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
        }
        state.goals.forEach { g ->
            Column(Modifier.padding(vertical = VxSpace.sm)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(VxIcons.Target, null, tint = colors.primary, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(VxSpace.sm))
                    Text(g.goal.title, style = MaterialTheme.typography.titleSmall, color = colors.onSurface, modifier = Modifier.weight(1f))
                    IconButton(onClick = { onDeleteGoal(g.goal.id) }, modifier = Modifier.semantics { contentDescription = "Remove goal ${g.goal.title}" }) {
                        Icon(VxIcons.Trash, null, tint = colors.onSurfaceVariant, modifier = Modifier.size(18.dp))
                    }
                }
                LinearBar(g.fraction, height = 6.dp)
                Spacer(Modifier.height(4.dp))
                Text(stringResource(Res.string.today_value_fmt_2, g.label, g.habitNames.joinToString()), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun BarChart(bars: List<Bar>) {
    val colors = LuminaTheme.colors
    val max = (bars.maxOfOrNull { it.total } ?: 0).coerceAtLeast(1)
    val summary = bars.joinToString("; ") { "${it.label}: ${it.done} of ${it.total}" }
    Row(
        Modifier.fillMaxWidth().height(170.dp).semantics { contentDescription = "Completions chart. $summary" },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        bars.forEach { bar ->
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom) {
                Text(
                    if (bar.total > 0) stringResource(Res.string.today_value_fmt, bar.done, bar.total) else "",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                    color = colors.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                val trackHeight = (110f * bar.total / max).coerceAtLeast(if (bar.isFuture) 18f else 10f)
                Box(
                    Modifier.width(24.dp).height(trackHeight.dp).clip(RoundedCornerShape(8.dp))
                        .background(if (bar.isCurrent) colors.primary.copy(alpha = 0.18f) else colors.primaryContainer),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    val fill = if (bar.total > 0) bar.done.toFloat() / bar.total else 0f
                    if (fill > 0f) {
                        Box(Modifier.fillMaxWidth().fillMaxHeight(fill).clip(RoundedCornerShape(8.dp)).background(if (bar.isCurrent) colors.primary else colors.primary.copy(alpha = 0.55f)))
                    }
                }
                Spacer(Modifier.height(VxSpace.sm))
                Box(
                    Modifier.size(26.dp).clip(CircleShape).background(if (bar.isCurrent) colors.primary else androidx.compose.ui.graphics.Color.Transparent),
                    contentAlignment = Alignment.Center
                ) {
                    Text(bar.label, style = MaterialTheme.typography.labelMedium, color = if (bar.isCurrent) colors.onPrimary else colors.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun StreakCard(title: String, days: Int, icon: ImageVector, caption: String, modifier: Modifier) {
    val colors = LuminaTheme.colors
    VxCard(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(title, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium), color = colors.onSurfaceVariant, modifier = Modifier.weight(1f))
            Icon(icon, null, tint = if (icon == VxIcons.Award) colors.statusWarning else colors.primary, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.height(VxSpace.xs))
        Text("$days ${if (days == 1) "day" else "days"}", style = MaterialTheme.typography.headlineSmall, color = colors.onSurface)
        Text(caption, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
    }
}

@Composable
private fun HabitRateRow(row: HabitStatRow, showMissed: Boolean = false) {
    val colors = LuminaTheme.colors
    Column(Modifier.padding(vertical = 6.dp).semantics(mergeDescendants = true) {}) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(VxIcons.forKey(row.habit.icon), null, tint = habitAccent(row.habit.color, colors.isDark), modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(VxSpace.sm))
            Text(row.habit.title, style = MaterialTheme.typography.titleSmall, color = colors.onSurface, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(stringResource(Res.string.common_value_fmt_2, row.stats.rate ?: 0), style = MaterialTheme.typography.labelLarge, color = colors.onSurface)
        }
        Spacer(Modifier.height(4.dp))
        LinearBar(row.stats.rateFraction, height = 6.dp)
        Text(
            stringResource(Res.string.report_of_fmt, row.stats.completed, row.stats.eligible) +
                (if (showMissed && row.stats.missed > 0) stringResource(Res.string.report_missed_fmt, row.stats.missed) else "") +
                (if (row.streak > 1) stringResource(Res.string.report_streak_fmt, row.streak, row.streakUnit.dropLast(1)) else ""),
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BreakdownSheet(state: ReportUiState, onDismiss: () -> Unit) {
    val colors = LuminaTheme.colors
    VxBottomSheet(onDismiss = onDismiss) {
        Column(Modifier.padding(horizontal = VxSpace.xxl).padding(bottom = VxSpace.xxxl)) {
            Text(stringResource(Res.string.report_how_this_is_calculated), style = MaterialTheme.typography.headlineSmall, color = colors.onSurface)
            Spacer(Modifier.height(VxSpace.xs))
            Text(state.periodLabel, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            Spacer(Modifier.height(VxSpace.lg))
            val s = state.overall
            BreakdownLine("Completed", s.completed, "counted")
            BreakdownLine("Not completed (period ended)", s.missed, "counted")
            BreakdownLine("Skipped", s.skipped, "excluded")
            BreakdownLine("Still open today", s.pending, "not counted yet")
            HorizontalDivider(Modifier.padding(vertical = VxSpace.md), color = colors.outlineVariant)
            Text(
                if (s.eligible > 0) stringResource(Res.string.report_completed_scheduled_fmt, s.completed, s.eligible, s.rate ?: 0) else stringResource(Res.string.report_nothing_has_been_due_yet),
                style = MaterialTheme.typography.titleSmall,
                color = colors.onSurface
            )
            Spacer(Modifier.height(VxSpace.sm))
            Text(
                stringResource(Res.string.report_rest_and_future_days_aren) + (state.previous.rate?.let { " Previous ${if (state.period == ReportPeriod.WEEK) "week" else "month"}: $it%." } ?: ""),
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun BreakdownLine(label: String, value: Int, note: String) {
    val colors = LuminaTheme.colors
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = colors.onSurface, modifier = Modifier.weight(1f))
        Text(note, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
        Spacer(Modifier.width(VxSpace.md))
        Text(stringResource(Res.string.calendar_value_fmt, value), style = MaterialTheme.typography.titleSmall, color = colors.onSurface, modifier = Modifier.widthIn(min = 32.dp), textAlign = TextAlign.End)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WhySheet(insight: PatternInsight, onDismiss: () -> Unit) {
    val colors = LuminaTheme.colors
    VxBottomSheet(onDismiss = onDismiss) {
        Column(Modifier.padding(horizontal = VxSpace.xxl).padding(bottom = VxSpace.xxxl)) {
            Text(stringResource(Res.string.report_why_this_pattern), style = MaterialTheme.typography.headlineSmall, color = colors.onSurface)
            Spacer(Modifier.height(VxSpace.xs))
            Text(stringResource(Res.string.report_last_30_days_by_scheduled), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            Spacer(Modifier.height(VxSpace.lg))
            insight.buckets.forEach { b ->
                Column(Modifier.padding(vertical = 6.dp)) {
                    Row {
                        Text(stringResource(Res.string.today_value_fmt_2, b.label, b.window), style = MaterialTheme.typography.titleSmall, color = colors.onSurface, modifier = Modifier.weight(1f))
                        Text(if (b.stats.eligible > 0) stringResource(Res.string.common_value_fmt_2, b.stats.rate ?: 0) else "—", style = MaterialTheme.typography.labelLarge, color = colors.onSurface)
                    }
                    Spacer(Modifier.height(4.dp))
                    LinearBar(b.stats.rateFraction, height = 6.dp)
                    Text(stringResource(Res.string.report_of_completed_fmt, b.stats.completed, b.stats.eligible), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(VxSpace.md))
            Text(
                stringResource(Res.string.report_try_moving_a_habit_to),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurface
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReflectionSheet(state: ReportUiState, onDismiss: () -> Unit, onSave: (String, String, String) -> Unit) {
    val colors = LuminaTheme.colors
    var a by remember { mutableStateOf(state.reflection?.achievements ?: "") }
    var o by remember { mutableStateOf(state.reflection?.obstacles ?: "") }
    var n by remember { mutableStateOf(state.reflection?.nextActions ?: "") }
    val edited = a != (state.reflection?.achievements ?: "") || o != (state.reflection?.obstacles ?: "") || n != (state.reflection?.nextActions ?: "")
    VxBottomSheet(onDismiss = onDismiss, hasUnsavedChanges = edited) {
        Column(Modifier.verticalScroll(rememberScrollState()).padding(horizontal = VxSpace.xxl).padding(bottom = VxSpace.xxxl)) {
            Text(if (state.period == ReportPeriod.WEEK) stringResource(Res.string.report_weekly_reflection) else stringResource(Res.string.report_monthly_reflection), style = MaterialTheme.typography.headlineSmall, color = colors.onSurface)
            Text(state.periodLabel, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            Spacer(Modifier.height(VxSpace.lg))
            VxTextField(stringResource(Res.string.report_what_went_well), a, { a = it }, singleLine = false, minLines = 2, maxChars = 1000)
            Spacer(Modifier.height(VxSpace.md))
            VxTextField(stringResource(Res.string.report_what_got_in_the_way), o, { o = it }, singleLine = false, minLines = 2, maxChars = 1000)
            Spacer(Modifier.height(VxSpace.md))
            VxTextField(stringResource(Res.string.report_what_will_you_adjust_next), n, { n = it }, singleLine = false, minLines = 2, maxChars = 1000)
            Spacer(Modifier.height(VxSpace.xl))
            PrimaryButton(stringResource(Res.string.report_save_reflection), { onSave(a, o, n) }, Modifier.fillMaxWidth(), enabled = a.isNotBlank() || o.isNotBlank() || n.isNotBlank())
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun GoalSheet(state: ReportUiState, onDismiss: () -> Unit, onSave: (String, GoalTargetType, Double, List<String>) -> Unit) {
    val colors = LuminaTheme.colors
    var title by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(GoalTargetType.COMPLETIONS) }
    var target by remember { mutableStateOf("20") }
    var selected by remember { mutableStateOf(setOf<String>()) }
    val edited = title.isNotBlank() || selected.isNotEmpty() || target != (if (type == GoalTargetType.RATE) "80" else "20")
    VxBottomSheet(onDismiss = onDismiss, hasUnsavedChanges = edited) {
        Column(Modifier.verticalScroll(rememberScrollState()).padding(horizontal = VxSpace.xxl).padding(bottom = VxSpace.xxxl)) {
            Text(stringResource(Res.string.report_new_goal), style = MaterialTheme.typography.headlineSmall, color = colors.onSurface)
            Spacer(Modifier.height(VxSpace.lg))
            VxTextField(stringResource(Res.string.report_goal), title, { title = it }, placeholder = stringResource(Res.string.report_e_g_run_a_5k), maxChars = 60)
            Spacer(Modifier.height(VxSpace.lg))
            SegmentedToggle(listOf(stringResource(Res.string.report_completions), stringResource(Res.string.report_completion_rate)), type.ordinal, onSelect = {
                type = GoalTargetType.entries[it]
                target = if (type == GoalTargetType.RATE) "80" else "20"
            })
            Spacer(Modifier.height(VxSpace.md))
            VxTextField(
                if (type == GoalTargetType.RATE) stringResource(Res.string.report_target_rate) else stringResource(Res.string.report_target_number_of_completions),
                target,
                { target = it.filter { c -> c.isDigit() }.take(4) },
                keyboardType = KeyboardType.Number
            )
            Spacer(Modifier.height(VxSpace.lg))
            Text(stringResource(Res.string.report_supporting_habits), style = MaterialTheme.typography.labelLarge, color = colors.onSurface)
            Spacer(Modifier.height(VxSpace.sm))
            if (state.activeHabits.isEmpty()) {
                Text(stringResource(Res.string.report_start_a_routine_first_to), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(VxSpace.sm), verticalArrangement = Arrangement.spacedBy(VxSpace.sm)) {
                state.activeHabits.forEach { h ->
                    CategoryChip(h.title, h.id in selected, onClick = { selected = if (h.id in selected) selected - h.id else selected + h.id })
                }
            }
            Spacer(Modifier.height(VxSpace.xl))
            PrimaryButton(
                stringResource(Res.string.report_save_goal),
                { onSave(title.trim(), type, target.toDoubleOrNull() ?: 0.0, selected.toList()) },
                Modifier.fillMaxWidth(),
                enabled = title.isNotBlank() && (target.toDoubleOrNull() ?: 0.0) > 0 && selected.isNotEmpty()
            )
        }
    }
}
