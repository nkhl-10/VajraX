package com.vajrax.ui.features.learn

import androidx.compose.runtime.Immutable

@Immutable
data class BookIdea(
    val id: String,
    val bookTitle: String,
    val author: String,
    val idea: String,
    val practicalApplication: String,
    val isExperimentActive: Boolean = false,
    val experimentDaysLeft: Int? = null
)

@Immutable
data class LearnUiState(
    val ideas: List<BookIdea> = emptyList(),
    val isApplyingIdeaId: String? = null,
    val userCustomPracticeText: String = "",
    val isLoading: Boolean = false
)

sealed interface LearnIntent {
    data object LoadIdeas : LearnIntent
    data class StartApplyFlow(val ideaId: String) : LearnIntent
    data class UpdatePracticeText(val text: String) : LearnIntent
    data class Launch7DayExperiment(val ideaId: String, val practiceText: String) : LearnIntent
    data object CancelApplyFlow : LearnIntent
}

sealed interface LearnEffect {
    data class ShowToast(val message: String) : LearnEffect
}
