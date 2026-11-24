package com.creatorsclub.engine.image.processor

import androidx.compose.ui.graphics.ColorMatrix
import com.creatorsclub.engine.image.model.FilterPreset
import com.creatorsclub.engine.image.model.ImageAdjustmentState

object ColorMatrixProcessor {

    /**
     * Calculates a combined ColorMatrix that can be used directly in Compose's
     * ColorFilter.colorMatrix(cm) for instantaneous 60 FPS preview updates.
     */
    fun createCombinedColorMatrix(state: ImageAdjustmentState): ColorMatrix {
        val result = ColorMatrix()

        // 1. Exposure & Brightness adjustments
        val brightnessOffset = (state.brightness + state.exposure) * 255f
        if (brightnessOffset != 0f) {
            val brightnessMatrix = ColorMatrix(
                floatArrayOf(
                    1f, 0f, 0f, 0f, brightnessOffset,
                    0f, 1f, 0f, 0f, brightnessOffset,
                    0f, 0f, 1f, 0f, brightnessOffset,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            result.timesAssign(brightnessMatrix)
        }

        // 2. Contrast adjustment
        if (state.contrast != 1f) {
            val scale = state.contrast
            val translate = (-0.5f * scale + 0.5f) * 255f
            val contrastMatrix = ColorMatrix(
                floatArrayOf(
                    scale, 0f, 0f, 0f, translate,
                    0f, scale, 0f, 0f, translate,
                    0f, 0f, scale, 0f, translate,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            result.timesAssign(contrastMatrix)
        }

        // 3. Saturation adjustment
        if (state.saturation != 1f) {
            val saturationMatrix = ColorMatrix().apply {
                setToSaturation(state.saturation)
            }
            result.timesAssign(saturationMatrix)
        }

        // 4. Warmth (Temperature) adjustment
        if (state.warmth != 0f) {
            val redScale = 1f + (state.warmth * 0.15f)
            val blueScale = 1f - (state.warmth * 0.15f)
            val warmthMatrix = ColorMatrix(
                floatArrayOf(
                    redScale, 0f, 0f, 0f, 0f,
                    0f, 1f, 0f, 0f, 0f,
                    0f, 0f, blueScale, 0f, 0f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            result.timesAssign(warmthMatrix)
        }

        // 5. Preset filters
        if (state.filterPreset != FilterPreset.NONE) {
            val presetMatrix = getPresetColorMatrix(state.filterPreset)
            result.timesAssign(presetMatrix)
        }

        return result
    }

    private fun getPresetColorMatrix(preset: FilterPreset): ColorMatrix {
        return when (preset) {
            FilterPreset.NONE -> ColorMatrix()
            FilterPreset.MONOCHROME -> ColorMatrix().apply { setToSaturation(0f) }
            FilterPreset.SEPIA -> ColorMatrix(
                floatArrayOf(
                    0.393f, 0.769f, 0.189f, 0f, 0f,
                    0.349f, 0.686f, 0.168f, 0f, 0f,
                    0.272f, 0.534f, 0.131f, 0f, 0f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            FilterPreset.CINEMATIC -> ColorMatrix(
                floatArrayOf(
                    1.1f, 0f, 0f, 0f, -10f,
                    0f, 1.0f, 0f, 0f, 5f,
                    0f, 0f, 1.2f, 0f, 15f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            FilterPreset.VINTAGE -> ColorMatrix(
                floatArrayOf(
                    0.9f, 0.1f, 0.1f, 0f, 10f,
                    0.1f, 0.8f, 0.1f, 0f, 5f,
                    0.1f, 0.1f, 0.7f, 0f, -5f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            FilterPreset.PORTRAIT -> ColorMatrix(
                floatArrayOf(
                    1.05f, 0f, 0f, 0f, 8f,
                    0f, 1.02f, 0f, 0f, 4f,
                    0f, 0f, 0.98f, 0f, 0f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            FilterPreset.FILM -> ColorMatrix(
                floatArrayOf(
                    1.0f, 0.05f, 0.05f, 0f, 12f,
                    0.05f, 0.95f, 0.05f, 0f, 8f,
                    0.05f, 0.05f, 0.9f, 0f, 5f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            FilterPreset.WARM -> ColorMatrix(
                floatArrayOf(
                    1.15f, 0f, 0f, 0f, 10f,
                    0f, 1.05f, 0f, 0f, 5f,
                    0f, 0f, 0.85f, 0f, -10f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            FilterPreset.COOL -> ColorMatrix(
                floatArrayOf(
                    0.88f, 0f, 0f, 0f, -10f,
                    0f, 1.0f, 0f, 0f, 0f,
                    0f, 0f, 1.18f, 0f, 15f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            FilterPreset.MATTE -> ColorMatrix(
                floatArrayOf(
                    0.95f, 0f, 0f, 0f, 25f,
                    0f, 0.95f, 0f, 0f, 25f,
                    0f, 0f, 0.95f, 0f, 25f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            FilterPreset.DRAMATIC -> ColorMatrix(
                floatArrayOf(
                    1.3f, 0f, 0f, 0f, -35f,
                    0f, 1.3f, 0f, 0f, -35f,
                    0f, 0f, 1.3f, 0f, -35f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
        }
    }
}
