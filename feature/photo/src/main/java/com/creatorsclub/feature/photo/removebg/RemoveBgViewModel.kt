package com.creatorsclub.feature.photo.removebg

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.creatorsclub.core.database.KreaviloDatabase
import com.creatorsclub.core.database.model.ProjectEntity
import com.creatorsclub.core.storage.manager.StorageManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.Stack
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class RemoveBgViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val storageManager = StorageManager(application)
    private val db = KreaviloDatabase.getInstance(application)
    private val projectDao = db.projectDao()

    private val _uiState = MutableStateFlow(RemoveBgUiState())
    val uiState: StateFlow<RemoveBgUiState> = _uiState.asStateFlow()

    private val maskUndoStack = Stack<Bitmap>()
    private val maskRedoStack = Stack<Bitmap>()

    fun loadImageFromUri(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, imageUri = uri) }
            val bitmap = decodeSampledBitmap(uri)
            if (bitmap != null) {
                processNewImage(bitmap)
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Could not load selected photo") }
            }
        }
    }

    fun loadImageFromBitmap(bitmap: Bitmap) {
        viewModelScope.launch {
            processNewImage(bitmap)
        }
    }

    private suspend fun processNewImage(bitmap: Bitmap) {
        _uiState.update { it.copy(isLoading = true, sourceBitmap = bitmap) }
        val mask = RemoveBgProcessor.generateForegroundMask(bitmap)
        val composite = RemoveBgProcessor.compositeSourceWithMask(bitmap, mask)

        val projectId = "proj_cutout_${UUID.randomUUID().toString().take(8)}"
        maskUndoStack.clear()
        maskRedoStack.clear()

        _uiState.update {
            it.copy(
                isLoading = false,
                projectId = projectId,
                projectName = "Cutout ${System.currentTimeMillis() % 10000}",
                sourceBitmap = bitmap,
                alphaMaskBitmap = mask,
                compositeBitmap = composite,
                canUndo = false,
                canRedo = false
            )
        }

        saveDurableCutoutProject(projectId, bitmap, mask, composite)
    }

    private suspend fun saveDurableCutoutProject(
        projectId: String,
        source: Bitmap,
        mask: Bitmap,
        composite: Bitmap
    ) = withContext(Dispatchers.IO) {
        runCatching {
            val dir = storageManager.getProjectsDir()
            val sourceFile = File(dir, "${projectId}_source.png")
            FileOutputStream(sourceFile).use { fos ->
                source.compress(Bitmap.CompressFormat.PNG, 100, fos)
            }

            val thumbFile = File(dir, "${projectId}_thumb.png")
            FileOutputStream(thumbFile).use { fos ->
                val thumb = Bitmap.createScaledBitmap(composite, 160, 160, true)
                thumb.compress(Bitmap.CompressFormat.PNG, 90, fos)
                thumb.recycle()
            }

            val projectEntity = ProjectEntity(
                projectId = projectId,
                name = _uiState.value.projectName,
                studioType = "PHOTO",
                createdAt = System.currentTimeMillis(),
                modifiedAt = System.currentTimeMillis(),
                canvasWidth = source.width,
                canvasHeight = source.height,
                aspectRatio = "CUTOUT",
                thumbnailPath = thumbFile.absolutePath,
                projectFilePath = sourceFile.absolutePath,
                isFavorite = false,
                isDeleted = false,
                deletedAt = null
            )
            projectDao.insertProject(projectEntity)
        }
    }

    fun selectTool(tool: RemoveBgTool) {
        _uiState.update { it.copy(activeTool = tool) }
    }

    fun updateBrushSize(size: Float) {
        _uiState.update { it.copy(brushSize = size) }
    }

    fun updateFeather(feather: Float) {
        _uiState.update { it.copy(featherRadius = feather) }
    }

    fun onBrushStroke(normX: Float, normY: Float) {
        val mask = _uiState.value.alphaMaskBitmap ?: return
        val src = _uiState.value.sourceBitmap ?: return
        val tool = _uiState.value.activeTool
        if (tool != RemoveBgTool.ERASE && tool != RemoveBgTool.RESTORE) return

        // Push current mask to undo stack before stroke
        val snapshot = mask.copy(mask.config ?: Bitmap.Config.ALPHA_8, true)
        maskUndoStack.push(snapshot)
        maskRedoStack.clear()

        val touchX = normX * mask.width
        val touchY = normY * mask.height
        val isErase = tool == RemoveBgTool.ERASE

        RemoveBgProcessor.applyBrushStroke(
            mask = mask,
            touchX = touchX,
            touchY = touchY,
            radius = _uiState.value.brushSize * (mask.width / 400f),
            isErase = isErase,
            feather = _uiState.value.featherRadius
        )

        viewModelScope.launch {
            val updatedComposite = RemoveBgProcessor.compositeSourceWithMask(src, mask)
            _uiState.update {
                it.copy(
                    alphaMaskBitmap = mask,
                    compositeBitmap = updatedComposite,
                    canUndo = maskUndoStack.isNotEmpty(),
                    canRedo = false
                )
            }
        }
    }

    fun undo() {
        if (maskUndoStack.isNotEmpty()) {
            val currentMask = _uiState.value.alphaMaskBitmap ?: return
            val src = _uiState.value.sourceBitmap ?: return
            maskRedoStack.push(currentMask.copy(currentMask.config ?: Bitmap.Config.ALPHA_8, true))

            val prevMask = maskUndoStack.pop()
            viewModelScope.launch {
                val updatedComposite = RemoveBgProcessor.compositeSourceWithMask(src, prevMask)
                _uiState.update {
                    it.copy(
                        alphaMaskBitmap = prevMask,
                        compositeBitmap = updatedComposite,
                        canUndo = maskUndoStack.isNotEmpty(),
                        canRedo = true
                    )
                }
            }
        }
    }

    fun redo() {
        if (maskRedoStack.isNotEmpty()) {
            val currentMask = _uiState.value.alphaMaskBitmap ?: return
            val src = _uiState.value.sourceBitmap ?: return
            maskUndoStack.push(currentMask.copy(currentMask.config ?: Bitmap.Config.ALPHA_8, true))

            val nextMask = maskRedoStack.pop()
            viewModelScope.launch {
                val updatedComposite = RemoveBgProcessor.compositeSourceWithMask(src, nextMask)
                _uiState.update {
                    it.copy(
                        alphaMaskBitmap = nextMask,
                        compositeBitmap = updatedComposite,
                        canUndo = true,
                        canRedo = maskRedoStack.isNotEmpty()
                    )
                }
            }
        }
    }

    fun exportTransparentPng() {
        val composite = _uiState.value.compositeBitmap ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true) }
            val filename = "Kreavilo_Cutout_${System.currentTimeMillis()}"
            val res = storageManager.exportImageToGallery(
                bitmap = composite,
                filename = filename,
                format = Bitmap.CompressFormat.PNG,
                quality = 100
            )

            res.onSuccess { uri ->
                _uiState.update { it.copy(isExporting = false, exportSuccessUri = uri) }
            }.onFailure { err ->
                _uiState.update { it.copy(isExporting = false, errorMessage = err.message ?: "Export failed") }
            }
        }
    }

    fun dismissExportMessage() {
        _uiState.update { it.copy(exportSuccessUri = null) }
    }

    fun dismissErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private suspend fun decodeSampledBitmap(uri: Uri): Bitmap? = withContext(Dispatchers.IO) {
        runCatching {
            val context = getApplication<Application>()
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeStream(stream, null, options)

                val maxDim = maxOf(options.outWidth, options.outHeight)
                var sampleSize = 1
                while (maxDim / sampleSize > 2560) {
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
