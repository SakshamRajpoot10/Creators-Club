package com.creatorsclub.feature.ar_drawing

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class ArDrawingUiState(
    val referenceBitmap: Bitmap? = null,
    val sketchOutlineBitmap: Bitmap? = null,
    val isOutlineMode: Boolean = true,
    val overlayOpacity: Float = 0.5f,
    val isScreenLocked: Boolean = false,
    val scale: Float = 1.0f,
    val offsetX: Float = 0f,
    val offsetY: Float = 0f,
    val rotation: Float = 0f,
    val isFlashlightOn: Boolean = false,
    val isLoading: Boolean = false
)

@HiltViewModel
class ArDrawingViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(ArDrawingUiState())
    val uiState: StateFlow<ArDrawingUiState> = _uiState.asStateFlow()

    fun setReferenceImage(bitmap: Bitmap) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, referenceBitmap = bitmap) }
            val outline = EdgeDetectionFilter.convertToSketchOutline(bitmap)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    sketchOutlineBitmap = outline
                )
            }
        }
    }

    fun loadReferenceFromUri(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true) }
            val bmp = runCatching {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream)
                }
            }.getOrNull()

            if (bmp != null) {
                withContext(Dispatchers.Main) {
                    setReferenceImage(bmp)
                }
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun loadSampleSketch() {
        val width = 600
        val height = 600
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 6f
        }

        // Draw an aesthetic butterfly / mandala outline
        canvas.drawCircle(300f, 300f, 180f, paint)
        canvas.drawCircle(300f, 300f, 90f, paint)
        canvas.drawLine(120f, 300f, 480f, 300f, paint)
        canvas.drawLine(300f, 120f, 300f, 480f, paint)

        setReferenceImage(bitmap)
    }

    fun toggleOutlineMode() {
        _uiState.update { it.copy(isOutlineMode = !it.isOutlineMode) }
    }

    fun setOverlayOpacity(opacity: Float) {
        _uiState.update { it.copy(overlayOpacity = opacity) }
    }

    fun toggleScreenLock() {
        _uiState.update { it.copy(isScreenLocked = !it.isScreenLocked) }
    }

    fun updateTransform(scale: Float, offsetX: Float, offsetY: Float, rotation: Float) {
        if (!_uiState.value.isScreenLocked) {
            _uiState.update {
                it.copy(scale = scale, offsetX = offsetX, offsetY = offsetY, rotation = rotation)
            }
        }
    }
}
