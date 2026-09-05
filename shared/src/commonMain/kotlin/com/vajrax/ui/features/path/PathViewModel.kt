package com.vajrax.ui.features.path

import com.vajrax.domain.repository.LifePathRepository
import com.vajrax.domain.repository.PracticeRepository
import com.vajrax.presentation.mvi.MviViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch

class PathViewModel(
    private val lifePathRepository: LifePathRepository,
    private val practiceRepository: PracticeRepository
) : MviViewModel<PathUiState, PathIntent, PathEffect>(PathUiState()) {

    init {
        sendIntent(PathIntent.LoadPathData)
    }

    override fun sendIntent(intent: PathIntent) {
        when (intent) {
            is PathIntent.LoadPathData -> loadPathData()
            is PathIntent.SelectPath -> selectPath(intent.pathId)
            is PathIntent.TogglePractice -> togglePractice(intent.practiceId, intent.active)
        }
    }

    private fun loadPathData() {
        viewModelScope.launch(Dispatchers.IO) {
            updateState { copy(isLoading = true) }

            val paths = lifePathRepository.getAllLifePaths()
            val active = lifePathRepository.getActiveLifePath() ?: paths.firstOrNull()
            val activeId = active?.id ?: "high_performance"

            val principles = lifePathRepository.getPrinciplesForPath(activeId)
            val practices = practiceRepository.getAllPractices()

            updateState {
                copy(
                    isLoading = false,
                    activePath = active,
                    availablePaths = paths,
                    principles = principles,
                    practices = practices
                )
            }
        }
    }

    private fun selectPath(pathId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            lifePathRepository.setActiveLifePath(pathId)
            val active = lifePathRepository.getActiveLifePath()
            val principles = lifePathRepository.getPrinciplesForPath(pathId)
            val paths = lifePathRepository.getAllLifePaths()

            updateState {
                copy(
                    activePath = active,
                    availablePaths = paths,
                    principles = principles
                )
            }

            sendEffect(PathEffect.ShowMessage("Path shifted to ${active?.name}"))
        }
    }

    private fun togglePractice(practiceId: String, active: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            practiceRepository.togglePracticeActive(practiceId, active)
            val updated = practiceRepository.getAllPractices()
            updateState { copy(practices = updated) }
        }
    }
}
