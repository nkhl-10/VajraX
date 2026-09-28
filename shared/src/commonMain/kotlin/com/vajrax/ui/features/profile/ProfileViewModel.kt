@file:OptIn(ExperimentalUuidApi::class)

package com.vajrax.ui.features.profile

import com.vajrax.core.time.AppClock
import com.vajrax.core.time.minuteTicks
import com.vajrax.domain.habit.Tracker
import com.vajrax.domain.repository.DataRepository
import com.vajrax.domain.repository.ProfileRepository
import com.vajrax.domain.repository.SettingsRepository
import com.vajrax.domain.repository.TemplateRepository
import com.vajrax.domain.repository.TrackerRepository
import com.vajrax.domain.template.DefaultTemplate
import com.vajrax.domain.usecase.AppHooks
import com.vajrax.presentation.mvi.MviViewModel
import com.vajrax.ui.theme.ThemeMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

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
    val myTemplates: List<DefaultTemplate> = emptyList(),
    val themeMode: ThemeMode = ThemeMode.AUTO,
    val remindersEnabled: Boolean = false,
    val privacy: String = "GENERIC",
    val widgetHideNames: Boolean = false,
    val isExporting: Boolean = false
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
    private val hooks: AppHooks? = null
) : MviViewModel<ProfileUiState, ProfileIntent, ProfileEffect>(ProfileUiState()) {

    init {
        val today = clock.minuteTicks().map { it.first }.distinctUntilChanged()
        viewModelScope.launch {
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
                        myTemplates = mine
                    )
                }
            }.collect { }
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
                        privacy = privacy ?: "GENERIC",
                        widgetHideNames = hide == "true"
                    )
                }
            }.collect { }
        }
    }

    override fun sendIntent(intent: ProfileIntent) {
        when (intent) {
            is ProfileIntent.SaveProfile -> io("Profile updated") {
                val email = intent.email.trim()
                if (email.isNotEmpty() && !EMAIL.matches(email)) throw com.vajrax.domain.usecase.RoutineException("Enter a valid email or leave it empty.")
                profiles.saveProfile(intent.name, email, clock.nowIso())
            }
            is ProfileIntent.SetTheme -> io(null) { settings.put(SettingsRepository.THEME, if (intent.mode == ThemeMode.AUTO) "SYSTEM" else intent.mode.name) }
            is ProfileIntent.SetReminders -> io(if (intent.enabled) "Reminders on" else "Reminders off") {
                settings.put(SettingsRepository.REMINDERS_ENABLED, intent.enabled.toString())
                hooks?.onRoutineChanged()
            }
            is ProfileIntent.SetPrivacy -> io(null) {
                settings.put(SettingsRepository.NOTIFICATION_PRIVACY, intent.privacy)
                hooks?.onRoutineChanged()
            }
            is ProfileIntent.SetWidgetHideNames -> io(null) {
                settings.put(SettingsRepository.WIDGET_HIDE_NAMES, intent.hide.toString())
                hooks?.onCheckInChanged()
            }
            is ProfileIntent.DuplicateTemplate -> io("Template duplicated") {
                val t = templates.getTemplateWithHabits(intent.templateId) ?: return@io
                templates.saveCustomTemplate(
                    t.copy(id = "custom_" + Uuid.random().toString(), name = "${t.name} (copy)".take(40), isCustom = true, isDraft = false, isCommunity = false, author = null)
                )
            }
            is ProfileIntent.DeleteTemplate -> io("Template deleted") { templates.deleteTemplate(intent.templateId) }
            ProfileIntent.Export -> {
                updateState { copy(isExporting = true) }
                viewModelScope.launch(Dispatchers.IO) {
                    runCatching { data.exportJson(clock.nowIso()) }
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
            ProfileIntent.DeleteAllData -> viewModelScope.launch(Dispatchers.IO) {
                runCatching {
                    data.wipePersonalData()
                    hooks?.onRoutineChanged()
                }.onSuccess { sendEffect(ProfileEffect.DataWiped) }
                    .onFailure { sendEffect(ProfileEffect.ShowMessage("Couldn't delete data. Nothing was changed.")) }
            }
        }
    }

    private fun io(success: String?, block: suspend () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { block() }
                .onSuccess { success?.let { sendEffect(ProfileEffect.ShowMessage(it)) } }
                .onFailure { sendEffect(ProfileEffect.ShowMessage(userMessage(it))) }
        }
    }

    companion object {
        private val EMAIL = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
    }
}
