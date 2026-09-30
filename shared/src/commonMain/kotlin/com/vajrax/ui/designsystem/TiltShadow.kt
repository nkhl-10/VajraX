package com.vajrax.ui.designsystem

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import com.vajrax.platform.DeviceTilt

/**
 * Soft card shadow lit from above that slides as the phone moves, as if the light stayed put.
 * The tilt is read at draw time, so sensor updates redraw the shadow without recomposing.
 */
fun Modifier.tiltShadow(shape: Shape, tilt: DeviceTilt, elevation: Dp, dark: Boolean): Modifier =
    dropShadow(shape) {
        val e = elevation.toPx()
        radius = e * 1.4f
        // Resting light from above; a full tilt moves the shadow about its own elevation twice over.
        offset = Offset(tilt.x * e * 1.8f, e * 0.55f + tilt.y * e * 1.8f)
        color = if (dark) Color.Black else Color(0xFF1E1B4B)
        alpha = if (dark) 0.6f else 0.2f
    }
