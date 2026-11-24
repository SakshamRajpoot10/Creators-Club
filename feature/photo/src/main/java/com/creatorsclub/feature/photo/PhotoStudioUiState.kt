package com.creatorsclub.feature.photo

import android.graphics.Bitmap
import android.net.Uri
import com.creatorsclub.engine.image.model.CropAspectRatio
import com.creatorsclub.engine.image.model.CropTransformState
import com.creatorsclub.engine.image.model.FilterPreset
import com.creatorsclub.engine.image.model.ImageAdjustmentState

enum class PhotoEditorTab {
    FILTERS,
    ADJUSTMENTS,
    CROP
}

enum class ActiveAdjustmentTool {
    BRIGHTNESS,
    CONTRAST,
    SATURATION,
    EXPOSURE,
    WARMTH
}

data class PhotoStudioUiState(
    val isLoading: Boolean = false,
    val sourceBitmap: Bitmap? = null,
    val imageUri: Uri? = null,
    val activeTab: PhotoEditorTab = PhotoEditorTab.FILTERS,
    val activeAdjustmentTool: ActiveAdjustmentTool = ActiveAdjustmentTool.BRIGHTNESS,
    val adjustments: ImageAdjustmentState = ImageAdjustmentState(),
    val cropTransform: CropTransformState = CropTransformState(),
    val isComparingOriginal: Boolean = false,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val exportSuccessUri: Uri? = null,
    val errorMessage: String? = null
)
