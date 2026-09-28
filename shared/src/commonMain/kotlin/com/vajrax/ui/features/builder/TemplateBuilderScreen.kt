package com.vajrax.ui.features.builder

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vajrax.core.time.TimeFormat
import com.vajrax.domain.habit.Habit
import com.vajrax.domain.usecase.RoutineManager
import com.vajrax.ui.designsystem.*
import com.vajrax.ui.features.templates.BottomBar
import com.vajrax.ui.features.templates.DashedAddBox
import com.vajrax.ui.theme.LuminaTheme
import com.vajrax.ui.theme.VxShape
import com.vajrax.ui.theme.VxSpace
import com.vajrax.ui.theme.habitAccent
import kotlinx.coroutines.flow.Flow
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
            title = if (state.isEditing) "Edit template" else "New template",
            onBack = leave,
            trailing = { TonalAction("Preview", VxIcons.Eye, onClick = { preview = true }) }
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
                Text("MAKE IT YOURS", style = MaterialTheme.typography.labelSmall, color = colors.primary)
            }
            Spacer(Modifier.height(VxSpace.xs))
            Text("Build a reusable template", style = MaterialTheme.typography.headlineMedium, color = colors.onSurface)
            Spacer(Modifier.height(VxSpace.xs))
            Text(
                "Turn a repeatable routine into a clear, guided check-in you can use anytime.",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant
            )

            Spacer(Modifier.height(VxSpace.xl))
            SectionLabel("Template details")
            Spacer(Modifier.height(VxSpace.sm))
            VxCard {
                VxTextField(
                    label = "Template name",
                    value = state.name,
                    onValueChange = { onIntent(BuilderIntent.SetName(it)) },
                    placeholder = "Weekly planning ritual",
                    helper = "Shown in your template library",
                    error = state.nameError,
                    maxChars = TemplateBuilderViewModel.NAME_MAX
                )
                Spacer(Modifier.height(VxSpace.lg))
                VxTextField(
                    label = "Description",
                    value = state.description,
                    onValueChange = { onIntent(BuilderIntent.SetDescription(it)) },
                    placeholder = "What is this routine for?",
                    singleLine = false,
                    minLines = 3,
                    maxChars = TemplateBuilderViewModel.DESCRIPTION_MAX
                )
            }

            Spacer(Modifier.height(VxSpace.xl))
            SectionLabel("Category")
            Text("Choose where this template belongs.", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            Spacer(Modifier.height(VxSpace.sm))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(VxSpace.sm), verticalArrangement = Arrangement.spacedBy(VxSpace.sm)) {
                TemplateBuilderViewModel.categories.forEach { c ->
                    CategoryChip(c, state.category == c, onClick = { onIntent(BuilderIntent.SetCategory(c)) })
                }
            }

            Spacer(Modifier.height(VxSpace.xl))
            SectionLabel("Template structure") {
                Text("${state.habits.size} ${if (state.habits.size == 1) "habit" else "habits"}", style = MaterialTheme.typography.labelMedium, color = colors.primary)
            }
            Text("Long-press the handle to reorder. Tap a habit to edit it.", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
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
                title = "Shape the next step",
                subtitle = "Add a check, count, minutes or value habit.",
                buttonLabel = "Add a habit",
                onClick = { adding = true }
            )

            Spacer(Modifier.height(VxSpace.lg))
            VxCard(onClick = { preview = true }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconBadge(VxIcons.Smartphone, colors.primary, size = 44.dp)
                    Spacer(Modifier.width(VxSpace.md))
                    Column(Modifier.weight(1f)) {
                        Text("Preview your template", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = colors.onSurface)
                        Text("See how this will feel before you publish.", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                    }
                    Icon(VxIcons.ArrowRight, null, tint = colors.primary, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(Modifier.height(VxSpace.xl))
        }
        BottomBar {
            Row(horizontalArrangement = Arrangement.spacedBy(VxSpace.md)) {
                SecondaryButton("Save draft", { onIntent(BuilderIntent.SaveDraft) }, Modifier.weight(1f), enabled = !state.isSaving)
                PrimaryButton(if (state.isEditing) "Save template" else "Create template", { onIntent(BuilderIntent.Create) }, Modifier.weight(1f), loading = state.isSaving)
            }
            TextButton(onClick = leave, modifier = Modifier.fillMaxWidth()) {
                Text("Cancel", color = colors.onSurfaceVariant, style = MaterialTheme.typography.labelLarge)
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
                onIntent(BuilderIntent.AddHabit(it))
                adding = false
            }
        )
    }
    if (preview) {
        ModalBottomSheet(onDismissRequest = { preview = false }, sheetState = rememberModalBottomSheetState(true), containerColor = colors.background) {
            Column(Modifier.verticalScroll(rememberScrollState()).padding(horizontal = VxSpace.gutter).padding(bottom = VxSpace.xxxl)) {
                Text("PREVIEW", style = MaterialTheme.typography.labelSmall, color = colors.primary)
                Spacer(Modifier.height(VxSpace.xs))
                Text(state.name.ifBlank { "Untitled template" }, style = MaterialTheme.typography.headlineSmall, color = colors.onSurface)
                Text(state.description.ifBlank { "No description yet." }, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
                Spacer(Modifier.height(VxSpace.md))
                Text("${state.habits.size} habits · ${TimeFormat.duration(state.dailyMinutes)} a day · ${state.category}", style = MaterialTheme.typography.labelLarge, color = colors.onSurface)
                Spacer(Modifier.height(VxSpace.lg))
                if (state.habits.isEmpty()) {
                    Text("Add habits to see today's plan.", style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
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
            title = "Discard this template?",
            message = "You can save it as a draft and finish later.",
            confirmLabel = "Discard",
            destructive = true,
            dismissLabel = "Keep editing",
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
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).border(1.dp, colors.outlineVariant.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
            .background(colors.surface).padding(VxSpace.xs)
    ) {
        if (habits.isEmpty()) {
            Text(
                "No habits yet.",
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
                    .clickable(role = Role.Button) { onEdit(index) }
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
