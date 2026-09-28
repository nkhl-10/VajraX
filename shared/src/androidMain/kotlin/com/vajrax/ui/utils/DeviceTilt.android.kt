package com.vajrax.ui.utils

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


@Composable
actual fun rememberDeviceTilt(): State<DeviceTilt> {
    val context = LocalContext.current
    val tiltState = remember { mutableStateOf(DeviceTilt(0f, 0f)) }

    DisposableEffect(context) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.values?.let { values ->
                    val rotationMatrix = FloatArray(9)
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, values)
                    val orientationAngles = FloatArray(3)
                    SensorManager.getOrientation(rotationMatrix, orientationAngles)
                    
                    // orientationAngles[1] is pitch (x-axis), orientationAngles[2] is roll (y-axis)
                    val pitch = orientationAngles[1]
                    val roll = orientationAngles[2]
                    
                    tiltState.value = DeviceTilt(
                        pitch = pitch.coerceIn(-1.5f, 1.5f), 
                        roll = roll.coerceIn(-1.5f, 1.5f)
                    )
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        if (rotationSensor != null) {
            sensorManager.registerListener(listener, rotationSensor, SensorManager.SENSOR_DELAY_UI)
        }

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    return tiltState
}


@Composable
actual fun Modifier.gyroShadowCard(
    elevation: Dp,
    cornerRadius: Dp
): Modifier = composed {
    val tilt = LocalDeviceTilt.current
    val density = LocalDensity.current.density
    
    // Scale tilt to dynamic shadow offset
    val maxOffsetPx = 25f * density
    val offsetX = (tilt.roll * 25f * density).coerceIn(-maxOffsetPx, maxOffsetPx)
    val offsetY = (tilt.pitch * -25f * density).coerceIn(-maxOffsetPx, maxOffsetPx) + (5f * density)

    val shadowColor = Color(0, 0, 0, 80).toArgb()
    val transparentColor = Color.Transparent.toArgb()
    val blurRadius = 50f * density
    val cornerRadiusPx = cornerRadius.value * density

    this.drawBehind {
        this.drawIntoCanvas { canvas ->
            val paint = Paint()
            val frameworkPaint = paint.asFrameworkPaint()
            frameworkPaint.color = transparentColor
            frameworkPaint.setShadowLayer(
                blurRadius,
                offsetX,
                offsetY,
                shadowColor
            )
            canvas.drawRoundRect(
                0f,
                0f,
                this.size.width,
                this.size.height,
                cornerRadiusPx,
                cornerRadiusPx,
                paint
            )
        }
    }
}
