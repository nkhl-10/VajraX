package com.vajrax.ui.features.today

import org.jetbrains.compose.resources.stringResource
import com.vajrax.resources.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.vajrax.core.time.TimeFormat
import com.vajrax.domain.habit.HabitType
import com.vajrax.domain.habit.SkipReason
import com.vajrax.domain.habit.formatValue
import com.vajrax.domain.model.ActionStatus
import com.vajrax.domain.model.TrackingMode
import com.vajrax.ui.designsystem.*
import com.vajrax.ui.theme.LuminaTheme
import com.vajrax.ui.theme.VxSpace
import com.vajrax.ui.theme.habitAccent

/**
 * Secondary actions for one of today's habits (spec 04 Screen 05): complete / log value,
 * minimum version, timer, snooze, move, skip with an optional reason, note, undo, details.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HabitActionSheet(
    item: TodayItem,
    onDismiss: () -> Unit,
    onIntent: (TodayIntent) -> Unit,
    onOpenDetails: () -> Unit
) {
    val colors = LuminaTheme.colors
    val habit = item.habit
    val occ = item.occurrence
    var mode by remember(item.id) { mutableStateOf("main") }
    var note by remember(item.id) { mutableStateOf(occ.note ?: "") }
    var valueText by remember(item.id) { mutableStateOf(occ.value?.let { formatValue(it) } ?: "") }
    var reason by remember(item.id) { mutableStateOf<SkipReason?>(null) }
    var pickTime by remember { mutableStateOf(false) }
    val isOpen = occ.isOpen
    val accent = habitAccent(habit.color, colors.isDark)

    fun act(intent: TodayIntent) {
        onIntent(intent)
        onDismiss()
    }

    val edited = note != (occ.note ?: "") || valueText != (occ.value?.let { formatValue(it) } ?: "") || reason != null
    VxBottomSheet(
        onDismiss = onDismiss,
        hasUnsavedChanges = edited,
        // Back from "skip" or "note" returns to the main actions instead of closing.
        onStepBack = { if (mode != "main") { mode = "main"; true } else false }
    ) {
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = VxSpace.xxl).padding(bottom = VxSpace.xxxl)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBadge(VxIcons.forKey(habit.icon), accent, size = 44.dp, iconSize = 22.dp)
                Spacer(Modifier.width(VxSpace.md))
                Column(Modifier.weight(1f)) {
                    Text(habit.title, style = MaterialTheme.typography.titleLarge, color = colors.onSurface)
                    Text(
                        listOfNotNull(item.timeLabel, TimeFormat.duration(habit.durationMinutes), habit.targetLabel(), habit.schedule.label()).joinToString(" · "),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(VxSpace.md))
            Text(
                when (occ.status) {
                    ActionStatus.COMPLETE -> "Done today"
                    ActionStatus.MINIMUM -> "Minimum version done today"
                    ActionStatus.SKIPPED -> "Skipped today" + (occ.skipReason?.let { " — $it" } ?: "")
                    ActionStatus.SNOOZED -> "Snoozed to ${TimeFormat.display(occ.scheduledTime)}"
                    else -> item.progressLabel ?: "Not done yet"
                },
                style = MaterialTheme.typography.labelMedium,
                color = colors.primary
            )
            Spacer(Modifier.height(VxSpace.lg))

            when (mode) {
                "skip" -> {
                    Text(stringResource(Res.string.today_skip_today_2), style = MaterialTheme.typography.titleMedium, color = colors.onSurface)
                    Text(
                        stringResource(Res.string.today_reason_optional),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant
                    )
                    Spacer(Modifier.height(VxSpace.md))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(VxSpace.sm), verticalArrangement = Arrangement.spacedBy(VxSpace.sm)) {
                        SkipReason.entries.forEach { r -> CategoryChip(r.label, reason == r, onClick = { reason = if (reason == r) null else r }) }
                    }
                    if (habit.durationMinutes >= 10 && isOpen) {
                        Spacer(Modifier.height(VxSpace.lg))
                        VxCard(onClick = { act(TodayIntent.CompleteMinimum(item.id)) }) {
                            Text(stringResource(Res.string.today_do_the_minimum_instead), style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = colors.onSurface)
                            Text(
                                stringResource(Res.string.today_counts_as_done_fmt, TimeFormat.duration(habit.minimumMinutes)),
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(Modifier.height(VxSpace.xl))
                    PrimaryButton(stringResource(Res.string.today_skip_today), { act(TodayIntent.Skip(item.id, reason?.label)) }, Modifier.fillMaxWidth())
                    TextButton(onClick = { mode = "main" }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(Res.string.today_back)) }
                }
                "note" -> {
                    VxTextField(
                        label = stringResource(Res.string.today_note_for_today),
                        value = note,
                        onValueChange = { note = it },
                        placeholder = stringResource(Res.string.today_how_did_it_go),
                        singleLine = false,
                        minLines = 3,
                        maxChars = 500
                    )
                    Spacer(Modifier.height(VxSpace.lg))
                    PrimaryButton(stringResource(Res.string.today_save_note), { act(TodayIntent.SaveNote(item.id, note)) }, Modifier.fillMaxWidth())
                    TextButton(onClick = { mode = "main" }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(Res.string.today_back)) }
                }
                else -> {
                    if (isOpen) {
                        if (habit.type == HabitType.BOOLEAN) {
                            PrimaryButton(stringResource(Res.string.today_mark_as_done), { act(TodayIntent.Complete(item.id)) }, Modifier.fillMaxWidth(), icon = VxIcons.Check)
                        } else {
                            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(VxSpace.md)) {
                                VxTextField(
                                    label = stringResource(
                                        Res.string.today_value_field_fmt,
                                        habit.unit ?: stringResource(Res.string.today_value_word),
                                        formatValue(habit.targetValue)
                                    ),
                                    value = valueText,
                                    onValueChange = { valueText = it.filter { c -> c.isDigit() || c == '.' }.take(9) },
                                    keyboardType = KeyboardType.Decimal,
                                    modifier = Modifier.weight(1f)
                                )
                                PrimaryButton(stringResource(Res.string.today_save), {
                                    valueText.toDoubleOrNull()?.let { act(TodayIntent.RecordValue(item.id, it)) }
                                }, enabled = valueText.toDoubleOrNull() != null)
                            }
                            Spacer(Modifier.height(VxSpace.sm))
                            SecondaryButton(stringResource(Res.string.today_reached_the_target), { act(TodayIntent.Complete(item.id)) }, Modifier.fillMaxWidth(), icon = VxIcons.Check)
                        }
                        Spacer(Modifier.height(VxSpace.md))
                        if (habit.trackingMode == TrackingMode.TIMER) {
                            SheetAction(VxIcons.Timer, stringResource(Res.string.today_start_focus_timer)) { act(TodayIntent.StartTimer(item.id)) }
                        }
                        if (habit.durationMinutes >= 10) {
                            SheetAction(VxIcons.Zap, stringResource(Res.string.today_do_the_minimum_fmt, TimeFormat.duration(habit.minimumMinutes))) { act(TodayIntent.CompleteMinimum(item.id)) }
                        }
                        SheetAction(VxIcons.Alarm, stringResource(Res.string.today_snooze_minutes_fmt, com.vajrax.domain.BusinessRules.SNOOZE_MINUTES)) { act(TodayIntent.Snooze(item.id)) }
                        SheetAction(VxIcons.Clock, stringResource(Res.string.today_move_to_another_time_today)) { pickTime = true }
                        SheetAction(VxIcons.SkipForward, stringResource(Res.string.today_skip_today)) { mode = "skip" }
                    } else {
                        SecondaryButton(stringResource(Res.string.today_undo_mark_as_not_done), { act(TodayIntent.Reopen(item.id)) }, Modifier.fillMaxWidth(), icon = VxIcons.Undo)
                        Spacer(Modifier.height(VxSpace.md))
                    }
                    SheetAction(VxIcons.Note, if (occ.note.isNullOrBlank()) stringResource(Res.string.today_add_a_note) else stringResource(Res.string.today_edit_note)) { mode = "note" }
                    SheetAction(VxIcons.Chart, stringResource(Res.string.today_habit_details_history)) { onOpenDetails() }
                }
            }
        }
    }

    if (pickTime) {
        TimePickerDialog(occ.scheduledTime ?: habit.time, title = stringResource(Res.string.today_move_today_s_fmt, habit.title), onDismiss = { pickTime = false }, onConfirm = {
            pickTime = false
            act(TodayIntent.Move(item.id, it))
        })
    }
}

@Composable
private fun SheetAction(icon: ImageVector, label: String, tint: Color? = null, onClick: () -> Unit) {
    val colors = LuminaTheme.colors
    val haptics = rememberHaptics()
    Row(
        Modifier.fillMaxWidth().heightIn(min = 52.dp).clickable(role = Role.Button) { haptics(VxHaptic.Tap); onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = tint ?: colors.onSurfaceVariant, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(VxSpace.lg))
        Text(label, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium), color = tint ?: colors.onSurface, modifier = Modifier.weight(1f))
        Icon(VxIcons.ChevronRight, null, tint = colors.onSurfaceVariant, modifier = Modifier.size(16.dp))
    }
}
