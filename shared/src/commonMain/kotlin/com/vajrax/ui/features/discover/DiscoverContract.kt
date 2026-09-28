package com.vajrax.ui.features.discover

import com.vajrax.domain.template.DefaultTemplate

data class DiscoverState(
    val isLoading: Boolean = true,
    val paths: List<DiscoverLifePath> = emptyList(),
    val rawTemplates: List<DefaultTemplate>? = null,
    val selectedPathIndex: Int = 0,
    val practices: List<DiscoverPractice> = emptyList()
)

sealed interface DiscoverIntent {
    object Initialize : DiscoverIntent
    data class SelectPath(val index: Int) : DiscoverIntent
    data class TogglePractice(val practiceId: String) : DiscoverIntent
    data class UseTemplate(val path: DiscoverLifePath) : DiscoverIntent
}

sealed interface DiscoverEffect {
    object NavigateToHome : DiscoverEffect
}
