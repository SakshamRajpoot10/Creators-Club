package com.creatorsclub.feature.interior

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

enum class FurnitureType(val displayName: String, val widthDp: Float, val heightDp: Float, val color: Color) {
    SOFA("3-Seater Sofa", 120f, 55f, Color(0xFF405BC7)),
    BED("King Bed", 110f, 130f, Color(0xFF7664C7)),
    DESK("Work Desk", 90f, 50f, Color(0xFF237A57)),
    DINING_TABLE("Dining Table", 100f, 80f, Color(0xFFF59E0B)),
    WARDROBE("Wardrobe", 100f, 45f, Color(0xFF182033)),
    PLANT("Indoor Plant", 35f, 35f, Color(0xFF10B981))
}

data class PlacedFurniture(
    val id: String,
    val type: FurnitureType,
    val x: Float,
    val y: Float,
    val rotation: Float = 0f,
    val scale: Float = 1.0f
)
