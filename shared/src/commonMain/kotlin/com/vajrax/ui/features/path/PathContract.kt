package com.vajrax.ui.features.path

import androidx.compose.runtime.Immutable
import com.vajrax.domain.model.LifePath
import com.vajrax.domain.model.Practice
import com.vajrax.domain.model.Principle

@Immutable
data class PathUiState(
    val activePath: LifePath? = null,
    val availablePaths: List<LifePath> = emptyList(),
    val principles: List<Principle> = emptyList(),
    val practices: List<Practice> = emptyList(),
    val isLoading: Boolean = false
)

sealed interface PathIntent {
    data object LoadPathData : PathIntent
    data class SelectPath(val pathId: String) : PathIntent
    data class TogglePractice(val practiceId: String, val active: Boolean) : PathIntent
}

sealed interface PathEffect {
    data class ShowMessage(val text: String) : PathEffect
}
