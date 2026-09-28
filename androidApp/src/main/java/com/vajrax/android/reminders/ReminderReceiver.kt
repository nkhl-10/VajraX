package com.vajrax.android.reminders

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Icon
import android.os.Build
import com.vajrax.android.MainActivity
import com.vajrax.android.R
import com.vajrax.core.time.AppClock
import com.vajrax.core.time.TimeFormat
import com.vajrax.domain.habit.Occurrence
import com.vajrax.domain.repository.PracticeRepository
import com.vajrax.domain.repository.SettingsRepository
import com.vajrax.domain.usecase.RoutineManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.context.GlobalContext

/**
 * Posts habit reminders and handles their Done / Snooze actions through the same
 * [RoutineManager] the app uses, then keeps the next alarm scheduled.
 * Also reschedules after reboot, app update and time / time-zone changes.
 */
class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                handle(context.applicationContext, intent)
            } catch (_: Exception) {
                // Never crash in the background; the next sync will recover.
            } finally {
                pending.finish()
            }
        }
    }

    private suspend fun handle(context: Context, intent: Intent) {
        val koin = GlobalContext.getOrNull() ?: return
        val routine = koin.get<RoutineManager>()
        val practices = koin.get<PracticeRepository>()
        val scheduler = koin.get<HabitReminderScheduler>()
        val clock = koin.get<AppClock>()
        when (intent.action) {
            ACTION_REMIND -> {
                val habitId = intent.getStringExtra(EXTRA_HABIT_ID) ?: return
                routine.materialize()
                val habit = practices.getHabit(habitId)
                val occ = practices.getOccurrence(habitId, clock.today())
                if (habit != null && !habit.isArchived && occ != null && occ.isOpen) {
                    val privacy = koin.get<SettingsRepository>().get(SettingsRepository.NOTIFICATION_PRIVACY) ?: "GENERIC"
                    val (title, text) = content(privacy, habit, occ)
                    notify(context, habitId, occ, title, text)
                }
                scheduler.sync()
            }
            ACTION_TEST -> {
                routine.materialize()
                val tracker = koin.get<com.vajrax.domain.repository.TrackerRepository>().getActiveTracker()
                val now = clock.minuteOfDay()
                val candidates = if (tracker == null) emptyList() else {
                    val habits = practices.getTrackerHabits(tracker.id).associateBy { it.id }
                    practices.getRange(clock.today(), clock.today()).filter { it.isOpen }
                        .mapNotNull { o -> habits[o.habitId]?.let { it to o } }
                        .sortedBy { (h, o) -> TimeFormat.toMinutes(o.scheduledTime ?: h.time) ?: Int.MAX_VALUE }
                }
                val pick = candidates.firstOrNull { (h, o) -> (TimeFormat.toMinutes(o.scheduledTime ?: h.time) ?: 0) + h.durationMinutes > now }
                    ?: candidates.firstOrNull()
                val privacy = koin.get<SettingsRepository>().get(SettingsRepository.NOTIFICATION_PRIVACY) ?: "GENERIC"
                if (pick == null) {
                    notifyPlain(context, "VAJRAX", "Reminders are working. Nothing is open right now.")
                } else {
                    val (habit, occ) = pick
                    val (title, text) = content(privacy, habit, occ)
                    notify(context, habit.id, occ, title, text)
                }
            }
            ACTION_DONE -> {
                val occId = intent.getStringExtra(EXTRA_OCCURRENCE_ID) ?: return
                cancelNotification(context, intent.getStringExtra(EXTRA_HABIT_ID))
                routine.complete(occId)
            }
            ACTION_SNOOZE -> {
                val occId = intent.getStringExtra(EXTRA_OCCURRENCE_ID) ?: return
                val habitId = intent.getStringExtra(EXTRA_HABIT_ID) ?: return
                cancelNotification(context, habitId)
                // Moves today's occurrence; the reminder is rescheduled to the new time via AppHooks.
                routine.snooze(occId, 15)
            }
            Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_TIMEZONE_CHANGED, Intent.ACTION_TIME_CHANGED -> {
                routine.materialize()
                scheduler.sync()
            }
        }
    }

    private fun content(privacy: String, habit: com.vajrax.domain.habit.Habit, occ: Occurrence): Pair<String, String> = when (privacy) {
        "FULL" -> habit.title to "${TimeFormat.display(occ.scheduledTime ?: habit.time)} · ${TimeFormat.duration(habit.durationMinutes)}"
        "HIDDEN" -> "VAJRAX" to "Reminder"
        else -> "VAJRAX" to "You have a habit due"
    }

    private fun notifyPlain(context: Context, title: String, text: String) {
        if (!permitted(context)) return
        ensureChannel(context)
        val notification = Notification.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_vajrax)
            .setContentTitle(title)
            .setContentText(text)
            .setAutoCancel(true)
            .build()
        context.getSystemService(NotificationManager::class.java)?.notify(TAG_TEST, NOTIFICATION_ID, notification)
    }

    private fun permitted(context: Context) = Build.VERSION.SDK_INT < 33 ||
        context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    private fun notify(context: Context, habitId: String, occ: Occurrence, title: String, text: String) {
        if (!permitted(context)) return
        ensureChannel(context)
        val open = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        fun action(actionName: String, code: Int): PendingIntent = PendingIntent.getBroadcast(
            context,
            habitId.hashCode() + code,
            Intent(context, ReminderReceiver::class.java).setAction(actionName)
                .putExtra(EXTRA_HABIT_ID, habitId)
                .putExtra(EXTRA_OCCURRENCE_ID, occ.id),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = Notification.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_vajrax)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(open)
            .setAutoCancel(true)
            .setCategory(Notification.CATEGORY_REMINDER)
            .setVisibility(Notification.VISIBILITY_PRIVATE)
            .addAction(Notification.Action.Builder(Icon.createWithResource(context, R.drawable.ic_action_check), "Done", action(ACTION_DONE, 1)).build())
            .addAction(Notification.Action.Builder(Icon.createWithResource(context, R.drawable.ic_action_check), "Snooze 15 min", action(ACTION_SNOOZE, 2)).build())
            .build()
        context.getSystemService(NotificationManager::class.java)?.notify(habitId, NOTIFICATION_ID, notification)
    }

    private fun cancelNotification(context: Context, habitId: String?) {
        habitId ?: return
        context.getSystemService(NotificationManager::class.java)?.cancel(habitId, NOTIFICATION_ID)
    }

    companion object {
        const val ACTION_REMIND = "com.vajrax.android.action.REMIND"
        const val ACTION_DONE = "com.vajrax.android.action.REMINDER_DONE"
        const val ACTION_SNOOZE = "com.vajrax.android.action.REMINDER_SNOOZE"
        const val ACTION_TEST = "com.vajrax.android.action.REMINDER_TEST"
        /** Notifications are tagged with the habit id so a check-in anywhere can clear them. */
        const val NOTIFICATION_ID = 1001
        private const val TAG_TEST = "vajrax_test"
        const val EXTRA_HABIT_ID = "habit_id"
        const val EXTRA_OCCURRENCE_ID = "occurrence_id"
        const val CHANNEL_ID = "habit_reminders"

        fun ensureChannel(context: Context) {
            val manager = context.getSystemService(NotificationManager::class.java) ?: return
            if (manager.getNotificationChannel(CHANNEL_ID) != null) return
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, context.getString(R.string.reminder_channel_name), NotificationManager.IMPORTANCE_DEFAULT).apply {
                    description = context.getString(R.string.reminder_channel_description)
                    setShowBadge(false)
                }
            )
        }
    }
}
