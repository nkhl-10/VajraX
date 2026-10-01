@file:OptIn(ExperimentalUuidApi::class)

package com.vajrax.domain.usecase

import com.vajrax.core.time.AppClock
import com.vajrax.core.time.TimeFormat
import com.vajrax.domain.habit.Habit
import com.vajrax.domain.habit.HabitType
import com.vajrax.domain.habit.Occurrence
import com.vajrax.domain.habit.OccurrencePlanner
import com.vajrax.domain.habit.ScheduleRules
import com.vajrax.domain.habit.ScheduleType
import com.vajrax.domain.habit.Tracker
import com.vajrax.domain.habit.TrackerStatus
import com.vajrax.domain.model.TrackingMode
import com.vajrax.domain.repository.PracticeRepository
import com.vajrax.domain.repository.ProfileRepository
import com.vajrax.domain.repository.SettingsRepository
import com.vajrax.domain.repository.TemplateRepository
import com.vajrax.domain.repository.TrackerRepository
import com.vajrax.domain.template.DefaultTemplate
import com.vajrax.domain.template.toHabitCopy
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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
    private val startupLock = Mutex()
    private var started = false

    /**
     * App start: seed the library, then create today's (and any missed days') occurrences.
     * Safe to call from several places at once (app, splash, onboarding): it runs once, unless
     * [force] (after "Delete all data").
     */
    suspend fun startup(force: Boolean = false) = startupLock.withLock {
        if (started && !force) return@withLock
        templates.seedSystemTemplatesIfNeeded()
        materialize()
        started = true
    }

    private val materializer = OccurrenceMaterializer(practices, settings, clock)

    /**
     * Creates PENDING occurrences for every scheduled habit up to today; past days keep the
     * schedule that was valid then (see [OccurrenceMaterializer]).
     */
    suspend fun materialize() = materializer.materialize()

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
        settings.put(SettingsRepository.ONBOARDING_PROGRESS, "")
        hooks.onRoutineChanged()
        return tracker
    }

    /**
     * Starts a routine that was set to begin on a later day today instead: moves its start date
     * and plans today's habits, so Home shows them right away.
     */
    suspend fun startToday() {
        val tracker = trackers.getActiveTracker() ?: throw RoutineException("Choose a routine first.")
        val today = clock.today()
        if (tracker.startDate <= today) return
        materialize()
        trackers.moveStart(tracker.id, today)
        planToday(practices.getTrackerHabits(tracker.id).filter { it.archivedAt == null })
        hooks.onRoutineChanged()
    }

    /** Renames the active routine (widgets show the name, so they refresh). */
    suspend fun renameRoutine(name: String) {
        val clean = name.trim()
        if (clean.isEmpty()) throw RoutineException("Give your routine a name.")
        val tracker = trackers.getActiveTracker() ?: throw RoutineException("Choose a routine first.")
        trackers.renameTracker(tracker.id, clean.take(40))
        hooks.onRoutineChanged()
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

    private suspend fun planToday(habits: List<Habit>) = materializer.planToday(habits)

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
    // Implemented by CheckInService; kept here so every entry point uses one object.

    private val checkIns = CheckInService(practices, clock, hooks)

    /** One-tap completion. For measurable habits this records the full target. */
    suspend fun complete(occurrenceId: String) = checkIns.complete(occurrenceId)

    /** Minimum version of the habit: still counts as done, recorded separately. */
    suspend fun completeMinimum(occurrenceId: String) = checkIns.completeMinimum(occurrenceId)

    /** Records a value for count / duration / measurable habits; completes at the target. */
    suspend fun recordValue(occurrenceId: String, value: Double) = checkIns.recordValue(occurrenceId, value)

    suspend fun increment(occurrenceId: String) = checkIns.increment(occurrenceId)

    /** Finishes a timer session; completes when the session reached the target duration. */
    suspend fun finishTimer(occurrenceId: String, elapsedMinutes: Int) = checkIns.finishTimer(occurrenceId, elapsedMinutes)

    suspend fun reopen(occurrenceId: String) = checkIns.reopen(occurrenceId)

    /** Undo: puts an occurrence back as it was (status, value and today's time). */
    suspend fun restore(previous: Occurrence) = checkIns.restore(previous)

    /** Circle tap: completes an open habit, or reopens a done / skipped one. Returns the state before. */
    suspend fun toggle(occurrenceId: String): Occurrence = checkIns.toggle(occurrenceId)

    suspend fun skip(occurrenceId: String, reason: String?) = checkIns.skip(occurrenceId, reason)

    /** Pushes today's still-open occurrence later without touching the habit definition. */
    suspend fun snooze(occurrenceId: String, minutes: Int = com.vajrax.domain.BusinessRules.SNOOZE_MINUTES) = checkIns.snooze(occurrenceId, minutes)

    /** Moves today's occurrence to a new time (history of other days unchanged). */
    suspend fun move(occurrenceId: String, time: String) = checkIns.move(occurrenceId, time)

    suspend fun setNote(occurrenceId: String, note: String) = checkIns.setNote(occurrenceId, note)

    /** Explicit correction of a past day's record from the calendar. */
    suspend fun correctPastRecord(occurrenceId: String, done: Boolean) = checkIns.correctPastRecord(occurrenceId, done)

    companion object {
        const val MAX_BACKFILL_DAYS = OccurrenceMaterializer.MAX_BACKFILL_DAYS

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
