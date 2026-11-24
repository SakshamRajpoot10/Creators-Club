package com.creatorsclub.feature.photo

import android.graphics.Bitmap
import android.net.Uri
import com.creatorsclub.engine.image.model.CropAspectRatio
import com.creatorsclub.engine.image.model.CropTransformState
import com.creatorsclub.engine.image.model.CurveChannel
import com.creatorsclub.engine.image.model.ExportConfig
import com.creatorsclub.engine.image.model.FilterPreset
import com.creatorsclub.engine.image.model.HslColorBand
import com.creatorsclub.engine.image.model.ImageAdjustmentState
import com.creatorsclub.engine.image.processor.HistogramData

enum class PhotoNavTab(val title: String) {
    LOOKS("Looks"),
    TOOLS("Tools"),
    EXPORT("Export")
}

enum class ActivePhotoTool(val displayName: String, val category: String) {
    NONE("None", "None"),
    TUNE("Tune Image", "Essentials"),
    CURVES("Curves", "Essentials"),
    HSL("HSL Mix", "Color"),
    WHITE_BALANCE("White Balance", "Color"),
    CROP("Crop & Rotate", "Essentials"),
    DETAILS("Details", "Essentials"),
    CREATIVE("Creative", "Creative"),
    HISTOGRAM("Histogram", "Advanced")
}

enum class ActiveTuneProperty(val label: String, val minVal: Float, val maxVal: Float, val defaultVal: Float) {
    BRIGHTNESS("Brightness", -1f, 1f, 0f),
    CONTRAST("Contrast", 0f, 2f, 1f),
    SATURATION("Saturation", 0f, 2f, 1f),
    EXPOSURE("Exposure", -1f, 1f, 0f),
    WARMTH("Warmth", -1f, 1f, 0f),
    TINT("Tint", -1f, 1f, 0f),
    HIGHLIGHTS("Highlights", -1f, 1f, 0f),
    SHADOWS("Shadows", -1f, 1f, 0f),
    VIGNETTE("Vignette", 0f, 1f, 0f)
}

data class PhotoStudioUiState(
    val isLoading: Boolean = false,
    val isExporting: Boolean = false,
    val sourceBitmap: Bitmap? = null,
    val previewThumbnail: Bitmap? = null,
    val imageUri: Uri? = null,
    val originalWidth: Int = 0,
    val originalHeight: Int = 0,

    // Navigation & Tool State
    val navTab: PhotoNavTab = PhotoNavTab.LOOKS,
    val activeTool: ActivePhotoTool = ActivePhotoTool.NONE,
    val activeTuneProperty: ActiveTuneProperty = ActiveTuneProperty.BRIGHTNESS,
    val activeCurveChannel: CurveChannel = CurveChannel.RGB,
    val activeHslBand: HslColorBand = HslColorBand.RED,

    // Image Edits
    val adjustments: ImageAdjustmentState = ImageAdjustmentState(),
    val cropTransform: CropTransformState = CropTransformState(),

    // Live Histogram & Analytics
    val histogramData: HistogramData? = null,
    val isHistogramVisible: Boolean = false,

    // Compare & Undo/Redo
    val isComparingOriginal: Boolean = false,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,

    // Export Flow
    val isExportSheetVisible: Boolean = false,
    val exportConfig: ExportConfig = ExportConfig(),
    val estimatedFileSizeKb: Long = 0L,
    val exportSuccessUri: Uri? = null,
    val errorMessage: String? = null
)
