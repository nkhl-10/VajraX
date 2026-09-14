package com.vajrax.di

import com.vajrax.data.local.DatabaseDriverFactory
import com.vajrax.data.local.DatabaseSeeder
import com.vajrax.data.local.VajraDatabase
import com.vajrax.data.remote.SupabaseSyncManager
import com.vajrax.data.remote.createHttpClient
import com.vajrax.data.repository.AuthRepositoryImpl
import com.vajrax.data.repository.GrowRepositoryImpl
import com.vajrax.data.repository.LearnRepositoryImpl
import com.vajrax.data.repository.LifePathRepositoryImpl
import com.vajrax.data.repository.PracticeRepositoryImpl
import com.vajrax.data.repository.ReviewRepositoryImpl
import com.vajrax.domain.ai.AiClient
import com.vajrax.domain.ai.AiPatternInterpreter
import com.vajrax.domain.engine.BehavioralEngine
import com.vajrax.domain.engine.BhedaEngine
import com.vajrax.domain.engine.DamaEngine
import com.vajrax.domain.engine.DandaEngine
import com.vajrax.domain.engine.SamaEngine
import com.vajrax.domain.growth.GrowthAnalyticsManager
import com.vajrax.domain.learn.BookToLifeManager
import com.vajrax.domain.passive.PassiveIntelligenceManager
import com.vajrax.domain.repository.AuthRepository
import com.vajrax.domain.repository.GrowRepository
import com.vajrax.domain.repository.LearnRepository
import com.vajrax.domain.repository.LifePathRepository
import com.vajrax.domain.repository.PracticeRepository
import com.vajrax.domain.repository.ReviewRepository
import com.vajrax.domain.template.LifePathTemplateEngine
import com.vajrax.ui.features.calendar.CalendarViewModel
import com.vajrax.ui.features.grow.GrowViewModel
import com.vajrax.ui.features.learn.LearnViewModel
import com.vajrax.ui.features.path.PathViewModel
import com.vajrax.ui.features.review.ReviewViewModel
import com.vajrax.ui.features.today.TodayViewModel
import org.koin.dsl.module

/**
 * Phase 18: Complete Production Koin Dependency Injection.
 * Provides Database, Repositories, Behavioral Engine, AI Interpretation, and ViewModels.
 */
fun dataModule() = module {
    // 0. Networking
    single { createHttpClient() }

    // 1. SQLDelight Database & Seeder
    single { 
        val driverFactory = get<DatabaseDriverFactory>()
        val db = VajraDatabase(driverFactory.createDriver())
        DatabaseSeeder(db).seedInitialDataIfEmpty()
        db
    }

    // 2. Repositories
    single<AuthRepository> { AuthRepositoryImpl(get()) }
    single<LifePathRepository> { LifePathRepositoryImpl(get()) }
    single<PracticeRepository> { PracticeRepositoryImpl(get()) }
    single<LearnRepository> { LearnRepositoryImpl(get()) }
    single<GrowRepository> { GrowRepositoryImpl(get()) }
    single<ReviewRepository> { ReviewRepositoryImpl(get()) }

    // 3. Behavioral Intervention Engine (Phase 12)
    single { SamaEngine() }
    single { DamaEngine(get()) }
    single { DandaEngine(get()) }
    single { BhedaEngine(get()) }
    single { BehavioralEngine(get(), get(), get(), get()) }

    // 4. Template Engine (Phase 14)
    single { LifePathTemplateEngine(get(), get()) }

    // 5. Book-to-Life Knowledge Pipeline (Phase 15)
    single { BookToLifeManager(get(), get()) }

    // 6. Growth & Character Analytics (Phase 16)
    single { GrowthAnalyticsManager(get(), get()) }

    // 7. Supabase Cloud Sync (Phase 17)
    single { SupabaseSyncManager(get(), get(), get()) }

    // 8. AI Pattern Interpretation Engine (Phase 18)
    single { AiPatternInterpreter() }
    single { com.vajrax.domain.ai.AiHumanTouchEngine() }
    single { AiClient(get(), get()) }


    // 9. Presentation ViewModels (MVI / UDF)
    single { TodayViewModel(get(), getOrNull()) }
    single { CalendarViewModel(get()) }
    single { PathViewModel(get(), get()) }
    single { LearnViewModel(get()) }
    single { GrowViewModel(get()) }
    single { ReviewViewModel(get()) }
    single { com.vajrax.ui.features.profile.ProfileViewModel(get(), get(), get()) }
}

