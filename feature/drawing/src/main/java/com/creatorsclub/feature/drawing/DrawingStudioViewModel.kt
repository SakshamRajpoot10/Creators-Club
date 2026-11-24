package com.creatorsclub.feature.drawing

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
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
import java.util.Stack
import java.util.UUID
import javax.inject.Inject
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

data class DrawingStudioUiState(
    val layers: List<DrawingLayer> = listOf(DrawingLayer(name = "Layer 1")),
    val activeLayerId: String = "",
    val currentStroke: DrawingStroke? = null,
    val currentShape: DrawingShape? = null,
    val selectedBrush: BrushType = BrushType.INK_PEN,
    val selectedShapeType: DrawingShapeType = DrawingShapeType.RECTANGLE,
    val strokeWidth: Float = 6f,
    val strokeAlpha: Float = 1f,
    val brushColor: Color = Color(0xFF182033),
    val fillColor: Color? = null,
    val symmetryMode: SymmetryMode = SymmetryMode.NONE,
    val isTransparentBackground: Boolean = false,
    val canvasBackgroundColor: Color = Color.White,
    val activeTab: DrawingToolTab = DrawingToolTab.BRUSHES,
    val isFavorite: Boolean = false,
    val projectId: String? = null,
    val isProjectTrashed: Boolean = false,
    val showTrashConfirmDialog: Boolean = false,
    val showClearCanvasDialog: Boolean = false,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val exportSuccessUri: Uri? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    val activeLayer: DrawingLayer?
        get() = layers.find { it.id == activeLayerId } ?: layers.firstOrNull()

    // Aggregate of all visible strokes across layers
    val allVisibleStrokes: List<DrawingStroke>
        get() = layers.filter { it.isVisible }.flatMap { it.strokes }

    val allVisibleShapes: List<DrawingShape>
        get() = layers.filter { it.isVisible }.flatMap { it.shapes }
}

@HiltViewModel
class DrawingStudioViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val storageManager = StorageManager(application)
    private val db = KreaviloDatabase.getInstance(application)
    private val projectDao = db.projectDao()

    private val _uiState = MutableStateFlow(DrawingStudioUiState())
    val uiState: StateFlow<DrawingStudioUiState> = _uiState.asStateFlow()

    private val undoStack = Stack<List<DrawingLayer>>()
    private val redoStack = Stack<List<DrawingLayer>>()

    init {
        // Ensure initial active layer ID matches the first layer
        val initialLayer = _uiState.value.layers.first()
        _uiState.update { it.copy(activeLayerId = initialLayer.id) }
    }

    fun selectTab(tab: DrawingToolTab) {
        _uiState.update { it.copy(activeTab = tab) }
    }

    fun selectBrush(brush: BrushType) {
        _uiState.update {
            it.copy(
                selectedBrush = brush,
                strokeWidth = brush.defaultWidth,
                strokeAlpha = brush.defaultAlpha
            )
        }
    }

    fun selectShapeType(shapeType: DrawingShapeType) {
        _uiState.update { it.copy(selectedShapeType = shapeType) }
    }

    fun setStrokeWidth(width: Float) {
        _uiState.update { it.copy(strokeWidth = width) }
    }

    fun setStrokeAlpha(alpha: Float) {
        _uiState.update { it.copy(strokeAlpha = alpha) }
    }

    fun setBrushColor(color: Color) {
        _uiState.update { it.copy(brushColor = color) }
    }

    fun setFillColor(color: Color?) {
        _uiState.update { it.copy(fillColor = color) }
    }

    fun setSymmetryMode(mode: SymmetryMode) {
        _uiState.update { it.copy(symmetryMode = mode) }
    }

    fun setTransparentBackground(isTransparent: Boolean) {
        _uiState.update { it.copy(isTransparentBackground = isTransparent) }
        persistAutosave()
    }

    // --- Drawing Stroke Handling ---

    fun startStroke(x: Float, y: Float) {
        val state = _uiState.value
        val active = state.activeLayer ?: return
        if (active.isLocked) return

        val isEraser = state.selectedBrush == BrushType.ERASER
        val newStroke = DrawingStroke(
            brushType = state.selectedBrush,
            points = listOf(DrawingPoint(x, y)),
            color = if (isEraser) Color.White else state.brushColor,
            strokeWidth = state.strokeWidth,
            alpha = state.strokeAlpha,
            isEraser = isEraser
        )
        _uiState.update { it.copy(currentStroke = newStroke) }
    }

    fun appendPoint(x: Float, y: Float) {
        val current = _uiState.value.currentStroke ?: return
        val updated = current.copy(points = current.points + DrawingPoint(x, y))
        _uiState.update { it.copy(currentStroke = updated) }
    }

    fun endStroke(canvasWidth: Float, canvasHeight: Float) {
        val current = _uiState.value.currentStroke ?: return
        val activeId = _uiState.value.activeLayerId

        saveForUndo()

        val generatedStrokes = generateSymmetryStrokes(current, _uiState.value.symmetryMode, canvasWidth, canvasHeight)

        _uiState.update { state ->
            val updatedLayers = state.layers.map { layer ->
                if (layer.id == activeId) {
                    layer.copy(strokes = layer.strokes + generatedStrokes)
                } else {
                    layer
                }
            }
            state.copy(
                layers = updatedLayers,
                currentStroke = null,
                canUndo = true,
                canRedo = false
            )
        }

        ensureProjectCreated()
        persistAutosave()
    }

    // --- Shape Addition ---

    fun addShape(shapeType: DrawingShapeType, startX: Float, startY: Float, endX: Float, endY: Float) {
        val activeId = _uiState.value.activeLayerId
        val state = _uiState.value
        val active = state.activeLayer ?: return
        if (active.isLocked) return

        saveForUndo()

        val shape = DrawingShape(
            shapeType = shapeType,
            points = listOf(DrawingPoint(startX, startY), DrawingPoint(endX, endY)),
            strokeColor = state.brushColor,
            fillColor = state.fillColor,
            strokeWidth = state.strokeWidth,
            alpha = state.strokeAlpha
        )

        _uiState.update { st ->
            val updatedLayers = st.layers.map { layer ->
                if (layer.id == activeId) {
                    layer.copy(shapes = layer.shapes + shape)
                } else layer
            }
            st.copy(layers = updatedLayers, canUndo = true, canRedo = false)
        }

        ensureProjectCreated()
        persistAutosave()
    }

    // --- Mathematical Symmetry Engine ---

    private fun generateSymmetryStrokes(
        baseStroke: DrawingStroke,
        mode: SymmetryMode,
        width: Float,
        height: Float
    ): List<DrawingStroke> {
        if (mode == SymmetryMode.NONE) return listOf(baseStroke)

        val results = mutableListOf(baseStroke)
        val cx = width / 2f
        val cy = height / 2f
        val epsilon = 4f // Protection against center clutter/black blob

        when (mode) {
            SymmetryMode.NONE -> { /* handled above */ }
            SymmetryMode.VERTICAL_MIRROR -> {
                val mirroredPoints = baseStroke.points.map { pt ->
                    val dx = pt.x - cx
                    val dy = pt.y - cy
                    if (hypot(dx, dy) < epsilon) pt
                    else DrawingPoint(x = 2 * cx - pt.x, y = pt.y)
                }
                results.add(baseStroke.copy(points = mirroredPoints))
            }
            SymmetryMode.HORIZONTAL_MIRROR -> {
                val mirroredPoints = baseStroke.points.map { pt ->
                    val dx = pt.x - cx
                    val dy = pt.y - cy
                    if (hypot(dx, dy) < epsilon) pt
                    else DrawingPoint(x = pt.x, y = 2 * cy - pt.y)
                }
                results.add(baseStroke.copy(points = mirroredPoints))
            }
            SymmetryMode.RADIAL_4X,
            SymmetryMode.RADIAL_6X,
            SymmetryMode.RADIAL_8X,
            SymmetryMode.RADIAL_12X -> {
                val folds = mode.folds
                val angleStep = (2.0 * Math.PI / folds)

                for (k in 1 until folds) {
                    val angle = k * angleStep
                    val cosA = cos(angle).toFloat()
                    val sinA = sin(angle).toFloat()

                    val rotatedPoints = baseStroke.points.map { pt ->
                        val dx = pt.x - cx
                        val dy = pt.y - cy
                        if (hypot(dx, dy) < epsilon) {
                            pt
                        } else {
                            val rx = dx * cosA - dy * sinA + cx
                            val ry = dx * sinA + dy * cosA + cy
                            DrawingPoint(rx, ry)
                        }
                    }
                    results.add(baseStroke.copy(points = rotatedPoints))
                }
            }
        }
        return results
    }

    // --- Layer Operations ---

    fun addLayer() {
        saveForUndo()
        val newCount = _uiState.value.layers.size + 1
        val newLayer = DrawingLayer(name = "Layer $newCount")
        _uiState.update {
            it.copy(
                layers = it.layers + newLayer,
                activeLayerId = newLayer.id,
                canUndo = true,
                canRedo = false
            )
        }
        persistAutosave()
    }

    fun selectLayer(layerId: String) {
        _uiState.update { it.copy(activeLayerId = layerId) }
    }

    fun toggleLayerVisibility(layerId: String) {
        _uiState.update { state ->
            val updated = state.layers.map {
                if (it.id == layerId) it.copy(isVisible = !it.isVisible) else it
            }
            state.copy(layers = updated)
        }
        persistAutosave()
    }

    fun toggleLayerLock(layerId: String) {
        _uiState.update { state ->
            val updated = state.layers.map {
                if (it.id == layerId) it.copy(isLocked = !it.isLocked) else it
            }
            state.copy(layers = updated)
        }
    }

    fun setLayerOpacity(layerId: String, opacity: Float) {
        _uiState.update { state ->
            val updated = state.layers.map {
                if (it.id == layerId) it.copy(opacity = opacity.coerceIn(0f, 1f)) else it
            }
            state.copy(layers = updated)
        }
        persistAutosave()
    }

    fun deleteLayer(layerId: String) {
        if (_uiState.value.layers.size <= 1) return // Keep at least one layer
        saveForUndo()
        _uiState.update { state ->
            val updated = state.layers.filterNot { it.id == layerId }
            val nextActiveId = if (state.activeLayerId == layerId) updated.first().id else state.activeLayerId
            state.copy(layers = updated, activeLayerId = nextActiveId, canUndo = true, canRedo = false)
        }
        persistAutosave()
    }

    // --- Clear Canvas & Undo/Redo ---

    fun setShowClearCanvasDialog(show: Boolean) {
        _uiState.update { it.copy(showClearCanvasDialog = show) }
    }

    fun clearCanvas() {
        saveForUndo()
        _uiState.update { state ->
            val clearedLayers = state.layers.map { it.copy(strokes = emptyList(), shapes = emptyList()) }
            state.copy(
                layers = clearedLayers,
                canUndo = true,
                canRedo = false,
                showClearCanvasDialog = false
            )
        }
        persistAutosave()
    }

    private fun saveForUndo() {
        undoStack.push(_uiState.value.layers)
        redoStack.clear()
        _uiState.update { it.copy(canUndo = true, canRedo = false) }
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            redoStack.push(_uiState.value.layers)
            val previous = undoStack.pop()
            _uiState.update {
                it.copy(
                    layers = previous,
                    canUndo = undoStack.isNotEmpty(),
                    canRedo = true
                )
            }
            persistAutosave()
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            undoStack.push(_uiState.value.layers)
            val next = redoStack.pop()
            _uiState.update {
                it.copy(
                    layers = next,
                    canUndo = true,
                    canRedo = redoStack.isNotEmpty()
                )
            }
            persistAutosave()
        }
    }

    // --- Favorites & Soft Deletion (5-Day Retention Policy) ---

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
                name = "Drawing Project",
                studioType = "DRAWING",
                createdAt = System.currentTimeMillis(),
                modifiedAt = System.currentTimeMillis(),
                canvasWidth = 1080,
                canvasHeight = 1080,
                aspectRatio = "1:1",
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

    fun exportDrawing(asPng: Boolean = true) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val layers = _uiState.value.layers
            val isTransparent = _uiState.value.isTransparentBackground && asPng
            val bitmap = renderLayersToBitmap(1080, 1080, layers, isTransparent)

            val format = if (isTransparent) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG
            val filename = "Kreavilo_Drawing_${System.currentTimeMillis()}"

            val result = storageManager.exportImageToGallery(
                bitmap = bitmap,
                filename = filename,
                format = format
            )

            result.onSuccess { uri ->
                _uiState.update { it.copy(isLoading = false, exportSuccessUri = uri) }
            }.onFailure { err ->
                _uiState.update { it.copy(isLoading = false, errorMessage = err.message) }
            }
        }
    }

    private suspend fun renderLayersToBitmap(
        width: Int,
        height: Int,
        layers: List<DrawingLayer>,
        isTransparent: Boolean
    ): Bitmap = withContext(Dispatchers.Default) {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        if (!isTransparent) {
            canvas.drawColor(android.graphics.Color.WHITE)
        }

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }

        // Render layers in forward order (bottom to top)
        for (layer in layers) {
            if (!layer.isVisible) continue
            val layerAlpha = layer.opacity

            // Render strokes
            for (stroke in layer.strokes) {
                if (stroke.points.size < 2) continue

                paint.color = stroke.color.toArgb()
                paint.strokeWidth = stroke.strokeWidth * 2.5f
                paint.alpha = (stroke.alpha * layerAlpha * 255).toInt().coerceIn(0, 255)

                when (stroke.brushType) {
                    BrushType.PENCIL -> {
                        paint.strokeCap = Paint.Cap.ROUND
                    }
                    BrushType.CALLIGRAPHY -> {
                        paint.strokeCap = Paint.Cap.SQUARE
                    }
                    BrushType.HIGHLIGHTER -> {
                        paint.strokeCap = Paint.Cap.SQUARE
                    }
                    else -> {
                        paint.strokeCap = Paint.Cap.ROUND
                    }
                }

                val path = Path()
                path.moveTo(stroke.points[0].x * 2.5f, stroke.points[0].y * 2.5f)
                for (i in 1 until stroke.points.size) {
                    path.lineTo(stroke.points[i].x * 2.5f, stroke.points[i].y * 2.5f)
                }
                canvas.drawPath(path, paint)
            }

            // Render shapes
            for (shape in layer.shapes) {
                if (shape.points.size < 2) continue
                val p1 = shape.points[0]
                val p2 = shape.points[1]

                val left = minOf(p1.x, p2.x) * 2.5f
                val top = minOf(p1.y, p2.y) * 2.5f
                val right = maxOf(p1.x, p2.x) * 2.5f
                val bottom = maxOf(p1.y, p2.y) * 2.5f

                paint.color = shape.strokeColor.toArgb()
                paint.strokeWidth = shape.strokeWidth * 2.5f
                paint.alpha = (shape.alpha * layerAlpha * 255).toInt().coerceIn(0, 255)

                when (shape.shapeType) {
                    DrawingShapeType.LINE -> {
                        canvas.drawLine(p1.x * 2.5f, p1.y * 2.5f, p2.x * 2.5f, p2.y * 2.5f, paint)
                    }
                    DrawingShapeType.RECTANGLE, DrawingShapeType.SQUARE -> {
                        canvas.drawRect(left, top, right, bottom, paint)
                    }
                    DrawingShapeType.ROUNDED_RECT -> {
                        canvas.drawRoundRect(left, top, right, bottom, shape.cornerRadius * 2.5f, shape.cornerRadius * 2.5f, paint)
                    }
                    DrawingShapeType.CIRCLE, DrawingShapeType.ELLIPSE -> {
                        canvas.drawOval(left, top, right, bottom, paint)
                    }
                    else -> {
                        canvas.drawLine(p1.x * 2.5f, p1.y * 2.5f, p2.x * 2.5f, p2.y * 2.5f, paint)
                    }
                }
            }
        }

        bitmap
    }
}
