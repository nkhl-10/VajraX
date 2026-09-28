package com.vajrax.domain

import com.vajrax.domain.analytics.HabitAnalytics
import com.vajrax.domain.habit.HabitSchedule
import com.vajrax.domain.habit.ScheduleType
import com.vajrax.domain.model.ActionStatus.COMPLETE
import com.vajrax.domain.model.ActionStatus.MINIMUM
import com.vajrax.domain.model.ActionStatus.PENDING
import com.vajrax.domain.model.ActionStatus.SKIPPED
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class HabitAnalyticsTest {

    private val today = date("2026-09-28") // Monday
    private val window = date("2026-09-01")..today

    @Test
    fun completionRateUsesOnlyEligibleScheduledOccurrences() {
        val h = habit("a")
        val records = listOf(
            occ("a", "2026-09-24", COMPLETE),
            occ("a", "2026-09-25", PENDING),  // past & open -> missed
            occ("a", "2026-09-26", SKIPPED),  // neutral
            occ("a", "2026-09-27", MINIMUM),  // minimum still counts as done
            occ("a", "2026-09-28", PENDING),  // today & open -> not counted yet
            occ("a", "2026-09-29", COMPLETE)  // future -> ignored
        )
        val s = HabitAnalytics(listOf(h), records, today, window).period(date("2026-09-24"), date("2026-09-30"))
        assertEquals(2, s.completed)
        assertEquals(3, s.eligible)
        assertEquals(1, s.missed)
        assertEquals(1, s.skipped)
        assertEquals(1, s.pending)
        assertEquals(67, s.rate)
    }

    @Test
    fun periodWithNothingDueHasNoRate() {
        val s = HabitAnalytics(listOf(habit("a")), listOf(occ("a", "2026-09-28", PENDING)), today, window).period(today, today)
        assertNull(s.rate)
        assertEquals(1, s.pending)
    }

    @Test
    fun habitStreakRespectsScheduleAndTreatsSkipsAsNeutral() {
        val h = habit("a")
        val records = listOf(
            occ("a", "2026-09-20", COMPLETE),
            occ("a", "2026-09-21", PENDING), // missed: breaks
            occ("a", "2026-09-22", COMPLETE),
            occ("a", "2026-09-23", COMPLETE),
            occ("a", "2026-09-24", SKIPPED), // neutral
            occ("a", "2026-09-25", COMPLETE),
            occ("a", "2026-09-28", PENDING)  // today, still open: does not break
        )
        val streak = HabitAnalytics(listOf(h), records, today, window).habitStreak("a")
        assertEquals(3, streak.current)
        assertEquals(3, streak.longest)
        assertEquals(date("2026-09-25"), streak.longestEnd)
    }

    @Test
    fun weeklyTargetShortfallCountsOnlyWhenTheWeekHasEnded() {
        val h = habit("w", HabitSchedule(ScheduleType.WEEKLY_TARGET, weeklyTarget = 3), start = "2026-09-14")
        val records = listOf(
            // Week of Sep 14: 2 of 3 -> 1 missed on Sunday Sep 20.
            occ("w", "2026-09-15", COMPLETE),
            occ("w", "2026-09-17", COMPLETE),
            occ("w", "2026-09-16", PENDING),
            // Week of Sep 21: 4 done, only 3 count.
            occ("w", "2026-09-21", COMPLETE),
            occ("w", "2026-09-22", COMPLETE),
            occ("w", "2026-09-23", COMPLETE),
            occ("w", "2026-09-24", COMPLETE),
            // Current week: nothing done yet -> pending, not missed.
            occ("w", "2026-09-28", PENDING)
        )
        val a = HabitAnalytics(listOf(h), records, today, window)
        val first = a.period(date("2026-09-14"), date("2026-09-20"))
        assertEquals(2, first.completed)
        assertEquals(3, first.eligible)
        assertEquals(1, first.missed)
        val second = a.period(date("2026-09-21"), date("2026-09-27"))
        assertEquals(3, second.completed)
        assertEquals(3, second.eligible)
        val current = a.period(date("2026-09-28"), date("2026-10-04"))
        assertEquals(0, current.eligible)
        assertEquals(3, current.pending)
        assertEquals(1, a.habitStreak("w").current)
    }

    @Test
    fun dayStreakNeedsEightyPercentAndIgnoresRestDays() {
        val a1 = habit("a")
        val b1 = habit("b", HabitSchedule(ScheduleType.WEEKDAYS, days = setOf(1, 2, 3, 4, 5)))
        val records = listOf(
            occ("a", "2026-09-24", COMPLETE), occ("b", "2026-09-24", COMPLETE), // Thu 2/2
            occ("a", "2026-09-25", COMPLETE), occ("b", "2026-09-25", PENDING),  // Fri 1/2 -> breaks
            occ("a", "2026-09-26", COMPLETE),                                   // Sat 1/1
            occ("a", "2026-09-27", COMPLETE),                                   // Sun 1/1
            occ("a", "2026-09-28", COMPLETE), occ("b", "2026-09-28", PENDING)   // today 1/2: not yet
        )
        val streak = HabitAnalytics(listOf(a1, b1), records, today, window).dayStreak()
        assertEquals(2, streak.current)
        assertEquals(2, streak.longest)
    }

    @Test
    fun weekComparisonUsesSameWeekdaysOfPreviousWeek() {
        val h = habit("a")
        val records = listOf(
            occ("a", "2026-09-21", COMPLETE), // last Monday
            occ("a", "2026-09-22", COMPLETE), // last Tuesday (outside the compared window)
            occ("a", "2026-09-28", COMPLETE)  // this Monday
        )
        val cmp = HabitAnalytics.weekComparison(HabitAnalytics(listOf(h), records, today, window), today)
        assertEquals(1, cmp.current.completed)
        assertEquals(1, cmp.previous.completed)
        assertEquals(0, cmp.rateDelta)
    }

    @Test
    fun mostConsistentAndNeedsAttentionRankByRate() {
        val good = habit("good")
        val weak = habit("weak")
        val records = (22..27).flatMap { d ->
            val day = "2026-09-$d"
            listOf(occ("good", day, COMPLETE), occ("weak", day, if (d % 3 == 0) COMPLETE else PENDING))
        }
        val a = HabitAnalytics(listOf(good, weak), records, today, window)
        assertEquals("good", a.mostConsistent(date("2026-09-22"), today).first().habit.id)
        assertEquals(listOf("weak"), a.needsAttention(date("2026-09-22"), today).map { it.habit.id })
    }
}
