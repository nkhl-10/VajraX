@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.vajrax.data.repository

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.vajrax.core.time.Dates
import com.vajrax.core.time.TimeFormat
import com.vajrax.data.local.DbDispatcher
import com.vajrax.data.local.VajraDatabase
import com.vajrax.data.local.io
import com.vajrax.data.local.toDb
import com.vajrax.data.local.toHabit
import com.vajrax.data.local.toOccurrence
import com.vajrax.data.local.trackingModeOf
import com.vajrax.domain.habit.Habit
import com.vajrax.domain.habit.Occurrence
import com.vajrax.domain.habit.OccurrencePlanner
import com.vajrax.domain.model.ActionStatus
import com.vajrax.domain.model.Practice
import com.vajrax.domain.model.ReflectionRating
import com.vajrax.domain.repository.ActionTimelineEntityResult
import com.vajrax.domain.repository.PracticeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate

class PracticeRepositoryImpl(
    private val database: VajraDatabase
) : PracticeRepository {
    private val queries = database.vajraDatabaseQueries

    // ---------------------------------------------------------------- legacy engine API

    private fun com.vajrax.data.local.PracticeEntity.toPractice() = Practice(
        id = id,
        principleId = principleId,
        title = title,
        targetDurationMinutes = targetDurationMinutes.toInt(),
        minimumDurationMinutes = minimumDurationMinutes.toInt(),
        preferredTime = preferredTime,
        trackingMode = trackingModeOf(trackingMode),
        isActive = isActive == 1L
    )

    override suspend fun getActivePractices(): List<Practice> = io {
        queries.getActivePractices().awaitAsList().map { it.toPractice() }
    }

    override suspend fun getAllPractices(): List<Practice> = io {
        queries.getAllPractices().awaitAsList().map { it.toPractice() }
    }

    override suspend fun insertPractice(practice: Practice): Unit = io {
        queries.insertPractice(
            id = practice.id,
            principleId = practice.principleId,
            title = practice.title,
            targetDurationMinutes = practice.targetDurationMinutes.toLong(),
            minimumDurationMinutes = practice.minimumDurationMinutes.toLong(),
            preferredTime = TimeFormat.normalize(practice.preferredTime) ?: practice.preferredTime,
            trackingMode = practice.trackingMode.name,
            isActive = practice.isActive.toDb()
        )
    }

    override suspend fun togglePracticeActive(practiceId: String, active: Boolean): Unit = io {
        queries.togglePracticeActive(active.toDb(), practiceId)
    }

    override suspend fun getTodayTimeline(date: String): List<ActionTimelineEntityResult> = io {
        queries.getTodayTimeline(date).awaitAsList().map {
            ActionTimelineEntityResult(
                id = it.id,
                practiceId = it.practiceId,
                date = it.date,
                scheduledTime = it.scheduledTime,
                status = ActionStatus.of(it.status),
                completedAt = it.completedAt,
                durationMinutes = it.durationMinutes?.toInt(),
                title = it.title,
                targetDurationMinutes = it.targetDurationMinutes.toInt(),
                minimumDurationMinutes = it.minimumDurationMinutes.toInt(),
                trackingMode = trackingModeOf(it.trackingMode)
            )
        }
    }

    override suspend fun updateActionStatus(actionId: String, status: ActionStatus, durationMinutes: Int?): Unit = io {
        queries.updateActionStatus(
            status = status.name,
            completedAt = null,
            durationMinutes = durationMinutes?.toLong(),
            id = actionId
        )
    }

    override suspend fun submitEvidence(
        actionId: String,
        note: String?,
        rating: ReflectionRating?,
        mediaPath: String?
    ): Unit = io {
        val createdAt = kotlin.time.Clock.System.now().toString()
        queries.insertEvidence(
            id = "ev_${actionId}_$createdAt",
            actionRecordId = actionId,
            reflectionRating = rating?.name,
            note = note,
            mediaPath = mediaPath,
            createdAt = createdAt
        )
    }

    // ---------------------------------------------------------------- habits

    override fun observeTrackerHabits(trackerId: String): Flow<List<Habit>> =
        queries.getHabitsForTracker(trackerId).asFlow().mapToList(DbDispatcher)
            .map { list -> list.map { it.toHabit() }.sortedForTimeline() }

    override suspend fun getTrackerHabits(trackerId: String): List<Habit> = io {
        queries.getHabitsForTracker(trackerId).awaitAsList().map { it.toHabit() }.sortedForTimeline()
    }

    override fun observeAllTrackedHabits(): Flow<List<Habit>> =
        queries.getTrackedHabits().asFlow().mapToList(DbDispatcher).map { list -> list.map { it.toHabit() } }

    override suspend fun getAllTrackedHabits(): List<Habit> = io {
        queries.getTrackedHabits().awaitAsList().map { it.toHabit() }
    }

    override suspend fun getHabit(habitId: String): Habit? = io {
        queries.getHabitById(habitId).awaitAsOneOrNull()?.toHabit()
    }

    override suspend fun insertHabit(habit: Habit, createdAt: String): Unit = io { insertHabitBlocking(habit, createdAt) }

    internal suspend fun insertHabitBlocking(habit: Habit, createdAt: String) {
        queries.insertHabit(
            id = habit.id,
            principleId = null,
            title = habit.title,
            targetDurationMinutes = habit.durationMinutes.toLong(),
            minimumDurationMinutes = habit.minimumMinutes.toLong(),
            preferredTime = habit.time,
            trackingMode = habit.trackingMode.name,
            isActive = (!habit.isArchived).toDb(),
            trackerId = habit.trackerId,
            category = habit.category,
            icon = habit.icon,
            color = habit.color,
            habitType = habit.type.name,
            targetValue = habit.targetValue,
            unit = habit.unit,
            scheduleType = habit.schedule.type.name,
            scheduleDays = habit.schedule.encodeDays(),
            weeklyTarget = habit.schedule.weeklyTarget.toLong(),
            intervalDays = habit.schedule.intervalDays.toLong(),
            reminderEnabled = habit.reminderEnabled.toDb(),
            reminderTime = habit.reminderTime,
            startDate = habit.startDate?.toString(),
            archivedAt = habit.archivedAt?.toString(),
            sortOrder = habit.sortOrder.toLong(),
            createdAt = createdAt
        )
    }

    override suspend fun updateHabit(habit: Habit): Unit = io {
        queries.updateHabit(
            title = habit.title,
            targetDurationMinutes = habit.durationMinutes.toLong(),
            minimumDurationMinutes = habit.minimumMinutes.toLong(),
            preferredTime = habit.time,
            trackingMode = habit.trackingMode.name,
            category = habit.category,
            icon = habit.icon,
            color = habit.color,
            habitType = habit.type.name,
            targetValue = habit.targetValue,
            unit = habit.unit,
            scheduleType = habit.schedule.type.name,
            scheduleDays = habit.schedule.encodeDays(),
            weeklyTarget = habit.schedule.weeklyTarget.toLong(),
            intervalDays = habit.schedule.intervalDays.toLong(),
            reminderEnabled = habit.reminderEnabled.toDb(),
            reminderTime = habit.reminderTime,
            startDate = habit.startDate?.toString(),
            id = habit.id
        )
    }

    override suspend fun archiveHabit(habitId: String, date: LocalDate): Unit = io {
        queries.archiveHabit(date.toString(), habitId)
    }

    override suspend fun archiveTrackerHabits(trackerId: String, date: LocalDate): Unit = io {
        queries.archiveHabitsForTracker(date.toString(), trackerId)
    }

    override suspend fun reorderHabits(orderedIds: List<String>): Unit = io {
        database.transaction {
            orderedIds.forEachIndexed { index, id -> queries.updateHabitSortOrder(index.toLong(), id) }
        }
    }

    // ---------------------------------------------------------------- occurrences

    override fun observeDay(date: LocalDate): Flow<List<Occurrence>> =
        queries.getTimelineForDate(date.toString()).asFlow().mapToList(DbDispatcher)
            .map { list -> list.map { it.toOccurrence() } }

    override fun observeRange(from: LocalDate, to: LocalDate): Flow<List<Occurrence>> =
        queries.getRecordsBetween(from.toString(), to.toString()).asFlow().mapToList(DbDispatcher)
            .map { list -> list.map { it.toOccurrence() } }

    override suspend fun getRange(from: LocalDate, to: LocalDate): List<Occurrence> = io {
        queries.getRecordsBetween(from.toString(), to.toString()).awaitAsList().map { it.toOccurrence() }
    }

    override suspend fun getHabitHistory(habitId: String, since: LocalDate): List<Occurrence> = io {
        queries.getRecordsForHabitSince(habitId, since.toString()).awaitAsList().map { it.toOccurrence() }
    }

    override suspend fun getOccurrence(occurrenceId: String): Occurrence? = io {
        queries.getOccurrenceById(occurrenceId).awaitAsOneOrNull()?.toOccurrence()
    }

    override suspend fun getOccurrence(habitId: String, date: LocalDate): Occurrence? = io {
        queries.getOccurrence(habitId, date.toString()).awaitAsOneOrNull()?.toOccurrence()
    }

    override suspend fun insertPlanned(planned: List<OccurrencePlanner.Planned>, nowIso: String): Unit = io {
        if (planned.isEmpty()) return@io
        database.transaction {
            planned.forEach { p ->
                queries.insertOccurrenceIfAbsent(
                    id = p.id,
                    practiceId = p.habitId,
                    date = p.date.toString(),
                    scheduledTime = p.time,
                    updatedAt = nowIso
                )
            }
        }
    }

    override suspend fun existingKeys(from: LocalDate, to: LocalDate): Set<String> = io {
        queries.getRecordsBetween(from.toString(), to.toString()).awaitAsList()
            .mapTo(HashSet()) { "${it.practiceId}|${it.date}" }
    }

    override suspend fun deleteOpenOccurrences(habitId: String, fromDate: LocalDate): Unit = io {
        queries.deletePendingOccurrence(habitId, fromDate.toString())
    }

    override suspend fun deleteOpenTrackerOccurrences(trackerId: String, fromDate: LocalDate): Unit = io {
        queries.deletePendingOccurrencesForTracker(fromDate.toString(), trackerId)
    }

    override suspend fun updateOpenOccurrenceTime(habitId: String, date: LocalDate, time: String?): Unit = io {
        queries.updatePendingOccurrenceTime(time, habitId, date.toString())
    }

    override suspend fun earliestRecordDate(): LocalDate? = io {
        Dates.parse(queries.earliestRecordDate().awaitAsOneOrNull()?.minDate)
    }

    override suspend fun checkIn(
        occurrenceId: String,
        status: ActionStatus,
        value: Double?,
        durationMinutes: Int?,
        completedAt: String?,
        nowIso: String
    ): Unit = io {
        queries.updateCheckIn(
            status = status.name,
            completedAt = completedAt,
            durationMinutes = durationMinutes?.toLong(),
            value_ = value,
            updatedAt = nowIso,
            id = occurrenceId
        )
    }

    override suspend fun skip(occurrenceId: String, reason: String?, nowIso: String): Unit = io {
        queries.updateSkip(reason, nowIso, occurrenceId)
    }

    override suspend fun moveOccurrence(occurrenceId: String, time: String?, status: ActionStatus, nowIso: String): Unit = io {
        queries.updateOccurrenceTime(time, status.name, nowIso, occurrenceId)
    }

    override suspend fun setNote(occurrenceId: String, note: String?, nowIso: String): Unit = io {
        queries.updateOccurrenceNote(note?.take(500), nowIso, occurrenceId)
    }
}

/** Timeline order: timed habits by time, then anytime habits by their sort order. */
internal fun List<Habit>.sortedForTimeline(): List<Habit> =
    sortedWith(compareBy<Habit> { TimeFormat.toMinutes(it.time) ?: Int.MAX_VALUE }.thenBy { it.sortOrder })
