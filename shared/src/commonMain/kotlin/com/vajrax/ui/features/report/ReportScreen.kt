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
import com.vajrax.ui.designsystem.*
import com.vajrax.ui.theme.LuminaTheme
import com.vajrax.ui.theme.VxSpace
import com.vajrax.ui.theme.habitAccent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

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
            ScreenTitle("Report", Modifier.weight(1f))
            SegmentedToggle(
                listOf("This Week", "This Month"),
                state.period.ordinal,
                onSelect = { onIntent(ReportIntent.SetPeriod(ReportPeriod.entries[it])) }
            )
        }
        Spacer(Modifier.height(VxSpace.lg))
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = VxSpace.lg)) {
            when {
                state.isLoading -> LoadingSkeleton()
                !state.hasData -> EmptyState(
                    VxIcons.Chart, "Your report builds as you check in",
                    "Complete a few habits and your weekly and monthly progress will appear here — calculated only from what you record.",
                    actionLabel = "Choose a template", onAction = onOpenDiscover
                )
                else -> ReportContent(
                    state,
                    onBreakdown = { breakdown = true },
                    onWhy = { why = true },
                    onReflection = { reflectionOpen = true },
                    onAddGoal = { goalOpen = true },
                    onDeleteGoal = { onIntent(ReportIntent.DeleteGoal(it)) }
                )
            }
            Spacer(Modifier.height(VxSpace.navClearance))
        }
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
                Text("Overall Completion", style = MaterialTheme.typography.titleLarge, color = colors.onSurface)
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
                    Text("SCORE", style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
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
                        state.previous.eligible == 0 -> append("Tap to see exactly how this is calculated.")
                        d > 0 -> {
                            append("You achieved ")
                            withStyle(SpanStyle(color = colors.primary, fontWeight = FontWeight.Bold)) { append("$d more ${if (d == 1) "task" else "tasks"}") }
                            append(" compared to last $periodWord.")
                        }
                        d < 0 -> {
                            append("${-d} fewer than this point last $periodWord — ")
                            withStyle(SpanStyle(color = colors.primary, fontWeight = FontWeight.Bold)) { append("every check-in still counts") }
                            append(".")
                        }
                        else -> append("Same number of completions as last $periodWord so far.")
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
            if (state.period == ReportPeriod.WEEK) "Daily completions vs targeted workload" else "Weekly completions vs targeted workload",
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurfaceVariant
        )
        Spacer(Modifier.height(VxSpace.lg))
        BarChart(state.bars)
    }
    Spacer(Modifier.height(VxSpace.lg))

    // Streaks
    Row(horizontalArrangement = Arrangement.spacedBy(VxSpace.md)) {
        StreakCard("Current Streak", state.currentStreak, VxIcons.Flame, "Days with 80%+ done", Modifier.weight(1f))
        StreakCard("Best Streak", state.bestStreak, VxIcons.Award, state.bestStreakEnd?.let { "Achieved $it" } ?: "Your record so far", Modifier.weight(1f))
    }
    Spacer(Modifier.height(VxSpace.lg))

    if (state.mostConsistent.isNotEmpty()) {
        VxCard {
            SectionLabel("Most consistent")
            Spacer(Modifier.height(VxSpace.md))
            state.mostConsistent.forEach { row -> HabitRateRow(row) }
        }
        Spacer(Modifier.height(VxSpace.lg))
    }
    if (state.needsAttention.isNotEmpty()) {
        VxCard {
            SectionLabel("Needs attention")
            Spacer(Modifier.height(VxSpace.xs))
            Text("Not failures — signals. Try a smaller version or a different time.", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            Spacer(Modifier.height(VxSpace.md))
            state.needsAttention.forEach { row -> HabitRateRow(row, showMissed = true) }
        }
        Spacer(Modifier.height(VxSpace.lg))
    }
    if (state.areas.isNotEmpty()) {
        VxCard {
            SectionLabel("Your areas")
            Text("Which part of your life system is becoming stronger?", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            Spacer(Modifier.height(VxSpace.md))
            state.areas.forEach { area ->
                Column(Modifier.padding(vertical = 6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(area.category, style = MaterialTheme.typography.titleSmall, color = colors.onSurface, modifier = Modifier.weight(1f))
                        if (area.tag != null) {
                            Text(
                                area.tag,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (area.tag == "Strongest") colors.onSuccessContainer else colors.onWarningContainer,
                                modifier = Modifier.clip(RoundedCornerShape(50)).background(if (area.tag == "Strongest") colors.successContainer else colors.warningContainer)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                            Spacer(Modifier.width(VxSpace.sm))
                        }
                        Text("${area.stats.rate ?: 0}%", style = MaterialTheme.typography.labelLarge, color = colors.onSurface)
                    }
                    Spacer(Modifier.height(4.dp))
                    LinearBar(area.stats.rateFraction, height = 6.dp)
                }
            }
        }
        Spacer(Modifier.height(VxSpace.lg))
    }

    VxCard {
        SectionLabel("Pattern discovery")
        Spacer(Modifier.height(VxSpace.sm))
        val insight = state.insight
        if (insight == null) {
            Text(
                "Keep checking in. Once there are a few weeks of records, VAJRAX shows which times of day work best for you — with the raw numbers behind it.",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant
            )
        } else {
            Text(insight.headline, style = MaterialTheme.typography.titleSmall, color = colors.onSurface)
            Spacer(Modifier.height(VxSpace.xs))
            Text(insight.rawData, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            Spacer(Modifier.height(VxSpace.sm))
            TonalAction("Why?", VxIcons.ArrowRight, onWhy)
        }
    }
    Spacer(Modifier.height(VxSpace.lg))

    VxCard {
        SectionLabel(if (state.period == ReportPeriod.WEEK) "Weekly summary" else "Monthly summary")
        Spacer(Modifier.height(VxSpace.md))
        Row(horizontalArrangement = Arrangement.spacedBy(VxSpace.sm)) {
            StatPod("Habits", "${state.habitsTracked}", Modifier.weight(1f))
            StatPod("Scheduled", "${state.overall.eligible + state.overall.pending}", Modifier.weight(1f))
            StatPod("Completed", "${state.overall.completed}", Modifier.weight(1f))
        }
        if (state.overall.skipped > 0) {
            Spacer(Modifier.height(VxSpace.sm))
            Text("${state.overall.skipped} skipped — not counted against you.", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
        }
    }
    Spacer(Modifier.height(VxSpace.lg))

    VxCard(onClick = onReflection) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(VxIcons.Pen, colors.primary, size = 36.dp, iconSize = 18.dp)
            Spacer(Modifier.width(VxSpace.md))
            Column(Modifier.weight(1f)) {
                Text(if (state.period == ReportPeriod.WEEK) "Weekly reflection" else "Monthly reflection", style = MaterialTheme.typography.titleMedium, color = colors.onSurface)
                Text(
                    state.reflection?.achievements?.takeIf { it.isNotBlank() } ?: "What went well, what got in the way, what to adjust.",
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
        SectionLabel("Goals") {
            TextButton(onClick = onAddGoal) { Text("Add goal") }
        }
        if (state.goals.isEmpty()) {
            Text("Link habits to a goal to see how daily actions add up.", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
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
                Text("${g.label} · ${g.habitNames.joinToString()}", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
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
                    if (bar.total > 0) "${bar.done}/${bar.total}" else "",
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
            Text("${row.stats.rate ?: 0}%", style = MaterialTheme.typography.labelLarge, color = colors.onSurface)
        }
        Spacer(Modifier.height(4.dp))
        LinearBar(row.stats.rateFraction, height = 6.dp)
        Text(
            "${row.stats.completed} of ${row.stats.eligible}" +
                (if (showMissed && row.stats.missed > 0) " · ${row.stats.missed} missed" else "") +
                (if (row.streak > 1) " · ${row.streak}-${row.streakUnit.dropLast(1)} streak" else ""),
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BreakdownSheet(state: ReportUiState, onDismiss: () -> Unit) {
    val colors = LuminaTheme.colors
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(true), containerColor = colors.surface) {
        Column(Modifier.padding(horizontal = VxSpace.xxl).padding(bottom = VxSpace.xxxl)) {
            Text("How this is calculated", style = MaterialTheme.typography.headlineSmall, color = colors.onSurface)
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
                if (s.eligible > 0) "${s.completed} completed ÷ ${s.eligible} scheduled = ${s.rate}%" else "Nothing has been due yet in this period.",
                style = MaterialTheme.typography.titleSmall,
                color = colors.onSurface
            )
            Spacer(Modifier.height(VxSpace.sm))
            Text(
                "Rest days and future days are never counted. Weekly-target habits are evaluated when their week ends. " +
                    "The change is compared with the same number of days in the previous ${if (state.period == ReportPeriod.WEEK) "week" else "month"}" +
                    (state.previous.rate?.let { " ($it%)." } ?: "."),
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
        Text("$value", style = MaterialTheme.typography.titleSmall, color = colors.onSurface, modifier = Modifier.widthIn(min = 32.dp), textAlign = TextAlign.End)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WhySheet(insight: PatternInsight, onDismiss: () -> Unit) {
    val colors = LuminaTheme.colors
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(true), containerColor = colors.surface) {
        Column(Modifier.padding(horizontal = VxSpace.xxl).padding(bottom = VxSpace.xxxl)) {
            Text("Why this pattern?", style = MaterialTheme.typography.headlineSmall, color = colors.onSurface)
            Spacer(Modifier.height(VxSpace.xs))
            Text("Completion by the time each habit was scheduled, last 30 days.", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            Spacer(Modifier.height(VxSpace.lg))
            insight.buckets.forEach { b ->
                Column(Modifier.padding(vertical = 6.dp)) {
                    Row {
                        Text("${b.label} · ${b.window}", style = MaterialTheme.typography.titleSmall, color = colors.onSurface, modifier = Modifier.weight(1f))
                        Text(if (b.stats.eligible > 0) "${b.stats.rate}%" else "—", style = MaterialTheme.typography.labelLarge, color = colors.onSurface)
                    }
                    Spacer(Modifier.height(4.dp))
                    LinearBar(b.stats.rateFraction, height = 6.dp)
                    Text("${b.stats.completed} of ${b.stats.eligible} completed", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(VxSpace.md))
            Text(
                "Suggestion: move a habit that often stays open into your strongest time window, or try its minimum version.",
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
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(true), containerColor = colors.surface) {
        Column(Modifier.verticalScroll(rememberScrollState()).padding(horizontal = VxSpace.xxl).padding(bottom = VxSpace.xxxl)) {
            Text(if (state.period == ReportPeriod.WEEK) "Weekly reflection" else "Monthly reflection", style = MaterialTheme.typography.headlineSmall, color = colors.onSurface)
            Text(state.periodLabel, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            Spacer(Modifier.height(VxSpace.lg))
            VxTextField("What went well?", a, { a = it }, singleLine = false, minLines = 2, maxChars = 1000)
            Spacer(Modifier.height(VxSpace.md))
            VxTextField("What got in the way?", o, { o = it }, singleLine = false, minLines = 2, maxChars = 1000)
            Spacer(Modifier.height(VxSpace.md))
            VxTextField("What will you adjust next?", n, { n = it }, singleLine = false, minLines = 2, maxChars = 1000)
            Spacer(Modifier.height(VxSpace.xl))
            PrimaryButton("Save reflection", { onSave(a, o, n) }, Modifier.fillMaxWidth(), enabled = a.isNotBlank() || o.isNotBlank() || n.isNotBlank())
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
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(true), containerColor = colors.surface) {
        Column(Modifier.verticalScroll(rememberScrollState()).padding(horizontal = VxSpace.xxl).padding(bottom = VxSpace.xxxl)) {
            Text("New goal", style = MaterialTheme.typography.headlineSmall, color = colors.onSurface)
            Spacer(Modifier.height(VxSpace.lg))
            VxTextField("Goal", title, { title = it }, placeholder = "e.g. Run a 5K", maxChars = 60)
            Spacer(Modifier.height(VxSpace.lg))
            SegmentedToggle(listOf("Completions", "Completion rate"), type.ordinal, onSelect = {
                type = GoalTargetType.entries[it]
                target = if (type == GoalTargetType.RATE) "80" else "20"
            })
            Spacer(Modifier.height(VxSpace.md))
            VxTextField(
                if (type == GoalTargetType.RATE) "Target rate (%)" else "Target number of completions",
                target,
                { target = it.filter { c -> c.isDigit() }.take(4) },
                keyboardType = KeyboardType.Number
            )
            Spacer(Modifier.height(VxSpace.lg))
            Text("Supporting habits", style = MaterialTheme.typography.labelLarge, color = colors.onSurface)
            Spacer(Modifier.height(VxSpace.sm))
            if (state.activeHabits.isEmpty()) {
                Text("Start a routine first to link habits.", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(VxSpace.sm), verticalArrangement = Arrangement.spacedBy(VxSpace.sm)) {
                state.activeHabits.forEach { h ->
                    CategoryChip(h.title, h.id in selected, onClick = { selected = if (h.id in selected) selected - h.id else selected + h.id })
                }
            }
            Spacer(Modifier.height(VxSpace.xl))
            PrimaryButton(
                "Save goal",
                { onSave(title.trim(), type, target.toDoubleOrNull() ?: 0.0, selected.toList()) },
                Modifier.fillMaxWidth(),
                enabled = title.isNotBlank() && (target.toDoubleOrNull() ?: 0.0) > 0 && selected.isNotEmpty()
            )
        }
    }
}
