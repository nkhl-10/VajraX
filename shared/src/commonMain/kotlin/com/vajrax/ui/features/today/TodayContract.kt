package com.vajrax.ui.features.today

import androidx.compose.runtime.Immutable
import com.vajrax.domain.habit.Habit
import com.vajrax.domain.habit.Occurrence

enum class TodayPhase { DONE, NOW, OPEN_EARLIER, UPCOMING, SKIPPED, WEEKLY_MET }

@Immutable
data class TodayItem(
    val habit: Habit,
    val occurrence: Occurrence,
    val phase: TodayPhase,
    /** "6:00 AM" (moved/snoozed time if any). */
    val timeLabel: String,
    /** e.g. "3 / 8 glasses" or "2 / 3 this week"; null for plain check habits. */
    val progressLabel: String? = null,
    val progressFraction: Float? = null
) {
    val id: String get() = occurrence.id
}

/**
 * A running (or paused) focus session. Persisted in settings so it survives rotation,
 * leaving the app and process death.
 */
@Immutable
data class TimerState(
    val item: TodayItem,
    val startedAtMs: Long,
    val accumulatedMs: Long,
    val running: Boolean
) {
    fun elapsedMs(nowMs: Long): Long = accumulatedMs + if (running) (nowMs - startedAtMs).coerceAtLeast(0) else 0
}

/** End-of-day summary shown once everything is done or the last habit's time has passed. */
@Immutable
data class DayWrap(val done: Int, val total: Int, val skipped: Int, val allDone: Boolean)

@Immutable
data class TodayUiState(
    val isLoading: Boolean = true,
    val hasTracker: Boolean = false,
    val trackerName: String = "",
    val greeting: String = "",
    val dateLabel: String = "",
    val statusLine: String = "",
    val items: List<TodayItem> = emptyList(),
    val nowId: String? = null,
    val doneCount: Int = 0,
    val totalCount: Int = 0,
    val weekRate: Int? = null,
    val consistencyRate: Int? = null,
    val timer: TimerState? = null,
    val dayWrap: DayWrap? = null,
    /** True at the end of the week while this week's reflection hasn't been written yet. */
    val weeklyReviewDue: Boolean = false,
    val error: String? = null
) {
    val todayFraction: Float get() = if (totalCount > 0) doneCount.toFloat() / totalCount else 0f
    val todayPercent: Int get() = (todayFraction * 100).toInt()
}

sealed interface TodayIntent {
    data class Toggle(val occurrenceId: String) : TodayIntent
    data class Complete(val occurrenceId: String) : TodayIntent
    data class CompleteMinimum(val occurrenceId: String) : TodayIntent
    data class Increment(val occurrenceId: String) : TodayIntent
    data class RecordValue(val occurrenceId: String, val value: Double) : TodayIntent
    data class Skip(val occurrenceId: String, val reason: String?) : TodayIntent
    data class Snooze(val occurrenceId: String) : TodayIntent
    data class Move(val occurrenceId: String, val time: String) : TodayIntent
    data class SaveNote(val occurrenceId: String, val note: String) : TodayIntent
    data class Reopen(val occurrenceId: String) : TodayIntent
    /** Undo: restore the occurrence exactly as it was before the last action. */
    data class Undo(val previous: Occurrence) : TodayIntent
    data class StartTimer(val occurrenceId: String) : TodayIntent
    data object PauseTimer : TodayIntent
    data object ResumeTimer : TodayIntent
    data object FinishTimer : TodayIntent
    data object CancelTimer : TodayIntent
}

sealed interface TodayEffect {
    /** [undo] offers an Undo action that restores the occurrence to this earlier state. */
    data class ShowMessage(val message: String, val undo: Occurrence? = null) : TodayEffect
}
