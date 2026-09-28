package com.vajrax.ui.features.today

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
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
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

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, containerColor = colors.surface) {
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
                    Text("Skip today?", style = MaterialTheme.typography.titleMedium, color = colors.onSurface)
                    Text(
                        "Reason (optional)",
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
                            Text("Do the minimum instead", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = colors.onSurface)
                            Text(
                                "${TimeFormat.duration(habit.minimumMinutes)} · counts as done",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(Modifier.height(VxSpace.xl))
                    PrimaryButton("Skip today", { act(TodayIntent.Skip(item.id, reason?.label)) }, Modifier.fillMaxWidth())
                    TextButton(onClick = { mode = "main" }, modifier = Modifier.fillMaxWidth()) { Text("Back") }
                }
                "note" -> {
                    VxTextField(
                        label = "Note for today",
                        value = note,
                        onValueChange = { note = it },
                        placeholder = "How did it go?",
                        singleLine = false,
                        minLines = 3,
                        maxChars = 500
                    )
                    Spacer(Modifier.height(VxSpace.lg))
                    PrimaryButton("Save note", { act(TodayIntent.SaveNote(item.id, note)) }, Modifier.fillMaxWidth())
                    TextButton(onClick = { mode = "main" }, modifier = Modifier.fillMaxWidth()) { Text("Back") }
                }
                else -> {
                    if (isOpen) {
                        if (habit.type == HabitType.BOOLEAN) {
                            PrimaryButton("Mark as done", { act(TodayIntent.Complete(item.id)) }, Modifier.fillMaxWidth(), icon = VxIcons.Check)
                        } else {
                            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(VxSpace.md)) {
                                VxTextField(
                                    label = "Today's ${habit.unit ?: "value"} (target ${formatValue(habit.targetValue)})",
                                    value = valueText,
                                    onValueChange = { valueText = it.filter { c -> c.isDigit() || c == '.' }.take(9) },
                                    keyboardType = KeyboardType.Decimal,
                                    modifier = Modifier.weight(1f)
                                )
                                PrimaryButton("Save", {
                                    valueText.toDoubleOrNull()?.let { act(TodayIntent.RecordValue(item.id, it)) }
                                }, enabled = valueText.toDoubleOrNull() != null)
                            }
                            Spacer(Modifier.height(VxSpace.sm))
                            SecondaryButton("Reached the target", { act(TodayIntent.Complete(item.id)) }, Modifier.fillMaxWidth(), icon = VxIcons.Check)
                        }
                        Spacer(Modifier.height(VxSpace.md))
                        if (habit.trackingMode == TrackingMode.TIMER) {
                            SheetAction(VxIcons.Timer, "Start focus timer") { act(TodayIntent.StartTimer(item.id)) }
                        }
                        if (habit.durationMinutes >= 10) {
                            SheetAction(VxIcons.Zap, "Do the minimum (${TimeFormat.duration(habit.minimumMinutes)})") { act(TodayIntent.CompleteMinimum(item.id)) }
                        }
                        SheetAction(VxIcons.Alarm, "Snooze 15 minutes") { act(TodayIntent.Snooze(item.id)) }
                        SheetAction(VxIcons.Clock, "Move to another time today") { pickTime = true }
                        SheetAction(VxIcons.SkipForward, "Skip today") { mode = "skip" }
                    } else {
                        SecondaryButton("Undo — mark as not done", { act(TodayIntent.Reopen(item.id)) }, Modifier.fillMaxWidth(), icon = VxIcons.Undo)
                        Spacer(Modifier.height(VxSpace.md))
                    }
                    SheetAction(VxIcons.Note, if (occ.note.isNullOrBlank()) "Add a note" else "Edit note") { mode = "note" }
                    SheetAction(VxIcons.Chart, "Habit details & history") { onOpenDetails() }
                }
            }
        }
    }

    if (pickTime) {
        TimePickerDialog(occ.scheduledTime ?: habit.time, title = "Move today's ${habit.title}", onDismiss = { pickTime = false }, onConfirm = {
            pickTime = false
            act(TodayIntent.Move(item.id, it))
        })
    }
}

@Composable
private fun SheetAction(icon: ImageVector, label: String, tint: Color? = null, onClick: () -> Unit) {
    val colors = LuminaTheme.colors
    Row(
        Modifier.fillMaxWidth().heightIn(min = 52.dp).clickable(role = Role.Button, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = tint ?: colors.onSurfaceVariant, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(VxSpace.lg))
        Text(label, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium), color = tint ?: colors.onSurface, modifier = Modifier.weight(1f))
        Icon(VxIcons.ChevronRight, null, tint = colors.onSurfaceVariant, modifier = Modifier.size(16.dp))
    }
}
