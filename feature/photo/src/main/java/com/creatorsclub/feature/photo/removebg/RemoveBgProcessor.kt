package com.creatorsclub.feature.photo.removebg

import android.graphics.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.hypot
import kotlin.math.roundToInt

object RemoveBgProcessor {

    /**
     * Executes on-device salient foreground segmentation to extract subject with soft alpha borders.
     * Works 100% offline with zero server calls.
     */
    suspend fun generateForegroundMask(source: Bitmap): Bitmap = withContext(Dispatchers.Default) {
        val w = source.width
        val h = source.height
        val maskBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ALPHA_8)

        val pixels = IntArray(w * h)
        source.getPixels(pixels, 0, w, 0, 0, w, h)

        val maskBytes = ByteArray(w * h)

        // 1. Establish corner and edge background color signatures
        val corners = intArrayOf(
            pixels[0],
            pixels[w - 1],
            pixels[(h - 1) * w],
            pixels[w * h - 1],
            pixels[w / 2],
            pixels[w * (h - 1) + w / 2]
        )

        val bgR = corners.map { Color.red(it) }.average().toInt()
        val bgG = corners.map { Color.green(it) }.average().toInt()
        val bgB = corners.map { Color.blue(it) }.average().toInt()

        // 2. Center-weighted distance prior (subjects usually inhabit the central 70% of frame)
        val cx = w / 2f
        val cy = h / 2f
        val maxDist = hypot(cx, cy)

        val threshold = 40

        for (y in 0 until h) {
            for (x in 0 until w) {
                val idx = y * w + x
                val p = pixels[idx]
                val r = Color.red(p)
                val g = Color.green(p)
                val b = Color.blue(p)

                val colorDiff = kotlin.math.abs(r - bgR) + kotlin.math.abs(g - bgG) + kotlin.math.abs(b - bgB)
                val distFromCenter = hypot(x - cx, y - cy) / maxDist

                // Prioritize pixels with strong color contrast from background or located in subject zone
                val isForeground = (colorDiff > threshold) || (distFromCenter < 0.45f && colorDiff > threshold / 2)

                maskBytes[idx] = if (isForeground) 255.toByte() else 0.toByte()
            }
        }

        // 3. Populate mask bitmap
        val buffer = java.nio.ByteBuffer.wrap(maskBytes)
        maskBitmap.copyPixelsFromBuffer(buffer)

        // 4. Smooth edges with bilateral / Gaussian blur pass on alpha channel
        val smoothedMask = Bitmap.createBitmap(w, h, Bitmap.Config.ALPHA_8)
        val canvas = Canvas(smoothedMask)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            maskFilter = BlurMaskFilter(3.5f, BlurMaskFilter.Blur.NORMAL)
        }
        canvas.drawBitmap(maskBitmap, 0f, 0f, paint)

        maskBitmap.recycle()
        smoothedMask
    }

    /**
     * Composites original source bitmap with alpha mask to produce a transparent ARGB_8888 image.
     */
    suspend fun compositeSourceWithMask(source: Bitmap, mask: Bitmap): Bitmap = withContext(Dispatchers.Default) {
        val w = source.width
        val h = source.height
        val output = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(output)
        canvas.drawBitmap(source, 0f, 0f, null)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
        }
        canvas.drawBitmap(mask, 0f, 0f, paint)

        output
    }

    /**
     * Modifies the alpha mask using manual Erase or Restore brush.
     */
    fun applyBrushStroke(
        mask: Bitmap,
        touchX: Float,
        touchY: Float,
        radius: Float,
        isErase: Boolean,
        feather: Float
    ): Bitmap {
        val canvas = Canvas(mask)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            if (feather > 0f) {
                maskFilter = BlurMaskFilter(feather, BlurMaskFilter.Blur.NORMAL)
            }
            if (isErase) {
                // Erase: clears mask alpha to 0
                xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
            } else {
                // Restore: sets mask alpha to 255
                xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC)
                color = Color.BLACK // ALPHA_8 stores alpha from color
                alpha = 255
            }
        }
        canvas.drawCircle(touchX, touchY, radius, paint)
        return mask
    }
}
