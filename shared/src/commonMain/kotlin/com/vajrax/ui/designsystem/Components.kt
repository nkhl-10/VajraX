package com.vajrax.ui.designsystem

import org.jetbrains.compose.resources.stringResource
import com.vajrax.resources.*
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.vajrax.core.time.TimeFormat
import com.vajrax.ui.theme.LuminaTheme
import com.vajrax.ui.theme.VxShape
import com.vajrax.ui.theme.VxSpace
import com.vajrax.ui.theme.accentOnContainer

// ---------------------------------------------------------------- surfaces

@Composable
fun VxCard(
    modifier: Modifier = Modifier,
    elevated: Boolean = false,
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(VxSpace.lg),
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = LuminaTheme.colors
    val shape = VxShape.card
    val tilt = com.vajrax.platform.LocalDeviceTilt.current
    val haptics = rememberHaptics()
    Column(
        modifier = modifier
            // Every card casts a shadow that follows the phone's movement; raised cards cast more.
            .tiltShadow(shape, tilt, elevation = if (elevated) 16.dp else 9.dp, dark = colors.isDark)
            .clip(shape)
            .background(colors.surface)
            .border(1.dp, colors.outlineVariant.copy(alpha = if (elevated) 0.5f else 0.8f), shape)
            .then(if (onClick != null) Modifier.clickable { haptics(VxHaptic.Tap); onClick() } else Modifier)
            .padding(contentPadding),
        content = content
    )
}

@Composable
fun ScreenTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.displaySmall,
        color = LuminaTheme.colors.onSurface,
        modifier = modifier.semantics { heading() }
    )
}

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier, trailing: (@Composable () -> Unit)? = null) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = LuminaTheme.colors.onSurfaceVariant,
            modifier = Modifier.weight(1f).semantics { heading() }
        )
        trailing?.invoke()
    }
}

@Composable
fun IconBadge(
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    background: Color = tint.copy(alpha = 0.12f),
    iconSize: Dp = 20.dp
) {
    Box(
        modifier = modifier.size(size).clip(RoundedCornerShape(size * 0.28f)).background(background),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(iconSize))
    }
}

// ---------------------------------------------------------------- progress

@Composable
fun ProgressRing(
    fraction: Float,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    stroke: Dp = 6.dp,
    color: Color = LuminaTheme.colors.primary,
    trackColor: Color = LuminaTheme.colors.surfaceContainerHigh,
    content: @Composable BoxScope.() -> Unit = {}
) {
    val animated by animateFloatAsState(fraction.coerceIn(0f, 1f), tween(450), label = "ring")
    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val s = stroke.toPx()
            val arcSize = Size(this.size.width - s, this.size.height - s)
            val topLeft = Offset(s / 2, s / 2)
            drawArc(trackColor, 0f, 360f, false, topLeft, arcSize, style = Stroke(s))
            if (animated > 0f) {
                drawArc(color, -90f, 360f * animated, false, topLeft, arcSize, style = Stroke(s, cap = StrokeCap.Round))
            }
        }
        content()
    }
}

@Composable
fun LinearBar(
    fraction: Float,
    modifier: Modifier = Modifier,
    height: Dp = 8.dp,
    color: Color = LuminaTheme.colors.primary,
    trackColor: Color = LuminaTheme.colors.surfaceContainerHigh
) {
    val animated by animateFloatAsState(fraction.coerceIn(0f, 1f), tween(450), label = "bar")
    Box(modifier = modifier.fillMaxWidth().height(height).clip(VxShape.pill).background(trackColor)) {
        Box(modifier = Modifier.fillMaxWidth(animated).fillMaxHeight().clip(VxShape.pill).background(color))
    }
}

@Composable
fun StatPod(title: String, value: String, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null) {
    val colors = LuminaTheme.colors
    val haptics = rememberHaptics()
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(VxShape.medium)
            .background(colors.background)
            .border(1.dp, colors.outlineVariant.copy(alpha = 0.8f), VxShape.medium)
            .then(if (onClick != null) Modifier.clickable { haptics(VxHaptic.Tap); onClick() } else Modifier)
            .padding(vertical = VxSpace.md)
            .semantics(mergeDescendants = true) {}
    ) {
        Text(title, style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant)
        Spacer(Modifier.height(VxSpace.xs))
        Text(value, style = MaterialTheme.typography.titleLarge, color = colors.onSurface)
    }
}

@Composable
fun DeltaChip(delta: Int?, modifier: Modifier = Modifier) {
    if (delta == null) return
    val colors = LuminaTheme.colors
    val up = delta >= 0
    val bg = if (up) colors.successContainer else colors.warningContainer
    val fg = if (up) colors.onSuccessContainer else colors.onWarningContainer
    Row(
        modifier = modifier.clip(VxShape.pill).background(bg).padding(horizontal = 10.dp, vertical = 4.dp)
            .semantics(mergeDescendants = true) { contentDescription = "${if (up) "Up" else "Down"} ${kotlin.math.abs(delta)} percent" },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(if (up) VxIcons.TrendingUp else VxIcons.TrendingDown, null, tint = fg, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(4.dp))
        Text(stringResource(Res.string.common_value_fmt_2, kotlin.math.abs(delta)), style = MaterialTheme.typography.labelMedium, color = fg)
    }
}

// ---------------------------------------------------------------- check-in controls

/** [MISSED] is a past day that wasn't done; [DISABLED] is a day that can't be checked (future). */
enum class CheckState { OPEN, DONE, DONE_MUTED, SKIPPED, MISSED, DISABLED, ACTIVE }

/**
 * Round completion control with a 48dp touch target and a textual state for screen readers
 * (spec 05: never rely on color alone).
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CheckCircle(
    state: CheckState,
    onClick: (() -> Unit)?,
    label: String,
    modifier: Modifier = Modifier,
    size: Dp = 28.dp,
    touchWidth: Dp = 48.dp,
    onLongClick: (() -> Unit)? = null
) {
    val colors = LuminaTheme.colors
    val fill by animateColorAsState(
        when (state) {
            CheckState.DONE, CheckState.ACTIVE -> colors.primary
            CheckState.OPEN -> colors.surfaceContainerHigh.copy(alpha = if (colors.isDark) 1f else 0.9f)
            else -> Color.Transparent
        },
        tween(200), label = "check"
    )
    val stateText = when (state) {
        CheckState.DONE, CheckState.DONE_MUTED -> "Done"
        CheckState.SKIPPED -> "Skipped"
        CheckState.MISSED -> "Not done"
        CheckState.DISABLED -> "Not available"
        CheckState.ACTIVE -> "Current, not done"
        CheckState.OPEN -> "Not done"
    }
    val interactive = onClick != null && state != CheckState.DISABLED
    val haptics = rememberHaptics()
    val checked = state == CheckState.DONE || state == CheckState.DONE_MUTED
    val feel: () -> Unit = { haptics(if (checked || state == CheckState.SKIPPED) VxHaptic.ToggleOff else VxHaptic.ToggleOn) }
    Box(
        modifier = modifier
            .width(touchWidth)
            .height(48.dp)
            .then(
                if (onLongClick != null) Modifier.combinedClickable(
                    onClick = { if (onClick != null) { feel(); onClick() } },
                    onLongClick = { haptics(VxHaptic.Tap); onLongClick() },
                    onLongClickLabel = "Correct this record",
                    role = Role.Button
                )
                else if (interactive) Modifier.toggleable(
                    value = checked,
                    role = Role.Checkbox,
                    onValueChange = { feel(); onClick() }
                ) else Modifier
            )
            .semantics {
                contentDescription = label
                stateDescription = stateText
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(fill)
                .then(
                    when (state) {
                        CheckState.DONE_MUTED -> Modifier.border(1.4.dp, colors.outline.copy(alpha = 0.7f), CircleShape)
                        CheckState.SKIPPED, CheckState.DISABLED -> Modifier.border(1.4.dp, colors.outlineVariant, CircleShape)
                        CheckState.MISSED -> Modifier.border(1.4.dp, colors.outline, CircleShape)
                        else -> Modifier
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            when (state) {
                CheckState.DONE -> Icon(VxIcons.Check, null, tint = colors.onPrimary, modifier = Modifier.size(size * 0.55f))
                CheckState.DONE_MUTED -> Icon(VxIcons.Check, null, tint = colors.outline, modifier = Modifier.size(size * 0.5f))
                CheckState.SKIPPED -> Icon(VxIcons.SkipForward, null, tint = colors.outline, modifier = Modifier.size(size * 0.45f))
                CheckState.ACTIVE -> Box(Modifier.size(size * 0.36f).clip(CircleShape).background(colors.onPrimary))
                // A mark as well as a colour, so missed and upcoming never look the same.
                CheckState.MISSED -> Icon(VxIcons.Minus, null, tint = colors.outline, modifier = Modifier.size(size * 0.45f))
                else -> Unit
            }
        }
    }
}

// ---------------------------------------------------------------- buttons

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    icon: ImageVector? = null,
    haptic: VxHaptic = VxHaptic.Tap
) {
    val haptics = rememberHaptics()
    Button(
        onClick = { haptics(haptic); onClick() },
        enabled = enabled && !loading,
        modifier = modifier.heightIn(min = 52.dp),
        shape = VxShape.control,
        colors = ButtonDefaults.buttonColors(containerColor = LuminaTheme.colors.primary, contentColor = LuminaTheme.colors.onPrimary)
    ) {
        if (loading) {
            CircularProgressIndicator(Modifier.size(18.dp), color = LuminaTheme.colors.onPrimary, strokeWidth = 2.dp)
        } else {
            if (icon != null) {
                Icon(icon, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(VxSpace.sm))
            }
            Text(text, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    destructive: Boolean = false
) {
    val colors = LuminaTheme.colors
    val haptics = rememberHaptics()
    val tint = if (destructive) colors.statusError else colors.primary
    OutlinedButton(
        onClick = { haptics(VxHaptic.Tap); onClick() },
        enabled = enabled,
        modifier = modifier.heightIn(min = 52.dp),
        shape = VxShape.control,
        border = BorderStroke(1.5.dp, if (enabled) tint.copy(alpha = 0.85f) else colors.outlineVariant),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = tint)
    ) {
        if (icon != null) {
            Icon(icon, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(VxSpace.sm))
        }
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

/** Small pill action used on the NOW card ("Mark done", "Notes"). */
@Composable
fun PillAction(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    filled: Boolean = false,
    haptic: VxHaptic = VxHaptic.Tap
) {
    val colors = LuminaTheme.colors
    val haptics = rememberHaptics()
    Row(
        modifier = modifier
            .minimumInteractiveComponentSize().heightIn(min = 40.dp)
            .clip(VxShape.pill)
            .background(if (filled) colors.primary else colors.surface)
            .border(1.dp, if (filled) colors.primary else colors.primary.copy(alpha = 0.25f), VxShape.pill)
            .clickable(role = Role.Button) { haptics(haptic); onClick() }
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = if (filled) colors.onPrimary else colors.primary, modifier = Modifier.size(15.dp))
        Spacer(Modifier.width(6.dp))
        Text(text, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = if (filled) colors.onPrimary else colors.primary)
    }
}

/** Round icon button with a 48dp target (header search, back, etc.). */
@Composable
fun RoundIconButton(icon: ImageVector, contentDescription: String, onClick: () -> Unit, modifier: Modifier = Modifier, tint: Color? = null) {
    val colors = LuminaTheme.colors
    val haptics = rememberHaptics()
    Box(
        modifier = modifier.size(48.dp).clip(CircleShape).clickable(role = Role.Button) { haptics(VxHaptic.Tap); onClick() }
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center
    ) {
        Box(Modifier.size(40.dp).clip(CircleShape).background(colors.surfaceDim), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = tint ?: colors.onSurface, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun UsePill(text: String = "Use", onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = LuminaTheme.colors
    val haptics = rememberHaptics()
    Box(
        modifier = modifier.minimumInteractiveComponentSize().heightIn(min = 40.dp).clip(VxShape.pill).background(colors.primary)
            .clickable(role = Role.Button) { haptics(VxHaptic.Tap); onClick() }.padding(horizontal = 22.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge, color = colors.onPrimary)
    }
}

// ---------------------------------------------------------------- selection

@Composable
fun SegmentedToggle(options: List<String>, selectedIndex: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    val colors = LuminaTheme.colors
    val haptics = rememberHaptics()
    Row(
        modifier = modifier.clip(VxShape.pill).background(colors.surfaceDim).padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        options.forEachIndexed { index, option ->
            val selected = index == selectedIndex
            Box(
                modifier = Modifier
                    .minimumInteractiveComponentSize().heightIn(min = 40.dp)
                    .clip(VxShape.pill)
                    .background(if (selected) colors.surface else Color.Transparent)
                    .selectable(selected = selected, role = Role.Tab, onClick = { if (!selected) haptics(VxHaptic.Select); onSelect(index) })
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    option,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium),
                    color = if (selected) colors.onSurface else colors.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun CategoryChip(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = LuminaTheme.colors
    val haptics = rememberHaptics()
    Box(
        modifier = modifier
            .minimumInteractiveComponentSize().heightIn(min = 40.dp)
            .clip(VxShape.pill)
            .background(if (selected) colors.primaryContainer else colors.surface)
            .border(if (selected) 1.5.dp else 1.dp, if (selected) colors.primary else colors.outlineVariant, VxShape.pill)
            .selectable(selected = selected, role = Role.Checkbox, onClick = { haptics(VxHaptic.Select); onClick() })
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium),
            color = if (selected) colors.onPrimaryContainer else colors.onSurface
        )
    }
}

// ---------------------------------------------------------------- inputs

@Composable
fun VxTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    helper: String? = null,
    error: String? = null,
    maxChars: Int? = null,
    singleLine: Boolean = true,
    minLines: Int = 1,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    val colors = LuminaTheme.colors
    Column(modifier = modifier) {
        Text(label, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium), color = colors.onSurface)
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = { v -> onValueChange(if (maxChars != null) v.take(maxChars) else v) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { if (placeholder.isNotEmpty()) Text(placeholder, color = colors.textTertiary) },
            singleLine = singleLine,
            minLines = minLines,
            isError = error != null,
            shape = VxShape.control,
            textStyle = MaterialTheme.typography.bodyLarge,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colors.primary,
                unfocusedBorderColor = colors.outlineVariant,
                focusedContainerColor = colors.surface,
                unfocusedContainerColor = colors.surface
            )
        )
        val bottom = error ?: helper
        if (bottom != null || maxChars != null) {
            Spacer(Modifier.height(4.dp))
            Row(Modifier.fillMaxWidth()) {
                Text(
                    bottom ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (error != null) colors.statusError else colors.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                if (maxChars != null) {
                    Text(stringResource(Res.string.common_value_fmt, value.length, maxChars), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                }
            }
        }
    }
}

/** Row showing a time value that opens the time picker. */
@Composable
fun TimeField(label: String, time: String?, onPick: () -> Unit, modifier: Modifier = Modifier, placeholder: String = "Anytime") {
    val colors = LuminaTheme.colors
    val haptics = rememberHaptics()
    Column(modifier) {
        Text(label, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium), color = colors.onSurface)
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp).clip(VxShape.control)
                .border(1.dp, colors.outlineVariant, VxShape.control).background(colors.surface)
                .clickable(role = Role.Button) { haptics(VxHaptic.Tap); onPick() }.padding(horizontal = VxSpace.lg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(VxIcons.Clock, null, tint = colors.onSurfaceVariant, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(VxSpace.md))
            Text(
                if (time == null) placeholder else TimeFormat.display(time),
                style = MaterialTheme.typography.bodyLarge,
                color = if (time == null) colors.onSurfaceVariant else colors.onSurface,
                modifier = Modifier.weight(1f)
            )
            Icon(VxIcons.ChevronDown, null, tint = colors.onSurfaceVariant, modifier = Modifier.size(18.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(initial: String?, onDismiss: () -> Unit, onConfirm: (String) -> Unit, title: String = "Pick a time") {
    val start = TimeFormat.toMinutes(initial) ?: (9 * 60)
    val state = rememberTimePickerState(initialHour = start / 60, initialMinute = start % 60, is24Hour = com.vajrax.core.time.TimeFormat.use24Hour)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, style = MaterialTheme.typography.titleLarge) },
        text = { Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { TimePicker(state = state) } },
        confirmButton = {
            TextButton(onClick = { onConfirm(TimeFormat.fromMinutes(state.hour * 60 + state.minute)) }) { Text(stringResource(Res.string.common_set)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(Res.string.common_cancel)) } },
        containerColor = LuminaTheme.colors.surface
    )
}

@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    destructive: Boolean = false,
    dismissLabel: String = "Cancel"
) {
    val colors = LuminaTheme.colors
    val haptics = rememberHaptics()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, style = MaterialTheme.typography.titleLarge) },
        text = { Text(message, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant) },
        confirmButton = {
            TextButton(onClick = { haptics(if (destructive) VxHaptic.Reject else VxHaptic.Confirm); onConfirm() }) {
                Text(confirmLabel, color = if (destructive) colors.statusError else colors.primary, style = MaterialTheme.typography.labelLarge)
            }
        },
        dismissButton = { TextButton(onClick = { haptics(VxHaptic.Tap); onDismiss() }) { Text(dismissLabel) } },
        containerColor = colors.surface
    )
}

// ---------------------------------------------------------------- states

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    val colors = LuminaTheme.colors
    Column(
        modifier = modifier.fillMaxWidth().padding(vertical = VxSpace.xxxl, horizontal = VxSpace.xxl),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconBadge(icon, colors.primary, size = 56.dp, iconSize = 26.dp)
        Spacer(Modifier.height(VxSpace.lg))
        Text(title, style = MaterialTheme.typography.titleMedium, color = colors.onSurface, textAlign = TextAlign.Center)
        Spacer(Modifier.height(VxSpace.xs))
        Text(message, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant, textAlign = TextAlign.Center)
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(VxSpace.lg))
            PrimaryButton(actionLabel, onAction)
        }
    }
}

@Composable
fun ErrorState(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    val colors = LuminaTheme.colors
    Column(modifier.fillMaxWidth().padding(VxSpace.xxl), horizontalAlignment = Alignment.CenterHorizontally) {
        IconBadge(VxIcons.Info, colors.statusError, size = 56.dp, iconSize = 26.dp)
        Spacer(Modifier.height(VxSpace.lg))
        Text(stringResource(Res.string.common_something_went_wrong), style = MaterialTheme.typography.titleMedium, color = colors.onSurface)
        Spacer(Modifier.height(VxSpace.xs))
        Text(message, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant, textAlign = TextAlign.Center)
        Spacer(Modifier.height(VxSpace.lg))
        SecondaryButton(stringResource(Res.string.common_try_again), onRetry)
    }
}

@Composable
fun LoadingSkeleton(modifier: Modifier = Modifier, rows: Int = 5) {
    val colors = LuminaTheme.colors
    val transition = rememberInfiniteTransition(label = "skeleton")
    val alpha by transition.animateFloat(0.45f, 0.9f, infiniteRepeatable(tween(800), RepeatMode.Reverse), label = "a")
    Column(modifier.fillMaxWidth().semantics { contentDescription = "Loading" }, verticalArrangement = Arrangement.spacedBy(VxSpace.md)) {
        Box(Modifier.fillMaxWidth().height(150.dp).clip(VxShape.card).alpha(alpha).background(colors.surfaceContainerHigh))
        repeat(rows) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(28.dp).clip(CircleShape).alpha(alpha).background(colors.surfaceContainerHigh))
                Spacer(Modifier.width(VxSpace.md))
                Box(Modifier.weight(1f).height(16.dp).clip(VxShape.pill).alpha(alpha).background(colors.surfaceContainerHigh))
            }
        }
    }
}

// ---------------------------------------------------------------- top bar

@Composable
fun VxTopBar(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    trailing: (@Composable RowScope.() -> Unit)? = null
) {
    val colors = LuminaTheme.colors
    val haptics = rememberHaptics()
    Row(
        modifier = modifier.fillMaxWidth().background(colors.surface).statusBarsPadding()
            .padding(horizontal = VxSpace.md, vertical = VxSpace.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(48.dp).clip(VxShape.control).clickable(role = Role.Button) { haptics(VxHaptic.Tap); onBack() }
                .semantics { contentDescription = "Back" },
            contentAlignment = Alignment.Center
        ) {
            Box(Modifier.size(40.dp).clip(VxShape.tile).background(colors.surfaceDim), contentAlignment = Alignment.Center) {
                Icon(VxIcons.ArrowLeft, null, tint = colors.onSurface, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(Modifier.width(VxSpace.sm))
        Text(
            title,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
            color = colors.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f).semantics { heading() }
        )
        trailing?.invoke(this)
    }
}

/** "Preview"-style tonal action for top bars. */
@Composable
fun TonalAction(text: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = LuminaTheme.colors
    val haptics = rememberHaptics()
    Row(
        modifier = modifier.minimumInteractiveComponentSize().heightIn(min = 40.dp).clip(VxShape.pill).background(colors.primaryContainer)
            .clickable(role = Role.Button) { haptics(VxHaptic.Tap); onClick() }.padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = colors.accentOnContainer, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text(text, style = MaterialTheme.typography.labelLarge, color = colors.accentOnContainer)
    }
}

/** A settings-style row: label, optional value, chevron. */
@Composable
fun ListRow(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    value: String? = null,
    icon: ImageVector? = null,
    titleColor: Color? = null,
    showChevron: Boolean = true
) {
    val colors = LuminaTheme.colors
    val haptics = rememberHaptics()
    Row(
        modifier = modifier.fillMaxWidth().heightIn(min = 56.dp).clickable(role = Role.Button) { haptics(VxHaptic.Tap); onClick() }
            .padding(horizontal = VxSpace.xl),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(icon, null, tint = titleColor ?: colors.onSurfaceVariant, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(VxSpace.md))
        }
        Text(title, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium), color = titleColor ?: colors.onSurface, modifier = Modifier.weight(1f))
        if (value != null) {
            Text(value, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
            Spacer(Modifier.width(VxSpace.sm))
        }
        if (showChevron) Icon(VxIcons.ChevronRight, null, tint = colors.onSurfaceVariant, modifier = Modifier.size(18.dp))
    }
}

@Composable
fun RowDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(modifier = modifier.padding(horizontal = VxSpace.xl), color = LuminaTheme.colors.outlineVariant.copy(alpha = 0.7f), thickness = 1.dp)
}
