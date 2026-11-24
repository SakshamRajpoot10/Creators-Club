package com.creatorsclub.core.storage.model

import kotlinx.serialization.Serializable

@Serializable
data class ProjectManifest(
    val formatVersion: Int = 1,
    val projectId: String,
    val name: String,
    val studioType: String,
    val createdAt: Long,
    val modifiedAt: Long,
    val canvas: CanvasProperties,
    val layers: List<LayerManifest> = emptyList(),
    val metadata: Map<String, String> = emptyMap()
)

@Serializable
data class CanvasProperties(
    val width: Int,
    val height: Int,
    val aspectRatio: String = "1:1",
    val backgroundColorHex: String = "#FFFFFF"
)

@Serializable
data class LayerManifest(
    val layerId: String,
    val type: String, // 'IMAGE', 'TEXT', 'VECTOR', 'DRAWING', 'VIDEO'
    val name: String = "Layer",
    val isVisible: Boolean = true,
    val isLocked: Boolean = false,
    val transform: LayerTransform = LayerTransform(),
    val imageProperties: ImageLayerProperties? = null,
    val textProperties: TextLayerProperties? = null
)

@Serializable
data class LayerTransform(
    val x: Float = 0f,
    val y: Float = 0f,
    val scaleX: Float = 1f,
    val scaleY: Float = 1f,
    val rotation: Float = 0f,
    val opacity: Float = 1f
)

@Serializable
data class ImageLayerProperties(
    val assetRelativePath: String,
    val brightness: Float = 0f,      // -1f to 1f
    val contrast: Float = 1f,        // 0f to 2f
    val saturation: Float = 1f,      // 0f to 2f
    val appliedFilterId: String? = null
)

@Serializable
data class TextLayerProperties(
    val text: String,
    val fontFamily: String = "Inter",
    val fontSize: Float = 24f,
    val colorHex: String = "#182033",
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val alignment: String = "CENTER"
)
