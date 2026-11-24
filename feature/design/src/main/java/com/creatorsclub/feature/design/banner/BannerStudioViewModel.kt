package com.creatorsclub.feature.design.banner

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.creatorsclub.core.database.KreaviloDatabase
import com.creatorsclub.core.database.model.ProjectEntity
import com.creatorsclub.core.storage.manager.StorageManager
import com.creatorsclub.engine.canvas.model.CanvasElement
import com.creatorsclub.engine.canvas.model.ImageElement
import com.creatorsclub.engine.canvas.model.ShapeElement
import com.creatorsclub.engine.canvas.model.ShapeType
import com.creatorsclub.engine.canvas.model.TextElement
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
class BannerStudioViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val db = KreaviloDatabase.getInstance(application)
    private val projectDao = db.projectDao()
    private val storageManager = StorageManager(application)

    private val _uiState = MutableStateFlow(BannerStudioUiState())
    val uiState: StateFlow<BannerStudioUiState> = _uiState.asStateFlow()

    private val undoStack = Stack<List<CanvasElement>>()
    private val redoStack = Stack<List<CanvasElement>>()

    init {
        loadDefaultPreset(BannerPreset.YOUTUBE_THUMBNAIL)
        refreshImportedAssets()
    }

    fun refreshImportedAssets() {
        val banners = storageManager.listImportedAssets("banners")
        _uiState.update { it.copy(importedBanners = banners) }
    }

    fun selectPreset(preset: BannerPreset) {
        saveForUndo()
        _uiState.update {
            it.copy(
                activePreset = preset,
                elements = emptyList(),
                selectedElementId = null
            )
        }
        loadDefaultPreset(preset)
    }

    private fun loadDefaultPreset(preset: BannerPreset) {
        val defaultElements = mutableListOf<CanvasElement>()
        when (preset) {
            BannerPreset.YOUTUBE_THUMBNAIL -> {
                // High-CTR YouTube style title & badge
                defaultElements.add(
                    ShapeElement(
                        id = "badge_bg_${UUID.randomUUID().toString().take(6)}",
                        x = 40f,
                        y = 40f,
                        width = 110f,
                        height = 36f,
                        fillColor = Color(0xFFEF4444),
                        shapeType = ShapeType.ROUNDED_RECTANGLE,
                        cornerRadius = 8f
                    )
                )
                defaultElements.add(
                    TextElement(
                        id = "badge_txt_${UUID.randomUUID().toString().take(6)}",
                        text = "NEW VIDEO",
                        x = 48f,
                        y = 48f,
                        fontSizeSp = 12f,
                        color = Color.White,
                        isBold = true
                    )
                )
                defaultElements.add(
                    TextElement(
                        id = "title_${UUID.randomUUID().toString().take(6)}",
                        text = "HOW TO CREATE VIRAL CONTENT",
                        x = 40f,
                        y = 90f,
                        fontSizeSp = 22f,
                        color = Color(0xFFFACC15), // High contrast YouTube yellow
                        isBold = true
                    )
                )
            }
            BannerPreset.YOUTUBE_BANNER -> {
                // Channel Banner safe-area center title
                defaultElements.add(
                    TextElement(
                        id = "channel_title_${UUID.randomUUID().toString().take(6)}",
                        text = "CREATORS CLUB",
                        x = 90f,
                        y = 90f,
                        fontSizeSp = 24f,
                        color = Color.White,
                        isBold = true
                    )
                )
                defaultElements.add(
                    TextElement(
                        id = "channel_tagline_${UUID.randomUUID().toString().take(6)}",
                        text = "NEW VIDEOS EVERY WEEK • SUBSCRIBE",
                        x = 90f,
                        y = 125f,
                        fontSizeSp = 11f,
                        color = Color(0xFF94A3B8),
                        isBold = false
                    )
                )
            }
            else -> {
                defaultElements.add(
                    TextElement(
                        id = "title_${UUID.randomUUID().toString().take(6)}",
                        text = preset.title.uppercase(),
                        x = 60f,
                        y = 80f,
                        fontSizeSp = 20f,
                        color = Color.White,
                        isBold = true
                    )
                )
            }
        }
        _uiState.update { it.copy(elements = defaultElements) }
    }

    fun toggleSafeZoneGuides() {
        _uiState.update { it.copy(showSafeZoneGuides = !it.showSafeZoneGuides) }
    }

    fun selectTab(tab: BannerTab) {
        _uiState.update { it.copy(activeTab = tab) }
    }

    fun selectElement(elementId: String?) {
        _uiState.update { it.copy(selectedElementId = elementId) }
    }

    fun setBackgroundColor(color: Color) {
        _uiState.update { it.copy(backgroundColor = color) }
    }

    fun addText(text: String, color: Color = Color.White, isBold: Boolean = true) {
        saveForUndo()
        val textEl = TextElement(
            id = "txt_${UUID.randomUUID().toString().take(6)}",
            text = text,
            x = 80f,
            y = 100f,
            fontSizeSp = 22f,
            color = color,
            isBold = isBold
        )
        _uiState.update { it.copy(elements = it.elements + textEl, selectedElementId = textEl.id) }
    }

    fun addCtaBadge(text: String, bgColor: Color = Color(0xFFDC2626)) {
        saveForUndo()
        val shape = ShapeElement(
            id = "shape_${UUID.randomUUID().toString().take(6)}",
            x = 50f,
            y = 50f,
            width = 120f,
            height = 40f,
            fillColor = bgColor,
            shapeType = ShapeType.ROUNDED_RECTANGLE,
            cornerRadius = 8f
        )
        val label = TextElement(
            id = "txt_${UUID.randomUUID().toString().take(6)}",
            text = text,
            x = 58f,
            y = 58f,
            fontSizeSp = 13f,
            color = Color.White,
            isBold = true
        )
        _uiState.update { it.copy(elements = it.elements + shape + label, selectedElementId = label.id) }
    }

    fun addImage(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val inputStream = getApplication<Application>().contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                if (bitmap != null) {
                    saveForUndo()
                    val imgEl = ImageElement(
                        id = "img_${UUID.randomUUID().toString().take(6)}",
                        x = 40f,
                        y = 40f,
                        width = 160f,
                        height = 160f * (bitmap.height.toFloat() / bitmap.width.toFloat().coerceAtLeast(1f)),
                        bitmap = bitmap,
                        assetUri = uri.toString()
                    )
                    _uiState.update { it.copy(elements = it.elements + imgEl, selectedElementId = imgEl.id) }
                }
            }
        }
    }

    fun updateElementPosition(elementId: String, newX: Float, newY: Float) {
        _uiState.update { state ->
            val updated = state.elements.map { el ->
                if (el.id == elementId) el.copyWithTransform(x = newX, y = newY) else el
            }
            state.copy(elements = updated)
        }
    }

    fun deleteSelectedElement() {
        val selectedId = _uiState.value.selectedElementId ?: return
        saveForUndo()
        _uiState.update { state ->
            state.copy(
                elements = state.elements.filter { it.id != selectedId },
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
            val prev = undoStack.pop()
            _uiState.update {
                it.copy(
                    elements = prev,
                    canUndo = undoStack.isNotEmpty(),
                    canRedo = true,
                    selectedElementId = null
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
                    canUndo = true,
                    canRedo = redoStack.isNotEmpty(),
                    selectedElementId = null
                )
            }
        }
    }

    fun exportBanner() {
        val state = _uiState.value
        _uiState.update { it.copy(isLoading = true, statusMessage = "Rendering ${state.activePreset.title} at ${state.activePreset.width}x${state.activePreset.height}...") }

        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val targetWidth = state.activePreset.width
                val targetHeight = state.activePreset.height
                val renderBitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(renderBitmap)

                // Background
                canvas.drawColor(state.backgroundColor.toArgb())

                // Scale factor from preview coordinate space (normalized ~360dp width)
                val scaleFactor = targetWidth / 340f

                val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    isSubpixelText = true
                }
                val shapePaint = Paint(Paint.ANTI_ALIAS_FLAG)

                for (el in state.elements) {
                    if (!el.isVisible) continue
                    when (el) {
                        is ShapeElement -> {
                            shapePaint.color = el.fillColor.toArgb()
                            val left = el.x * scaleFactor
                            val top = el.y * scaleFactor
                            val right = left + (el.width * scaleFactor)
                            val bottom = top + (el.height * scaleFactor)
                            canvas.drawRoundRect(
                                RectF(left, top, right, bottom),
                                el.cornerRadius * scaleFactor,
                                el.cornerRadius * scaleFactor,
                                shapePaint
                            )
                        }
                        is TextElement -> {
                            textPaint.color = el.color.toArgb()
                            textPaint.textSize = el.fontSizeSp * scaleFactor
                            textPaint.isFakeBoldText = el.isBold
                            canvas.drawText(
                                el.text,
                                el.x * scaleFactor,
                                (el.y + (el.fontSizeSp * 0.9f)) * scaleFactor,
                                textPaint
                            )
                        }
                        is ImageElement -> {
                            el.bitmap?.let { bmp ->
                                val left = el.x * scaleFactor
                                val top = el.y * scaleFactor
                                val dst = RectF(left, top, left + (el.width * scaleFactor), top + (el.height * scaleFactor))
                                canvas.drawBitmap(bmp, null, dst, null)
                            }
                        }
                    }
                }

                // Save to file
                val filename = "Banner_${state.activePreset.name}_${System.currentTimeMillis()}.png"
                val outFile = File(storageManager.getProjectsDir(), filename)
                FileOutputStream(outFile).use { fos ->
                    renderBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                }

                // Register in DB
                val projectId = "proj_banner_${UUID.randomUUID().toString().take(8)}"
                projectDao.insertProject(
                    ProjectEntity(
                        projectId = projectId,
                        name = "${state.activePreset.title} - ${System.currentTimeMillis() % 10000}",
                        studioType = "DESIGN",
                        createdAt = System.currentTimeMillis(),
                        modifiedAt = System.currentTimeMillis(),
                        canvasWidth = targetWidth,
                        canvasHeight = targetHeight,
                        aspectRatio = state.activePreset.aspectRatioLabel,
                        thumbnailPath = outFile.absolutePath,
                        projectFilePath = outFile.absolutePath
                    )
                )

                renderBitmap.recycle()
                outFile
            }.onSuccess { file ->
                withContext(Dispatchers.Main) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            statusMessage = "Exported successfully to ${file.name}!",
                            exportSuccessUri = Uri.fromFile(file)
                        )
                    }
                }
            }.onFailure { err ->
                withContext(Dispatchers.Main) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Export failed: ${err.message}"
                        )
                    }
                }
            }
        }
    }

    fun dismissFeedback() {
        _uiState.update { it.copy(statusMessage = null, errorMessage = null) }
    }
}
