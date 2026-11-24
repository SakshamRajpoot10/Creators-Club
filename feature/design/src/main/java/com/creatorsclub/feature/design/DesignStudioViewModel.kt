package com.creatorsclub.feature.design

import android.app.Application
import android.graphics.Color as AndroidColor
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.creatorsclub.core.storage.manager.StorageManager
import com.creatorsclub.engine.canvas.model.CanvasElement
import com.creatorsclub.engine.canvas.model.DesignTemplate
import com.creatorsclub.engine.canvas.model.ShapeElement
import com.creatorsclub.engine.canvas.model.ShapeType
import com.creatorsclub.engine.canvas.model.TextElement
import com.creatorsclub.engine.canvas.renderer.DesignCanvasRenderer
import com.creatorsclub.engine.canvas.templates.DefaultTemplates
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Stack
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class DesignStudioViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val storageManager = StorageManager(application)

    private val _uiState = MutableStateFlow(
        DesignStudioUiState(
            availableTemplates = DefaultTemplates.allTemplates
        )
    )
    val uiState: StateFlow<DesignStudioUiState> = _uiState.asStateFlow()

    private val undoStack = Stack<List<CanvasElement>>()
    private val redoStack = Stack<List<CanvasElement>>()

    init {
        // Load first template as default canvas
        if (DefaultTemplates.allTemplates.isNotEmpty()) {
            loadTemplate(DefaultTemplates.allTemplates.first())
        }
        refreshImportedAssets()
    }

    fun refreshImportedAssets() {
        val stickers = storageManager.listImportedAssets("stickers")
        _uiState.update { it.copy(importedStickers = stickers) }
    }

    fun addSticker(text: String, bgColor: Color) {
        saveForUndo()
        val shape = ShapeElement(
            id = "shape_${UUID.randomUUID().toString().take(6)}",
            x = 120f,
            y = 120f,
            width = 140f,
            height = 44f,
            fillColor = bgColor,
            shapeType = ShapeType.ROUNDED_RECTANGLE,
            cornerRadius = 12f
        )
        val label = TextElement(
            id = "txt_${UUID.randomUUID().toString().take(6)}",
            text = text,
            x = 130f,
            y = 132f,
            fontSizeSp = 13f,
            color = Color.White,
            isBold = true
        )
        _uiState.update { it.copy(elements = it.elements + shape + label, selectedElementId = label.id) }
    }

    fun addImageElement(filePath: String) {
        saveForUndo()
        val bitmap = android.graphics.BitmapFactory.decodeFile(filePath) ?: return
        val newElement = com.creatorsclub.engine.canvas.model.ImageElement(
            id = "img_${UUID.randomUUID().toString().take(6)}",
            x = 100f,
            y = 100f,
            width = 160f,
            height = 160f * (bitmap.height.toFloat() / bitmap.width.toFloat().coerceAtLeast(1f)),
            bitmap = bitmap,
            assetUri = filePath
        )
        _uiState.update { it.copy(elements = it.elements + newElement, selectedElementId = newElement.id) }
    }

    fun addImageFromUri(uri: android.net.Uri) {
        viewModelScope.launch {
            val fileName = "sticker_${System.currentTimeMillis()}.png"
            val fileResult = storageManager.importAssetFromUri(uri, "stickers", fileName)
            if (fileResult.isSuccess) {
                refreshImportedAssets()
                addImageElement(fileResult.getOrThrow().absolutePath)
            }
        }
    }

    fun selectTab(tab: DesignStudioTab) {
        _uiState.update { it.copy(activeTab = tab) }
    }

    fun selectElement(elementId: String?) {
        _uiState.update { it.copy(selectedElementId = elementId) }
    }

    fun updateElementPosition(elementId: String, newX: Float, newY: Float) {
        _uiState.update { state ->
            val updated = state.elements.map { el ->
                if (el.id == elementId) el.copyWithTransform(x = newX, y = newY) else el
            }
            state.copy(elements = updated)
        }
    }

    fun addText(text: String, isHeading: Boolean = false) {
        saveForUndo()
        val newElement = TextElement(
            id = "txt_${UUID.randomUUID().toString().take(6)}",
            text = text,
            x = 160f,
            y = 160f,
            fontSizeSp = if (isHeading) 36f else 22f,
            isBold = isHeading,
            color = Color(0xFF182033)
        )
        _uiState.update {
            it.copy(
                elements = it.elements + newElement,
                selectedElementId = newElement.id
            )
        }
    }

    fun addShape(type: ShapeType, color: Color) {
        saveForUndo()
        val newShape = ShapeElement(
            id = "shape_${UUID.randomUUID().toString().take(6)}",
            shapeType = type,
            fillColor = color,
            x = 160f,
            y = 160f,
            width = 120f,
            height = 80f
        )
        _uiState.update {
            it.copy(
                elements = it.elements + newShape,
                selectedElementId = newShape.id
            )
        }
    }

    fun setBackgroundColor(color: Color) {
        _uiState.update { it.copy(backgroundColor = color) }
    }

    fun deleteSelectedElement() {
        val selectedId = _uiState.value.selectedElementId ?: return
        saveForUndo()
        _uiState.update { state ->
            state.copy(
                elements = state.elements.filterNot { it.id == selectedId },
                selectedElementId = null
            )
        }
    }

    fun loadTemplate(template: DesignTemplate) {
        saveForUndo()
        val newElements = mutableListOf<CanvasElement>()

        // Add shapes
        template.rawShapeElements.forEach { spec ->
            val shapeType = when (spec.shapeType) {
                "CIRCLE" -> ShapeType.CIRCLE
                "BADGE_STAR" -> ShapeType.BADGE_STAR
                else -> ShapeType.ROUNDED_RECTANGLE
            }
            newElements.add(
                ShapeElement(
                    id = spec.id,
                    x = spec.x * 0.35f, // Scaled for mobile design canvas
                    y = spec.y * 0.35f,
                    width = spec.width * 0.35f,
                    height = spec.height * 0.35f,
                    fillColor = Color(AndroidColor.parseColor(spec.fillColorHex)),
                    shapeType = shapeType
                )
            )
        }

        // Add texts
        template.rawTextElements.forEach { spec ->
            newElements.add(
                TextElement(
                    id = spec.id,
                    text = spec.text,
                    x = spec.x * 0.35f,
                    y = spec.y * 0.35f,
                    fontSizeSp = spec.fontSizeSp,
                    color = Color(AndroidColor.parseColor(spec.colorHex)),
                    isBold = spec.isBold,
                    isItalic = spec.isItalic
                )
            )
        }

        val bgColor = runCatching { Color(AndroidColor.parseColor(template.backgroundColorHex)) }.getOrDefault(Color.White)

        _uiState.update {
            it.copy(
                canvasWidth = template.canvasWidth,
                canvasHeight = template.canvasHeight,
                backgroundColor = bgColor,
                elements = newElements,
                selectedElementId = null
            )
        }
    }

    private fun saveForUndo() {
        undoStack.push(_uiState.value.elements)
        redoStack.clear()
        _uiState.update { it.copy(canUndo = true, canRedo = false) }
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            redoStack.push(_uiState.value.elements)
            val previous = undoStack.pop()
            _uiState.update {
                it.copy(
                    elements = previous,
                    selectedElementId = null,
                    canUndo = undoStack.isNotEmpty(),
                    canRedo = true
                )
            }
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            undoStack.push(_uiState.value.elements)
            val next = redoStack.pop()
            _uiState.update {
                it.copy(
                    elements = next,
                    selectedElementId = null,
                    canUndo = true,
                    canRedo = redoStack.isNotEmpty()
                )
            }
        }
    }

    fun exportDesign() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val state = _uiState.value
            val renderedBitmap = DesignCanvasRenderer.renderToBitmap(
                width = state.canvasWidth,
                height = state.canvasHeight,
                backgroundColor = state.backgroundColor.toArgb(),
                elements = state.elements
            )

            val exportResult = storageManager.exportImageToGallery(
                bitmap = renderedBitmap,
                filename = "Kreavilo_Design_${System.currentTimeMillis()}"
            )

            exportResult.onSuccess { uri ->
                _uiState.update { it.copy(isLoading = false, exportSuccessUri = uri) }
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, errorMessage = error.message) }
            }
        }
    }
}
