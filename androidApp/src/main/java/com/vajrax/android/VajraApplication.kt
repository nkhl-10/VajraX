package com.vajrax.android

import android.app.Application
import android.app.NotificationManager
import android.os.Build
import com.vajrax.android.account.KeystoreTokenStore
import com.vajrax.android.account.SyncWork
import com.vajrax.android.reminders.HabitReminderScheduler
import com.vajrax.android.reminders.ReminderReceiver
import com.vajrax.android.widget.GlanceWidgetController
import com.vajrax.android.widget.VajraWidgets
import com.vajrax.core.coroutines.runCatchingCancellable
import com.vajrax.core.time.AppClock
import com.vajrax.core.time.TimeFormat
import com.vajrax.data.local.DatabaseDriverFactory
import com.vajrax.data.remote.ApiConfig
import com.vajrax.data.remote.TokenStore
import com.vajrax.di.initKoin
import com.vajrax.domain.account.AccountService
import com.vajrax.domain.account.AccountState
import com.vajrax.domain.repository.PracticeRepository
import com.vajrax.domain.repository.SettingsRepository
import com.vajrax.domain.usecase.AppHooks
import com.vajrax.domain.usecase.RoutineManager
import com.vajrax.platform.WidgetController
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
                        single<AppHooks> {
                            AndroidAppHooks(applicationContext, get(), get(), get(), get(), signedIn = ::isSignedIn)
                        }
                        // Optional account + sync: only when a server is configured (BuildConfig.API_BASE_URL).
                        single<TokenStore> { KeystoreTokenStore(applicationContext) }
                        if (BuildConfig.API_BASE_URL.isNotBlank()) {
                            single {
                                ApiConfig(
                                    BuildConfig.API_BASE_URL,
                                    deviceName = "${Build.MANUFACTURER} ${Build.MODEL}".take(DEVICE_NAME_MAX)
                                )
                            }
                        }
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
            runCatchingCancellable { publishWidgetPreviews() }.onFailure {
                com.vajrax.core.log.VxLog.w(
                    "App",
                    "Widget previews not published",
                    it
                )
            }
        }
        // Background sync runs only while signed in.
        appScope.launch {
            GlobalContext.get().get<AccountService>().state.collect { state ->
                val app = this@VajraApplication
                if (state is AccountState.SignedIn) SyncWork.schedulePeriodic(app) else SyncWork.cancel(app)
            }
        }
    }

    private fun isSignedIn(): Boolean = GlobalContext.getOrNull()?.getOrNull<AccountService>()?.state?.value is AccountState.SignedIn

    /** Widget-picker previews (Android 15+) are rate-limited, so publish once per install or update. */
    private suspend fun publishWidgetPreviews() {
        val settings = GlobalContext.get().get<SettingsRepository>()
        val stamp = packageManager.getPackageInfo(packageName, 0).lastUpdateTime.toString()
        val done = settings.get(KEY_WIDGET_PREVIEWS) == stamp
        if (VajraWidgets.publishPreviews(this, alreadyPublished = done) && !done) {
            settings.put(
                KEY_WIDGET_PREVIEWS,
                stamp
            )
        }
    }

    private companion object {
        const val KEY_WIDGET_PREVIEWS = "widget_previews_published"
        const val DEVICE_NAME_MAX = 80
    }
}

/** Keeps reminders, posted notifications and the widget in step with every data change. */
class AndroidAppHooks(
    private val context: android.content.Context,
    private val scheduler: HabitReminderScheduler,
    private val widget: WidgetController,
    private val practices: PracticeRepository,
    private val clock: AppClock,
    private val signedIn: () -> Boolean = { false }
) : AppHooks {
    override suspend fun onRoutineChanged() {
        runCatchingCancellable { scheduler.sync() }.onFailure {
            com.vajrax.core.log.VxLog.w(
                "Reminders",
                "Couldn't reschedule reminders",
                it
            )
        }
        clearResolvedNotifications()
        widget.refresh()
        uploadSoon()
    }

    override suspend fun onCheckInChanged() {
        clearResolvedNotifications()
        widget.refresh()
        uploadSoon()
    }

    /** A change made from a widget or notification reaches the account even if the app isn't opened. */
    private fun uploadSoon() {
        if (signedIn()) SyncWork.requestSoon(context)
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
