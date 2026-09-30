package com.vajrax.ui

import com.vajrax.domain.habit
import com.vajrax.ui.features.templates.ActivationState
import com.vajrax.ui.features.templates.DraftHabit
import kotlin.test.Test
import kotlin.test.assertEquals

/** Customize screen: "Today · N left" counts only habits whose window hasn't ended yet. */
class ActivationStateTest {
    private val drafts = listOf(
        DraftHabit(habit("wake", time = "06:00").copy(durationMinutes = 30)),
        DraftHabit(habit("lunch", time = "12:30").copy(durationMinutes = 60)),
        DraftHabit(habit("read", time = "21:00").copy(durationMinutes = 20)),
        DraftHabit(habit("water", time = "").copy(durationMinutes = 5)),
        DraftHabit(habit("gym", time = "18:00").copy(durationMinutes = 45), included = false)
    )

    @Test
    fun countsHabitsStillAheadAndUntimedOnes() {
        // 13:00: wake is over, lunch is in progress, read is later, water is untimed; gym is excluded.
        assertEquals(3, ActivationState(drafts = drafts, minuteOfDay = 13 * 60).remainingToday)
    }

    @Test
    fun lateEveningLeavesOnlyUntimedHabits() {
        assertEquals(1, ActivationState(drafts = drafts, minuteOfDay = 22 * 60).remainingToday)
    }

    @Test
    fun earlyMorningKeepsEverything() {
        assertEquals(4, ActivationState(drafts = drafts, minuteOfDay = 5 * 60).remainingToday)
    }
}
