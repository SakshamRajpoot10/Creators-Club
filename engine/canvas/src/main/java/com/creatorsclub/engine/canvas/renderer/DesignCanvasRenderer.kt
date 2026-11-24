package com.creatorsclub.engine.canvas.renderer

import android.graphics.*
import androidx.compose.ui.graphics.toArgb
import com.creatorsclub.engine.canvas.model.CanvasElement
import com.creatorsclub.engine.canvas.model.ImageElement
import com.creatorsclub.engine.canvas.model.ShapeElement
import com.creatorsclub.engine.canvas.model.ShapeType
import com.creatorsclub.engine.canvas.model.TextAlignment
import com.creatorsclub.engine.canvas.model.TextElement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object DesignCanvasRenderer {

    /**
     * Renders all vector, text, and image elements onto a full-resolution bitmap.
     */
    suspend fun renderToBitmap(
        width: Int,
        height: Int,
        backgroundColor: Int,
        elements: List<CanvasElement>
    ): Bitmap = withContext(Dispatchers.Default) {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background
        canvas.drawColor(backgroundColor)

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 32f
        }
        val shapePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        val bitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

        for (element in elements) {
            if (!element.isVisible) continue

            canvas.save()
            canvas.translate(element.x, element.y)
            if (element.rotation != 0f) {
                canvas.rotate(element.rotation)
            }
            if (element.scale != 1f) {
                canvas.scale(element.scale, element.scale)
            }

            when (element) {
                is TextElement -> {
                    textPaint.apply {
                        textSize = element.fontSizeSp * 2.5f // Scale to pixel canvas
                        color = element.color.toArgb()
                        typeface = Typeface.create(
                            Typeface.DEFAULT,
                            when {
                                element.isBold && element.isItalic -> Typeface.BOLD_ITALIC
                                element.isBold -> Typeface.BOLD
                                element.isItalic -> Typeface.ITALIC
                                else -> Typeface.NORMAL
                            }
                        )
                        textAlign = when (element.alignment) {
                            TextAlignment.LEFT -> Paint.Align.LEFT
                            TextAlignment.CENTER -> Paint.Align.CENTER
                            TextAlignment.RIGHT -> Paint.Align.RIGHT
                        }
                    }
                    canvas.drawText(element.text, 0f, 0f, textPaint)
                }

                is ShapeElement -> {
                    shapePaint.color = element.fillColor.toArgb()
                    val rect = RectF(-element.width / 2f, -element.height / 2f, element.width / 2f, element.height / 2f)
                    when (element.shapeType) {
                        ShapeType.ROUNDED_RECTANGLE -> canvas.drawRoundRect(rect, element.cornerRadius, element.cornerRadius, shapePaint)
                        ShapeType.RECTANGLE -> canvas.drawRect(rect, shapePaint)
                        ShapeType.CIRCLE -> {
                            val radius = minOf(element.width, element.height) / 2f
                            canvas.drawCircle(0f, 0f, radius, shapePaint)
                        }
                        ShapeType.BADGE_STAR -> canvas.drawRoundRect(rect, 8f, 8f, shapePaint)
                    }
                }

                is ImageElement -> {
                    element.bitmap?.let { bmp ->
                        bitmapPaint.alpha = (element.opacity * 255).toInt().coerceIn(0, 255)
                        val dstRect = RectF(-element.width / 2f, -element.height / 2f, element.width / 2f, element.height / 2f)
                        canvas.drawBitmap(bmp, null, dstRect, bitmapPaint)
                    }
                }
            }

            canvas.restore()
        }

        bitmap
    }
}
