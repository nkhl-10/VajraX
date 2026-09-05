package com.vajrax.ui.features.review

import com.vajrax.domain.repository.ReviewRepository
import com.vajrax.presentation.mvi.MviViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch

class ReviewViewModel(
    private val reviewRepository: ReviewRepository
) : MviViewModel<ReviewUiState, ReviewIntent, ReviewEffect>(ReviewUiState()) {

    init {
        sendIntent(ReviewIntent.LoadWeeklyReview)
    }

    override fun sendIntent(intent: ReviewIntent) {
        when (intent) {
            is ReviewIntent.LoadWeeklyReview -> loadReviewData()
            is ReviewIntent.ApplyBhedaRecommendation -> applyRecommendation()
        }
    }

    private fun loadReviewData() {
        viewModelScope.launch(Dispatchers.IO) {
            val summary = reviewRepository.getWeeklySummary()
            val pattern = reviewRepository.getActivePatternInsight()

            updateState {
                copy(
                    weeklyConsistency = summary.weeklyConsistency,
                    plannedCount = summary.plannedCount,
                    completedCount = summary.completedCount,
                    strongestArea = summary.strongestArea,
                    primaryFriction = summary.primaryFriction,
                    patternInsight = pattern
                )
            }
        }
    }

    private fun applyRecommendation() {
        viewModelScope.launch(Dispatchers.IO) {
            val pattern = uiState.value.patternInsight
            if (pattern != null) {
                reviewRepository.markPatternApplied("pattern_1")
            }
            updateState { copy(isRecommendationApplied = true) }
            sendEffect(ReviewEffect.ShowToast("Recommendation applied. Schedule updated."))
        }
    }
}
