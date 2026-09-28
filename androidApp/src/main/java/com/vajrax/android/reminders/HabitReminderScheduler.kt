@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.vajrax.android.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.vajrax.core.time.AppClock
import com.vajrax.core.time.TimeFormat
import com.vajrax.domain.habit.ScheduleRules
import com.vajrax.domain.repository.PracticeRepository
import com.vajrax.domain.repository.SettingsRepository
import com.vajrax.domain.repository.TrackerRepository
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant

/**
 * Keeps exactly one pending alarm per habit that has a reminder (spec 06 §8): obsolete alarms are
 * cancelled before rescheduling, request codes are stable per habit, nothing polls in the background.
 */
class HabitReminderScheduler(
    private val context: Context,
    private val practices: PracticeRepository,
    private val trackers: TrackerRepository,
    private val settings: SettingsRepository,
    private val clock: AppClock
) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    suspend fun sync() {
        val previous = settings.get(KEY_SCHEDULED)?.split(",")?.filter { it.isNotBlank() }.orEmpty()
        previous.forEach { cancel(it) }
        val enabled = settings.get(SettingsRepository.REMINDERS_ENABLED) == "true"
        val tracker = trackers.getActiveTracker()
        if (!enabled || tracker == null) {
            settings.put(KEY_SCHEDULED, "")
            return
        }
        val today = clock.today()
        val nowMinute = clock.minuteOfDay()
        val scheduled = mutableListOf<String>()
        practices.getTrackerHabits(tracker.id).filter { it.reminderEnabled && !it.isArchived }.forEach { habit ->
            val minutes = TimeFormat.toMinutes(habit.reminderTime ?: habit.time) ?: return@forEach
            val todayOcc = practices.getOccurrence(habit.id, today)
            // A snoozed or moved occurrence reminds at its new time today.
            val movedToday = todayOcc?.takeIf { it.isOpen && it.scheduledTime != null && it.scheduledTime != habit.time }
                ?.let { TimeFormat.toMinutes(it.scheduledTime) }
            for (offset in 0..14) {
                val day = today.plus(offset, DateTimeUnit.DAY)
                val reminderMinute = if (offset == 0 && movedToday != null) movedToday else minutes
                if (offset == 0) {
                    if (todayOcc != null && !todayOcc.isOpen) continue
                    if (todayOcc == null && !ScheduleRules.isScheduled(habit, day)) continue
                    if (reminderMinute <= nowMinute) continue
                } else if (!ScheduleRules.isScheduled(habit, day)) continue
                val at = LocalDateTime(day, LocalTime(reminderMinute / 60, reminderMinute % 60)).toInstant(clock.timeZone())
                scheduleAt(habit.id, at.toEpochMilliseconds())
                scheduled += habit.id
                break
            }
        }
        settings.put(KEY_SCHEDULED, scheduled.joinToString(","))
    }

    fun scheduleAt(habitId: String, epochMillis: Long) {
        alarmManager?.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, epochMillis, pendingIntent(habitId))
    }

    fun cancel(habitId: String) {
        alarmManager?.cancel(pendingIntent(habitId))
    }

    private fun pendingIntent(habitId: String): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java)
            .setAction(ReminderReceiver.ACTION_REMIND)
            .putExtra(ReminderReceiver.EXTRA_HABIT_ID, habitId)
        return PendingIntent.getBroadcast(
            context,
            habitId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        private const val KEY_SCHEDULED = "reminder_scheduled_ids"
    }
}
