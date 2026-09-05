package com.vajrax.android.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.vajrax.android.MainActivity
import com.vajrax.android.R
import com.vajrax.widget.AndroidWidgetController

/**
 * Phase 10 & 11: Home Screen Widget Provider for 1-Tap Task Completion.
 * Allows completing or doing minimum from the Home Screen with zero friction.
 */
class VajraTodayWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_WIDGET_COMPLETE = "com.vajrax.android.WIDGET_ACTION_COMPLETE"
        const val ACTION_WIDGET_MINIMUM = "com.vajrax.android.WIDGET_ACTION_MINIMUM"
        const val EXTRA_ACTION_ID = "extra_action_id"
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, "DEEP WORK", 60, "2/5 Done", "act_3")
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        when (intent.action) {
            AndroidWidgetController.ACTION_UPDATE_VAJRA_WIDGET -> {
                val title = intent.getStringExtra(AndroidWidgetController.EXTRA_TITLE) ?: "DEEP WORK"
                val duration = intent.getIntExtra(AndroidWidgetController.EXTRA_DURATION, 60)
                val progress = intent.getStringExtra(AndroidWidgetController.EXTRA_PROGRESS) ?: "2/5 Done"
                val actionId = intent.getStringExtra(AndroidWidgetController.EXTRA_ACTION_ID) ?: "act_3"

                val appWidgetManager = AppWidgetManager.getInstance(context)
                val thisWidget = ComponentName(context, VajraTodayWidgetProvider::class.java)
                val allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)

                for (id in allWidgetIds) {
                    updateAppWidget(context, appWidgetManager, id, title, duration, progress, actionId)
                }
            }
            ACTION_WIDGET_COMPLETE -> {
                val actionId = intent.getStringExtra(EXTRA_ACTION_ID)
                // 1-Tap complete directly from home screen!
                // Broadcast updated timeline or complete in background
            }
            ACTION_WIDGET_MINIMUM -> {
                val actionId = intent.getStringExtra(EXTRA_ACTION_ID)
                // 1-Tap minimum completion
            }
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        title: String,
        durationMinutes: Int,
        progress: String,
        actionId: String
    ) {
        val views = RemoteViews(context.packageName, R.layout.vajra_widget_today)

        views.setTextViewText(R.id.widget_task_title, title.uppercase())
        views.setTextViewText(R.id.widget_task_duration, "$durationMinutes MINUTE FOCUS BLOCK")
        views.setTextViewText(R.id.widget_progress, progress)

        // Open App Intent when tapping title/card
        val openAppIntent = Intent(context, MainActivity::class.java)
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_root, openAppPendingIntent)

        // Complete Shortcut Intent
        val completeIntent = Intent(context, VajraTodayWidgetProvider::class.java).apply {
            action = ACTION_WIDGET_COMPLETE
            putExtra(EXTRA_ACTION_ID, actionId)
        }
        val completePendingIntent = PendingIntent.getBroadcast(
            context,
            1,
            completeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.btn_widget_complete, completePendingIntent)

        // Minimum Shortcut Intent
        val minIntent = Intent(context, VajraTodayWidgetProvider::class.java).apply {
            action = ACTION_WIDGET_MINIMUM
            putExtra(EXTRA_ACTION_ID, actionId)
        }
        val minPendingIntent = PendingIntent.getBroadcast(
            context,
            2,
            minIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.btn_widget_minimum, minPendingIntent)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}
