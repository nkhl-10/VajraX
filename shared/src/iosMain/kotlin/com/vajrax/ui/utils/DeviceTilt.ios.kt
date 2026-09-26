package com.vajrax.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

@Composable
actual fun rememberDeviceTilt(): State<DeviceTilt> {
    // For iOS, we return a dummy state.
    // CoreMotion integration can be added later if needed.
    return remember { mutableStateOf(DeviceTilt(0f, 0f)) }
}
