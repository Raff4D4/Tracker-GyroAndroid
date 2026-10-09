package com.example.tracker_gyro.service

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.accessibility.AccessibilityEvent
import kotlin.math.sqrt

object ForegroundGate {
    var isActive: Boolean = false
}

class SystemGestureAccessibilityService : AccessibilityService(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private lateinit var powerManager: PowerManager
    private var vibrator: Vibrator? = null

    private var proximitySensor: Sensor? = null
    private var accelerometer: Sensor? = null

    private var lastProximityTime = 0L
    private var waveCount = 0
    private val handler = Handler(Looper.getMainLooper())
    private val waveTimeoutRunnable = Runnable { waveCount = 0 }

    private var lastAccel: FloatArray? = null
    private var shakeCooldown = 0L

    override fun onServiceConnected() {
        super.onServiceConnected()
        powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        proximitySensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {}

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        if (!powerManager.isInteractive || ForegroundGate.isActive) return
        when (event.sensor.type) {
            Sensor.TYPE_PROXIMITY -> handleProximity(event.values[0])
            Sensor.TYPE_ACCELEROMETER -> handleAccelerometer(event.values)
        }
    }

    private fun triggerHaptic() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(50)
        }
    }

    private fun handleProximity(distance: Float) {
        val now = System.currentTimeMillis()
        if (distance < 2.0f) {
            if (lastProximityTime == 0L) {
                lastProximityTime = now
            }
        } else {
            if (lastProximityTime > 0) {
                val duration = now - lastProximityTime
                lastProximityTime = 0L

                if (duration in 80..450) {
                    waveCount++
                    handler.removeCallbacks(waveTimeoutRunnable)
                    if (waveCount == 1) {
                        handler.postDelayed({
                            if (waveCount == 1) {
                                triggerHaptic()
                                performGlobalAction(GLOBAL_ACTION_HOME)
                                waveCount = 0
                            }
                        }, 500)
                    } else if (waveCount >= 2) {
                        triggerHaptic()
                        performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS)
                        waveCount = 0
                    }
                } else if (duration > 1000) {
                    waveCount = 0
                }
            }
        }
    }

    private fun handleAccelerometer(values: FloatArray) {
        val now = System.currentTimeMillis()
        if (now - shakeCooldown < 1000) return

        if (lastAccel != null) {
            val dx = values[0] - lastAccel!![0]
            val dy = values[1] - lastAccel!![1]
            val dz = values[2] - lastAccel!![2]
            val delta = sqrt((dx * dx + dy * dy + dz * dz).toDouble()).toFloat()
            
            if (delta > 20f && kotlin.math.abs(dz) > 15f) {
                triggerHaptic()
                performGlobalAction(GLOBAL_ACTION_BACK)
                shakeCooldown = now
            }
        } else {
            lastAccel = FloatArray(3)
        }
        lastAccel!![0] = values[0]
        lastAccel!![1] = values[1]
        lastAccel!![2] = values[2]
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onDestroy() {
        super.onDestroy()
        if (::sensorManager.isInitialized) {
            sensorManager.unregisterListener(this)
        }
        handler.removeCallbacks(waveTimeoutRunnable)
    }
}
