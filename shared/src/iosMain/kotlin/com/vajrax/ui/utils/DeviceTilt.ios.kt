package com.vajrax.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


@Composable
actual fun rememberDeviceTilt(): State<DeviceTilt> {
    // For iOS, we return a dummy state.
    // CoreMotion integration can be added later if needed.
    return remember { mutableStateOf(DeviceTilt(0f, 0f)) }
}


@Composable
actual fun Modifier.gyroShadowCard(
    elevation: Dp,
    cornerRadius: Dp
): Modifier = composed {
    // Basic fallback for iOS - no dynamic shadow offset yet
    this.graphicsLayer {
        this.shadowElevation = elevation.toPx()
        this.shape = androidx.compose.foundation.shape.RoundedCornerShape(cornerRadius)
    }
}
