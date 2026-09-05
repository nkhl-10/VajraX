package com.vajrax.ui.features.review

import androidx.compose.runtime.Immutable

@Immutable
data class PatternInsight(
    val title: String,
    val description: String,
    val actionSuggestion: String
)

@Immutable
data class ReviewUiState(
    val weeklyConsistency: Int = 84,
    val plannedCount: Int = 20,
    val completedCount: Int = 17,
    val strongestArea: String = "Competence (Deep Work)",
    val primaryFriction: String = "Late-night screen use before sleep",
    val patternInsight: PatternInsight? = null,
    val isLoading: Boolean = false,
    val isRecommendationApplied: Boolean = false
)

sealed interface ReviewIntent {
    data object LoadWeeklyReview : ReviewIntent
    data object ApplyBhedaRecommendation : ReviewIntent
}

sealed interface ReviewEffect {
    data class ShowToast(val message: String) : ReviewEffect
}
