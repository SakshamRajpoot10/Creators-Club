package com.creatorsclub.engine.canvas.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
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
    canvasWidthDp: Int,
    canvasHeightDp: Int,
    onSelectElement: (String?) -> Unit,
    onUpdateElementTransform: (String, Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(canvasWidthDp.dp)
            .height(canvasHeightDp.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .pointerInput(elements) {
                detectDragGestures(
                    onDragStart = { offset ->
                        // Hit-test elements from top to bottom
                        val clicked = elements.asReversed().find { el ->
                            val dx = offset.x - el.x
                            val dy = offset.y - el.y
                            // 50dp bounding touch box
                            dx in -60f..60f && dy in -60f..60f
                        }
                        onSelectElement(clicked?.id)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        selectedElementId?.let { id ->
                            val current = elements.find { it.id == id } ?: return@let
                            if (!current.isLocked) {
                                onUpdateElementTransform(id, current.x + dragAmount.x, current.y + dragAmount.y)
                            }
                        }
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            for (element in elements) {
                if (!element.isVisible) continue

                translate(left = element.x, top = element.y) {
                    rotate(degrees = element.rotation) {
                        scale(scale = element.scale) {
                            when (element) {
                                is ShapeElement -> drawShape(element)
                                is ImageElement -> drawImageElement(element)
                                is TextElement -> {
                                    // Text drawn via overlaid Compose Text or native canvas text
                                }
                            }
                        }
                    }
                }
            }
        }

        // Overlay Text Elements for crisp subpixel text rendering
        for (element in elements) {
            if (element is TextElement && element.isVisible) {
                val isSelected = element.id == selectedElementId
                Box(
                    modifier = Modifier
                        .offset { IntOffset(element.x.roundToInt() - 100, element.y.roundToInt() - 25) }
                        .width(200.dp)
                        .then(
                            if (isSelected) Modifier.border(1.dp, Color(0xFF405BC7), RoundedCornerShape(4.dp))
                            else Modifier
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = element.text,
                        fontSize = (element.fontSizeSp * 0.7f).sp, // Scaled for mobile view
                        fontWeight = if (element.isBold) FontWeight.Bold else FontWeight.Normal,
                        color = element.color
                    )
                }
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
            cornerRadius = CornerRadius(8f, 8f)
        )
    }
}

private fun DrawScope.drawImageElement(image: ImageElement) {
    image.bitmap?.let { bmp ->
        val halfW = image.width / 2f
        val halfH = image.height / 2f
        drawImage(
            image = bmp.asImageBitmap(),
            dstOffset = IntOffset((-halfW).roundToInt(), (-halfH).roundToInt()),
            dstSize = androidx.compose.ui.unit.IntSize(image.width.roundToInt(), image.height.roundToInt()),
            alpha = image.opacity
        )
    }
}
