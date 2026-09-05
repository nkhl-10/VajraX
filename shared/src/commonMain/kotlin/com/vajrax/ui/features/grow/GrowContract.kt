package com.vajrax.ui.features.grow

import androidx.compose.runtime.Immutable
import com.vajrax.domain.model.ReflectionRating

@Immutable
data class EvidenceEntry(
    val id: String,
    val practiceTitle: String,
    val date: String,
    val note: String,
    val rating: ReflectionRating?,
    val durationMinutes: Int
)

@Immutable
data class GrowthDimension(
    val title: String,
    val subtitle: String,
    val metricValue: String,
    val metricUnit: String
)

@Immutable
data class GrowUiState(
    val dimensions: List<GrowthDimension> = emptyList(),
    val recentEvidence: List<EvidenceEntry> = emptyList(),
    val totalFocusedHours: Int = 48,
    val totalCommitmentsKept: Int = 112,
    val isLoading: Boolean = false
)

sealed interface GrowIntent {
    data object LoadGrowthData : GrowIntent
}

sealed interface GrowEffect
