package com.vajrax.ui.features.onboarding

import com.vajrax.domain.model.LifePath
import com.vajrax.domain.model.Practice
import com.vajrax.domain.repository.LifePathRepository
import com.vajrax.domain.repository.PracticeRepository
import com.vajrax.domain.repository.TemplateRepository
import com.vajrax.presentation.mvi.MviViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class OnboardingViewModel(
    private val templateRepository: TemplateRepository,
    private val lifePathRepository: LifePathRepository,
    private val practiceRepository: PracticeRepository
) : MviViewModel<OnboardingUiState, OnboardingIntent, OnboardingEffect>(OnboardingUiState()) {

    override fun sendIntent(intent: OnboardingIntent) {
        when (intent) {
            is OnboardingIntent.StartOnboarding -> loadTemplates()
            is OnboardingIntent.SelectTemplate -> activateTemplate(intent.templateId)
            is OnboardingIntent.StartBlank -> activateTemplate("blank_custom_template")
        }
    }

    private fun loadTemplates() {
        viewModelScope.launch(Dispatchers.IO) {
            updateState { copy(step = OnboardingStep.CHOOSE_TEMPLATE, isLoading = true, error = null) }
            try {
                val allTemplates = templateRepository.getAllTemplates()
                val provided = allTemplates.filter { it.category != "Custom" && it.id != "blank_custom_template" }
                
                updateState {
                    copy(
                        isLoading = false,
                        templates = provided
                    )
                }
            } catch (e: Exception) {
                updateState { copy(isLoading = false, error = e.message) }
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun activateTemplate(templateId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            updateState { copy(isLoading = true) }
            try {
                val template = templateRepository.getTemplateWithHabits(templateId)
                if (template != null) {
                    // Create a user-owned copy of the template as an active LifePath
                    val newPathId = Uuid.random().toString()
                    val lifePath = LifePath(
                        id = newPathId,
                        name = if (templateId == "blank_custom_template") "My Path" else template.name,
                        description = template.description,
                        isActive = true
                    )
                    lifePathRepository.setActiveLifePath(newPathId) 

                    // Instantiate all habits as user Practices
                    template.habits.forEach { habit ->
                        val practice = Practice(
                            id = Uuid.random().toString(),
                            principleId = null,
                            title = habit.name,
                            targetDurationMinutes = habit.duration,
                            minimumDurationMinutes = habit.duration / 3,
                            preferredTime = habit.startTime,
                            trackingMode = habit.trackingType,
                            isActive = true
                        )
                        practiceRepository.insertPractice(practice)
                    }

                    updateState { copy(isLoading = false) }
                    sendEffect(OnboardingEffect.NavigateToHome)
                } else {
                    updateState { copy(isLoading = false, error = "Template not found") }
                }
            } catch (e: Exception) {
                updateState { copy(isLoading = false, error = e.message) }
                sendEffect(OnboardingEffect.ShowMessage("Error: ${e.message}"))
            }
        }
    }
}
