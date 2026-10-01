package com.vajrax.app

import com.vajrax.core.coroutines.AppDispatchers
import com.vajrax.core.coroutines.runCatchingCancellable
import com.vajrax.core.time.AppClock
import com.vajrax.core.time.minuteTicks
import com.vajrax.domain.repository.SettingsRepository
import com.vajrax.domain.repository.TrackerRepository
import com.vajrax.domain.usecase.RoutineManager
import com.vajrax.ui.theme.ThemeMode
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

data class AppState(
    val startRoute: String? = null,
    val themeMode: ThemeMode = ThemeMode.AUTO,
    val startupError: String? = null
)

/**
 * App-wide state: startup (seed + materialize), start destination, theme, and day rollover.
 */
class AppViewModel(
    private val routineManager: RoutineManager,
    private val trackers: TrackerRepository,
    private val settings: SettingsRepository,
    private val clock: AppClock
) {
    // A failure in one app-wide job is logged, never crashes the app.
    private val scope = CoroutineScope(
        Dispatchers.Main + SupervisorJob() +
            CoroutineExceptionHandler { _, e -> com.vajrax.core.log.VxLog.w("App", "Unhandled error", e) }
    )
    private val _state = MutableStateFlow(AppState())
    val state: StateFlow<AppState> = _state.asStateFlow()

    init {
        start()
        scope.launch {
            settings.observe(SettingsRepository.THEME).collect { raw ->
                _state.update { it.copy(themeMode = ThemeMode.of(raw)) }
            }
        }
        scope.launch(AppDispatchers.IO) {
            var lastDay: LocalDate? = null
            clock.minuteTicks().collect { (day, _) ->
                if (lastDay != null && day != lastDay) {
                    runCatchingCancellable { routineManager.materialize() }.onFailure { com.vajrax.core.log.VxLog.w("App", "Day rollover failed", it) }
                }
                lastDay = day
            }
        }
    }

    /**
     * Prepares the database and picks the first screen. A failure keeps the user on the splash
     * with a retry: guessing "onboarding" would make a returning user set up a new routine.
     */
    fun start() {
        scope.launch(AppDispatchers.IO) {
            val route = runCatchingCancellable {
                routineManager.startup()
                if (trackers.getActiveTracker() != null) "home" else "onboarding"
            }
            _state.update {
                it.copy(
                    startRoute = route.getOrNull(),
                    startupError = route.exceptionOrNull()?.let { "We couldn't open your data on this phone." }
                )
            }
        }
    }

    /** Splash "Try again" after a failed start. */
    fun retryStart() {
        if (_state.value.startRoute != null) return
        _state.update { it.copy(startupError = null) }
        start()
    }

    /** Called after "Delete all data": the next launch path is onboarding. */
    fun resetToOnboarding() {
        _state.update { it.copy(startRoute = "onboarding") }
        scope.launch(AppDispatchers.IO) {
            runCatchingCancellable { routineManager.startup(force = true) }.onFailure { com.vajrax.core.log.VxLog.w("App", "Restart after wipe failed", it) }
        }
    }
}
