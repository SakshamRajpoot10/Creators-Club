package com.creatorsclub.engine.image

import com.creatorsclub.engine.image.model.CurvePoint
import com.creatorsclub.engine.image.processor.CurvesProcessor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CurvesSplineTest {

    @Test
    fun testDefaultIdentityCurve() {
        val points = listOf(CurvePoint(0f, 0f), CurvePoint(1f, 1f))
        val lut = CurvesProcessor.buildCurveLut(points)

        assertEquals(256, lut.size)
        assertEquals(0, lut[0])
        assertEquals(128, lut[128])
        assertEquals(255, lut[255])
    }

    @Test
    fun testMidpointLiftCurve() {
        // Lift midtones
        val points = listOf(CurvePoint(0f, 0f), CurvePoint(0.5f, 0.75f), CurvePoint(1f, 1f))
        val lut = CurvesProcessor.buildCurveLut(points)

        assertEquals(256, lut.size)
        assertEquals(0, lut[0])
        assertTrue("Midtone should be lifted above 128", lut[128] > 128)
        assertEquals(255, lut[255])

        // Ensure all values are bounded between 0 and 255
        for (i in lut.indices) {
            assertTrue("LUT values must be bounded [0, 255]", lut[i] in 0..255)
        }
    }
}
