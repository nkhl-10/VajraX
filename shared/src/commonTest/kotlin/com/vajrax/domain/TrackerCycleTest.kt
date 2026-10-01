package com.vajrax.domain

import com.vajrax.domain.habit.Tracker
import com.vajrax.domain.habit.TrackerStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** A routine's first cycle ends after totalDays; the habits carry on. */
class TrackerCycleTest {
    private val tracker = Tracker("t1", "tpl", "Morning", date("2026-09-01"), 30, true, TrackerStatus.ACTIVE, null)

    @Test
    fun countsDaysAndFlagsTheEndOfTheCycle() {
        assertEquals(1, tracker.daysIn(date("2026-09-01")))
        assertEquals(30, tracker.daysIn(date("2026-09-30")))
        assertFalse(tracker.cycleComplete(date("2026-09-30")))
        assertTrue(tracker.cycleComplete(date("2026-10-01")))
        assertEquals(31, tracker.daysIn(date("2026-10-01")))
        // The clamped cycle day stays at 30 for the routine card.
        assertEquals(30, tracker.dayNumber(date("2026-10-05")))
    }
}
