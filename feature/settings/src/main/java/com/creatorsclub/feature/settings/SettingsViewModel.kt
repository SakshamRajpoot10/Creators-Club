package com.creatorsclub.feature.settings

import android.app.Application
import android.net.Uri
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

data class CreatorAssetStats(
    val fontsCount: Int = 0,
    val audioCount: Int = 0,
    val stickersCount: Int = 0,
    val bannersCount: Int = 0
)

data class SettingsUiState(
    val cacheSizeMb: Float = 0f,
    val models: List<ModelInfo> = emptyList(),
    val appVersion: String = "1.0.0-PROD",
    val author: String = "Saksham Rajpoot",
    val isCacheCleared: Boolean = false,
    val assetStats: CreatorAssetStats = CreatorAssetStats(),
    val statusMessage: String? = null
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
        refreshCreatorHubStats()
    }

    private fun refreshCacheSize() {
        val bytes = storageManager.getCacheDir().walkTopDown().filter { it.isFile }.map { it.length() }.sum()
        _uiState.update { it.copy(cacheSizeMb = bytes / (1024f * 1024f)) }
    }

    fun refreshCreatorHubStats() {
        val fonts = storageManager.listImportedAssets("fonts").size
        val audio = storageManager.listImportedAssets("audio").size
        val stickers = storageManager.listImportedAssets("stickers").size
        val banners = storageManager.listImportedAssets("banners").size
        _uiState.update {
            it.copy(
                assetStats = CreatorAssetStats(
                    fontsCount = fonts,
                    audioCount = audio,
                    stickersCount = stickers,
                    bannersCount = banners
                )
            )
        }
    }

    fun importAsset(uri: Uri, subfolder: String, filename: String) {
        viewModelScope.launch {
            val result = storageManager.importAssetFromUri(uri, subfolder, filename)
            if (result.isSuccess) {
                refreshCreatorHubStats()
                _uiState.update { it.copy(statusMessage = "Successfully imported $filename into $subfolder library!") }
            } else {
                _uiState.update { it.copy(statusMessage = "Failed to import asset: ${result.exceptionOrNull()?.message}") }
            }
        }
    }

    fun clearCache() {
        viewModelScope.launch {
            storageManager.clearCache()
            _uiState.update { it.copy(cacheSizeMb = 0f, isCacheCleared = true) }
        }
    }

    fun dismissStatusMessage() {
        _uiState.update { it.copy(statusMessage = null) }
    }
}
