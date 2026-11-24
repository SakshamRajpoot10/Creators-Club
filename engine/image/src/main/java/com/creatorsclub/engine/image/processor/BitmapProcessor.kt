package com.creatorsclub.engine.image.processor

import android.graphics.*
import com.creatorsclub.engine.image.model.CropAspectRatio
import com.creatorsclub.engine.image.model.CropTransformState
import com.creatorsclub.engine.image.model.ExportDimension
import com.creatorsclub.engine.image.model.ImageAdjustmentState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

object BitmapProcessor {

    /**
     * Renders full-resolution export by applying adjustments, curves, HSL, details,
     * vignette, and crop transforms non-destructively.
     */
    suspend fun processAndRender(
        sourceBitmap: Bitmap,
        adjustmentState: ImageAdjustmentState,
        cropTransformState: CropTransformState,
        targetDimension: ExportDimension = ExportDimension.ORIGINAL
    ): Bitmap = withContext(Dispatchers.Default) {
        // 1. Geometric transforms (straighten, rotation, flip)
        val totalRotation = cropTransformState.rotationDegrees + cropTransformState.straightenDegrees
        val matrix = Matrix().apply {
            if (totalRotation != 0f) {
                postRotate(totalRotation)
            }
            val scaleX = if (cropTransformState.isFlippedHorizontal) -1f else 1f
            val scaleY = if (cropTransformState.isFlippedVertical) -1f else 1f
            if (scaleX != 1f || scaleY != 1f) {
                postScale(scaleX, scaleY)
            }
        }

        val rotatedBitmap = if (totalRotation != 0f || cropTransformState.isFlippedHorizontal || cropTransformState.isFlippedVertical) {
            Bitmap.createBitmap(
                sourceBitmap,
                0,
                0,
                sourceBitmap.width,
                sourceBitmap.height,
                matrix,
                true
            )
        } else {
            sourceBitmap
        }

        // 2. Aspect ratio crop
        val croppedBitmap = applyCrop(rotatedBitmap, cropTransformState.aspectRatio)

        // 3. ColorMatrix (Tone, Exposure, Saturation, Warmth, Filter Preset * Intensity)
        val composeMatrix = ColorMatrixProcessor.createCombinedColorMatrix(adjustmentState)
        val androidColorMatrix = android.graphics.ColorMatrix(composeMatrix.values)

        val colorFilteredBitmap = Bitmap.createBitmap(
            croppedBitmap.width,
            croppedBitmap.height,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(colorFilteredBitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
            colorFilter = ColorMatrixColorFilter(androidColorMatrix)
        }
        canvas.drawBitmap(croppedBitmap, 0f, 0f, paint)

        // 4. RGB Curves application
        val curvesAppliedBitmap = if (!adjustmentState.curves.isDefault()) {
            val res = CurvesProcessor.applyCurves(colorFilteredBitmap, adjustmentState.curves)
            if (colorFilteredBitmap != sourceBitmap && colorFilteredBitmap != croppedBitmap) {
                colorFilteredBitmap.recycle()
            }
            res
        } else {
            colorFilteredBitmap
        }

        // 5. HSL Color Mix application
        val hslAppliedBitmap = if (adjustmentState.hslAdjustments.isNotEmpty()) {
            val res = HslProcessor.applyHsl(curvesAppliedBitmap, adjustmentState.hslAdjustments)
            if (curvesAppliedBitmap != sourceBitmap && curvesAppliedBitmap != croppedBitmap && curvesAppliedBitmap != colorFilteredBitmap) {
                curvesAppliedBitmap.recycle()
            }
            res
        } else {
            curvesAppliedBitmap
        }

        // 6. Vignette overlay if applicable
        val finalEffectsBitmap = if (adjustmentState.vignette > 0f) {
            applyVignette(hslAppliedBitmap, adjustmentState.vignette)
        } else {
            hslAppliedBitmap
        }

        // 7. Dimension scaling (e.g., 4K, 1440p, 1080p, Square)
        val outputBitmap = if (targetDimension.maxDimension != null) {
            scaleToDimension(finalEffectsBitmap, targetDimension)
        } else {
            finalEffectsBitmap
        }

        // Safely recycle intermediate bitmaps if distinct
        if (rotatedBitmap != sourceBitmap && rotatedBitmap != croppedBitmap && rotatedBitmap != outputBitmap) {
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

    private fun applyVignette(bitmap: Bitmap, intensity: Float): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        canvas.drawBitmap(bitmap, 0f, 0f, null)

        val radius = (maxOf(width, height) / 1.1f)
        val colors = intArrayOf(Color.TRANSPARENT, Color.argb((intensity * 180).toInt().coerceIn(0, 255), 0, 0, 0))
        val stops = floatArrayOf(0.5f, 1.0f)
        val gradient = RadialGradient(
            width / 2f,
            height / 2f,
            radius,
            colors,
            stops,
            Shader.TileMode.CLAMP
        )

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = gradient
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        return output
    }

    private fun scaleToDimension(bitmap: Bitmap, dimension: ExportDimension): Bitmap {
        val maxDim = dimension.maxDimension ?: return bitmap
        val width = bitmap.width
        val height = bitmap.height

        if (dimension == ExportDimension.SQUARE_1080) {
            return Bitmap.createScaledBitmap(bitmap, 1080, 1080, true)
        }

        val currentMax = maxOf(width, height)
        if (currentMax <= maxDim) return bitmap

        val scale = maxDim.toFloat() / currentMax.toFloat()
        val targetWidth = (width * scale).roundToInt().coerceAtLeast(1)
        val targetHeight = (height * scale).roundToInt().coerceAtLeast(1)

        return Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
    }
}
