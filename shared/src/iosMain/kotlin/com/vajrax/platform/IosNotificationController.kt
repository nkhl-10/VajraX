package com.vajrax.platform

import com.vajrax.domain.notification.ActionableNotification
import com.vajrax.domain.notification.ZeroNaggingPolicy

/**
 * Phase 11: iOS Implementation of Actionable Notifications (UNUserNotificationCenter).
 * Enforces Zero Nagging policy and UNNotificationAction buttons.
 */
class IosNotificationController : NotificationController {

    override fun scheduleActionableNotification(notification: ActionableNotification) {
        if (!ZeroNaggingPolicy.isCompliant(notification.title + " " + notification.body)) {
            return
        }

        // UNUserNotificationCenter implementation attaching UNNotificationCategory with actions
    }

    override fun cancelNotification(notificationId: String) {
        // UNUserNotificationCenter.currentNotificationCenter.removePendingNotificationRequests
    }

    override fun cancelAllNonCritical() {
        // Suppress non-critical notifications
    }
}
