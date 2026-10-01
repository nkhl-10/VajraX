package com.vajrax.domain

import com.vajrax.domain.habit.CheckInAction
import com.vajrax.domain.habit.HabitType
import com.vajrax.domain.model.TrackingMode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** One rule for the check-in button on Home, the dial and the widgets. */
class CheckInActionTest {
    @Test
    fun picksTheActionFromTheHabitKind() {
        assertEquals(CheckInAction.Done, CheckInAction.of(habit()))
        assertEquals(CheckInAction.PlusOne, CheckInAction.of(habit().copy(type = HabitType.COUNT, targetValue = 8.0)))
        assertEquals(CheckInAction.StartTimer, CheckInAction.of(habit().copy(trackingMode = TrackingMode.TIMER)))
        assertEquals(CheckInAction.LogValue, CheckInAction.of(habit().copy(type = HabitType.VALUE, targetValue = 8000.0)))
        assertEquals(CheckInAction.LogValue, CheckInAction.of(habit().copy(type = HabitType.DURATION, targetValue = 20.0)))
    }

    @Test
    fun countBeatsTimerSoACountHabitAlwaysTicks() {
        assertEquals(CheckInAction.PlusOne, CheckInAction.of(habit().copy(type = HabitType.COUNT, trackingMode = TrackingMode.TIMER)))
    }

    @Test
    fun onlyDoneAndPlusOneFinishWithoutTheApp() {
        assertTrue(CheckInAction.Done.worksInBackground)
        assertTrue(CheckInAction.PlusOne.worksInBackground)
        assertFalse(CheckInAction.StartTimer.worksInBackground)
        assertFalse(CheckInAction.LogValue.worksInBackground)
    }
}
