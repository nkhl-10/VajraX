package com.vajrax.ui

import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.vajrax.data.local.VajraDatabase
import com.vajrax.data.repository.DataRepositoryImpl
import com.vajrax.data.repository.PracticeRepositoryImpl
import com.vajrax.data.repository.ProfileRepositoryImpl
import com.vajrax.data.repository.SettingsRepositoryImpl
import com.vajrax.data.repository.TemplateRepositoryImpl
import com.vajrax.data.repository.TrackerRepositoryImpl
import com.vajrax.domain.FixedClock
import com.vajrax.domain.usecase.PreferencesService
import com.vajrax.domain.usecase.ProfileRules
import com.vajrax.domain.usecase.ProfileService
import com.vajrax.domain.usecase.RoutineManager
import com.vajrax.domain.usecase.TemplateLibraryService
import com.vajrax.ui.features.profile.ProfileEffect
import com.vajrax.ui.features.profile.ProfileIntent
import com.vajrax.ui.features.profile.ProfileViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull

/** Profile screen state and writes, against a real in-memory database. */
@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {
    private val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).also {
        VajraDatabase.Schema.synchronous().create(it)
    }
    private val db = VajraDatabase(driver)
    private val clock = FixedClock(LocalDateTime(LocalDate.parse("2026-09-28"), LocalTime(9, 0)))
    private val practices = PracticeRepositoryImpl(db)
    private val trackers = TrackerRepositoryImpl(db, practices)
    private val templates = TemplateRepositoryImpl(db)
    private val settings = SettingsRepositoryImpl(db)
    private val profiles = ProfileRepositoryImpl(db)
    private val routine = RoutineManager(practices, trackers, templates, settings, profiles, clock)

    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel() = ProfileViewModel(
        profiles, trackers, templates, settings, DataRepositoryImpl(db), clock, practices,
        PreferencesService(settings), ProfileService(profiles, clock), TemplateLibraryService(templates)
    )

    /** Keeps the screen "visible" (its flows only run while collected) and records messages. */
    private fun TestScope.attach(vm: ProfileViewModel): MutableStateFlow<List<String>> {
        val messages = MutableStateFlow<List<String>>(emptyList())
        backgroundScope.launch { vm.uiState.collect { } }
        backgroundScope.launch {
            vm.effect.filterIsInstance<ProfileEffect.ShowMessage>().collect { m -> messages.update { it + m.message } }
        }
        // Effects aren't replayed: subscribe before the test sends any intent.
        runCurrent()
        return messages
    }

    private suspend fun activate() {
        routine.startup()
        val template = assertNotNull(templates.getTemplateWithHabits("morning_discipline"))
        routine.activateTemplate(template, routine.draftHabits(template))
    }

    @Test
    fun showsTheActiveRoutineWithAFriendlyDefaultName() = runTest {
        activate()
        val vm = viewModel()
        attach(vm)

        val state = vm.uiState.first { !it.isLoading && it.tracker != null }
        assertEquals("You", state.displayName)
        assertEquals(1, state.dayNumber)
        vm.onCleared()
    }

    @Test
    fun invalidEmailIsRejectedAndNothingIsSaved() = runTest {
        routine.startup()
        val vm = viewModel()
        val messages = attach(vm)

        vm.sendIntent(ProfileIntent.SaveProfile("Sam", "sam@"))

        val shown = messages.first { it.isNotEmpty() }.single()
        assertEquals(ProfileRules.emailError("sam@"), shown)
        assertNotEquals("Sam", profiles.getProfile()?.displayName)
        vm.onCleared()
    }

    @Test
    fun savedProfileUpdatesTheHeader() = runTest {
        routine.startup()
        val vm = viewModel()
        val messages = attach(vm)

        vm.sendIntent(ProfileIntent.SaveProfile("  Sam Lee ", "sam@example.com"))

        val state = vm.uiState.first { it.displayName == "Sam Lee" }
        assertEquals("SL", state.initials)
        assertEquals("sam@example.com", state.email)
        assertEquals("Profile updated", messages.first { it.isNotEmpty() }.single())
        vm.onCleared()
    }

    @Test
    fun remindersSwitchFollowsTheStoredSetting() = runTest {
        routine.startup()
        val vm = viewModel()
        attach(vm)

        vm.sendIntent(ProfileIntent.SetReminders(true))
        vm.uiState.first { it.remindersEnabled }
        vm.sendIntent(ProfileIntent.SetReminders(false))
        vm.uiState.first { !it.remindersEnabled }
        vm.onCleared()
    }
}
