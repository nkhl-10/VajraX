package com.vajrax.android.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.semantics.contentDescription
import androidx.glance.semantics.semantics
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.vajrax.android.MainActivity
import com.vajrax.android.R
import com.vajrax.domain.habit.HabitType
import com.vajrax.domain.usecase.RoutineManager
import com.vajrax.platform.WidgetController
import com.vajrax.platform.WidgetTimelineSnapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.context.GlobalContext

/*
 * Home-screen widgets built with Jetpack Glance (spec 11).
 *  - "VAJRAX · Today": responsive. Small = current habit with Done / Snooze; taller = every habit
 *    of today with a one-tap check (tap again to undo), +1 for count habits, Later on the current one.
 *  - "VAJRAX · Now": compact current-habit card.
 * Both read live from the app database and write through RoutineManager — the same path as
 * the app and reminder notifications, so completing from the home screen needs no app launch.
 */

// ---------------------------------------------------------------- colours (light / dark)

private object WColors {
    val onSurface = ColorProvider(day = Color(0xFF111827), night = Color(0xFFF9FAFB))
    val onSurfaceVariant = ColorProvider(day = Color(0xFF6B7280), night = Color(0xFF94A3B8))
    val primary = ColorProvider(day = Color(0xFF4F46E5), night = Color(0xFF818CF8))
    val onPrimary = ColorProvider(day = Color(0xFFFFFFFF), night = Color(0xFF0F172A))
    val onPrimaryContainer = ColorProvider(day = Color(0xFF4338CA), night = Color(0xFFE0E7FF))
    val track = ColorProvider(day = Color(0xFFE5E7EB), night = Color(0xFF262B38))
}

// ---------------------------------------------------------------- actions

/** Done / undo, +1 and snooze for one occurrence, executed without opening the app. */
class HabitWidgetAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val occurrenceId = parameters[OCCURRENCE] ?: return
        val routine = GlobalContext.getOrNull()?.get<RoutineManager>() ?: return
        runCatching {
            when (parameters[OP]) {
                OP_INCREMENT -> routine.increment(occurrenceId)
                OP_SNOOZE -> routine.snooze(occurrenceId, 15)
                OP_DONE -> routine.complete(occurrenceId)
                else -> routine.toggle(occurrenceId)
            }
        }
        VajraWidgets.updateAll(context)
    }

    companion object {
        val OCCURRENCE = ActionParameters.Key<String>("occurrence_id")
        val OP = ActionParameters.Key<String>("op")
        const val OP_TOGGLE = "toggle"
        const val OP_DONE = "done"
        const val OP_INCREMENT = "increment"
        const val OP_SNOOZE = "snooze"

        fun params(occurrenceId: String, op: String) = actionParametersOf(OCCURRENCE to occurrenceId, OP to op)
    }
}

private fun habitAction(occurrenceId: String, op: String) =
    actionRunCallback<HabitWidgetAction>(HabitWidgetAction.params(occurrenceId, op))

// ---------------------------------------------------------------- widgets & receivers

class TodayGlanceWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Responsive(
        setOf(DpSize(180.dp, 110.dp), DpSize(250.dp, 110.dp), DpSize(250.dp, 200.dp), DpSize(300.dp, 320.dp))
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val data = WidgetData.observe()
        provideContent {
            val today by data.collectAsState(initial = WidgetToday.Empty)
            WidgetFrame {
                if (LocalSize.current.height < 170.dp) NowContent(today) else ListContent(today)
            }
        }
    }
}

class NowGlanceWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Single

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val data = WidgetData.observe()
        provideContent {
            val today by data.collectAsState(initial = WidgetToday.Empty)
            WidgetFrame { NowContent(today) }
        }
    }
}

class TodayWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TodayGlanceWidget()
}

class NowWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = NowGlanceWidget()
}

object VajraWidgets {
    suspend fun updateAll(context: Context) {
        runCatching { TodayGlanceWidget().updateAll(context) }
        runCatching { NowGlanceWidget().updateAll(context) }
    }
}

/** [WidgetController] for the shared code: every data change refreshes the Glance widgets. */
class GlanceWidgetController(private val context: Context) : WidgetController {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    override fun updateWidget(snapshot: WidgetTimelineSnapshot) = refresh()
    override fun refresh() {
        scope.launch { VajraWidgets.updateAll(context) }
    }
}

// ---------------------------------------------------------------- content

@Composable
private fun WidgetFrame(content: @Composable () -> Unit) {
    Box(
        modifier = GlanceModifier.fillMaxSize()
            .background(ImageProvider(R.drawable.widget_bg))
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) { content() }
}

@Composable
private fun Header(today: WidgetToday) {
    Row(
        modifier = GlanceModifier.fillMaxWidth().clickable(actionStartActivity<MainActivity>()),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            (today.trackerName ?: "VAJRAX").uppercase().take(26),
            style = TextStyle(color = WColors.primary, fontSize = 11.sp, fontWeight = FontWeight.Bold),
            maxLines = 1,
            modifier = GlanceModifier.defaultWeight()
        )
        if (today.total > 0) {
            Text(
                "${today.done}/${today.total}",
                style = TextStyle(color = WColors.onSurface, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            )
        }
    }
    Spacer(GlanceModifier.height(6.dp))
    LinearProgressIndicator(
        progress = today.fraction,
        modifier = GlanceModifier.fillMaxWidth().height(6.dp),
        color = WColors.primary,
        backgroundColor = WColors.track
    )
}

@Composable
private fun EmptyMessage(today: WidgetToday) {
    val (title, body) = when {
        !today.hasTracker -> "Choose a routine" to "Tap to pick a template"
        today.total == 0 && today.rows.isEmpty() -> "Rest day" to "Nothing scheduled today"
        else -> "All done ✓" to "Every habit completed today"
    }
    Column(modifier = GlanceModifier.fillMaxWidth().clickable(actionStartActivity<MainActivity>())) {
        Text(title, style = TextStyle(color = WColors.onSurface, fontSize = 16.sp, fontWeight = FontWeight.Bold))
        Text(body, style = TextStyle(color = WColors.onSurfaceVariant, fontSize = 12.sp))
    }
}

@Composable
private fun NowContent(today: WidgetToday) {
    Column(modifier = GlanceModifier.fillMaxSize()) {
        Header(today)
        Spacer(GlanceModifier.height(10.dp))
        val row = today.current
        if (row == null) {
            EmptyMessage(today)
            return@Column
        }
        val started = today.nowMinute >= (com.vajrax.core.time.TimeFormat.toMinutes(row.occurrence.scheduledTime ?: row.habit.time) ?: 0)
        Text(
            if (started) "NOW" else "NEXT",
            style = TextStyle(color = WColors.onSurfaceVariant, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        )
        Text(
            row.title(today.hideNames),
            style = TextStyle(color = WColors.onSurface, fontSize = 16.sp, fontWeight = FontWeight.Bold),
            maxLines = 1
        )
        Text(row.subtitle(), style = TextStyle(color = WColors.onSurfaceVariant, fontSize = 12.sp), maxLines = 1)
        Spacer(GlanceModifier.defaultWeight())
        Row(modifier = GlanceModifier.fillMaxWidth()) {
            val counts = row.habit.type == HabitType.COUNT
            Chip(
                text = "Snooze",
                filled = false,
                onClick = habitAction(row.id, HabitWidgetAction.OP_SNOOZE),
                description = "Snooze ${row.title(today.hideNames)} 15 minutes",
                modifier = GlanceModifier.defaultWeight()
            )
            Spacer(GlanceModifier.width(8.dp))
            Chip(
                text = if (counts) "+1" else "✓ Done",
                filled = true,
                onClick = habitAction(row.id, if (counts) HabitWidgetAction.OP_INCREMENT else HabitWidgetAction.OP_DONE),
                description = if (counts) "Add one to ${row.title(today.hideNames)}" else "Mark ${row.title(today.hideNames)} done",
                modifier = GlanceModifier.defaultWeight()
            )
        }
    }
}

@Composable
private fun ListContent(today: WidgetToday) {
    Column(modifier = GlanceModifier.fillMaxSize()) {
        Header(today)
        Spacer(GlanceModifier.height(8.dp))
        if (today.rows.isEmpty()) {
            EmptyMessage(today)
            return@Column
        }
        LazyColumn(modifier = GlanceModifier.fillMaxWidth().defaultWeight()) {
            items(today.rows, itemId = { it.id.hashCode().toLong() }) { row ->
                HabitListRow(row, today.hideNames)
            }
        }
    }
}

@Composable
private fun HabitListRow(row: WidgetRow, hideNames: Boolean) {
    val occ = row.occurrence
    val title = row.title(hideNames)
    val toggle = habitAction(row.id, HabitWidgetAction.OP_TOGGLE)
    Column(modifier = GlanceModifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Row(
            modifier = GlanceModifier.fillMaxWidth()
                .then(if (row.isCurrent) GlanceModifier.background(ImageProvider(R.drawable.widget_row_now)) else GlanceModifier)
                .padding(end = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                provider = ImageProvider(
                    when {
                        occ.isDone -> R.drawable.ic_widget_check_done
                        occ.isSkipped -> R.drawable.ic_widget_check_skipped
                        else -> R.drawable.ic_widget_check_open
                    }
                ),
                contentDescription = if (occ.isDone || occ.isSkipped) "Undo $title" else "Mark $title done",
                modifier = GlanceModifier.size(44.dp).padding(8.dp).clickable(toggle)
            )
            Column(modifier = GlanceModifier.defaultWeight().clickable(toggle)) {
                Text(
                    title,
                    style = TextStyle(
                        color = if (occ.isOpen) WColors.onSurface else WColors.onSurfaceVariant,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1
                )
                Text(row.subtitle(), style = TextStyle(color = WColors.onSurfaceVariant, fontSize = 12.sp), maxLines = 1)
            }
            when {
                occ.isOpen && row.habit.type == HabitType.COUNT ->
                    Chip("+1", filled = false, onClick = habitAction(row.id, HabitWidgetAction.OP_INCREMENT), description = "Add one to $title")
                occ.isOpen && row.isCurrent ->
                    Chip("Later", filled = false, onClick = habitAction(row.id, HabitWidgetAction.OP_SNOOZE), description = "Snooze $title 15 minutes")
                else -> Unit
            }
        }
    }
}

@Composable
private fun Chip(
    text: String,
    filled: Boolean,
    onClick: androidx.glance.action.Action,
    description: String,
    modifier: GlanceModifier = GlanceModifier
) {
    Box(
        modifier = modifier.height(36.dp)
            .background(ImageProvider(if (filled) R.drawable.widget_btn_primary else R.drawable.widget_btn_secondary))
            .padding(horizontal = 12.dp)
            .clickable(onClick)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            style = TextStyle(
                color = if (filled) WColors.onPrimary else WColors.onPrimaryContainer,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            ),
            maxLines = 1
        )
    }
}
