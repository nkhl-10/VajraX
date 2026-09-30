package com.vajrax.ui.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import com.vajrax.platform.LocalPlatformActions
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.vajrax.app.AppViewModel
import com.vajrax.core.time.AppClock
import com.vajrax.domain.repository.PracticeRepository
import com.vajrax.domain.repository.TemplateRepository
import com.vajrax.domain.repository.TrackerRepository
import com.vajrax.domain.usecase.RoutineManager
import com.vajrax.presentation.mvi.MviViewModel
import com.vajrax.ui.components.FloatingPillNavBar
import com.vajrax.ui.designsystem.LocalVxSnackbar
import com.vajrax.ui.features.builder.TemplateBuilderScreen
import com.vajrax.ui.features.builder.TemplateBuilderViewModel
import com.vajrax.ui.features.calendar.CalendarScreen
import com.vajrax.ui.features.calendar.CalendarViewModel
import com.vajrax.ui.features.discover.DiscoverScreen
import com.vajrax.ui.features.discover.DiscoverViewModel
import com.vajrax.ui.features.legal.LegalDoc
import com.vajrax.ui.features.legal.LegalScreen
import com.vajrax.ui.features.onboarding.OnboardingIntent
import com.vajrax.ui.features.onboarding.OnboardingScreen
import com.vajrax.ui.features.onboarding.OnboardingStep
import com.vajrax.ui.features.onboarding.OnboardingViewModel
import com.vajrax.ui.features.profile.ProfileScreen
import com.vajrax.ui.features.profile.ProfileViewModel
import com.vajrax.ui.features.report.ReportScreen
import com.vajrax.ui.features.report.ReportViewModel
import com.vajrax.ui.features.routine.HabitDetailScreen
import com.vajrax.ui.features.routine.HabitDetailViewModel
import com.vajrax.ui.features.routine.RoutineScreen
import com.vajrax.ui.features.routine.RoutineViewModel
import com.vajrax.ui.features.splash.SplashScreen
import com.vajrax.ui.features.templates.ActivationEffect
import com.vajrax.ui.features.templates.ActivationIntent
import com.vajrax.ui.features.templates.ActivationViewModel
import com.vajrax.ui.features.templates.CustomizeScreen
import com.vajrax.ui.features.templates.TemplateDetailScreen
import com.vajrax.ui.features.today.TodayScreen
import com.vajrax.ui.features.today.TodayViewModel
import com.vajrax.ui.theme.LuminaTheme
import com.vajrax.ui.utils.PlatformBackHandler
import kotlinx.serialization.Serializable
import org.koin.compose.koinInject

enum class NavTabType { HOME, CALENDAR, DISCOVER, REPORT, PROFILE }

data class NavTabItem(val label: String, val type: NavTabType, val route: Any)

// ---------------------------------------------------------------- type-safe routes

@Serializable object SplashRoute
@Serializable object OnboardingRoute
@Serializable object HomeRoute
@Serializable object CalendarRoute
@Serializable object DiscoverRoute
@Serializable object ReportRoute
@Serializable object ProfileRoute
@Serializable data class TemplateRoute(val id: String, val fromOnboarding: Boolean = false)
@Serializable data class CustomizeRoute(val id: String, val fromOnboarding: Boolean = false)
@Serializable data class BuilderRoute(val id: String? = null)
@Serializable object RoutineRoute
@Serializable data class HabitRoute(val id: String)
@Serializable data class LegalRoute(val doc: String)

private val tabs = listOf(
    NavTabItem("Home", NavTabType.HOME, HomeRoute),
    NavTabItem("Calendar", NavTabType.CALENDAR, CalendarRoute),
    NavTabItem("Discover", NavTabType.DISCOVER, DiscoverRoute),
    NavTabItem("Report", NavTabType.REPORT, ReportRoute),
    NavTabItem("Profile", NavTabType.PROFILE, ProfileRoute)
)

/** Creates a screen-scoped view model that is cleared when the destination leaves composition. */
@Composable
private fun <T : MviViewModel<*, *, *>> rememberScreenViewModel(key: Any?, factory: () -> T): T {
    val vm = remember(key) { factory() }
    DisposableEffect(vm) { onDispose { vm.onCleared() } }
    return vm
}

/**
 * Screens you drill into from a tab (template, customize, builder, routine, habit). They push in
 * from the right and pop back to the right, so the direction of travel shows depth; switching
 * between peer tabs stays a quick crossfade.
 */
private fun NavDestination.isDrillIn(): Boolean =
    hasRoute(TemplateRoute::class) || hasRoute(CustomizeRoute::class) || hasRoute(BuilderRoute::class) ||
        hasRoute(RoutineRoute::class) || hasRoute(HabitRoute::class) || hasRoute(LegalRoute::class)

/**
 * Apps targeting API 36 can't lock orientation on tablets and unfolded foldables, so wide
 * windows get a centred phone-width column instead of stretched cards and lists.
 */
private val MaxContentWidth = 640.dp

private const val PUSH_MS = 260
private const val LEAVE_MS = 180

private fun NavHostController.switchTab(route: Any) {
    navigate(route) {
        popUpTo(HomeRoute) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

private fun NavHostController.resetTo(route: Any) {
    navigate(route) {
        popUpTo(graph.id) { inclusive = true }
        launchSingleTop = true
    }
}

@Composable
fun MainNavigation(appViewModel: AppViewModel) {
    val colors = LuminaTheme.colors
    val appState by appViewModel.state.collectAsState()
    val navController = rememberNavController()
    val entry by navController.currentBackStackEntryAsState()
    val destination = entry?.destination
    val currentTab = tabs.firstOrNull { tab -> destination?.hasRoute(tab.route::class) == true }?.type
    val snackbarHost = remember { SnackbarHostState() }
    val backdrop = rememberGraphicsLayer()
    var contentOrigin by remember { mutableStateOf(Offset.Zero) }
    val blurSupported = LocalPlatformActions.current.supportsBackdropBlur

    val todayVm = koinInject<TodayViewModel>()
    val calendarVm = koinInject<CalendarViewModel>()
    val discoverVm = koinInject<DiscoverViewModel>()
    val reportVm = koinInject<ReportViewModel>()
    val profileVm = koinInject<ProfileViewModel>()
    val onboardingVm = koinInject<OnboardingViewModel>()
    val activationVm = koinInject<ActivationViewModel>()
    val routineManager = koinInject<RoutineManager>()
    val practices = koinInject<PracticeRepository>()
    val trackers = koinInject<TrackerRepository>()
    val templates = koinInject<TemplateRepository>()
    val clock = koinInject<AppClock>()

    LaunchedEffect(activationVm) {
        activationVm.effect.collect { e ->
            if (e is ActivationEffect.Activated) navController.resetTo(HomeRoute)
        }
    }

    CompositionLocalProvider(LocalVxSnackbar provides snackbarHost) {
        // Horizontal safe area (landscape navigation bar, display cutouts); vertical insets are per screen.
        Box(
            Modifier.fillMaxSize().background(colors.background)
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
        ) {
            NavHost(
                navController = navController,
                startDestination = SplashRoute,
                modifier = Modifier.align(Alignment.TopCenter).fillMaxHeight().widthIn(max = MaxContentWidth).fillMaxWidth()
                    .onGloballyPositioned { contentOrigin = it.positionInRoot() }
                    // Record the screen so the glass nav bar can frost what is behind it.
                    .drawWithContent {
                        backdrop.record { this@drawWithContent.drawContent() }
                        drawLayer(backdrop)
                    },
                enterTransition = {
                    if (targetState.destination.isDrillIn()) {
                        slideInHorizontally(tween(PUSH_MS, easing = FastOutSlowInEasing)) { it / 3 } + fadeIn(tween(PUSH_MS))
                    } else fadeIn(tween(180))
                },
                exitTransition = {
                    if (targetState.destination.isDrillIn()) {
                        slideOutHorizontally(tween(PUSH_MS, easing = FastOutSlowInEasing)) { -it / 8 } + fadeOut(tween(LEAVE_MS))
                    } else fadeOut(tween(120))
                },
                popEnterTransition = {
                    if (initialState.destination.isDrillIn()) {
                        slideInHorizontally(tween(PUSH_MS, easing = FastOutSlowInEasing)) { -it / 8 } + fadeIn(tween(PUSH_MS))
                    } else fadeIn(tween(180))
                },
                popExitTransition = {
                    if (initialState.destination.isDrillIn()) {
                        slideOutHorizontally(tween(LEAVE_MS, easing = FastOutSlowInEasing)) { it / 3 } + fadeOut(tween(LEAVE_MS))
                    } else fadeOut(tween(120))
                }
            ) {
                composable<SplashRoute> {
                    SplashScreen(appState.startRoute, appState.startupError) { route ->
                        navController.resetTo(if (route == "home") HomeRoute else OnboardingRoute)
                    }
                }
                composable<OnboardingRoute> {
                    val state by onboardingVm.uiState.collectAsState()
                    PlatformBackHandler(enabled = state.step != OnboardingStep.WELCOME) {
                        onboardingVm.sendIntent(OnboardingIntent.Back)
                    }
                    OnboardingScreen(
                        state = state,
                        recommended = remember(state.templates, state.goals, state.morningMinutes) { onboardingVm.recommended(state) },
                        onIntent = onboardingVm::sendIntent,
                        onOpenTemplate = { id -> navController.navigate(TemplateRoute(id, fromOnboarding = true)) },
                        onUseTemplate = { id -> navController.navigate(CustomizeRoute(id, fromOnboarding = true)) },
                        onOpenLegal = { doc -> navController.navigate(LegalRoute(doc.name)) }
                    )
                }
                composable<HomeRoute> {
                    val state by todayVm.uiState.collectAsState()
                    TodayScreen(
                        state = state,
                        effects = todayVm.effect,
                        onIntent = todayVm::sendIntent,
                        onOpenDiscover = { navController.switchTab(DiscoverRoute) },
                        onOpenRoutine = { navController.navigate(RoutineRoute) },
                        onOpenHabit = { navController.navigate(HabitRoute(it)) },
                        onOpenReport = { navController.switchTab(ReportRoute) },
                        onWeeklyReview = {
                            reportVm.sendIntent(com.vajrax.ui.features.report.ReportIntent.StartWeeklyReview)
                            navController.switchTab(ReportRoute)
                        }
                    )
                }
                composable<CalendarRoute> {
                    val state by calendarVm.uiState.collectAsState()
                    CalendarScreen(state, calendarVm.effect, calendarVm::sendIntent, onOpenDiscover = { navController.switchTab(DiscoverRoute) })
                }
                composable<DiscoverRoute> {
                    val state by discoverVm.uiState.collectAsState()
                    DiscoverScreen(
                        state = state,
                        onIntent = discoverVm::sendIntent,
                        onOpenTemplate = { navController.navigate(TemplateRoute(it)) },
                        onUseTemplate = { navController.navigate(CustomizeRoute(it)) },
                        onCreateTemplate = { navController.navigate(BuilderRoute()) }
                    )
                }
                composable<ReportRoute> {
                    val state by reportVm.uiState.collectAsState()
                    ReportScreen(state, reportVm.effect, reportVm::sendIntent, onOpenDiscover = { navController.switchTab(DiscoverRoute) })
                }
                composable<ProfileRoute> {
                    val state by profileVm.uiState.collectAsState()
                    ProfileScreen(
                        state = state,
                        effects = profileVm.effect,
                        onIntent = profileVm::sendIntent,
                        onViewRoutine = { navController.navigate(RoutineRoute) },
                        onChangeTemplate = { navController.switchTab(DiscoverRoute) },
                        onCreateTemplate = { navController.navigate(BuilderRoute()) },
                        onEditTemplate = { navController.navigate(BuilderRoute(it)) },
                        onUseTemplate = { navController.navigate(CustomizeRoute(it)) },
                        onOpenLegal = { doc -> navController.navigate(LegalRoute(doc.name)) },
                        onDataWiped = {
                            appViewModel.resetToOnboarding()
                            onboardingVm.sendIntent(OnboardingIntent.Restart)
                            navController.resetTo(OnboardingRoute)
                        }
                    )
                }
                composable<TemplateRoute> { backStack ->
                    val route = backStack.toRoute<TemplateRoute>()
                    LaunchedEffect(route.id) { activationVm.sendIntent(ActivationIntent.Load(route.id)) }
                    val state by activationVm.uiState.collectAsState()
                    TemplateDetailScreen(
                        state = state,
                        onBack = { navController.popBackStack() },
                        onUse = { navController.navigate(CustomizeRoute(route.id, route.fromOnboarding)) }
                    )
                }
                composable<CustomizeRoute> { backStack ->
                    val route = backStack.toRoute<CustomizeRoute>()
                    LaunchedEffect(route.id) { activationVm.sendIntent(ActivationIntent.Load(route.id)) }
                    val state by activationVm.uiState.collectAsState()
                    CustomizeScreen(state, activationVm::sendIntent, onBack = { navController.popBackStack() })
                }
                composable<BuilderRoute> { backStack ->
                    val route = backStack.toRoute<BuilderRoute>()
                    val vm = rememberScreenViewModel(route.id) { TemplateBuilderViewModel(templates, route.id) }
                    val state by vm.uiState.collectAsState()
                    TemplateBuilderScreen(
                        state = state,
                        effects = vm.effect,
                        onIntent = vm::sendIntent,
                        onBack = { navController.popBackStack() },
                        onSaved = { id, published ->
                            navController.popBackStack()
                            if (published) navController.navigate(TemplateRoute(id))
                        }
                    )
                }
                composable<RoutineRoute> {
                    val vm = rememberScreenViewModel(Unit) { RoutineViewModel(routineManager, practices, trackers, templates) }
                    val state by vm.uiState.collectAsState()
                    RoutineScreen(
                        state = state,
                        effects = vm.effect,
                        onIntent = vm::sendIntent,
                        onBack = { navController.popBackStack() },
                        onOpenHabit = { navController.navigate(HabitRoute(it)) },
                        onChangeTemplate = { navController.switchTab(DiscoverRoute) }
                    )
                }
                composable<LegalRoute> { backStack ->
                    val route = backStack.toRoute<LegalRoute>()
                    LegalScreen(LegalDoc.of(route.doc), onBack = { navController.popBackStack() })
                }
                composable<HabitRoute> { backStack ->
                    val route = backStack.toRoute<HabitRoute>()
                    val vm = rememberScreenViewModel(route.id) { HabitDetailViewModel(route.id, routineManager, practices, clock) }
                    val state by vm.uiState.collectAsState()
                    HabitDetailScreen(state, vm.effect, vm::sendIntent, onBack = { navController.popBackStack() })
                }
            }

            // The focus timer is a full-screen moment: hide the nav so it never covers its buttons.
            val todayState by todayVm.uiState.collectAsState()
            val timerOpen = currentTab == NavTabType.HOME && todayState.timer != null
            Column(Modifier.align(Alignment.BottomCenter).widthIn(max = MaxContentWidth)) {
                SnackbarHost(
                    hostState = snackbarHost,
                    modifier = Modifier.padding(horizontal = 16.dp).then(if (currentTab == null || timerOpen) Modifier.navigationBarsPadding() else Modifier)
                ) { data ->
                    Snackbar(
                        snackbarData = data,
                        containerColor = colors.onSurface,
                        contentColor = colors.surface,
                        actionColor = colors.primaryContainer,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp)
                    )
                }
                if (currentTab != null && !timerOpen) {
                    FloatingPillNavBar(
                        tabs = tabs,
                        currentType = currentTab,
                        onTabSelected = { navController.switchTab(it.route) },
                        backdrop = if (blurSupported) backdrop else null,
                        backdropOrigin = contentOrigin,
                        modifier = Modifier.navigationBarsPadding()
                    )
                }
            }
        }
    }
}
