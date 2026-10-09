package com.example.tracker_gyro.features.parallax

import android.app.Application
import android.hardware.Sensor
import android.hardware.SensorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tracker_gyro.core.sensor.SensorTracker
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ParallaxViewModel(application: Application) : AndroidViewModel(application) {
    private val sensorTracker = SensorTracker(application)
    
    private val _pitch = MutableStateFlow(0f)
    val pitch: StateFlow<Float> = _pitch.asStateFlow()

    private val _roll = MutableStateFlow(0f)
    val roll: StateFlow<Float> = _roll.asStateFlow()

    private var trackingJob: Job? = null
    private val alpha = 0.15f
    private var lastPitch = 0f
    private var lastRoll = 0f

    fun startTracking() {
        if (trackingJob?.isActive == true) return
        trackingJob = viewModelScope.launch {
            sensorTracker.getSensorFlow(Sensor.TYPE_ROTATION_VECTOR).collect { values ->
                val rotationMatrix = FloatArray(9)
                SensorManager.getRotationMatrixFromVector(rotationMatrix, values)
                val orientation = FloatArray(3)
                SensorManager.getOrientation(rotationMatrix, orientation)

                val currentPitch = Math.toDegrees(orientation[1].toDouble()).toFloat()
                val currentRoll = Math.toDegrees(orientation[2].toDouble()).toFloat()

                lastPitch += alpha * (currentPitch - lastPitch)
                lastRoll += alpha * (currentRoll - lastRoll)

                _pitch.value = lastPitch
                _roll.value = lastRoll
            }
        }
    }

    fun stopTracking() {
        trackingJob?.cancel()
    }
}
