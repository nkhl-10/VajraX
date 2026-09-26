package com.vajrax.ui.features.path

import androidx.compose.runtime.Immutable
import com.vajrax.domain.template.DefaultTemplate

@Immutable
data class PathUiState(
    val providedTemplates: List<DefaultTemplate> = emptyList(),
    val customTemplates: List<DefaultTemplate> = emptyList(),
    val communityTemplates: List<DefaultTemplate> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed interface PathIntent {
    data object LoadPathData : PathIntent
    data class SelectPath(val templateId: String) : PathIntent
    data object OpenLearn : PathIntent
}

sealed interface PathEffect {
    data class ShowMessage(val text: String) : PathEffect
    data object NavigateToLearn : PathEffect
}
