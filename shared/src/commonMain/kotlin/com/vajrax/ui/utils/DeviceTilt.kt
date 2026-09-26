package com.vajrax.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
fun Modifier.gyroShadowCard(
    elevation: Dp = 24.dp
): Modifier = composed {
    val tilt = LocalDeviceTilt.current
    
    // Scale the tilt (which is roughly -1.5 to 1.5 radians) to a subtle degree rotation
    // We reverse pitch and roll directions for natural "look around" parallax feeling
    val maxRotation = 8f
    val rotationX = (tilt.pitch * -15f).coerceIn(-maxRotation, maxRotation)
    val rotationY = (tilt.roll * 15f).coerceIn(-maxRotation, maxRotation)

    this.graphicsLayer {
        this.rotationX = rotationX
        this.rotationY = rotationY
        this.shadowElevation = elevation.toPx()
        this.spotShadowColor = Color.Black
        this.ambientShadowColor = Color.Black
        this.cameraDistance = 8 * density
        this.shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
        this.clip = false // let the card itself clip if needed, or shadow will clip contents if true. Actually, shadow requires a shape.
    }
}
