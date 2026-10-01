package com.vajrax.domain.usecase

import com.vajrax.core.time.AppClock
import com.vajrax.core.time.TimeFormat
import com.vajrax.domain.habit.Habit
import com.vajrax.domain.habit.HabitType
import com.vajrax.domain.habit.Occurrence
import com.vajrax.domain.model.ActionStatus
import com.vajrax.domain.repository.PracticeRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Every check-in write: done, minimum, values, +1, timer sessions, skip, snooze, move, notes,
 * undo and past corrections. App taps, widget buttons and notification actions all land here.
 */
class CheckInService(
    private val practices: PracticeRepository,
    private val clock: AppClock,
    private val hooks: AppHooks
) {

    /**
     * Serializes every check-in write: app taps, widget buttons and notification actions can race,
     * and read-modify-write updates (e.g. "+1") must never lose a step.
     */
    private val checkInLock = Mutex()

    private suspend fun <T> writing(block: suspend () -> T): T = checkInLock.withLock { block() }

    private suspend fun open(occurrenceId: String): Pair<Occurrence, Habit> {
        val occ = practices.getOccurrence(occurrenceId) ?: throw RoutineException("This habit is no longer scheduled.")
        if (occ.date > clock.today()) throw RoutineException("Future days can't be checked in yet.")
        val habit = practices.getHabit(occ.habitId) ?: throw RoutineException("This habit no longer exists.")
        return occ to habit
    }

    private suspend fun completeLocked(occurrenceId: String) {
        val (_, habit) = open(occurrenceId)
        val value = if (habit.type == HabitType.BOOLEAN) null else habit.targetValue
        practices.checkIn(occurrenceId, ActionStatus.COMPLETE, value, habit.durationMinutes, clock.nowIso(), clock.nowIso())
    }

    private suspend fun recordValueLocked(occurrenceId: String, value: Double) {
        val (_, habit) = open(occurrenceId)
        val v = value.coerceIn(0.0, 1_000_000.0)
        val done = v >= habit.targetValue
        val minutes = if (habit.type == HabitType.DURATION) v.toInt() else null
        practices.checkIn(
            occurrenceId,
            if (done) ActionStatus.COMPLETE else ActionStatus.PENDING,
            v,
            minutes,
            if (done) clock.nowIso() else null,
            clock.nowIso()
        )
    }

    /** One-tap completion. For measurable habits this records the full target. */
    suspend fun complete(occurrenceId: String) {
        writing { completeLocked(occurrenceId) }
        hooks.onCheckInChanged()
    }

    /** Minimum version of the habit (Sāma): still counts as done, recorded separately. */
    suspend fun completeMinimum(occurrenceId: String) {
        writing {
            val (_, habit) = open(occurrenceId)
            practices.checkIn(occurrenceId, ActionStatus.MINIMUM, null, habit.minimumMinutes, clock.nowIso(), clock.nowIso())
        }
        hooks.onCheckInChanged()
    }

    /** Records a value for count / duration / measurable habits; completes when the target is reached. */
    suspend fun recordValue(occurrenceId: String, value: Double) {
        writing { recordValueLocked(occurrenceId, value) }
        hooks.onCheckInChanged()
    }

    suspend fun increment(occurrenceId: String) {
        writing {
            val (occ, habit) = open(occurrenceId)
            recordValueLocked(occurrenceId, (occ.value ?: 0.0) + habit.quickStep())
        }
        hooks.onCheckInChanged()
    }

    /** Finishes a timer session; completes when the session reached the target duration. */
    suspend fun finishTimer(occurrenceId: String, elapsedMinutes: Int) {
        writing {
            val (_, habit) = open(occurrenceId)
            val minutes = elapsedMinutes.coerceAtLeast(1)
            val status = when {
                minutes >= habit.durationMinutes -> ActionStatus.COMPLETE
                minutes >= habit.minimumMinutes -> ActionStatus.MINIMUM
                else -> ActionStatus.PENDING
            }
            val done = status != ActionStatus.PENDING
            practices.checkIn(
                occurrenceId, status, if (habit.type == HabitType.DURATION) minutes.toDouble() else null,
                minutes, if (done) clock.nowIso() else null, clock.nowIso()
            )
        }
        hooks.onCheckInChanged()
    }

    /** Undo to open with no recorded value (used when there is no earlier state to restore). */
    suspend fun reopen(occurrenceId: String) {
        writing {
            open(occurrenceId)
            practices.checkIn(occurrenceId, ActionStatus.PENDING, null, null, null, clock.nowIso())
        }
        hooks.onCheckInChanged()
    }

    /**
     * Undo: puts an occurrence back exactly as it was before the last action — status, value and,
     * after a snooze or move, its time for today.
     */
    suspend fun restore(previous: Occurrence) {
        val timeChanged = writing {
            val (current, _) = open(previous.id)
            if (previous.isSkipped) {
                practices.skip(previous.id, previous.skipReason, clock.nowIso())
            } else {
                val status = if (previous.isOpen) ActionStatus.PENDING else previous.status
                practices.checkIn(previous.id, status, previous.value, previous.durationMinutes, previous.completedAt, clock.nowIso())
            }
            val moved = current.scheduledTime != previous.scheduledTime
            if (moved) practices.moveOccurrence(previous.id, previous.scheduledTime, previous.status, clock.nowIso())
            moved
        }
        // A restored time also moves today's reminder back.
        if (timeChanged) hooks.onRoutineChanged() else hooks.onCheckInChanged()
    }

    /** Circle tap: completes an open habit, or reopens a done / skipped one. Returns the state before. */
    suspend fun toggle(occurrenceId: String): Occurrence {
        val before = writing {
            val (occ, _) = open(occurrenceId)
            if (occ.isDone || occ.isSkipped) {
                practices.checkIn(occurrenceId, ActionStatus.PENDING, null, null, null, clock.nowIso())
            } else {
                completeLocked(occurrenceId)
            }
            occ
        }
        hooks.onCheckInChanged()
        return before
    }

    suspend fun skip(occurrenceId: String, reason: String?) {
        writing {
            open(occurrenceId)
            practices.skip(occurrenceId, reason?.take(80), clock.nowIso())
        }
        hooks.onCheckInChanged()
    }

    /** Pushes today's still-open occurrence later without touching the habit definition. */
    suspend fun snooze(occurrenceId: String, minutes: Int = com.vajrax.domain.BusinessRules.SNOOZE_MINUTES) {
        writing {
            val (occ, habit) = open(occurrenceId)
            if (occ.date != clock.today()) throw RoutineException("Only today's habits can be snoozed.")
            // A stale notification or widget must never overwrite a completion or a skip.
            if (!occ.isOpen) throw RoutineException("This habit is already done for today.")
            val base = maxOf(TimeFormat.toMinutes(occ.scheduledTime ?: habit.time) ?: clock.minuteOfDay(), clock.minuteOfDay())
            val target = (base + minutes).coerceAtMost(23 * 60 + 59)
            practices.moveOccurrence(occurrenceId, TimeFormat.fromMinutes(target), ActionStatus.SNOOZED, clock.nowIso())
        }
        // Reminders follow today's new time.
        hooks.onRoutineChanged()
    }

    /** Moves today's occurrence to a new time (history of other days unchanged). */
    suspend fun move(occurrenceId: String, time: String) {
        writing {
            val (occ, _) = open(occurrenceId)
            if (occ.date != clock.today()) throw RoutineException("Only today's habits can be moved.")
            val normalized = TimeFormat.normalize(time) ?: throw RoutineException("Pick a valid time.")
            val status = if (occ.isOpen) ActionStatus.PENDING else occ.status
            practices.moveOccurrence(occurrenceId, normalized, status, clock.nowIso())
        }
        hooks.onRoutineChanged()
    }

    suspend fun setNote(occurrenceId: String, note: String) {
        writing {
            open(occurrenceId)
            practices.setNote(occurrenceId, note.trim().ifBlank { null }, clock.nowIso())
        }
    }

    /** Explicit correction of a past day's record from the calendar (spec: explicit correction only). */
    suspend fun correctPastRecord(occurrenceId: String, done: Boolean) {
        writing {
            if (done) completeLocked(occurrenceId)
            else {
                open(occurrenceId)
                practices.checkIn(occurrenceId, ActionStatus.PENDING, null, null, null, clock.nowIso())
            }
        }
        hooks.onCheckInChanged()
    }

}
