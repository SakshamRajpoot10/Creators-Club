package com.creatorsclub.engine.canvas.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.creatorsclub.engine.canvas.model.CanvasElement
import com.creatorsclub.engine.canvas.model.ImageElement
import com.creatorsclub.engine.canvas.model.ShapeElement
import com.creatorsclub.engine.canvas.model.ShapeType
import com.creatorsclub.engine.canvas.model.TextElement
import kotlin.math.roundToInt

@Composable
fun InteractiveDesignCanvas(
    elements: List<CanvasElement>,
    selectedElementId: String?,
    backgroundColor: Color,
    documentWidth: Int,
    documentHeight: Int,
    onSelectElement: (String?) -> Unit,
    onDoubleTapElement: (String) -> Unit = {},
    onUpdateElementTransform: (String, Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        val availableWidth = constraints.maxWidth.toFloat().coerceAtLeast(100f)
        val availableHeight = constraints.maxHeight.toFloat().coerceAtLeast(100f)

        // Viewport scale that fits document completely with 8% breathing margin
        val viewportScale = minOf(
            availableWidth / documentWidth.toFloat(),
            availableHeight / documentHeight.toFloat()
        ) * 0.92f

        val canvasPixelWidth = documentWidth * viewportScale
        val canvasPixelHeight = documentHeight * viewportScale
        val originX = (availableWidth - canvasPixelWidth) / 2f
        val originY = (availableHeight - canvasPixelHeight) / 2f

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(elements, viewportScale, originX, originY) {
                    detectTapGestures(
                        onDoubleTap = { screenOffset ->
                            val docX = (screenOffset.x - originX) / viewportScale
                            val docY = (screenOffset.y - originY) / viewportScale
                            val clicked = hitTestElement(elements, docX, docY)
                            if (clicked != null) {
                                onSelectElement(clicked.id)
                                onDoubleTapElement(clicked.id)
                            }
                        },
                        onTap = { screenOffset ->
                            val docX = (screenOffset.x - originX) / viewportScale
                            val docY = (screenOffset.y - originY) / viewportScale
                            val clicked = hitTestElement(elements, docX, docY)
                            onSelectElement(clicked?.id)
                        }
                    )
                }
                .pointerInput(elements, selectedElementId, viewportScale, originX, originY) {
                    detectDragGestures(
                        onDragStart = { screenOffset ->
                            val docX = (screenOffset.x - originX) / viewportScale
                            val docY = (screenOffset.y - originY) / viewportScale
                            val clicked = hitTestElement(elements, docX, docY)
                            if (clicked != null) {
                                onSelectElement(clicked.id)
                            }
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            selectedElementId?.let { id ->
                                val current = elements.find { it.id == id } ?: return@let
                                if (!current.isLocked) {
                                    // Inverse viewport matrix mapping: screen px -> doc px
                                    val docDeltaX = dragAmount.x / viewportScale
                                    val docDeltaY = dragAmount.y / viewportScale
                                    onUpdateElementTransform(id, current.x + docDeltaX, current.y + docDeltaY)
                                }
                            }
                        }
                    )
                }
        ) {
            // Native Compose Canvas
            Canvas(modifier = Modifier.fillMaxSize()) {
                // 1. Draw Document Canvas Boundary & Drop Shadow
                drawRect(
                    color = Color(0x33000000),
                    topLeft = Offset(originX + 4f, originY + 4f),
                    size = Size(canvasPixelWidth, canvasPixelHeight)
                )
                drawRect(
                    color = backgroundColor,
                    topLeft = Offset(originX, originY),
                    size = Size(canvasPixelWidth, canvasPixelHeight)
                )
                drawRect(
                    color = Color(0xFF333A4D),
                    topLeft = Offset(originX, originY),
                    size = Size(canvasPixelWidth, canvasPixelHeight),
                    style = Stroke(width = 2f)
                )

                // 2. Render all document elements with Viewport Transform
                translate(left = originX, top = originY) {
                    scale(scaleX = viewportScale, scaleY = viewportScale, pivot = Offset.Zero) {
                        for (element in elements) {
                            if (!element.isVisible) continue

                            translate(left = element.x, top = element.y) {
                                rotate(degrees = element.rotation) {
                                    scale(scale = element.scale) {
                                        when (element) {
                                            is ShapeElement -> drawShape(element)
                                            is ImageElement -> drawImageElement(element)
                                            is TextElement -> {
                                                // Text is rendered via overlaid composable for crisp anti-aliasing
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 3. Render Selection Bounding Box & Handles for Selected Element
                selectedElementId?.let { selId ->
                    val sel = elements.find { it.id == selId }
                    if (sel != null && sel.isVisible) {
                        val selScreenX = originX + sel.x * viewportScale
                        val selScreenY = originY + sel.y * viewportScale

                        val (w, h) = when (sel) {
                            is ShapeElement -> Pair(sel.width * viewportScale, sel.height * viewportScale)
                            is ImageElement -> Pair(sel.width * viewportScale, sel.height * viewportScale)
                            is TextElement -> Pair(
                                (sel.text.length * sel.fontSizeSp * 0.7f * viewportScale).coerceAtLeast(80f * viewportScale),
                                (sel.fontSizeSp * 1.5f * viewportScale).coerceAtLeast(30f * viewportScale)
                            )
                        }

                        val halfW = w / 2f
                        val halfH = h / 2f
                        val topLeft = Offset(selScreenX - halfW - 4f, selScreenY - halfH - 4f)
                        val boxSize = Size(w + 8f, h + 8f)

                        // Selection outline
                        drawRoundRect(
                            color = Color(0xFF405BC7),
                            topLeft = topLeft,
                            size = boxSize,
                            cornerRadius = CornerRadius(4f, 4f),
                            style = Stroke(width = 2f)
                        )

                        // Corner handles
                        val handleRadius = 5f
                        val handleColor = Color(0xFF405BC7)
                        drawCircle(handleColor, handleRadius, Offset(topLeft.x, topLeft.y))
                        drawCircle(handleColor, handleRadius, Offset(topLeft.x + boxSize.width, topLeft.y))
                        drawCircle(handleColor, handleRadius, Offset(topLeft.x, topLeft.y + boxSize.height))
                        drawCircle(handleColor, handleRadius, Offset(topLeft.x + boxSize.width, topLeft.y + boxSize.height))
                    }
                }
            }

            // Overlaid Text composables for crisp high-dpi subpixel text rendering
            for (element in elements) {
                if (element is TextElement && element.isVisible) {
                    val isSelected = element.id == selectedElementId
                    val textScreenX = originX + element.x * viewportScale
                    val textScreenY = originY + element.y * viewportScale

                    val scaledFontSize = (element.fontSizeSp * viewportScale).coerceAtLeast(8f)

                    Box(
                        modifier = Modifier
                            .offset {
                                IntOffset(
                                    x = (textScreenX - 300).roundToInt(),
                                    y = (textScreenY - 20).roundToInt()
                                )
                            }
                            .width(600.dp)
                            .wrapContentHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = element.text,
                            fontSize = scaledFontSize.sp,
                            fontWeight = if (element.isBold) FontWeight.Bold else FontWeight.Normal,
                            fontStyle = if (element.isItalic) androidx.compose.ui.text.font.FontStyle.Italic else androidx.compose.ui.text.font.FontStyle.Normal,
                            color = element.color,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

private fun hitTestElement(elements: List<CanvasElement>, docX: Float, docY: Float): CanvasElement? {
    return elements.asReversed().find { el ->
        if (!el.isVisible) return@find false
        when (el) {
            is ShapeElement -> {
                val halfW = el.width / 2f
                val halfH = el.height / 2f
                docX in (el.x - halfW)..(el.x + halfW) && docY in (el.y - halfH)..(el.y + halfH)
            }
            is ImageElement -> {
                val halfW = el.width / 2f
                val halfH = el.height / 2f
                docX in (el.x - halfW)..(el.x + halfW) && docY in (el.y - halfH)..(el.y + halfH)
            }
            is TextElement -> {
                val approxHalfW = maxOf(60f, el.text.length * el.fontSizeSp * 0.35f)
                val approxHalfH = maxOf(25f, el.fontSizeSp * 0.9f)
                docX in (el.x - approxHalfW)..(el.x + approxHalfW) && docY in (el.y - approxHalfH)..(el.y + approxHalfH)
            }
        }
    }
}

private fun DrawScope.drawShape(shape: ShapeElement) {
    val halfW = shape.width / 2f
    val halfH = shape.height / 2f
    when (shape.shapeType) {
        ShapeType.ROUNDED_RECTANGLE -> drawRoundRect(
            color = shape.fillColor,
            topLeft = Offset(-halfW, -halfH),
            size = Size(shape.width, shape.height),
            cornerRadius = CornerRadius(shape.cornerRadius, shape.cornerRadius)
        )
        ShapeType.RECTANGLE -> drawRect(
            color = shape.fillColor,
            topLeft = Offset(-halfW, -halfH),
            size = Size(shape.width, shape.height)
        )
        ShapeType.CIRCLE -> drawCircle(
            color = shape.fillColor,
            radius = minOf(halfW, halfH),
            center = Offset.Zero
        )
        ShapeType.BADGE_STAR -> drawRoundRect(
            color = shape.fillColor,
            topLeft = Offset(-halfW, -halfH),
            size = Size(shape.width, shape.height),
            cornerRadius = CornerRadius(12f, 12f)
        )
    }
}

private fun DrawScope.drawImageElement(image: ImageElement) {
    image.bitmap?.let { bmp ->
        val halfW = image.width / 2f
        val halfH = image.height / 2f
        drawImage(
            image = bmp.asImageBitmap(),
            topLeft = Offset(-halfW, -halfH),
            alpha = image.opacity
        )
    }
}
