package com.vajrax.android

import android.app.Application
import android.app.NotificationManager
import com.vajrax.core.time.AppClock
import com.vajrax.core.time.TimeFormat
import com.vajrax.domain.repository.PracticeRepository
import com.vajrax.android.reminders.HabitReminderScheduler
import com.vajrax.android.reminders.ReminderReceiver
import com.vajrax.data.local.DatabaseDriverFactory
import com.vajrax.di.initKoin
import com.vajrax.domain.usecase.AppHooks
import com.vajrax.domain.usecase.RoutineManager
import com.vajrax.platform.WidgetController
import com.vajrax.android.widget.GlanceWidgetController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.context.GlobalContext
import org.koin.dsl.module

/**
 * Starts the dependency graph for every entry point (activity, widget, reminder receivers),
 * so background actions work even when the UI was never opened.
 */
class VajraApplication : Application() {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        if (GlobalContext.getOrNull() == null) {
            initKoin {
                modules(
                    module {
                        single { DatabaseDriverFactory(applicationContext) }
                        single<WidgetController> { GlanceWidgetController(applicationContext) }
                        single { HabitReminderScheduler(applicationContext, get(), get(), get(), get()) }
                        single<AppHooks> { AndroidAppHooks(applicationContext, get(), get(), get(), get()) }
                    }
                )
            }
        }
        TimeFormat.use24Hour = android.text.format.DateFormat.is24HourFormat(this)
        ReminderReceiver.ensureChannel(this)
        appScope.launch {
            runCatching {
                val koin = GlobalContext.get()
                koin.get<RoutineManager>().startup()
                koin.get<HabitReminderScheduler>().sync()
                koin.get<WidgetController>().refresh()
            }
        }
    }
}

/** Keeps reminders, posted notifications and the widget in step with every data change. */
class AndroidAppHooks(
    private val context: android.content.Context,
    private val scheduler: HabitReminderScheduler,
    private val widget: WidgetController,
    private val practices: PracticeRepository,
    private val clock: AppClock
) : AppHooks {
    override suspend fun onRoutineChanged() {
        runCatching { scheduler.sync() }
        clearResolvedNotifications()
        widget.refresh()
    }

    override suspend fun onCheckInChanged() {
        clearResolvedNotifications()
        widget.refresh()
    }

    /** A reminder whose habit was completed or skipped elsewhere (app, widget) is removed from the shade. */
    private suspend fun clearResolvedNotifications() {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val today = clock.today()
        runCatching {
            manager.activeNotifications
                .filter { it.id == ReminderReceiver.NOTIFICATION_ID && it.tag != null }
                .forEach { n ->
                    val occ = practices.getOccurrence(n.tag, today)
                    if (occ == null || !occ.isOpen) manager.cancel(n.tag, n.id)
                }
        }
    }
}
