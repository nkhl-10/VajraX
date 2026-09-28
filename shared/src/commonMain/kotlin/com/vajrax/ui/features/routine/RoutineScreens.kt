package com.vajrax.ui.features.routine

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vajrax.core.time.Dates
import com.vajrax.core.time.TimeFormat
import com.vajrax.domain.habit.Habit
import com.vajrax.domain.usecase.RoutineManager
import com.vajrax.ui.designsystem.*
import com.vajrax.ui.features.templates.DashedAddBox
import com.vajrax.ui.theme.LuminaTheme
import com.vajrax.ui.theme.VxShape
import com.vajrax.ui.theme.VxSpace
import com.vajrax.ui.theme.habitAccent
import kotlinx.coroutines.flow.Flow

@Composable
private fun CollectMessages(effects: Flow<RoutineEffect>) {
    val snackbar = LocalVxSnackbar.current
    LaunchedEffect(effects) {
        effects.collect { e -> if (e is RoutineEffect.ShowMessage) snackbar.showSnackbar(e.message) }
    }
}

@Composable
fun RoutineScreen(
    state: RoutineState,
    effects: Flow<RoutineEffect>,
    onIntent: (RoutineIntent) -> Unit,
    onBack: () -> Unit,
    onOpenHabit: (String) -> Unit,
    onChangeTemplate: () -> Unit
) {
    val colors = LuminaTheme.colors
    CollectMessages(effects)
    var editing by remember { mutableStateOf<Habit?>(null) }
    var adding by remember { mutableStateOf(false) }
    var renaming by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().background(colors.background)) {
        VxTopBar("My routine", onBack = onBack, trailing = {
            if (state.tracker != null) TonalAction("Rename", VxIcons.Pencil, onClick = { renaming = true })
        })
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = VxSpace.gutter).navigationBarsPadding()) {
            Spacer(Modifier.height(VxSpace.xl))
            val tracker = state.tracker
            when {
                state.isLoading -> LoadingSkeleton()
                tracker == null -> EmptyState(VxIcons.Compass, "No active routine", "Choose a template to start.", actionLabel = "Choose a template", onAction = onChangeTemplate)
                else -> {
                    Text(tracker.name, style = MaterialTheme.typography.headlineMedium, color = colors.onSurface)
                    Text(
                        "Started ${Dates.shortLabel(tracker.startDate)} · ${state.habits.size} habits · ${TimeFormat.duration(state.habits.sumOf { it.durationMinutes })} a day",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant
                    )
                    Spacer(Modifier.height(VxSpace.sm))
                    Text(
                        "This is your personal copy — editing it never changes the original template. Changes apply from today; past days keep their records.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant
                    )
                    Spacer(Modifier.height(VxSpace.xl))
                    SectionLabel("Habits")
                    Spacer(Modifier.height(VxSpace.sm))
                    state.habits.forEach { h ->
                        RoutineHabitRow(h, onEdit = { editing = h }, onDetails = { onOpenHabit(h.id) })
                        Spacer(Modifier.height(VxSpace.sm))
                    }
                    Spacer(Modifier.height(VxSpace.sm))
                    DashedAddBox("Add a habit", "It starts today and appears in Today's plan.", "Add habit", onClick = { adding = true })
                    Spacer(Modifier.height(VxSpace.xl))
                    VxCard(contentPadding = PaddingValues(vertical = VxSpace.xs)) {
                        ListRow("Save as a reusable template", onClick = { onIntent(RoutineIntent.SaveAsTemplate) }, icon = VxIcons.Copy)
                        RowDivider()
                        ListRow("Switch to another template", onClick = onChangeTemplate, icon = VxIcons.Repeat)
                    }
                    if (state.pastTrackers.isNotEmpty()) {
                        Spacer(Modifier.height(VxSpace.xl))
                        SectionLabel("Past routines")
                        Spacer(Modifier.height(VxSpace.sm))
                        VxCard {
                            state.pastTrackers.forEach { t ->
                                Row(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                                    Text(t.name, style = MaterialTheme.typography.bodyMedium, color = colors.onSurface, modifier = Modifier.weight(1f))
                                    Text(
                                        "${Dates.shortLabel(t.startDate)} – ${t.endedAt?.let { Dates.shortLabel(it) } ?: "…"}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colors.onSurfaceVariant
                                    )
                                }
                            }
                            Text("History from past routines is included in your reports.", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                        }
                    }
                }
            }
            Spacer(Modifier.height(VxSpace.xxxl))
        }
    }

    editing?.let { h ->
        HabitEditorSheet(
            initial = h,
            isNew = false,
            onDismiss = { editing = null },
            onSave = {
                onIntent(RoutineIntent.Update(it))
                editing = null
            },
            onRemove = {
                onIntent(RoutineIntent.Archive(h.id))
                editing = null
            }
        )
    }
    if (adding) {
        HabitEditorSheet(
            initial = RoutineManager.blankHabit(),
            isNew = true,
            onDismiss = { adding = false },
            onSave = {
                onIntent(RoutineIntent.Add(it))
                adding = false
            }
        )
    }
    if (renaming) {
        var name by remember { mutableStateOf(state.tracker?.name ?: "") }
        AlertDialog(
            onDismissRequest = { renaming = false },
            title = { Text("Rename routine") },
            text = { VxTextField("Name", name, { name = it }, maxChars = 40) },
            confirmButton = {
                TextButton(onClick = {
                    onIntent(RoutineIntent.Rename(name))
                    renaming = false
                }, enabled = name.isNotBlank()) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { renaming = false }) { Text("Cancel") } },
            containerColor = colors.surface
        )
    }
}

@Composable
private fun RoutineHabitRow(habit: Habit, onEdit: () -> Unit, onDetails: () -> Unit) {
    val colors = LuminaTheme.colors
    Row(
        Modifier.fillMaxWidth().clip(VxShape.medium).background(colors.surface)
            .border(1.dp, colors.outlineVariant.copy(alpha = 0.8f), VxShape.medium)
            .clickable(role = Role.Button, onClick = onEdit)
            .padding(start = VxSpace.md, top = VxSpace.xs, bottom = VxSpace.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconBadge(VxIcons.forKey(habit.icon), habitAccent(habit.color, colors.isDark), size = 40.dp)
        Spacer(Modifier.width(VxSpace.md))
        Column(Modifier.weight(1f).padding(vertical = VxSpace.sm)) {
            Text(habit.title, style = MaterialTheme.typography.titleSmall, color = colors.onSurface, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(
                listOfNotNull(TimeFormat.display(habit.time).ifBlank { null }, TimeFormat.duration(habit.durationMinutes), habit.targetLabel(), habit.schedule.label(), if (habit.reminderEnabled) "Reminder" else null)
                    .joinToString(" · "),
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant
            )
        }
        IconButton(onClick = onDetails, modifier = Modifier.semantics { contentDescription = "${habit.title} details" }) {
            Icon(VxIcons.Chart, null, tint = colors.primary, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
fun HabitDetailScreen(
    state: HabitDetailState,
    effects: Flow<RoutineEffect>,
    onIntent: (RoutineIntent) -> Unit,
    onBack: () -> Unit
) {
    val colors = LuminaTheme.colors
    CollectMessages(effects)
    var editing by remember { mutableStateOf(false) }
    val habit = state.habit

    Column(Modifier.fillMaxSize().background(colors.background)) {
        VxTopBar("Habit", onBack = onBack, trailing = {
            if (habit != null && !habit.isArchived) TonalAction("Edit", VxIcons.Pencil, onClick = { editing = true })
        })
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = VxSpace.gutter).navigationBarsPadding()) {
            Spacer(Modifier.height(VxSpace.xl))
            when {
                state.isLoading -> LoadingSkeleton()
                habit == null -> EmptyState(VxIcons.Info, "Habit not found", "It may have been removed.")
                else -> {
                    val accent = habitAccent(habit.color, colors.isDark)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconBadge(VxIcons.forKey(habit.icon), accent, size = 56.dp, iconSize = 26.dp)
                        Spacer(Modifier.width(VxSpace.lg))
                        Column {
                            Text(habit.title, style = MaterialTheme.typography.headlineSmall, color = colors.onSurface)
                            Text(habit.category + if (habit.isArchived) " · Archived" else "", style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
                        }
                    }
                    Spacer(Modifier.height(VxSpace.xl))
                    Row(horizontalArrangement = Arrangement.spacedBy(VxSpace.sm)) {
                        StatPod("Current streak", "${state.streak.current} ${state.streakUnit}", Modifier.weight(1f))
                        StatPod("Longest", "${state.streak.longest} ${state.streakUnit}", Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(VxSpace.sm))
                    Row(horizontalArrangement = Arrangement.spacedBy(VxSpace.sm)) {
                        StatPod("Last 30 days", state.last30.rate?.let { "$it%" } ?: "—", Modifier.weight(1f))
                        StatPod("Completed", "${state.allTime.completed}", Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(VxSpace.xl))
                    VxCard {
                        SectionLabel("Schedule")
                        Spacer(Modifier.height(VxSpace.sm))
                        DetailLine("Time", TimeFormat.display(habit.time).ifBlank { "Anytime" })
                        DetailLine("Duration", TimeFormat.duration(habit.durationMinutes))
                        DetailLine("Repeats", habit.schedule.label())
                        DetailLine("Target", habit.targetLabel() ?: "Done / not done")
                        DetailLine("Reminder", if (habit.reminderEnabled) TimeFormat.display(habit.reminderTime ?: habit.time) else "Off")
                        habit.startDate?.let { DetailLine("Started", Dates.shortLabel(it)) }
                    }
                    Spacer(Modifier.height(VxSpace.lg))
                    VxCard {
                        SectionLabel("Last 5 weeks")
                        Spacer(Modifier.height(VxSpace.sm))
                        Row(Modifier.fillMaxWidth()) {
                            listOf("M", "T", "W", "T", "F", "S", "S").forEach {
                                Text(it, style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                            }
                        }
                        state.history.chunked(7).forEach { week ->
                            Row(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                                week.forEach { dot ->
                                    Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                        val (bg, border) = when (dot.kind) {
                                            2 -> colors.primary to colors.primary
                                            3 -> Color.Transparent to colors.outline
                                            1 -> colors.surfaceContainerHigh to colors.surfaceContainerHigh
                                            4 -> Color.Transparent to colors.primary
                                            else -> Color.Transparent to colors.outlineVariant.copy(alpha = 0.5f)
                                        }
                                        Box(
                                            Modifier.size(22.dp).clip(RoundedCornerShape(6.dp)).background(bg).border(1.dp, border, RoundedCornerShape(6.dp))
                                                .semantics {
                                                    contentDescription = "${Dates.shortLabel(dot.date)}: " + when (dot.kind) {
                                                        2 -> "done"; 3 -> "skipped"; 1 -> "not done"; 4 -> "today"; 5 -> "upcoming"; else -> "not scheduled"
                                                    }
                                                }
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(VxSpace.sm))
                        Text("Filled: done · Outline: skipped · Grey: not done · Blank: rest day", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                    }
                    if (state.notes.isNotEmpty()) {
                        Spacer(Modifier.height(VxSpace.lg))
                        VxCard {
                            SectionLabel("Recent notes")
                            state.notes.forEach { (date, note) ->
                                Spacer(Modifier.height(VxSpace.sm))
                                Text(Dates.shortLabel(date), style = MaterialTheme.typography.labelMedium, color = colors.primary)
                                Text(note, style = MaterialTheme.typography.bodyMedium, color = colors.onSurface)
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(VxSpace.xxxl))
        }
    }

    if (editing && habit != null) {
        HabitEditorSheet(
            initial = habit,
            isNew = false,
            onDismiss = { editing = false },
            onSave = {
                onIntent(RoutineIntent.Update(it))
                editing = false
            },
            onRemove = {
                onIntent(RoutineIntent.Archive(habit.id))
                editing = false
            }
        )
    }
}

@Composable
private fun DetailLine(label: String, value: String) {
    val colors = LuminaTheme.colors
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = colors.onSurface)
    }
}
