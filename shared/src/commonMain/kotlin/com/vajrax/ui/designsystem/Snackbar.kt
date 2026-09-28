package com.vajrax.ui.designsystem

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.staticCompositionLocalOf

/** App-wide snackbar host (rendered above the floating nav bar by MainNavigation). */
val LocalVxSnackbar = staticCompositionLocalOf { SnackbarHostState() }
