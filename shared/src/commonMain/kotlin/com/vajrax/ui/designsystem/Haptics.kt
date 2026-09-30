package com.vajrax.ui.designsystem

import androidx.compose.foundation.clickable
import androidx.compose.foundation.selection.selectable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

/**
 * One touch-feedback vocabulary for the whole app, so every control feels the same. Android
 * plays these only when the system "touch feedback" setting is on.
 */
enum class VxHaptic {
    /** Buttons, rows, icon buttons. */
    Tap,
    /** Picking a tab, chip, segment or option. */
    Select,
    /** A habit or switch turned on / off. */
    ToggleOn,
    ToggleOff,
    /** A habit completed from a button. */
    Confirm,
    /** Confirming something destructive (delete, wipe). */
    Reject,
    /** Each step while turning the dial. */
    Tick,
    /** A drag has started. */
    DragStart;

    internal val type: HapticFeedbackType
        get() = when (this) {
            Tap -> HapticFeedbackType.VirtualKey
            Select -> HapticFeedbackType.SegmentTick
            ToggleOn -> HapticFeedbackType.ToggleOn
            ToggleOff -> HapticFeedbackType.ToggleOff
            Confirm -> HapticFeedbackType.Confirm
            Reject -> HapticFeedbackType.Reject
            // Not SegmentFrequentTick: phones with a basic vibration motor (no effect primitives,
            // e.g. moto g57) drop it silently, while SegmentTick falls back to a short click.
            Tick -> HapticFeedbackType.SegmentTick
            DragStart -> HapticFeedbackType.GestureThresholdActivate
        }
}

/** Returns a function that plays one [VxHaptic]. */
@Composable
fun rememberHaptics(): (VxHaptic) -> Unit {
    val feedback = LocalHapticFeedback.current
    return remember(feedback) { { kind -> feedback.performHapticFeedback(kind.type) } }
}

/** [onClick] preceded by [kind] feedback. */
@Composable
fun hapticClick(kind: VxHaptic = VxHaptic.Tap, onClick: () -> Unit): () -> Unit {
    val haptics = rememberHaptics()
    return { haptics(kind); onClick() }
}

/** [clickable] with [kind] feedback. */
fun Modifier.hapticClickable(
    kind: VxHaptic = VxHaptic.Tap,
    role: Role? = Role.Button,
    onClickLabel: String? = null,
    onClick: () -> Unit
): Modifier = composed {
    val haptics = rememberHaptics()
    clickable(role = role, onClickLabel = onClickLabel) { haptics(kind); onClick() }
}

/** [selectable] with a selection tick when picking something new. */
fun Modifier.hapticSelectable(selected: Boolean, role: Role? = null, onClick: () -> Unit): Modifier = composed {
    val haptics = rememberHaptics()
    selectable(selected = selected, role = role) { if (!selected) haptics(VxHaptic.Select); onClick() }
}
