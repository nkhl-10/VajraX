package com.vajrax.ui.features.calendar

import androidx.compose.runtime.Immutable

@Immutable
data class CalendarMatrixRow(
    val id: String,
    val title: String,
    val timeSubtitle: String,
    val iconEmoji: String,
    val mondayCompleted: Boolean,
    val tuesdayCompleted: Boolean,
    val wednesdayCompleted: Boolean,
    val thursdayCompleted: Boolean,
    val fridayCompleted: Boolean
)

@Immutable
data class CalendarUiState(
    val selectedDateText: String = "Wed, 21 Jan",
    val selectedDayOfWeek: Int = 2, // 0: Mon, 1: Tue, 2: Wed, 3: Thu, 4: Fri
    val days: List<DayTab> = listOf(
        DayTab(19, "Mon", 0),
        DayTab(20, "Tue", 1),
        DayTab(21, "Wed", 2),
        DayTab(22, "Thu", 3),
        DayTab(23, "Fri", 4)
    ),
    val tasks: List<CalendarMatrixRow> = listOf(
        CalendarMatrixRow("1", "Daily Review", "06:30 AM", "🕒", true, true, true, false, false),
        CalendarMatrixRow("2", "Deep Work", "09:00 - 10:00 AM", "💼", false, false, true, false, false),
        CalendarMatrixRow("3", "Lunch Walk", "12:30 - 01:00 PM", "📍", false, true, true, false, false),
        CalendarMatrixRow("4", "Learning", "03:00 - 04:00 PM", "📖", false, false, true, false, false),
        CalendarMatrixRow("5", "Workout", "06:00 - 07:00 PM", "⚡", true, true, true, true, false),
        CalendarMatrixRow("6", "Reading", "08:30 - 09:00 PM", "📱", false, false, true, false, false),
        CalendarMatrixRow("7", "Plan Tomorrow", "09:30 - 09:45 PM", "📝", false, false, true, false, false),
        CalendarMatrixRow("8", "Sleep", "10:30 PM", "🌙", false, false, true, false, false)
    )
)

data class DayTab(
    val dateNumber: Int,
    val dayName: String,
    val index: Int
)

sealed interface CalendarIntent {
    data class SelectDay(val dayIndex: Int) : CalendarIntent
    data class ToggleTaskDay(val taskId: String, val dayIndex: Int) : CalendarIntent
}
