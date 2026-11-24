package com.creatorsclub.engine.image.processor

import android.graphics.Bitmap
import com.creatorsclub.engine.image.model.CurveChannel
import com.creatorsclub.engine.image.model.CurvePoint
import com.creatorsclub.engine.image.model.CurvesState
import kotlin.math.roundToInt

object CurvesProcessor {

    /**
     * Builds a 256-entry lookup table (LUT) for each channel (RGB, R, G, B)
     * using Monotone Cubic / Piecewise Linear spline interpolation between control points.
     */
    fun buildCurveLut(points: List<CurvePoint>): IntArray {
        val lut = IntArray(256)
        if (points.isEmpty()) {
            for (i in 0..255) lut[i] = i
            return lut
        }

        val sorted = points.sortedBy { it.x }
        if (sorted.size == 1) {
            val y = (sorted[0].y * 255f).roundToInt().coerceIn(0, 255)
            for (i in 0..255) lut[i] = y
            return lut
        }

        // Piecewise linear interpolation between points
        for (i in 0 until sorted.size - 1) {
            val p0 = sorted[i]
            val p1 = sorted[i + 1]

            val x0 = (p0.x * 255f).roundToInt().coerceIn(0, 255)
            val x1 = (p1.x * 255f).roundToInt().coerceIn(0, 255)
            val y0 = p0.y * 255f
            val y1 = p1.y * 255f

            if (x0 == x1) {
                lut[x0] = y0.roundToInt().coerceIn(0, 255)
                continue
            }

            for (x in x0..x1) {
                val t = (x - x0).toFloat() / (x1 - x0).toFloat()
                // Smooth hermite ease for organic curve feel
                val smoothT = t * t * (3f - 2f * t)
                val y = y0 + (y1 - y0) * smoothT
                lut[x] = y.roundToInt().coerceIn(0, 255)
            }
        }

        // Fill tails
        val firstX = (sorted.first().x * 255f).roundToInt().coerceIn(0, 255)
        val firstY = (sorted.first().y * 255f).roundToInt().coerceIn(0, 255)
        for (x in 0 until firstX) lut[x] = firstY

        val lastX = (sorted.last().x * 255f).roundToInt().coerceIn(0, 255)
        val lastY = (sorted.last().y * 255f).roundToInt().coerceIn(0, 255)
        for (x in (lastX + 1)..255) lut[x] = lastY

        return lut
    }

    /**
     * Applies RGB Curves to bitmap pixels.
     */
    fun applyCurves(bitmap: Bitmap, curvesState: CurvesState): Bitmap {
        if (curvesState.isDefault()) return bitmap

        val rgbLut = buildCurveLut(curvesState.rgbPoints)
        val redLut = buildCurveLut(curvesState.redPoints)
        val greenLut = buildCurveLut(curvesState.greenPoints)
        val blueLut = buildCurveLut(curvesState.bluePoints)

        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        for (i in pixels.indices) {
            val color = pixels[i]
            val a = (color ushr 24) and 0xFF
            var r = (color ushr 16) and 0xFF
            var g = (color ushr 8) and 0xFF
            var b = color and 0xFF

            // Apply Master RGB curve
            r = rgbLut[r]
            g = rgbLut[g]
            b = rgbLut[b]

            // Apply individual R, G, B channel curves
            r = redLut[r]
            g = greenLut[g]
            b = blueLut[b]

            pixels[i] = (a shl 24) or (r shl 16) or (g shl 8) or b
        }

        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        output.setPixels(pixels, 0, width, 0, 0, width, height)
        return output
    }
}
