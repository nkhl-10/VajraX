@file:OptIn(ExperimentalCoroutinesApi::class, ExperimentalUuidApi::class)

package com.vajrax.ui.features.routine

import com.vajrax.core.time.AppClock
import com.vajrax.core.time.Dates
import com.vajrax.domain.analytics.HabitAnalytics
import com.vajrax.domain.analytics.PeriodStats
import com.vajrax.domain.analytics.StreakResult
import com.vajrax.domain.habit.Habit
import com.vajrax.domain.habit.Occurrence
import com.vajrax.domain.habit.Tracker
import com.vajrax.domain.repository.PracticeRepository
import com.vajrax.domain.repository.TemplateRepository
import com.vajrax.domain.repository.TrackerRepository
import com.vajrax.domain.template.DefaultTemplate
import com.vajrax.domain.template.toTemplateHabit
import com.vajrax.domain.usecase.RoutineManager
import com.vajrax.presentation.mvi.MviViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

// ------------------------------------------------------------------ My routine (spec 04 Screen 04)

data class RoutineState(
    val isLoading: Boolean = true,
    val tracker: Tracker? = null,
    val habits: List<Habit> = emptyList(),
    val pastTrackers: List<Tracker> = emptyList(),
    val error: String? = null
)

sealed interface RoutineIntent {
    data class Rename(val name: String) : RoutineIntent
    data class Add(val habit: Habit) : RoutineIntent
    data class Update(val habit: Habit) : RoutineIntent
    data class Archive(val habitId: String) : RoutineIntent
    data object SaveAsTemplate : RoutineIntent
    data object ClearError : RoutineIntent
}

sealed interface RoutineEffect {
    data class ShowMessage(val message: String) : RoutineEffect
}

class RoutineViewModel(
    private val routineManager: RoutineManager,
    private val practices: PracticeRepository,
    private val trackers: TrackerRepository,
    private val templates: TemplateRepository
) : MviViewModel<RoutineState, RoutineIntent, RoutineEffect>(RoutineState()) {

    init {
        viewModelScope.launch {
            trackers.observeActiveTracker()
                .flatMapLatest { t -> if (t == null) flowOf(null to emptyList()) else practices.observeTrackerHabits(t.id).map { t to it } }
                .collect { (t, habits) ->
                    val past = runCatching { trackers.getAllTrackers().filter { !it.isActive } }.getOrDefault(emptyList())
                    updateState { copy(isLoading = false, tracker = t, habits = habits, pastTrackers = past) }
                }
        }
    }

    override fun sendIntent(intent: RoutineIntent) {
        when (intent) {
            is RoutineIntent.Rename -> write(null) {
                val t = currentState().tracker ?: return@write
                if (intent.name.isNotBlank()) trackers.renameTracker(t.id, intent.name.take(40))
            }
            is RoutineIntent.Add -> write("Habit added") { routineManager.addHabit(intent.habit) }
            is RoutineIntent.Update -> write("Changes saved — past days keep their original schedule") { routineManager.updateHabit(intent.habit) }
            is RoutineIntent.Archive -> write("Habit archived. Its history is kept.") { routineManager.archiveHabit(intent.habitId) }
            RoutineIntent.SaveAsTemplate -> write("Saved to My templates") {
                val s = currentState()
                val t = s.tracker ?: return@write
                templates.saveCustomTemplate(
                    DefaultTemplate(
                        id = "custom_" + Uuid.random().toString(),
                        name = t.name.take(40),
                        category = "Routine",
                        description = "Saved from my routine",
                        difficulty = "Custom",
                        estimatedDuration = "Daily",
                        habits = s.habits.mapIndexed { i, h -> h.toTemplateHabit(i) },
                        isCustom = true,
                        durationDays = t.totalDays,
                        recommendedFor = "Created by you"
                    )
                )
            }
            RoutineIntent.ClearError -> updateState { copy(error = null) }
        }
    }

    private fun write(success: String?, block: suspend () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { block() }
                .onSuccess { success?.let { sendEffect(RoutineEffect.ShowMessage(it)) } }
                .onFailure { sendEffect(RoutineEffect.ShowMessage(userMessage(it))) }
        }
    }
}

// ------------------------------------------------------------------ Habit detail (spec 04 Screen 06)

data class HistoryDot(val date: LocalDate, val kind: Int) // 0 rest, 1 missed, 2 done, 3 skipped, 4 open/today, 5 future

data class HabitDetailState(
    val isLoading: Boolean = true,
    val habit: Habit? = null,
    val streak: StreakResult = StreakResult(),
    val streakUnit: String = "days",
    val last30: PeriodStats = PeriodStats(),
    val allTime: PeriodStats = PeriodStats(),
    val history: List<HistoryDot> = emptyList(),
    val notes: List<Pair<LocalDate, String>> = emptyList()
)

class HabitDetailViewModel(
    private val habitId: String,
    private val routineManager: RoutineManager,
    private val practices: PracticeRepository,
    private val clock: AppClock
) : MviViewModel<HabitDetailState, RoutineIntent, RoutineEffect>(HabitDetailState()) {

    init {
        viewModelScope.launch {
            val today = clock.today()
            val from = Dates.startOfWeek(today.minus(365, DateTimeUnit.DAY))
            combine(practices.observeAllTrackedHabits(), practices.observeRange(from, today)) { habits, records ->
                val habit = habits.firstOrNull { it.id == habitId }
                val mine = records.filter { it.habitId == habitId }
                build(habit, mine, today, from)
            }.collect { s -> updateState { s } }
        }
    }

    private fun build(habit: Habit?, records: List<Occurrence>, today: LocalDate, from: LocalDate): HabitDetailState {
        if (habit == null) return HabitDetailState(isLoading = false)
        val analytics = HabitAnalytics(listOf(habit), records, today, from..today)
        val byDate = records.associateBy { it.date }
        val start = Dates.startOfWeek(today.minus(34, DateTimeUnit.DAY))
        val dots = Dates.range(start, Dates.endOfWeek(today)).map { d ->
            val occ = byDate[d]
            val kind = when {
                d > today -> 5
                occ == null -> 0
                occ.isDone -> 2
                occ.isSkipped -> 3
                d == today -> 4
                else -> 1
            }
            HistoryDot(d, kind)
        }
        return HabitDetailState(
            isLoading = false,
            habit = habit,
            streak = analytics.habitStreak(habit.id),
            streakUnit = if (habit.schedule.type == com.vajrax.domain.habit.ScheduleType.WEEKLY_TARGET) "weeks" else "days",
            last30 = analytics.habitPeriod(habit.id, today.minus(29, DateTimeUnit.DAY), today),
            allTime = analytics.habitPeriod(habit.id, from, today),
            history = dots,
            notes = records.filter { !it.note.isNullOrBlank() }.sortedByDescending { it.date }.take(5).map { it.date to it.note!! }
        )
    }

    override fun sendIntent(intent: RoutineIntent) {
        when (intent) {
            is RoutineIntent.Update -> write("Changes saved — past days keep their original schedule") { routineManager.updateHabit(intent.habit) }
            is RoutineIntent.Archive -> write("Habit archived. Its history is kept.") { routineManager.archiveHabit(intent.habitId) }
            else -> Unit
        }
    }

    private fun write(success: String, block: suspend () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { block() }
                .onSuccess { sendEffect(RoutineEffect.ShowMessage(success)) }
                .onFailure { sendEffect(RoutineEffect.ShowMessage(userMessage(it))) }
        }
    }
}
