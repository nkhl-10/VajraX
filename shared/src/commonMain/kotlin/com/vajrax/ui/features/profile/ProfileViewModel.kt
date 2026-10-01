@file:OptIn(ExperimentalUuidApi::class, kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.vajrax.ui.features.profile

import com.vajrax.core.coroutines.AppDispatchers
import com.vajrax.core.coroutines.runCatchingCancellable
import com.vajrax.core.time.AppClock
import com.vajrax.core.time.Dates
import com.vajrax.core.time.minuteTicks
import com.vajrax.domain.analytics.HabitAnalytics
import com.vajrax.domain.habit.Tracker
import com.vajrax.domain.repository.DataRepository
import com.vajrax.domain.repository.PracticeRepository
import com.vajrax.domain.repository.ProfileRepository
import com.vajrax.domain.repository.SettingsRepository
import com.vajrax.domain.repository.TemplateRepository
import com.vajrax.domain.repository.TrackerRepository
import com.vajrax.domain.template.DefaultTemplate
import com.vajrax.domain.usecase.AppHooks
import com.vajrax.presentation.mvi.MviViewModel
import com.vajrax.ui.theme.ThemeMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.minus
import kotlin.uuid.ExperimentalUuidApi

@androidx.compose.runtime.Immutable
data class ProfileUiState(
    val isLoading: Boolean = true,
    val displayName: String = "",
    val email: String = "",
    val initials: String = "",
    val tracker: Tracker? = null,
    val trackerDescription: String = "",
    val dayNumber: Int = 0,
    val cycleFraction: Float = 0f,
    /** The first cycle is over; the routine carries on ([daysIn] keeps counting). */
    val cycleComplete: Boolean = false,
    val daysIn: Int = 0,
    val myTemplates: List<DefaultTemplate> = emptyList(),
    val themeMode: ThemeMode = ThemeMode.AUTO,
    val remindersEnabled: Boolean = false,
    val privacy: String = com.vajrax.domain.habit.NotificationPrivacy.Default.name,
    val widgetHideNames: Boolean = false,
    val isExporting: Boolean = false,
    /** Days in a row with 80 %+ of habits done. */
    val streakDays: Int = 0,
    /** Completion this week, or null before anything was due. */
    val weekRate: Int? = null
)

sealed interface ProfileIntent {
    data class SaveProfile(val name: String, val email: String) : ProfileIntent
    data class SetTheme(val mode: ThemeMode) : ProfileIntent
    data class SetReminders(val enabled: Boolean) : ProfileIntent
    data class SetPrivacy(val privacy: String) : ProfileIntent
    data class SetWidgetHideNames(val hide: Boolean) : ProfileIntent
    data class DuplicateTemplate(val templateId: String) : ProfileIntent
    data class DeleteTemplate(val templateId: String) : ProfileIntent
    data object Export : ProfileIntent
    data object DeleteAllData : ProfileIntent
}

sealed interface ProfileEffect {
    data class ShowMessage(val message: String) : ProfileEffect
    data class ExportReady(val fileName: String, val json: String) : ProfileEffect
    data object DataWiped : ProfileEffect
}

/** Profile & settings (spec 04 Screen 12): local profile, current tracker, my templates, preferences, data. */
class ProfileViewModel(
    private val profiles: ProfileRepository,
    private val trackers: TrackerRepository,
    private val templates: TemplateRepository,
    private val settings: SettingsRepository,
    private val data: DataRepository,
    private val clock: AppClock,
    private val practices: PracticeRepository,
    private val preferences: com.vajrax.domain.usecase.PreferencesService,
    private val profileService: com.vajrax.domain.usecase.ProfileService,
    private val library: com.vajrax.domain.usecase.TemplateLibraryService,
    private val hooks: AppHooks? = null
) : MviViewModel<ProfileUiState, ProfileIntent, ProfileEffect>(ProfileUiState()) {

    init {
        val today = clock.minuteTicks().map { it.first }.distinctUntilChanged()
        launchLoad {
            combine(profiles.observeProfile(), trackers.observeActiveTracker(), templates.observeCustomTemplates(), today) { p, t, mine, d ->
                val description = t?.let { templates.getTemplateWithHabits(it.templateId)?.description } ?: ""
                val day = t?.dayNumber(d) ?: 0
                updateState {
                    copy(
                        isLoading = false,
                        displayName = p?.displayName?.ifBlank { null } ?: "You",
                        email = p?.email ?: "",
                        initials = p?.initials?.takeIf { it != "?" } ?: "Y",
                        tracker = t,
                        trackerDescription = description,
                        dayNumber = day,
                        cycleFraction = if (t != null && t.totalDays > 0) day.toFloat() / t.totalDays else 0f,
                        cycleComplete = t?.cycleComplete(d) == true,
                        daysIn = t?.daysIn(d) ?: 0,
                        myTemplates = mine
                    )
                }
            }.whileVisible().collect { }
        }
        // Header stats, from the same analytics as Home and Report.
        viewModelScope.launch {
            today.flatMapLatest { day ->
                val from = Dates.startOfWeek(day.minus(60, DateTimeUnit.DAY))
                combine(practices.observeAllTrackedHabits(), practices.observeRange(from, day)) { habits, records ->
                    val analytics = HabitAnalytics(habits, records, day, from..day)
                    analytics.dayStreak().current to analytics.period(Dates.startOfWeek(day), day).rate
                }
            }
                .flowOn(Dispatchers.Default)
                .whileVisible()
                .collect { (streak, week) -> updateState { copy(streakDays = streak, weekRate = week) } }
        }
        viewModelScope.launch {
            combine(
                settings.observe(SettingsRepository.THEME),
                settings.observe(SettingsRepository.REMINDERS_ENABLED),
                settings.observe(SettingsRepository.NOTIFICATION_PRIVACY),
                settings.observe(SettingsRepository.WIDGET_HIDE_NAMES)
            ) { theme, reminders, privacy, hide ->
                updateState {
                    copy(
                        themeMode = ThemeMode.of(theme),
                        remindersEnabled = reminders == "true",
                        privacy = com.vajrax.domain.habit.NotificationPrivacy.of(privacy).name,
                        widgetHideNames = hide == "true"
                    )
                }
            }.collect { }
        }
    }

    override fun sendIntent(intent: ProfileIntent) {
        when (intent) {
            is ProfileIntent.SaveProfile -> io("Profile updated") { profileService.save(intent.name, intent.email) }
            is ProfileIntent.SetTheme -> io(null) { preferences.setTheme(if (intent.mode == ThemeMode.AUTO) "SYSTEM" else intent.mode.name) }
            is ProfileIntent.SetReminders -> io(if (intent.enabled) "Reminders on" else "Reminders off") {
                preferences.setRemindersEnabled(intent.enabled)
            }
            is ProfileIntent.SetPrivacy -> io(null) { preferences.setNotificationPrivacy(intent.privacy) }
            is ProfileIntent.SetWidgetHideNames -> io(null) { preferences.setWidgetHideNames(intent.hide) }
            is ProfileIntent.DuplicateTemplate -> io("Template duplicated") { library.duplicate(intent.templateId) }
            is ProfileIntent.DeleteTemplate -> io("Template deleted") { library.delete(intent.templateId) }
            ProfileIntent.Export -> {
                updateState { copy(isExporting = true) }
                viewModelScope.launch(AppDispatchers.IO) {
                    runCatchingCancellable { data.exportJson(clock.nowIso()) }
                        .onSuccess { json ->
                            updateState { copy(isExporting = false) }
                            sendEffect(ProfileEffect.ExportReady("vajrax-export-${clock.today()}.json", json))
                        }
                        .onFailure {
                            updateState { copy(isExporting = false) }
                            sendEffect(ProfileEffect.ShowMessage("Export failed. Please try again."))
                        }
                }
            }
            ProfileIntent.DeleteAllData -> viewModelScope.launch(AppDispatchers.IO) {
                runCatchingCancellable {
                    data.wipePersonalData()
                    hooks?.onRoutineChanged()
                }.onSuccess { sendEffect(ProfileEffect.DataWiped) }
                    .onFailure { sendEffect(ProfileEffect.ShowMessage("Couldn't delete data. Nothing was changed.")) }
            }
        }
    }

    private fun io(success: String?, block: suspend () -> Unit) {
        viewModelScope.launch(AppDispatchers.IO) {
            runCatchingCancellable { block() }
                .onSuccess { success?.let { sendEffect(ProfileEffect.ShowMessage(it)) } }
                .onFailure { sendEffect(ProfileEffect.ShowMessage(userMessage(it))) }
        }
    }
}
