package com.vajrax.domain.repository

import com.vajrax.domain.habit.Habit
import com.vajrax.domain.habit.Occurrence
import com.vajrax.domain.habit.OccurrencePlanner
import com.vajrax.domain.model.ActionStatus
import com.vajrax.domain.model.Practice
import com.vajrax.domain.model.ReflectionRating
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

/**
 * Habits (PracticeEntity) and their per-day occurrences (ActionRecordEntity).
 * The single persistence gateway used by the app screens, the widget and notification actions.
 */
interface PracticeRepository {
    // ---- Legacy VAJRAX engine API (kept for Learn / Path / engines) ----
    suspend fun getActivePractices(): List<Practice>
    suspend fun getAllPractices(): List<Practice>
    suspend fun insertPractice(practice: Practice)
    suspend fun togglePracticeActive(practiceId: String, active: Boolean)
    suspend fun getTodayTimeline(date: String): List<ActionTimelineEntityResult>
    suspend fun updateActionStatus(actionId: String, status: ActionStatus, durationMinutes: Int?)
    suspend fun submitEvidence(actionId: String, note: String?, rating: ReflectionRating?, mediaPath: String?)

    // ---- Habits ----
    fun observeTrackerHabits(trackerId: String): Flow<List<Habit>>
    suspend fun getTrackerHabits(trackerId: String): List<Habit>
    /** Every habit that ever belonged to a tracker, archived ones included (analytics need them). */
    fun observeAllTrackedHabits(): Flow<List<Habit>>
    suspend fun getAllTrackedHabits(): List<Habit>
    suspend fun getHabit(habitId: String): Habit?
    suspend fun insertHabit(habit: Habit, createdAt: String)
    suspend fun updateHabit(habit: Habit)
    suspend fun archiveHabit(habitId: String, date: LocalDate)
    suspend fun archiveTrackerHabits(trackerId: String, date: LocalDate)
    suspend fun reorderHabits(orderedIds: List<String>)

    // ---- Occurrences ----
    fun observeDay(date: LocalDate): Flow<List<Occurrence>>
    fun observeRange(from: LocalDate, to: LocalDate): Flow<List<Occurrence>>
    suspend fun getRange(from: LocalDate, to: LocalDate): List<Occurrence>
    suspend fun getHabitHistory(habitId: String, since: LocalDate): List<Occurrence>
    suspend fun getOccurrence(occurrenceId: String): Occurrence?
    suspend fun getOccurrence(habitId: String, date: LocalDate): Occurrence?
    suspend fun insertPlanned(planned: List<OccurrencePlanner.Planned>, nowIso: String)
    suspend fun existingKeys(from: LocalDate, to: LocalDate): Set<String>
    suspend fun deleteOpenOccurrences(habitId: String, fromDate: LocalDate)
    suspend fun deleteOpenTrackerOccurrences(trackerId: String, fromDate: LocalDate)
    suspend fun updateOpenOccurrenceTime(habitId: String, date: LocalDate, time: String?)
    suspend fun earliestRecordDate(): LocalDate?

    suspend fun checkIn(
        occurrenceId: String,
        status: ActionStatus,
        value: Double?,
        durationMinutes: Int?,
        completedAt: String?,
        nowIso: String
    )
    suspend fun skip(occurrenceId: String, reason: String?, nowIso: String)
    suspend fun moveOccurrence(occurrenceId: String, time: String?, status: ActionStatus, nowIso: String)
    suspend fun setNote(occurrenceId: String, note: String?, nowIso: String)
}

data class ActionTimelineEntityResult(
    val id: String,
    val practiceId: String,
    val date: String,
    val scheduledTime: String?,
    val status: ActionStatus,
    val completedAt: String?,
    val durationMinutes: Int?,
    val title: String,
    val targetDurationMinutes: Int,
    val minimumDurationMinutes: Int,
    val trackingMode: com.vajrax.domain.model.TrackingMode
)
