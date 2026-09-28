package com.vajrax.app

import com.vajrax.core.time.AppClock
import com.vajrax.core.time.minuteTicks
import com.vajrax.domain.repository.SettingsRepository
import com.vajrax.domain.repository.TrackerRepository
import com.vajrax.domain.usecase.RoutineManager
import com.vajrax.ui.theme.ThemeMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
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
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val _state = MutableStateFlow(AppState())
    val state: StateFlow<AppState> = _state.asStateFlow()

    init {
        start()
        scope.launch {
            settings.observe(SettingsRepository.THEME).collect { raw ->
                _state.update { it.copy(themeMode = ThemeMode.of(raw)) }
            }
        }
        scope.launch(Dispatchers.IO) {
            var lastDay: LocalDate? = null
            clock.minuteTicks().collect { (day, _) ->
                if (lastDay != null && day != lastDay) runCatching { routineManager.materialize() }
                lastDay = day
            }
        }
    }

    fun start() {
        scope.launch(Dispatchers.IO) {
            val route = runCatching {
                routineManager.startup()
                if (trackers.getActiveTracker() != null) "home" else "onboarding"
            }
            _state.update {
                it.copy(
                    startRoute = route.getOrElse { "onboarding" },
                    startupError = route.exceptionOrNull()?.let { "Couldn't prepare your data. Restart the app to try again." }
                )
            }
        }
    }

    /** Called after "Delete all data": the next launch path is onboarding. */
    fun resetToOnboarding() {
        _state.update { it.copy(startRoute = "onboarding") }
        scope.launch(Dispatchers.IO) { runCatching { routineManager.startup() } }
    }
}
