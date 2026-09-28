package com.vajrax.domain.template

import com.vajrax.core.time.TimeFormat
import com.vajrax.domain.habit.Habit
import com.vajrax.domain.habit.HabitIconResolver
import com.vajrax.domain.habit.HabitSchedule
import com.vajrax.domain.habit.HabitType
import com.vajrax.domain.habit.ScheduleType

/** Builds the user-owned habit copy of a template habit (never linked back to the template). */
fun DefaultHabit.toHabitCopy(id: String, trackerId: String?, order: Int): Habit {
    val look = HabitIconResolver.resolve(name)
    val days = repeatDays.filter { it in 1..7 }.toSet()
    val schedule = when {
        scheduleType == ScheduleType.WEEKLY_TARGET -> HabitSchedule(ScheduleType.WEEKLY_TARGET, weeklyTarget = weeklyTarget.coerceAtLeast(1))
        scheduleType == ScheduleType.INTERVAL -> HabitSchedule(ScheduleType.INTERVAL, intervalDays = intervalDays.coerceAtLeast(1))
        days.isNotEmpty() && days != HabitSchedule.ALL_DAYS -> HabitSchedule(ScheduleType.WEEKDAYS, days = days)
        else -> HabitSchedule(ScheduleType.DAILY)
    }
    val minutes = duration.coerceAtLeast(1)
    return Habit(
        id = id,
        trackerId = trackerId,
        title = name.trim(),
        category = category ?: look.category,
        icon = icon ?: look.icon,
        color = color ?: look.color,
        type = habitType,
        targetValue = if (habitType == HabitType.BOOLEAN) 1.0 else targetValue.coerceAtLeast(1.0),
        unit = unit,
        time = TimeFormat.normalize(startTime),
        durationMinutes = minutes,
        minimumMinutes = (minutes / 3).coerceAtLeast(1),
        trackingMode = trackingType,
        schedule = schedule,
        reminderEnabled = false,
        reminderTime = TimeFormat.normalize(startTime),
        sortOrder = order
    )
}

/** Converts a habit back into a template habit (used by the template builder). */
fun Habit.toTemplateHabit(order: Int): DefaultHabit = DefaultHabit(
    name = title,
    startTime = time ?: "09:00",
    duration = durationMinutes,
    trackingType = trackingMode,
    target = targetValue.toString(),
    repeatDays = if (schedule.type == ScheduleType.WEEKDAYS) schedule.days.sorted() else listOf(1, 2, 3, 4, 5, 6, 7),
    reminderEnabled = reminderEnabled,
    sortOrder = order,
    habitType = type,
    targetValue = targetValue,
    unit = unit,
    category = category,
    icon = icon,
    color = color,
    scheduleType = schedule.type,
    weeklyTarget = schedule.weeklyTarget,
    intervalDays = schedule.intervalDays
)
