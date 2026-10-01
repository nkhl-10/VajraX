package com.vajrax.domain.usecase

import com.vajrax.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * User preferences that change what the app does in the background (reminders, wake time).
 * Every write goes through here so reminders and widgets are refreshed via [AppHooks].
 */
class PreferencesService(
    private val settings: SettingsRepository,
    private val hooks: AppHooks = NoopAppHooks
) {
    fun observeRemindersEnabled(): Flow<Boolean> =
        settings.observe(SettingsRepository.REMINDERS_ENABLED).map { it == "true" }

    suspend fun setRemindersEnabled(enabled: Boolean) {
        settings.put(SettingsRepository.REMINDERS_ENABLED, enabled.toString())
        hooks.onRoutineChanged()
    }

    /** Theme as stored: "SYSTEM", "LIGHT" or "DARK". */
    suspend fun setTheme(stored: String) = settings.put(SettingsRepository.THEME, stored)

    /** Lock-screen detail for reminders ("FULL", "GENERIC", "HIDDEN"); reminders are re-posted. */
    suspend fun setNotificationPrivacy(privacy: String) {
        settings.put(SettingsRepository.NOTIFICATION_PRIVACY, privacy)
        hooks.onRoutineChanged()
    }

    /** Hides habit names on home-screen widgets; widgets redraw. */
    suspend fun setWidgetHideNames(hide: Boolean) {
        settings.put(SettingsRepository.WIDGET_HIDE_NAMES, hide.toString())
        hooks.onCheckInChanged()
    }

    /** Goals picked in onboarding (used to recommend templates). */
    suspend fun saveOnboardingGoals(goals: Set<String>) = settings.put(SettingsRepository.ONBOARDING_GOALS, goals.joinToString("|"))

    /** Unfinished onboarding (step and answers), so a restart or process death resumes it. */
    suspend fun saveOnboardingProgress(raw: String) = settings.put(SettingsRepository.ONBOARDING_PROGRESS, raw)

    suspend fun onboardingProgress(): String? = settings.get(SettingsRepository.ONBOARDING_PROGRESS)?.takeIf { it.isNotBlank() }

    /** Home shows the rotary dial or the list. */
    suspend fun setHomeDial(dial: Boolean) = settings.put(SettingsRepository.HOME_VIEW, if (dial) "DIAL" else "LIST")

    /**
     * Onboarding "Your day". Skipping still saves the defaults, so reminders are explicitly off
     * (and the app can offer to turn them on) instead of silently never being scheduled.
     */
    suspend fun saveDayPreferences(wakeTime: String, morningMinutes: Int, remindersOn: Boolean, privacy: String?) {
        settings.put(SettingsRepository.WAKE_TIME, wakeTime)
        settings.put(SettingsRepository.MORNING_MINUTES, morningMinutes.toString())
        settings.put(SettingsRepository.REMINDERS_ENABLED, remindersOn.toString())
        settings.put(SettingsRepository.NOTIFICATION_PRIVACY, privacy ?: DEFAULT_PRIVACY)
        hooks.onRoutineChanged()
    }

    companion object {
        val DEFAULT_PRIVACY = com.vajrax.domain.habit.NotificationPrivacy.Default.name
    }
}
