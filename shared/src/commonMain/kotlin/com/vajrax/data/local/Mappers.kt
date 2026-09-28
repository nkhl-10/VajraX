package com.vajrax.data.local

import com.vajrax.core.time.Dates
import com.vajrax.core.time.TimeFormat
import com.vajrax.domain.habit.Habit
import com.vajrax.domain.habit.HabitIconResolver
import com.vajrax.domain.habit.HabitSchedule
import com.vajrax.domain.habit.HabitType
import com.vajrax.domain.habit.Occurrence
import com.vajrax.domain.habit.ScheduleType
import com.vajrax.domain.habit.Tracker
import com.vajrax.domain.habit.TrackerStatus
import com.vajrax.domain.model.ActionStatus
import com.vajrax.domain.model.TrackingMode
import com.vajrax.domain.template.DefaultHabit
import com.vajrax.domain.template.DefaultTemplate
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

internal val DbDispatcher: CoroutineDispatcher = Dispatchers.IO

internal suspend fun <T> io(block: () -> T): T = withContext(DbDispatcher) { block() }

internal fun Boolean.toDb(): Long = if (this) 1L else 0L

internal fun trackingModeOf(raw: String?): TrackingMode =
    TrackingMode.entries.firstOrNull { it.name == raw } ?: TrackingMode.MANUAL

internal fun PracticeEntity.toHabit(): Habit = Habit(
    id = id,
    trackerId = trackerId,
    title = title,
    category = category,
    icon = icon,
    color = color,
    type = HabitType.of(habitType),
    targetValue = targetValue,
    unit = unit,
    time = TimeFormat.normalize(preferredTime),
    durationMinutes = targetDurationMinutes.toInt(),
    minimumMinutes = minimumDurationMinutes.toInt(),
    trackingMode = trackingModeOf(trackingMode),
    schedule = HabitSchedule(
        type = ScheduleType.of(scheduleType),
        days = HabitSchedule.decodeDays(scheduleDays),
        weeklyTarget = weeklyTarget.toInt().coerceAtLeast(1),
        intervalDays = intervalDays.toInt().coerceAtLeast(1)
    ),
    reminderEnabled = reminderEnabled == 1L,
    reminderTime = TimeFormat.normalize(reminderTime),
    startDate = Dates.parse(startDate),
    archivedAt = Dates.parse(archivedAt),
    sortOrder = sortOrder.toInt()
)

internal fun ActionRecordEntity.toOccurrence(): Occurrence = Occurrence(
    id = id,
    habitId = practiceId,
    date = Dates.parse(date) ?: kotlinx.datetime.LocalDate(1970, 1, 1),
    scheduledTime = TimeFormat.normalize(scheduledTime),
    status = ActionStatus.of(status),
    completedAt = completedAt,
    durationMinutes = durationMinutes?.toInt(),
    value = value_,
    note = note,
    skipReason = skipReason
)

internal fun UserTemplateEntity.toTracker(): Tracker = Tracker(
    id = id,
    templateId = templateId,
    name = name ?: "My routine",
    startDate = Dates.parse(startDate) ?: kotlinx.datetime.LocalDate(1970, 1, 1),
    totalDays = totalDays.toInt(),
    isActive = isActive == 1L,
    status = if (status == TrackerStatus.ARCHIVED.name) TrackerStatus.ARCHIVED else TrackerStatus.ACTIVE,
    endedAt = Dates.parse(endedAt)
)

internal fun TemplateHabitEntity.toDefaultHabit(): DefaultHabit = DefaultHabit(
    name = name,
    startTime = TimeFormat.normalize(startTime) ?: startTime,
    duration = durationMinutes.toInt(),
    trackingType = trackingModeOf(trackingMode),
    target = target,
    repeatDays = repeatDays.split(",").mapNotNull { it.trim().toIntOrNull() },
    reminderEnabled = reminderEnabled == 1L,
    sortOrder = sortOrder.toInt(),
    habitType = HabitType.of(habitType),
    targetValue = targetValue,
    unit = unit,
    category = category,
    icon = icon,
    color = color,
    scheduleType = ScheduleType.of(scheduleType),
    weeklyTarget = weeklyTarget.toInt(),
    intervalDays = intervalDays.toInt()
)

internal fun TemplateEntity.toDefaultTemplate(habits: List<DefaultHabit>): DefaultTemplate = DefaultTemplate(
    id = id,
    name = title,
    category = category,
    description = description,
    difficulty = "Medium",
    estimatedDuration = frequency,
    habits = habits,
    isCommunity = isCommunity == 1L,
    author = author,
    isCustom = isCustom == 1L,
    isDraft = isDraft == 1L,
    frequencyLabel = frequency,
    durationDays = durationDays.toInt(),
    recommendedFor = recommendedFor ?: ""
)
