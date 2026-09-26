package com.vajrax.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import com.vajrax.ui.navigation.MainNavigation
import com.vajrax.ui.theme.VajraTheme
import com.vajrax.ui.utils.LocalDeviceTilt
import com.vajrax.ui.utils.rememberDeviceTilt

/**
 * Shared entry point for the VAJRAX application.
 * This is loaded by both androidApp and iosApp.
 */
@Composable
fun VajraApp() {
    val tilt by rememberDeviceTilt()
    VajraTheme {
        CompositionLocalProvider(LocalDeviceTilt provides tilt) {
            MainNavigation()
        }
    }
}
