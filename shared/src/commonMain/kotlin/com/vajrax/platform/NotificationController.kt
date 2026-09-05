package com.vajrax.platform

import com.vajrax.domain.notification.ActionableNotification

/**
 * Phase 11: OS-level Notification Dispatcher Interface (Android AlarmManager / iOS UNUserNotificationCenter).
 */
interface NotificationController {
    /**
     * Schedules a rich actionable notification with [Done] and [Minimum] quick buttons.
     */
    fun scheduleActionableNotification(notification: ActionableNotification)

    /**
     * Cancels a scheduled notification by practice ID.
     */
    fun cancelNotification(notificationId: String)

    /**
     * Cancels all non-critical notifications when Progressive Autonomy level reaches SILENT.
     */
    fun cancelAllNonCritical()
}
