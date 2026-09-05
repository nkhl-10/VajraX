package com.vajrax.ui.features.grow

import com.vajrax.domain.growth.GrowthAnalyticsManager
import com.vajrax.presentation.mvi.MviViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
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
        }
    }

    private fun loadGrowthData() {
        viewModelScope.launch(Dispatchers.IO) {
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
