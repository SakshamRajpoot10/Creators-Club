package com.creatorsclub.feature.settings

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.creatorsclub.core.storage.manager.StorageManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    val bannersCount: Int = 0,
    val videosCount: Int = 0,
    val imagesCount: Int = 0
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
                ModelInfo("Sobel Gradient Operator", "Camera AR pencil outline generator", "0 MB (Native)", true)
            )
        )
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            refreshCacheSize()
            refreshCreatorHubStats()
        }
    }

    fun refreshCacheSize() {
        viewModelScope.launch(Dispatchers.IO) {
            val bytes = runCatching {
                val dir = storageManager.getCacheDir()
                if (dir.exists()) {
                    dir.walkTopDown().filter { it.isFile }.map { it.length() }.sum()
                } else {
                    0L
                }
            }.getOrDefault(0L)
            val mb = bytes / (1024f * 1024f)
            _uiState.update { it.copy(cacheSizeMb = mb) }
        }
    }

    fun refreshCreatorHubStats() {
        viewModelScope.launch(Dispatchers.IO) {
            val stats = runCatching {
                val fonts = storageManager.listImportedAssets("fonts").size
                val audio = storageManager.listImportedAssets("audio").size
                val stickers = storageManager.listImportedAssets("stickers").size
                val banners = storageManager.listImportedAssets("banners").size
                val videos = storageManager.listImportedAssets("videos").size
                val images = storageManager.listImportedAssets("images").size
                CreatorAssetStats(
                    fontsCount = fonts,
                    audioCount = audio,
                    stickersCount = stickers,
                    bannersCount = banners,
                    videosCount = videos,
                    imagesCount = images
                )
            }.getOrDefault(CreatorAssetStats())
            _uiState.update { it.copy(assetStats = stats) }
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
