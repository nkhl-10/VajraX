package com.vajrax.android

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.provider.Settings
import com.vajrax.platform.DeviceTilt
import kotlin.math.abs
import kotlin.math.exp

/**
 * Feeds [DeviceTilt] from the gyroscope while the app is in front. Rotation speed is integrated
 * into an angle that decays back to zero, so shadows follow a movement and then settle, whatever
 * angle the phone is normally held at. Off when there's no gyroscope or animations are disabled.
 */
class TiltSensor(private val context: Context, private val tilt: DeviceTilt) : SensorEventListener {
    private val manager = context.getSystemService(SensorManager::class.java)
    private val gyroscope: Sensor? = manager?.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    private var lastNanos = 0L
    private var roll = 0f
    private var pitch = 0f

    fun start() {
        if (gyroscope == null || animationsOff()) return
        lastNanos = 0L
        manager?.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_GAME)
    }

    fun stop() {
        manager?.unregisterListener(this)
        roll = 0f
        pitch = 0f
        tilt.x = 0f
        tilt.y = 0f
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (lastNanos == 0L) {
            lastNanos = event.timestamp
            return
        }
        val dt = ((event.timestamp - lastNanos) / 1_000_000_000f).coerceIn(0f, 0.1f)
        lastNanos = event.timestamp
        val settle = exp(-dt / SETTLE_SECONDS)
        // values[0] = speed around the screen's x axis (tipping), values[1] = around y (turning).
        roll = ((roll + event.values[1] * dt) * settle).coerceIn(-MAX_ANGLE, MAX_ANGLE)
        pitch = ((pitch + event.values[0] * dt) * settle).coerceIn(-MAX_ANGLE, MAX_ANGLE)
        val x = roll / MAX_ANGLE
        val y = pitch / MAX_ANGLE
        // Skip sub-pixel changes so a phone lying still doesn't keep redrawing.
        if (abs(x - tilt.x) > 0.01f || abs(y - tilt.y) > 0.01f) {
            tilt.x = x
            tilt.y = y
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    private fun animationsOff(): Boolean =
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f

    private companion object {
        /** About 12 degrees of movement moves a shadow its full distance. */
        const val MAX_ANGLE = 0.21f
        const val SETTLE_SECONDS = 2.5f
    }
}
