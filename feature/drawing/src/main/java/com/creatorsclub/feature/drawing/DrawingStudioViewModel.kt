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
import javax.inject.Inject
import kotlin.math.cos
import kotlin.math.sin

data class DrawingStudioUiState(
    val strokes: List<DrawingStroke> = emptyList(),
    val currentStroke: DrawingStroke? = null,
    val selectedBrush: BrushType = BrushType.INK_PEN,
    val strokeWidth: Float = 6f,
    val strokeAlpha: Float = 1f,
    val brushColor: Color = Color(0xFF182033),
    val symmetryMode: SymmetryMode = SymmetryMode.NONE,
    val isTransparentBackground: Boolean = false,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val exportSuccessUri: Uri? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class DrawingStudioViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val storageManager = StorageManager(application)

    private val _uiState = MutableStateFlow(DrawingStudioUiState())
    val uiState: StateFlow<DrawingStudioUiState> = _uiState.asStateFlow()

    private val undoStack = Stack<List<DrawingStroke>>()
    private val redoStack = Stack<List<DrawingStroke>>()

    fun startStroke(x: Float, y: Float) {
        val state = _uiState.value
        val isEraser = state.selectedBrush == BrushType.ERASER
        val newStroke = DrawingStroke(
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
        undoStack.push(_uiState.value.strokes)
        redoStack.clear()

        val generatedStrokes = generateSymmetryStrokes(current, _uiState.value.symmetryMode, canvasWidth, canvasHeight)

        _uiState.update {
            it.copy(
                strokes = it.strokes + generatedStrokes,
                currentStroke = null,
                canUndo = true,
                canRedo = false
            )
        }
    }

    private fun generateSymmetryStrokes(
        baseStroke: DrawingStroke,
        mode: SymmetryMode,
        width: Float,
        height: Float
    ): List<DrawingStroke> {
        val results = mutableListOf(baseStroke)
        val cx = width / 2f
        val cy = height / 2f

        when (mode) {
            SymmetryMode.NONE -> { /* Only base stroke */ }
            SymmetryMode.VERTICAL_MIRROR -> {
                val mirroredPoints = baseStroke.points.map { pt ->
                    DrawingPoint(x = 2 * cx - pt.x, y = pt.y)
                }
                results.add(baseStroke.copy(points = mirroredPoints))
            }
            SymmetryMode.HORIZONTAL_MIRROR -> {
                val mirroredPoints = baseStroke.points.map { pt ->
                    DrawingPoint(x = pt.x, y = 2 * cy - pt.y)
                }
                results.add(baseStroke.copy(points = mirroredPoints))
            }
            SymmetryMode.RADIAL_MANDALA -> {
                // 8-fold radial symmetry
                val angleStep = (2 * Math.PI / 8).toFloat()
                for (k in 1 until 8) {
                    val angle = k * angleStep
                    val cosA = cos(angle.toDouble()).toFloat()
                    val sinA = sin(angle.toDouble()).toFloat()

                    val rotatedPoints = baseStroke.points.map { pt ->
                        val dx = pt.x - cx
                        val dy = pt.y - cy
                        val rx = dx * cosA - dy * sinA + cx
                        val ry = dx * sinA + dy * cosA + cy
                        DrawingPoint(rx, ry)
                    }
                    results.add(baseStroke.copy(points = rotatedPoints))
                }
            }
        }
        return results
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

    fun setStrokeWidth(width: Float) {
        _uiState.update { it.copy(strokeWidth = width) }
    }

    fun setStrokeAlpha(alpha: Float) {
        _uiState.update { it.copy(strokeAlpha = alpha) }
    }

    fun setBrushColor(color: Color) {
        _uiState.update { it.copy(brushColor = color) }
    }

    fun setSymmetryMode(mode: SymmetryMode) {
        _uiState.update { it.copy(symmetryMode = mode) }
    }

    fun setTransparentBackground(isTransparent: Boolean) {
        _uiState.update { it.copy(isTransparentBackground = isTransparent) }
    }

    fun clearCanvas() {
        if (_uiState.value.strokes.isNotEmpty()) {
            undoStack.push(_uiState.value.strokes)
            redoStack.clear()
            _uiState.update { it.copy(strokes = emptyList(), canUndo = true, canRedo = false) }
        }
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            redoStack.push(_uiState.value.strokes)
            val previous = undoStack.pop()
            _uiState.update {
                it.copy(
                    strokes = previous,
                    canUndo = undoStack.isNotEmpty(),
                    canRedo = true
                )
            }
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            undoStack.push(_uiState.value.strokes)
            val next = redoStack.pop()
            _uiState.update {
                it.copy(
                    strokes = next,
                    canUndo = true,
                    canRedo = redoStack.isNotEmpty()
                )
            }
        }
    }

    fun dismissExportMessage() {
        _uiState.update { it.copy(exportSuccessUri = null) }
    }

    fun dismissErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun exportDrawing() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val strokes = _uiState.value.strokes
            val isTransparent = _uiState.value.isTransparentBackground
            val bitmap = renderStrokesToBitmap(1080, 1080, strokes, isTransparent)

            val result = storageManager.exportImageToGallery(
                bitmap = bitmap,
                filename = "Kreavilo_Drawing_${System.currentTimeMillis()}",
                format = Bitmap.CompressFormat.PNG
            )

            result.onSuccess { uri ->
                _uiState.update { it.copy(isLoading = false, exportSuccessUri = uri) }
            }.onFailure { err ->
                _uiState.update { it.copy(isLoading = false, errorMessage = err.message) }
            }
        }
    }

    private suspend fun renderStrokesToBitmap(
        width: Int,
        height: Int,
        strokes: List<DrawingStroke>,
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

        for (stroke in strokes) {
            if (stroke.points.size < 2) continue
            paint.color = stroke.color.toArgb()
            paint.strokeWidth = stroke.strokeWidth * 2.5f
            paint.alpha = (stroke.alpha * 255).toInt().coerceIn(0, 255)

            val path = Path()
            path.moveTo(stroke.points[0].x * 2.5f, stroke.points[0].y * 2.5f)
            for (i in 1 until stroke.points.size) {
                path.lineTo(stroke.points[i].x * 2.5f, stroke.points[i].y * 2.5f)
            }
            canvas.drawPath(path, paint)
        }

        bitmap
    }
}
