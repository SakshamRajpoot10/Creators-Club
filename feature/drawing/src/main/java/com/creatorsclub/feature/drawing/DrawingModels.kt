package com.creatorsclub.feature.drawing

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import java.util.UUID

enum class BrushType(
    val displayName: String,
    val defaultWidth: Float,
    val defaultAlpha: Float
) {
    PENCIL("Pencil", 4f, 0.85f),
    INK_PEN("Ink Pen", 6f, 1.0f),
    AIRBRUSH("Airbrush", 28f, 0.30f),
    WATERCOLOR("Watercolor", 26f, 0.38f),
    MARKER("Marker", 18f, 0.70f),
    HIGHLIGHTER("Highlighter", 24f, 0.45f),
    HARD_ROUND("Hard Round", 10f, 1.0f),
    SOFT_ROUND("Soft Round", 16f, 0.65f),
    CALLIGRAPHY("Calligraphy", 14f, 0.95f),
    ERASER("Eraser", 24f, 1.0f)
}

enum class SymmetryMode(
    val displayName: String,
    val chipLabel: String,
    val folds: Int,
    val isRadial: Boolean
) {
    NONE("Off", "Off", 1, false),
    VERTICAL_MIRROR("Vertical Mirror", "Vertical", 2, false),
    HORIZONTAL_MIRROR("Horizontal Mirror", "Horizontal", 2, false),
    RADIAL_4X("4-Way Radial", "4x", 4, true),
    RADIAL_6X("6-Way Radial", "6x", 6, true),
    RADIAL_8X("8x Mandala", "8x", 8, true),
    RADIAL_12X("12-Way Radial", "12x", 12, true)
}

enum class DrawingShapeType(val displayName: String) {
    LINE("Line"),
    POLYLINE("Polyline"),
    FREEHAND("Freehand"),
    BEZIER_QUAD("Quad Bézier"),
    BEZIER_CUBIC("Cubic Bézier"),
    RECTANGLE("Rectangle"),
    SQUARE("Square"),
    ROUNDED_RECT("Rounded Rect"),
    CIRCLE("Circle"),
    ELLIPSE("Ellipse"),
    TRIANGLE("Triangle"),
    POLYGON("Polygon"),
    STAR("Star"),
    ARROW("Arrow")
}

data class DrawingPoint(val x: Float, val y: Float)

data class DrawingStroke(
    val id: String = UUID.randomUUID().toString(),
    val brushType: BrushType = BrushType.INK_PEN,
    val points: List<DrawingPoint>,
    val color: Color,
    val strokeWidth: Float,
    val alpha: Float,
    val isEraser: Boolean = false
)

data class DrawingShape(
    val id: String = UUID.randomUUID().toString(),
    val shapeType: DrawingShapeType,
    val points: List<DrawingPoint>,
    val strokeColor: Color,
    val fillColor: Color? = null,
    val strokeWidth: Float = 4f,
    val alpha: Float = 1f,
    val cornerRadius: Float = 16f,
    val polygonSides: Int = 6
)

data class DrawingLayer(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val isVisible: Boolean = true,
    val isLocked: Boolean = false,
    val opacity: Float = 1.0f,
    val strokes: List<DrawingStroke> = emptyList(),
    val shapes: List<DrawingShape> = emptyList()
)

enum class DrawingToolTab(val title: String) {
    BRUSHES("Brushes"),
    SHAPES("Shapes"),
    SYMMETRY("Symmetry"),
    LAYERS("Layers"),
    COLOR("Color")
}
