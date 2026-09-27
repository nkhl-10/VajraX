package com.vajrax.ui.features.onboarding

import com.vajrax.domain.template.DefaultTemplate

enum class OnboardingStep {
    CHOOSE_TEMPLATE   // Welcome step removed — go straight to template selection
}

data class OnboardingUiState(
    val step: OnboardingStep = OnboardingStep.CHOOSE_TEMPLATE,
    val isLoading: Boolean = true,              // Start loading eagerly
    val error: String? = null,
    val templates: List<DefaultTemplate> = emptyList()
)

sealed interface OnboardingIntent {
    data object StartOnboarding : OnboardingIntent  // kept for compat, unused
    data class SelectTemplate(val templateId: String) : OnboardingIntent
    data object StartBlank : OnboardingIntent
}

sealed interface OnboardingEffect {
    data object NavigateToHome : OnboardingEffect
    data class ShowMessage(val message: String) : OnboardingEffect
}
