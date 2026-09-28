package com.vajrax.ui.features.onboarding

import com.vajrax.domain.template.DefaultTemplate

enum class OnboardingStep { WELCOME, ABOUT, ROUTINE, TEMPLATE }

enum class ReminderStyle(val label: String, val description: String, val privacy: String?) {
    FULL("Show habit names", "“Time for Morning walk”", "FULL"),
    GENERIC("Keep it private", "“You have a habit due”", "GENERIC"),
    OFF("No reminders", "You can turn them on later", null)
}

data class OnboardingUiState(
    val step: OnboardingStep = OnboardingStep.WELCOME,
    val name: String = "",
    val goals: Set<String> = emptySet(),
    val wakeTime: String = "06:30",
    val morningMinutes: Int = 30,
    val reminderStyle: ReminderStyle = ReminderStyle.GENERIC,
    val isLoading: Boolean = true,
    val error: String? = null,
    val templates: List<DefaultTemplate> = emptyList(),
    val category: String? = null,
    val query: String = ""
) {
    val stepIndex: Int get() = step.ordinal

    val filtered: List<DefaultTemplate>
        get() = templates.filter { t ->
            (category == null || t.category == category) &&
                (query.isBlank() || t.name.contains(query, ignoreCase = true) || t.description.contains(query, ignoreCase = true))
        }
}

sealed interface OnboardingIntent {
    data object Next : OnboardingIntent
    data object Restart : OnboardingIntent
    data object Back : OnboardingIntent
    data object SkipPreferences : OnboardingIntent
    data class SetName(val name: String) : OnboardingIntent
    data class ToggleGoal(val goal: String) : OnboardingIntent
    data class SetWakeTime(val time: String) : OnboardingIntent
    data class SetMorningMinutes(val minutes: Int) : OnboardingIntent
    data class SetReminderStyle(val style: ReminderStyle) : OnboardingIntent
    data class SelectCategory(val category: String?) : OnboardingIntent
    data class Search(val query: String) : OnboardingIntent
}

sealed interface OnboardingEffect {
    data class ShowMessage(val message: String) : OnboardingEffect
}
