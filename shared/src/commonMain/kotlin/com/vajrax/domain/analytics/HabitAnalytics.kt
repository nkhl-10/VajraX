package com.vajrax.domain.analytics

import com.vajrax.core.time.Dates
import com.vajrax.core.time.TimeFormat
import com.vajrax.domain.habit.Habit
import com.vajrax.domain.habit.Occurrence
import com.vajrax.domain.habit.ScheduleRules
import com.vajrax.domain.model.ActionStatus
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Counts behind every percentage in the app, so a rate can always be explained
 * ("98 completed ÷ 120 scheduled; 4 skipped and 3 still open are not counted").
 */
data class PeriodStats(
    val completed: Int = 0,
    /** Scheduled occurrences whose period has ended or that were completed. The denominator. */
    val eligible: Int = 0,
    val skipped: Int = 0,
    /** Scheduled today and not done yet — not counted as missed (spec rule 1). */
    val pending: Int = 0,
    val missed: Int = 0
) {
    val rate: Int? get() = if (eligible > 0) ((completed * 100.0) / eligible).roundToInt() else null
    val rateFraction: Float get() = if (eligible > 0) completed.toFloat() / eligible else 0f
    val scheduled: Int get() = eligible + skipped + pending

    operator fun plus(o: PeriodStats) = PeriodStats(
        completed + o.completed, eligible + o.eligible, skipped + o.skipped, pending + o.pending, missed + o.missed
    )
}

data class DayStats(val date: LocalDate, val stats: PeriodStats)

data class StreakResult(val current: Int = 0, val longest: Int = 0, val longestEnd: LocalDate? = null)

data class HabitStats(
    val habit: Habit,
    val stats: PeriodStats,
    val streak: StreakResult,
    /** "days" for daily-style habits, "weeks" for weekly-target habits. */
    val streakUnit: String
)

data class CategoryStats(val category: String, val stats: PeriodStats)

data class TimeBucket(val label: String, val window: String, val stats: PeriodStats)

data class Comparison(val current: PeriodStats, val previous: PeriodStats) {
    /** Percentage-point change, null when either period has nothing to compare. */
    val rateDelta: Int? get() {
        val c = current.rate ?: return null
        val p = previous.rate ?: return null
        return c - p
    }
    val completedDelta: Int get() = current.completed - previous.completed
}

private data class Contribution(
    val habitId: String,
    val date: LocalDate,
    val time: String?,
    val stats: PeriodStats
)

/**
 * Analytics derived only from persisted occurrences and each habit's schedule (spec 06 §6, brief §5).
 *
 * Rules implemented here:
 * 1. Completion rate = completed ÷ eligible scheduled occurrences.
 * 2. Future occurrences and today's still-open occurrences are never missed.
 * 3. Skipped occurrences are neutral: excluded from both numerator and denominator.
 * 4. Rest days produce no occurrence, so they are never failures.
 * 5. Weekly-target habits are evaluated per ISO week: completions count on the day they happen
 *    (capped at the target) and any shortfall is counted once, on the last day of a finished week.
 *
 * [records] must cover [window]; weekly targets are evaluated for weeks overlapping the window.
 */
class HabitAnalytics(
    habits: Collection<Habit>,
    records: List<Occurrence>,
    private val today: LocalDate,
    private val window: ClosedRange<LocalDate>
) {
    private val habitsById: Map<String, Habit> = habits.associateBy { it.id }
    private val recordsByHabit: Map<String, List<Occurrence>> =
        records.filter { it.habitId in habitsById }.groupBy { it.habitId }.mapValues { (_, v) -> v.sortedBy { it.date } }
    private val contributions: List<Contribution> = buildContributions()
    private val byDate: Map<LocalDate, List<Contribution>> = contributions.groupBy { it.date }

    private fun buildContributions(): List<Contribution> {
        val out = ArrayList<Contribution>()
        for ((habitId, list) in recordsByHabit) {
            val habit = habitsById.getValue(habitId)
            if (ScheduleRules.isFlexible(habit)) {
                out += weeklyContributions(habit, list)
            } else {
                for (occ in list) {
                    val stats = classify(occ) ?: continue
                    out += Contribution(habitId, occ.date, occ.scheduledTime ?: habit.time, stats)
                }
            }
        }
        return out
    }

    private fun classify(occ: Occurrence): PeriodStats? = when {
        occ.date > today -> null
        occ.isDone -> PeriodStats(completed = 1, eligible = 1)
        occ.isSkipped -> PeriodStats(skipped = 1)
        occ.status == ActionStatus.MISSED -> PeriodStats(eligible = 1, missed = 1)
        occ.date < today -> PeriodStats(eligible = 1, missed = 1)
        else -> PeriodStats(pending = 1)
    }

    private fun weeklyContributions(habit: Habit, list: List<Occurrence>): List<Contribution> {
        val out = ArrayList<Contribution>()
        val target = habit.schedule.weeklyTarget.coerceAtLeast(1)
        val firstActive = habit.startDate ?: list.firstOrNull()?.date ?: return out
        var weekStart = Dates.startOfWeek(maxOf(window.start, firstActive))
        // A week that begins before the window (and while the habit was already active) is only
        // partly covered by [records]; scoring it would invent a shortfall, so it is left out.
        val startedBeforeWindow = habit.startDate == null || habit.startDate < window.start
        if (weekStart < window.start && startedBeforeWindow) weekStart = weekStart.plus(7, DateTimeUnit.DAY)
        val lastDay = minOf(window.endInclusive, today)
        while (weekStart <= lastDay) {
            val weekEnd = weekStart.plus(6, DateTimeUnit.DAY)
            val activeDays = Dates.range(weekStart, weekEnd).count { ScheduleRules.isScheduled(habit, it) }
            if (activeDays > 0) {
                val effectiveTarget = min(target, activeDays)
                val done = list.filter { it.date in weekStart..weekEnd && it.date <= today && it.isDone }
                val counted = done.take(effectiveTarget)
                counted.forEach {
                    out += Contribution(habit.id, it.date, it.scheduledTime ?: habit.time, PeriodStats(completed = 1, eligible = 1))
                }
                val shortfall = effectiveTarget - counted.size
                if (shortfall > 0) {
                    if (weekEnd < today) {
                        out += Contribution(habit.id, weekEnd, habit.time, PeriodStats(eligible = shortfall, missed = shortfall))
                    } else {
                        out += Contribution(habit.id, today, habit.time, PeriodStats(pending = shortfall))
                    }
                }
            }
            weekStart = weekStart.plus(7, DateTimeUnit.DAY)
        }
        return out
    }

    private fun sum(items: List<Contribution>) = items.fold(PeriodStats()) { acc, c -> acc + c.stats }

    private fun inRange(from: LocalDate, to: LocalDate) = contributions.filter { it.date in from..to }

    fun period(from: LocalDate, to: LocalDate): PeriodStats = sum(inRange(from, to))

    fun day(date: LocalDate): PeriodStats = sum(byDate[date].orEmpty())

    fun days(from: LocalDate, to: LocalDate): List<DayStats> = Dates.range(from, to).map { DayStats(it, day(it)) }

    fun habitPeriod(habitId: String, from: LocalDate, to: LocalDate): PeriodStats =
        sum(contributions.filter { it.habitId == habitId && it.date in from..to })

    fun habitStats(from: LocalDate, to: LocalDate): List<HabitStats> {
        val grouped = inRange(from, to).groupBy { it.habitId }
        return grouped.mapNotNull { (id, items) ->
            val habit = habitsById[id] ?: return@mapNotNull null
            HabitStats(
                habit = habit,
                stats = sum(items),
                streak = habitStreak(id),
                streakUnit = if (ScheduleRules.isFlexible(habit)) "weeks" else "days"
            )
        }
    }

    /** Highest completion rate first; needs at least 2 eligible occurrences to rank. */
    fun mostConsistent(from: LocalDate, to: LocalDate, limit: Int = 5): List<HabitStats> =
        habitStats(from, to)
            .filter { it.stats.eligible >= 2 }
            .sortedWith(compareByDescending<HabitStats> { it.stats.rateFraction }.thenByDescending { it.stats.completed })
            .take(limit)

    /** Low completion rate (below 60 %) or two or more missed occurrences. Lowest first. */
    fun needsAttention(from: LocalDate, to: LocalDate, limit: Int = 3): List<HabitStats> =
        habitStats(from, to)
            .filter { it.stats.eligible >= 2 && ((it.stats.rate ?: 100) < 60 || it.stats.missed >= 2) }
            .sortedWith(compareBy<HabitStats> { it.stats.rateFraction }.thenByDescending { it.stats.missed })
            .take(limit)

    fun categoryStats(from: LocalDate, to: LocalDate): List<CategoryStats> =
        inRange(from, to)
            .groupBy { habitsById[it.habitId]?.category ?: "General" }
            .map { (category, items) -> CategoryStats(category, sum(items)) }
            .filter { it.stats.eligible > 0 }
            .sortedByDescending { it.stats.rateFraction }

    fun timeBuckets(from: LocalDate, to: LocalDate): List<TimeBucket> {
        val buckets = linkedMapOf(
            "Morning" to ("5 AM – 12 PM" to mutableListOf<Contribution>()),
            "Afternoon" to ("12 PM – 5 PM" to mutableListOf()),
            "Evening" to ("5 PM – 10 PM" to mutableListOf()),
            "Night" to ("10 PM – 5 AM" to mutableListOf())
        )
        inRange(from, to).forEach { c ->
            val minutes = TimeFormat.toMinutes(c.time) ?: return@forEach
            val hour = minutes / 60
            val key = when (hour) {
                in 5..11 -> "Morning"
                in 12..16 -> "Afternoon"
                in 17..21 -> "Evening"
                else -> "Night"
            }
            buckets.getValue(key).second += c
        }
        return buckets.map { (label, pair) -> TimeBucket(label, pair.first, sum(pair.second)) }
    }

    /**
     * Day streak: a day counts when at least [threshold] of its eligible habits were completed.
     * Days with nothing eligible (rest days, all skipped) neither extend nor break the streak;
     * today only counts once it qualifies.
     */
    fun dayStreak(threshold: Float = 0.8f): StreakResult {
        val start = window.start
        val end = minOf(window.endInclusive, today)
        var run = 0
        var longest = 0
        var longestEnd: LocalDate? = null
        for (d in Dates.range(start, end)) {
            val s = day(d)
            // Today still has open habits: judge it against everything scheduled so far today.
            val fraction = if (d == today) {
                val scheduled = s.eligible + s.pending
                if (scheduled > 0) s.completed.toFloat() / scheduled else 0f
            } else s.rateFraction
            when {
                s.eligible == 0 && d != today -> Unit
                s.eligible == 0 && s.pending == 0 -> Unit
                fraction >= threshold -> {
                    run++
                    if (run > longest) {
                        longest = run
                        longestEnd = d
                    }
                }
                d == today -> Unit
                else -> run = 0
            }
        }
        return StreakResult(current = run, longest = longest, longestEnd = longestEnd)
    }

    /** Consecutive successful scheduled occurrences of one habit, respecting its schedule. */
    fun habitStreak(habitId: String): StreakResult {
        val habit = habitsById[habitId] ?: return StreakResult()
        val items = contributions.filter { it.habitId == habitId }.sortedBy { it.date }
        var run = 0
        var longest = 0
        var longestEnd: LocalDate? = null
        if (ScheduleRules.isFlexible(habit)) {
            val weeks = items.groupBy { Dates.startOfWeek(it.date) }.entries.sortedBy { it.key }
            for ((weekStart, list) in weeks) {
                val s = sum(list)
                val weekEnd = weekStart.plus(6, DateTimeUnit.DAY)
                when {
                    s.eligible > 0 && s.missed == 0 && s.pending == 0 -> {
                        run++
                        if (run > longest) { longest = run; longestEnd = weekEnd }
                    }
                    weekEnd >= today -> Unit
                    s.missed > 0 -> run = 0
                }
            }
        } else {
            for (c in items) {
                when {
                    c.stats.completed > 0 -> {
                        run++
                        if (run > longest) { longest = run; longestEnd = c.date }
                    }
                    c.stats.missed > 0 -> run = 0
                    else -> Unit // skipped or still pending today
                }
            }
        }
        return StreakResult(run, longest, longestEnd)
    }

    companion object {
        /** This week so far vs the same weekdays of last week. */
        fun weekComparison(analytics: HabitAnalytics, today: LocalDate): Comparison {
            val start = Dates.startOfWeek(today)
            val elapsed = Dates.daysBetween(start, today)
            val prevStart = start.minus(7, DateTimeUnit.DAY)
            return Comparison(
                current = analytics.period(start, today),
                previous = analytics.period(prevStart, prevStart.plus(elapsed, DateTimeUnit.DAY))
            )
        }

        /** This month so far vs the same number of days at the start of last month. */
        fun monthComparison(analytics: HabitAnalytics, today: LocalDate): Comparison {
            val start = Dates.startOfMonth(today)
            val elapsed = Dates.daysBetween(start, today)
            val prevStart = start.minus(1, DateTimeUnit.MONTH)
            val prevEnd = minOf(prevStart.plus(elapsed, DateTimeUnit.DAY), Dates.endOfMonth(prevStart))
            return Comparison(
                current = analytics.period(start, today),
                previous = analytics.period(prevStart, prevEnd)
            )
        }
    }
}
