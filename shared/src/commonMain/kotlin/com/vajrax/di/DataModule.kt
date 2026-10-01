package com.vajrax.di

import com.vajrax.core.time.AppClock
import com.vajrax.core.time.SystemAppClock
import com.vajrax.data.local.DatabaseDriverFactory
import com.vajrax.data.local.VajraDatabase
import com.vajrax.data.remote.SupabaseSyncManager
import com.vajrax.data.remote.createHttpClient
import com.vajrax.data.repository.AuthRepositoryImpl
import com.vajrax.data.repository.DataRepositoryImpl
import com.vajrax.data.repository.GoalRepositoryImpl
import com.vajrax.data.repository.GrowRepositoryImpl
import com.vajrax.data.repository.LearnRepositoryImpl
import com.vajrax.data.repository.LifePathRepositoryImpl
import com.vajrax.data.repository.PracticeRepositoryImpl
import com.vajrax.data.repository.ProfileRepositoryImpl
import com.vajrax.data.repository.ReflectionRepositoryImpl
import com.vajrax.data.repository.ReviewRepositoryImpl
import com.vajrax.data.repository.SettingsRepositoryImpl
import com.vajrax.data.repository.TemplateRepositoryImpl
import com.vajrax.data.repository.TrackerRepositoryImpl
import com.vajrax.domain.ai.AiClient
import com.vajrax.domain.ai.AiPatternInterpreter
import com.vajrax.domain.engine.BehavioralEngine
import com.vajrax.domain.engine.BhedaEngine
import com.vajrax.domain.engine.DamaEngine
import com.vajrax.domain.engine.DandaEngine
import com.vajrax.domain.engine.SamaEngine
import com.vajrax.domain.growth.GrowthAnalyticsManager
import com.vajrax.domain.learn.BookToLifeManager
import com.vajrax.domain.repository.AuthRepository
import com.vajrax.domain.repository.DataRepository
import com.vajrax.domain.repository.GoalRepository
import com.vajrax.domain.repository.GrowRepository
import com.vajrax.domain.repository.LearnRepository
import com.vajrax.domain.repository.LifePathRepository
import com.vajrax.domain.repository.PracticeRepository
import com.vajrax.domain.repository.ProfileRepository
import com.vajrax.domain.repository.ReflectionRepository
import com.vajrax.domain.repository.ReviewRepository
import com.vajrax.domain.repository.SettingsRepository
import com.vajrax.domain.repository.TemplateRepository
import com.vajrax.domain.repository.TrackerRepository
import com.vajrax.domain.template.LifePathTemplateEngine
import com.vajrax.domain.usecase.AppHooks
import com.vajrax.domain.usecase.NoopAppHooks
import com.vajrax.domain.usecase.PreferencesService
import com.vajrax.domain.usecase.RoutineManager
import com.vajrax.app.AppViewModel
import com.vajrax.ui.features.calendar.CalendarViewModel
import com.vajrax.ui.features.discover.DiscoverViewModel
import com.vajrax.ui.features.learn.LearnViewModel
import com.vajrax.ui.features.onboarding.OnboardingViewModel
import com.vajrax.ui.features.path.PathViewModel
import com.vajrax.ui.features.profile.ProfileViewModel
import com.vajrax.ui.features.report.ReportViewModel
import com.vajrax.ui.features.review.ReviewViewModel
import com.vajrax.ui.features.today.TodayViewModel
import com.vajrax.ui.features.grow.GrowViewModel
import org.koin.dsl.module

/**
 * Shared dependency graph. Platform modules (see androidApp) provide [DatabaseDriverFactory]
 * and may provide [AppHooks] / [com.vajrax.platform.WidgetController]; they are looked up with
 * getOrNull so the shared graph works without them (tests, iOS).
 */
fun dataModule() = module {
    single { createHttpClient() }
    single<AppClock> { SystemAppClock }
    // Offline release: always offline. The sync phase provides a platform monitor instead.
    single<com.vajrax.domain.sync.ConnectivityMonitor> { com.vajrax.domain.sync.OfflineOnly }

    // Opening the database is lazy (first query, always on a background dispatcher); reference
    // data is seeded during RoutineManager.startup(), never on the thread that first asks for it.
    single { VajraDatabase(get<DatabaseDriverFactory>().createDriver()) }

    // Repositories
    single { PracticeRepositoryImpl(get()) }
    single<PracticeRepository> { get<PracticeRepositoryImpl>() }
    single<TrackerRepository> { TrackerRepositoryImpl(get(), get()) }
    single<TemplateRepository> { TemplateRepositoryImpl(get()) }
    single<SettingsRepository> { SettingsRepositoryImpl(get()) }
    single<ProfileRepository> { ProfileRepositoryImpl(get()) }
    single<GoalRepository> { GoalRepositoryImpl(get()) }
    single<ReflectionRepository> { ReflectionRepositoryImpl(get()) }
    single<DataRepository> { DataRepositoryImpl(get()) }
    single<AuthRepository> { AuthRepositoryImpl(get()) }
    single<LifePathRepository> { LifePathRepositoryImpl(get()) }
    single<LearnRepository> { LearnRepositoryImpl(get()) }
    single<GrowRepository> { GrowRepositoryImpl(get()) }
    single<ReviewRepository> { ReviewRepositoryImpl(get()) }

    // Domain
    single {
        RoutineManager(
            practices = get(),
            trackers = get(),
            templates = get(),
            settings = get(),
            profiles = get(),
            clock = get(),
            hooks = getOrNull<AppHooks>() ?: NoopAppHooks
        )
    }

    // Legacy VAJRAX engines (Learn / Path / AI companion)
    single { SamaEngine() }
    single { DamaEngine(get()) }
    single { DandaEngine(get()) }
    single { BhedaEngine(get()) }
    single { BehavioralEngine(get(), get(), get(), get()) }
    single { LifePathTemplateEngine(get(), get()) }
    single { BookToLifeManager(get(), get()) }
    single { GrowthAnalyticsManager(get(), get()) }
    single { SupabaseSyncManager(get(), get(), get()) }
    single { AiPatternInterpreter() }
    single { com.vajrax.domain.ai.AiHumanTouchEngine() }
    single { AiClient(get(), get()) }

    // Presentation
    single { AppViewModel(get(), get(), get(), get()) }
    single { PreferencesService(get(), getOrNull<AppHooks>() ?: NoopAppHooks) }
    single { com.vajrax.domain.usecase.ProfileService(get(), get()) }
    single { com.vajrax.domain.usecase.GoalService(get(), get()) }
    single { com.vajrax.domain.usecase.ReflectionService(get(), get()) }
    single { com.vajrax.domain.usecase.TemplateLibraryService(get()) }
    single { OnboardingViewModel(get(), get(), get(), get(), get(), get(), get()) }
    single { TodayViewModel(get(), get(), get(), get(), get(), get(), get(), get()) }
    single { CalendarViewModel(get(), get(), get(), get()) }
    single { DiscoverViewModel(get(), get()) }
    single { com.vajrax.ui.features.templates.ActivationViewModel(get(), get(), get(), get(), get()) }
    single { ReportViewModel(get(), get(), get(), get(), get(), get(), get()) }
    single { ProfileViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), getOrNull<AppHooks>()) }
    // Screens opened on top of a tab get a fresh view model each time (cleared when they close).
    factory { (templateId: String?) -> com.vajrax.ui.features.builder.TemplateBuilderViewModel(get(), get(), templateId) }
    factory { com.vajrax.ui.features.routine.RoutineViewModel(get(), get(), get(), get()) }
    factory { (habitId: String) -> com.vajrax.ui.features.routine.HabitDetailViewModel(habitId, get(), get(), get()) }
    single { PathViewModel(get(), get(), get()) }
    single { LearnViewModel(get()) }
    single { GrowViewModel(get()) }
    single { ReviewViewModel(get()) }
}
