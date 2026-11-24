package com.creatorsclub.engine.canvas.model

import kotlinx.serialization.Serializable

@Serializable
enum class TemplateCategory(val displayName: String) {
    INSTAGRAM_POST("Instagram Post (1:1)"),
    INSTAGRAM_STORY("Instagram Story (9:16)"),
    YOUTUBE_THUMBNAIL("YouTube Thumbnail (16:9)"),
    SALE_BANNER("Sale & Offer Banner"),
    BUSINESS_FLYER("Flyer / Poster")
}

@Serializable
data class DesignTemplate(
    val templateId: String,
    val title: String,
    val category: TemplateCategory,
    val canvasWidth: Int,
    val canvasHeight: Int,
    val backgroundColorHex: String,
    val previewThumbnailResName: String? = null,
    val rawTextElements: List<TemplateTextSpec> = emptyList(),
    val rawShapeElements: List<TemplateShapeSpec> = emptyList()
)

@Serializable
data class TemplateTextSpec(
    val id: String,
    val text: String,
    val x: Float,
    val y: Float,
    val fontSizeSp: Float,
    val colorHex: String,
    val isBold: Boolean = false,
    val isItalic: Boolean = false
)

@Serializable
data class TemplateShapeSpec(
    val id: String,
    val shapeType: String,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val fillColorHex: String,
    val cornerRadius: Float = 12f
)
