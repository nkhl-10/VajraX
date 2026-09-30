package com.vajrax.platform

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * How far the phone has just been turned, from the gyroscope, in -1..1 on each axis
 * ([x] = turned left/right, [y] = tipped towards/away). It eases back to 0 when the phone is
 * still, so card shadows drift with a movement and then settle. Stays 0 where there is no
 * gyroscope or animations are switched off.
 */
@Stable
class DeviceTilt {
    var x by mutableFloatStateOf(0f)
    var y by mutableFloatStateOf(0f)
}

val LocalDeviceTilt = staticCompositionLocalOf { DeviceTilt() }
