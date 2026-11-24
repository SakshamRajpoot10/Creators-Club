package com.creatorsclub.feature.product

import android.graphics.Bitmap
import android.net.Uri

enum class StudioBackdrop(val displayName: String, val primaryColorHex: String) {
    TRANSPARENT("Transparent PNG", "#00000000"),
    WHITE_STUDIO("Pure White Studio", "#FFFFFF"),
    SOFT_GRADIENT("Modern Blue Tint", "#E4E9FA"),
    CONCRETE_PEDESTAL("Gray Showcase", "#E5E7EB"),
    WARM_CREATIVE("Warm Peach Studio", "#FEF3C7"),
    DARK_ELEGANCE("Midnight Showcase", "#182033")
}

data class ProductStudioUiState(
    val isLoading: Boolean = false,
    val originalBitmap: Bitmap? = null,
    val cutoutBitmap: Bitmap? = null,
    val selectedBackdrop: StudioBackdrop = StudioBackdrop.WHITE_STUDIO,
    val productScale: Float = 1.0f,
    val productOffsetY: Float = 0f,
    val shadowIntensity: Float = 0.45f,
    val hasRemovedBackground: Boolean = false,
    val exportSuccessUri: Uri? = null,
    val errorMessage: String? = null
)
