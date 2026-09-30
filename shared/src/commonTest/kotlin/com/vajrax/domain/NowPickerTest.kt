package com.vajrax.domain

import com.vajrax.domain.habit.NowPicker
import com.vajrax.domain.habit.NowPicker.Candidate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class NowPickerTest {
    private fun c(id: String, hh: Int, mm: Int = 0, dur: Int = 15, open: Boolean = true, snoozed: Boolean = false, at: String? = null) =
        Candidate(id, hh * 60 + mm, dur, open, snoozed, at)

    private val routine = listOf(c("wake", 5, 30), c("water", 5, 35), c("walk", 6, 0, dur = 30), c("read", 21, 0))

    @Test
    fun lateStartBeginsAtTheTopAndMovesDownOnDone() {
        val now = 16 * 60
        assertEquals("wake", NowPicker.pick(routine, now))
        assertEquals("water", NowPicker.next(routine, now, "wake", "2026-09-30T10:31:00Z"))
        val afterTwo = routine.map {
            when (it.id) {
                "wake" -> it.copy(open = false, completedAt = "2026-09-30T10:31:00Z")
                "water" -> it.copy(open = false, completedAt = "2026-09-30T10:32:00Z")
                else -> it
            }
        }
        assertEquals("walk", NowPicker.pick(afterTwo, now))
    }

    @Test
    fun habitInItsTimeWindowWins() {
        assertEquals("walk", NowPicker.pick(routine, 6 * 60 + 10))
    }

    @Test
    fun habitStartingSoonWinsOverOlderOpenOnes() {
        val list = routine + c("gym", 17, 20)
        assertEquals("gym", NowPicker.pick(list.sortedBy { it.start }, 17 * 60))
    }

    @Test
    fun continuesAfterTheLastCompletedHabitInsteadOfJumpingBack() {
        // Wake-up was left open; walk was done, so focus moves on to read, not back to wake.
        val list = routine.map { if (it.id == "walk") it.copy(open = false, completedAt = "2026-09-30T01:00:00Z") else it }
        assertEquals("read", NowPicker.pick(list, 8 * 60))
    }

    @Test
    fun snoozingMovesFocusOn() {
        val snoozed = routine.map { if (it.id == "wake") it.copy(start = 16 * 60 + 15, snoozed = true) else it }
            .sortedBy { it.start }
        assertEquals("water", NowPicker.pick(snoozed, 16 * 60))
        // When the snoozed time arrives it becomes current again.
        assertEquals("wake", NowPicker.pick(snoozed, 16 * 60 + 16))
    }

    @Test
    fun wrapsToTheFirstOpenHabitAndStopsWhenAllAreDone() {
        val list = routine.map { if (it.id == "read") it.copy(open = false, completedAt = "2026-09-30T02:00:00Z") else it }
        assertEquals("wake", NowPicker.pick(list, 12 * 60))
        assertNull(NowPicker.pick(routine.map { it.copy(open = false) }, 12 * 60))
    }
}
