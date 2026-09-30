package com.vajrax.domain.repository

import com.vajrax.domain.habit.Habit
import com.vajrax.domain.habit.Tracker
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

/** Personal trackers (UserTemplateEntity). */
interface TrackerRepository {
    fun observeActiveTracker(): Flow<Tracker?>
    suspend fun getActiveTracker(): Tracker?
    suspend fun getAllTrackers(): List<Tracker>
    suspend fun getTracker(id: String): Tracker?

    /**
     * Archives the current tracker and stores [tracker] with its [habits] in one transaction.
     * History of the archived tracker is kept.
     */
    suspend fun replaceActiveTracker(
        tracker: Tracker,
        habits: List<Habit>,
        userId: String,
        today: LocalDate,
        nowIso: String
    )
    suspend fun renameTracker(id: String, name: String)
    /** Moves a not-yet-started tracker (and its habits) to begin on [date]. */
    suspend fun moveStart(id: String, date: LocalDate)
}

/** Key/value preferences stored in SettingEntity. */
interface SettingsRepository {
    fun observe(key: String): Flow<String?>
    suspend fun get(key: String): String?
    suspend fun put(key: String, value: String)

    companion object Keys {
        const val THEME = "theme_mode" // SYSTEM | LIGHT | DARK
        const val REMINDERS_ENABLED = "reminders_enabled" // true | false
        const val NOTIFICATION_PRIVACY = "notification_privacy" // FULL | GENERIC | HIDDEN
        const val WIDGET_HIDE_NAMES = "widget_hide_names"
        const val ONBOARDING_GOALS = "onboarding_goals"
        const val WAKE_TIME = "wake_time"
        const val MORNING_MINUTES = "morning_minutes"
        const val LAST_MATERIALIZED = "last_materialized_date"
        const val LIBRARY_VERSION = "template_library_version"
        const val ONBOARDING_DONE = "onboarding_done"
        const val HOME_VIEW = "home_view" // DIAL | LIST
    }
}

data class UserProfile(val userId: String, val displayName: String, val email: String) {
    val initials: String
        get() = displayName.split(" ").filter { it.isNotBlank() }.take(2)
            .joinToString("") { it.first().uppercase() }.ifBlank { "?" }
}

interface ProfileRepository {
    fun observeProfile(): Flow<UserProfile?>
    suspend fun getProfile(): UserProfile?
    suspend fun saveProfile(displayName: String, email: String, nowIso: String): UserProfile
}

enum class GoalTargetType { COMPLETIONS, RATE }

data class Goal(
    val id: String,
    val title: String,
    val description: String?,
    val targetType: GoalTargetType,
    val targetValue: Double,
    val startDate: LocalDate,
    val endDate: LocalDate?,
    val habitIds: List<String>
)

interface GoalRepository {
    fun observeGoals(): Flow<List<Goal>>
    suspend fun saveGoal(goal: Goal, nowIso: String)
    suspend fun deleteGoal(goalId: String)
}

enum class ReflectionPeriod { WEEK, MONTH }

data class Reflection(
    val periodType: ReflectionPeriod,
    val periodStart: LocalDate,
    val achievements: String,
    val obstacles: String,
    val nextActions: String
)

interface ReflectionRepository {
    fun observeReflection(periodType: ReflectionPeriod, periodStart: LocalDate): Flow<Reflection?>
    suspend fun saveReflection(reflection: Reflection, nowIso: String)
}

/** Export and delete of all personal tracking data (FR-027, FR-028). */
interface DataRepository {
    suspend fun exportJson(exportedAt: String): String
    suspend fun wipePersonalData()
}
