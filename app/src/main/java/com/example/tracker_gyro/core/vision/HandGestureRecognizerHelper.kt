package com.example.tracker_gyro.core.vision

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.gesturerecognizer.GestureRecognizer
import com.google.mediapipe.tasks.vision.gesturerecognizer.GestureRecognizerResult
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

enum class FeedGestureEvent {
    NEXT,
    PREVIOUS,
    TOGGLE_PAUSE,
    LIKE,
    NONE
}

class HandGestureRecognizerHelper(private val context: Context) : ImageAnalysis.Analyzer {

    private var gestureRecognizer: GestureRecognizer? = null
    private val _gestureFlow = MutableSharedFlow<FeedGestureEvent>(extraBufferCapacity = 1)
    val gestureFlow: SharedFlow<FeedGestureEvent> = _gestureFlow.asSharedFlow()

    init {
        setupGestureRecognizer()
    }

    private fun setupGestureRecognizer() {
        val baseOptions = BaseOptions.builder()
            .setModelAssetPath("gesture_recognizer.task")
            .build()

        val options = GestureRecognizer.GestureRecognizerOptions.builder()
            .setBaseOptions(baseOptions)
            .setRunningMode(RunningMode.LIVE_STREAM)
            .setResultListener { result, _ ->
                processResult(result)
            }
            .build()

        try {
            gestureRecognizer = GestureRecognizer.createFromOptions(context, options)
        } catch (_: Exception) {}
    }

    private fun processResult(result: GestureRecognizerResult) {
        val gestures = result.gestures()
        if (gestures.isNotEmpty()) {
            val primaryGesture = gestures[0].firstOrNull()
            val categoryName = primaryGesture?.categoryName()
            val score = primaryGesture?.score() ?: 0f

            if (score > 0.6f) {
                val event = when (categoryName) {
                    "Thumb_Down" -> FeedGestureEvent.NEXT
                    "Thumb_Up" -> FeedGestureEvent.PREVIOUS
                    "Open_Palm" -> FeedGestureEvent.TOGGLE_PAUSE
                    "Closed_Fist" -> FeedGestureEvent.LIKE
                    else -> FeedGestureEvent.NONE
                }
                if (event != FeedGestureEvent.NONE) {
                    _gestureFlow.tryEmit(event)
                }
            }
        }
    }

    override fun analyze(imageProxy: ImageProxy) {
        val bitmap = imageProxy.toBitmap()
        
        val matrix = Matrix().apply {
            postRotate(imageProxy.imageInfo.rotationDegrees.toFloat())
            postScale(-1f, 1f, bitmap.width / 2f, bitmap.height / 2f)
        }
        val rotatedBitmap = Bitmap.createBitmap(
            bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
        )

        val mpImage = BitmapImageBuilder(rotatedBitmap).build()

        gestureRecognizer?.recognizeAsync(mpImage, imageProxy.imageInfo.timestamp)
        imageProxy.close()
    }

    fun close() {
        gestureRecognizer?.close()
        gestureRecognizer = null
    }
}
