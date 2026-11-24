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
enum class HslColorBand(val displayName: String, val colorHex: Long) {
    RED("Red", 0xFFFF3B30),
    ORANGE("Orange", 0xFFFF9500),
    YELLOW("Yellow", 0xFFFFCC00),
    GREEN("Green", 0xFF34C759),
    AQUA("Aqua", 0xFF00C7BE),
    BLUE("Blue", 0xFF007AFF),
    PURPLE("Purple", 0xFF5856D6),
    MAGENTA("Magenta", 0xFFAF52DE)
}

@Serializable
data class HslBandAdjustment(
    val hue: Float = 0f,        // -1f to 1f (-180° to +180°)
    val saturation: Float = 0f, // -1f to 1f
    val luminance: Float = 0f   // -1f to 1f
)

@Serializable
enum class CurveChannel {
    RGB, RED, GREEN, BLUE
}

@Serializable
data class CurvePoint(
    val x: Float, // 0f to 1f
    val y: Float  // 0f to 1f
)

@Serializable
data class CurvesState(
    val rgbPoints: List<CurvePoint> = listOf(CurvePoint(0f, 0f), CurvePoint(1f, 1f)),
    val redPoints: List<CurvePoint> = listOf(CurvePoint(0f, 0f), CurvePoint(1f, 1f)),
    val greenPoints: List<CurvePoint> = listOf(CurvePoint(0f, 0f), CurvePoint(1f, 1f)),
    val bluePoints: List<CurvePoint> = listOf(CurvePoint(0f, 0f), CurvePoint(1f, 1f))
) {
    fun isDefault(): Boolean {
        return rgbPoints.size == 2 && rgbPoints[0] == CurvePoint(0f, 0f) && rgbPoints[1] == CurvePoint(1f, 1f) &&
               redPoints.size == 2 && redPoints[0] == CurvePoint(0f, 0f) && redPoints[1] == CurvePoint(1f, 1f) &&
               greenPoints.size == 2 && greenPoints[0] == CurvePoint(0f, 0f) && greenPoints[1] == CurvePoint(1f, 1f) &&
               bluePoints.size == 2 && bluePoints[0] == CurvePoint(0f, 0f) && bluePoints[1] == CurvePoint(1f, 1f)
    }

    fun getPointsForChannel(channel: CurveChannel): List<CurvePoint> = when (channel) {
        CurveChannel.RGB -> rgbPoints
        CurveChannel.RED -> redPoints
        CurveChannel.GREEN -> greenPoints
        CurveChannel.BLUE -> bluePoints
    }

    fun copyWithPoints(channel: CurveChannel, points: List<CurvePoint>): CurvesState = when (channel) {
        CurveChannel.RGB -> copy(rgbPoints = points)
        CurveChannel.RED -> copy(redPoints = points)
        CurveChannel.GREEN -> copy(greenPoints = points)
        CurveChannel.BLUE -> copy(bluePoints = points)
    }
}

@Serializable
data class DetailsState(
    val sharpening: Float = 0f, // 0f to 1f
    val structure: Float = 0f   // 0f to 1f
)

@Serializable
data class CreativeEffectsState(
    val dehaze: Float = 0f,     // 0f to 1f
    val grain: Float = 0f,      // 0f to 1f
    val glow: Float = 0f,       // 0f to 1f
    val hdrStyle: Float = 0f,   // 0f to 1f
    val blurRadius: Float = 0f  // 0f to 1f
)

@Serializable
data class ImageAdjustmentState(
    val brightness: Float = 0f,       // -1f to 1f (0 = default)
    val contrast: Float = 1f,         // 0f to 2f (1 = default)
    val saturation: Float = 1f,       // 0f to 2f (1 = default)
    val exposure: Float = 0f,         // -1f to 1f (0 = default)
    val warmth: Float = 0f,           // -1f to 1f (0 = default)
    val tint: Float = 0f,             // -1f to 1f (0 = default)
    val highlights: Float = 0f,       // -1f to 1f (0 = default)
    val shadows: Float = 0f,          // -1f to 1f (0 = default)
    val vignette: Float = 0f,         // 0f to 1f (0 = default)
    val filterPreset: FilterPreset = FilterPreset.NONE,
    val filterIntensity: Float = 1.0f, // 0f to 1f (1 = 100%)
    val details: DetailsState = DetailsState(),
    val curves: CurvesState = CurvesState(),
    val hslAdjustments: Map<HslColorBand, HslBandAdjustment> = emptyMap(),
    val creative: CreativeEffectsState = CreativeEffectsState()
)

@Serializable
data class CropTransformState(
    val aspectRatio: CropAspectRatio = CropAspectRatio.ORIGINAL,
    val rotationDegrees: Float = 0f,
    val straightenDegrees: Float = 0f, // -45f to 45f
    val isFlippedHorizontal: Boolean = false,
    val isFlippedVertical: Boolean = false
)

@Serializable
enum class ExportFormat(val extension: String, val mimeType: String) {
    JPEG("jpg", "image/jpeg"),
    PNG("png", "image/png"),
    WEBP("webp", "image/webp")
}

@Serializable
enum class ExportDimension(val label: String, val maxDimension: Int?) {
    ORIGINAL("Original Resolution", null),
    UHD_4K("4K UHD (3840 px)", 3840),
    QHD_1440P("1440p QHD (2560 px)", 2560),
    FHD_1080P("1080p Full HD (1920 px)", 1920),
    SQUARE_1080("Square (1080 × 1080)", 1080)
}

@Serializable
data class ExportConfig(
    val filename: String = "Kreavilo_Photo_${System.currentTimeMillis()}",
    val format: ExportFormat = ExportFormat.JPEG,
    val quality: Int = 95, // 60..100
    val dimension: ExportDimension = ExportDimension.ORIGINAL,
    val stripLocation: Boolean = true,
    val stripAllMetadata: Boolean = false
)
