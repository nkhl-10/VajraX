package com.vajrax.ui.features.onboarding

import com.vajrax.domain.template.DefaultTemplate

enum class OnboardingStep {
    WELCOME,
    CHOOSE_TEMPLATE
}

data class OnboardingUiState(
    val step: OnboardingStep = OnboardingStep.WELCOME,
    val isLoading: Boolean = false,
    val error: String? = null,
    val templates: List<DefaultTemplate> = emptyList()
)

sealed interface OnboardingIntent {
    data object StartOnboarding : OnboardingIntent
    data class SelectTemplate(val templateId: String) : OnboardingIntent
    data object StartBlank : OnboardingIntent
}

sealed interface OnboardingEffect {
    data object NavigateToHome : OnboardingEffect
    data class ShowMessage(val message: String) : OnboardingEffect
}
