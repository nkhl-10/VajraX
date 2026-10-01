package com.vajrax.android

import com.vajrax.core.coroutines.runCatchingCancellable
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
import com.vajrax.android.widget.VajraWidgets
import com.vajrax.domain.repository.SettingsRepository
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
        // Debug builds log handled failures to logcat; release builds stay silent.
        if (applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE != 0) {
            com.vajrax.core.log.VxLog.sink = { tag, message, error -> android.util.Log.w("VajraX/$tag", message, error) }
        }
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
        com.vajrax.data.remote.SupabaseConfig.configure(BuildConfig.SUPABASE_URL, BuildConfig.SUPABASE_KEY)
        TimeFormat.use24Hour = android.text.format.DateFormat.is24HourFormat(this)
        ReminderReceiver.ensureChannel(this)
        appScope.launch {
            runCatchingCancellable {
                val koin = GlobalContext.get()
                koin.get<RoutineManager>().startup()
                koin.get<HabitReminderScheduler>().sync()
                koin.get<WidgetController>().refresh()
            }.onFailure { com.vajrax.core.log.VxLog.w("App", "Startup work failed", it) }
            runCatchingCancellable { publishWidgetPreviews() }.onFailure { com.vajrax.core.log.VxLog.w("App", "Widget previews not published", it) }
        }
    }

    /** Widget-picker previews (Android 15+) are rate-limited, so publish once per install or update. */
    private suspend fun publishWidgetPreviews() {
        val settings = GlobalContext.get().get<SettingsRepository>()
        val stamp = packageManager.getPackageInfo(packageName, 0).lastUpdateTime.toString()
        val done = settings.get(KEY_WIDGET_PREVIEWS) == stamp
        if (VajraWidgets.publishPreviews(this, alreadyPublished = done) && !done) settings.put(KEY_WIDGET_PREVIEWS, stamp)
    }

    private companion object {
        const val KEY_WIDGET_PREVIEWS = "widget_previews_published"
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
        runCatchingCancellable { scheduler.sync() }.onFailure { com.vajrax.core.log.VxLog.w("Reminders", "Couldn't reschedule reminders", it) }
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
        runCatchingCancellable {
            manager.activeNotifications
                .filter { it.id == ReminderReceiver.NOTIFICATION_ID && it.tag != null }
                .forEach { n ->
                    val occ = practices.getOccurrence(n.tag, today)
                    if (occ == null || !occ.isOpen) manager.cancel(n.tag, n.id)
                }
        }.onFailure { com.vajrax.core.log.VxLog.w("Reminders", "Couldn't clear finished reminders", it) }
    }
}
