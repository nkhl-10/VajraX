package com.vajrax.domain.habit

import com.vajrax.domain.model.TrackingMode

/**
 * What the main "check in" button does for a habit. Home, the dial and the widgets all ask this,
 * so a habit behaves the same everywhere:
 *  - [PlusOne] for count habits (one step towards the target),
 *  - [StartTimer] for timed habits (a focus session records the real minutes),
 *  - [LogValue] for measured habits (the user enters the value),
 *  - [Done] for everything else.
 * [StartTimer] and [LogValue] need the app, so a widget opens it instead of guessing a value.
 */
enum class CheckInAction(val label: String) {
    Done("Done"),
    PlusOne("+1"),
    StartTimer("Start"),
    LogValue("Log");

    /** True when the action can finish from a notification or widget without opening the app. */
    val worksInBackground: Boolean get() = this == Done || this == PlusOne

    companion object {
        fun of(habit: Habit): CheckInAction = when {
            habit.type == HabitType.COUNT -> PlusOne
            habit.trackingMode == TrackingMode.TIMER -> StartTimer
            habit.type != HabitType.BOOLEAN -> LogValue
            else -> Done
        }
    }
}
