package com.creatorsclub.feature.photo

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.net.Uri
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.creatorsclub.core.database.KreaviloDatabase
import com.creatorsclub.core.database.model.ProjectEntity
import com.creatorsclub.core.storage.manager.StorageManager
import com.creatorsclub.engine.image.model.*
import com.creatorsclub.engine.image.processor.BitmapProcessor
import com.creatorsclub.engine.image.processor.HistogramProcessor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import java.util.Stack
import java.util.UUID
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class PhotoStudioViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val storageManager = StorageManager(application)
    private val db = KreaviloDatabase.getInstance(application)
    private val projectDao = db.projectDao()
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = true }

    private val _uiState = MutableStateFlow(PhotoStudioUiState())
    val uiState: StateFlow<PhotoStudioUiState> = _uiState.asStateFlow()

    private val undoStack = Stack<Pair<ImageAdjustmentState, CropTransformState>>()
    private val redoStack = Stack<Pair<ImageAdjustmentState, CropTransformState>>()
    private var snapshotBeforeTool: Pair<ImageAdjustmentState, CropTransformState>? = null
    private var histogramJob: Job? = null

    fun loadImageFromUri(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, imageUri = uri) }
            val bitmap = decodeSampledBitmapFromUri(uri)
            if (bitmap != null) {
                val thumbnail = withContext(Dispatchers.Default) {
                    runCatching { Bitmap.createScaledBitmap(bitmap, 160, 160, true) }.getOrNull()
                }
                val newProjectId = "proj_photo_${UUID.randomUUID().toString().take(8)}"
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        projectId = newProjectId,
                        projectName = "Photo ${System.currentTimeMillis() % 10000}",
                        isFavorite = false,
                        sourceBitmap = bitmap,
                        previewThumbnail = thumbnail,
                        originalWidth = bitmap.width,
                        originalHeight = bitmap.height,
                        adjustments = ImageAdjustmentState(),
                        cropTransform = CropTransformState(),
                        canUndo = false,
                        canRedo = false
                    )
                }
                undoStack.clear()
                redoStack.clear()
                calculateEstimatedFileSize()
                refreshHistogram(bitmap)

                // Durable project auto-creation on successful import
                autoCreateDurableProject(newProjectId, bitmap, thumbnail, uri)
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
        val thumbnail = runCatching { Bitmap.createScaledBitmap(bitmap, 160, 160, true) }.getOrNull()
        val newProjectId = "proj_photo_${UUID.randomUUID().toString().take(8)}"
        _uiState.update {
            it.copy(
                projectId = newProjectId,
                projectName = "Photo ${System.currentTimeMillis() % 10000}",
                isFavorite = false,
                sourceBitmap = bitmap,
                previewThumbnail = thumbnail,
                originalWidth = bitmap.width,
                originalHeight = bitmap.height,
                adjustments = ImageAdjustmentState(),
                cropTransform = CropTransformState(),
                canUndo = false,
                canRedo = false
            )
        }
        undoStack.clear()
        redoStack.clear()
        calculateEstimatedFileSize()
        refreshHistogram(bitmap)

        viewModelScope.launch {
            autoCreateDurableProject(newProjectId, bitmap, thumbnail, null)
        }
    }

    fun loadImageFromBitmap(bitmap: Bitmap) {
        setSourceBitmap(bitmap)
    }

    fun createBlankCanvas(width: Int = 1080, height: Int = 1080, color: Int = Color.WHITE) {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(color)
        setSourceBitmap(bitmap)
    }

    fun loadSampleImage() {
        val width = 1080
        val height = 1080
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint().apply { isAntiAlias = true }

        // Gradient Background
        val shader = LinearGradient(
            0f, 0f, width.toFloat(), height.toFloat(),
            intArrayOf(
                Color.parseColor("#405BC7"),
                Color.parseColor("#7664C7"),
                Color.parseColor("#EC4899")
            ),
            null,
            Shader.TileMode.CLAMP
        )
        paint.shader = shader
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

        // Center Art
        paint.shader = null
        paint.color = Color.WHITE
        paint.alpha = 40
        canvas.drawCircle(width / 2f, height / 2f, 320f, paint)

        paint.color = Color.WHITE
        paint.alpha = 70
        canvas.drawCircle(width / 2f, height / 2f, 180f, paint)

        setSourceBitmap(bitmap)
    }

    // --- Durable Project Persistence & Auto-Creation ---

    private suspend fun autoCreateDurableProject(
        projectId: String,
        bitmap: Bitmap,
        thumbnail: Bitmap?,
        sourceUri: Uri?
    ) = withContext(Dispatchers.IO) {
        runCatching {
            val projectsDir = storageManager.getProjectsDir()

            // Save durable source copy
            val sourceFile = File(projectsDir, "${projectId}_source.png")
            FileOutputStream(sourceFile).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            }

            // Save thumbnail
            val thumbFile = File(projectsDir, "${projectId}_thumb.jpg")
            if (thumbnail != null) {
                FileOutputStream(thumbFile).use { fos ->
                    thumbnail.compress(Bitmap.CompressFormat.JPEG, 85, fos)
                }
            }

            // Save recipe JSON
            val recipe = PhotoEditRecipe(
                version = 1,
                sourceUri = sourceFile.absolutePath,
                adjustments = _uiState.value.adjustments,
                cropTransform = _uiState.value.cropTransform
            )
            val recipeFile = File(projectsDir, "${projectId}_recipe.json")
            recipeFile.writeText(json.encodeToString(recipe))

            val projectEntity = ProjectEntity(
                projectId = projectId,
                name = _uiState.value.projectName,
                studioType = "PHOTO",
                createdAt = System.currentTimeMillis(),
                modifiedAt = System.currentTimeMillis(),
                canvasWidth = bitmap.width,
                canvasHeight = bitmap.height,
                aspectRatio = _uiState.value.cropTransform.aspectRatio.displayName,
                thumbnailPath = thumbFile.absolutePath,
                projectFilePath = recipeFile.absolutePath,
                isFavorite = false,
                isDeleted = false,
                deletedAt = null
            )

            projectDao.insertProject(projectEntity)
        }
    }

    fun loadExistingProject(projectId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true) }
            val project = projectDao.getProjectById(projectId)
            if (project != null) {
                val recipeFile = File(project.projectFilePath)
                val recipe: PhotoEditRecipe? = if (recipeFile.exists()) {
                    runCatching { json.decodeFromString<PhotoEditRecipe>(recipeFile.readText()) }.getOrNull()
                } else null

                val sourcePath = recipe?.sourceUri ?: project.projectFilePath
                val sourceFile = File(sourcePath)
                val bitmap = if (sourceFile.exists()) {
                    BitmapFactory.decodeFile(sourceFile.absolutePath)
                } else null

                if (bitmap != null) {
                    val thumbnail = runCatching { Bitmap.createScaledBitmap(bitmap, 160, 160, true) }.getOrNull()
                    val adjustments = recipe?.adjustments ?: ImageAdjustmentState()
                    val cropTransform = recipe?.cropTransform ?: CropTransformState()

                    withContext(Dispatchers.Main) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                projectId = project.projectId,
                                projectName = project.name,
                                isFavorite = project.isFavorite,
                                sourceBitmap = bitmap,
                                previewThumbnail = thumbnail,
                                originalWidth = bitmap.width,
                                originalHeight = bitmap.height,
                                adjustments = adjustments,
                                cropTransform = cropTransform,
                                canUndo = false,
                                canRedo = false
                            )
                        }
                        undoStack.clear()
                        redoStack.clear()
                        calculateEstimatedFileSize()
                        refreshHistogram(bitmap)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        _uiState.update { it.copy(isLoading = false, errorMessage = "Could not load project asset") }
                    }
                }
            } else {
                withContext(Dispatchers.Main) {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Project not found") }
                }
            }
        }
    }

    private fun persistAutosave() {
        val projectId = _uiState.value.projectId ?: return
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val project = projectDao.getProjectById(projectId) ?: return@launch
                val recipe = PhotoEditRecipe(
                    version = 1,
                    sourceUri = null,
                    adjustments = _uiState.value.adjustments,
                    cropTransform = _uiState.value.cropTransform
                )
                val recipeFile = File(project.projectFilePath)
                recipeFile.writeText(json.encodeToString(recipe))

                val updatedProject = project.copy(
                    modifiedAt = System.currentTimeMillis(),
                    aspectRatio = _uiState.value.cropTransform.aspectRatio.displayName
                )
                projectDao.updateProject(updatedProject)
            }
        }
    }

    fun toggleFavorite() {
        val projectId = _uiState.value.projectId ?: return
        val newFav = !_uiState.value.isFavorite
        _uiState.update { it.copy(isFavorite = newFav) }
        viewModelScope.launch(Dispatchers.IO) {
            projectDao.setFavorite(projectId, newFav)
        }
    }

    fun setShowTrashConfirmDialog(show: Boolean) {
        _uiState.update { it.copy(showTrashConfirmDialog = show) }
    }

    fun softDeleteProject() {
        val projectId = _uiState.value.projectId ?: return
        _uiState.update { it.copy(showTrashConfirmDialog = false, isProjectTrashed = true) }
        viewModelScope.launch(Dispatchers.IO) {
            projectDao.softDeleteProject(projectId)
        }
    }

    private fun refreshHistogram(bitmap: Bitmap) {
        histogramJob?.cancel()
        histogramJob = viewModelScope.launch {
            val hist = HistogramProcessor.computeHistogram(bitmap)
            _uiState.update { it.copy(histogramData = hist) }
        }
    }

    // --- Navigation & Tool Management ---

    fun selectNavTab(tab: PhotoNavTab) {
        if (tab == PhotoNavTab.EXPORT) {
            _uiState.update { it.copy(isExportSheetVisible = true) }
        } else {
            _uiState.update { it.copy(navTab = tab, isExportSheetVisible = false, activeTool = ActivePhotoTool.NONE) }
        }
    }

    fun openTool(tool: ActivePhotoTool) {
        snapshotBeforeTool = Pair(_uiState.value.adjustments, _uiState.value.cropTransform)
        _uiState.update { it.copy(activeTool = tool) }
    }

    fun closeTool(applyChanges: Boolean) {
        if (!applyChanges && snapshotBeforeTool != null) {
            val (prevAdj, prevCrop) = snapshotBeforeTool!!
            _uiState.update {
                it.copy(
                    activeTool = ActivePhotoTool.NONE,
                    adjustments = prevAdj,
                    cropTransform = prevCrop
                )
            }
        } else {
            saveStateForUndo()
            _uiState.update { it.copy(activeTool = ActivePhotoTool.NONE) }
            persistAutosave()
        }
        snapshotBeforeTool = null
    }

    fun selectTuneProperty(prop: ActiveTuneProperty) {
        _uiState.update { it.copy(activeTuneProperty = prop) }
    }

    fun selectCurveChannel(channel: CurveChannel) {
        _uiState.update { it.copy(activeCurveChannel = channel) }
    }

    fun selectHslBand(band: HslColorBand) {
        _uiState.update { it.copy(activeHslBand = band) }
    }

    fun toggleHistogram() {
        _uiState.update { it.copy(isHistogramVisible = !it.isHistogramVisible) }
    }

    // --- Filter Presets & Intensity ---

    fun applyFilter(preset: FilterPreset) {
        saveStateForUndo()
        _uiState.update {
            it.copy(adjustments = it.adjustments.copy(filterPreset = preset, filterIntensity = 1.0f))
        }
        persistAutosave()
    }

    fun setFilterIntensity(intensity: Float) {
        _uiState.update {
            it.copy(adjustments = it.adjustments.copy(filterIntensity = intensity.coerceIn(0f, 1f)))
        }
    }

    // --- Adjustments / Tune Image ---

    fun updateTuneValue(prop: ActiveTuneProperty, value: Float) {
        val adj = _uiState.value.adjustments
        val updated = when (prop) {
            ActiveTuneProperty.BRIGHTNESS -> adj.copy(brightness = value)
            ActiveTuneProperty.CONTRAST -> adj.copy(contrast = value)
            ActiveTuneProperty.SATURATION -> adj.copy(saturation = value)
            ActiveTuneProperty.EXPOSURE -> adj.copy(exposure = value)
            ActiveTuneProperty.WARMTH -> adj.copy(warmth = value)
            ActiveTuneProperty.TINT -> adj.copy(tint = value)
            ActiveTuneProperty.HIGHLIGHTS -> adj.copy(highlights = value)
            ActiveTuneProperty.SHADOWS -> adj.copy(shadows = value)
            ActiveTuneProperty.VIGNETTE -> adj.copy(vignette = value)
        }
        _uiState.update { it.copy(adjustments = updated) }
    }

    // --- Curves ---

    fun updateCurvePoints(channel: CurveChannel, points: List<CurvePoint>) {
        val curves = _uiState.value.adjustments.curves.copyWithPoints(channel, points)
        _uiState.update {
            it.copy(adjustments = it.adjustments.copy(curves = curves))
        }
    }

    fun resetCurveChannel(channel: CurveChannel) {
        val defaultPoints = listOf(CurvePoint(0f, 0f), CurvePoint(1f, 1f))
        updateCurvePoints(channel, defaultPoints)
    }

    // --- HSL Color Mix ---

    fun updateHslBand(band: HslColorBand, hue: Float? = null, saturation: Float? = null, luminance: Float? = null) {
        val currentMap = _uiState.value.adjustments.hslAdjustments.toMutableMap()
        val currentAdj = currentMap[band] ?: HslBandAdjustment()
        val updatedAdj = currentAdj.copy(
            hue = hue ?: currentAdj.hue,
            saturation = saturation ?: currentAdj.saturation,
            luminance = luminance ?: currentAdj.luminance
        )
        currentMap[band] = updatedAdj
        _uiState.update {
            it.copy(adjustments = it.adjustments.copy(hslAdjustments = currentMap))
        }
    }

    // --- Details & Sharpening ---

    fun updateDetails(sharpening: Float, structure: Float) {
        _uiState.update {
            it.copy(
                adjustments = it.adjustments.copy(
                    details = it.adjustments.details.copy(
                        sharpening = sharpening.coerceIn(0f, 1f),
                        structure = structure.coerceIn(0f, 1f)
                    )
                )
            )
        }
    }

    // --- Creative Effects ---

    fun updateCreative(dehaze: Float? = null, grain: Float? = null, glow: Float? = null, blurRadius: Float? = null) {
        val c = _uiState.value.adjustments.creative
        val updated = c.copy(
            dehaze = dehaze ?: c.dehaze,
            grain = grain ?: c.grain,
            glow = glow ?: c.glow,
            blurRadius = blurRadius ?: c.blurRadius
        )
        _uiState.update {
            it.copy(adjustments = it.adjustments.copy(creative = updated))
        }
    }

    // --- Crop & Transform ---

    fun setAspectRatio(ratio: CropAspectRatio) {
        _uiState.update {
            it.copy(
                cropTransform = it.cropTransform.copy(
                    aspectRatio = ratio,
                    cropRect = NormalizedCropRect() // Reset custom window to preset bounds
                )
            )
        }
        calculateEstimatedFileSize()
    }

    fun updateCropRect(cropRect: NormalizedCropRect) {
        _uiState.update {
            it.copy(cropTransform = it.cropTransform.copy(cropRect = cropRect))
        }
    }

    fun rotate90() {
        _uiState.update {
            val newAngle = (it.cropTransform.rotationDegrees + 90f) % 360f
            it.copy(cropTransform = it.cropTransform.copy(rotationDegrees = newAngle))
        }
    }

    fun updateStraighten(degrees: Float) {
        _uiState.update {
            it.copy(cropTransform = it.cropTransform.copy(straightenDegrees = degrees))
        }
    }

    fun flipHorizontal() {
        _uiState.update {
            it.copy(cropTransform = it.cropTransform.copy(isFlippedHorizontal = !it.cropTransform.isFlippedHorizontal))
        }
    }

    fun flipVertical() {
        _uiState.update {
            it.copy(cropTransform = it.cropTransform.copy(isFlippedVertical = !it.cropTransform.isFlippedVertical))
        }
    }

    // --- Compare & Undo/Redo ---

    fun setComparingOriginal(isComparing: Boolean) {
        _uiState.update { it.copy(isComparingOriginal = isComparing) }
    }

    private fun saveStateForUndo() {
        undoStack.push(Pair(_uiState.value.adjustments, _uiState.value.cropTransform))
        redoStack.clear()
        _uiState.update { it.copy(canUndo = true, canRedo = false) }
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            redoStack.push(Pair(_uiState.value.adjustments, _uiState.value.cropTransform))
            val (prevAdj, prevCrop) = undoStack.pop()
            _uiState.update {
                it.copy(
                    adjustments = prevAdj,
                    cropTransform = prevCrop,
                    canUndo = undoStack.isNotEmpty(),
                    canRedo = true
                )
            }
            persistAutosave()
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            undoStack.push(Pair(_uiState.value.adjustments, _uiState.value.cropTransform))
            val (nextAdj, nextCrop) = redoStack.pop()
            _uiState.update {
                it.copy(
                    adjustments = nextAdj,
                    cropTransform = nextCrop,
                    canUndo = true,
                    canRedo = redoStack.isNotEmpty()
                )
            }
            persistAutosave()
        }
    }

    // --- Export Sheet & Config ---

    fun showExportSheet() {
        calculateEstimatedFileSize()
        _uiState.update { it.copy(isExportSheetVisible = true) }
    }

    fun hideExportSheet() {
        _uiState.update { it.copy(isExportSheetVisible = false) }
    }

    fun updateExportConfig(config: ExportConfig) {
        _uiState.update { it.copy(exportConfig = config) }
        calculateEstimatedFileSize()
    }

    private fun calculateEstimatedFileSize() {
        val state = _uiState.value
        val baseW = if (state.originalWidth > 0) state.originalWidth else 1080
        val baseH = if (state.originalHeight > 0) state.originalHeight else 1080

        val maxDim = state.exportConfig.dimension.maxDimension
        val (finalW, finalH) = if (maxDim != null) {
            val scale = maxDim.toFloat() / maxOf(baseW, baseH).toFloat().coerceAtLeast(1f)
            Pair((baseW * scale).roundToInt(), (baseH * scale).roundToInt())
        } else {
            Pair(baseW, baseH)
        }

        val totalPixels = finalW.toLong() * finalH.toLong()
        val quality = state.exportConfig.quality.toFloat() / 100f

        val estimatedBytes = when (state.exportConfig.format) {
            ExportFormat.PNG -> (totalPixels * 1.8).toLong()
            ExportFormat.WEBP -> (totalPixels * 0.12 * quality).toLong()
            ExportFormat.JPEG -> (totalPixels * 0.22 * quality).toLong()
        }

        _uiState.update { it.copy(estimatedFileSizeKb = (estimatedBytes / 1024L).coerceAtLeast(50L)) }
    }

    fun exportImage() {
        val bitmap = _uiState.value.sourceBitmap ?: return
        val config = _uiState.value.exportConfig

        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, isExportSheetVisible = false) }

            // Re-render from source bitmap using full resolution non-destructive pipeline
            val rendered = BitmapProcessor.processAndRender(
                sourceBitmap = bitmap,
                adjustmentState = _uiState.value.adjustments,
                cropTransformState = _uiState.value.cropTransform,
                targetDimension = config.dimension
            )

            val compressFormat = when (config.format) {
                ExportFormat.PNG -> Bitmap.CompressFormat.PNG
                ExportFormat.WEBP -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    Bitmap.CompressFormat.WEBP_LOSSY
                } else {
                    @Suppress("DEPRECATION")
                    Bitmap.CompressFormat.WEBP
                }
                ExportFormat.JPEG -> Bitmap.CompressFormat.JPEG
            }

            val exportResult = storageManager.exportImageToGallery(
                bitmap = rendered,
                filename = config.filename.ifBlank { "Kreavilo_Photo_${System.currentTimeMillis()}" },
                format = compressFormat,
                quality = config.quality
            )

            exportResult.onSuccess { uri ->
                _uiState.update {
                    it.copy(
                        isExporting = false,
                        exportSuccessUri = uri
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isExporting = false,
                        errorMessage = error.message ?: "Failed to export image"
                    )
                }
            }
        }
    }

    fun dismissExportMessage() {
        _uiState.update { it.copy(exportSuccessUri = null) }
    }

    fun dismissErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private suspend fun decodeSampledBitmapFromUri(uri: Uri): Bitmap? = withContext(Dispatchers.IO) {
        runCatching {
            val context = getApplication<Application>()
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeStream(stream, null, options)

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
