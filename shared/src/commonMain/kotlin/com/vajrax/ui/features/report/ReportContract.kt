package com.vajrax.ui.features.report

import androidx.compose.runtime.Immutable
import com.vajrax.domain.analytics.PeriodStats
import com.vajrax.domain.analytics.TimeBucket
import com.vajrax.domain.habit.Habit
import com.vajrax.domain.repository.Goal
import com.vajrax.domain.repository.GoalTargetType
import com.vajrax.domain.repository.Reflection
import com.vajrax.domain.repository.ReflectionPeriod
import kotlinx.datetime.LocalDate

enum class ReportPeriod { WEEK, MONTH }

@Immutable
data class Bar(val label: String, val done: Int, val total: Int, val isCurrent: Boolean, val isFuture: Boolean)

@Immutable
data class HabitStatRow(val habit: Habit, val stats: PeriodStats, val streak: Int, val streakUnit: String)

@Immutable
data class AreaRow(val category: String, val stats: PeriodStats, val tag: String?)

@Immutable
data class PatternInsight(val headline: String, val rawData: String, val buckets: List<TimeBucket>)

@Immutable
data class GoalProgress(val goal: Goal, val current: Double, val fraction: Float, val label: String, val habitNames: List<String>)

@Immutable
data class ReportUiState(
    val isLoading: Boolean = true,
    val hasData: Boolean = false,
    val period: ReportPeriod = ReportPeriod.WEEK,
    val periodLabel: String = "",
    val overall: PeriodStats = PeriodStats(),
    val previous: PeriodStats = PeriodStats(),
    val rateDelta: Int? = null,
    val completedDelta: Int = 0,
    val headline: String = "",
    val bars: List<Bar> = emptyList(),
    val barsTitle: String = "",
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val bestStreakEnd: String? = null,
    val mostConsistent: List<HabitStatRow> = emptyList(),
    val needsAttention: List<HabitStatRow> = emptyList(),
    val areas: List<AreaRow> = emptyList(),
    val insight: PatternInsight? = null,
    val habitsTracked: Int = 0,
    val reflectionPeriod: ReflectionPeriod = ReflectionPeriod.WEEK,
    val reflectionStart: LocalDate? = null,
    val reflection: Reflection? = null,
    val goals: List<GoalProgress> = emptyList(),
    val activeHabits: List<Habit> = emptyList(),
    /** Set when the user arrives from the weekly-review prompt; the screen opens the reflection sheet. */
    val openReflection: Boolean = false
)

sealed interface ReportIntent {
    data class SetPeriod(val period: ReportPeriod) : ReportIntent
    data object StartWeeklyReview : ReportIntent
    data object ReflectionOpened : ReportIntent
    data class SaveReflection(val achievements: String, val obstacles: String, val nextActions: String) : ReportIntent
    data class SaveGoal(
        val id: String?,
        val title: String,
        val targetType: GoalTargetType,
        val target: Double,
        val endDate: LocalDate?,
        val habitIds: List<String>
    ) : ReportIntent
    data class DeleteGoal(val goalId: String) : ReportIntent
}

sealed interface ReportEffect {
    data class ShowMessage(val message: String) : ReportEffect
}
