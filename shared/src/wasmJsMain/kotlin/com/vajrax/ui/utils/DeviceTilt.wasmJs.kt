package com.vajrax.ui.utils

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp

/** Browsers on desktops have no motion sensor, so cards keep a still shadow. */
@Composable
actual fun rememberDeviceTilt(): State<DeviceTilt> = remember { mutableStateOf(DeviceTilt(0f, 0f)) }

@Composable
actual fun Modifier.gyroShadowCard(
    elevation: Dp,
    cornerRadius: Dp
): Modifier = composed {
    graphicsLayer {
        shadowElevation = elevation.toPx()
        shape = RoundedCornerShape(cornerRadius)
    }
}
