package com.vajrax.domain

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

enum class TaskCategory { SCHOLAR, DEEP_WORK, HEALTH, MAINTENANCE }

data class DailyTask(
    val id: String,
    val title: String,
    val isCompleted: Boolean,
    val scheduledDate: LocalDate,
    val durationMinutes: Int,
    val category: TaskCategory
)

// Represents a single day in the 'Pure Schedule Matrix'
data class CalendarDay(
    val date: LocalDate,
    val dayOfWeek: DayOfWeek,
    val dayOfMonth: Int,
    val tasks: List<DailyTask>,
    val isToday: Boolean
) {
    // Helper for UI formatting (e.g., 'Mon', 'Tue')
    val shortWeekDay: String get() = dayOfWeek.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
}

// Represents the 5-day work week (Mon-Fri)
data class WeekSchedule(
    val days: List<CalendarDay>
)
