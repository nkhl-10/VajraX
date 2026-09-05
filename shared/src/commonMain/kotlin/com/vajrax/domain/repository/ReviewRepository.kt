package com.vajrax.domain.repository

import com.vajrax.ui.features.review.PatternInsight

interface ReviewRepository {
    suspend fun getWeeklySummary(): WeeklySummaryResult
    suspend fun getActivePatternInsight(): PatternInsight?
    suspend fun markPatternApplied(patternId: String)
}

data class WeeklySummaryResult(
    val weeklyConsistency: Int,
    val plannedCount: Int,
    val completedCount: Int,
    val strongestArea: String,
    val primaryFriction: String
)
