package com.vajrax.ui.features.onboarding

import com.vajrax.core.time.AppClock
import com.vajrax.domain.repository.ProfileRepository
import com.vajrax.domain.repository.SettingsRepository
import com.vajrax.domain.repository.TemplateRepository
import com.vajrax.domain.template.DefaultTemplate
import com.vajrax.domain.template.TemplateCatalog
import com.vajrax.presentation.mvi.MviViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch

/**
 * First-launch flow (spec 03 §1, 04 Screen 01): welcome → name & goals → optional routine
 * preferences → template choice. Template preview / customize / activation are shared with Discover.
 */
class OnboardingViewModel(
    private val templates: TemplateRepository,
    private val settings: SettingsRepository,
    private val profiles: ProfileRepository,
    private val clock: AppClock,
    private val routineManager: com.vajrax.domain.usecase.RoutineManager
) : MviViewModel<OnboardingUiState, OnboardingIntent, OnboardingEffect>(OnboardingUiState()) {

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                routineManager.startup()
                val profile = profiles.getProfile()
                val all = templates.getAllTemplates().filter { !it.isDraft }
                updateState {
                    copy(
                        isLoading = false,
                        templates = all,
                        name = profile?.displayName ?: name,
                        wakeTime = wakeTime
                    )
                }
            }.onFailure {
                updateState { copy(isLoading = false, error = "Couldn't load templates. Try again.") }
            }
        }
    }

    override fun sendIntent(intent: OnboardingIntent) {
        when (intent) {
            OnboardingIntent.Next -> next()
            OnboardingIntent.Restart -> {
                updateState { OnboardingUiState() }
                load()
            }
            OnboardingIntent.Back -> updateState {
                copy(step = OnboardingStep.entries[(step.ordinal - 1).coerceAtLeast(0)])
            }
            OnboardingIntent.SkipPreferences -> {
                persist(skipPrefs = true)
                updateState { copy(step = OnboardingStep.TEMPLATE) }
            }
            is OnboardingIntent.SetName -> updateState { copy(name = intent.name.take(40)) }
            is OnboardingIntent.ToggleGoal -> updateState {
                copy(goals = if (intent.goal in goals) goals - intent.goal else if (goals.size < 3) goals + intent.goal else goals)
            }
            is OnboardingIntent.SetWakeTime -> updateState { copy(wakeTime = intent.time) }
            is OnboardingIntent.SetMorningMinutes -> updateState { copy(morningMinutes = intent.minutes) }
            is OnboardingIntent.SetReminderStyle -> updateState { copy(reminderStyle = intent.style) }
            is OnboardingIntent.SelectCategory -> updateState { copy(category = intent.category) }
            is OnboardingIntent.Search -> updateState { copy(query = intent.query.take(40)) }
            is OnboardingIntent.ShowArc -> updateState { copy(arcTab = intent.arc, category = null) }
        }
    }

    private fun next() {
        val s = currentState()
        when (s.step) {
            OnboardingStep.WELCOME -> updateState { copy(step = OnboardingStep.ABOUT) }
            OnboardingStep.ABOUT -> {
                viewModelScope.launch(Dispatchers.IO) {
                    runCatching {
                        val existing = profiles.getProfile()
                        profiles.saveProfile(s.name, existing?.email ?: "", clock.nowIso())
                        settings.put(SettingsRepository.ONBOARDING_GOALS, s.goals.joinToString("|"))
                    }
                }
                updateState { copy(step = OnboardingStep.ROUTINE) }
            }
            OnboardingStep.ROUTINE -> {
                persist(skipPrefs = false)
                updateState { copy(step = OnboardingStep.TEMPLATE) }
            }
            OnboardingStep.TEMPLATE -> Unit
        }
    }

    private fun persist(skipPrefs: Boolean) {
        val s = currentState()
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                if (!skipPrefs) {
                    settings.put(SettingsRepository.WAKE_TIME, s.wakeTime)
                    settings.put(SettingsRepository.MORNING_MINUTES, s.morningMinutes.toString())
                    val style = s.reminderStyle
                    settings.put(SettingsRepository.REMINDERS_ENABLED, (style != ReminderStyle.OFF).toString())
                    style.privacy?.let { settings.put(SettingsRepository.NOTIFICATION_PRIVACY, it) }
                }
            }
        }
    }

    /** Templates matching the chosen goals; shorter routines first when mornings are short. */
    fun recommended(state: OnboardingUiState): List<DefaultTemplate> {
        val wanted = state.goals.flatMap { TemplateCatalog.goalToCategories[it].orEmpty() }.toSet()
        if (wanted.isEmpty()) return state.templates.filter { !it.isCommunity && !it.isCustom && it.category != "Arc" }.take(3)
        return state.templates
            .filter { it.category in wanted && it.category != "Arc" }
            .sortedBy { t ->
                val minutes = t.habits.sumOf { it.duration }
                if (state.morningMinutes <= 30) minutes else -t.habits.size
            }
            .take(4)
    }
}
