package com.vajrax.ui.features.discover

data class DiscoverState(
    val isLoading: Boolean = true,
    val paths: List<DiscoverLifePath> = emptyList(),
    val selectedPathIndex: Int = 0,
    val practices: List<DiscoverPractice> = emptyList()
)

sealed interface DiscoverIntent {
    object Initialize : DiscoverIntent
    data class SelectPath(val index: Int) : DiscoverIntent
    data class TogglePractice(val practiceId: String) : DiscoverIntent
}
