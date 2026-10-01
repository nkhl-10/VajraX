package com.vajrax.ui.features.today

import androidx.compose.ui.graphics.vector.ImageVector
import com.vajrax.domain.habit.CheckInAction
import com.vajrax.ui.designsystem.VxHaptic
import com.vajrax.ui.designsystem.VxIcons

/** Icon for the check-in button of a habit. */
internal val CheckInAction.icon: ImageVector
    get() = when (this) {
        CheckInAction.Done -> VxIcons.Check
        CheckInAction.PlusOne -> VxIcons.Plus
        CheckInAction.StartTimer -> VxIcons.Play
        CheckInAction.LogValue -> VxIcons.Pencil
    }

/** Done confirms; +1 ticks; starting a timer or opening the value sheet is a plain tap. */
internal val CheckInAction.haptic: VxHaptic
    get() = when (this) {
        CheckInAction.Done -> VxHaptic.Confirm
        CheckInAction.PlusOne -> VxHaptic.Tick
        CheckInAction.StartTimer, CheckInAction.LogValue -> VxHaptic.Tap
    }
