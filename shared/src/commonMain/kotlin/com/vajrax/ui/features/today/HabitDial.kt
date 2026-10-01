package com.vajrax.ui.features.today

import org.jetbrains.compose.resources.stringResource
import com.vajrax.resources.*
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.vajrax.domain.habit.CheckInAction
import com.vajrax.ui.designsystem.PillAction
import com.vajrax.ui.designsystem.VxHaptic
import com.vajrax.ui.designsystem.hapticClickable
import com.vajrax.ui.designsystem.rememberHaptics
import com.vajrax.ui.designsystem.VxIcons
import com.vajrax.ui.theme.LuminaTheme
import com.vajrax.ui.theme.accentOnContainer
import com.vajrax.ui.theme.habitAccent
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/** Angle between neighbouring habits on the dial, in degrees; tighter when there are many. */
private fun stepFor(count: Int): Float = when {
    count > 16 -> 24f
    count > 10 -> 28f
    else -> 36f
}

/**
 * Today's habits on a half-circle dial, like the finger wheel of an old rotary telephone.
 * Habits sit in holes along the top arc in time order; the one under the finger stop at
 * 12 o'clock is selected and its details and actions show in the hub below it.
 *
 * Drag sideways to turn the dial (it clicks at each habit and settles on the nearest one), or tap
 * a hole to turn it there. Tapping the selected hole, or the hub's main button, checks it in.
 * When the current habit changes (for example after Done) the dial turns to it by itself.
 */
@Composable
fun HabitDial(
    items: List<TodayItem>,
    nowId: String?,
    onPrimary: (TodayItem) -> Unit,
    onUndo: (TodayItem) -> Unit,
    onSnooze: (TodayItem) -> Unit,
    onSkip: (TodayItem) -> Unit,
    onOpen: (TodayItem) -> Unit,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) return
    val colors = LuminaTheme.colors
    val density = LocalDensity.current
    val haptics = rememberHaptics()
    val scope = rememberCoroutineScope()

    fun indexOf(id: String?) = items.indexOfFirst { it.id == id }.takeIf { it >= 0 }
    val startIndex = indexOf(nowId) ?: items.indexOfFirst { it.occurrence.isOpen }.coerceAtLeast(0)

    var rotation by remember { mutableFloatStateOf(startIndex.toFloat()) }
    var selectedId by remember { mutableStateOf(items[startIndex].id) }
    var dragging by remember { mutableStateOf(false) }
    var settleJob by remember { mutableStateOf<Job?>(null) }

    fun settleTo(index: Int) {
        val target = index.coerceIn(0, items.lastIndex)
        selectedId = items[target].id
        settleJob?.cancel()
        settleJob = scope.launch {
            animate(rotation, target.toFloat(), animationSpec = spring(dampingRatio = 0.72f, stiffness = 260f)) { v, _ -> rotation = v }
        }
    }

    // The current habit moved on (Done, Skip, a new minute): turn the dial to it.
    LaunchedEffect(nowId) { indexOf(nowId)?.let { settleTo(it) } }
    // Habits were added, removed or re-timed: keep the same habit selected.
    val ids = items.map { it.id }
    LaunchedEffect(ids) {
        if (!dragging) {
            val i = indexOf(selectedId) ?: rotation.roundToInt().coerceIn(0, items.lastIndex)
            settleTo(i)
        }
    }
    // A click for every habit passed while turning by hand.
    val tickIndex by remember { derivedStateOf { rotation.roundToInt() } }
    LaunchedEffect(tickIndex) { if (dragging) haptics(VxHaptic.Tick) }

    val hubIndex = tickIndex.coerceIn(0, items.lastIndex)
    val hubItem = items[hubIndex]

    BoxWithConstraints(
        modifier.fillMaxWidth().semantics {
            contentDescription = "Habit dial, ${items.size} habits. ${hubItem.habit.title} is selected."
            // TalkBack can't drag the dial, so it gets the same moves as actions.
            customActions = buildList {
                if (hubIndex < items.lastIndex) add(CustomAccessibilityAction("Next habit") { settleTo(hubIndex + 1); true })
                if (hubIndex > 0) add(CustomAccessibilityAction("Previous habit") { settleTo(hubIndex - 1); true })
                if (hubItem.occurrence.isOpen) add(CustomAccessibilityAction("Check in ${hubItem.habit.title}") { onPrimary(hubItem); true })
                else add(CustomAccessibilityAction("Undo ${hubItem.habit.title}") { onUndo(hubItem); true })
            }
        }
    ) {
        val hole = 56.dp
        val topRoom = 34.dp
        val radius = maxWidth / 2 - hole * 0.62f - 6.dp
        val centreY = topRoom + hole * 0.62f + radius
        val hubWidth = (radius - hole * 0.62f - 10.dp) * 2
        val radiusPx = with(density) { radius.toPx() }
        val centreXPx = with(density) { maxWidth.toPx() } / 2
        // The hub fits inside the ring, clear of the enlarged top hole.
        val hubTop = minOf(104.dp, radius - hole * 0.75f)
        val step = stepFor(items.size)
        val stepRad = step * PI.toFloat() / 180f
        val pxPerHabit = radiusPx * stepRad

        Box(
            Modifier.fillMaxWidth().height(centreY + 76.dp)
                .pointerInput(items.size, pxPerHabit) {
                    val velocity = VelocityTracker()
                    detectHorizontalDragGestures(
                        onDragStart = {
                            settleJob?.cancel()
                            haptics(VxHaptic.DragStart)
                            dragging = true
                            velocity.resetTracking()
                        },
                        onDragEnd = {
                            dragging = false
                            val fling = velocity.calculateVelocity().x / pxPerHabit * 0.18f
                            settleTo((rotation - fling).roundToInt())
                        },
                        onDragCancel = {
                            dragging = false
                            settleTo(rotation.roundToInt())
                        }
                    ) { change, dx ->
                        change.consume()
                        velocity.addPosition(change.uptimeMillis, change.position)
                        // Finger right turns the dial clockwise, bringing earlier habits to the top.
                        rotation = (rotation - dx / pxPerHabit).coerceIn(-0.45f, items.lastIndex + 0.45f)
                    }
                }
        ) {
            DialPlate(radius = radius, centreY = centreY, band = hole + 18.dp)

            items.forEachIndexed { index, item ->
                DialHole(
                    item = item,
                    isNow = item.id == nowId,
                    size = hole,
                    radius = radius,
                    centreXPx = centreXPx,
                    centreY = centreY,
                    angle = { (index - rotation) * step },
                    step = step,
                    onClick = {
                        if (index == hubIndex && !dragging) {
                            if (item.occurrence.isOpen) {
                                haptics(CheckInAction.of(item.habit).haptic)
                                onPrimary(item)
                            } else {
                                haptics(VxHaptic.ToggleOff)
                                onUndo(item)
                            }
                        } else {
                            haptics(VxHaptic.Select)
                            settleTo(index)
                        }
                    }
                )
            }

            // Hub: the habit under the finger stop.
            Column(
                Modifier.width(hubWidth).align(Alignment.TopCenter)
                    .offset(y = centreY - hubTop)
                    .height(hubTop - 6.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .hapticClickable(onClickLabel = "Details") { onOpen(hubItem) },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(
                    stringResource(Res.string.today_value_fmt_2, hubLabel(hubItem, nowId), hubItem.timeLabel),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = colors.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    hubItem.habit.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                val detail = hubItem.progressLabel ?: when {
                    hubItem.occurrence.isDone -> "Done"
                    hubItem.occurrence.isSkipped -> "Skipped"
                    else -> null
                }
                if (detail != null) {
                    Text(detail, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant, maxLines = 1)
                }
            }

            // Actions for the selected habit, just below the dial's centre line.
            Row(
                Modifier.align(Alignment.TopCenter).offset(y = centreY + 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (hubItem.occurrence.isOpen) {
                    PillAction(stringResource(Res.string.today_snooze), VxIcons.Alarm, { onSnooze(hubItem) })
                    val action = CheckInAction.of(hubItem.habit)
                    PillAction(action.label, action.icon, { onPrimary(hubItem) }, filled = true, haptic = action.haptic)
                    PillAction(stringResource(Res.string.today_skip), VxIcons.SkipForward, { onSkip(hubItem) })
                } else {
                    PillAction(stringResource(Res.string.today_undo), VxIcons.Undo, { onUndo(hubItem) }, haptic = VxHaptic.ToggleOff)
                }
            }
        }
    }
}

private fun hubLabel(item: TodayItem, nowId: String?): String = when {
    item.occurrence.isDone -> "DONE"
    item.occurrence.isSkipped -> "SKIPPED"
    item.id == nowId -> "NOW"
    item.phase == TodayPhase.OPEN_EARLIER -> "STILL OPEN"
    item.phase == TodayPhase.WEEKLY_MET -> "WEEK DONE"
    else -> "NEXT"
}

/** The dial's plate: a half ring the holes sit in, and the finger stop at 12 o'clock. */
@Composable
private fun DialPlate(radius: androidx.compose.ui.unit.Dp, centreY: androidx.compose.ui.unit.Dp, band: androidx.compose.ui.unit.Dp) {
    val colors = LuminaTheme.colors
    val plate = if (colors.isDark) colors.surfaceContainerHigh else colors.surfaceContainer
    val rim = colors.outlineVariant
    val stop = colors.primary
    Canvas(Modifier.fillMaxSize()) {
        val r = radius.toPx()
        val cy = centreY.toPx()
        val cx = size.width / 2
        val bandPx = band.toPx()
        val topLeft = Offset(cx - r, cy - r)
        val arcSize = Size(r * 2, r * 2)
        // Flat ends on the centre line keep the band clear of the buttons below it.
        drawArc(plate, 180f, 180f, useCenter = false, topLeft = topLeft, size = arcSize, style = Stroke(bandPx, cap = StrokeCap.Butt))
        // Hairline rims inside and outside the band.
        for (edge in listOf(r - bandPx / 2, r + bandPx / 2)) {
            drawArc(
                rim, 180f, 180f, useCenter = false,
                topLeft = Offset(cx - edge, cy - edge), size = Size(edge * 2, edge * 2),
                style = Stroke(1.dp.toPx())
            )
        }
        // Finger stop: a small pointer just above the selected hole.
        val tipY = cy - r - bandPx / 2 - 2.dp.toPx()
        val w = 7.dp.toPx()
        val path = Path().apply {
            moveTo(cx - w, tipY - w * 1.3f)
            lineTo(cx + w, tipY - w * 1.3f)
            lineTo(cx, tipY)
            close()
        }
        drawPath(path, stop)
    }
}

@Composable
private fun DialHole(
    item: TodayItem,
    isNow: Boolean,
    size: androidx.compose.ui.unit.Dp,
    radius: androidx.compose.ui.unit.Dp,
    centreXPx: Float,
    centreY: androidx.compose.ui.unit.Dp,
    angle: () -> Float,
    step: Float,
    onClick: () -> Unit
) {
    val colors = LuminaTheme.colors
    val density = LocalDensity.current
    val accent = habitAccent(item.habit.color, colors.isDark)
    val done = item.occurrence.isDone
    val skipped = item.occurrence.isSkipped
    val sizePx = with(density) { size.toPx() }
    val onScreen by remember { derivedStateOf { abs(angle()) < 95f } }
    val radiusPx = with(density) { radius.toPx() }
    val centreYPx = with(density) { centreY.toPx() }
    val labelGapPx = with(density) { (size * 0.62f + 12.dp).toPx() }

    fun position(extra: Float): Offset {
        val rad = angle() * PI.toFloat() / 180f
        val r = radiusPx + extra
        return Offset(centreXPx + r * sin(rad), centreYPx - r * cos(rad))
    }
    run {
        fun edgeAlpha(): Float = ((100f - abs(angle())) / 30f).coerceIn(0f, 1f)

        // Time label outside the ring; the selected habit shows its time in the hub instead.
        Text(
            item.timeLabel,
            style = MaterialTheme.typography.labelSmall,
            color = colors.onSurfaceVariant,
            maxLines = 1,
            modifier = Modifier
                .offset {
                    val p = position(labelGapPx)
                    IntOffset((p.x - 28.dp.toPx()).roundToInt(), (p.y - 8.dp.toPx()).roundToInt())
                }
                .width(56.dp)
                .graphicsLayer {
                    // Only the neighbours of the selected habit show a time: the selected one has
                    // it in the hub, and labels further round would run off the screen edge.
                    val a = abs(angle())
                    val rise = (a / (step * 0.7f)).coerceIn(0f, 1f)
                    val fall = ((step * 1.5f - a) / (step * 0.4f)).coerceIn(0f, 1f)
                    alpha = rise * fall
                },
            textAlign = TextAlign.Center
        )

        Box(
            Modifier
                .offset {
                    val p = position(0f)
                    IntOffset((p.x - sizePx / 2).roundToInt(), (p.y - sizePx / 2).roundToInt())
                }
                .size(size)
                .graphicsLayer {
                    val closeness = 1f - (abs(angle()) / step).coerceIn(0f, 1f)
                    val s = 1f + 0.2f * closeness - 0.1f * (abs(angle()) / 90f).coerceIn(0f, 1f)
                    scaleX = s
                    scaleY = s
                    alpha = edgeAlpha()
                }
                .clip(CircleShape)
                .background(
                    when {
                        done -> colors.primary
                        isNow -> colors.primaryContainer
                        else -> colors.surface
                    }
                )
                .border(
                    width = if (isNow && !done) 2.dp else 1.dp,
                    color = when {
                        done -> colors.primary
                        isNow -> colors.primary
                        else -> colors.outlineVariant
                    },
                    shape = CircleShape
                )
                // Holes turned out of sight are skipped by TalkBack (and can't be tapped).
                .then(if (onScreen) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier.clearAndSetSemantics { })
                .semantics {
                    contentDescription = buildString {
                        append(item.habit.title).append(", ").append(item.timeLabel).append(", ")
                        append(if (done) "done" else if (skipped) "skipped" else "not done")
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                if (done) VxIcons.Check else VxIcons.forKey(item.habit.icon),
                contentDescription = null,
                tint = when {
                    done -> colors.onPrimary
                    skipped -> colors.onSurfaceVariant.copy(alpha = 0.5f)
                    isNow -> colors.accentOnContainer
                    else -> accent
                },
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
