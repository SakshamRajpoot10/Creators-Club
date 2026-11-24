package com.creatorsclub.feature.drawing

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

enum class BrushType(val displayName: String, val defaultWidth: Float, val defaultAlpha: Float) {
    PENCIL("Pencil", 4f, 0.9f),
    INK_PEN("Ink Pen", 6f, 1f),
    AIRBRUSH("Airbrush", 24f, 0.35f),
    WATERCOLOR("Watercolor", 32f, 0.45f),
    ERASER("Eraser", 20f, 1f)
}

enum class SymmetryMode(val displayName: String, val folds: Int) {
    NONE("Normal", 1),
    VERTICAL_MIRROR("Vertical Mirror", 2),
    HORIZONTAL_MIRROR("Horizontal Mirror", 2),
    RADIAL_MANDALA("Mandala / Rangoli (8x)", 8)
}

data class DrawingPoint(val x: Float, val y: Float)

data class DrawingStroke(
    val points: List<DrawingPoint>,
    val color: Color,
    val strokeWidth: Float,
    val alpha: Float,
    val isEraser: Boolean = false
)
