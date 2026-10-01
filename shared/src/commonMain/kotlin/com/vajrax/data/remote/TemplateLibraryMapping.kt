package com.vajrax.data.remote

import com.vajrax.contract.sync.TemplateHabitPayload
import com.vajrax.contract.templates.LibraryTemplate
import com.vajrax.domain.habit.HabitType
import com.vajrax.domain.habit.ScheduleType
import com.vajrax.domain.model.TrackingMode
import com.vajrax.domain.template.DefaultHabit
import com.vajrax.domain.template.DefaultTemplate

/** The app's template model ↔ the API's library template (same fields the local tables store). */
fun DefaultTemplate.toLibraryTemplate(): LibraryTemplate = LibraryTemplate(
    id = id,
    title = name,
    description = description,
    category = category,
    frequency = frequencyLabel,
    author = author,
    isCommunity = isCommunity,
    durationDays = durationDays,
    recommendedFor = recommendedFor.ifBlank { null },
    habits = habits.mapIndexed { index, habit -> habit.toPayload(index) }
)

fun LibraryTemplate.toDefaultTemplate(): DefaultTemplate = DefaultTemplate(
    id = id,
    name = title,
    category = category,
    description = description,
    difficulty = "Medium",
    estimatedDuration = frequency,
    habits = habits.sortedBy { it.sortOrder }.map { it.toDefaultHabit() },
    isCommunity = isCommunity,
    author = author,
    frequencyLabel = frequency,
    durationDays = durationDays,
    recommendedFor = recommendedFor.orEmpty()
)

fun DefaultHabit.toPayload(index: Int = sortOrder): TemplateHabitPayload = TemplateHabitPayload(
    name = name,
    startTime = startTime,
    durationMinutes = duration,
    trackingMode = trackingType.name,
    target = target,
    repeatDays = repeatDays.joinToString(","),
    reminderEnabled = reminderEnabled,
    sortOrder = index,
    category = category,
    icon = icon,
    color = color,
    habitType = habitType.name,
    targetValue = targetValue,
    unit = unit,
    scheduleType = scheduleType.name,
    weeklyTarget = weeklyTarget,
    intervalDays = intervalDays
)

fun TemplateHabitPayload.toDefaultHabit(): DefaultHabit = DefaultHabit(
    name = name,
    startTime = startTime,
    duration = durationMinutes,
    trackingType = TrackingMode.entries.firstOrNull { it.name == trackingMode } ?: TrackingMode.MANUAL,
    target = target,
    repeatDays = repeatDays.split(",").mapNotNull { it.trim().toIntOrNull() },
    reminderEnabled = reminderEnabled,
    sortOrder = sortOrder,
    habitType = HabitType.of(habitType),
    targetValue = targetValue,
    unit = unit,
    category = category,
    icon = icon,
    color = color,
    scheduleType = ScheduleType.of(scheduleType),
    weeklyTarget = weeklyTarget,
    intervalDays = intervalDays
)
