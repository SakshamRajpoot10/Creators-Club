package com.creatorsclub.feature.audio

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.creatorsclub.core.storage.manager.StorageManager
import com.creatorsclub.engine.audio.recorder.AudioRecorder
import com.creatorsclub.engine.audio.tts.NativeTextToSpeech
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class AudioStudioUiState(
    val isRecording: Boolean = false,
    val recordingDurationSec: Int = 0,
    val amplitudeWaveforms: List<Float> = emptyList(),
    val ttsInputText: String = "",
    val isSpeakingTts: Boolean = false,
    val savedRecordings: List<File> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class AudioStudioViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val recorder = AudioRecorder(application)
    private val tts = NativeTextToSpeech(application)
    private val storageManager = StorageManager(application)

    private val _uiState = MutableStateFlow(AudioStudioUiState())
    val uiState: StateFlow<AudioStudioUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            tts.initialize()
        }
    }

    fun startRecording() {
        val outputFile = File(
            storageManager.getProjectsDir(),
            "Kreavilo_Voice_${System.currentTimeMillis()}.m4a"
        )
        val result = recorder.startRecording(outputFile)
        result.onSuccess {
            _uiState.update { it.copy(isRecording = true, recordingDurationSec = 0, amplitudeWaveforms = emptyList()) }
            pollAmplitudes()
        }.onFailure { err ->
            _uiState.update { it.copy(errorMessage = err.message) }
        }
    }

    private fun pollAmplitudes() {
        viewModelScope.launch {
            while (_uiState.value.isRecording) {
                delay(100)
                val amp = recorder.getMaxAmplitude()
                val normalized = (amp / 32767f).coerceIn(0.1f, 1f)
                _uiState.update {
                    val updatedWave = (it.amplitudeWaveforms + normalized).takeLast(40)
                    it.copy(amplitudeWaveforms = updatedWave, recordingDurationSec = it.recordingDurationSec + 1)
                }
            }
        }
    }

    fun stopRecording() {
        val result = recorder.stopRecording()
        result.onSuccess { file ->
            _uiState.update {
                it.copy(
                    isRecording = false,
                    savedRecordings = it.savedRecordings + file
                )
            }
        }.onFailure { err ->
            _uiState.update { it.copy(isRecording = false, errorMessage = err.message) }
        }
    }

    fun updateTtsText(text: String) {
        _uiState.update { it.copy(ttsInputText = text) }
    }

    fun speakTts(languageCode: String = "en-IN") {
        val text = _uiState.value.ttsInputText
        if (text.isNotBlank()) {
            _uiState.update { it.copy(isSpeakingTts = true) }
            tts.speak(text, languageCode)
        }
    }

    fun stopTts() {
        tts.stop()
        _uiState.update { it.copy(isSpeakingTts = false) }
    }

    override fun onCleared() {
        super.onCleared()
        tts.release()
    }
}
