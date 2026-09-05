package com.vajrax.domain.engine

import com.vajrax.domain.repository.ReviewRepository
import com.vajrax.domain.repository.WeeklySummaryResult

data class DandaRealityReport(
    val weeklyConsistencyPercentage: Int,
    val plannedCount: Int,
    val completedCount: Int,
    val missedCount: Int,
    val strongestDimension: String,
    val primaryFrictionPoint: String,
    val objectiveMessage: String
)

/**
 * Phase 12: Daṇḍa (Objective Reality Check) Engine.
 * Principle: Pure truth, zero shame. Provides objective data reflection for weekly alignment.
 */
class DandaEngine(
    private val reviewRepository: ReviewRepository
) {

    suspend fun generateRealityReport(): DandaRealityReport {
        val summary: WeeklySummaryResult = reviewRepository.getWeeklySummary()
        val missed = maxOf(0, summary.plannedCount - summary.completedCount)

        val message = if (summary.weeklyConsistency >= 80) {
            "Strong alignment. You executed ${summary.completedCount} of ${summary.plannedCount} commitments."
        } else {
            "${summary.completedCount} of ${summary.plannedCount} commitments kept. No guilt—observe the friction variables and adapt."
        }

        return DandaRealityReport(
            weeklyConsistencyPercentage = summary.weeklyConsistency,
            plannedCount = summary.plannedCount,
            completedCount = summary.completedCount,
            missedCount = missed,
            strongestDimension = summary.strongestArea,
            primaryFrictionPoint = summary.primaryFriction,
            objectiveMessage = message
        )
    }
}
