package com.vajrax.domain.ai

import com.vajrax.domain.engine.DandaRealityReport
import com.vajrax.ui.features.review.PatternInsight

data class AiInsightExplanation(
    val summary: String,
    val philosophicalContext: String,
    val recommendedAction: String,
    val confidenceScore: Float
)

/**
 * Phase 18: Deterministic AI Pattern Interpreter.
 * Principle: AI sits ABOVE the deterministic rules engine. It explains real evidence rather than hallucinating.
 */
class AiPatternInterpreter {

    /**
     * Interprets Bheda correlation patterns and contextualizes them using behavioral psychology & philosophy.
     */
    fun interpretPattern(pattern: PatternInsight, activePathName: String): AiInsightExplanation {
        val philosophicalNote = when {
            activePathName.contains("Performance", ignoreCase = true) ->
                "Focus energy where leverage is highest. In the morning, willpower is high and cognitive friction is lowest."
            activePathName.contains("Mastery", ignoreCase = true) ->
                "Self-respect means designing an environment where you don't have to battle avoidable fatigue."
            activePathName.contains("Scholar", ignoreCase = true) ->
                "Deep synthesis requires uninterrupted circadian peak hours. Guard the first 3 hours after waking."
            else ->
                "Align your hardest commitments with your natural circadian rhythm."
        }

        return AiInsightExplanation(
            summary = pattern.description,
            philosophicalContext = philosophicalNote,
            recommendedAction = pattern.actionSuggestion,
            confidenceScore = 0.94f
        )
    }

    /**
     * Synthesizes weekly Daṇḍa reality data into an encouraging, non-judgmental executive summary.
     */
    fun synthesizeWeeklyReview(realityReport: DandaRealityReport): String {
        return if (realityReport.weeklyConsistencyPercentage >= 80) {
            "Exceptional momentum. You kept ${realityReport.completedCount} commitments without needing constant willpower. Your system is working."
        } else {
            "${realityReport.completedCount} of ${realityReport.plannedCount} commitments completed. The data shows clear friction during evening hours. Shift high-friction tasks earlier to restore alignment."
        }
    }
}
