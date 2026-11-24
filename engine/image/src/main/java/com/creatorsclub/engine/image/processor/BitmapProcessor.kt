package com.creatorsclub.engine.image.processor

import android.graphics.*
import com.creatorsclub.engine.image.model.CropAspectRatio
import com.creatorsclub.engine.image.model.CropTransformState
import com.creatorsclub.engine.image.model.ImageAdjustmentState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

object BitmapProcessor {

    /**
     * Renders full-resolution export by applying adjustments and transforms non-destructively.
     */
    suspend fun processAndRender(
        sourceBitmap: Bitmap,
        adjustmentState: ImageAdjustmentState,
        cropTransformState: CropTransformState
    ): Bitmap = withContext(Dispatchers.Default) {
        // 1. First, apply geometric transforms (rotation, flip)
        val matrix = Matrix().apply {
            if (cropTransformState.rotationDegrees != 0f) {
                postRotate(cropTransformState.rotationDegrees)
            }
            val scaleX = if (cropTransformState.isFlippedHorizontal) -1f else 1f
            val scaleY = if (cropTransformState.isFlippedVertical) -1f else 1f
            if (scaleX != 1f || scaleY != 1f) {
                postScale(scaleX, scaleY)
            }
        }

        val rotatedBitmap = Bitmap.createBitmap(
            sourceBitmap,
            0,
            0,
            sourceBitmap.width,
            sourceBitmap.height,
            matrix,
            true
        )

        // 2. Apply aspect ratio crop if designated
        val croppedBitmap = applyCrop(rotatedBitmap, cropTransformState.aspectRatio)

        // 3. Apply ColorMatrix filter adjustments onto bitmap
        val composeMatrix = ColorMatrixProcessor.createCombinedColorMatrix(adjustmentState)
        val androidColorMatrix = android.graphics.ColorMatrix(composeMatrix.values)

        val outputBitmap = Bitmap.createBitmap(
            croppedBitmap.width,
            croppedBitmap.height,
            Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(outputBitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
            colorFilter = ColorMatrixColorFilter(androidColorMatrix)
        }

        canvas.drawBitmap(croppedBitmap, 0f, 0f, paint)

        // If intermediate bitmaps were created and differ from source, recycle them safely
        if (rotatedBitmap != sourceBitmap && rotatedBitmap != croppedBitmap) {
            rotatedBitmap.recycle()
        }
        if (croppedBitmap != sourceBitmap && croppedBitmap != outputBitmap) {
            croppedBitmap.recycle()
        }

        outputBitmap
    }

    private fun applyCrop(bitmap: Bitmap, targetRatio: CropAspectRatio): Bitmap {
        val desiredRatio = targetRatio.ratio ?: return bitmap

        val currentRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
        val cropWidth: Int
        val cropHeight: Int

        if (currentRatio > desiredRatio) {
            // Image is wider than desired ratio: crop width
            cropHeight = bitmap.height
            cropWidth = (bitmap.height * desiredRatio).roundToInt().coerceAtMost(bitmap.width)
        } else {
            // Image is taller than desired ratio: crop height
            cropWidth = bitmap.width
            cropHeight = (bitmap.width / desiredRatio).roundToInt().coerceAtMost(bitmap.height)
        }

        val startX = ((bitmap.width - cropWidth) / 2).coerceAtLeast(0)
        val startY = ((bitmap.height - cropHeight) / 2).coerceAtLeast(0)

        return Bitmap.createBitmap(bitmap, startX, startY, cropWidth, cropHeight)
    }
}
