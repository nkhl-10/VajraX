package com.vajrax.ui.features.grow

import com.vajrax.core.coroutines.AppDispatchers
import com.vajrax.domain.growth.GrowthAnalyticsManager
import com.vajrax.presentation.mvi.MviViewModel
import kotlinx.coroutines.launch

class GrowViewModel(
    private val growthAnalyticsManager: GrowthAnalyticsManager
) : MviViewModel<GrowUiState, GrowIntent, GrowEffect>(GrowUiState()) {

    init {
        sendIntent(GrowIntent.LoadGrowthData)
    }

    override fun sendIntent(intent: GrowIntent) {
        when (intent) {
            is GrowIntent.LoadGrowthData -> loadGrowthData()
            is GrowIntent.OpenPatternWhyDialog -> updateState { copy(showWhyDialog = true) }
            is GrowIntent.DismissPatternWhyDialog -> updateState { copy(showWhyDialog = false) }
            is GrowIntent.OpenReview -> viewModelScope.launch { sendEffect(GrowEffect.NavigateToReview) }
        }
    }

    private fun loadGrowthData() {
        viewModelScope.launch(AppDispatchers.IO) {
            updateState { copy(isLoading = true) }
            val report = growthAnalyticsManager.generateGrowthReport()

            updateState {
                copy(
                    isLoading = false,
                    dimensions = report.dimensions,
                    recentEvidence = report.recentEvidence,
                    totalFocusedHours = report.totalFocusHours,
                    totalCommitmentsKept = report.totalCommitmentsKept
                )
            }
        }
    }
}
