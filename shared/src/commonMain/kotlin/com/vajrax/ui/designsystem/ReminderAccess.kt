package com.vajrax.ui.designsystem

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf

/**
 * Whether habit reminders can actually fire (app setting on and notification permission granted),
 * and how to turn them on from wherever the user is (it asks for permission when needed).
 */
@Immutable
data class ReminderAccess(val on: Boolean, val turnOn: () -> Unit)

val LocalReminderAccess = compositionLocalOf { ReminderAccess(on = true, turnOn = {}) }
