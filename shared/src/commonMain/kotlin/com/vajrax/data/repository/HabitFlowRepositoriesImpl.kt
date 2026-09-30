package com.vajrax.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.vajrax.core.time.Dates
import com.vajrax.data.local.DbDispatcher
import com.vajrax.data.local.VajraDatabase
import com.vajrax.data.local.io
import com.vajrax.data.local.toDb
import com.vajrax.data.local.toHabit
import com.vajrax.data.local.toOccurrence
import com.vajrax.data.local.toTracker
import com.vajrax.domain.habit.Habit
import com.vajrax.domain.habit.Tracker
import com.vajrax.domain.repository.DataRepository
import com.vajrax.domain.repository.Goal
import com.vajrax.domain.repository.GoalRepository
import com.vajrax.domain.repository.GoalTargetType
import com.vajrax.domain.repository.ProfileRepository
import com.vajrax.domain.repository.Reflection
import com.vajrax.domain.repository.ReflectionPeriod
import com.vajrax.domain.repository.ReflectionRepository
import com.vajrax.domain.repository.SettingsRepository
import com.vajrax.domain.repository.TrackerRepository
import com.vajrax.domain.repository.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray

class TrackerRepositoryImpl(
    private val database: VajraDatabase,
    private val practiceRepository: PracticeRepositoryImpl
) : TrackerRepository {
    private val queries = database.vajraDatabaseQueries

    override fun observeActiveTracker(): Flow<Tracker?> =
        queries.getActiveTracker().asFlow().mapToOneOrNull(DbDispatcher).map { it?.toTracker() }

    override suspend fun getActiveTracker(): Tracker? = io { queries.getActiveTracker().executeAsOneOrNull()?.toTracker() }

    override suspend fun getAllTrackers(): List<Tracker> = io { queries.getAllTrackers().executeAsList().map { it.toTracker() } }

    override suspend fun getTracker(id: String): Tracker? = io { queries.getTrackerById(id).executeAsOneOrNull()?.toTracker() }

    override suspend fun replaceActiveTracker(
        tracker: Tracker,
        habits: List<Habit>,
        userId: String,
        today: LocalDate,
        nowIso: String
    ): Unit = io {
        database.transaction {
            queries.getAllTrackers().executeAsList().filter { it.isActive == 1L }.forEach { old ->
                // History stays; only today's and future open occurrences of the old routine go away.
                queries.deletePendingOccurrencesForTracker(today.toString(), old.id)
                queries.archiveHabitsForTracker(today.toString(), old.id)
            }
            queries.archiveActiveTrackers(today.toString())
            queries.insertTracker(
                id = tracker.id,
                userId = userId,
                templateId = tracker.templateId,
                currentDay = 1L,
                totalDays = tracker.totalDays.toLong(),
                progressPercent = 0L,
                isActive = 1L,
                name = tracker.name,
                startDate = tracker.startDate.toString(),
                status = tracker.status.name,
                endedAt = null
            )
            habits.forEach { practiceRepository.insertHabitBlocking(it.copy(trackerId = tracker.id), nowIso) }
        }
    }

    override suspend fun renameTracker(id: String, name: String): Unit = io { queries.renameTracker(name.trim(), id) }

    override suspend fun moveStart(id: String, date: LocalDate): Unit = io {
        database.transaction {
            queries.moveTrackerStart(date.toString(), id)
            queries.moveTrackerHabitsStart(date.toString(), id)
        }
    }
}

class SettingsRepositoryImpl(private val database: VajraDatabase) : SettingsRepository {
    private val queries = database.vajraDatabaseQueries

    override fun observe(key: String): Flow<String?> = queries.getSetting(key).asFlow().mapToOneOrNull(DbDispatcher)

    override suspend fun get(key: String): String? = io { queries.getSetting(key).executeAsOneOrNull() }

    override suspend fun put(key: String, value: String): Unit = io { queries.putSetting(key, value) }
}

class ProfileRepositoryImpl(private val database: VajraDatabase) : ProfileRepository {
    private val queries = database.vajraDatabaseQueries

    private fun com.vajrax.data.local.UserSessionEntity.toProfile() = UserProfile(userId, displayName, email)

    override fun observeProfile(): Flow<UserProfile?> =
        queries.getCurrentUser().asFlow().mapToOneOrNull(DbDispatcher).map { it?.toProfile() }

    override suspend fun getProfile(): UserProfile? = io { queries.getCurrentUser().executeAsOneOrNull()?.toProfile() }

    override suspend fun saveProfile(displayName: String, email: String, nowIso: String): UserProfile = io {
        val name = displayName.trim().take(40)
        val mail = email.trim().take(80)
        val existing = queries.getCurrentUser().executeAsOneOrNull()
        if (existing == null) {
            queries.insertOrUpdateUser(LOCAL_USER_ID, mail, name, 0L, null, nowIso)
            UserProfile(LOCAL_USER_ID, name, mail)
        } else {
            queries.updateUserProfile(name, mail, existing.userId)
            UserProfile(existing.userId, name, mail)
        }
    }

    companion object {
        const val LOCAL_USER_ID = "local_user"
    }
}

class GoalRepositoryImpl(private val database: VajraDatabase) : GoalRepository {
    private val queries = database.vajraDatabaseQueries

    override fun observeGoals(): Flow<List<Goal>> = combine(
        queries.getGoals().asFlow().mapToList(DbDispatcher),
        queries.getAllGoalHabits().asFlow().mapToList(DbDispatcher)
    ) { goals, links ->
        val byGoal = links.groupBy { it.goalId }
        goals.map { g ->
            Goal(
                id = g.id,
                title = g.title,
                description = g.description,
                targetType = if (g.targetType == GoalTargetType.RATE.name) GoalTargetType.RATE else GoalTargetType.COMPLETIONS,
                targetValue = g.targetValue,
                startDate = Dates.parse(g.startDate) ?: LocalDate(1970, 1, 1),
                endDate = Dates.parse(g.endDate),
                habitIds = byGoal[g.id].orEmpty().map { it.habitId }
            )
        }
    }

    override suspend fun saveGoal(goal: Goal, nowIso: String): Unit = io {
        database.transaction {
            queries.upsertGoal(
                id = goal.id,
                title = goal.title.trim().take(60),
                description = goal.description?.trim()?.take(200),
                targetType = goal.targetType.name,
                targetValue = goal.targetValue,
                startDate = goal.startDate.toString(),
                endDate = goal.endDate?.toString(),
                status = "ACTIVE",
                createdAt = nowIso
            )
            queries.deleteGoalHabits(goal.id)
            goal.habitIds.distinct().forEach { queries.insertGoalHabit(goal.id, it, 1.0) }
        }
    }

    override suspend fun deleteGoal(goalId: String): Unit = io {
        database.transaction {
            queries.deleteGoalHabits(goalId)
            queries.deleteGoal(goalId)
        }
    }
}

class ReflectionRepositoryImpl(private val database: VajraDatabase) : ReflectionRepository {
    private val queries = database.vajraDatabaseQueries

    override fun observeReflection(periodType: ReflectionPeriod, periodStart: LocalDate): Flow<Reflection?> =
        queries.getReflection(periodType.name, periodStart.toString()).asFlow().mapToOneOrNull(DbDispatcher).map { r ->
            r?.let { Reflection(periodType, periodStart, it.achievements, it.obstacles, it.nextActions) }
        }

    override suspend fun saveReflection(reflection: Reflection, nowIso: String): Unit = io {
        val existing = queries.getReflection(reflection.periodType.name, reflection.periodStart.toString()).executeAsOneOrNull()
        queries.upsertReflection(
            id = existing?.id ?: "refl_${reflection.periodType.name.lowercase()}_${reflection.periodStart}",
            periodType = reflection.periodType.name,
            periodStart = reflection.periodStart.toString(),
            achievements = reflection.achievements.trim().take(1000),
            obstacles = reflection.obstacles.trim().take(1000),
            nextActions = reflection.nextActions.trim().take(1000),
            createdAt = existing?.createdAt ?: nowIso,
            updatedAt = nowIso
        )
    }
}

class DataRepositoryImpl(private val database: VajraDatabase) : DataRepository {
    private val queries = database.vajraDatabaseQueries
    private val json = Json { prettyPrint = true }

    override suspend fun exportJson(exportedAt: String): String = io {
        val profile = queries.getCurrentUser().executeAsOneOrNull()
        val root: JsonObject = buildJsonObject {
            put("format", "vajrax-export")
            put("version", 2)
            put("exportedAt", exportedAt)
            put("profile", buildJsonObject {
                put("displayName", profile?.displayName ?: "")
                put("email", profile?.email ?: "")
            })
            putJsonArray("trackers") {
                queries.getAllTrackers().executeAsList().forEach { t ->
                    add(buildJsonObject {
                        put("id", t.id); put("templateId", t.templateId); put("name", t.name ?: "")
                        put("startDate", t.startDate ?: ""); put("status", t.status); put("endedAt", t.endedAt ?: "")
                    })
                }
            }
            putJsonArray("habits") {
                queries.getTrackedHabits().executeAsList().map { it.toHabit() }.forEach { h ->
                    add(buildJsonObject {
                        put("id", h.id); put("trackerId", h.trackerId ?: ""); put("name", h.title)
                        put("category", h.category); put("type", h.type.name); put("target", h.targetValue)
                        put("unit", h.unit ?: ""); put("time", h.time ?: ""); put("durationMinutes", h.durationMinutes)
                        put("schedule", h.schedule.type.name); put("days", h.schedule.encodeDays())
                        put("weeklyTarget", h.schedule.weeklyTarget); put("intervalDays", h.schedule.intervalDays)
                        put("startDate", h.startDate?.toString() ?: ""); put("archivedAt", h.archivedAt?.toString() ?: "")
                    })
                }
            }
            putJsonArray("logs") {
                queries.getRecordsBetween("0000-01-01", "9999-12-31").executeAsList().map { it.toOccurrence() }.forEach { o ->
                    add(buildJsonObject {
                        put("habitId", o.habitId); put("date", o.date.toString()); put("status", o.status.name)
                        put("time", o.scheduledTime ?: ""); put("value", o.value ?: 0.0)
                        put("completedAt", o.completedAt ?: ""); put("note", o.note ?: ""); put("skipReason", o.skipReason ?: "")
                    })
                }
            }
            putJsonArray("goals") {
                queries.getGoals().executeAsList().forEach { g ->
                    add(buildJsonObject {
                        put("title", g.title); put("targetType", g.targetType); put("target", g.targetValue)
                        put("startDate", g.startDate); put("endDate", g.endDate ?: "")
                    })
                }
            }
            putJsonArray("reflections") {
                queries.getAllReflections().executeAsList().forEach { r ->
                    add(buildJsonObject {
                        put("period", r.periodType); put("start", r.periodStart)
                        put("achievements", r.achievements); put("obstacles", r.obstacles); put("nextActions", r.nextActions)
                    })
                }
            }
            put("customTemplates", JsonArray(queries.getCustomTemplates().executeAsList().map { JsonPrimitive(it.title) }))
        }
        json.encodeToString(JsonObject.serializer(), root)
    }

    override suspend fun wipePersonalData(): Unit = io { queries.wipePersonalData() }
}
