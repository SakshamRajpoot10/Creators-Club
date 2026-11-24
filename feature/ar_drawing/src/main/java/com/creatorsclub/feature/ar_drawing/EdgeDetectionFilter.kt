package com.creatorsclub.feature.ar_drawing

import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.sqrt

object EdgeDetectionFilter {

    /**
     * Converts a bitmap into a clean sketch line-art outline using a Sobel gradient operator.
     * 100% on-device, zero cloud dependency, zero latency.
     */
    suspend fun convertToSketchOutline(source: Bitmap, threshold: Int = 30): Bitmap = withContext(Dispatchers.Default) {
        val width = source.width
        val height = source.height
        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        // Convert to grayscale first
        val gray = IntArray(width * height)
        val pixels = IntArray(width * height)
        source.getPixels(pixels, 0, width, 0, 0, width, height)

        for (i in pixels.indices) {
            val c = pixels[i]
            val r = (c shr 16) and 0xFF
            val g = (c shr 8) and 0xFF
            val b = c and 0xFF
            gray[i] = (0.299 * r + 0.587 * g + 0.114 * b).toInt()
        }

        val outPixels = IntArray(width * height)

        // Sobel 3x3 kernels
        // Gx: [-1 0 1, -2 0 2, -1 0 1]
        // Gy: [-1 -2 -1,  0  0  0,  1  2  1]
        for (y in 1 until height - 1) {
            for (x in 1 until width - 1) {
                val p00 = gray[(y - 1) * width + (x - 1)]
                val p02 = gray[(y - 1) * width + (x + 1)]
                val p10 = gray[y * width + (x - 1)]
                val p12 = gray[y * width + (x + 1)]
                val p20 = gray[(y + 1) * width + (x - 1)]
                val p22 = gray[(y + 1) * width + (x + 1)]

                val p01 = gray[(y - 1) * width + x]
                val p21 = gray[(y + 1) * width + x]

                val gx = (p02 + 2 * p12 + p22) - (p00 + 2 * p10 + p20)
                val gy = (p20 + 2 * p21 + p22) - (p00 + 2 * p01 + p02)
                val mag = sqrt((gx * gx + gy * gy).toDouble()).toInt()

                // If edge detected, draw dark line; otherwise pure white background
                if (mag > threshold) {
                    outPixels[y * width + x] = AndroidColor.BLACK
                } else {
                    outPixels[y * width + x] = AndroidColor.WHITE
                }
            }
        }

        output.setPixels(outPixels, 0, width, 0, 0, width, height)
        output
    }
}
