package com.vajrax.domain.model

/**
 * Phase 7: Comprehensive Domain Models mapping to the SQLDelight Schema.
 */

enum class TrackingMode {
    MANUAL, PASSIVE, TIMER, HYBRID
}

enum class ActionStatus {
    PENDING, ONGOING, COMPLETE, MINIMUM, SKIPPED, MISSED
}

enum class ReflectionRating {
    EASY, OKAY, HARD
}

data class LifePath(
    val id: String,
    val name: String,
    val description: String?,
    val isActive: Boolean
)

data class Principle(
    val id: String,
    val lifePathId: String,
    val title: String,
    val description: String
)

data class Practice(
    val id: String,
    val principleId: String?,
    val title: String,
    val targetDurationMinutes: Int,
    val minimumDurationMinutes: Int,
    val preferredTime: String?, // Used for ordering in the Living Timeline (e.g., "09:00")
    val trackingMode: TrackingMode,
    val isActive: Boolean
)

data class ActionRecord(
    val id: String,
    val practiceId: String,
    val date: String, // "YYYY-MM-DD"
    val scheduledTime: String?,
    val status: ActionStatus,
    val completedAt: String?,
    val durationMinutes: Int?
)

data class Evidence(
    val id: String,
    val actionRecordId: String,
    val reflectionRating: ReflectionRating?,
    val note: String?,
    val mediaPath: String?
)
