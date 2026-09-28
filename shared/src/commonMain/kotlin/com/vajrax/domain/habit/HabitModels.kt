package com.vajrax.domain.habit

import com.vajrax.domain.model.ActionStatus
import com.vajrax.domain.model.TrackingMode
import kotlinx.datetime.LocalDate

/** How a check-in is recorded. */
enum class HabitType(val label: String) {
    BOOLEAN("Done / not done"),
    COUNT("Count"),
    DURATION("Duration"),
    VALUE("Measurable value");

    companion object {
        fun of(raw: String?): HabitType = entries.firstOrNull { it.name == raw } ?: BOOLEAN
    }
}

enum class ScheduleType(val label: String) {
    DAILY("Every day"),
    WEEKDAYS("Selected days"),
    WEEKLY_TARGET("Times per week"),
    INTERVAL("Every few days");

    companion object {
        fun of(raw: String?): ScheduleType = entries.firstOrNull { it.name == raw } ?: DAILY
    }
}

/**
 * @param days ISO day numbers (1 = Monday … 7 = Sunday) for [ScheduleType.WEEKDAYS].
 * Days that are not listed are rest days and never count as missed.
 */
data class HabitSchedule(
    val type: ScheduleType = ScheduleType.DAILY,
    val days: Set<Int> = ALL_DAYS,
    val weeklyTarget: Int = 3,
    val intervalDays: Int = 2
) {
    fun label(): String = when (type) {
        ScheduleType.DAILY -> "Daily"
        ScheduleType.WEEKDAYS -> when (days) {
            ALL_DAYS -> "Daily"
            setOf(1, 2, 3, 4, 5) -> "Weekdays"
            setOf(6, 7) -> "Weekends"
            else -> days.sorted().joinToString(" ") { DAY_LETTERS[it - 1] }
        }
        ScheduleType.WEEKLY_TARGET -> "${weeklyTarget}× a week"
        ScheduleType.INTERVAL -> "Every $intervalDays days"
    }

    fun encodeDays(): String = days.sorted().joinToString(",")

    companion object {
        val ALL_DAYS = setOf(1, 2, 3, 4, 5, 6, 7)
        val DAY_LETTERS = listOf("M", "T", "W", "T", "F", "S", "S")

        fun decodeDays(raw: String?): Set<Int> =
            raw?.split(",")?.mapNotNull { it.trim().toIntOrNull() }?.filter { it in 1..7 }?.toSet()
                ?.takeIf { it.isNotEmpty() } ?: ALL_DAYS
    }
}

/** A habit inside a user's tracker (persisted as PracticeEntity). */
data class Habit(
    val id: String,
    val trackerId: String?,
    val title: String,
    val category: String = "General",
    val icon: String = "check",
    val color: String = "indigo",
    val type: HabitType = HabitType.BOOLEAN,
    val targetValue: Double = 1.0,
    val unit: String? = null,
    /** "HH:mm" or null for anytime habits. */
    val time: String? = null,
    val durationMinutes: Int = 15,
    val minimumMinutes: Int = 5,
    val trackingMode: TrackingMode = TrackingMode.MANUAL,
    val schedule: HabitSchedule = HabitSchedule(),
    val reminderEnabled: Boolean = false,
    val reminderTime: String? = null,
    val startDate: LocalDate? = null,
    val archivedAt: LocalDate? = null,
    val sortOrder: Int = 0
) {
    val isArchived: Boolean get() = archivedAt != null

    /** Short "Daily · Health" subtitle used in lists. */
    fun subtitle(): String = "${schedule.label()} · $category"

    fun targetLabel(): String? = when (type) {
        HabitType.BOOLEAN -> null
        HabitType.COUNT -> "${formatValue(targetValue)} ${unit ?: "times"}"
        HabitType.DURATION -> "${formatValue(targetValue)} ${unit ?: "min"}"
        HabitType.VALUE -> "${formatValue(targetValue)} ${unit ?: ""}".trim()
    }

    /** Step used by the quick "+" control for count-like habits. */
    fun quickStep(): Double = when (type) {
        HabitType.COUNT -> 1.0
        HabitType.DURATION -> if (targetValue >= 30) 10.0 else 5.0
        HabitType.VALUE -> if (targetValue >= 10) (targetValue / 10).let { kotlin.math.max(1.0, kotlin.math.round(it)) } else 1.0
        HabitType.BOOLEAN -> 1.0
    }
}

fun formatValue(value: Double): String =
    if (value == kotlin.math.floor(value)) value.toLong().toString() else ((value * 10).toLong() / 10.0).toString()

/** A scheduled instance of a habit on a date (persisted as ActionRecordEntity). */
data class Occurrence(
    val id: String,
    val habitId: String,
    val date: LocalDate,
    val scheduledTime: String?,
    val status: ActionStatus,
    val completedAt: String? = null,
    val durationMinutes: Int? = null,
    val value: Double? = null,
    val note: String? = null,
    val skipReason: String? = null
) {
    val isDone: Boolean get() = status == ActionStatus.COMPLETE || status == ActionStatus.MINIMUM
    val isSkipped: Boolean get() = status == ActionStatus.SKIPPED
    val isOpen: Boolean
        get() = status == ActionStatus.PENDING || status == ActionStatus.SNOOZED || status == ActionStatus.ONGOING

    companion object {
        fun idFor(habitId: String, date: LocalDate) = "occ_${habitId}_$date"
    }
}

/** A habit joined with today's (or a given day's) occurrence. */
data class TimelineEntry(val habit: Habit, val occurrence: Occurrence)

enum class TrackerStatus { ACTIVE, ARCHIVED }

/** A user's personal copy of a template. Editing it never modifies the source template. */
data class Tracker(
    val id: String,
    val templateId: String,
    val name: String,
    val startDate: LocalDate,
    val totalDays: Int,
    val isActive: Boolean,
    val status: TrackerStatus,
    val endedAt: LocalDate?
) {
    /** 1-based day of the tracker cycle, clamped to [1, totalDays]. */
    fun dayNumber(today: LocalDate): Int {
        val elapsed = com.vajrax.core.time.Dates.daysBetween(startDate, today) + 1
        return elapsed.coerceIn(1, totalDays.coerceAtLeast(1))
    }
}

enum class SkipReason(val label: String) {
    NO_TIME("No time"),
    TIRED("Low energy"),
    SICK("Not well"),
    TRAVEL("Travelling"),
    REST("Planned rest"),
    OTHER("Other")
}
