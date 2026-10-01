package com.vajrax.ui.utils

import androidx.compose.runtime.Composable

/** The browser's own back button moves through the navigation history. */
@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) = Unit
