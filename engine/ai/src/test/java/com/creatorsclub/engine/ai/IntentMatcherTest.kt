package com.creatorsclub.engine.ai

import com.creatorsclub.engine.ai.matcher.IntentMatcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class IntentMatcherTest {

    @Test
    fun queryBackgroundRemoval_matchesProductStudio() {
        val response = IntentMatcher.matchQuery("Please remove background from this shoe photo")
        assertNotNull(response.matchedTool)
        assertEquals("product", response.suggestedStudioId)
        assertTrue(response.explanationText.contains("Import your product or subject photo"))
    }

    @Test
    fun queryHindiPhotoEdit_matchesPhotoStudio() {
        val response = IntentMatcher.matchQuery("photo edit karna hai aur filter lagana hai")
        assertNotNull(response.matchedTool)
        assertEquals("photo", response.suggestedStudioId)
        assertTrue(response.explanationText.contains("Cinematic, Vintage, or B&W filters"))
    }

    @Test
    fun queryReelsVideo_matchesVideoStudio() {
        val response = IntentMatcher.matchQuery("how to create reel with cuts and music")
        assertNotNull(response.matchedTool)
        assertEquals("video", response.suggestedStudioId)
    }

    @Test
    fun queryRangoliMandala_matchesDrawingStudio() {
        val response = IntentMatcher.matchQuery("mujhe rangoli aur mandala draw karna hai")
        assertNotNull(response.matchedTool)
        assertEquals("drawing", response.suggestedStudioId)
        assertTrue(response.explanationText.contains("Mandala (8x) or Mirror symmetry mode"))
    }

    @Test
    fun queryScriptTeleprompter_matchesWritingStudio() {
        val response = IntentMatcher.matchQuery("need a script and teleprompter for speaking")
        assertNotNull(response.matchedTool)
        assertEquals("writing", response.suggestedStudioId)
    }

    @Test
    fun queryFloorplanInterior_matchesInteriorStudio() {
        val response = IntentMatcher.matchQuery("ghar ka room plan design banana hai")
        assertNotNull(response.matchedTool)
        assertEquals("interior", response.suggestedStudioId)
        assertTrue(response.explanationText.contains("Drag furniture items"))
    }

    @Test
    fun queryUnknown_returnsHelpfulGuidance() {
        val response = IntentMatcher.matchQuery("what is quantum computing")
        assertEquals(null, response.matchedTool)
        assertEquals("studios", response.suggestedStudioId)
        assertEquals("Kreavilo Creative Assistant", response.title)
        assertTrue(response.explanationText.contains("I can guide you across any creative task!"))
    }
}
