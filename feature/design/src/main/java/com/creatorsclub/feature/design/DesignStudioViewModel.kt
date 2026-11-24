package com.creatorsclub.feature.design

import android.app.Application
import android.graphics.Color as AndroidColor
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.creatorsclub.core.database.KreaviloDatabase
import com.creatorsclub.core.database.model.ProjectEntity
import com.creatorsclub.core.storage.manager.StorageManager
import com.creatorsclub.engine.canvas.model.CanvasElement
import com.creatorsclub.engine.canvas.model.DesignTemplate
import com.creatorsclub.engine.canvas.model.ShapeElement
import com.creatorsclub.engine.canvas.model.ShapeType
import com.creatorsclub.engine.canvas.model.TextElement
import com.creatorsclub.engine.canvas.renderer.DesignCanvasRenderer
import com.creatorsclub.engine.canvas.templates.DefaultTemplates
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
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
    private val db = KreaviloDatabase.getInstance(application)
    private val projectDao = db.projectDao()

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
        val docCx = _uiState.value.canvasWidth / 2f
        val docCy = _uiState.value.canvasHeight / 2f

        val shape = ShapeElement(
            id = "shape_${UUID.randomUUID().toString().take(6)}",
            x = docCx,
            y = docCy,
            width = 380f,
            height = 110f,
            fillColor = bgColor,
            shapeType = ShapeType.ROUNDED_RECTANGLE,
            cornerRadius = 28f
        )
        val label = TextElement(
            id = "txt_${UUID.randomUUID().toString().take(6)}",
            text = text,
            x = docCx,
            y = docCy,
            fontSizeSp = 32f,
            color = Color.White,
            isBold = true
        )
        _uiState.update { it.copy(elements = it.elements + shape + label, selectedElementId = label.id) }
        ensureProjectCreated()
        persistAutosave()
    }

    fun addImageElement(filePath: String) {
        saveForUndo()
        val bitmap = android.graphics.BitmapFactory.decodeFile(filePath) ?: return
        val docCx = _uiState.value.canvasWidth / 2f
        val docCy = _uiState.value.canvasHeight / 2f

        val newElement = com.creatorsclub.engine.canvas.model.ImageElement(
            id = "img_${UUID.randomUUID().toString().take(6)}",
            x = docCx,
            y = docCy,
            width = 400f,
            height = 400f * (bitmap.height.toFloat() / bitmap.width.toFloat().coerceAtLeast(1f)),
            bitmap = bitmap,
            assetUri = filePath
        )
        _uiState.update { it.copy(elements = it.elements + newElement, selectedElementId = newElement.id) }
        ensureProjectCreated()
        persistAutosave()
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
        persistAutosave()
    }

    fun addText(text: String, isHeading: Boolean = false) {
        saveForUndo()
        val docCx = _uiState.value.canvasWidth / 2f
        val docCy = _uiState.value.canvasHeight / 2f

        val newElement = TextElement(
            id = "txt_${UUID.randomUUID().toString().take(6)}",
            text = text,
            x = docCx,
            y = docCy,
            fontSizeSp = if (isHeading) 52f else 32f,
            isBold = isHeading,
            color = Color(0xFF182033)
        )
        _uiState.update {
            it.copy(
                elements = it.elements + newElement,
                selectedElementId = newElement.id
            )
        }
        ensureProjectCreated()
        persistAutosave()
    }

    fun addShape(type: ShapeType, color: Color) {
        saveForUndo()
        val docCx = _uiState.value.canvasWidth / 2f
        val docCy = _uiState.value.canvasHeight / 2f

        val newShape = ShapeElement(
            id = "shape_${UUID.randomUUID().toString().take(6)}",
            shapeType = type,
            fillColor = color,
            x = docCx,
            y = docCy,
            width = 360f,
            height = 240f
        )
        _uiState.update {
            it.copy(
                elements = it.elements + newShape,
                selectedElementId = newShape.id
            )
        }
        ensureProjectCreated()
        persistAutosave()
    }

    fun setBackgroundColor(color: Color) {
        _uiState.update { it.copy(backgroundColor = color) }
        persistAutosave()
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
        persistAutosave()
    }

    // --- Inline Text Editing ---

    fun startEditingText(elementId: String) {
        val element = _uiState.value.elements.find { it.id == elementId } as? TextElement
        _uiState.update { it.copy(editingTextElement = element) }
    }

    fun dismissTextEditing() {
        _uiState.update { it.copy(editingTextElement = null) }
    }

    fun updateEditingText(text: String, fontSizeSp: Float, isBold: Boolean, color: Color) {
        val editing = _uiState.value.editingTextElement ?: return
        saveForUndo()
        _uiState.update { state ->
            val updated = state.elements.map { el ->
                if (el.id == editing.id && el is TextElement) {
                    el.copy(text = text, fontSizeSp = fontSizeSp, isBold = isBold, color = color)
                } else el
            }
            state.copy(elements = updated, editingTextElement = null)
        }
        persistAutosave()
    }

    // --- Template Loading in Canonical Document Coordinates ---

    fun loadTemplate(template: DesignTemplate) {
        saveForUndo()
        val newElements = mutableListOf<CanvasElement>()

        // Add shapes with true document coordinates: centerX = spec.x + spec.width / 2f
        template.rawShapeElements.forEach { spec ->
            val shapeType = when (spec.shapeType) {
                "CIRCLE" -> ShapeType.CIRCLE
                "BADGE_STAR" -> ShapeType.BADGE_STAR
                else -> ShapeType.ROUNDED_RECTANGLE
            }
            val centerX = spec.x + spec.width / 2f
            val centerY = spec.y + spec.height / 2f

            newElements.add(
                ShapeElement(
                    id = spec.id,
                    x = centerX,
                    y = centerY,
                    width = spec.width,
                    height = spec.height,
                    fillColor = Color(AndroidColor.parseColor(spec.fillColorHex)),
                    shapeType = shapeType,
                    cornerRadius = spec.cornerRadius
                )
            )
        }

        // Add texts with true document coordinates
        template.rawTextElements.forEach { spec ->
            newElements.add(
                TextElement(
                    id = spec.id,
                    text = spec.text,
                    x = spec.x,
                    y = spec.y,
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

        ensureProjectCreated()
        persistAutosave()
    }

    // --- Undo / Redo ---

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
            persistAutosave()
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
            persistAutosave()
        }
    }

    // --- Favorites & Soft Deletion (5-Day Retention) ---

    fun setShowTrashConfirmDialog(show: Boolean) {
        _uiState.update { it.copy(showTrashConfirmDialog = show) }
    }

    fun toggleFavorite() {
        val newFav = !_uiState.value.isFavorite
        _uiState.update { it.copy(isFavorite = newFav) }
        val projectId = _uiState.value.projectId ?: return
        viewModelScope.launch(Dispatchers.IO) {
            projectDao.setFavorite(projectId, newFav)
        }
    }

    fun trashProject() {
        val projectId = _uiState.value.projectId ?: return
        viewModelScope.launch(Dispatchers.IO) {
            projectDao.softDeleteProject(projectId)
            _uiState.update {
                it.copy(
                    isProjectTrashed = true,
                    showTrashConfirmDialog = false
                )
            }
        }
    }

    // --- Room Persistence ---

    private fun ensureProjectCreated() {
        if (_uiState.value.projectId != null) return
        val newId = UUID.randomUUID().toString()
        _uiState.update { it.copy(projectId = newId) }

        viewModelScope.launch(Dispatchers.IO) {
            val entity = ProjectEntity(
                projectId = newId,
                name = "Design Project",
                studioType = "DESIGN",
                createdAt = System.currentTimeMillis(),
                modifiedAt = System.currentTimeMillis(),
                canvasWidth = _uiState.value.canvasWidth,
                canvasHeight = _uiState.value.canvasHeight,
                aspectRatio = "${_uiState.value.canvasWidth}:${_uiState.value.canvasHeight}",
                thumbnailPath = "",
                projectFilePath = "",
                isFavorite = _uiState.value.isFavorite,
                isDeleted = false,
                deletedAt = null
            )
            projectDao.insertProject(entity)
        }
    }

    private fun persistAutosave() {
        val projectId = _uiState.value.projectId ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val existing = projectDao.getProjectById(projectId) ?: return@launch
            val updated = existing.copy(
                modifiedAt = System.currentTimeMillis(),
                isFavorite = _uiState.value.isFavorite
            )
            projectDao.updateProject(updated)
        }
    }

    fun dismissExportMessage() {
        _uiState.update { it.copy(exportSuccessUri = null) }
    }

    fun dismissErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    // --- Export Pipeline ---

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
