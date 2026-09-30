package com.vajrax.ui.features.templates

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vajrax.core.time.TimeFormat
import com.vajrax.domain.habit.Habit
import com.vajrax.domain.template.TemplateCatalog
import com.vajrax.domain.usecase.RoutineManager
import com.vajrax.ui.designsystem.*
import com.vajrax.ui.theme.LuminaTheme
import com.vajrax.ui.theme.VxShape
import com.vajrax.ui.theme.VxSpace
import com.vajrax.ui.theme.habitAccent
import com.vajrax.ui.theme.accentOnContainer

@Composable
fun TemplateDetailScreen(
    state: ActivationState,
    onBack: () -> Unit,
    onUse: () -> Unit
) {
    val colors = LuminaTheme.colors
    Column(Modifier.fillMaxSize().background(colors.background)) {
        VxTopBar(title = "Template", onBack = onBack)
        val template = state.template
        when {
            state.isLoading -> LoadingSkeleton(Modifier.padding(VxSpace.gutter))
            template == null -> EmptyState(VxIcons.Info, "Template unavailable", state.error ?: "Try another template.")
            else -> {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = VxSpace.gutter, vertical = VxSpace.xl)
                ) {
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                template.category.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.accentOnContainer,
                                modifier = Modifier.clip(VxShape.pill).background(colors.primaryContainer).padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                            if (template.author != null) {
                                Spacer(Modifier.width(VxSpace.sm))
                                Text("by @${template.author}", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                            }
                        }
                        Spacer(Modifier.height(VxSpace.md))
                        Text(template.name, style = MaterialTheme.typography.headlineMedium, color = colors.onSurface)
                        Spacer(Modifier.height(VxSpace.sm))
                        Text(template.description, style = MaterialTheme.typography.bodyLarge, color = colors.onSurfaceVariant)
                        Spacer(Modifier.height(VxSpace.lg))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(VxIcons.Target, null, tint = colors.primary, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(VxSpace.sm))
                            Text("Recommended for: ${template.recommendedFor.ifBlank { "Building a steady routine" }}", style = MaterialTheme.typography.bodyMedium, color = colors.onSurface)
                        }
                        if (template.category in HEALTH_CATEGORIES) {
                            Spacer(Modifier.height(VxSpace.md))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(VxIcons.Info, null, tint = colors.onSurfaceVariant, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(VxSpace.sm))
                                Text(
                                    "General wellness ideas, not medical advice.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(Modifier.height(VxSpace.xl))
                        Row(horizontalArrangement = Arrangement.spacedBy(VxSpace.sm)) {
                            StatPod("Habits", "${state.drafts.size}", Modifier.weight(1f))
                            StatPod("Per day", TimeFormat.duration(state.drafts.sumOf { it.habit.durationMinutes }), Modifier.weight(1f))
                            StatPod("Cycle", template.frequencyLabel, Modifier.weight(1f))
                        }
                        Spacer(Modifier.height(VxSpace.xxl))
                        SectionLabel("Habit timeline")
                        Spacer(Modifier.height(VxSpace.md))
                    }
                    itemsIndexed(state.drafts, key = { i, d -> "${d.habit.id}_$i" }) { _, d ->
                        TimelinePreviewRow(d.habit)
                    }
                    item { Spacer(Modifier.height(VxSpace.lg)) }
                }
                BottomBar {
                    PrimaryButton("Use this template", onUse, Modifier.fillMaxWidth())
                }
            }
        }
    }
}

/** Template categories whose habits touch exercise, diet, fasting or sleep. */
private val HEALTH_CATEGORIES = setOf("Fitness", "Health", "Detox", "Challenge", "Mind", "Evening", "Morning")

@Composable
private fun TimelinePreviewRow(habit: Habit) {
    val colors = LuminaTheme.colors
    val accent = habitAccent(habit.color, colors.isDark)
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = VxSpace.sm).semantics(mergeDescendants = true) {},
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            TimeFormat.display(habit.time).ifBlank { "Anytime" },
            style = MaterialTheme.typography.labelMedium,
            color = colors.onSurfaceVariant,
            modifier = Modifier.width(72.dp)
        )
        IconBadge(VxIcons.forKey(habit.icon), accent, size = 36.dp, iconSize = 18.dp)
        Spacer(Modifier.width(VxSpace.md))
        Column(Modifier.weight(1f)) {
            Text(habit.title, style = MaterialTheme.typography.titleSmall, color = colors.onSurface, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(
                listOfNotNull(TimeFormat.duration(habit.durationMinutes), habit.targetLabel(), habit.category).joinToString(" · "),
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant
            )
        }
    }
}

@Composable
fun BottomBar(content: @Composable ColumnScope.() -> Unit) {
    val colors = LuminaTheme.colors
    Column(
        Modifier.fillMaxWidth().background(colors.surface)
            .drawBehind { drawLine(colors.outlineVariant, androidx.compose.ui.geometry.Offset(0f, 0f), androidx.compose.ui.geometry.Offset(size.width, 0f), 2f) }
            .navigationBarsPadding()
            .padding(horizontal = VxSpace.gutter, vertical = VxSpace.lg),
        content = content
    )
}

@Composable
fun DashedAddBox(title: String, subtitle: String, buttonLabel: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = LuminaTheme.colors
    Column(
        modifier = modifier.fillMaxWidth()
            .drawBehind {
                drawRoundRect(
                    color = colors.primary.copy(alpha = 0.35f),
                    style = Stroke(width = 3f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f))),
                    cornerRadius = CornerRadius(16.dp.toPx())
                )
            }
            .clip(RoundedCornerShape(16.dp))
            .background(colors.primaryContainer.copy(alpha = 0.35f))
            .padding(VxSpace.lg)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(VxIcons.Lightbulb, colors.primary, size = 36.dp, iconSize = 18.dp)
            Spacer(Modifier.width(VxSpace.md))
            Column {
                Text(title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = colors.onSurface)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            }
        }
        Spacer(Modifier.height(VxSpace.md))
        Row(
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).clip(RoundedCornerShape(12.dp))
                .background(colors.surface).border(1.dp, colors.primary.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                .hapticClickable(onClick = onClick),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(VxIcons.Plus, null, tint = colors.primary, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(VxSpace.sm))
            Text(buttonLabel, style = MaterialTheme.typography.labelLarge, color = colors.primary)
        }
    }
}

@Composable
fun CustomizeScreen(
    state: ActivationState,
    onIntent: (ActivationIntent) -> Unit,
    onBack: () -> Unit
) {
    val colors = LuminaTheme.colors
    var editingIndex by remember { mutableStateOf<Int?>(null) }
    var adding by remember { mutableStateOf(false) }
    var confirmSwitch by remember { mutableStateOf(false) }
    val template = state.template

    Column(Modifier.fillMaxSize().background(colors.background)) {
        VxTopBar(title = "Customize", onBack = onBack)
        if (state.isLoading || template == null) {
            LoadingSkeleton(Modifier.padding(VxSpace.gutter))
            return@Column
        }
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = VxSpace.gutter, vertical = VxSpace.xl)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(VxIcons.Sparkles, null, tint = colors.primary, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("MAKE IT YOURS", style = MaterialTheme.typography.labelSmall, color = colors.primary)
                }
                Spacer(Modifier.height(VxSpace.sm))
                Text(
                    if (template.id == TemplateCatalog.BLANK_ID) "Build your tracker" else "Personalize ${template.name}",
                    style = MaterialTheme.typography.headlineSmall,
                    color = colors.onSurface
                )
                Spacer(Modifier.height(VxSpace.xs))
                Text(
                    "Untick or tap to edit.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant
                )
                Spacer(Modifier.height(VxSpace.xl))
                VxTextField(
                    label = "Tracker name",
                    value = state.trackerName,
                    onValueChange = { onIntent(ActivationIntent.Rename(it)) },
                    maxChars = 40
                )
                Spacer(Modifier.height(VxSpace.lg))
                Text("Start", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium), color = colors.onSurface)
                Spacer(Modifier.height(VxSpace.sm))
                val tomorrow = state.startDate != null && state.today != null && state.startDate > state.today
                Row(horizontalArrangement = Arrangement.spacedBy(VxSpace.sm)) {
                    val left = state.remainingToday
                    CategoryChip(
                        if (state.included.isEmpty()) "Today" else if (left == 0) "Today · none left" else "Today · $left left",
                        !tomorrow,
                        onClick = { onIntent(ActivationIntent.StartTomorrow(false)) }
                    )
                    CategoryChip("Tomorrow", tomorrow, onClick = { onIntent(ActivationIntent.StartTomorrow(true)) })
                }
                if (state.wakeTime != null && template.habits.isNotEmpty()) {
                    Spacer(Modifier.height(VxSpace.md))
                    Row(
                        Modifier.fillMaxWidth().clip(VxShape.small).background(colors.primaryContainer).padding(VxSpace.md),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(VxIcons.Sunrise, null, tint = colors.primary, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(VxSpace.sm))
                        Text(
                            "Timed around your ${TimeFormat.display(state.wakeTime)} wake-up",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onPrimaryContainer
                        )
                    }
                }
                Spacer(Modifier.height(VxSpace.xxl))
                SectionLabel("Habits") {
                    Text("${state.included.size} of ${state.drafts.size} selected", style = MaterialTheme.typography.labelMedium, color = colors.primary)
                }
                Spacer(Modifier.height(VxSpace.sm))
            }
            if (state.drafts.isEmpty()) {
                item {
                    Text(
                        "No habits yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = VxSpace.md)
                    )
                }
            }
            itemsIndexed(state.drafts, key = { i, d -> "${d.habit.id}_$i" }) { index, d ->
                DraftRow(d, onToggle = { onIntent(ActivationIntent.ToggleHabit(index)) }, onEdit = { editingIndex = index })
                Spacer(Modifier.height(VxSpace.sm))
            }
            item {
                Spacer(Modifier.height(VxSpace.sm))
                DashedAddBox(
                    title = "Add your own habit",
                    subtitle = "Check, count, minutes or value.",
                    buttonLabel = "Add habit",
                    onClick = { adding = true }
                )
                Spacer(Modifier.height(VxSpace.lg))
            }
        }
        BottomBar {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${state.included.size} habits · ${TimeFormat.duration(state.dailyMinutes)} a day",
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.onSurface,
                    modifier = Modifier.weight(1f)
                )
            }
            if (state.error != null) {
                Spacer(Modifier.height(VxSpace.xs))
                Text(state.error, style = MaterialTheme.typography.bodySmall, color = colors.statusError)
            }
            Spacer(Modifier.height(VxSpace.md))
            PrimaryButton(
                text = if (state.hasActiveTracker) "Switch to this routine" else "Start my routine",
                onClick = { if (state.hasActiveTracker) confirmSwitch = true else onIntent(ActivationIntent.Activate) },
                enabled = state.included.isNotEmpty(),
                loading = state.isActivating,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    editingIndex?.let { index ->
        val draft = state.drafts.getOrNull(index)
        if (draft != null) {
            HabitEditorSheet(
                initial = draft.habit,
                isNew = false,
                onDismiss = { editingIndex = null },
                onSave = {
                    onIntent(ActivationIntent.UpdateHabit(index, it))
                    editingIndex = null
                },
                onRemove = {
                    onIntent(ActivationIntent.RemoveHabit(index))
                    editingIndex = null
                },
                removeLabel = "Remove habit"
            )
        }
    }
    if (adding) {
        HabitEditorSheet(
            initial = RoutineManager.blankHabit(),
            isNew = true,
            onDismiss = { adding = false },
            onSave = {
                onIntent(ActivationIntent.AddHabit(it))
                adding = false
            }
        )
    }
    if (confirmSwitch) {
        ConfirmDialog(
            title = "Switch routine?",
            message = "Current routine is archived from today. History stays.",
            confirmLabel = "Switch",
            onConfirm = {
                confirmSwitch = false
                onIntent(ActivationIntent.Activate)
            },
            onDismiss = { confirmSwitch = false }
        )
    }
}

@Composable
private fun DraftRow(draft: DraftHabit, onToggle: () -> Unit, onEdit: () -> Unit) {
    val colors = LuminaTheme.colors
    val habit = draft.habit
    val accent = habitAccent(habit.color, colors.isDark)
    Row(
        modifier = Modifier.fillMaxWidth().clip(VxShape.medium).background(colors.surface)
            .border(1.dp, colors.outlineVariant.copy(alpha = 0.8f), VxShape.medium)
            .hapticClickable(onClick = onEdit)
            .padding(start = VxSpace.xs, end = VxSpace.md, top = VxSpace.xs, bottom = VxSpace.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = draft.included,
            onCheckedChange = { onToggle() },
            colors = CheckboxDefaults.colors(checkedColor = colors.primary),
            modifier = Modifier.semantics { contentDescription = "Include ${habit.title}" }
        )
        IconBadge(VxIcons.forKey(habit.icon), if (draft.included) accent else colors.outline, size = 36.dp, iconSize = 18.dp)
        Spacer(Modifier.width(VxSpace.md))
        Column(Modifier.weight(1f).padding(vertical = VxSpace.sm)) {
            Text(
                habit.title,
                style = MaterialTheme.typography.titleSmall,
                color = if (draft.included) colors.onSurface else colors.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                listOfNotNull(
                    TimeFormat.display(habit.time).ifBlank { null },
                    TimeFormat.duration(habit.durationMinutes),
                    habit.targetLabel(),
                    habit.schedule.label()
                ).joinToString(" · "),
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant
            )
        }
        Icon(VxIcons.ChevronRight, null, tint = colors.onSurfaceVariant, modifier = Modifier.size(18.dp))
    }
}
