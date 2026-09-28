package com.vajrax.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.vajrax.platform.LocalPlatformActions
import com.vajrax.platform.NoopPlatformActions
import com.vajrax.platform.PlatformActions
import com.vajrax.ui.navigation.MainNavigation
import com.vajrax.ui.theme.LuminaTheme
import com.vajrax.ui.theme.VajraTheme
import org.koin.compose.koinInject

/**
 * Shared entry point for the VAJRAX application (Android and iOS).
 */
@Composable
fun VajraApp(platform: PlatformActions = NoopPlatformActions) {
    val appViewModel = koinInject<AppViewModel>()
    val state by appViewModel.state.collectAsState()
    VajraTheme(themeMode = state.themeMode) {
        val dark = LuminaTheme.colors.isDark
        LaunchedEffect(dark) { platform.setSystemBarsDark(dark) }
        CompositionLocalProvider(LocalPlatformActions provides platform) {
            MainNavigation(appViewModel)
        }
    }
}
