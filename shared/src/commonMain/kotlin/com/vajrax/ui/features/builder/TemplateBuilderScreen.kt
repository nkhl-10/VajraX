package com.vajrax.ui.features.builder

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vajrax.core.time.TimeFormat
import com.vajrax.domain.habit.Habit
import com.vajrax.domain.usecase.RoutineManager
import com.vajrax.resources.*
import com.vajrax.ui.designsystem.*
import com.vajrax.ui.features.templates.BottomBar
import com.vajrax.ui.features.templates.DashedAddBox
import com.vajrax.ui.theme.LuminaTheme
import com.vajrax.ui.theme.VxShape
import com.vajrax.ui.theme.VxSpace
import com.vajrax.ui.theme.habitAccent
import kotlinx.coroutines.flow.Flow
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TemplateBuilderScreen(
    state: BuilderState,
    effects: Flow<BuilderEffect>,
    onIntent: (BuilderIntent) -> Unit,
    onBack: () -> Unit,
    onSaved: (templateId: String, published: Boolean) -> Unit
) {
    val colors = LuminaTheme.colors
    val snackbar = LocalVxSnackbar.current
    var editing by remember { mutableStateOf<Int?>(null) }
    var adding by remember { mutableStateOf(false) }
    var preview by remember { mutableStateOf(false) }
    var confirmLeave by remember { mutableStateOf(false) }
    val dirty = state.name.isNotBlank() || state.habits.isNotEmpty() || state.description.isNotBlank()
    val leave = { if (dirty && !state.isEditing) confirmLeave = true else onBack() }

    LaunchedEffect(effects) {
        effects.collect { e ->
            when (e) {
                is BuilderEffect.Saved -> onSaved(e.templateId, e.published)
                is BuilderEffect.ShowMessage -> snackbar.showSnackbar(e.message)
            }
        }
    }

    Column(Modifier.fillMaxSize().background(colors.background)) {
        VxTopBar(
            title = if (state.isEditing) stringResource(Res.string.builder_edit_template) else stringResource(Res.string.builder_new_template),
            onBack = leave,
            trailing = { TonalAction(stringResource(Res.string.builder_preview_2), VxIcons.Eye, onClick = { preview = true }) }
        )
        if (state.isLoading) {
            LoadingSkeleton(Modifier.padding(VxSpace.gutter))
            return@Column
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = VxSpace.gutter)) {
            Spacer(Modifier.height(VxSpace.xl))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(VxIcons.Sparkles, null, tint = colors.primary, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(stringResource(Res.string.templates_make_it_yours), style = MaterialTheme.typography.labelSmall, color = colors.primary)
            }
            Spacer(Modifier.height(VxSpace.xs))
            Text(stringResource(Res.string.builder_build_a_reusable_template), style = MaterialTheme.typography.headlineMedium, color = colors.onSurface)
            Spacer(Modifier.height(VxSpace.xs))
            Text(
                stringResource(Res.string.builder_turn_a_repeatable_routine_into),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant
            )

            Spacer(Modifier.height(VxSpace.xl))
            SectionLabel(stringResource(Res.string.builder_template_details))
            Spacer(Modifier.height(VxSpace.sm))
            VxCard {
                VxTextField(
                    label = stringResource(Res.string.builder_template_name),
                    value = state.name,
                    onValueChange = { onIntent(BuilderIntent.SetName(it)) },
                    placeholder = stringResource(Res.string.builder_weekly_planning_ritual),
                    helper = stringResource(Res.string.builder_shown_in_your_template_library),
                    error = state.nameError,
                    maxChars = TemplateBuilderViewModel.NAME_MAX
                )
                Spacer(Modifier.height(VxSpace.lg))
                VxTextField(
                    label = stringResource(Res.string.builder_description),
                    value = state.description,
                    onValueChange = { onIntent(BuilderIntent.SetDescription(it)) },
                    placeholder = stringResource(Res.string.builder_what_is_this_routine_for),
                    singleLine = false,
                    minLines = 3,
                    maxChars = TemplateBuilderViewModel.DESCRIPTION_MAX
                )
            }

            Spacer(Modifier.height(VxSpace.xl))
            SectionLabel(stringResource(Res.string.common_category))
            Text(stringResource(Res.string.builder_choose_where_this_template_belongs), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            Spacer(Modifier.height(VxSpace.sm))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(VxSpace.sm), verticalArrangement = Arrangement.spacedBy(VxSpace.sm)) {
                TemplateBuilderViewModel.categories.forEach { c ->
                    CategoryChip(c, state.category == c, onClick = { onIntent(BuilderIntent.SetCategory(c)) })
                }
            }

            Spacer(Modifier.height(VxSpace.xl))
            SectionLabel(stringResource(Res.string.builder_template_structure)) {
                Text("${state.habits.size} ${if (state.habits.size == 1) "habit" else "habits"}", style = MaterialTheme.typography.labelMedium, color = colors.primary)
            }
            Text(stringResource(Res.string.builder_drag_to_reorder_tap_to), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            Spacer(Modifier.height(VxSpace.sm))
            ReorderableHabits(
                habits = state.habits,
                onMove = { from, to -> onIntent(BuilderIntent.MoveHabit(from, to)) },
                onEdit = { editing = it }
            )
            if (state.habitsError != null) {
                Text(state.habitsError, style = MaterialTheme.typography.bodySmall, color = colors.statusError)
                Spacer(Modifier.height(VxSpace.sm))
            }
            DashedAddBox(
                title = stringResource(Res.string.builder_shape_the_next_step),
                subtitle = stringResource(Res.string.templates_check_count_minutes_or_value),
                buttonLabel = stringResource(Res.string.builder_add_a_habit),
                onClick = { adding = true }
            )

            Spacer(Modifier.height(VxSpace.lg))
            VxCard(onClick = { preview = true }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconBadge(VxIcons.Smartphone, colors.primary, size = 44.dp)
                    Spacer(Modifier.width(VxSpace.md))
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(Res.string.builder_preview_your_template), style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = colors.onSurface)
                        Text(stringResource(Res.string.builder_see_how_this_will_feel), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                    }
                    Icon(VxIcons.ArrowRight, null, tint = colors.primary, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(Modifier.height(VxSpace.xl))
        }
        BottomBar {
            Row(horizontalArrangement = Arrangement.spacedBy(VxSpace.md)) {
                SecondaryButton(stringResource(Res.string.builder_save_draft), { onIntent(BuilderIntent.SaveDraft) }, Modifier.weight(1f), enabled = !state.isSaving)
                PrimaryButton(if (state.isEditing) stringResource(Res.string.builder_save_template) else stringResource(Res.string.discover_create_template), { onIntent(BuilderIntent.Create) }, Modifier.weight(1f), loading = state.isSaving)
            }
            TextButton(onClick = leave, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(Res.string.common_cancel), color = colors.onSurfaceVariant, style = MaterialTheme.typography.labelLarge)
            }
        }
    }

    editing?.let { index ->
        state.habits.getOrNull(index)?.let { habit ->
            HabitEditorSheet(
                initial = habit,
                isNew = false,
                onDismiss = { editing = null },
                onSave = {
                    onIntent(BuilderIntent.UpdateHabit(index, it))
                    editing = null
                },
                onRemove = {
                    onIntent(BuilderIntent.RemoveHabit(index))
                    editing = null
                },
                removeLabel = stringResource(Res.string.templates_remove_habit)
            )
        }
    }
    if (adding) {
        HabitEditorSheet(
            initial = RoutineManager.blankHabit(),
            isNew = true,
            onDismiss = { adding = false },
            onSave = {
                onIntent(BuilderIntent.AddHabit(it))
                adding = false
            }
        )
    }
    if (preview) {
        VxBottomSheet(onDismiss = { preview = false }, containerColor = colors.background) {
            Column(Modifier.verticalScroll(rememberScrollState()).padding(horizontal = VxSpace.gutter).padding(bottom = VxSpace.xxxl)) {
                Text(stringResource(Res.string.builder_preview), style = MaterialTheme.typography.labelSmall, color = colors.primary)
                Spacer(Modifier.height(VxSpace.xs))
                Text(state.name.ifBlank { "Untitled template" }, style = MaterialTheme.typography.headlineSmall, color = colors.onSurface)
                Text(state.description.ifBlank { "No description yet." }, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
                Spacer(Modifier.height(VxSpace.md))
                Text(stringResource(Res.string.builder_habits_a_day_fmt, state.habits.size, TimeFormat.duration(state.dailyMinutes), state.category), style = MaterialTheme.typography.labelLarge, color = colors.onSurface)
                Spacer(Modifier.height(VxSpace.lg))
                if (state.habits.isEmpty()) {
                    Text(stringResource(Res.string.builder_add_habits_to_see_today), style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
                }
                state.habits.sortedBy { TimeFormat.toMinutes(it.time) ?: Int.MAX_VALUE }.forEach { h ->
                    Row(Modifier.fillMaxWidth().padding(vertical = VxSpace.xs), verticalAlignment = Alignment.CenterVertically) {
                        CheckCircle(CheckState.OPEN, null, h.title)
                        Text(h.title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = colors.onSurface, modifier = Modifier.weight(1f))
                        Text(TimeFormat.display(h.time), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                    }
                }
            }
        }
    }
    if (confirmLeave) {
        ConfirmDialog(
            title = stringResource(Res.string.builder_discard_this_template),
            message = stringResource(Res.string.builder_you_can_save_it_as),
            confirmLabel = stringResource(Res.string.common_discard),
            destructive = true,
            dismissLabel = stringResource(Res.string.common_keep_editing),
            onConfirm = {
                confirmLeave = false
                onBack()
            },
            onDismiss = { confirmLeave = false }
        )
    }
}

@Composable
private fun ReorderableHabits(habits: List<Habit>, onMove: (Int, Int) -> Unit, onEdit: (Int) -> Unit) {
    val colors = LuminaTheme.colors
    var dragging by remember { mutableStateOf<Int?>(null) }
    var offset by remember { mutableStateOf(0f) }
    var itemHeight by remember { mutableStateOf(1) }
    Column(
        Modifier.fillMaxWidth().clip(VxShape.medium).border(1.dp, colors.outlineVariant.copy(alpha = 0.7f), VxShape.medium)
            .background(colors.surface).padding(VxSpace.xs)
    ) {
        if (habits.isEmpty()) {
            Text(
                stringResource(Res.string.templates_no_habits_yet),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant,
                modifier = Modifier.padding(VxSpace.lg)
            )
        }
        habits.forEachIndexed { index, habit ->
            val isDragged = dragging == index
            val accent = habitAccent(habit.color, colors.isDark)
            Row(
                Modifier.fillMaxWidth()
                    .onSizeChanged { itemHeight = it.height.coerceAtLeast(1) }
                    .graphicsLayer { translationY = if (isDragged) offset else 0f }
                    .then(if (isDragged) Modifier.shadow(8.dp, VxShape.medium) else Modifier)
                    .padding(vertical = 3.dp)
                    .clip(VxShape.medium)
                    .background(colors.surface)
                    .border(1.dp, colors.outlineVariant.copy(alpha = 0.8f), VxShape.medium)
                    .hapticClickable { onEdit(index) }
                    .semantics {
                        customActions = listOfNotNull(
                            if (index > 0) CustomAccessibilityAction("Move up") { onMove(index, index - 1); true } else null,
                            if (index < habits.lastIndex) CustomAccessibilityAction("Move down") { onMove(index, index + 1); true } else null
                        )
                    }
                    .padding(end = VxSpace.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier.size(width = 36.dp, height = 64.dp)
                        .pointerInput(index, habits.size) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = { dragging = index; offset = 0f },
                                onDragEnd = {
                                    val target = (index + (offset / itemHeight).roundToInt()).coerceIn(0, habits.lastIndex)
                                    dragging = null
                                    offset = 0f
                                    if (target != index) onMove(index, target)
                                },
                                onDragCancel = { dragging = null; offset = 0f },
                                onDrag = { change, amount ->
                                    change.consume()
                                    offset += amount.y
                                }
                            )
                        }
                        .semantics { contentDescription = "Reorder ${habit.title}" },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(VxIcons.Grip, null, tint = colors.onSurfaceVariant, modifier = Modifier.size(18.dp))
                }
                IconBadge(VxIcons.forKey(habit.icon), accent, size = 40.dp)
                Spacer(Modifier.width(VxSpace.md))
                Column(Modifier.weight(1f).padding(vertical = VxSpace.md)) {
                    Text(habit.title, style = MaterialTheme.typography.titleSmall, color = colors.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(TemplateBuilderViewModel.summary(habit), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Icon(VxIcons.ChevronRight, null, tint = colors.onSurfaceVariant, modifier = Modifier.size(18.dp))
            }
        }
    }
    Spacer(Modifier.height(VxSpace.md))
}
