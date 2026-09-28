@file:OptIn(ExperimentalUuidApi::class)

package com.vajrax.domain.usecase

import com.vajrax.core.time.AppClock
import com.vajrax.core.time.Dates
import com.vajrax.core.time.TimeFormat
import com.vajrax.domain.template.toHabitCopy
import com.vajrax.domain.habit.Habit
import com.vajrax.domain.habit.HabitType
import com.vajrax.domain.habit.Occurrence
import com.vajrax.domain.habit.OccurrencePlanner
import com.vajrax.domain.habit.ScheduleRules
import com.vajrax.domain.habit.ScheduleType
import com.vajrax.domain.habit.Tracker
import com.vajrax.domain.habit.TrackerStatus
import com.vajrax.domain.model.ActionStatus
import com.vajrax.domain.model.TrackingMode
import com.vajrax.domain.repository.PracticeRepository
import com.vajrax.domain.repository.ProfileRepository
import com.vajrax.domain.repository.SettingsRepository
import com.vajrax.domain.repository.TemplateRepository
import com.vajrax.domain.repository.TrackerRepository
import com.vajrax.domain.template.DefaultTemplate
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/** Platform side effects after data changes (widget refresh, reminder rescheduling). */
interface AppHooks {
    suspend fun onRoutineChanged() {}
    suspend fun onCheckInChanged() {}
}

object NoopAppHooks : AppHooks

/** Typed, user-presentable failures (spec 06 §9). */
class RoutineException(message: String) : Exception(message)

/**
 * Orchestrates every write that touches schedules or occurrences so that the rules in
 * [ScheduleRules] and [OccurrencePlanner] are applied the same way from the app, the widget
 * and notification actions (spec 06 §7: one repository/domain path for all entry points).
 */
class RoutineManager(
    private val practices: PracticeRepository,
    private val trackers: TrackerRepository,
    private val templates: TemplateRepository,
    private val settings: SettingsRepository,
    private val profiles: ProfileRepository,
    private val clock: AppClock,
    private val hooks: AppHooks = NoopAppHooks
) {
    private val materializeLock = Mutex()

    /** App start: seed the library, then create today's (and any missed days') occurrences. */
    suspend fun startup() {
        templates.seedSystemTemplatesIfNeeded()
        materialize()
    }

    /**
     * Creates PENDING occurrences for every scheduled habit from the day after the last run up
     * to today. Past days are frozen with the schedule that was valid when they were created.
     */
    suspend fun materialize(): Unit = materializeLock.withLock {
        val today = clock.today()
        val habits = practices.getAllTrackedHabits()
        if (habits.isEmpty()) {
            settings.put(SettingsRepository.LAST_MATERIALIZED, today.toString())
            return@withLock
        }
        val last = Dates.parse(settings.get(SettingsRepository.LAST_MATERIALIZED))
        val earliestStart = habits.mapNotNull { it.startDate }.minOrNull() ?: today
        val cap = today.minus(MAX_BACKFILL_DAYS, DateTimeUnit.DAY)
        var from = when {
            last == null -> earliestStart
            last >= today -> today
            else -> last.plus(1, DateTimeUnit.DAY)
        }
        if (from < cap) from = cap
        if (from > today) from = today
        val existing = practices.existingKeys(from, today)
        val planned = OccurrencePlanner.plan(habits, from, today, existing)
        practices.insertPlanned(planned, clock.nowIso())
        settings.put(SettingsRepository.LAST_MATERIALIZED, today.toString())
    }

    // ------------------------------------------------------------------ trackers

    /**
     * Creates the user's personal copy of [template] from [habits] (already customized in the UI)
     * and makes it the active tracker. The source template is never modified (FR-003, FR-004).
     */
    suspend fun activateTemplate(
        template: DefaultTemplate,
        habits: List<Habit>,
        startDate: LocalDate = clock.today(),
        name: String = template.name
    ): Tracker {
        if (habits.isEmpty()) throw RoutineException("Add at least one habit to start this routine.")
        habits.forEach { validate(it) }
        materialize()
        val today = clock.today()
        val tracker = Tracker(
            id = "trk_" + Uuid.random().toString(),
            templateId = template.id,
            name = name.trim().ifBlank { template.name },
            startDate = startDate,
            totalDays = template.durationDays.coerceIn(1, 366),
            isActive = true,
            status = TrackerStatus.ACTIVE,
            endedAt = null
        )
        val copies = habits.mapIndexed { index, h ->
            h.copy(
                id = "hab_" + Uuid.random().toString(),
                trackerId = tracker.id,
                startDate = startDate,
                archivedAt = null,
                sortOrder = index
            )
        }
        val userId = profiles.getProfile()?.userId ?: profiles.saveProfile("", "", clock.nowIso()).userId
        trackers.replaceActiveTracker(tracker, copies, userId, today, clock.nowIso())
        planToday(copies)
        settings.put(SettingsRepository.ONBOARDING_DONE, "true")
        hooks.onRoutineChanged()
        return tracker
    }

    /** Editable habit drafts for a template, times shifted so the first habit starts at [wakeTime]. */
    fun draftHabits(template: DefaultTemplate, wakeTime: String? = null): List<Habit> {
        val drafts = template.habits.sortedBy { it.sortOrder }.mapIndexed { i, h -> h.toHabitCopy("draft_$i", null, i) }
        val wake = TimeFormat.toMinutes(wakeTime) ?: return drafts
        val times = drafts.mapNotNull { TimeFormat.toMinutes(it.time) }
        val first = times.minOrNull() ?: return drafts
        val shift = wake - first
        // Only shift morning-anchored routines, never by more than three hours,
        // and never so far that a habit would cross midnight.
        if (first >= 12 * 60 || kotlin.math.abs(shift) > 180 || shift == 0) return drafts
        if (times.any { it + shift !in 0..(24 * 60 - 1) }) return drafts
        return drafts.map { d ->
            val t = TimeFormat.toMinutes(d.time) ?: return@map d
            val shifted = TimeFormat.fromMinutes(t + shift)
            d.copy(time = shifted, reminderTime = shifted)
        }
    }

    // ------------------------------------------------------------------ habits

    suspend fun addHabit(habit: Habit): Habit {
        validate(habit)
        val tracker = trackers.getActiveTracker() ?: throw RoutineException("Choose a routine first.")
        val today = clock.today()
        val existing = practices.getTrackerHabits(tracker.id)
        val created = habit.copy(
            id = "hab_" + Uuid.random().toString(),
            trackerId = tracker.id,
            startDate = habit.startDate ?: today,
            archivedAt = null,
            sortOrder = (existing.maxOfOrNull { it.sortOrder } ?: -1) + 1
        )
        practices.insertHabit(created, clock.nowIso())
        planToday(listOf(created))
        hooks.onRoutineChanged()
        return created
    }

    /**
     * Updates a habit definition. Past occurrences are materialized first so they keep the
     * schedule that applied on their dates (FR-014); only today's open occurrence is adjusted.
     *
     * Weekly targets are evaluated from the habit definition itself, so a change that involves a
     * weekly-target schedule would re-score past weeks. Such an edit instead archives the current
     * version (keeping its history and scoring) and continues with a new version from today.
     */
    suspend fun updateHabit(habit: Habit) {
        validate(habit)
        materialize()
        val previous = practices.getHabit(habit.id) ?: throw RoutineException("This habit no longer exists.")
        if (changesWeeklyScoring(previous, habit)) {
            val today = clock.today()
            practices.archiveHabit(previous.id, today)
            practices.deleteOpenOccurrences(previous.id, today)
            val next = habit.copy(
                id = "hab_" + Uuid.random().toString(),
                trackerId = previous.trackerId,
                startDate = today,
                archivedAt = null,
                sortOrder = previous.sortOrder
            )
            practices.insertHabit(next, clock.nowIso())
            planToday(listOf(next))
            hooks.onRoutineChanged()
            return
        }
        practices.updateHabit(habit)
        val today = clock.today()
        val updated = practices.getHabit(habit.id) ?: return
        val occ = practices.getOccurrence(updated.id, today)
        val scheduled = ScheduleRules.isScheduled(updated, today)
        when {
            occ == null && scheduled -> planToday(listOf(updated))
            occ != null && occ.isOpen && !scheduled -> practices.deleteOpenOccurrences(updated.id, today)
            // Keep a move / snooze made for today unless the habit's own time changed.
            occ != null && occ.isOpen && previous.time != updated.time ->
                practices.updateOpenOccurrenceTime(updated.id, today, updated.time)
        }
        hooks.onRoutineChanged()
    }

    private fun changesWeeklyScoring(before: Habit, after: Habit): Boolean {
        val weeklyBefore = before.schedule.type == ScheduleType.WEEKLY_TARGET
        val weeklyAfter = after.schedule.type == ScheduleType.WEEKLY_TARGET
        if (!weeklyBefore && !weeklyAfter) return false
        return weeklyBefore != weeklyAfter || before.schedule.weeklyTarget != after.schedule.weeklyTarget
    }

    /** Archives a habit: it disappears from today on, its history is kept (FR-007). */
    suspend fun archiveHabit(habitId: String) {
        materialize()
        val today = clock.today()
        practices.archiveHabit(habitId, today)
        practices.deleteOpenOccurrences(habitId, today)
        hooks.onRoutineChanged()
    }

    suspend fun reorderHabits(orderedIds: List<String>) {
        practices.reorderHabits(orderedIds)
        hooks.onRoutineChanged()
    }

    private suspend fun planToday(habits: List<Habit>) {
        val today = clock.today()
        val existing = practices.existingKeys(today, today)
        practices.insertPlanned(OccurrencePlanner.plan(habits, today, today, existing), clock.nowIso())
    }

    private fun validate(h: Habit) {
        if (h.title.isBlank()) throw RoutineException("Give the habit a name.")
        if (h.title.length > 60) throw RoutineException("Habit names can be up to 60 characters.")
        if (h.durationMinutes !in 1..1440) throw RoutineException("Duration must be between 1 minute and 24 hours.")
        if (h.type != HabitType.BOOLEAN && (h.targetValue <= 0 || h.targetValue > 1_000_000)) {
            throw RoutineException("Target must be a positive number.")
        }
        if (h.time != null && TimeFormat.toMinutes(h.time) == null) throw RoutineException("Pick a valid time.")
        if (h.schedule.days.isEmpty()) throw RoutineException("Pick at least one day.")
        if (h.schedule.weeklyTarget !in 1..7) throw RoutineException("Weekly target must be 1–7 times.")
        if (h.schedule.intervalDays !in 1..365) throw RoutineException("Interval must be 1–365 days.")
    }

    // ------------------------------------------------------------------ check-ins

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

    /** Undo: puts an occurrence back exactly as it was before the last action (status + value). */
    suspend fun restore(previous: Occurrence) {
        writing {
            open(previous.id)
            if (previous.isSkipped) {
                practices.skip(previous.id, previous.skipReason, clock.nowIso())
            } else {
                val status = if (previous.isOpen) ActionStatus.PENDING else previous.status
                practices.checkIn(previous.id, status, previous.value, previous.durationMinutes, previous.completedAt, clock.nowIso())
            }
        }
        hooks.onCheckInChanged()
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
    suspend fun snooze(occurrenceId: String, minutes: Int = 15) {
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

    companion object {
        const val MAX_BACKFILL_DAYS = 400

        /** Blank editable habit used by the habit editor. */
        fun blankHabit(time: String? = "09:00") = Habit(
            id = "draft_new",
            trackerId = null,
            title = "",
            category = "Routine",
            icon = "check",
            color = "indigo",
            time = time,
            durationMinutes = 15,
            minimumMinutes = 5,
            trackingMode = TrackingMode.MANUAL,
            reminderTime = time
        )
    }
}
