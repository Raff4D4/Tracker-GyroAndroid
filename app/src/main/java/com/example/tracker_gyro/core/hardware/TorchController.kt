package com.example.tracker_gyro.core.hardware

import android.content.Context
import android.hardware.camera2.CameraManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TorchController(context: Context) {
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private val _torchState = MutableStateFlow(false)
    val torchState: StateFlow<Boolean> = _torchState.asStateFlow()

    private var cameraId: String? = null
    
    private val torchCallback = object : CameraManager.TorchCallback() {
        override fun onTorchModeChanged(callbackCameraId: String, enabled: Boolean) {
            if (callbackCameraId == cameraId) {
                _torchState.value = enabled
            }
        }
    }

    init {
        cameraId = try {
            cameraManager.cameraIdList.firstOrNull { id ->
                val characteristics = cameraManager.getCameraCharacteristics(id)
                val hasFlash = characteristics.get(
                    android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE
                ) == true
                val facing = characteristics.get(
                    android.hardware.camera2.CameraCharacteristics.LENS_FACING
                )
                hasFlash && facing == android.hardware.camera2.CameraCharacteristics.LENS_FACING_BACK
            }
        } catch (_: Exception) {
            null
        }
        
        try {
            cameraId?.let { 
                cameraManager.registerTorchCallback(torchCallback, null)
            }
        } catch (_: Exception) { }
    }

    fun setTorch(enabled: Boolean) {
        val id = cameraId ?: return
        try {
            cameraManager.setTorchMode(id, enabled)
            _torchState.value = enabled
        } catch (_: Exception) {
            _torchState.value = false
        }
    }
    
    fun release() {
        try {
            cameraManager.unregisterTorchCallback(torchCallback)
        } catch (_: Exception) {}
    }
}
