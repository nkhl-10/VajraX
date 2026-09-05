package com.vajrax.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.vajrax.ui.components.FloatingPillNavBar
import com.vajrax.ui.features.calendar.CalendarScreen
import com.vajrax.ui.features.calendar.CalendarViewModel
import com.vajrax.ui.features.grow.GrowScreen
import com.vajrax.ui.features.grow.GrowViewModel
import com.vajrax.ui.features.path.PathScreen
import com.vajrax.ui.features.path.PathViewModel
import com.vajrax.ui.features.profile.ProfileScreen
import com.vajrax.ui.features.today.TodayScreen
import com.vajrax.ui.features.today.TodayViewModel
import com.vajrax.ui.theme.LuminaTheme
import org.koin.compose.koinInject

enum class NavTabType {
    HOME,
    CALENDAR,
    DISCOVER,
    REPORT,
    PROFILE
}

data class NavTabItem(
    val route: String,
    val label: String,
    val type: NavTabType
)

/**
 * Main Navigation with Tactile Floating Capsule Pill Bottom Navigation Bar.
 * Connects Home, Calendar Matrix, Discover, Report, and Profile.
 */
@Composable
fun MainNavigation() {
    val colors = LuminaTheme.colors
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "home"

    // Inject ViewModels via Koin
    val todayViewModel = koinInject<TodayViewModel>()
    val calendarViewModel = koinInject<CalendarViewModel>()
    val pathViewModel = koinInject<PathViewModel>()
    val growViewModel = koinInject<GrowViewModel>()

    val navTabs = listOf(
        NavTabItem("home", "Home", NavTabType.HOME),
        NavTabItem("calendar", "Calendar", NavTabType.CALENDAR),
        NavTabItem("discover", "Discover", NavTabType.DISCOVER),
        NavTabItem("report", "Report", NavTabType.REPORT),
        NavTabItem("profile", "Profile", NavTabType.PROFILE)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        // Main Screen Content
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.fillMaxSize()
        ) {
            composable("home") {
                val state by todayViewModel.uiState.collectAsState()
                TodayScreen(
                    state = state,
                    onIntent = todayViewModel::sendIntent
                )
            }
            composable("calendar") {
                val state by calendarViewModel.uiState.collectAsState()
                CalendarScreen(
                    state = state,
                    onIntent = calendarViewModel::sendIntent
                )
            }
            composable("discover") {
                val state by pathViewModel.uiState.collectAsState()
                PathScreen(
                    state = state,
                    onIntent = pathViewModel::sendIntent
                )
            }
            composable("report") {
                val state by growViewModel.uiState.collectAsState()
                GrowScreen(
                    state = state,
                    onIntent = growViewModel::sendIntent
                )
            }
            composable("profile") {
                ProfileScreen()
            }
        }

        // Floating Pill Navigation Bar (Aligned at BottomCenter with safe insets)
        FloatingPillNavBar(
            tabs = navTabs,
            currentRoute = currentRoute,
            onTabSelected = { route ->
                navController.navigate(route) {
                    popUpTo("home") { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
        )
    }
}


