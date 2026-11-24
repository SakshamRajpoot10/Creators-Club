package com.creatorsclub.feature.video

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.creatorsclub.core.storage.manager.StorageManager
import com.creatorsclub.engine.video.exporter.Media3VideoExporter
import com.creatorsclub.engine.video.model.VideoClip
import com.creatorsclub.engine.video.model.VideoTransition
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID
import javax.inject.Inject

data class VideoStudioUiState(
    val clips: List<VideoClip> = emptyList(),
    val selectedClipId: String? = null,
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L,
    val totalDurationMs: Long = 0L,
    val isLoading: Boolean = false,
    val exportSuccessFile: File? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class VideoStudioViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val storageManager = StorageManager(application)
    val player = ExoPlayer.Builder(application).build()

    private val _uiState = MutableStateFlow(VideoStudioUiState())
    val uiState: StateFlow<VideoStudioUiState> = _uiState.asStateFlow()

    fun addClip(uri: Uri, durationMs: Long = 5000L) {
        val newClip = VideoClip(
            id = "clip_${UUID.randomUUID().toString().take(6)}",
            uri = uri,
            name = "Clip ${_uiState.value.clips.size + 1}",
            startMs = 0L,
            endMs = durationMs,
            durationMs = durationMs
        )
        val updatedClips = _uiState.value.clips + newClip
        _uiState.update {
            it.copy(
                clips = updatedClips,
                selectedClipId = newClip.id,
                totalDurationMs = updatedClips.sumOf { c -> c.effectiveDurationMs }
            )
        }
        syncPlayerWithClips()
    }

    fun selectClip(id: String) {
        _uiState.update { it.copy(selectedClipId = id) }
    }

    fun trimSelectedClip(startMs: Long, endMs: Long) {
        val id = _uiState.value.selectedClipId ?: return
        val updated = _uiState.value.clips.map { clip ->
            if (clip.id == id) clip.copy(startMs = startMs, endMs = endMs) else clip
        }
        _uiState.update {
            it.copy(
                clips = updated,
                totalDurationMs = updated.sumOf { c -> c.effectiveDurationMs }
            )
        }
        syncPlayerWithClips()
    }

    fun setClipSpeed(speed: Float) {
        val id = _uiState.value.selectedClipId ?: return
        val updated = _uiState.value.clips.map { clip ->
            if (clip.id == id) clip.copy(speed = speed) else clip
        }
        _uiState.update {
            it.copy(
                clips = updated,
                totalDurationMs = updated.sumOf { c -> c.effectiveDurationMs }
            )
        }
    }

    fun setClipTransition(transition: VideoTransition) {
        val id = _uiState.value.selectedClipId ?: return
        val updated = _uiState.value.clips.map { clip ->
            if (clip.id == id) clip.copy(transition = transition) else clip
        }
        _uiState.update { it.copy(clips = updated) }
    }

    fun deleteSelectedClip() {
        val id = _uiState.value.selectedClipId ?: return
        val updated = _uiState.value.clips.filter { it.id != id }
        _uiState.update {
            it.copy(
                clips = updated,
                selectedClipId = updated.firstOrNull()?.id,
                totalDurationMs = updated.sumOf { c -> c.endMs - c.startMs }
            )
        }
        syncPlayerWithClips()
    }

    fun togglePlayPause() {
        if (player.isPlaying) {
            player.pause()
            _uiState.update { it.copy(isPlaying = false) }
        } else {
            player.play()
            _uiState.update { it.copy(isPlaying = true) }
        }
    }

    fun dismissExportMessage() {
        _uiState.update { it.copy(exportSuccessFile = null) }
    }

    private fun syncPlayerWithClips() {
        player.clearMediaItems()
        for (clip in _uiState.value.clips) {
            player.addMediaItem(MediaItem.fromUri(clip.uri))
        }
        player.prepare()
    }

    fun exportVideo() {
        val clips = _uiState.value.clips
        if (clips.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val outputFile = File(storageManager.getProjectsDir(), "Kreavilo_Reel_${System.currentTimeMillis()}.mp4")
            val result = Media3VideoExporter.exportClips(getApplication(), clips, outputFile)
            result.onSuccess { file ->
                _uiState.update { it.copy(isLoading = false, exportSuccessFile = file) }
            }.onFailure { err ->
                _uiState.update { it.copy(isLoading = false, errorMessage = err.message) }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        player.release()
    }
}
