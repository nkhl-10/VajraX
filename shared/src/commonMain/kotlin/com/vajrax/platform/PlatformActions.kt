package com.vajrax.platform

import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * UI-triggered platform capabilities. Android implements these in MainActivity
 * (Storage Access Framework export, POST_NOTIFICATIONS permission); other targets use no-ops.
 */
interface PlatformActions {
    /** Lets the user pick where to save [content]; never uploads anywhere. */
    fun exportFile(fileName: String, content: String, onResult: (Boolean) -> Unit)
    fun notificationsPermitted(): Boolean
    fun requestNotificationPermission(onResult: (Boolean) -> Unit)
    /** Status / navigation bar icon contrast for the in-app theme. */
    fun setSystemBarsDark(dark: Boolean) {}
    /** Live backdrop blur for glass surfaces (Android 12+); otherwise a more opaque tint is used. */
    val supportsBackdropBlur: Boolean get() = false
    /** Whether the launcher supports adding the Today widget from inside the app. */
    fun canPinWidget(): Boolean = false
    /** [list] = the "Today" list widget (every habit); otherwise the compact "Now" widget. */
    fun requestPinWidget(list: Boolean) {}
    /** Opens the system notification settings for this app (after the permission was denied). */
    fun openNotificationSettings() {}
    /**
     * Occurrence the app was opened for from outside (a widget's "Start" or "Log"), or null.
     * Home handles it once and calls [consumeOpenHabitRequest].
     */
    val openHabitRequest: StateFlow<String?> get() = NoOpenRequest
    fun consumeOpenHabitRequest() {}
    /** Live gyroscope tilt for shadows; null where the platform has none. */
    val deviceTilt: DeviceTilt? get() = null
    /** Posts a reminder for the current habit right away so the user can check how reminders look. */
    fun sendTestReminder() {}
    val appVersion: String

    /** The web keeps data in the account (no lasting local storage), so it starts at sign-in. */
    val requiresAccount: Boolean get() = false

    /** Home-screen widgets exist (Android). */
    val supportsWidgets: Boolean get() = true

    /** Habit reminders can ring (not in the browser). */
    val supportsReminders: Boolean get() = true
}

private val NoOpenRequest: StateFlow<String?> = MutableStateFlow(null)

object NoopPlatformActions : PlatformActions {
    override fun exportFile(fileName: String, content: String, onResult: (Boolean) -> Unit) = onResult(false)
    override fun notificationsPermitted(): Boolean = false
    override fun requestNotificationPermission(onResult: (Boolean) -> Unit) = onResult(false)
    override val appVersion: String = "1.0"
}

val LocalPlatformActions = staticCompositionLocalOf<PlatformActions> { NoopPlatformActions }
