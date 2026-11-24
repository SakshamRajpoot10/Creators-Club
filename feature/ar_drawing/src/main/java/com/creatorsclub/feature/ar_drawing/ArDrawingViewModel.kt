package com.creatorsclub.feature.ar_drawing

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
