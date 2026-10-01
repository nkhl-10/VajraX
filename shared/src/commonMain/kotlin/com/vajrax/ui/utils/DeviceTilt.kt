package com.vajrax.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class DeviceTilt(val pitch: Float, val roll: Float)

val LocalDeviceTilt = compositionLocalOf { DeviceTilt(0f, 0f) }

@Composable
expect fun rememberDeviceTilt(): State<DeviceTilt>

/**
 * A custom modifier that applies a heavy, elegant box-shadow (approximating 0px 5px 50px -5px #000000)
 * and uses gyro sensor data to slightly tilt the card on the X and Y axes, creating a 3D parallax effect.
 */
@Composable
expect fun Modifier.gyroShadowCard(
    elevation: Dp = 24.dp,
    cornerRadius: Dp = 24.dp
): Modifier

