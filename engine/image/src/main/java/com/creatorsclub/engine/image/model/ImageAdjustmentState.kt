package com.creatorsclub.engine.image.model

import kotlinx.serialization.Serializable

@Serializable
enum class FilterPreset(val displayName: String, val category: String = "Essentials") {
    // Essentials
    NONE("Original", "Essentials"),
    VIVID("Vivid Color", "Essentials"),
    HDR("HDR Boost", "Essentials"),
    SOFT_GLOW("Soft Glow", "Essentials"),
    CRISP("Crisp Detail", "Essentials"),

    // Cinema
    CINEMATIC("Cinematic", "Cinema"),
    TEAL_ORANGE("Teal & Orange", "Cinema"),
    HOLLYWOOD("Hollywood 70s", "Cinema"),
    CYBERPUNK("Cyberpunk", "Cinema"),
    BLADE_RUNNER("Neon Noir", "Cinema"),
    INDIE_FILM("Indie Gold", "Cinema"),
    DOCUMENTARY("Docu Real", "Cinema"),

    // Classic & Vintage
    VINTAGE("Vintage 90s", "Classic"),
    FILM("Film 35mm", "Classic"),
    KODACHROME("Kodachrome", "Classic"),
    POLAROID("Retro Polar", "Classic"),
    BLEACH_BYPASS("Bleach Bypass", "Classic"),
    FADED("Faded Memory", "Classic"),
    SEPIA("Sepia Classic", "Classic"),

    // Monochrome
    MONOCHROME("B&W Classic", "Monochrome"),
    DRAMATIC_BW("High Contrast B&W", "Monochrome"),
    SILVER("Silver Gelatin", "Monochrome"),
    CARBON("Matte Carbon", "Monochrome"),
    MOODY("Moody Noir", "Monochrome"),
    SEPIA_BW("Warm Monochrome", "Monochrome"),

    // Creative & Mood
    WARM("Warm Sun", "Creative"),
    COOL("Cool Breeze", "Creative"),
    MATTE("Matte Fade", "Creative"),
    DRAMATIC("Dramatic Contrast", "Creative"),
    GOLDEN_HOUR("Golden Hour", "Creative"),
    PASTEL("Pastel Dream", "Creative"),
    EMERALD("Emerald Forest", "Creative"),
    SUNSET("Sunset Blush", "Creative"),
    URBAN("Urban Street", "Creative")
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
