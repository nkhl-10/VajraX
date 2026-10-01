package com.vajrax.domain

import com.vajrax.domain.habit.NotificationPrivacy
import com.vajrax.domain.repository.SettingsRepository
import com.vajrax.domain.usecase.AppHooks
import com.vajrax.domain.usecase.PreferencesService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Preference writes land in settings and refresh reminders / widgets through the hooks. */
class PreferencesServiceTest {
    private class FakeSettings : SettingsRepository {
        val values = MutableStateFlow<Map<String, String>>(emptyMap())
        override fun observe(key: String): Flow<String?> = values.map { it[key] }
        override suspend fun get(key: String): String? = values.value[key]
        override suspend fun put(key: String, value: String) = values.update { it + (key to value) }
    }

    private class RecordingHooks : AppHooks {
        var routineChanges = 0
        var checkInChanges = 0
        override suspend fun onRoutineChanged() { routineChanges++ }
        override suspend fun onCheckInChanged() { checkInChanges++ }
    }

    private val settings = FakeSettings()
    private val hooks = RecordingHooks()
    private val service = PreferencesService(settings, hooks)

    @Test
    fun skippingYourDayStillSavesExplicitDefaults() = runTest {
        service.saveDayPreferences("06:30", 30, remindersOn = false, privacy = null)

        assertEquals("06:30", settings.get(SettingsRepository.WAKE_TIME))
        assertEquals("30", settings.get(SettingsRepository.MORNING_MINUTES))
        // Reminders are explicitly off (not missing), so the editor can offer to turn them on.
        assertEquals("false", settings.get(SettingsRepository.REMINDERS_ENABLED))
        assertEquals(NotificationPrivacy.Default.name, settings.get(SettingsRepository.NOTIFICATION_PRIVACY))
        assertEquals(1, hooks.routineChanges)
    }

    @Test
    fun turningRemindersOnReschedulesThem() = runTest {
        service.setRemindersEnabled(true)

        assertTrue(service.observeRemindersEnabled().first())
        assertEquals(1, hooks.routineChanges)
        assertEquals(0, hooks.checkInChanges)
    }

    @Test
    fun hidingWidgetNamesOnlyRedrawsWidgets() = runTest {
        service.setWidgetHideNames(true)

        assertEquals("true", settings.get(SettingsRepository.WIDGET_HIDE_NAMES))
        assertEquals(1, hooks.checkInChanges)
        assertEquals(0, hooks.routineChanges)
    }

    @Test
    fun privacyChangeRepostsReminders() = runTest {
        service.setNotificationPrivacy(NotificationPrivacy.HIDDEN.name)

        assertEquals("HIDDEN", settings.get(SettingsRepository.NOTIFICATION_PRIVACY))
        assertEquals(1, hooks.routineChanges)
    }

    @Test
    fun onboardingAnswersRoundTrip() = runTest {
        assertNull(service.onboardingProgress())
        service.saveOnboardingProgress("   ")
        assertNull(service.onboardingProgress(), "blank progress means nothing to resume")
        service.saveOnboardingProgress("""{"step":2}""")
        assertEquals("""{"step":2}""", service.onboardingProgress())

        service.saveOnboardingGoals(linkedSetOf("sleep", "focus"))
        assertEquals("sleep|focus", settings.get(SettingsRepository.ONBOARDING_GOALS))
    }

    @Test
    fun homeViewIsStoredAsDialOrList() = runTest {
        service.setHomeDial(false)
        assertEquals("LIST", settings.get(SettingsRepository.HOME_VIEW))
        service.setHomeDial(true)
        assertEquals("DIAL", settings.get(SettingsRepository.HOME_VIEW))
    }
}
