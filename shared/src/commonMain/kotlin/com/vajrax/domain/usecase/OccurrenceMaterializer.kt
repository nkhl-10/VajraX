package com.vajrax.domain.usecase

import com.vajrax.core.time.AppClock
import com.vajrax.core.time.Dates
import com.vajrax.domain.habit.Habit
import com.vajrax.domain.habit.OccurrencePlanner
import com.vajrax.domain.repository.PracticeRepository
import com.vajrax.domain.repository.SettingsRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.minus
import kotlinx.datetime.plus

/** Turns habit schedules into dated occurrences, once per day, without rewriting history. */
class OccurrenceMaterializer(
    private val practices: PracticeRepository,
    private val settings: SettingsRepository,
    private val clock: AppClock
) {
    private val lock = Mutex()

    /**
     * Creates PENDING occurrences for every scheduled habit from the day after the last run up
     * to today. Past days are frozen with the schedule that was valid when they were created.
     */
    suspend fun materialize(): Unit = lock.withLock {
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

    /** Creates today's missing occurrences for [habits] (after activation or edits). */
    suspend fun planToday(habits: List<Habit>) {
        val today = clock.today()
        val existing = practices.existingKeys(today, today)
        practices.insertPlanned(OccurrencePlanner.plan(habits, today, today, existing), clock.nowIso())
    }

    companion object {
        /** Longest gap (days the app wasn't opened) that is back-filled. */
        const val MAX_BACKFILL_DAYS = 400
    }
}
