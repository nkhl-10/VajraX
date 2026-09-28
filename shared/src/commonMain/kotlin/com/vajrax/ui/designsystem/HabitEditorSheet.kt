package com.vajrax.ui.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.vajrax.core.time.TimeFormat
import com.vajrax.domain.habit.Habit
import com.vajrax.domain.habit.HabitIconResolver
import com.vajrax.domain.habit.HabitSchedule
import com.vajrax.domain.habit.HabitType
import com.vajrax.domain.habit.ScheduleType
import com.vajrax.domain.habit.formatValue
import com.vajrax.domain.model.TrackingMode
import com.vajrax.ui.theme.LuminaTheme
import com.vajrax.ui.theme.VxShape
import com.vajrax.ui.theme.VxSpace
import com.vajrax.ui.theme.habitAccent

/**
 * Create / edit a habit: name, icon, color, category, tracking type + target, time, duration,
 * schedule (daily, selected days, weekly target, interval) and reminder.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HabitEditorSheet(
    initial: Habit,
    isNew: Boolean,
    onDismiss: () -> Unit,
    onSave: (Habit) -> Unit,
    onRemove: (() -> Unit)? = null,
    removeLabel: String = "Archive habit",
    errorMessage: String? = null
) {
    val colors = LuminaTheme.colors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var draft by remember(initial.id) { mutableStateOf(initial) }
    var targetText by remember(initial.id) { mutableStateOf(formatValue(initial.targetValue)) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showReminderPicker by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var targetError by remember { mutableStateOf<String?>(null) }
    var confirmRemove by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, containerColor = colors.surface) {
        Column(
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
                .padding(horizontal = VxSpace.xxl).padding(bottom = VxSpace.xxxl)
        ) {
            Text(if (isNew) "New habit" else "Edit habit", style = MaterialTheme.typography.headlineSmall, color = colors.onSurface)
            Spacer(Modifier.height(VxSpace.xl))

            VxTextField(
                label = "Habit name",
                value = draft.title,
                onValueChange = {
                    draft = draft.copy(title = it)
                    nameError = null
                },
                placeholder = "e.g. Read 10 pages",
                error = nameError,
                maxChars = 60
            )

            Spacer(Modifier.height(VxSpace.xl))
            Text("Icon & color", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium), color = colors.onSurface)
            Spacer(Modifier.height(VxSpace.sm))
            val accent = habitAccent(draft.color, colors.isDark)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(VxSpace.xs), verticalArrangement = Arrangement.spacedBy(VxSpace.xs)) {
                HabitIconResolver.icons.forEach { key ->
                    val selected = draft.icon == key
                    Box(
                        modifier = Modifier.size(48.dp).clip(VxShape.small)
                            .background(if (selected) accent.copy(alpha = 0.16f) else colors.surfaceDim)
                            .border(if (selected) 1.5.dp else 0.dp, if (selected) accent else colors.surfaceDim, VxShape.small)
                            .selectable(selected, role = Role.RadioButton) { draft = draft.copy(icon = key) }
                            .semantics { contentDescription = "Icon $key" },
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.material3.Icon(VxIcons.forKey(key), null, tint = if (selected) accent else colors.onSurfaceVariant, modifier = Modifier.size(20.dp))
                    }
                }
            }
            Spacer(Modifier.height(VxSpace.md))
            Row(horizontalArrangement = Arrangement.spacedBy(VxSpace.xs)) {
                HabitIconResolver.colors.forEach { key ->
                    val c = habitAccent(key, colors.isDark)
                    val selected = draft.color == key
                    Box(
                        modifier = Modifier.size(36.dp).clip(CircleShape)
                            .border(if (selected) 2.dp else 0.dp, if (selected) colors.onSurface else c, CircleShape)
                            .padding(4.dp).clip(CircleShape).background(c)
                            .selectable(selected, role = Role.RadioButton) { draft = draft.copy(color = key) }
                            .semantics { contentDescription = "Color $key" }
                    )
                }
            }

            Spacer(Modifier.height(VxSpace.xl))
            Text("Category", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium), color = colors.onSurface)
            Spacer(Modifier.height(VxSpace.sm))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(VxSpace.sm), verticalArrangement = Arrangement.spacedBy(VxSpace.sm)) {
                HabitIconResolver.categories.forEach { cat ->
                    CategoryChip(cat, draft.category == cat, onClick = { draft = draft.copy(category = cat) })
                }
            }

            Spacer(Modifier.height(VxSpace.xl))
            Text("How do you track it?", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium), color = colors.onSurface)
            Spacer(Modifier.height(VxSpace.sm))
            val types = listOf(HabitType.BOOLEAN to "Check", HabitType.COUNT to "Count", HabitType.DURATION to "Minutes", HabitType.VALUE to "Value")
            SegmentedToggle(
                options = types.map { it.second },
                selectedIndex = types.indexOfFirst { it.first == draft.type }.coerceAtLeast(0),
                onSelect = { i ->
                    val t = types[i].first
                    val defaultUnit = when (t) {
                        HabitType.COUNT -> "times"
                        HabitType.DURATION -> "min"
                        HabitType.VALUE -> draft.unit ?: ""
                        HabitType.BOOLEAN -> null
                    }
                    val target = when (t) {
                        HabitType.BOOLEAN -> 1.0
                        HabitType.DURATION -> draft.durationMinutes.toDouble()
                        else -> draft.targetValue.coerceAtLeast(1.0)
                    }
                    draft = draft.copy(type = t, unit = defaultUnit, targetValue = target)
                    targetText = formatValue(target)
                }
            )
            if (draft.type != HabitType.BOOLEAN) {
                Spacer(Modifier.height(VxSpace.md))
                Row(horizontalArrangement = Arrangement.spacedBy(VxSpace.md)) {
                    VxTextField(
                        label = "Daily target",
                        value = targetText,
                        onValueChange = { v ->
                            targetText = v.filter { it.isDigit() || it == '.' }.take(8)
                            targetError = null
                        },
                        keyboardType = KeyboardType.Decimal,
                        error = targetError,
                        modifier = Modifier.weight(1f)
                    )
                    VxTextField(
                        label = "Unit",
                        value = draft.unit ?: "",
                        onValueChange = { draft = draft.copy(unit = it.take(16)) },
                        placeholder = if (draft.type == HabitType.COUNT) "glasses" else "pages",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.height(VxSpace.xl))
            TimeField("Time", draft.time, onPick = { showTimePicker = true })

            Spacer(Modifier.height(VxSpace.xl))
            Text("Duration", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium), color = colors.onSurface)
            Spacer(Modifier.height(VxSpace.sm))
            Stepper(
                value = TimeFormat.duration(draft.durationMinutes),
                onMinus = {
                    val m = (draft.durationMinutes - if (draft.durationMinutes > 60) 15 else 5).coerceAtLeast(5)
                    draft = draft.copy(durationMinutes = m, minimumMinutes = (m / 3).coerceAtLeast(1))
                },
                onPlus = {
                    val m = (draft.durationMinutes + if (draft.durationMinutes >= 60) 15 else 5).coerceAtMost(720)
                    draft = draft.copy(durationMinutes = m, minimumMinutes = (m / 3).coerceAtLeast(1))
                },
                label = "Duration"
            )
            Spacer(Modifier.height(VxSpace.sm))
            SwitchRow(
                title = "Use focus timer",
                subtitle = "Start a timer from Today; finishing early still counts as the minimum version.",
                checked = draft.trackingMode == TrackingMode.TIMER,
                onChange = { draft = draft.copy(trackingMode = if (it) TrackingMode.TIMER else TrackingMode.MANUAL) }
            )

            Spacer(Modifier.height(VxSpace.xl))
            Text("Repeat", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium), color = colors.onSurface)
            Spacer(Modifier.height(VxSpace.sm))
            val schedules = listOf(ScheduleType.DAILY to "Daily", ScheduleType.WEEKDAYS to "Days", ScheduleType.WEEKLY_TARGET to "Weekly", ScheduleType.INTERVAL to "Interval")
            SegmentedToggle(
                options = schedules.map { it.second },
                selectedIndex = schedules.indexOfFirst { it.first == draft.schedule.type }.coerceAtLeast(0),
                onSelect = { i -> draft = draft.copy(schedule = draft.schedule.copy(type = schedules[i].first)) }
            )
            Spacer(Modifier.height(VxSpace.md))
            when (draft.schedule.type) {
                ScheduleType.WEEKDAYS -> DayPicker(draft.schedule.days) { days -> draft = draft.copy(schedule = draft.schedule.copy(days = days)) }
                ScheduleType.WEEKLY_TARGET -> Stepper(
                    value = "${draft.schedule.weeklyTarget}× per week",
                    onMinus = { draft = draft.copy(schedule = draft.schedule.copy(weeklyTarget = (draft.schedule.weeklyTarget - 1).coerceAtLeast(1))) },
                    onPlus = { draft = draft.copy(schedule = draft.schedule.copy(weeklyTarget = (draft.schedule.weeklyTarget + 1).coerceAtMost(7))) },
                    label = "Times per week"
                )
                ScheduleType.INTERVAL -> Stepper(
                    value = "Every ${draft.schedule.intervalDays} days",
                    onMinus = { draft = draft.copy(schedule = draft.schedule.copy(intervalDays = (draft.schedule.intervalDays - 1).coerceAtLeast(2))) },
                    onPlus = { draft = draft.copy(schedule = draft.schedule.copy(intervalDays = (draft.schedule.intervalDays + 1).coerceAtMost(30))) },
                    label = "Interval"
                )
                ScheduleType.DAILY -> Text("Scheduled every day.", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            }
            val restNote = when (draft.schedule.type) {
                ScheduleType.WEEKDAYS -> "Unselected days are rest days and never count as missed."
                ScheduleType.WEEKLY_TARGET -> "Do it on any days you like; progress is checked at the end of each week."
                ScheduleType.INTERVAL -> "Counted from the start date."
                else -> null
            }
            if (restNote != null) {
                Spacer(Modifier.height(VxSpace.xs))
                Text(restNote, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            }

            Spacer(Modifier.height(VxSpace.xl))
            SwitchRow(
                title = "Reminder",
                subtitle = if (draft.reminderEnabled) "At ${TimeFormat.display(draft.reminderTime ?: draft.time)}" else "Off",
                checked = draft.reminderEnabled,
                onChange = { draft = draft.copy(reminderEnabled = it, reminderTime = draft.reminderTime ?: draft.time) }
            )
            if (draft.reminderEnabled) {
                TextButton(onClick = { showReminderPicker = true }) { Text("Change reminder time") }
            }

            if (errorMessage != null) {
                Spacer(Modifier.height(VxSpace.md))
                Text(errorMessage, style = MaterialTheme.typography.bodyMedium, color = colors.statusError)
            }

            Spacer(Modifier.height(VxSpace.xxl))
            PrimaryButton(
                text = if (isNew) "Add habit" else "Save changes",
                onClick = {
                    val name = draft.title.trim()
                    val target = targetText.toDoubleOrNull()
                    when {
                        name.isEmpty() -> nameError = "Give the habit a name."
                        draft.type != HabitType.BOOLEAN && (target == null || target <= 0) -> targetError = "Enter a number above 0."
                        else -> onSave(
                            draft.copy(
                                title = name,
                                targetValue = if (draft.type == HabitType.BOOLEAN) 1.0 else target!!,
                                unit = draft.unit?.trim()?.ifBlank { null },
                                schedule = if (draft.schedule.type == ScheduleType.WEEKDAYS && draft.schedule.days.isEmpty())
                                    draft.schedule.copy(days = HabitSchedule.ALL_DAYS) else draft.schedule
                            )
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            if (onRemove != null) {
                Spacer(Modifier.height(VxSpace.sm))
                SecondaryButton(removeLabel, onClick = { confirmRemove = true }, modifier = Modifier.fillMaxWidth(), destructive = true, icon = VxIcons.Archive)
            }
        }
    }

    if (showTimePicker) {
        TimePickerDialog(draft.time, onDismiss = { showTimePicker = false }, onConfirm = { t ->
            val followReminder = draft.reminderTime == null || draft.reminderTime == draft.time
            draft = draft.copy(time = t, reminderTime = if (followReminder) t else draft.reminderTime)
            showTimePicker = false
        })
    }
    if (showReminderPicker) {
        TimePickerDialog(draft.reminderTime ?: draft.time, title = "Reminder time", onDismiss = { showReminderPicker = false }, onConfirm = { t ->
            draft = draft.copy(reminderTime = t)
            showReminderPicker = false
        })
    }
    if (confirmRemove && onRemove != null) {
        ConfirmDialog(
            title = "$removeLabel?",
            message = "It won't be scheduled from today. Everything you've already recorded stays in your history.",
            confirmLabel = removeLabel,
            destructive = true,
            onConfirm = {
                confirmRemove = false
                onRemove()
            },
            onDismiss = { confirmRemove = false }
        )
    }
}

@Composable
fun Stepper(value: String, onMinus: () -> Unit, onPlus: () -> Unit, label: String, modifier: Modifier = Modifier) {
    val colors = LuminaTheme.colors
    Row(
        modifier = modifier.fillMaxWidth().clip(androidx.compose.foundation.shape.RoundedCornerShape(14.dp))
            .border(1.dp, colors.outlineVariant, androidx.compose.foundation.shape.RoundedCornerShape(14.dp)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onMinus, modifier = Modifier.semantics { contentDescription = "Decrease $label" }) {
            Icon(VxIcons.Minus, null, tint = colors.primary)
        }
        Text(value, style = MaterialTheme.typography.titleSmall, color = colors.onSurface, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        IconButton(onClick = onPlus, modifier = Modifier.semantics { contentDescription = "Increase $label" }) {
            Icon(VxIcons.Plus, null, tint = colors.primary)
        }
    }
}

@Composable
fun DayPicker(days: Set<Int>, onChange: (Set<Int>) -> Unit) {
    val colors = LuminaTheme.colors
    val names = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        (1..7).forEach { d ->
            val selected = d in days
            Box(
                modifier = Modifier.size(44.dp).clip(CircleShape)
                    .background(if (selected) colors.primary else colors.surfaceDim)
                    .selectable(selected, role = Role.Checkbox) {
                        val next = if (selected) days - d else days + d
                        if (next.isNotEmpty()) onChange(next)
                    }
                    .semantics { contentDescription = names[d - 1] },
                contentAlignment = Alignment.Center
            ) {
                Text(HabitSchedule.DAY_LETTERS[d - 1], style = MaterialTheme.typography.labelLarge, color = if (selected) colors.onPrimary else colors.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun SwitchRow(title: String, subtitle: String?, checked: Boolean, onChange: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    val colors = LuminaTheme.colors
    Row(
        modifier = modifier.fillMaxWidth().heightIn(min = 56.dp)
            .selectable(checked, role = Role.Switch) { onChange(!checked) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium), color = colors.onSurface)
            if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
        }
        Spacer(Modifier.width(VxSpace.md))
        Switch(
            checked = checked,
            onCheckedChange = null,
            colors = SwitchDefaults.colors(checkedTrackColor = colors.primary, checkedThumbColor = colors.onPrimary)
        )
    }
}
