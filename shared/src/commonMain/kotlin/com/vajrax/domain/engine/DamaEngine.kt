package com.vajrax.domain.engine

import com.vajrax.domain.repository.GrowRepository
import com.vajrax.ui.features.grow.EvidenceEntry

data class DamaGrowthProof(
    val title: String,
    val narrative: String,
    val highlightedQuote: String?,
    val streakDays: Int
)

/**
 * Phase 12: Dāma (Incentivize / Real Reward) Engine.
 * Principle: Replace arbitrary XP/badges with real proof of past resilience from user's own evidence logs.
 */
class DamaEngine(
    private val growRepository: GrowRepository
) {

    suspend fun generateGrowthReward(practiceTitle: String): DamaGrowthProof? {
        val evidenceList = growRepository.getRecentEvidence(15)
        val matching = evidenceList.filter { it.practiceTitle.equals(practiceTitle, ignoreCase = true) }

        if (matching.isEmpty()) return null

        val hardSessions = matching.filter { it.note.isNotBlank() }
        val quote = hardSessions.firstOrNull()?.note

        return DamaGrowthProof(
            title = "Evidence of Character",
            narrative = "You have shown up for $practiceTitle across ${matching.size} documented sessions.",
            highlightedQuote = quote,
            streakDays = matching.size
        )
    }
}
