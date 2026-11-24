package com.creatorsclub.engine.image

import com.creatorsclub.engine.image.model.CropAspectRatio
import com.creatorsclub.engine.image.model.CropTransformState
import com.creatorsclub.engine.image.model.ImageAdjustmentState
import com.creatorsclub.engine.image.model.PhotoEditRecipe
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class RecipeSerializationTest {

    private val json = Json { ignoreUnknownKeys = true; prettyPrint = true }

    @Test
    fun testRecipeSerializationAndDeserialization() {
        val originalRecipe = PhotoEditRecipe(
            version = 1,
            sourceUri = "/path/to/source.png",
            adjustments = ImageAdjustmentState(
                brightness = 0.25f,
                contrast = 1.15f,
                saturation = 1.30f,
                exposure = -0.10f,
                warmth = 0.05f
            ),
            cropTransform = CropTransformState(
                aspectRatio = CropAspectRatio.PORTRAIT_4_5,
                rotationDegrees = 90f
            )
        )

        val serialized = json.encodeToString(originalRecipe)
        assertNotNull(serialized)

        val deserialized = json.decodeFromString<PhotoEditRecipe>(serialized)
        assertEquals(1, deserialized.version)
        assertEquals("/path/to/source.png", deserialized.sourceUri)
        assertEquals(0.25f, deserialized.adjustments.brightness, 0.001f)
        assertEquals(1.15f, deserialized.adjustments.contrast, 0.001f)
        assertEquals(CropAspectRatio.PORTRAIT_4_5, deserialized.cropTransform.aspectRatio)
        assertEquals(90f, deserialized.cropTransform.rotationDegrees, 0.001f)
    }
}
