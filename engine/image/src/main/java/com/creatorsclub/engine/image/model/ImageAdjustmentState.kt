package com.creatorsclub.engine.image.model

import kotlinx.serialization.Serializable

@Serializable
enum class FilterPreset(val displayName: String) {
    NONE("Original"),
    CINEMATIC("Cinematic"),
    VINTAGE("Vintage"),
    MONOCHROME("B&W"),
    PORTRAIT("Portrait"),
    FILM("Film 35mm"),
    WARM("Warm Sun"),
    COOL("Cool Breeze"),
    MATTE("Matte Fade"),
    SEPIA("Sepia Classic"),
    DRAMATIC("Dramatic Contrast")
}

@Serializable
enum class CropAspectRatio(val displayName: String, val ratio: Float?) {
    ORIGINAL("Original", null),
    FREE("Freeform", null),
    SQUARE_1_1("1:1 Square", 1.0f),
    PORTRAIT_4_5("4:5 Instagram", 4f / 5f),
    STORY_9_16("9:16 Story/Reel", 9f / 16f),
    LANDSCAPE_16_9("16:9 YouTube", 16f / 9f),
    PHOTO_3_4("3:4 Photo", 3f / 4f),
    CLASSIC_2_3("2:3 Classic", 2f / 3f)
}

@Serializable
data class ImageAdjustmentState(
    val brightness: Float = 0f,       // -1f to 1f (0 = default)
    val contrast: Float = 1f,         // 0f to 2f (1 = default)
    val saturation: Float = 1f,       // 0f to 2f (1 = default)
    val exposure: Float = 0f,         // -1f to 1f (0 = default)
    val warmth: Float = 0f,           // -1f to 1f (0 = default)
    val vignette: Float = 0f,         // 0f to 1f (0 = default)
    val filterPreset: FilterPreset = FilterPreset.NONE
)

@Serializable
data class CropTransformState(
    val aspectRatio: CropAspectRatio = CropAspectRatio.ORIGINAL,
    val rotationDegrees: Float = 0f,
    val isFlippedHorizontal: Boolean = false,
    val isFlippedVertical: Boolean = false
)
