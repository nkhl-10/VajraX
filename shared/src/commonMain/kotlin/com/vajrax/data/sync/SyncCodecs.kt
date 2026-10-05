package com.vajrax.data.sync

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import com.vajrax.contract.ContractJson
import com.vajrax.contract.sync.GoalPayload
import com.vajrax.contract.sync.HabitPayload
import com.vajrax.contract.sync.OccurrencePayload
import com.vajrax.contract.sync.ReflectionPayload
import com.vajrax.contract.sync.SettingPayload
import com.vajrax.contract.sync.SyncEntity
import com.vajrax.contract.sync.TemplateHabitPayload
import com.vajrax.contract.sync.TemplatePayload
import com.vajrax.contract.sync.TrackerPayload
import com.vajrax.data.local.VajraDatabase
import com.vajrax.data.repository.ProfileRepositoryImpl
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Local rows ↔ sync payloads. [read] returns null when the record no longer exists (it then
 * travels as a deletion); [write] and [delete] apply a downloaded record. Callers run them inside
 * a transaction with SyncState.applying = 1 so the change-capture triggers stay quiet.
 */
internal class SyncCodecs(database: VajraDatabase) {
    private val q = database.vajraDatabaseQueries

    @Suppress("CyclomaticComplexMethod", "LongMethod") // one branch per synced record type
    suspend fun read(entity: String, id: String): JsonObject? = when (entity) {
        SyncEntity.TRACKER -> q.getTrackerById(id).awaitAsOneOrNull()?.let {
            encode(
                TrackerPayload.serializer(),
                TrackerPayload(
                    templateId = it.templateId, name = it.name, startDate = it.startDate, totalDays = it.totalDays.toInt(),
                    currentDay = it.currentDay.toInt(), progressPercent = it.progressPercent.toInt(), isActive = it.isActive == 1L,
                    status = it.status, endedAt = it.endedAt
                )
            )
        }
        SyncEntity.HABIT -> q.getHabitById(id).awaitAsOneOrNull()?.takeIf { it.trackerId != null }?.let {
            encode(
                HabitPayload.serializer(),
                HabitPayload(
                    trackerId = it.trackerId, title = it.title, targetDurationMinutes = it.targetDurationMinutes.toInt(),
                    minimumDurationMinutes = it.minimumDurationMinutes.toInt(), preferredTime = it.preferredTime,
                    trackingMode = it.trackingMode, isActive = it.isActive == 1L, category = it.category, icon = it.icon,
                    color = it.color, habitType = it.habitType, targetValue = it.targetValue, unit = it.unit,
                    scheduleType = it.scheduleType, scheduleDays = it.scheduleDays, weeklyTarget = it.weeklyTarget.toInt(),
                    intervalDays = it.intervalDays.toInt(), reminderEnabled = it.reminderEnabled == 1L,
                    reminderTime = it.reminderTime, startDate = it.startDate, archivedAt = it.archivedAt,
                    sortOrder = it.sortOrder.toInt(), createdAt = it.createdAt
                )
            )
        }
        SyncEntity.OCCURRENCE -> q.getOccurrenceById(id).awaitAsOneOrNull()?.let {
            encode(
                OccurrencePayload.serializer(),
                OccurrencePayload(
                    habitId = it.practiceId, date = it.date, scheduledTime = it.scheduledTime, status = it.status,
                    completedAt = it.completedAt, durationMinutes = it.durationMinutes?.toInt(), value = it.value_,
                    note = it.note, skipReason = it.skipReason
                )
            )
        }
        SyncEntity.GOAL -> q.getGoalById(id).awaitAsOneOrNull()?.let {
            encode(
                GoalPayload.serializer(),
                GoalPayload(
                    title = it.title, description = it.description, targetType = it.targetType, targetValue = it.targetValue,
                    startDate = it.startDate, endDate = it.endDate, status = it.status, createdAt = it.createdAt,
                    habitIds = q.getGoalHabitIds(id).awaitAsList()
                )
            )
        }
        SyncEntity.REFLECTION -> q.getReflectionById(id).awaitAsOneOrNull()?.let {
            encode(
                ReflectionPayload.serializer(),
                ReflectionPayload(
                    it.periodType,
                    it.periodStart,
                    it.achievements,
                    it.obstacles,
                    it.nextActions,
                    it.createdAt,
                    it.updatedAt
                )
            )
        }
        SyncEntity.TEMPLATE -> q.getTemplateById(id).awaitAsOneOrNull()?.takeIf { it.isCustom == 1L }?.let { t ->
            val habits = q.getHabitsForTemplate(id).awaitAsList().map {
                TemplateHabitPayload(
                    name = it.name, startTime = it.startTime, durationMinutes = it.durationMinutes.toInt(),
                    trackingMode = it.trackingMode, target = it.target, repeatDays = it.repeatDays,
                    reminderEnabled = it.reminderEnabled == 1L, sortOrder = it.sortOrder.toInt(), category = it.category,
                    icon = it.icon, color = it.color, habitType = it.habitType, targetValue = it.targetValue, unit = it.unit,
                    scheduleType = it.scheduleType, weeklyTarget = it.weeklyTarget.toInt(), intervalDays = it.intervalDays.toInt()
                )
            }
            encode(
                TemplatePayload.serializer(),
                TemplatePayload(
                    title = t.title, description = t.description, category = t.category, frequency = t.frequency,
                    author = t.author, durationDays = t.durationDays.toInt(), isDraft = t.isDraft == 1L,
                    recommendedFor = t.recommendedFor, updatedAt = t.updatedAt, habits = habits
                )
            )
        }
        SyncEntity.SETTING -> q.getSetting(
            id
        ).awaitAsOneOrNull()?.let { encode(SettingPayload.serializer(), SettingPayload(it)) }
        else -> null
    }

    @OptIn(ExperimentalTime::class)
    @Suppress("CyclomaticComplexMethod") // one branch per synced record type
    suspend fun write(entity: String, id: String, payload: JsonObject, updatedAt: Long) {
        when (entity) {
            SyncEntity.TRACKER -> decode(TrackerPayload.serializer(), payload).run {
                q.insertTracker(
                    id, ProfileRepositoryImpl.LOCAL_USER_ID, templateId, currentDay.toLong(), totalDays.toLong(),
                    progressPercent.toLong(), if (isActive) 1L else 0L, name, startDate, status, endedAt
                )
            }
            SyncEntity.HABIT -> decode(HabitPayload.serializer(), payload).run {
                q.insertHabit(
                    id, null, title, targetDurationMinutes.toLong(), minimumDurationMinutes.toLong(), preferredTime, trackingMode,
                    if (isActive) 1L else 0L, trackerId, category, icon, color, habitType, targetValue, unit, scheduleType,
                    scheduleDays, weeklyTarget.toLong(), intervalDays.toLong(), if (reminderEnabled) 1L else 0L, reminderTime,
                    startDate, archivedAt, sortOrder.toLong(), createdAt
                )
            }
            SyncEntity.OCCURRENCE -> decode(OccurrencePayload.serializer(), payload).run {
                q.upsertOccurrence(
                    id, habitId, date, scheduledTime, status, completedAt, durationMinutes?.toLong(), value, note, skipReason,
                    Instant.fromEpochMilliseconds(updatedAt).toString()
                )
            }
            SyncEntity.GOAL -> decode(GoalPayload.serializer(), payload).run {
                q.upsertGoal(id, title, description, targetType, targetValue, startDate, endDate, status, createdAt)
                q.deleteGoalHabits(id)
                habitIds.distinct().forEach { q.insertGoalHabit(id, it, 1.0) }
            }
            SyncEntity.REFLECTION -> decode(ReflectionPayload.serializer(), payload).run {
                q.upsertReflection(
                    id,
                    periodType,
                    periodStart,
                    achievements,
                    obstacles,
                    nextActions,
                    createdAt,
                    this.updatedAt
                )
            }
            SyncEntity.TEMPLATE -> decode(TemplatePayload.serializer(), payload).run {
                q.upsertTemplate(
                    id, title, description, habits.size.toLong(), frequency, author, 0L, 0L, 1L, category, durationDays.toLong(),
                    if (isDraft) 1L else 0L, recommendedFor, this.updatedAt
                )
                q.deleteTemplateHabits(id)
                habits.forEachIndexed { index, h ->
                    q.insertTemplateHabit(
                        "${id}_habit_$index", id, h.name, h.startTime, h.durationMinutes.toLong(), h.trackingMode, h.target,
                        h.repeatDays, if (h.reminderEnabled) 1L else 0L, index.toLong(), h.category, h.icon, h.color, h.habitType,
                        h.targetValue, h.unit, h.scheduleType, h.weeklyTarget.toLong(), h.intervalDays.toLong()
                    )
                }
            }
            SyncEntity.SETTING -> q.putSetting(id, decode(SettingPayload.serializer(), payload).value)
        }
    }

    suspend fun delete(entity: String, id: String) {
        when (entity) {
            SyncEntity.TRACKER -> q.deleteTrackerById(id)
            SyncEntity.HABIT -> q.deleteHabitById(id)
            SyncEntity.OCCURRENCE -> q.deleteOccurrenceById(id)
            SyncEntity.GOAL -> {
                q.deleteGoalHabits(id)
                q.deleteGoal(id)
            }
            SyncEntity.REFLECTION -> q.deleteReflectionById(id)
            SyncEntity.TEMPLATE -> {
                q.deleteTemplateHabits(id)
                q.deleteTemplate(id)
            }
            SyncEntity.SETTING -> q.deleteSetting(id)
        }
    }

    private fun <T> encode(serializer: KSerializer<T>, value: T): JsonObject = ContractJson.encodeToJsonElement(
        serializer,
        value
    ).jsonObject

    private fun <T> decode(serializer: KSerializer<T>, payload: JsonObject): T = ContractJson.decodeFromJsonElement(
        serializer,
        payload
    )
}
