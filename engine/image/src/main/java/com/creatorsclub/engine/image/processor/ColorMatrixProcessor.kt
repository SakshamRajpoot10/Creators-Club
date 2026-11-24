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

    fun getPresetColorMatrix(preset: FilterPreset): ColorMatrix {
        return when (preset) {
            FilterPreset.NONE -> ColorMatrix()
            FilterPreset.VIVID -> ColorMatrix().apply { setToSaturation(1.4f) }
            FilterPreset.HDR -> ColorMatrix(
                floatArrayOf(
                    1.2f, 0f, 0f, 0f, -15f,
                    0f, 1.2f, 0f, 0f, -15f,
                    0f, 0f, 1.2f, 0f, -15f,
                    0f, 0f, 0f, 1f, 0f
                )
            ).also { it.timesAssign(ColorMatrix().apply { setToSaturation(1.25f) }) }
            FilterPreset.SOFT_GLOW -> ColorMatrix(
                floatArrayOf(
                    1.05f, 0f, 0f, 0f, 15f,
                    0f, 1.05f, 0f, 0f, 15f,
                    0f, 0f, 1.05f, 0f, 15f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            FilterPreset.CRISP -> ColorMatrix(
                floatArrayOf(
                    1.15f, 0f, 0f, 0f, -10f,
                    0f, 1.15f, 0f, 0f, -10f,
                    0f, 0f, 1.15f, 0f, -10f,
                    0f, 0f, 0f, 1f, 0f
                )
            )

            // Cinema
            FilterPreset.CINEMATIC -> ColorMatrix(
                floatArrayOf(
                    1.1f, 0f, 0f, 0f, -10f,
                    0f, 1.0f, 0f, 0f, 5f,
                    0f, 0f, 1.2f, 0f, 15f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            FilterPreset.TEAL_ORANGE -> ColorMatrix(
                floatArrayOf(
                    1.2f, 0.05f, -0.1f, 0f, 10f,
                    -0.05f, 1.1f, 0.05f, 0f, 5f,
                    -0.15f, 0.05f, 1.3f, 0f, 18f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            FilterPreset.HOLLYWOOD -> ColorMatrix(
                floatArrayOf(
                    1.15f, 0.08f, 0f, 0f, 12f,
                    0.05f, 1.05f, 0f, 0f, 8f,
                    -0.05f, 0f, 0.9f, 0f, -8f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            FilterPreset.CYBERPUNK -> ColorMatrix(
                floatArrayOf(
                    1.3f, -0.1f, 0.1f, 0f, 20f,
                    -0.1f, 0.9f, 0.2f, 0f, -10f,
                    0.1f, 0.2f, 1.4f, 0f, 25f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            FilterPreset.BLADE_RUNNER -> ColorMatrix(
                floatArrayOf(
                    1.25f, 0f, 0.1f, 0f, 15f,
                    0f, 0.85f, 0.1f, 0f, -12f,
                    -0.1f, 0.1f, 1.35f, 0f, 20f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            FilterPreset.INDIE_FILM -> ColorMatrix(
                floatArrayOf(
                    1.08f, 0.06f, 0f, 0f, 8f,
                    0.04f, 1.02f, 0f, 0f, 4f,
                    -0.08f, 0f, 0.88f, 0f, -4f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            FilterPreset.DOCUMENTARY -> ColorMatrix(
                floatArrayOf(
                    1.05f, 0f, 0f, 0f, -5f,
                    0f, 1.05f, 0f, 0f, -5f,
                    0f, 0f, 1.05f, 0f, -5f,
                    0f, 0f, 0f, 1f, 0f
                )
            ).also { it.timesAssign(ColorMatrix().apply { setToSaturation(0.85f) }) }

            // Classic & Vintage
            FilterPreset.VINTAGE -> ColorMatrix(
                floatArrayOf(
                    0.9f, 0.1f, 0.1f, 0f, 10f,
                    0.1f, 0.8f, 0.1f, 0f, 5f,
                    0.1f, 0.1f, 0.7f, 0f, -5f,
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
            FilterPreset.KODACHROME -> ColorMatrix(
                floatArrayOf(
                    1.12f, 0.05f, 0f, 0f, 14f,
                    0f, 1.08f, 0.02f, 0f, 6f,
                    -0.05f, 0f, 0.92f, 0f, -10f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            FilterPreset.POLAROID -> ColorMatrix(
                floatArrayOf(
                    1.05f, 0.08f, 0f, 0f, 16f,
                    0.02f, 0.98f, 0f, 0f, 10f,
                    0f, 0f, 0.85f, 0f, 12f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            FilterPreset.BLEACH_BYPASS -> ColorMatrix(
                floatArrayOf(
                    1.3f, 0f, 0f, 0f, -20f,
                    0f, 1.3f, 0f, 0f, -20f,
                    0f, 0f, 1.3f, 0f, -20f,
                    0f, 0f, 0f, 1f, 0f
                )
            ).also { it.timesAssign(ColorMatrix().apply { setToSaturation(0.4f) }) }
            FilterPreset.FADED -> ColorMatrix(
                floatArrayOf(
                    0.9f, 0f, 0f, 0f, 30f,
                    0f, 0.9f, 0f, 0f, 30f,
                    0f, 0f, 0.9f, 0f, 30f,
                    0f, 0f, 0f, 1f, 0f
                )
            ).also { it.timesAssign(ColorMatrix().apply { setToSaturation(0.8f) }) }
            FilterPreset.SEPIA -> ColorMatrix(
                floatArrayOf(
                    0.393f, 0.769f, 0.189f, 0f, 0f,
                    0.349f, 0.686f, 0.168f, 0f, 0f,
                    0.272f, 0.534f, 0.131f, 0f, 0f,
                    0f, 0f, 0f, 1f, 0f
                )
            )

            // Monochrome
            FilterPreset.MONOCHROME -> ColorMatrix().apply { setToSaturation(0f) }
            FilterPreset.DRAMATIC_BW -> ColorMatrix(
                floatArrayOf(
                    1.4f, 0f, 0f, 0f, -40f,
                    0f, 1.4f, 0f, 0f, -40f,
                    0f, 0f, 1.4f, 0f, -40f,
                    0f, 0f, 0f, 1f, 0f
                )
            ).also { it.timesAssign(ColorMatrix().apply { setToSaturation(0f) }) }
            FilterPreset.SILVER -> ColorMatrix(
                floatArrayOf(
                    1.1f, 0f, 0f, 0f, 5f,
                    0f, 1.1f, 0f, 0f, 5f,
                    0f, 0f, 1.15f, 0f, 10f,
                    0f, 0f, 0f, 1f, 0f
                )
            ).also { it.timesAssign(ColorMatrix().apply { setToSaturation(0f) }) }
            FilterPreset.CARBON -> ColorMatrix(
                floatArrayOf(
                    0.9f, 0f, 0f, 0f, 25f,
                    0f, 0.9f, 0f, 0f, 25f,
                    0f, 0f, 0.9f, 0f, 25f,
                    0f, 0f, 0f, 1f, 0f
                )
            ).also { it.timesAssign(ColorMatrix().apply { setToSaturation(0f) }) }
            FilterPreset.MOODY -> ColorMatrix(
                floatArrayOf(
                    1.25f, 0f, 0f, 0f, -30f,
                    0f, 1.25f, 0f, 0f, -30f,
                    0f, 0f, 1.25f, 0f, -30f,
                    0f, 0f, 0f, 1f, 0f
                )
            ).also { it.timesAssign(ColorMatrix().apply { setToSaturation(0f) }) }
            FilterPreset.SEPIA_BW -> ColorMatrix(
                floatArrayOf(
                    0.38f, 0.74f, 0.18f, 0f, 10f,
                    0.34f, 0.66f, 0.16f, 0f, 5f,
                    0.26f, 0.50f, 0.12f, 0f, -5f,
                    0f, 0f, 0f, 1f, 0f
                )
            )

            // Creative & Mood
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
            FilterPreset.GOLDEN_HOUR -> ColorMatrix(
                floatArrayOf(
                    1.2f, 0.08f, 0f, 0f, 18f,
                    0.05f, 1.08f, 0f, 0f, 12f,
                    -0.1f, 0f, 0.82f, 0f, -15f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            FilterPreset.PASTEL -> ColorMatrix(
                floatArrayOf(
                    0.95f, 0.05f, 0.05f, 0f, 20f,
                    0.05f, 0.95f, 0.05f, 0f, 20f,
                    0.05f, 0.05f, 0.95f, 0f, 25f,
                    0f, 0f, 0f, 1f, 0f
                )
            ).also { it.timesAssign(ColorMatrix().apply { setToSaturation(0.75f) }) }
            FilterPreset.EMERALD -> ColorMatrix(
                floatArrayOf(
                    0.88f, 0.05f, 0f, 0f, -5f,
                    0.02f, 1.22f, 0.05f, 0f, 15f,
                    0f, 0.05f, 0.95f, 0f, 5f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            FilterPreset.SUNSET -> ColorMatrix(
                floatArrayOf(
                    1.25f, 0.05f, 0.05f, 0f, 20f,
                    0.02f, 0.95f, 0.05f, 0f, 5f,
                    -0.05f, 0.05f, 1.15f, 0f, 12f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            FilterPreset.URBAN -> ColorMatrix(
                floatArrayOf(
                    1.12f, 0f, 0f, 0f, -8f,
                    0f, 1.02f, 0f, 0f, 2f,
                    0f, 0f, 1.08f, 0f, 8f,
                    0f, 0f, 0f, 1f, 0f
                )
            ).also { it.timesAssign(ColorMatrix().apply { setToSaturation(1.15f) }) }
        }
    }
}
