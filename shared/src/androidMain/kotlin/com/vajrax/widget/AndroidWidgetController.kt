package com.vajrax.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.vajrax.platform.WidgetController
import com.vajrax.platform.WidgetTimelineSnapshot

/**
 * Android Widget Controller.
 * Broadcasts widget update events to AppWidgetProvider.
 */
class AndroidWidgetController(
    private val context: Context
) : WidgetController {

    companion object {
        const val ACTION_UPDATE_VAJRA_WIDGET = "com.vajrax.android.ACTION_UPDATE_WIDGET"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_DURATION = "extra_duration"
        const val EXTRA_PROGRESS = "extra_progress"
        const val EXTRA_ACTION_ID = "extra_action_id"
    }

    override fun updateWidget(snapshot: WidgetTimelineSnapshot) {
        val intent = Intent(ACTION_UPDATE_VAJRA_WIDGET).apply {
            putExtra(EXTRA_TITLE, snapshot.currentFocusTitle)
            putExtra(EXTRA_DURATION, snapshot.currentFocusDuration)
            putExtra(EXTRA_PROGRESS, "${snapshot.completedCount}/${snapshot.totalCount}")
            putExtra(EXTRA_ACTION_ID, snapshot.currentActionId)
            setPackage(context.packageName)
        }
        context.sendBroadcast(intent)
    }
}
