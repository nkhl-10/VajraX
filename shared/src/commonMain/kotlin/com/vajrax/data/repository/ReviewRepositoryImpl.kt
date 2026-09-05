package com.vajrax.data.repository

import com.vajrax.data.local.VajraDatabase
import com.vajrax.domain.repository.ReviewRepository
import com.vajrax.domain.repository.WeeklySummaryResult
import com.vajrax.ui.features.review.PatternInsight

class ReviewRepositoryImpl(
    private val database: VajraDatabase
) : ReviewRepository {
    private val queries = database.vajraDatabaseQueries

    override suspend fun getWeeklySummary(): WeeklySummaryResult {
        val completedCount = queries.countCompletedActionsBetweenDates("2026-08-24", "2026-08-30").executeAsOne().toInt()
        val totalCount = queries.countTotalActionsBetweenDates("2026-08-24", "2026-08-30").executeAsOne().toInt()
        val consistency = if (totalCount > 0) ((completedCount.toFloat() / totalCount) * 100).toInt() else 85

        return WeeklySummaryResult(
            weeklyConsistency = consistency,
            plannedCount = if (totalCount > 0) totalCount else 20,
            completedCount = if (completedCount > 0) completedCount else 17,
            strongestArea = "Competence (Deep Work)",
            primaryFriction = "Late-night screen use before sleep"
        )
    }

    override suspend fun getActivePatternInsight(): PatternInsight? {
        return queries.getActivePatternInsights().executeAsOneOrNull()?.let {
            PatternInsight(
                title = it.title,
                description = it.description,
                actionSuggestion = it.recommendation
            )
        }
    }

    override suspend fun markPatternApplied(patternId: String) {
        queries.markPatternInsightApplied(patternId)
    }
}
