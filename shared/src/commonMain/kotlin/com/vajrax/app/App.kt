package com.vajrax.app

import androidx.compose.runtime.Composable
import com.vajrax.ui.navigation.MainNavigation
import com.vajrax.ui.theme.VajraTheme

/**
 * Shared entry point for the VAJRAX application.
 * This is loaded by both androidApp and iosApp.
 */
@Composable
fun VajraApp() {
    VajraTheme {
        MainNavigation()
    }
}
