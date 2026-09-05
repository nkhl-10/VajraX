package com.vajrax.domain.engine

import com.vajrax.domain.model.Practice

data class SamaInterventionPlan(
    val practiceTitle: String,
    val targetMinutes: Int,
    val proposedMinimumMinutes: Int,
    val rationale: String
)

/**
 * Phase 12: Sāma (Align) Engine.
 * Principle: Least-force behavioral intervention. Make the action impossible to fail.
 */
class SamaEngine {

    fun calculateMinimumViableAction(practice: Practice): SamaInterventionPlan {
        val minMinutes = if (practice.minimumDurationMinutes > 0) {
            practice.minimumDurationMinutes
        } else {
            maxOf(5, practice.targetDurationMinutes / 4)
        }

        return SamaInterventionPlan(
            practiceTitle = practice.title,
            targetMinutes = practice.targetDurationMinutes,
            proposedMinimumMinutes = minMinutes,
            rationale = "Preserve the neural pathway and self-identity by executing $minMinutes min instead of zero."
        )
    }
}
