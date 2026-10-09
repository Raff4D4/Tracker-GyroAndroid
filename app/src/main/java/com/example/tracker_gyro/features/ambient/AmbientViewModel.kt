package com.example.tracker_gyro.features.ambient

import android.app.Application
import android.hardware.Sensor
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tracker_gyro.core.hardware.TorchController
import com.example.tracker_gyro.core.sensor.SensorTracker
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AmbientViewModel(application: Application) : AndroidViewModel(application) {
    private val sensorTracker = SensorTracker(application)
    private val torchController = TorchController(application)

    private val _lux = MutableStateFlow(0f)
    val lux: StateFlow<Float> = _lux.asStateFlow()

    private val _autoMode = MutableStateFlow(false)
    val autoMode: StateFlow<Boolean> = _autoMode.asStateFlow()

    val torchState = torchController.torchState

    private val luxHistory = FloatArray(10)
    private var historyIndex = 0
    private var historyCount = 0
    private var trackingJob: Job? = null

    fun startTracking() {
        if (trackingJob?.isActive == true) return
        trackingJob = viewModelScope.launch {
            sensorTracker.getSensorFlow(Sensor.TYPE_LIGHT).collect { values ->
                val currentLux = values[0]
                luxHistory[historyIndex] = currentLux
                historyIndex = (historyIndex + 1) % 10
                if (historyCount < 10) historyCount++

                val avgLux = luxHistory.take(historyCount).average().toFloat()
                _lux.value = avgLux

                if (_autoMode.value) {
                    if (avgLux < 10f && !torchState.value) {
                        torchController.setTorch(true)
                    } else if (avgLux > 25f && torchState.value) {
                        torchController.setTorch(false)
                    }
                }
            }
        }
    }

    fun stopTracking() {
        trackingJob?.cancel()
        torchController.setTorch(false)
    }

    fun toggleAutoMode(enabled: Boolean) {
        _autoMode.value = enabled
        if (!enabled) {
            torchController.setTorch(false)
        }
    }

    fun setManualTorch(enabled: Boolean) {
        if (!_autoMode.value) {
            torchController.setTorch(enabled)
        }
    }

    override fun onCleared() {
        super.onCleared()
        torchController.setTorch(false)
    }
}
