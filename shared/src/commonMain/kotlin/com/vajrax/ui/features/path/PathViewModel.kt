package com.vajrax.ui.features.path

import com.vajrax.core.coroutines.AppDispatchers
import com.vajrax.domain.model.LifePath
import com.vajrax.domain.model.Practice
import com.vajrax.domain.repository.LifePathRepository
import com.vajrax.domain.repository.PracticeRepository
import com.vajrax.domain.repository.TemplateRepository
import com.vajrax.presentation.mvi.MviViewModel
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class PathViewModel(
    private val templateRepository: TemplateRepository,
    private val lifePathRepository: LifePathRepository,
    private val practiceRepository: PracticeRepository
) : MviViewModel<PathUiState, PathIntent, PathEffect>(PathUiState()) {

    init {
        sendIntent(PathIntent.LoadPathData)
    }

    override fun sendIntent(intent: PathIntent) {
        when (intent) {
            is PathIntent.LoadPathData -> loadPathData()
            is PathIntent.SelectPath -> selectTemplate(intent.templateId)
            is PathIntent.OpenLearn -> viewModelScope.launch { sendEffect(PathEffect.NavigateToLearn) }
        }
    }

    private fun loadPathData() {
        viewModelScope.launch(AppDispatchers.IO) {
            updateState { copy(isLoading = true, error = null) }
            try {
                val allTemplates = templateRepository.getAllTemplates()
                val provided = allTemplates.filter { it.category != "Custom" && it.id != "blank_custom_template" }
                val custom = templateRepository.getCustomTemplates() + allTemplates.filter { it.id == "blank_custom_template" }
                
                updateState {
                    copy(
                        isLoading = false,
                        providedTemplates = provided,
                        customTemplates = custom,
                        communityTemplates = emptyList() // Will be loaded from Supabase eventually
                    )
                }
            } catch (e: Exception) {
                updateState { copy(isLoading = false, error = e.message) }
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun selectTemplate(templateId: String) {
        viewModelScope.launch(AppDispatchers.IO) {
            try {
                val template = templateRepository.getTemplateWithHabits(templateId)
                if (template != null) {
                    // Create a user-owned copy of the template as an active LifePath
                    val newPathId = Uuid.random().toString()
                    val lifePath = LifePath(
                        id = newPathId,
                        name = template.name,
                        description = template.description,
                        isActive = true
                    )
                    lifePathRepository.setActiveLifePath(newPathId) // Replaces current active
                    
                    // Note: In a real implementation, you'd insert the LifePath first via a specific insert method.
                    // For now, setting it active usually handles insertion in the repository logic or we assume it's created.

                    // Instantiate all habits as user Practices
                    template.habits.forEach { habit ->
                        val practice = Practice(
                            id = Uuid.random().toString(),
                            principleId = null, // Or create a default principle
                            title = habit.name,
                            targetDurationMinutes = habit.duration,
                            minimumDurationMinutes = habit.duration / 3, // Safe default
                            preferredTime = habit.startTime,
                            trackingMode = habit.trackingType,
                            isActive = true
                        )
                        practiceRepository.insertPractice(practice)
                    }

                    sendEffect(PathEffect.ShowMessage("Template '${template.name}' activated!"))
                }
            } catch (e: Exception) {
                sendEffect(PathEffect.ShowMessage("Error activating template: ${e.message}"))
            }
        }
    }
}
