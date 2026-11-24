package com.creatorsclub.core.navigation

import kotlinx.serialization.Serializable

sealed interface KreaviloDestination {

    // Main Bottom Navigation Tabs
    @Serializable
    data object Home : KreaviloDestination

    @Serializable
    data object Studios : KreaviloDestination

    @Serializable
    data object Create : KreaviloDestination

    @Serializable
    data object Projects : KreaviloDestination

    @Serializable
    data object Settings : KreaviloDestination

    // Creative Studios
    @Serializable
    data class PhotoStudio(
        val projectId: String? = null,
        val imageUri: String? = null
    ) : KreaviloDestination

    @Serializable
    data class DesignStudio(
        val projectId: String? = null,
        val templateId: String? = null
    ) : KreaviloDestination

    @Serializable
    data class VideoStudio(
        val projectId: String? = null
    ) : KreaviloDestination

    @Serializable
    data class DrawingStudio(
        val projectId: String? = null
    ) : KreaviloDestination

    @Serializable
    data class ARDrawingStudio(
        val referenceImageUri: String? = null
    ) : KreaviloDestination

    @Serializable
    data class ProductStudio(
        val imageUri: String? = null
    ) : KreaviloDestination

    @Serializable
    data class InteriorStudio(
        val projectId: String? = null
    ) : KreaviloDestination

    @Serializable
    data class WritingStudio(
        val projectId: String? = null
    ) : KreaviloDestination

    @Serializable
    data class AudioStudio(
        val projectId: String? = null
    ) : KreaviloDestination

    @Serializable
    data class BannerStudio(
        val projectId: String? = null,
        val preset: String? = null
    ) : KreaviloDestination

    // Creator AI Full-Screen Experience
    @Serializable
    data class CreatorAI(
        val initialPrompt: String? = null
    ) : KreaviloDestination
}
