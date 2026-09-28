package com.vajrax.platform

data class WidgetTimelineSnapshot(
    val currentFocusTitle: String,
    val currentFocusDuration: Int,
    val currentFocusScheduledTime: String?,
    val currentActionId: String?,
    val completedCount: Int,
    val totalCount: Int,
    val nextFocusTitle: String?
)

/**
 * Multiplatform Home Screen Widget Controller.
 * Pushes real-time snapshot updates to Android AppWidgets and iOS WidgetKit.
 */
interface WidgetController {
    fun updateWidget(snapshot: WidgetTimelineSnapshot)

    /** Ask widgets to re-read today's data from the shared database. */
    fun refresh() {}
}
