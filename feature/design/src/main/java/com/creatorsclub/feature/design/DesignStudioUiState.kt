package com.creatorsclub.feature.design

import android.net.Uri
import androidx.compose.ui.graphics.Color
import com.creatorsclub.engine.canvas.model.CanvasElement
import com.creatorsclub.engine.canvas.model.DesignTemplate

enum class DesignStudioTab {
    TEMPLATES,
    TEXT,
    SHAPES,
    BACKGROUND
}

data class DesignStudioUiState(
    val isLoading: Boolean = false,
    val canvasWidth: Int = 1080,
    val canvasHeight: Int = 1080,
    val backgroundColor: Color = Color.White,
    val elements: List<CanvasElement> = emptyList(),
    val selectedElementId: String? = null,
    val activeTab: DesignStudioTab = DesignStudioTab.TEMPLATES,
    val availableTemplates: List<DesignTemplate> = emptyList(),
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val exportSuccessUri: Uri? = null,
    val errorMessage: String? = null
)
