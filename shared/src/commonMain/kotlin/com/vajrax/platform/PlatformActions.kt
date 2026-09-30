package com.vajrax.platform

import androidx.compose.runtime.staticCompositionLocalOf

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
    /** Live gyroscope tilt for shadows; null where the platform has none. */
    val deviceTilt: DeviceTilt? get() = null
    /** Posts a reminder for the current habit right away so the user can check how reminders look. */
    fun sendTestReminder() {}
    val appVersion: String
}

object NoopPlatformActions : PlatformActions {
    override fun exportFile(fileName: String, content: String, onResult: (Boolean) -> Unit) = onResult(false)
    override fun notificationsPermitted(): Boolean = false
    override fun requestNotificationPermission(onResult: (Boolean) -> Unit) = onResult(false)
    override val appVersion: String = "1.0"
}

val LocalPlatformActions = staticCompositionLocalOf<PlatformActions> { NoopPlatformActions }
