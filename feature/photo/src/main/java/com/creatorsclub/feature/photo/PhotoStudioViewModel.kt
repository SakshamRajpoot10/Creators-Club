package com.creatorsclub.feature.photo

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.creatorsclub.core.storage.manager.StorageManager
import com.creatorsclub.engine.image.model.CropAspectRatio
import com.creatorsclub.engine.image.model.FilterPreset
import com.creatorsclub.engine.image.model.ImageAdjustmentState
import com.creatorsclub.engine.image.processor.BitmapProcessor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Stack
import javax.inject.Inject

@HiltViewModel
class PhotoStudioViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val storageManager = StorageManager(application)

    private val _uiState = MutableStateFlow(PhotoStudioUiState())
    val uiState: StateFlow<PhotoStudioUiState> = _uiState.asStateFlow()

    private val undoStack = Stack<ImageAdjustmentState>()
    private val redoStack = Stack<ImageAdjustmentState>()

    fun loadImageFromUri(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, imageUri = uri) }
            val bitmap = decodeSampledBitmapFromUri(uri)
            if (bitmap != null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        sourceBitmap = bitmap,
                        adjustments = ImageAdjustmentState(),
                        canUndo = false,
                        canRedo = false
                    )
                }
                undoStack.clear()
                redoStack.clear()
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Could not load selected image"
                    )
                }
            }
        }
    }

    fun setSourceBitmap(bitmap: Bitmap) {
        _uiState.update {
            it.copy(
                sourceBitmap = bitmap,
                adjustments = ImageAdjustmentState(),
                canUndo = false,
                canRedo = false
            )
        }
        undoStack.clear()
        redoStack.clear()
    }

    fun selectTab(tab: PhotoEditorTab) {
        _uiState.update { it.copy(activeTab = tab) }
    }

    fun selectAdjustmentTool(tool: ActiveAdjustmentTool) {
        _uiState.update { it.copy(activeAdjustmentTool = tool) }
    }

    fun applyFilter(preset: FilterPreset) {
        saveStateForUndo()
        _uiState.update {
            it.copy(adjustments = it.adjustments.copy(filterPreset = preset))
        }
    }

    fun updateBrightness(value: Float) {
        _uiState.update {
            it.copy(adjustments = it.adjustments.copy(brightness = value))
        }
    }

    fun updateContrast(value: Float) {
        _uiState.update {
            it.copy(adjustments = it.adjustments.copy(contrast = value))
        }
    }

    fun updateSaturation(value: Float) {
        _uiState.update {
            it.copy(adjustments = it.adjustments.copy(saturation = value))
        }
    }

    fun updateExposure(value: Float) {
        _uiState.update {
            it.copy(adjustments = it.adjustments.copy(exposure = value))
        }
    }

    fun updateWarmth(value: Float) {
        _uiState.update {
            it.copy(adjustments = it.adjustments.copy(warmth = value))
        }
    }

    fun setAspectRatio(ratio: CropAspectRatio) {
        _uiState.update {
            it.copy(cropTransform = it.cropTransform.copy(aspectRatio = ratio))
        }
    }

    fun rotate90() {
        _uiState.update {
            val newAngle = (it.cropTransform.rotationDegrees + 90f) % 360f
            it.copy(cropTransform = it.cropTransform.copy(rotationDegrees = newAngle))
        }
    }

    fun flipHorizontal() {
        _uiState.update {
            it.copy(cropTransform = it.cropTransform.copy(isFlippedHorizontal = !it.cropTransform.isFlippedHorizontal))
        }
    }

    fun setComparingOriginal(isComparing: Boolean) {
        _uiState.update { it.copy(isComparingOriginal = isComparing) }
    }

    private fun saveStateForUndo() {
        undoStack.push(_uiState.value.adjustments)
        redoStack.clear()
        _uiState.update { it.copy(canUndo = true, canRedo = false) }
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            redoStack.push(_uiState.value.adjustments)
            val previousState = undoStack.pop()
            _uiState.update {
                it.copy(
                    adjustments = previousState,
                    canUndo = undoStack.isNotEmpty(),
                    canRedo = true
                )
            }
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            undoStack.push(_uiState.value.adjustments)
            val nextState = redoStack.pop()
            _uiState.update {
                it.copy(
                    adjustments = nextState,
                    canUndo = true,
                    canRedo = redoStack.isNotEmpty()
                )
            }
        }
    }

    fun exportImage() {
        val bitmap = _uiState.value.sourceBitmap ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val rendered = BitmapProcessor.processAndRender(
                sourceBitmap = bitmap,
                adjustmentState = _uiState.value.adjustments,
                cropTransformState = _uiState.value.cropTransform
            )
            val exportResult = storageManager.exportImageToGallery(
                bitmap = rendered,
                filename = "Kreavilo_Photo_${System.currentTimeMillis()}"
            )
            exportResult.onSuccess { uri ->
                _uiState.update { it.copy(isLoading = false, exportSuccessUri = uri) }
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, errorMessage = error.message) }
            }
        }
    }

    private suspend fun decodeSampledBitmapFromUri(uri: Uri): Bitmap? = withContext(Dispatchers.IO) {
        runCatching {
            val context = getApplication<Application>()
            context.contentResolver.openInputStream(uri)?.use { stream ->
                // First decode bounds
                val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeStream(stream, null, options)

                // Downsample if mega-resolution (>4000px) to prevent OOM
                val maxDim = maxOf(options.outWidth, options.outHeight)
                var sampleSize = 1
                while (maxDim / sampleSize > 3840) {
                    sampleSize *= 2
                }

                context.contentResolver.openInputStream(uri)?.use { secondStream ->
                    val decodeOptions = BitmapFactory.Options().apply {
                        inSampleSize = sampleSize
                        inPreferredConfig = Bitmap.Config.ARGB_8888
                    }
                    BitmapFactory.decodeStream(secondStream, null, decodeOptions)
                }
            }
        }.getOrNull()
    }
}
