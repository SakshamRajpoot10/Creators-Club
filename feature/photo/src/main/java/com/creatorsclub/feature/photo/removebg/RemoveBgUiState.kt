package com.creatorsclub.feature.photo.removebg

import android.graphics.Bitmap
import android.net.Uri

enum class RemoveBgTool {
    AUTO,
    ERASE,
    RESTORE,
    FEATHER
}

data class RemoveBgUiState(
    val isLoading: Boolean = false,
    val isExporting: Boolean = false,
    val sourceBitmap: Bitmap? = null,
    val alphaMaskBitmap: Bitmap? = null,
    val compositeBitmap: Bitmap? = null,
    val imageUri: Uri? = null,
    val projectId: String? = null,
    val projectName: String = "Cutout Photo",
    val isFavorite: Boolean = false,

    val activeTool: RemoveBgTool = RemoveBgTool.AUTO,
    val brushSize: Float = 36f, // 10dp to 80dp
    val featherRadius: Float = 4f, // 0 to 20 px

    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val exportSuccessUri: Uri? = null,
    val errorMessage: String? = null
)
