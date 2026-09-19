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
data class PrincipleStrengthItem(
    val name: String,
    val percentage: Int,
    val isStrongest: Boolean = false,
    val isNeedsAttention: Boolean = false
)

@Immutable
data class PatternWhyPoint(
    val timeWindow: String,
    val completionRate: String,
    val sampleSize: String
)

@Immutable
data class PatternInsightItem(
    val title: String,
    val interpretation: String,
    val rawDataSummary: String,
    val confidenceScore: String,
    val whyBreakdown: List<PatternWhyPoint>,
    val rootCause: String,
    val recommendation: String
)

@Immutable
data class GrowUiState(
    val dimensions: List<GrowthDimension> = emptyList(),
    val recentEvidence: List<EvidenceEntry> = emptyList(),
    val principles: List<PrincipleStrengthItem> = listOf(
        PrincipleStrengthItem(name = "Physical Discipline", percentage = 91, isStrongest = true),
        PrincipleStrengthItem(name = "Deep Work", percentage = 84),
        PrincipleStrengthItem(name = "Self-Respect", percentage = 78),
        PrincipleStrengthItem(name = "Continuous Learning", percentage = 72),
        PrincipleStrengthItem(name = "Daily Reflection", percentage = 63, isNeedsAttention = true)
    ),
    val activeInsight: PatternInsightItem = PatternInsightItem(
        title = "Deep Work Timing Correlation",
        interpretation = "Deep Work is 34% more likely to succeed before 11:00 AM.",
        rawDataSummary = "18 sessions (14 AM / 4 PM)",
        confidenceScore = "94% Reliability",
        whyBreakdown = listOf(
            PatternWhyPoint(timeWindow = "06:00 AM – 11:00 AM", completionRate = "93% Complete", sampleSize = "13 of 14 sessions"),
            PatternWhyPoint(timeWindow = "01:00 PM – 05:00 PM", completionRate = "50% Complete", sampleSize = "2 of 4 sessions"),
            PatternWhyPoint(timeWindow = "08:00 PM – 11:00 PM", completionRate = "25% Complete", sampleSize = "1 of 4 sessions")
        ),
        rootCause = "High cognitive energy window aligns with early circadian phase; afternoon context switching increases resistance.",
        recommendation = "Lock your primary 90-minute Deep Work block between 8:00 AM and 10:30 AM."
    ),
    val showWhyDialog: Boolean = false,
    val totalFocusedHours: Int = 48,
    val totalCommitmentsKept: Int = 112,
    val isLoading: Boolean = false
)

sealed interface GrowIntent {
    data object LoadGrowthData : GrowIntent
    data object OpenPatternWhyDialog : GrowIntent
    data object DismissPatternWhyDialog : GrowIntent
}

sealed interface GrowEffect

