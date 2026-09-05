package com.vajrax.domain.notification

import com.vajrax.domain.model.Practice
import com.vajrax.domain.repository.PracticeRepository
import com.vajrax.platform.NotificationController

/**
 * Phase 11: Notification Scheduler.
 * Schedules non-intrusive focus block notifications with direct action buttons.
 */
class NotificationScheduler(
    private val notificationController: NotificationController,
    private val practiceRepository: PracticeRepository
) {
    suspend fun scheduleDailyTimelineNotifications(practices: List<Practice>) {
        practices.filter { it.isActive }.forEach { practice ->
            val time = practice.preferredTime ?: return@forEach
            val notification = ActionableNotification(
                id = "notif_${practice.id}",
                practiceId = practice.id,
                title = "VAJRAX · ${practice.title}",
                body = "Your focus block starts now (${practice.targetDurationMinutes} min).",
                scheduledTimeEpochMillis = 0L, // In real app, calculate epoch from HH:mm
                level = NotificationLevel.CONTEXTUAL,
                actions = listOf(
                    ActionableNotificationButton.DONE,
                    ActionableNotificationButton.MINIMUM
                )
            )
            notificationController.scheduleActionableNotification(notification)
        }
    }

    fun applyAutonomyLevel(level: NotificationLevel) {
        if (level == NotificationLevel.SILENT) {
            notificationController.cancelAllNonCritical()
        }
    }
}
