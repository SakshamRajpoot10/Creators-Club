package com.creatorsclub.feature.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.creatorsclub.core.storage.manager.StorageManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ModelInfo(
    val name: String,
    val purpose: String,
    val sizeMb: String,
    val isReady: Boolean
)

data class SettingsUiState(
    val cacheSizeMb: Float = 0f,
    val models: List<ModelInfo> = emptyList(),
    val appVersion: String = "1.0.0-PROD",
    val author: String = "Saksham Rajpoot",
    val isCacheCleared: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val storageManager = StorageManager(application)

    private val _uiState = MutableStateFlow(
        SettingsUiState(
            models = listOf(
                ModelInfo("MediaPipe Interactive Segmenter", "Instant foreground cutout & background removal", "9.6 MB", true),
                ModelInfo("ML Kit Text Recognition v2", "On-device OCR text extraction", "Core module", true),
                ModelInfo("Sobel Gradient Operator", "Camera AR pencil outline generator", "0 MB (Native)", true),
                ModelInfo("Android Speech Recognition", "Offline Hindi & English voice assistant", "Native", true)
            )
        )
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        refreshCacheSize()
    }

    private fun refreshCacheSize() {
        val bytes = storageManager.getCacheDir().walkTopDown().filter { it.isFile }.map { it.length() }.sum()
        _uiState.update { it.copy(cacheSizeMb = bytes / (1024f * 1024f)) }
    }

    fun clearCache() {
        viewModelScope.launch {
            storageManager.clearCache()
            _uiState.update { it.copy(cacheSizeMb = 0f, isCacheCleared = true) }
        }
    }
}
