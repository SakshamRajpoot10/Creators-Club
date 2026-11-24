package com.creatorsclub.core.storage

import com.creatorsclub.core.storage.model.CanvasProperties
import com.creatorsclub.core.storage.model.LayerManifest
import com.creatorsclub.core.storage.model.LayerTransform
import com.creatorsclub.core.storage.model.ProjectManifest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ProjectManifestTest {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @Test
    fun projectManifest_serializationAndDeserialization_preservesAllFields() {
        val originalManifest = ProjectManifest(
            formatVersion = 1,
            projectId = "proj_test_101",
            name = "Diwali Poster 2026",
            studioType = "DESIGN",
            createdAt = 1732440000000L,
            modifiedAt = 1732441000000L,
            canvas = CanvasProperties(
                width = 1080,
                height = 1080,
                aspectRatio = "1:1",
                backgroundColorHex = "#FF5722"
            ),
            layers = listOf(
                LayerManifest(
                    layerId = "layer_1",
                    type = "TEXT",
                    name = "Headline",
                    isVisible = true,
                    isLocked = false,
                    transform = LayerTransform(x = 100f, y = 200f, scaleX = 1.2f, scaleY = 1.2f, rotation = 0f, opacity = 1f)
                ),
                LayerManifest(
                    layerId = "layer_2",
                    type = "IMAGE",
                    name = "Product Photo",
                    isVisible = true,
                    isLocked = true,
                    transform = LayerTransform(x = 540f, y = 540f, scaleX = 1f, scaleY = 1f, rotation = 15f, opacity = 0.9f)
                )
            ),
            metadata = mapOf("author" to "Saksham", "app" to "Kreavilo Studio")
        )

        val encodedJson = json.encodeToString(ProjectManifest.serializer(), originalManifest)
        assertNotNull(encodedJson)

        val decodedManifest = json.decodeFromString(ProjectManifest.serializer(), encodedJson)
        assertEquals(originalManifest.projectId, decodedManifest.projectId)
        assertEquals(originalManifest.name, decodedManifest.name)
        assertEquals(originalManifest.studioType, decodedManifest.studioType)
        assertEquals(2, decodedManifest.layers.size)
        assertEquals("Diwali Poster 2026", decodedManifest.name)
        assertEquals("Saksham", decodedManifest.metadata["author"])
        assertEquals(1080, decodedManifest.canvas.width)
    }
}
