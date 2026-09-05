package com.vajrax.domain.notification

/**
 * Phase 11: International Notification Hierarchy & Zero Nagging Enforcement.
 */

enum class NotificationLevel {
    SILENT,      // No intrusive sound/vibration, passive mirror only
    CONTEXTUAL,  // Triggered strictly at the boundary of a scheduled focus block
    ADAPTIVE,    // Dispatched by Bheda engine (e.g. "Move workout earlier")
    CRITICAL     // Time-sensitive commitment deadlines only
}

enum class ActionableNotificationButton {
    DONE,
    MINIMUM,
    OPEN_APP
}

data class ActionableNotification(
    val id: String,
    val practiceId: String,
    val title: String,
    val body: String,
    val scheduledTimeEpochMillis: Long,
    val level: NotificationLevel,
    val actions: List<ActionableNotificationButton> = listOf(
        ActionableNotificationButton.DONE,
        ActionableNotificationButton.MINIMUM
    )
)

/**
 * Zero Nagging Policy Engine.
 * Rejects emotional manipulation ("We miss you!", "Streak dying!").
 */
object ZeroNaggingPolicy {
    private val FORBIDDEN_PATTERNS = listOf(
        "miss you",
        "streak is dying",
        "come back",
        "don't give up",
        "hurry",
        "last chance"
    )

    fun isCompliant(notificationText: String): Boolean {
        val lower = notificationText.lowercase()
        return FORBIDDEN_PATTERNS.none { lower.contains(it) }
    }
}
