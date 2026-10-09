package com.example.tracker_gyro.features.bubblelevel

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

class BubbleLevelViewModel(application: Application) : AndroidViewModel(application) {
    private val sensorTracker = SensorTracker(application)

    private val _pitch = MutableStateFlow(0f)
    val pitch: StateFlow<Float> = _pitch.asStateFlow()

    private val _roll = MutableStateFlow(0f)
    val roll: StateFlow<Float> = _roll.asStateFlow()

    private val _isLevel = MutableStateFlow(false)
    val isLevel: StateFlow<Boolean> = _isLevel.asStateFlow()

    private var trackingJob: Job? = null
    
    private var lastPitch = 0f
    private var lastRoll = 0f
    private val alpha = 0.15f

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
                
                lastPitch = lastPitch + alpha * (currentPitch - lastPitch)
                lastRoll = lastRoll + alpha * (currentRoll - lastRoll)

                _pitch.value = lastPitch
                _roll.value = lastRoll

                _isLevel.value = kotlin.math.abs(lastPitch) <= 0.5f && kotlin.math.abs(lastRoll) <= 0.5f
            }
        }
    }

    fun stopTracking() {
        trackingJob?.cancel()
    }
}
