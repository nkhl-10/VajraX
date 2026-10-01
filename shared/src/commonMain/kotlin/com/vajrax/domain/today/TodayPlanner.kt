package com.vajrax.domain.today

import com.vajrax.core.time.Dates
import com.vajrax.core.time.TimeFormat
import com.vajrax.domain.habit.Habit
import com.vajrax.domain.habit.NowPicker
import com.vajrax.domain.habit.Occurrence
import com.vajrax.domain.habit.ScheduleType
import com.vajrax.domain.habit.Tracker
import com.vajrax.domain.model.ActionStatus
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber

/** Where a habit stands today. */
enum class DayPhase { DONE, NOW, OPEN_EARLIER, UPCOMING, SKIPPED, WEEKLY_MET }

/** One of today's habits with its phase, in the order the day runs. */
data class PlannedHabit(
    val habit: Habit,
    val occurrence: Occurrence,
    val phase: DayPhase,
    /** Minute of day it starts (snoozed / moved time if any), null for "anytime". */
    val startMinute: Int?,
    /** Completions so far this week, for weekly-target habits; null otherwise. */
    val weeklyDone: Int?
) {
    val id: String get() = occurrence.id
}

/** End-of-day summary: everything done, or the last habit's time has passed. */
data class DayWrapUp(val done: Int, val total: Int, val skipped: Int, val allDone: Boolean)

/** Everything Home and the widgets show about today, computed once from the database rows. */
data class TodayPlan(
    val items: List<PlannedHabit>,
    val nowId: String?,
    /** What becomes current once [nowId] is done. */
    val nextId: String?,
    /** Habits that count today (skipped and weekly targets already met don't). */
    val done: Int,
    val total: Int,
    val openEarlier: Int,
    val wrapUp: DayWrapUp?,
    val weeklyReviewDue: Boolean,
    /** Set while the routine hasn't started yet. */
    val startsOn: LocalDate?,
    /** First habit of a routine that hasn't started yet. */
    val firstHabit: Habit?,
    /** The routine's first cycle is over (habits carry on). */
    val cycleComplete: Boolean
) {
    val skipped: Int get() = items.count { it.phase == DayPhase.SKIPPED }

    companion object {
        val Empty = TodayPlan(emptyList(), null, null, 0, 0, 0, null, false, null, null, false)
    }
}

/**
 * Today's rules in one place, shared by Home and the home-screen widgets so they always agree on
 * phases, counts and what is current.
 */
object TodayPlanner {
    /** Saturday from this minute, and all of Sunday, invite the weekly review. */
    private const val REVIEW_SATURDAY_FROM = 18 * 60

    /** Long overnight habits (sleep) count as "over" an hour after they start. */
    private const val WRAP_UP_MAX_MINUTES = 60
    private const val LATEST_WRAP_UP = 23 * 60 + 30

    /**
     * @param records today's occurrences plus the rest of this week (weekly targets and the
     *   weekly review look at the week).
     */
    fun plan(
        day: LocalDate,
        minute: Int,
        tracker: Tracker?,
        habits: List<Habit>,
        records: List<Occurrence>,
        reflectionDone: Boolean,
        nowIso: String
    ): TodayPlan {
        if (tracker == null) return TodayPlan.Empty
        val habitsById = habits.associateBy { it.id }
        val weekStart = Dates.startOfWeek(day)
        val weekDone = records.filter { it.date >= weekStart && it.date <= day && it.isDone }
            .groupBy { it.habitId }.mapValues { it.value.size }

        val items = records.filter { it.date == day }
            .mapNotNull { occ -> habitsById[occ.habitId]?.let { it to occ } }
            .map { (habit, occ) ->
                val start = TimeFormat.toMinutes(occ.scheduledTime ?: habit.time)
                val weekly = habit.schedule.type == ScheduleType.WEEKLY_TARGET
                val doneThisWeek = weekDone[habit.id] ?: 0
                val phase = when {
                    occ.isDone -> DayPhase.DONE
                    occ.isSkipped -> DayPhase.SKIPPED
                    weekly && doneThisWeek >= habit.schedule.weeklyTarget -> DayPhase.WEEKLY_MET
                    start != null && start + habit.durationMinutes <= minute -> DayPhase.OPEN_EARLIER
                    else -> DayPhase.UPCOMING
                }
                PlannedHabit(habit, occ, phase, start, if (weekly) doneThisWeek else null)
            }
            .sortedWith(compareBy<PlannedHabit> { it.startMinute ?: Int.MAX_VALUE }.thenBy { it.habit.sortOrder })

        val candidates = items.map { candidate(it.habit, it.occurrence, it.phase) }
        val nowId = NowPicker.pick(candidates, minute)
        val nextId = nowId?.let { NowPicker.next(candidates, minute, it, nowIso) }
        val planned = items.map { if (it.id == nowId) it.copy(phase = DayPhase.NOW) else it }

        val counted = planned.filter { it.phase != DayPhase.SKIPPED && it.phase != DayPhase.WEEKLY_MET }
        val done = counted.count { it.phase == DayPhase.DONE }
        val total = counted.size

        val lastEnd = counted.mapNotNull { i -> i.startMinute?.let { it + minOf(i.habit.durationMinutes, WRAP_UP_MAX_MINUTES) } }
            .maxOrNull()?.coerceAtMost(LATEST_WRAP_UP)
        val allDone = total > 0 && done == total
        val wrapUp = if (total > 0 && (allDone || (lastEnd != null && minute >= lastEnd))) {
            DayWrapUp(done, total, planned.count { it.phase == DayPhase.SKIPPED }, allDone)
        } else null

        val iso = day.dayOfWeek.isoDayNumber
        val reviewWindow = iso == 7 || (iso == 6 && minute >= REVIEW_SATURDAY_FROM)
        val weekHasRecords = records.any { it.date >= weekStart && it.date <= day && (it.isDone || it.isSkipped) }

        val startsOn = tracker.startDate.takeIf { it > day }
        val firstHabit = if (startsOn == null) null else habits
            .filter { it.trackerId == tracker.id && it.archivedAt == null }
            .minByOrNull { TimeFormat.toMinutes(it.time) ?: Int.MAX_VALUE }

        return TodayPlan(
            items = planned,
            nowId = nowId,
            nextId = nextId,
            done = done,
            total = total,
            openEarlier = planned.count { it.phase == DayPhase.OPEN_EARLIER },
            wrapUp = wrapUp,
            weeklyReviewDue = reviewWindow && weekHasRecords && !reflectionDone,
            startsOn = startsOn,
            firstHabit = firstHabit,
            cycleComplete = tracker.cycleComplete(day)
        )
    }

    /** The [NowPicker] view of one habit; "open" means it still needs doing today. */
    fun candidate(habit: Habit, occurrence: Occurrence, phase: DayPhase): NowPicker.Candidate = NowPicker.Candidate(
        id = occurrence.id,
        start = TimeFormat.toMinutes(occurrence.scheduledTime ?: habit.time),
        durationMinutes = habit.durationMinutes,
        open = phase == DayPhase.UPCOMING || phase == DayPhase.OPEN_EARLIER || phase == DayPhase.NOW,
        snoozed = occurrence.status == ActionStatus.SNOOZED,
        completedAt = occurrence.completedAt?.takeIf { occurrence.isDone }
    )
}
