package com.creatorsclub.feature.design.banner

import android.net.Uri
import androidx.compose.ui.graphics.Color
import com.creatorsclub.engine.canvas.model.CanvasElement
import java.io.File

enum class BannerPreset(
    val title: String,
    val width: Int,
    val height: Int,
    val aspectRatioLabel: String,
    val description: String,
    val hasSafeZone: Boolean
) {
    YOUTUBE_THUMBNAIL(
        title = "YouTube Thumbnail",
        width = 1280,
        height = 720,
        aspectRatioLabel = "16:9",
        description = "Standard high-CTR 1280x720 video thumbnail with timestamp safe guide",
        hasSafeZone = true
    ),
    YOUTUBE_BANNER(
        title = "YouTube Channel Banner",
        width = 2560,
        height = 1440,
        aspectRatioLabel = "16:9",
        description = "2560x1440 channel art with 1235x338 all-device safe zone",
        hasSafeZone = true
    ),
    INSTAGRAM_POST(
        title = "Instagram Post",
        width = 1080,
        height = 1080,
        aspectRatioLabel = "1:1",
        description = "Square post for Instagram feed & Facebook",
        hasSafeZone = false
    ),
    INSTAGRAM_PORTRAIT(
        title = "Instagram Portrait",
        width = 1080,
        height = 1350,
        aspectRatioLabel = "4:5",
        description = "Vertical portrait feed post with maximum screen real-estate",
        hasSafeZone = false
    ),
    INSTAGRAM_STORY(
        title = "Story & Reel Cover",
        width = 1080,
        height = 1920,
        aspectRatioLabel = "9:16",
        description = "Full-screen vertical story and video thumbnail",
        hasSafeZone = true
    ),
    X_HEADER(
        title = "X (Twitter) Header",
        width = 1500,
        height = 500,
        aspectRatioLabel = "3:1",
        description = "Header banner for profile pages",
        hasSafeZone = true
    )
}

enum class BannerTab {
    PRESETS,
    TEXT,
    BADGES,
    BACKGROUND,
    LAYERS
}

data class BannerStudioUiState(
    val isLoading: Boolean = false,
    val activePreset: BannerPreset = BannerPreset.YOUTUBE_THUMBNAIL,
    val showSafeZoneGuides: Boolean = true,
    val backgroundColor: Color = Color(0xFF0F172A),
    val elements: List<CanvasElement> = emptyList(),
    val selectedElementId: String? = null,
    val activeTab: BannerTab = BannerTab.PRESETS,
    val importedBanners: List<File> = emptyList(),
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val exportSuccessUri: Uri? = null,
    val statusMessage: String? = null,
    val errorMessage: String? = null
)
