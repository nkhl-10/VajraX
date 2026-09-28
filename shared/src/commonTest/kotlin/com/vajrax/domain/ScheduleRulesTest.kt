package com.vajrax.domain

import com.vajrax.core.time.TimeFormat
import com.vajrax.domain.habit.HabitSchedule
import com.vajrax.domain.habit.OccurrencePlanner
import com.vajrax.domain.habit.ScheduleRules
import com.vajrax.domain.habit.ScheduleType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ScheduleRulesTest {

    @Test
    fun dailyHabitIsScheduledEveryDayFromItsStartDate() {
        val h = habit(start = "2026-09-10")
        assertFalse(ScheduleRules.isScheduled(h, date("2026-09-09")))
        assertTrue(ScheduleRules.isScheduled(h, date("2026-09-10")))
        assertTrue(ScheduleRules.isScheduled(h, date("2026-12-31")))
    }

    @Test
    fun weekdayHabitSkipsRestDays() {
        // 2026-09-28 is a Monday, 2026-09-26 a Saturday.
        val h = habit(schedule = HabitSchedule(ScheduleType.WEEKDAYS, days = setOf(1, 2, 3, 4, 5)))
        assertTrue(ScheduleRules.isScheduled(h, date("2026-09-28")))
        assertFalse(ScheduleRules.isScheduled(h, date("2026-09-26")))
        assertFalse(ScheduleRules.isScheduled(h, date("2026-09-27")))
    }

    @Test
    fun intervalHabitRepeatsFromStartDate() {
        val h = habit(schedule = HabitSchedule(ScheduleType.INTERVAL, intervalDays = 3), start = "2026-09-01")
        assertTrue(ScheduleRules.isScheduled(h, date("2026-09-01")))
        assertFalse(ScheduleRules.isScheduled(h, date("2026-09-02")))
        assertTrue(ScheduleRules.isScheduled(h, date("2026-09-04")))
    }

    @Test
    fun archivedHabitIsNotScheduledFromArchiveDay() {
        val h = habit(archived = "2026-09-20")
        assertTrue(ScheduleRules.isScheduled(h, date("2026-09-19")))
        assertFalse(ScheduleRules.isScheduled(h, date("2026-09-20")))
    }

    @Test
    fun plannerCreatesOnlyMissingScheduledOccurrences() {
        val daily = habit("a")
        val weekdays = habit("b", HabitSchedule(ScheduleType.WEEKDAYS, days = setOf(1)))
        val planned = OccurrencePlanner.plan(
            listOf(daily, weekdays),
            date("2026-09-26"), date("2026-09-28"),
            existing = setOf("a|2026-09-26")
        )
        assertEquals(listOf("a|2026-09-27", "a|2026-09-28", "b|2026-09-28"), planned.map { "${it.habitId}|${it.date}" })
    }

    @Test
    fun timeFormatAcceptsLegacyAndRangeFormats() {
        assertEquals("06:30", TimeFormat.normalize("06:30 AM"))
        assertEquals("21:00", TimeFormat.normalize("09:00 - 10:00 PM"))
        assertEquals("00:15", TimeFormat.normalize("12:15 AM"))
        assertEquals("12:30", TimeFormat.normalize("12:30 PM"))
        assertEquals("07:05", TimeFormat.normalize("7:05"))
        assertNull(TimeFormat.normalize("25:00"))
        assertEquals("6:30 AM", TimeFormat.display("06:30"))
        assertEquals("09:00 - 10:00 AM", TimeFormat.displayRange("09:00", 60))
    }
}
