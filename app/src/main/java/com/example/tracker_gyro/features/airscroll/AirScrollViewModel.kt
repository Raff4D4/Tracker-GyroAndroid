package com.example.tracker_gyro.features.airscroll

import android.app.Application
import android.hardware.Sensor
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tracker_gyro.core.sensor.SensorTracker
import com.example.tracker_gyro.core.vision.FeedGestureEvent
import com.example.tracker_gyro.core.vision.HandGestureRecognizerHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.sqrt

class AirScrollViewModel(application: Application) : AndroidViewModel(application) {
    private val sensorTracker = SensorTracker(application)
    val gestureHelper = HandGestureRecognizerHelper(application)

    private val _lux = MutableStateFlow(0f)
    val lux: StateFlow<Float> = _lux.asStateFlow()

    private val _isPrivacyMode = MutableStateFlow(false)
    val isPrivacyMode: StateFlow<Boolean> = _isPrivacyMode.asStateFlow()

    private val _shakeEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val shakeEvent: SharedFlow<Unit> = _shakeEvent.asSharedFlow()

    private val _currentGesture = MutableStateFlow(FeedGestureEvent.NONE)
    val currentGesture: StateFlow<FeedGestureEvent> = _currentGesture.asStateFlow()

    private var trackingJob: Job? = null
    private var lastAccel: FloatArray? = null

    init {
        viewModelScope.launch {
            gestureHelper.gestureFlow.collect { event ->
                _currentGesture.value = event
            }
        }
    }

    fun startTracking() {
        if (trackingJob?.isActive == true) return
        trackingJob = viewModelScope.launch {
            launch {
                sensorTracker.getSensorFlow(Sensor.TYPE_PROXIMITY).collect { values ->
                    _isPrivacyMode.value = values[0] < 2.0f
                }
            }

            launch {
                sensorTracker.getSensorFlow(Sensor.TYPE_LIGHT).collect { values ->
                    _lux.value = values[0]
                }
            }

            launch {
                sensorTracker.getSensorFlow(Sensor.TYPE_ACCELEROMETER).collect { values ->
                    if (lastAccel != null) {
                        val dx = values[0] - lastAccel!![0]
                        val dy = values[1] - lastAccel!![1]
                        val dz = values[2] - lastAccel!![2]
                        val delta = sqrt((dx * dx + dy * dy + dz * dz).toDouble()).toFloat()
                        if (delta > 20f) {
                            _shakeEvent.tryEmit(Unit)
                        }
                    } else {
                        lastAccel = FloatArray(3)
                    }
                    lastAccel!![0] = values[0]
                    lastAccel!![1] = values[1]
                    lastAccel!![2] = values[2]
                }
            }
        }
    }

    fun stopTracking() {
        trackingJob?.cancel()
    }

    override fun onCleared() {
        gestureHelper.close()
    }
}
