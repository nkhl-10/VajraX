package com.vajrax.ui.features.templates

import com.vajrax.core.time.AppClock
import com.vajrax.core.time.TimeFormat
import com.vajrax.domain.habit.Habit
import com.vajrax.domain.repository.SettingsRepository
import com.vajrax.domain.repository.TemplateRepository
import com.vajrax.domain.repository.TrackerRepository
import com.vajrax.domain.template.DefaultTemplate
import com.vajrax.domain.template.TemplateCatalog
import com.vajrax.domain.usecase.RoutineManager
import com.vajrax.presentation.mvi.MviViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

data class DraftHabit(val habit: Habit, val included: Boolean = true)

data class ActivationState(
    val isLoading: Boolean = true,
    val template: DefaultTemplate? = null,
    val drafts: List<DraftHabit> = emptyList(),
    val trackerName: String = "",
    val startDate: LocalDate? = null,
    val today: LocalDate? = null,
    val wakeTime: String? = null,
    val hasActiveTracker: Boolean = false,
    val isActivating: Boolean = false,
    val activated: Boolean = false,
    val error: String? = null
) {
    val included: List<Habit> get() = drafts.filter { it.included }.map { it.habit }
    val dailyMinutes: Int get() = included.sumOf { it.durationMinutes }
}

sealed interface ActivationIntent {
    data class Load(val templateId: String) : ActivationIntent
    data class ToggleHabit(val index: Int) : ActivationIntent
    data class UpdateHabit(val index: Int, val habit: Habit) : ActivationIntent
    data class RemoveHabit(val index: Int) : ActivationIntent
    data class AddHabit(val habit: Habit) : ActivationIntent
    data class Rename(val name: String) : ActivationIntent
    data class StartTomorrow(val tomorrow: Boolean) : ActivationIntent
    data object Activate : ActivationIntent
    data object ClearError : ActivationIntent
}

sealed interface ActivationEffect {
    data object Activated : ActivationEffect
}

/**
 * One template → personal tracker session, shared by Template detail and Customize
 * (reached from onboarding or Discover). The template itself is never modified.
 */
class ActivationViewModel(
    private val templates: TemplateRepository,
    private val trackers: TrackerRepository,
    private val routineManager: RoutineManager,
    private val settings: SettingsRepository,
    private val clock: AppClock
) : MviViewModel<ActivationState, ActivationIntent, ActivationEffect>(ActivationState()) {

    override fun sendIntent(intent: ActivationIntent) {
        when (intent) {
            is ActivationIntent.Load -> load(intent.templateId)
            is ActivationIntent.ToggleHabit -> updateState {
                copy(drafts = drafts.mapIndexed { i, d -> if (i == intent.index) d.copy(included = !d.included) else d })
            }
            is ActivationIntent.UpdateHabit -> updateState {
                copy(drafts = drafts.mapIndexed { i, d -> if (i == intent.index) d.copy(habit = intent.habit) else d }.sortedByTime())
            }
            is ActivationIntent.RemoveHabit -> updateState { copy(drafts = drafts.filterIndexed { i, _ -> i != intent.index }) }
            is ActivationIntent.AddHabit -> updateState {
                copy(drafts = (drafts + DraftHabit(intent.habit.copy(id = "draft_${drafts.size}_${clock.nowIso()}"))).sortedByTime())
            }
            is ActivationIntent.Rename -> updateState { copy(trackerName = intent.name.take(40)) }
            is ActivationIntent.StartTomorrow -> updateState {
                copy(startDate = if (intent.tomorrow) today?.plus(1, DateTimeUnit.DAY) else today)
            }
            ActivationIntent.Activate -> activate()
            ActivationIntent.ClearError -> updateState { copy(error = null) }
        }
    }

    private fun List<DraftHabit>.sortedByTime() = sortedBy { TimeFormat.toMinutes(it.habit.time) ?: Int.MAX_VALUE }

    private fun load(templateId: String) {
        if (currentState().template?.id == templateId && !currentState().isLoading && !currentState().activated) return
        updateState { ActivationState(isLoading = true) }
        viewModelScope.launch(Dispatchers.IO) {
            val template = if (templateId == TemplateCatalog.BLANK_ID) blankTemplate() else templates.getTemplateWithHabits(templateId)
            val wake = settings.get(SettingsRepository.WAKE_TIME)
            val today = clock.today()
            val active = trackers.getActiveTracker() != null
            if (template == null) {
                updateState { copy(isLoading = false, error = "This template is no longer available.") }
                return@launch
            }
            val remindersOn = settings.get(SettingsRepository.REMINDERS_ENABLED) == "true"
            val drafts = withDefaultReminders(routineManager.draftHabits(template, wake), remindersOn).map { DraftHabit(it) }
            updateState {
                copy(
                    isLoading = false,
                    template = template,
                    drafts = drafts,
                    trackerName = template.name,
                    startDate = today,
                    today = today,
                    wakeTime = wake,
                    hasActiveTracker = active,
                    error = null
                )
            }
        }
    }

    /**
     * Zero-nagging default: when reminders are on, only the first habit of the day and the two
     * longest habits get a reminder. Every habit's reminder can be changed in its editor.
     */
    private fun withDefaultReminders(habits: List<Habit>, enabled: Boolean): List<Habit> {
        if (!enabled || habits.isEmpty()) return habits
        val first = habits.minByOrNull { TimeFormat.toMinutes(it.time) ?: Int.MAX_VALUE }?.id
        val longest = habits.filter { it.id != first }.sortedByDescending { it.durationMinutes }.take(2).map { it.id }
        val chosen = setOfNotNull(first) + longest
        return habits.map { if (it.id in chosen) it.copy(reminderEnabled = true, reminderTime = it.reminderTime ?: it.time) else it }
    }

    private fun blankTemplate() = DefaultTemplate(
        id = TemplateCatalog.BLANK_ID,
        name = "My routine",
        category = "Custom",
        description = "A tracker built entirely by you.",
        difficulty = "Any",
        estimatedDuration = "Daily",
        habits = emptyList(),
        recommendedFor = "Anyone who knows exactly what they want to track"
    )

    private fun activate() {
        val s = currentState()
        val template = s.template ?: return
        if (s.isActivating) return
        updateState { copy(isActivating = true, error = null) }
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                routineManager.activateTemplate(template, s.included, s.startDate ?: clock.today(), s.trackerName)
            }.onSuccess {
                updateState { copy(isActivating = false, hasActiveTracker = true, activated = true) }
                sendEffect(ActivationEffect.Activated)
            }.onFailure { t ->
                updateState { copy(isActivating = false, error = userMessage(t)) }
            }
        }
    }
}
