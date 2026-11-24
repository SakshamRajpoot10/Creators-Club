package com.creatorsclub.engine.image.processor

import android.graphics.Bitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class HistogramData(
    val redBins: IntArray,
    val greenBins: IntArray,
    val blueBins: IntArray,
    val lumaBins: IntArray,
    val maxCount: Int
)

object HistogramProcessor {

    /**
     * Computes a 256-bin histogram for Red, Green, Blue, and Luminance.
     * Uses sub-sampling for real-time responsiveness (<5 ms on mobile).
     */
    suspend fun computeHistogram(bitmap: Bitmap): HistogramData = withContext(Dispatchers.Default) {
        val red = IntArray(256)
        val green = IntArray(256)
        val blue = IntArray(256)
        val luma = IntArray(256)

        val width = bitmap.width
        val height = bitmap.height

        // Downsample stride to evaluate ~20,000 to 50,000 pixels max for ultra-fast calculation
        val totalPixels = width * height
        val step = maxOf(1, totalPixels / 40000)

        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        var maxVal = 1
        var i = 0
        while (i < pixels.size) {
            val color = pixels[i]
            val r = (color ushr 16) and 0xFF
            val g = (color ushr 8) and 0xFF
            val b = color and 0xFF
            val y = (0.299f * r + 0.587f * g + 0.114f * b).toInt().coerceIn(0, 255)

            red[r]++
            green[g]++
            blue[b]++
            luma[y]++

            if (luma[y] > maxVal) maxVal = luma[y]
            if (red[r] > maxVal) maxVal = red[r]
            if (green[g] > maxVal) maxVal = green[g]
            if (blue[b] > maxVal) maxVal = blue[b]

            i += step
        }

        HistogramData(
            redBins = red,
            greenBins = green,
            blueBins = blue,
            lumaBins = luma,
            maxCount = maxVal
        )
    }
}
