package com.creatorsclub.engine.image.processor

import android.graphics.Bitmap
import android.graphics.Color
import com.creatorsclub.engine.image.model.HslBandAdjustment
import com.creatorsclub.engine.image.model.HslColorBand
import kotlin.math.roundToInt

object HslProcessor {

    /**
     * Maps hue degrees [0, 360) to the closest HslColorBand.
     */
    fun getBandForHue(hue: Float): HslColorBand {
        val h = (hue % 360f + 360f) % 360f
        return when {
            h < 15f || h >= 345f -> HslColorBand.RED
            h < 45f -> HslColorBand.ORANGE
            h < 75f -> HslColorBand.YELLOW
            h < 165f -> HslColorBand.GREEN
            h < 195f -> HslColorBand.AQUA
            h < 255f -> HslColorBand.BLUE
            h < 285f -> HslColorBand.PURPLE
            else -> HslColorBand.MAGENTA
        }
    }

    /**
     * Applies 8-channel HSL color mix adjustments to a bitmap.
     */
    fun applyHsl(bitmap: Bitmap, adjustments: Map<HslColorBand, HslBandAdjustment>): Bitmap {
        if (adjustments.isEmpty() || adjustments.values.all { it.hue == 0f && it.saturation == 0f && it.luminance == 0f }) {
            return bitmap
        }

        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val hsv = FloatArray(3)

        for (i in pixels.indices) {
            val color = pixels[i]
            val a = (color ushr 24) and 0xFF
            val r = (color ushr 16) and 0xFF
            val g = (color ushr 8) and 0xFF
            val b = color and 0xFF

            Color.RGBToHSV(r, g, b, hsv)
            val band = getBandForHue(hsv[0])
            val adj = adjustments[band]

            if (adj != null && (adj.hue != 0f || adj.saturation != 0f || adj.luminance != 0f)) {
                // Hue shift (-180° to +180°)
                hsv[0] = (hsv[0] + adj.hue * 60f + 360f) % 360f

                // Saturation adjustment
                hsv[1] = (hsv[1] * (1f + adj.saturation)).coerceIn(0f, 1f)

                // Value/Luminance adjustment
                hsv[2] = (hsv[2] * (1f + adj.luminance)).coerceIn(0f, 1f)

                val newRgb = Color.HSVToColor(a, hsv)
                pixels[i] = newRgb
            }
        }

        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        output.setPixels(pixels, 0, width, 0, 0, width, height)
        return output
    }
}
