package com.vajrax.android.widget

import android.content.Context
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.Action
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
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
import com.vajrax.core.coroutines.runCatchingCancellable
import com.vajrax.domain.habit.CheckInAction
import com.vajrax.domain.habit.Habit
import com.vajrax.domain.habit.HabitType
import com.vajrax.domain.habit.Occurrence
import com.vajrax.domain.habit.Tracker
import com.vajrax.domain.habit.TrackerStatus
import com.vajrax.domain.model.ActionStatus
import com.vajrax.domain.usecase.RoutineManager
import com.vajrax.platform.WidgetController
import com.vajrax.platform.WidgetTimelineSnapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import org.koin.core.context.GlobalContext

/*
 * Home-screen widgets built with Jetpack Glance (spec 11).
 *  - "VAJRAX · Now": the current habit with Done / Snooze and what comes next.
 *  - "VAJRAX · Today": responsive. Short = the Now card; taller = the current habit as a card,
 *    then the rest still to do, then what's done (tap a circle to tick or undo).
 * Both read live from the app database and write through RoutineManager, the same path as the
 * app and reminder notifications. The current habit is picked by the shared NowPicker, so after
 * Done the widget moves to the same next habit the app shows.
 */

// ---------------------------------------------------------------- colours (light / dark)

/** Widget palette from res/values(-night)/colors.xml, the same place as the widget drawables. */
private object WColors {
    val onSurface = androidx.glance.unit.ColorProvider(R.color.vx_on_surface)
    val onSurfaceVariant = androidx.glance.unit.ColorProvider(R.color.vx_on_surface_variant)
    val primary = androidx.glance.unit.ColorProvider(R.color.vx_widget_accent)
    // White keeps 4.5:1 on the indigo button in both themes.
    val onPrimary = androidx.glance.unit.ColorProvider(R.color.vx_on_primary)
    val onPrimaryContainer = androidx.glance.unit.ColorProvider(R.color.vx_widget_on_container)
    val track = androidx.glance.unit.ColorProvider(R.color.vx_track)
}

// ---------------------------------------------------------------- actions

/** Done / undo, +1 and snooze for one occurrence, executed without opening the app. */
class HabitWidgetAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val occurrenceId = parameters[OCCURRENCE] ?: return
        val routine = GlobalContext.getOrNull()?.get<RoutineManager>() ?: return
        runCatchingCancellable {
            when (parameters[OP]) {
                OP_INCREMENT -> routine.increment(occurrenceId)
                OP_SNOOZE -> routine.snooze(occurrenceId, com.vajrax.domain.BusinessRules.SNOOZE_MINUTES)
                OP_DONE -> routine.complete(occurrenceId)
                else -> routine.toggle(occurrenceId)
            }
        }.onFailure { com.vajrax.core.log.VxLog.w("Widget", "Widget action failed", it) }
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

private val openApp: Action get() = actionStartActivity<MainActivity>()

private val OPEN_OCCURRENCE = ActionParameters.Key<String>(MainActivity.EXTRA_OPEN_OCCURRENCE)

/** Opens the app on Home with this occurrence's timer or value entry. */
private fun openHabit(occurrenceId: String): Action =
    actionStartActivity<MainActivity>(actionParametersOf(OPEN_OCCURRENCE to occurrenceId))

// ---------------------------------------------------------------- widgets & receivers

class TodayGlanceWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Responsive(
        setOf(DpSize(180.dp, 110.dp), DpSize(250.dp, 110.dp), DpSize(250.dp, 200.dp), DpSize(300.dp, 320.dp))
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val data = WidgetData.observe()
        provideContent {
            val today by data.collectAsState(initial = WidgetToday.Empty)
            WidgetFrame { if (LocalSize.current.height < 170.dp) NowContent(today) else ListContent(today) }
        }
    }

    /** Picker preview (Android 15+): sample habits, never the user's own. */
    override suspend fun providePreview(context: Context, widgetCategory: Int) {
        provideContent { WidgetFrame { ListContent(WidgetPreview.sample) } }
    }
}

class NowGlanceWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Responsive(setOf(DpSize(180.dp, 110.dp), DpSize(250.dp, 110.dp), DpSize(250.dp, 160.dp)))

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val data = WidgetData.observe()
        provideContent {
            val today by data.collectAsState(initial = WidgetToday.Empty)
            WidgetFrame { NowContent(today) }
        }
    }

    override suspend fun providePreview(context: Context, widgetCategory: Int) {
        provideContent { WidgetFrame { NowContent(WidgetPreview.sample) } }
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
        runCatchingCancellable { TodayGlanceWidget().updateAll(context) }.onFailure { com.vajrax.core.log.VxLog.w("Widget", "Today widget refresh failed", it) }
        runCatchingCancellable { NowGlanceWidget().updateAll(context) }.onFailure { com.vajrax.core.log.VxLog.w("Widget", "Now widget refresh failed", it) }
    }

    /**
     * Publishes the generated picker previews (Android 15+). The system rate-limits this, so it
     * runs once per app version.
     */
    suspend fun publishPreviews(context: Context, alreadyPublished: Boolean): Boolean {
        if (Build.VERSION.SDK_INT < 35 || alreadyPublished) return alreadyPublished
        val manager = GlanceAppWidgetManager(context)
        val results = listOf(
            runCatchingCancellable { manager.setWidgetPreviews(TodayWidgetReceiver::class) }.getOrNull(),
            runCatchingCancellable { manager.setWidgetPreviews(NowWidgetReceiver::class) }.getOrNull()
        )
        return results.all { it == GlanceAppWidgetManager.SET_WIDGET_PREVIEWS_RESULT_SUCCESS }
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

/** Example routine for the widget picker preview. */
private object WidgetPreview {
    private val day = LocalDate(2026, 1, 5)

    private fun row(id: String, title: String, time: String, status: ActionStatus, minutes: Int = 10) = Habit(
        id = id, trackerId = "preview", title = title, category = "Morning", time = time, durationMinutes = minutes
    ) to Occurrence("occ_$id", id, day, time, status, completedAt = if (status == ActionStatus.COMPLETE) "2026-01-05T06:40:00Z" else null)

    val sample: WidgetToday by lazy {
        val pairs = listOf(
            row("wake", "Wake up", "06:30", ActionStatus.COMPLETE, 5),
            row("water", "Drink water", "06:35", ActionStatus.PENDING, 5),
            row("walk", "Morning walk", "06:45", ActionStatus.PENDING, 30),
            row("read", "Read 10 pages", "21:00", ActionStatus.PENDING, 20)
        )
        WidgetData.build(
            Tracker("preview", "preview", "Morning routine", day, 30, true, TrackerStatus.ACTIVE, null),
            pairs.map { it.first }, pairs.map { it.second }, hideNames = false, now = 6 * 60 + 36, day = day, nowIso = "2026-01-05T06:41:00Z"
        )
    }
}

// ---------------------------------------------------------------- content

@Composable
private fun WidgetFrame(content: @Composable () -> Unit) {
    Box(
        modifier = GlanceModifier.fillMaxSize()
            .appWidgetBackground()
            .background(ImageProvider(R.drawable.widget_bg))
            // Matches the launcher's widget corners on Android 12+.
            .cornerRadius(android.R.dimen.system_app_widget_background_radius)
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) { content() }
}

@Composable
private fun Header(today: WidgetToday) {
    Row(
        modifier = GlanceModifier.fillMaxWidth().clickable(openApp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            (today.trackerName ?: LocalContext.current.getString(R.string.brand_wordmark)).uppercase().take(26),
            style = TextStyle(color = WColors.primary, fontSize = 11.sp, fontWeight = FontWeight.Bold),
            maxLines = 1,
            modifier = GlanceModifier.defaultWeight()
        )
        if (today.total > 0) {
            Text(
                LocalContext.current.getString(R.string.widget_count, today.done, today.total),
                style = TextStyle(color = WColors.onSurface, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            )
        }
    }
    Spacer(GlanceModifier.height(6.dp))
    LinearProgressIndicator(
        progress = today.fraction,
        modifier = GlanceModifier.fillMaxWidth().height(4.dp).cornerRadius(2.dp),
        color = WColors.primary,
        backgroundColor = WColors.track
    )
}

@Composable
private fun EmptyMessage(today: WidgetToday) {
    val context = LocalContext.current
    val (title, body) = when {
        !today.hasTracker -> context.getString(R.string.widget_empty_choose_title) to context.getString(R.string.widget_empty_choose_body)
        today.startsLater -> context.getString(R.string.widget_empty_later_title) to context.getString(R.string.widget_empty_later_body)
        today.rows.isEmpty() -> context.getString(R.string.widget_empty_rest_title) to context.getString(R.string.widget_empty_rest_body)
        else -> context.getString(R.string.widget_empty_complete_title) to
            context.getString(R.string.widget_empty_complete_body, today.done, today.total)
    }
    val finished = today.hasTracker && !today.startsLater && today.rows.isNotEmpty()
    Row(modifier = GlanceModifier.fillMaxWidth().clickable(openApp), verticalAlignment = Alignment.CenterVertically) {
        if (finished) {
            Image(
                provider = ImageProvider(R.drawable.ic_widget_check_done),
                contentDescription = null,
                modifier = GlanceModifier.size(28.dp)
            )
            Spacer(GlanceModifier.width(10.dp))
        }
        Column {
            Text(title, style = TextStyle(color = WColors.onSurface, fontSize = 16.sp, fontWeight = FontWeight.Bold))
            Text(body, style = TextStyle(color = WColors.onSurfaceVariant, fontSize = 12.sp))
        }
    }
}

/** "NOW · 6:35 AM" above the habit name. */
@Composable
private fun PhaseLabel(today: WidgetToday, row: WidgetRow) {
    Text(
        LocalContext.current.let { "${today.label(it, row).uppercase()} · ${row.time(it)}" },
        style = TextStyle(color = WColors.primary, fontSize = 11.sp, fontWeight = FontWeight.Bold),
        maxLines = 1
    )
}

@Composable
private fun ActionButtons(row: WidgetRow, title: String) {
    val action = CheckInAction.of(row.habit)
    val context = LocalContext.current
    Row(modifier = GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Chip(
            text = context.getString(R.string.widget_snooze),
            filled = false,
            onClick = habitAction(row.id, HabitWidgetAction.OP_SNOOZE),
            description = com.vajrax.domain.BusinessRules.SNOOZE_MINUTES.let {
                context.resources.getQuantityString(R.plurals.widget_snooze_description, it, title, it)
            },
            modifier = GlanceModifier.defaultWeight()
        )
        Spacer(GlanceModifier.width(8.dp))
        // Timed and measured habits need the app (a focus session, a value), so the widget opens
        // it on that habit rather than logging the full target as if it were done.
        Chip(
            text = context.getString(
                when (action) {
                    CheckInAction.Done -> R.string.widget_action_done
                    CheckInAction.PlusOne -> R.string.widget_action_plus_one
                    CheckInAction.StartTimer -> R.string.widget_action_start
                    CheckInAction.LogValue -> R.string.widget_action_log
                }
            ),
            filled = true,
            icon = if (action == CheckInAction.Done) R.drawable.ic_action_check else null,
            onClick = when (action) {
                CheckInAction.Done -> habitAction(row.id, HabitWidgetAction.OP_DONE)
                CheckInAction.PlusOne -> habitAction(row.id, HabitWidgetAction.OP_INCREMENT)
                CheckInAction.StartTimer, CheckInAction.LogValue -> openHabit(row.id)
            },
            description = context.getString(
                when (action) {
                    CheckInAction.Done -> R.string.widget_mark_done_description
                    CheckInAction.PlusOne -> R.string.widget_plus_one_description
                    CheckInAction.StartTimer -> R.string.widget_start_description
                    CheckInAction.LogValue -> R.string.widget_log_description
                },
                title
            ),
            modifier = GlanceModifier.defaultWeight()
        )
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
        val context = LocalContext.current
        val title = row.title(context, today.hideNames)
        val roomy = LocalSize.current.height >= 150.dp
        Column(modifier = GlanceModifier.fillMaxWidth().clickable(openApp)) {
            PhaseLabel(today, row)
            Text(
                title,
                style = TextStyle(color = WColors.onSurface, fontSize = 16.sp, fontWeight = FontWeight.Bold),
                maxLines = if (roomy) 2 else 1
            )
            if (row.habit.type != HabitType.BOOLEAN) {
                Text(row.subtitle(LocalContext.current), style = TextStyle(color = WColors.onSurfaceVariant, fontSize = 12.sp), maxLines = 1)
            }
        }
        Spacer(GlanceModifier.defaultWeight())
        ActionButtons(row, title)
        val next = today.next
        if (roomy && next != null) {
            Spacer(GlanceModifier.height(8.dp))
            Text(
                context.getString(R.string.widget_then, next.title(context, today.hideNames), next.time(context)),
                style = TextStyle(color = WColors.onSurfaceVariant, fontSize = 12.sp),
                maxLines = 1,
                modifier = GlanceModifier.clickable(openApp)
            )
        }
    }
}

@Composable
private fun ListContent(today: WidgetToday) {
    Column(modifier = GlanceModifier.fillMaxSize()) {
        Header(today)
        Spacer(GlanceModifier.height(10.dp))
        val current = today.current
        if (current == null && today.rows.isEmpty()) {
            EmptyMessage(today)
            return@Column
        }
        if (current == null) {
            EmptyMessage(today)
            Spacer(GlanceModifier.height(8.dp))
        }
        val rest = today.ordered.filter { it != current }
        val firstResolved = rest.firstOrNull { !it.occurrence.isOpen }?.id
        LazyColumn(modifier = GlanceModifier.fillMaxWidth().defaultWeight()) {
            if (current != null) {
                item(itemId = 1L) { CurrentCard(today, current) }
            }
            items(rest, itemId = { it.id.hashCode().toLong() }) { row ->
                Column(modifier = GlanceModifier.fillMaxWidth()) {
                    if (row.id == firstResolved && current != null) SectionLabel(LocalContext.current.getString(R.string.widget_section_done))
                    HabitListRow(row, today.hideNames)
                }
            }
        }
    }
}

@Composable
private fun CurrentCard(today: WidgetToday, row: WidgetRow) {
    val context = LocalContext.current
    val title = row.title(context, today.hideNames)
    Column(modifier = GlanceModifier.fillMaxWidth().padding(bottom = 6.dp)) {
        Column(
            modifier = GlanceModifier.fillMaxWidth()
                .background(ImageProvider(R.drawable.widget_row_now))
                .cornerRadius(14.dp)
                .padding(12.dp)
        ) {
            Column(modifier = GlanceModifier.fillMaxWidth().clickable(openApp)) {
                PhaseLabel(today, row)
                Text(
                    title,
                    style = TextStyle(color = WColors.onSurface, fontSize = 15.sp, fontWeight = FontWeight.Bold),
                    maxLines = 2
                )
                if (row.habit.type != HabitType.BOOLEAN) {
                    Text(row.subtitle(LocalContext.current), style = TextStyle(color = WColors.onSurfaceVariant, fontSize = 12.sp), maxLines = 1)
                }
            }
            Spacer(GlanceModifier.height(10.dp))
            ActionButtons(row, title)
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text.uppercase(),
        style = TextStyle(color = WColors.onSurfaceVariant, fontSize = 11.sp, fontWeight = FontWeight.Bold),
        modifier = GlanceModifier.padding(start = 4.dp, top = 8.dp, bottom = 2.dp)
    )
}

@Composable
private fun HabitListRow(row: WidgetRow, hideNames: Boolean) {
    val occ = row.occurrence
    val context = LocalContext.current
    val title = row.title(context, hideNames)
    Row(modifier = GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        // The circle is the only tap that changes data; the rest of the row opens the app,
        // so a stray tap never ticks a habit off.
        Image(
            provider = ImageProvider(
                when {
                    occ.isDone -> R.drawable.ic_widget_check_done
                    occ.isSkipped -> R.drawable.ic_widget_check_skipped
                    else -> R.drawable.ic_widget_check_open
                }
            ),
            contentDescription = context.getString(
                if (occ.isDone || occ.isSkipped) R.string.widget_undo_description else R.string.widget_mark_done_description,
                title
            ),
            modifier = GlanceModifier.size(44.dp).padding(10.dp).clickable(habitAction(row.id, HabitWidgetAction.OP_TOGGLE))
        )
        Column(modifier = GlanceModifier.defaultWeight().clickable(openApp)) {
            Text(
                title,
                style = TextStyle(
                    color = if (occ.isOpen) WColors.onSurface else WColors.onSurfaceVariant,
                    fontSize = 14.sp,
                    fontWeight = if (occ.isOpen) FontWeight.Bold else FontWeight.Medium
                ),
                maxLines = 1
            )
            Text(row.subtitle(LocalContext.current), style = TextStyle(color = WColors.onSurfaceVariant, fontSize = 12.sp), maxLines = 1)
        }
        if (occ.isOpen && CheckInAction.of(row.habit) == CheckInAction.PlusOne) {
            Chip("+1", filled = false, onClick = habitAction(row.id, HabitWidgetAction.OP_INCREMENT), description = context.getString(R.string.widget_plus_one_description, title))
        }
    }
}

@Composable
private fun Chip(
    text: String,
    filled: Boolean,
    onClick: Action,
    description: String,
    modifier: GlanceModifier = GlanceModifier,
    icon: Int? = null
) {
    val content = if (filled) WColors.onPrimary else WColors.onPrimaryContainer
    Box(
        modifier = modifier.height(40.dp)
            .background(ImageProvider(if (filled) R.drawable.widget_btn_primary else R.drawable.widget_btn_secondary))
            .cornerRadius(20.dp)
            .padding(horizontal = 12.dp)
            .clickable(onClick)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Image(
                    provider = ImageProvider(icon),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(content),
                    modifier = GlanceModifier.size(16.dp)
                )
                Spacer(GlanceModifier.width(6.dp))
            }
            Text(text, style = TextStyle(color = content, fontSize = 13.sp, fontWeight = FontWeight.Bold), maxLines = 1)
        }
    }
}
