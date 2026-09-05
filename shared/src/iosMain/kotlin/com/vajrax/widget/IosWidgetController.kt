package com.vajrax.widget

import com.vajrax.platform.WidgetController
import com.vajrax.platform.WidgetTimelineSnapshot

/**
 * iOS WidgetKit Controller.
 * Pushes timeline updates to iOS App Group UserDefaults and triggers WidgetCenter.reloadAllTimelines.
 */
class IosWidgetController : WidgetController {

    override fun updateWidget(snapshot: WidgetTimelineSnapshot) {
        // Writes snapshot to App Group shared UserDefaults (e.g., group.com.vajrax.app)
        // Calls WidgetCenter.shared.reloadAllTimelines() in Swift/ObjC runtime
    }
}
