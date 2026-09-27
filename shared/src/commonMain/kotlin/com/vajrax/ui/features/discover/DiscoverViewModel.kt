package com.vajrax.ui.features.discover

import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DiscoverViewModel(
    private val repository: DiscoverRepository
) {
    private val _state = MutableStateFlow(DiscoverState())
    val state: StateFlow<DiscoverState> = _state.asStateFlow()

    init {
        onIntent(DiscoverIntent.Initialize)
    }

    fun onIntent(intent: DiscoverIntent) {
        when (intent) {
            is DiscoverIntent.Initialize -> loadPaths()
            is DiscoverIntent.SelectPath -> selectPath(intent.index)
            is DiscoverIntent.TogglePractice -> togglePractice(intent.practiceId)
        }
    }

    private fun loadPaths() {
        viewModelScope.launch {
            repository.getLifePaths().collect { paths ->
                _state.update { it.copy(isLoading = false, paths = paths) }
                // Load practices for the first path by default
                if (paths.isNotEmpty()) {
                    loadPracticesForPath(paths.first().id)
                }
            }
        }
    }

    private fun selectPath(index: Int) {
        _state.update { it.copy(selectedPathIndex = index) }
        val selectedPath = _state.value.paths.getOrNull(index)
        if (selectedPath != null) {
            loadPracticesForPath(selectedPath.id)
        }
    }

    private fun loadPracticesForPath(pathId: String) {
        viewModelScope.launch {
            repository.getPracticesForPath(pathId).collect { practices ->
                _state.update { it.copy(practices = practices) }
            }
        }
    }

    private fun togglePractice(practiceId: String) {
        viewModelScope.launch {
            repository.togglePractice(practiceId)
        }
    }

    private val viewModelScope = kotlinx.coroutines.MainScope()
}
