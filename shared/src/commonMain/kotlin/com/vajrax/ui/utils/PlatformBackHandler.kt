package com.vajrax.ui.utils

import androidx.compose.runtime.Composable

/** System back handling for in-screen steps (onboarding). */
@Composable
expect fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit)
