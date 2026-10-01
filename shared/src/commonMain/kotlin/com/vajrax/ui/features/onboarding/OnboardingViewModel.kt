package com.vajrax.ui.features.onboarding

import com.vajrax.core.coroutines.AppDispatchers
import com.vajrax.core.coroutines.runCatchingCancellable
import com.vajrax.core.time.AppClock
import com.vajrax.domain.repository.ProfileRepository
import com.vajrax.domain.repository.SettingsRepository
import com.vajrax.domain.repository.TemplateRepository
import com.vajrax.domain.template.DefaultTemplate
import com.vajrax.domain.template.TemplateCatalog
import com.vajrax.presentation.mvi.MviViewModel
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
    private val routineManager: com.vajrax.domain.usecase.RoutineManager,
    private val preferences: com.vajrax.domain.usecase.PreferencesService,
    private val profileService: com.vajrax.domain.usecase.ProfileService
) : MviViewModel<OnboardingUiState, OnboardingIntent, OnboardingEffect>(OnboardingUiState()) {

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch(AppDispatchers.IO) {
            runCatchingCancellable {
                routineManager.startup()
                val profile = profiles.getProfile()
                val all = templates.getAllTemplates().filter { !it.isDraft }
                val saved = preferences.onboardingProgress()?.let { raw ->
                    runCatchingCancellable { progressJson.decodeFromString(SavedProgress.serializer(), raw) }.getOrNull()
                }
                updateState {
                    val base = copy(isLoading = false, templates = all, name = profile?.displayName ?: name)
                    // Resume where the user left off (the app may have been closed mid-way).
                    saved?.let { p ->
                        base.copy(
                            step = OnboardingStep.entries.firstOrNull { it.name == p.step } ?: step,
                            name = p.name,
                            goals = p.goals.toSet(),
                            wakeTime = p.wakeTime,
                            morningMinutes = p.morningMinutes,
                            reminderStyle = ReminderStyle.entries.firstOrNull { it.name == p.reminderStyle } ?: reminderStyle
                        )
                    } ?: base
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
                saveProgress()
                load()
            }
            OnboardingIntent.Back -> {
                updateState { copy(step = OnboardingStep.entries[(step.ordinal - 1).coerceAtLeast(0)]) }
                saveProgress()
            }
            OnboardingIntent.SkipPreferences -> {
                persist(skipPrefs = true)
                updateState { copy(step = OnboardingStep.TEMPLATE) }
                saveProgress()
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
        advance()
        saveProgress()
    }

    /** Remembers the step and answers so onboarding resumes after the app is closed. */
    private fun saveProgress() {
        val s = currentState()
        val raw = if (s.step == OnboardingStep.WELCOME && s.name.isBlank() && s.goals.isEmpty()) "" else progressJson.encodeToString(
            SavedProgress.serializer(),
            SavedProgress(s.step.name, s.name, s.goals.toList(), s.wakeTime, s.morningMinutes, s.reminderStyle.name)
        )
        viewModelScope.launch(AppDispatchers.IO) {
            runCatchingCancellable { preferences.saveOnboardingProgress(raw) }
                .onFailure { com.vajrax.core.log.VxLog.w("Onboarding", "Couldn't save progress", it) }
        }
    }

    private fun advance() {
        val s = currentState()
        when (s.step) {
            OnboardingStep.WELCOME -> updateState { copy(step = OnboardingStep.ABOUT) }
            OnboardingStep.ABOUT -> {
                viewModelScope.launch(AppDispatchers.IO) {
                    runCatchingCancellable {
                        profileService.saveName(s.name)
                        preferences.saveOnboardingGoals(s.goals)
                    }.onFailure { com.vajrax.core.log.VxLog.w("Onboarding", "Couldn't save name and goals", it) }
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
        viewModelScope.launch(AppDispatchers.IO) {
            runCatchingCancellable {
                // Skip keeps the defaults shown on the screen, with reminders off until asked for.
                val style = if (skipPrefs) ReminderStyle.OFF else s.reminderStyle
                preferences.saveDayPreferences(s.wakeTime, s.morningMinutes, style != ReminderStyle.OFF, style.privacy)
            }.onFailure { com.vajrax.core.log.VxLog.w("Onboarding", "Couldn't save day preferences", it) }
        }
    }

    @kotlinx.serialization.Serializable
    private data class SavedProgress(
        val step: String,
        val name: String,
        val goals: List<String>,
        val wakeTime: String,
        val morningMinutes: Int,
        val reminderStyle: String
    )

    private val progressJson = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }

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
