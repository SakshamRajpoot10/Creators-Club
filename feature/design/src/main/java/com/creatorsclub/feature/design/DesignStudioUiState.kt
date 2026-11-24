package com.creatorsclub.feature.design

import android.net.Uri
import androidx.compose.ui.graphics.Color
import com.creatorsclub.engine.canvas.model.CanvasElement
import com.creatorsclub.engine.canvas.model.DesignTemplate
import java.io.File

enum class DesignStudioTab {
    TEMPLATES,
    TEXT,
    SHAPES,
    STICKERS,
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
    val importedStickers: List<File> = emptyList(),
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val isFavorite: Boolean = false,
    val projectId: String? = null,
    val isProjectTrashed: Boolean = false,
    val showTrashConfirmDialog: Boolean = false,
    val editingTextElement: com.creatorsclub.engine.canvas.model.TextElement? = null,
    val exportSuccessUri: Uri? = null,
    val errorMessage: String? = null
)
