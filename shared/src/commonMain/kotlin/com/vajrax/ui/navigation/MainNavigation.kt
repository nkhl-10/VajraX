package com.vajrax.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.vajrax.app.AppViewModel
import com.vajrax.ui.components.FloatingPillNavBar
import com.vajrax.ui.features.calendar.CalendarScreen
import com.vajrax.ui.features.calendar.CalendarViewModel
import com.vajrax.ui.features.grow.GrowScreen
import com.vajrax.ui.features.grow.GrowViewModel
import com.vajrax.ui.features.onboarding.OnboardingScreen
import com.vajrax.ui.features.onboarding.OnboardingViewModel
import com.vajrax.ui.features.path.PathScreen
import com.vajrax.ui.features.discover.DiscoverScreen
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

@Composable
fun MainNavigation() {
    val colors = LuminaTheme.colors
    
    val appViewModel = koinInject<AppViewModel>()
    val startDest by appViewModel.startDestination.collectAsState()

    if (startDest == null) {
        Box(modifier = Modifier.fillMaxSize().background(colors.background), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = colors.primary)
        }
        return
    }

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: startDest!!

    val todayViewModel = koinInject<TodayViewModel>()
    val calendarViewModel = koinInject<CalendarViewModel>()
    val pathViewModel = koinInject<PathViewModel>()
    val growViewModel = koinInject<GrowViewModel>()
    val profileViewModel = koinInject<com.vajrax.ui.features.profile.ProfileViewModel>()
    val onboardingViewModel = koinInject<OnboardingViewModel>()

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
        NavHost(
            navController = navController,
            startDestination = startDest!!,
            modifier = Modifier.fillMaxSize()
        ) {
            composable("onboarding") {
                val state by onboardingViewModel.uiState.collectAsState()
                androidx.compose.runtime.LaunchedEffect(Unit) {
                    onboardingViewModel.effect.collect { effect ->
                        when (effect) {
                            is com.vajrax.ui.features.onboarding.OnboardingEffect.NavigateToHome -> {
                                navController.navigate("home") {
                                    popUpTo("onboarding") { inclusive = true }
                                }
                            }
                            is com.vajrax.ui.features.onboarding.OnboardingEffect.ShowMessage -> { /* Show Snackbar */ }
                        }
                    }
                }
                OnboardingScreen(
                    state = state,
                    onIntent = onboardingViewModel::sendIntent
                )
            }
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
                DiscoverScreen()
            }
            composable("report") {
                val state by growViewModel.uiState.collectAsState()
                androidx.compose.runtime.LaunchedEffect(Unit) {
                    growViewModel.effect.collect { effect ->
                        when (effect) {
                            is com.vajrax.ui.features.grow.GrowEffect.NavigateToReview -> navController.navigate("review")
                        }
                    }
                }
                GrowScreen(
                    state = state,
                    onIntent = growViewModel::sendIntent
                )
            }
            composable("profile") {
                val state by profileViewModel.uiState.collectAsState()
                ProfileScreen(
                    state = state,
                    onSync = profileViewModel::triggerCloudSync
                )
            }
            composable("learn") {
                val learnViewModel = koinInject<com.vajrax.ui.features.learn.LearnViewModel>()
                val state by learnViewModel.uiState.collectAsState()
                com.vajrax.ui.features.learn.LearnScreen(
                    state = state,
                    onIntent = learnViewModel::sendIntent
                )
            }
            composable("review") {
                val reviewViewModel = koinInject<com.vajrax.ui.features.review.ReviewViewModel>()
                val state by reviewViewModel.uiState.collectAsState()
                com.vajrax.ui.features.review.ReviewScreen(
                    state = state,
                    onIntent = reviewViewModel::sendIntent
                )
            }
        }

        if (currentRoute != "onboarding") {
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
}
