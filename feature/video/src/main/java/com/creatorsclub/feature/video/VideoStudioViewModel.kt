package com.creatorsclub.feature.video

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.creatorsclub.core.database.KreaviloDatabase
import com.creatorsclub.core.database.model.ProjectEntity
import com.creatorsclub.core.storage.manager.StorageManager
import com.creatorsclub.engine.video.exporter.Media3VideoExporter
import com.creatorsclub.engine.video.model.VideoClip
import com.creatorsclub.engine.video.model.VideoTransition
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID
import javax.inject.Inject

enum class VideoToolTab {
    TIMELINE,
    AUDIO,
    SPEED,
    TRANSITIONS,
    EXPORT
}

data class VideoStudioUiState(
    val clips: List<VideoClip> = emptyList(),
    val selectedClipId: String? = null,
    val backgroundAudioUri: Uri? = null,
    val backgroundAudioName: String? = null,
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L,
    val totalDurationMs: Long = 0L,
    val activeTab: VideoToolTab = VideoToolTab.TIMELINE,
    val isFavorite: Boolean = false,
    val projectId: String? = null,
    val isProjectTrashed: Boolean = false,
    val showTrashConfirmDialog: Boolean = false,
    val exportResolution: String = "1080p",
    val isLoading: Boolean = false,
    val exportSuccessFile: File? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class VideoStudioViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val storageManager = StorageManager(application)
    private val db = KreaviloDatabase.getInstance(application)
    private val projectDao = db.projectDao()

    val player = ExoPlayer.Builder(application).build()

    private val _uiState = MutableStateFlow(VideoStudioUiState())
    val uiState: StateFlow<VideoStudioUiState> = _uiState.asStateFlow()

    private var playbackPollingJob: Job? = null

    init {
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _uiState.update { it.copy(isPlaying = isPlaying) }
                if (isPlaying) {
                    startPositionPolling()
                } else {
                    stopPositionPolling()
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    _uiState.update { it.copy(isPlaying = false, currentPositionMs = 0L) }
                    player.seekTo(0)
                }
            }
        })
    }

    private fun startPositionPolling() {
        playbackPollingJob?.cancel()
        playbackPollingJob = viewModelScope.launch {
            while (player.isPlaying) {
                _uiState.update { it.copy(currentPositionMs = player.currentPosition) }
                delay(200)
            }
        }
    }

    private fun stopPositionPolling() {
        playbackPollingJob?.cancel()
        _uiState.update { it.copy(currentPositionMs = player.currentPosition) }
    }

    fun selectTab(tab: VideoToolTab) {
        _uiState.update { it.copy(activeTab = tab) }
    }

    fun setBackgroundAudio(uri: Uri?, name: String? = null) {
        _uiState.update {
            it.copy(
                backgroundAudioUri = uri,
                backgroundAudioName = name ?: uri?.lastPathSegment ?: "Imported Audio Track"
            )
        }
        persistAutosave()
    }

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
        ensureProjectCreated()
        persistAutosave()
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
        persistAutosave()
    }

    fun splitClipAtCurrentPosition() {
        val id = _uiState.value.selectedClipId ?: return
        val currentClips = _uiState.value.clips
        val clip = currentClips.find { it.id == id } ?: return

        val currentPos = player.currentPosition
        if (currentPos > 500 && currentPos < clip.durationMs - 500) {
            val left = clip.copy(endMs = currentPos)
            val right = clip.copy(
                id = "clip_${UUID.randomUUID().toString().take(6)}",
                name = "${clip.name} (B)",
                startMs = currentPos
            )
            val index = currentClips.indexOf(clip)
            val updated = currentClips.toMutableList()
            updated[index] = left
            updated.add(index + 1, right)

            _uiState.update {
                it.copy(
                    clips = updated,
                    selectedClipId = right.id,
                    totalDurationMs = updated.sumOf { c -> c.effectiveDurationMs }
                )
            }
            syncPlayerWithClips()
            persistAutosave()
        }
    }

    fun moveClipLeft(id: String) {
        val clips = _uiState.value.clips.toMutableList()
        val index = clips.indexOfFirst { it.id == id }
        if (index > 0) {
            val temp = clips[index]
            clips[index] = clips[index - 1]
            clips[index - 1] = temp
            _uiState.update { it.copy(clips = clips) }
            syncPlayerWithClips()
            persistAutosave()
        }
    }

    fun moveClipRight(id: String) {
        val clips = _uiState.value.clips.toMutableList()
        val index = clips.indexOfFirst { it.id == id }
        if (index in 0 until clips.size - 1) {
            val temp = clips[index]
            clips[index] = clips[index + 1]
            clips[index + 1] = temp
            _uiState.update { it.copy(clips = clips) }
            syncPlayerWithClips()
            persistAutosave()
        }
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
        persistAutosave()
    }

    fun setClipTransition(transition: VideoTransition) {
        val id = _uiState.value.selectedClipId ?: return
        val updated = _uiState.value.clips.map { clip ->
            if (clip.id == id) clip.copy(transition = transition) else clip
        }
        _uiState.update { it.copy(clips = updated) }
        persistAutosave()
    }

    fun deleteSelectedClip() {
        val id = _uiState.value.selectedClipId ?: return
        val updated = _uiState.value.clips.filter { it.id != id }
        _uiState.update {
            it.copy(
                clips = updated,
                selectedClipId = updated.firstOrNull()?.id,
                totalDurationMs = updated.sumOf { c -> c.effectiveDurationMs }
            )
        }
        syncPlayerWithClips()
        persistAutosave()
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

    fun seekTo(positionMs: Long) {
        player.seekTo(positionMs)
        _uiState.update { it.copy(currentPositionMs = positionMs) }
    }

    fun seekRelative(deltaMs: Long) {
        val newPos = (player.currentPosition + deltaMs).coerceIn(0L, _uiState.value.totalDurationMs)
        seekTo(newPos)
    }

    // --- Favorites & Soft Deletion (5-Day Retention) ---

    fun setShowTrashConfirmDialog(show: Boolean) {
        _uiState.update { it.copy(showTrashConfirmDialog = show) }
    }

    fun toggleFavorite() {
        val newFav = !_uiState.value.isFavorite
        _uiState.update { it.copy(isFavorite = newFav) }
        val projectId = _uiState.value.projectId ?: return
        viewModelScope.launch(Dispatchers.IO) {
            projectDao.setFavorite(projectId, newFav)
        }
    }

    fun trashProject() {
        val projectId = _uiState.value.projectId ?: return
        viewModelScope.launch(Dispatchers.IO) {
            projectDao.softDeleteProject(projectId)
            _uiState.update {
                it.copy(
                    isProjectTrashed = true,
                    showTrashConfirmDialog = false
                )
            }
        }
    }

    // --- Room Persistence ---

    private fun ensureProjectCreated() {
        if (_uiState.value.projectId != null) return
        val newId = UUID.randomUUID().toString()
        _uiState.update { it.copy(projectId = newId) }

        viewModelScope.launch(Dispatchers.IO) {
            val entity = ProjectEntity(
                projectId = newId,
                name = "Video Project",
                studioType = "VIDEO",
                createdAt = System.currentTimeMillis(),
                modifiedAt = System.currentTimeMillis(),
                canvasWidth = 1920,
                canvasHeight = 1080,
                aspectRatio = "16:9",
                thumbnailPath = "",
                projectFilePath = "",
                isFavorite = _uiState.value.isFavorite,
                isDeleted = false,
                deletedAt = null
            )
            projectDao.insertProject(entity)
        }
    }

    private fun persistAutosave() {
        val projectId = _uiState.value.projectId ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val existing = projectDao.getProjectById(projectId) ?: return@launch
            val updated = existing.copy(
                modifiedAt = System.currentTimeMillis(),
                isFavorite = _uiState.value.isFavorite
            )
            projectDao.updateProject(updated)
        }
    }

    fun dismissExportMessage() {
        _uiState.update { it.copy(exportSuccessFile = null) }
    }

    fun dismissErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
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
        playbackPollingJob?.cancel()
        player.release()
    }
}
