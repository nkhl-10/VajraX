package com.vajrax.domain.habit

import com.vajrax.core.time.Dates
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber

/**
 * Scheduling rules (spec 02 FR-011..FR-014, spec 06 §5).
 *
 * - A habit is never scheduled before its start date, nor on/after the day it was archived.
 * - WEEKDAYS habits are only scheduled on the selected ISO days; other days are rest days.
 * - INTERVAL habits repeat every N days counted from the start date.
 * - WEEKLY_TARGET habits can be done on any day; the target is evaluated per ISO week
 *   (see [com.vajrax.domain.analytics.HabitAnalytics]), so an individual day is never "missed".
 */
object ScheduleRules {

    fun isScheduled(habit: Habit, date: LocalDate): Boolean {
        habit.startDate?.let { if (date < it) return false }
        habit.archivedAt?.let { if (date >= it) return false }
        val schedule = habit.schedule
        return when (schedule.type) {
            ScheduleType.DAILY -> true
            ScheduleType.WEEKDAYS -> date.dayOfWeek.isoDayNumber in schedule.days
            ScheduleType.WEEKLY_TARGET -> true
            ScheduleType.INTERVAL -> {
                val anchor = habit.startDate ?: return true
                val interval = schedule.intervalDays.coerceAtLeast(1)
                Dates.daysBetween(anchor, date) % interval == 0
            }
        }
    }

    /** True for habits whose individual days can never be counted as missed. */
    fun isFlexible(habit: Habit): Boolean = habit.schedule.type == ScheduleType.WEEKLY_TARGET
}

/**
 * Pure planner: which occurrence rows must exist for [habits] between [from] and [to] (inclusive).
 * Rows that already exist are listed in [existing] as "habitId|date" keys and are left untouched,
 * so history is frozen once materialized.
 */
object OccurrencePlanner {

    data class Planned(val habitId: String, val date: LocalDate, val time: String?) {
        val id: String get() = Occurrence.idFor(habitId, date)
    }

    fun plan(
        habits: List<Habit>,
        from: LocalDate,
        to: LocalDate,
        existing: Set<String> = emptySet()
    ): List<Planned> {
        if (to < from) return emptyList()
        val days = Dates.range(from, to)
        val out = ArrayList<Planned>()
        for (habit in habits) {
            for (day in days) {
                if (!ScheduleRules.isScheduled(habit, day)) continue
                if ("${habit.id}|$day" in existing) continue
                out += Planned(habit.id, day, habit.time)
            }
        }
        return out
    }
}
