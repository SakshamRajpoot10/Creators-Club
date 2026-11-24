package com.creatorsclub.engine.image

import com.creatorsclub.engine.image.model.CropAspectRatio
import com.creatorsclub.engine.image.model.NormalizedCropRect
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.roundToInt

class CropGeometryTest {

    private fun calculateMaxCenteredCropBounds(
        sourceWidth: Int,
        sourceHeight: Int,
        targetRatio: CropAspectRatio
    ): Pair<Int, Int> {
        val desiredRatio = targetRatio.ratio ?: return Pair(sourceWidth, sourceHeight)
        val currentRatio = sourceWidth.toFloat() / sourceHeight.toFloat()
        val cropWidth: Int
        val cropHeight: Int

        if (currentRatio > desiredRatio) {
            // Wider than desired ratio: height-constrained, crop width
            cropHeight = sourceHeight
            cropWidth = (sourceHeight * desiredRatio).roundToInt().coerceIn(1, sourceWidth)
        } else {
            // Taller than desired ratio: width-constrained, crop height
            cropWidth = sourceWidth
            cropHeight = (sourceWidth / desiredRatio).roundToInt().coerceIn(1, sourceHeight)
        }
        return Pair(cropWidth, cropHeight)
    }

    @Test
    fun testLandscape16x9CropFixture() {
        // Section O4 fixture: 4000x3000 source with 16:9 aspect ratio
        val (w, h) = calculateMaxCenteredCropBounds(4000, 3000, CropAspectRatio.LANDSCAPE_16_9)
        assertEquals(4000, w)
        assertEquals(2250, h)
    }

    @Test
    fun testSquare1x1CropFixture() {
        // Section O4 fixture: 4000x3000 source with 1:1 aspect ratio
        val (w, h) = calculateMaxCenteredCropBounds(4000, 3000, CropAspectRatio.SQUARE_1_1)
        assertEquals(3000, w)
        assertEquals(3000, h)
    }

    @Test
    fun testPortrait4x5CropFixture() {
        // Section O4 fixture: 4000x3000 source with 4:5 aspect ratio
        val (w, h) = calculateMaxCenteredCropBounds(4000, 3000, CropAspectRatio.PORTRAIT_4_5)
        assertEquals(2400, w)
        assertEquals(3000, h)
    }

    @Test
    fun testPortraitSource9x16Crop() {
        // 3000x4000 portrait source with 9:16 aspect ratio
        val (w, h) = calculateMaxCenteredCropBounds(3000, 4000, CropAspectRatio.STORY_9_16)
        assertEquals(2250, w)
        assertEquals(4000, h)
    }
}
