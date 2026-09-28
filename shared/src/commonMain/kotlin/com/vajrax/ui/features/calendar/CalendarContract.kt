package com.vajrax.ui.features.calendar

import androidx.compose.runtime.Immutable
import com.vajrax.domain.analytics.PeriodStats
import com.vajrax.domain.habit.Habit
import com.vajrax.domain.model.ActionStatus
import kotlinx.datetime.LocalDate

enum class CalendarMode { WEEK, MONTH }

enum class CellKind { DONE_TODAY, DONE_PAST, SKIPPED, OPEN_TODAY, MISSED, FUTURE, REST }

@Immutable
data class DayHeader(
    val date: LocalDate,
    val dayNumber: Int,
    val dayName: String,
    val isToday: Boolean,
    val isSelected: Boolean
)

@Immutable
data class MatrixCell(val date: LocalDate, val occurrenceId: String?, val kind: CellKind)

@Immutable
data class MatrixRow(val habit: Habit, val timeLabel: String, val cells: List<MatrixCell>)

/** Heatmap level: -1 nothing scheduled, 0 none done, 1 low, 2 medium, 3 high. */
@Immutable
data class MonthCell(
    val date: LocalDate?,
    val level: Int,
    val isToday: Boolean,
    val isSelected: Boolean,
    val isFuture: Boolean,
    val rate: Int?
)

@Immutable
data class DayHabit(
    val occurrenceId: String,
    val habit: Habit,
    val status: ActionStatus,
    val isPast: Boolean,
    val isToday: Boolean
)

@Immutable
data class CalendarUiState(
    val isLoading: Boolean = true,
    val hasTracker: Boolean = false,
    val trackerName: String = "",
    val mode: CalendarMode = CalendarMode.WEEK,
    val today: LocalDate? = null,
    val selected: LocalDate? = null,
    val rangeLabel: String = "",
    val weekDays: List<DayHeader> = emptyList(),
    val rows: List<MatrixRow> = emptyList(),
    val monthLabel: String = "",
    val monthCells: List<MonthCell> = emptyList(),
    val monthStats: PeriodStats = PeriodStats(),
    val selectedLabel: String = "",
    val selectedStats: PeriodStats = PeriodStats(),
    val selectedItems: List<DayHabit> = emptyList()
)

sealed interface CalendarIntent {
    data class SetMode(val mode: CalendarMode) : CalendarIntent
    data class Select(val date: LocalDate) : CalendarIntent
    data class Shift(val days: Int) : CalendarIntent
    data class ShiftMonth(val months: Int) : CalendarIntent
    data object GoToday : CalendarIntent
    data class ToggleToday(val occurrenceId: String) : CalendarIntent
    data class CorrectPast(val occurrenceId: String, val done: Boolean) : CalendarIntent
}

sealed interface CalendarEffect {
    data class ShowMessage(val message: String) : CalendarEffect
}
