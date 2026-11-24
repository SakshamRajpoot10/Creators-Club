package com.creatorsclub.engine.canvas

import androidx.compose.ui.graphics.Color
import com.creatorsclub.engine.canvas.model.ShapeElement
import com.creatorsclub.engine.canvas.model.ShapeType
import com.creatorsclub.engine.canvas.model.TextAlignment
import com.creatorsclub.engine.canvas.model.TextElement
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CanvasElementTest {

    @Test
    fun textElement_transformsProperly() {
        val initial = TextElement(
            id = "txt_1",
            x = 50f,
            y = 100f,
            text = "Create without limits",
            fontSizeSp = 28f,
            color = Color(0xFF405BC7),
            alignment = TextAlignment.CENTER
        )

        val transformed = initial.copyWithTransform(x = 150f, y = 250f, scale = 1.5f, rotation = 45f)
        assertEquals(150f, transformed.x)
        assertEquals(250f, transformed.y)
        assertEquals(1.5f, transformed.scale)
        assertEquals(45f, transformed.rotation)
        assertEquals("Create without limits", transformed.text)
        assertEquals(TextAlignment.CENTER, transformed.alignment)
    }

    @Test
    fun shapeElement_transformsAndRetainsProperties() {
        val shape = ShapeElement(
            id = "shape_badge",
            x = 200f,
            y = 300f,
            shapeType = ShapeType.ROUNDED_RECTANGLE,
            fillColor = Color(0xFFF7F8FC),
            width = 120f,
            height = 60f,
            cornerRadius = 16f
        )

        assertEquals(ShapeType.ROUNDED_RECTANGLE, shape.shapeType)
        assertEquals(16f, shape.cornerRadius)
        assertFalse(shape.isLocked)
        assertTrue(shape.isVisible)

        val moved = shape.copyWithTransform(x = 300f, y = 400f)
        assertEquals(300f, moved.x)
        assertEquals(400f, moved.y)
        assertEquals(shape.cornerRadius, moved.cornerRadius)
    }
}
