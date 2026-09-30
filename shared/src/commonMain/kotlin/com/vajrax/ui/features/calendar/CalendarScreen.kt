package com.vajrax.ui.features.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vajrax.core.time.Dates
import com.vajrax.core.time.TimeFormat
import com.vajrax.domain.model.ActionStatus
import com.vajrax.ui.designsystem.*
import com.vajrax.ui.theme.LuminaTheme
import com.vajrax.ui.theme.VxShape
import com.vajrax.ui.theme.VxSpace
import com.vajrax.ui.theme.habitAccent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@Composable
fun CalendarScreen(
    state: CalendarUiState,
    effects: Flow<CalendarEffect>,
    onIntent: (CalendarIntent) -> Unit,
    onOpenDiscover: () -> Unit
) {
    val colors = LuminaTheme.colors
    val snackbar = LocalVxSnackbar.current
    val scope = rememberCoroutineScope()
    var correction by remember { mutableStateOf<Pair<MatrixCell, String>?>(null) }

    LaunchedEffect(effects) {
        effects.collect { e -> if (e is CalendarEffect.ShowMessage) scope.launch { snackbar.showSnackbar(e.message) } }
    }

    Column(Modifier.fillMaxSize().background(colors.background).statusBarsPadding()) {
        Spacer(Modifier.height(VxSpace.lg))
        Row(
            Modifier.fillMaxWidth().padding(horizontal = VxSpace.gutter),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ScreenTitle("Calendar", Modifier.weight(1f))
            SegmentedToggle(
                options = listOf("Week", "Month"),
                selectedIndex = state.mode.ordinal,
                onSelect = { onIntent(CalendarIntent.SetMode(CalendarMode.entries[it])) }
            )
        }
        Spacer(Modifier.height(VxSpace.lg))

        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = VxSpace.lg)
        ) {
            if (state.isLoading) {
                LoadingSkeleton()
            } else if (!state.hasTracker && state.rows.isEmpty()) {
                EmptyState(VxIcons.Calendar, "Your calendar is empty", "Start a routine to see your days.", actionLabel = "Choose a template", onAction = onOpenDiscover)
            } else if (state.mode == CalendarMode.WEEK) {
                WeekMatrix(
                    state = state,
                    onIntent = onIntent,
                    onPastTap = { scope.launch { snackbar.showSnackbar("Long-press to correct a past day") } },
                    onPastLongPress = { cell, title -> correction = cell to title }
                )
                Spacer(Modifier.height(VxSpace.md))
                Legend(
                    listOf(
                        LegendItem(colors.primary, "Done today", filled = true),
                        LegendItem(colors.outline, "Done", filled = false, check = true),
                        LegendItem(colors.outlineVariant, "Not done / upcoming", filled = false)
                    )
                )
            } else {
                MonthView(state, onIntent)
            }
            Spacer(Modifier.height(VxSpace.navClearance))
        }
    }

    correction?.let { (cell, title) ->
        val markDone = cell.kind != CellKind.DONE_PAST
        ConfirmDialog(
            title = "Correct this record?",
            message = "$title · ${Dates.shortLabel(cell.date)} → ${if (markDone) "done" else "not done"}",
            confirmLabel = if (markDone) "Mark done" else "Mark not done",
            onConfirm = {
                cell.occurrenceId?.let { onIntent(CalendarIntent.CorrectPast(it, markDone)) }
                correction = null
            },
            onDismiss = { correction = null }
        )
    }
}

@Composable
private fun WeekMatrix(
    state: CalendarUiState,
    onIntent: (CalendarIntent) -> Unit,
    onPastTap: () -> Unit,
    onPastLongPress: (MatrixCell, String) -> Unit
) {
    val colors = LuminaTheme.colors
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        RoundIconButton(VxIcons.ChevronLeft, "Previous days", onClick = { onIntent(CalendarIntent.Shift(-5)) })
        Text(state.rangeLabel, style = MaterialTheme.typography.labelLarge, color = colors.onSurface, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
        if (state.selected != state.today) {
            TextButton(onClick = { onIntent(CalendarIntent.GoToday) }) { Text("Today") }
        }
        RoundIconButton(VxIcons.ChevronRight, "Next days", onClick = { onIntent(CalendarIntent.Shift(5)) })
    }
    Spacer(Modifier.height(VxSpace.sm))
    var dragTotal by remember { mutableStateOf(0f) }
    VxCard(
        elevated = true,
        contentPadding = PaddingValues(vertical = VxSpace.lg),
        modifier = Modifier.pointerInput(Unit) {
            detectHorizontalDragGestures(
                onDragEnd = {
                    if (dragTotal > 120f) onIntent(CalendarIntent.Shift(-5)) else if (dragTotal < -120f) onIntent(CalendarIntent.Shift(5))
                    dragTotal = 0f
                },
                onHorizontalDrag = { _, amount -> dragTotal += amount }
            )
        }
    ) {
        Row(Modifier.fillMaxWidth().padding(start = VxSpace.lg, end = VxSpace.sm), verticalAlignment = Alignment.Top) {
            Text(
                state.trackerName.ifBlank { "Your habits" },
                style = MaterialTheme.typography.titleMedium,
                color = colors.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f).padding(top = 6.dp, end = VxSpace.sm)
            )
            state.weekDays.forEach { d ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(40.dp).clip(RoundedCornerShape(12.dp))
                        .hapticClickable(kind = VxHaptic.Select) { onIntent(CalendarIntent.Select(d.date)) }
                        .semantics { contentDescription = "${d.dayName} ${d.dayNumber}${if (d.isToday) ", today" else ""}" }
                ) {
                    Box(
                        Modifier.size(32.dp).clip(CircleShape).background(if (d.isSelected) colors.primary else Color.Transparent),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "${d.dayNumber}",
                            style = MaterialTheme.typography.labelLarge,
                            color = when {
                                d.isSelected -> colors.onPrimary
                                d.isToday -> colors.primary
                                else -> colors.onSurfaceVariant
                            }
                        )
                    }
                    Text(
                        d.dayName,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = if (d.isSelected) FontWeight.Bold else FontWeight.Normal),
                        color = if (d.isSelected) colors.primary else colors.onSurfaceVariant
                    )
                }
            }
        }
        Spacer(Modifier.height(VxSpace.md))
        HorizontalDivider(Modifier.padding(horizontal = VxSpace.lg), color = colors.outlineVariant.copy(alpha = 0.7f))
        if (state.rows.isEmpty()) {
            Text(
                "No habits in this range.",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant,
                modifier = Modifier.padding(VxSpace.lg)
            )
        }
        state.rows.forEachIndexed { index, row ->
            MatrixRowView(row, onIntent, onPastTap, onPastLongPress)
            if (index != state.rows.lastIndex) {
                HorizontalDivider(Modifier.padding(horizontal = VxSpace.lg), color = colors.outlineVariant.copy(alpha = 0.4f))
            }
        }
    }
}

@Composable
private fun MatrixRowView(
    row: MatrixRow,
    onIntent: (CalendarIntent) -> Unit,
    onPastTap: () -> Unit,
    onPastLongPress: (MatrixCell, String) -> Unit
) {
    val colors = LuminaTheme.colors
    Row(Modifier.fillMaxWidth().padding(start = VxSpace.md, end = VxSpace.sm, top = 2.dp, bottom = 2.dp), verticalAlignment = Alignment.CenterVertically) {
        IconBadge(VxIcons.forKey(row.habit.icon), habitAccent(row.habit.color, colors.isDark), size = 36.dp, iconSize = 18.dp)
        Spacer(Modifier.width(VxSpace.sm))
        Column(Modifier.weight(1f)) {
            Text(row.habit.title, style = MaterialTheme.typography.labelLarge, color = colors.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(row.timeLabel, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Normal), color = colors.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        row.cells.forEach { cell ->
            val label = "${row.habit.title}, ${Dates.shortLabel(cell.date)}"
            when (cell.kind) {
                CellKind.DONE_TODAY -> CheckCircle(CheckState.DONE, { cell.occurrenceId?.let { onIntent(CalendarIntent.ToggleToday(it)) } }, label, size = 24.dp, touchWidth = 40.dp)
                CellKind.OPEN_TODAY -> CheckCircle(CheckState.OPEN, { cell.occurrenceId?.let { onIntent(CalendarIntent.ToggleToday(it)) } }, label, size = 24.dp, touchWidth = 40.dp)
                CellKind.DONE_PAST -> CheckCircle(CheckState.DONE_MUTED, onPastTap, label, size = 24.dp, touchWidth = 40.dp, onLongClick = { onPastLongPress(cell, row.habit.title) })
                CellKind.MISSED -> CheckCircle(CheckState.DISABLED, onPastTap, "$label, not done", size = 24.dp, touchWidth = 40.dp, onLongClick = { onPastLongPress(cell, row.habit.title) })
                CellKind.SKIPPED -> CheckCircle(CheckState.SKIPPED, null, "$label, skipped", size = 24.dp, touchWidth = 40.dp)
                CellKind.FUTURE -> CheckCircle(CheckState.DISABLED, null, "$label, upcoming", size = 24.dp, touchWidth = 40.dp)
                CellKind.REST -> Box(Modifier.width(40.dp).height(48.dp).semantics { contentDescription = "$label, rest day" }, contentAlignment = Alignment.Center) {
                    Box(Modifier.width(8.dp).height(2.dp).clip(VxShape.pill).background(colors.outlineVariant))
                }
            }
        }
    }
}

private data class LegendItem(val color: Color, val label: String, val filled: Boolean, val check: Boolean = false)

@Composable
private fun Legend(items: List<LegendItem>) {
    val colors = LuminaTheme.colors
    Row(Modifier.fillMaxWidth().padding(horizontal = VxSpace.sm), horizontalArrangement = Arrangement.spacedBy(VxSpace.lg)) {
        items.forEach { item ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(12.dp).clip(CircleShape)
                        .background(if (item.filled) item.color else Color.Transparent)
                        .border(1.dp, item.color, CircleShape)
                )
                Spacer(Modifier.width(6.dp))
                Text(item.label, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Normal), color = colors.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun MonthView(state: CalendarUiState, onIntent: (CalendarIntent) -> Unit) {
    val colors = LuminaTheme.colors
    VxCard(elevated = true) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            RoundIconButton(VxIcons.ChevronLeft, "Previous month", onClick = { onIntent(CalendarIntent.ShiftMonth(-1)) })
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(state.monthLabel, style = MaterialTheme.typography.titleMedium, color = colors.onSurface)
                Text(
                    state.monthStats.rate?.let { "$it% completed · ${state.monthStats.completed} of ${state.monthStats.eligible}" } ?: "No completed days yet",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant
                )
            }
            RoundIconButton(VxIcons.ChevronRight, "Next month", onClick = { onIntent(CalendarIntent.ShiftMonth(1)) })
        }
        Spacer(Modifier.height(VxSpace.md))
        Row(Modifier.fillMaxWidth()) {
            listOf("M", "T", "W", "T", "F", "S", "S").forEach {
                Text(it, style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
            }
        }
        Spacer(Modifier.height(VxSpace.xs))
        state.monthCells.chunked(7).forEach { week ->
            Row(Modifier.fillMaxWidth()) {
                week.forEach { cell -> HeatCell(cell, Modifier.weight(1f), onClick = { cell.date?.let { onIntent(CalendarIntent.Select(it)) } }) }
            }
        }
        Spacer(Modifier.height(VxSpace.md))
        Row(horizontalArrangement = Arrangement.spacedBy(VxSpace.md), verticalAlignment = Alignment.CenterVertically) {
            listOf(3 to "High", 2 to "Medium", 1 to "Low", 0 to "None").forEach { (level, label) ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(12.dp).clip(RoundedCornerShape(3.dp)).background(heatColor(level)).border(1.dp, colors.outlineVariant, RoundedCornerShape(3.dp)))
                    Spacer(Modifier.width(4.dp))
                    Text(label, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Normal), color = colors.onSurfaceVariant)
                }
            }
        }
    }
    Spacer(Modifier.height(VxSpace.lg))
    VxCard {
        Text(state.selectedLabel, style = MaterialTheme.typography.titleMedium, color = colors.onSurface)
        val s = state.selectedStats
        Text(
            when {
                state.selected != null && state.today != null && state.selected > state.today -> "Upcoming day"
                s.scheduled == 0 -> "Nothing was scheduled — rest day"
                else -> "${s.completed} of ${s.eligible + s.pending} done" + (if (s.skipped > 0) " · ${s.skipped} skipped" else "")
            },
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurfaceVariant
        )
        Spacer(Modifier.height(VxSpace.sm))
        state.selectedItems.forEach { item ->
            Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                val (icon, tint, text) = when (item.status) {
                    ActionStatus.COMPLETE, ActionStatus.MINIMUM -> Triple(VxIcons.Check, colors.statusSuccess, if (item.status == ActionStatus.MINIMUM) "Minimum" else "Done")
                    ActionStatus.SKIPPED -> Triple(VxIcons.SkipForward, colors.onSurfaceVariant, "Skipped")
                    else -> Triple(VxIcons.Clock, colors.onSurfaceVariant, if (item.isPast) "Not done" else "Open")
                }
                Icon(icon, null, tint = tint, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(VxSpace.sm))
                Text(item.habit.title, style = MaterialTheme.typography.bodyMedium, color = colors.onSurface, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${TimeFormat.display(item.habit.time)} · $text", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun heatColor(level: Int): Color {
    val colors = LuminaTheme.colors
    return when (level) {
        3 -> colors.primary
        2 -> colors.primary.copy(alpha = 0.55f)
        1 -> colors.primary.copy(alpha = 0.22f)
        0 -> colors.surfaceContainerHigh
        else -> Color.Transparent
    }
}

@Composable
private fun HeatCell(cell: MonthCell, modifier: Modifier, onClick: () -> Unit) {
    val colors = LuminaTheme.colors
    Box(modifier.aspectRatio(1f).padding(3.dp), contentAlignment = Alignment.Center) {
        if (cell.date == null) return@Box
        val bg = heatColor(cell.level)
        val description = "${Dates.shortLabel(cell.date)}: " + when {
            cell.isFuture -> "upcoming"
            cell.level == -1 -> "nothing scheduled"
            else -> "${cell.rate ?: 0} percent completed"
        }
        Box(
            Modifier.fillMaxSize().clip(RoundedCornerShape(10.dp)).background(bg)
                .then(if (cell.isSelected) Modifier.border(2.dp, colors.onSurface, RoundedCornerShape(10.dp)) else if (cell.isToday) Modifier.border(1.5.dp, colors.primary, RoundedCornerShape(10.dp)) else Modifier)
                .hapticClickable(onClick = onClick)
                .semantics { contentDescription = description },
            contentAlignment = Alignment.Center
        ) {
            Text(
                "${cell.date.day}",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = if (cell.isToday) FontWeight.Bold else FontWeight.Medium),
                color = when {
                    cell.level == 3 || cell.level == 2 -> colors.onPrimary
                    cell.isFuture -> colors.textTertiary
                    else -> colors.onSurface
                }
            )
        }
    }
}
