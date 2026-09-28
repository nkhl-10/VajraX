package com.vajrax.ui.features.discover

import com.vajrax.domain.repository.TemplateRepository
import com.vajrax.domain.repository.PracticeRepository
import com.vajrax.domain.model.Practice
import com.vajrax.domain.template.DefaultTemplate
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid
import kotlin.uuid.ExperimentalUuidApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

@OptIn(ExperimentalUuidApi::class)
class DiscoverViewModel(
    private val templateRepository: TemplateRepository,
    private val practiceRepository: PracticeRepository
) : ViewModel() {
    private val _state = MutableStateFlow(DiscoverState())
    val state: StateFlow<DiscoverState> = _state.asStateFlow()
    
    private val _effect = MutableSharedFlow<DiscoverEffect>()
    val effect: SharedFlow<DiscoverEffect> = _effect.asSharedFlow()

    init {
        loadTemplates()
    }

    fun onIntent(intent: DiscoverIntent) {
        when (intent) {
            is DiscoverIntent.Initialize -> loadTemplates()
            is DiscoverIntent.SelectPath -> selectTemplate(intent.index)
            is DiscoverIntent.TogglePractice -> {} // Preview mode only
            is DiscoverIntent.UseTemplate -> useTemplate(intent.path)
        }
    }

    private fun loadTemplates() {
        viewModelScope.launch {
            try {
                val templates = templateRepository.getAllTemplates()
                val paths = templates.map { template ->
                    DiscoverLifePath(
                        id = template.id,
                        title = template.name,
                        subtitle = template.category,
                        description = template.description,
                        accentColorHex = "#5D3FD3",
                        practices = template.habits.map { habit ->
                            DiscoverPractice(
                                id = habit.name,
                                title = habit.name,
                                time = habit.startTime,
                                duration = "${habit.duration}m",
                                icon = "⚙️",
                                isRequired = true
                            )
                        }
                    )
                }
                _state.update { it.copy(isLoading = false, paths = paths, rawTemplates = templates) }
                if (paths.isNotEmpty()) {
                    selectTemplate(0)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun selectTemplate(index: Int) {
        _state.update { it.copy(selectedPathIndex = index) }
        val selectedPath = _state.value.paths.getOrNull(index)
        if (selectedPath != null) {
            _state.update { it.copy(practices = selectedPath.practices) }
        }
    }

    private fun useTemplate(path: DiscoverLifePath) {
        viewModelScope.launch {
            try {
                val template = _state.value.rawTemplates?.find { it.id == path.id } ?: return@launch
                
                // Reset existing active practices
                val existing = practiceRepository.getActivePractices()
                existing.forEach { 
                    practiceRepository.togglePracticeActive(it.id, false)
                }
                
                // Insert new habits
                template.habits.forEach { habit ->
                    val practice = Practice(
                        id = Uuid.random().toString(),
                        principleId = "",
                        title = habit.name,
                        targetDurationMinutes = habit.duration,
                        minimumDurationMinutes = habit.duration,
                        preferredTime = habit.startTime,
                        trackingMode = habit.trackingType,
                        isActive = true
                    )
                    practiceRepository.insertPractice(practice)
                }
                _effect.emit(DiscoverEffect.NavigateToHome)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}
