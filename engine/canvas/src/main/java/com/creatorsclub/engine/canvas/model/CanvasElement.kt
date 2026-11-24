package com.creatorsclub.engine.canvas.model

import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Serializable

sealed interface CanvasElement {
    val id: String
    val x: Float
    val y: Float
    val scale: Float
    val rotation: Float
    val isLocked: Boolean
    val isVisible: Boolean

    fun copyWithTransform(x: Float = this.x, y: Float = this.y, scale: Float = this.scale, rotation: Float = this.rotation): CanvasElement
}

data class TextElement(
    override val id: String,
    override val x: Float,
    override val y: Float,
    override val scale: Float = 1f,
    override val rotation: Float = 0f,
    override val isLocked: Boolean = false,
    override val isVisible: Boolean = true,
    val text: String,
    val fontSizeSp: Float = 24f,
    val color: Color = Color(0xFF182033),
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val alignment: TextAlignment = TextAlignment.CENTER
) : CanvasElement {
    override fun copyWithTransform(x: Float, y: Float, scale: Float, rotation: Float): TextElement {
        return copy(x = x, y = y, scale = scale, rotation = rotation)
    }
}

data class ImageElement(
    override val id: String,
    override val x: Float,
    override val y: Float,
    override val scale: Float = 1f,
    override val rotation: Float = 0f,
    override val isLocked: Boolean = false,
    override val isVisible: Boolean = true,
    val bitmap: Bitmap? = null,
    val assetUri: String? = null,
    val width: Float = 200f,
    val height: Float = 200f,
    val opacity: Float = 1f
) : CanvasElement {
    override fun copyWithTransform(x: Float, y: Float, scale: Float, rotation: Float): ImageElement {
        return copy(x = x, y = y, scale = scale, rotation = rotation)
    }
}

enum class ShapeType {
    RECTANGLE,
    ROUNDED_RECTANGLE,
    CIRCLE,
    BADGE_STAR
}

data class ShapeElement(
    override val id: String,
    override val x: Float,
    override val y: Float,
    override val scale: Float = 1f,
    override val rotation: Float = 0f,
    override val isLocked: Boolean = false,
    override val isVisible: Boolean = true,
    val shapeType: ShapeType = ShapeType.ROUNDED_RECTANGLE,
    val width: Float = 160f,
    val height: Float = 80f,
    val fillColor: Color = Color(0xFF405BC7),
    val cornerRadius: Float = 16f
) : CanvasElement {
    override fun copyWithTransform(x: Float, y: Float, scale: Float, rotation: Float): ShapeElement {
        return copy(x = x, y = y, scale = scale, rotation = rotation)
    }
}

enum class TextAlignment {
    LEFT,
    CENTER,
    RIGHT
}
