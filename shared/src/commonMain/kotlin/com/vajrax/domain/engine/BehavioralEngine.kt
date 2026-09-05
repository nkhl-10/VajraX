package com.vajrax.domain.engine

import com.vajrax.domain.model.Practice
import com.vajrax.ui.features.review.PatternInsight

/**
 * Phase 12: Master Behavioral Intervention Engine.
 * Implements the 4-tier psychological framework (Sāma → Dāma → Daṇḍa → Bheda).
 */
class BehavioralEngine(
    val sama: SamaEngine,
    val dama: DamaEngine,
    val danda: DandaEngine,
    val bheda: BhedaEngine
) {
    /**
     * Sāma: Proposes least-friction fallback when user indicates friction/skip.
     */
    fun align(practice: Practice): SamaInterventionPlan {
        return sama.calculateMinimumViableAction(practice)
    }

    /**
     * Dāma: Extracts real character proof from past logs.
     */
    suspend fun reward(practiceTitle: String): DamaGrowthProof? {
        return dama.generateGrowthReward(practiceTitle)
    }

    /**
     * Daṇḍa: Provides weekly objective reality check.
     */
    suspend fun reality(): DandaRealityReport {
        return danda.generateRealityReport()
    }

    /**
     * Bheda: Discovers environmental and circadian correlations.
     */
    suspend fun discoverPatterns(): List<PatternInsight> {
        return bheda.discoverPatterns()
    }
}
