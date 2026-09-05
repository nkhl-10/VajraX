package com.vajrax.domain.passive

import com.vajrax.domain.model.ActionStatus
import com.vajrax.domain.model.TrackingMode
import com.vajrax.domain.repository.PracticeRepository
import com.vajrax.platform.PassiveIntelligenceController

data class PassiveHealthSnapshot(
    val stepsToday: Int,
    val sleepDurationMinutes: Int,
    val distractionMinutesToday: Int
)

/**
 * Phase 13: Passive Intelligence Manager.
 * Principle: Zero Friction. Automatically checks OS health/screen data to auto-resolve habits.
 */
class PassiveIntelligenceManager(
    private val passiveController: PassiveIntelligenceController,
    private val practiceRepository: PracticeRepository
) {

    suspend fun getHealthSnapshot(): PassiveHealthSnapshot {
        val steps = passiveController.getStepsForToday()
        val sleep = passiveController.getSleepDurationMinutes()
        val distraction = passiveController.getDistractionMinutesForToday()

        return PassiveHealthSnapshot(
            stepsToday = steps,
            sleepDurationMinutes = sleep,
            distractionMinutesToday = distraction
        )
    }

    /**
     * Scans today's timeline and automatically completes passive physical practices
     * if the user has met their target (e.g. 5,000 steps for a walk).
     */
    suspend fun autoSyncPassivePractices(todayDate: String = "2026-08-30"): Int {
        val snapshot = getHealthSnapshot()
        val timeline = practiceRepository.getTodayTimeline(todayDate)
        var autoCompletedCount = 0

        timeline.filter { it.trackingMode == TrackingMode.PASSIVE && it.status == ActionStatus.PENDING }.forEach { item ->
            // If practice is a Walk / Steps practice and steps >= 3000
            val isWalkPractice = item.title.contains("walk", ignoreCase = true) || item.title.contains("step", ignoreCase = true)
            if (isWalkPractice && snapshot.stepsToday >= 3000) {
                practiceRepository.updateActionStatus(item.id, ActionStatus.COMPLETE, item.targetDurationMinutes)
                practiceRepository.submitEvidence(
                    actionId = item.id,
                    note = "Auto-tracked via OS Health Data (${snapshot.stepsToday} steps recorded).",
                    rating = com.vajrax.domain.model.ReflectionRating.EASY,
                    mediaPath = null
                )
                autoCompletedCount++
            }

            // If practice is Sleep / Rest and sleep duration recorded
            val isSleepPractice = item.title.contains("sleep", ignoreCase = true) || item.title.contains("rest", ignoreCase = true)
            if (isSleepPractice && snapshot.sleepDurationMinutes >= 360) {
                practiceRepository.updateActionStatus(item.id, ActionStatus.COMPLETE, snapshot.sleepDurationMinutes)
                autoCompletedCount++
            }
        }

        return autoCompletedCount
    }
}
