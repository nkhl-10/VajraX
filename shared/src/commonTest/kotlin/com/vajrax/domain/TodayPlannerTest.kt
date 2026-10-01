package com.vajrax.domain

import com.vajrax.domain.habit.HabitSchedule
import com.vajrax.domain.habit.ScheduleType
import com.vajrax.domain.habit.Tracker
import com.vajrax.domain.habit.TrackerStatus
import com.vajrax.domain.model.ActionStatus
import com.vajrax.domain.today.DayPhase
import com.vajrax.domain.today.TodayPlanner
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Home and the widgets both read today from this plan. */
class TodayPlannerTest {
    private val tracker = Tracker("t1", "tpl", "Morning", date("2026-09-01"), 30, true, TrackerStatus.ACTIVE, null)
    private val now = "2026-09-30T08:00:00Z"

    @Test
    fun weeklyTargetAlreadyMetDoesNotCountToday() {
        val weekly = habit("gym", schedule = HabitSchedule(type = ScheduleType.WEEKLY_TARGET, weeklyTarget = 2), time = "18:00")
        val daily = habit("walk", time = "07:00")
        val records = listOf(
            occ("gym", "2026-09-28", ActionStatus.COMPLETE, "18:00"),
            occ("gym", "2026-09-29", ActionStatus.COMPLETE, "18:00"),
            occ("gym", "2026-09-30", ActionStatus.PENDING, "18:00"),
            occ("walk", "2026-09-30", ActionStatus.PENDING, "07:00")
        )
        val plan = TodayPlanner.plan(date("2026-09-30"), 9 * 60, tracker, listOf(weekly, daily), records, reflectionDone = false, nowIso = now)
        assertEquals(DayPhase.WEEKLY_MET, plan.items.first { it.habit.id == "gym" }.phase)
        assertEquals(1, plan.total)
        assertEquals(0, plan.done)
    }

    @Test
    fun picksNowAndWhatComesNext() {
        val a = habit("a", time = "07:00")
        val b = habit("b", time = "08:00")
        val records = listOf(occ("a", "2026-09-30", ActionStatus.PENDING, "07:00"), occ("b", "2026-09-30", ActionStatus.PENDING, "08:00"))
        val plan = TodayPlanner.plan(date("2026-09-30"), 7 * 60 + 5, tracker, listOf(a, b), records, reflectionDone = false, nowIso = now)
        assertEquals(DayPhase.NOW, plan.items.first().phase)
        assertEquals(plan.items[0].id, plan.nowId)
        assertEquals(plan.items[1].id, plan.nextId)
        assertNull(plan.wrapUp)
    }

    @Test
    fun wrapsUpWhenEverythingIsDone() {
        val a = habit("a", time = "07:00")
        val records = listOf(occ("a", "2026-09-30", ActionStatus.COMPLETE, "07:00"))
        val plan = TodayPlanner.plan(date("2026-09-30"), 7 * 60 + 30, tracker, listOf(a), records, reflectionDone = false, nowIso = now)
        val wrap = assertNotNull(plan.wrapUp)
        assertTrue(wrap.allDone)
        assertNull(plan.nowId)
    }

    @Test
    fun weeklyReviewOnSundayUntilAReflectionIsWritten() {
        val a = habit("a", time = "07:00")
        val records = listOf(occ("a", "2026-10-04", ActionStatus.COMPLETE, "07:00"))
        val sunday = date("2026-10-04")
        assertTrue(TodayPlanner.plan(sunday, 10 * 60, tracker, listOf(a), records, reflectionDone = false, nowIso = now).weeklyReviewDue)
        assertFalse(TodayPlanner.plan(sunday, 10 * 60, tracker, listOf(a), records, reflectionDone = true, nowIso = now).weeklyReviewDue)
        // Friday is too early.
        assertFalse(TodayPlanner.plan(date("2026-10-02"), 20 * 60, tracker, listOf(a), records, reflectionDone = false, nowIso = now).weeklyReviewDue)
    }

    @Test
    fun routineStartingTomorrowNamesItsFirstHabit() {
        val later = tracker.copy(startDate = date("2026-10-01"))
        val wake = habit("wake", time = "05:30")
        val walk = habit("walk", time = "07:00")
        val plan = TodayPlanner.plan(date("2026-09-30"), 20 * 60, later, listOf(walk, wake), emptyList(), reflectionDone = false, nowIso = now)
        assertEquals(date("2026-10-01"), plan.startsOn)
        assertEquals("wake", plan.firstHabit?.id)
        assertEquals(0, plan.total)
    }
}
