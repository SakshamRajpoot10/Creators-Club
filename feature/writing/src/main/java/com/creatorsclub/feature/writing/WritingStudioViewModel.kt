package com.creatorsclub.feature.writing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WritingStudioUiState(
    val scriptContent: String = "",
    val wordCount: Int = 0,
    val characterCount: Int = 0,
    val estimatedReadingTimeMinutes: Float = 0f,
    val isTeleprompterActive: Boolean = false,
    val teleprompterScrollSpeed: Int = 2,
    val teleprompterFontSizeSp: Float = 24f,
    val isTeleprompterMirrored: Boolean = false,
    val isTeleprompterScrolling: Boolean = false,
    val exportSuccessMessage: String? = null
)

@HiltViewModel
class WritingStudioViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(WritingStudioUiState())
    val uiState: StateFlow<WritingStudioUiState> = _uiState.asStateFlow()

    fun updateContent(text: String) {
        val words = if (text.isBlank()) 0 else text.trim().split("\\s+".toRegex()).size
        val chars = text.length
        val readingTime = words / 130f // ~130 words per minute average speech rate

        _uiState.update {
            it.copy(
                scriptContent = text,
                wordCount = words,
                characterCount = chars,
                estimatedReadingTimeMinutes = readingTime
            )
        }
    }

    fun applyTemplate(templateType: String) {
        val templateText = when (templateType) {
            "YOUTUBE_HOOK" -> """
# VIDEO TITLE: [Insert Catchy Video Title Here]

## 1. THE HOOK (0:00 - 0:15)
- State the shocking problem or intriguing question immediately.
- "In the next 5 minutes, you will discover why..."

## 2. THE PROMISE (0:15 - 0:45)
- What will the viewer achieve by watching until the end?

## 3. CORE CONTENT (0:45 - 4:00)
- Point 1: [Key Insight]
- Point 2: [Demonstration / Proof]
- Point 3: [Common Mistake to Avoid]

## 4. CALL TO ACTION & OUTRO (4:00 - 4:30)
- "If this helped you, like and subscribe for more creative guides!"
            """.trimIndent()

            "REEL_SCRIPT" -> """
# 30-SECOND REEL SCRIPT

[0-3s] VISUAL HOOK: [Show unexpected result or quick cut]
AUDIO: "Stop doing this mistake in 2026..."

[3-15s] THE SOLUTION:
- Step 1: Open Kreavilo Studio.
- Step 2: Choose your template.
- Step 3: Export in 1080p without watermark.

[15-30s] CTA: "Save this reel for later and share with a creator friend!"
            """.trimIndent()

            else -> """
# PRODUCT PITCH SCRIPT

Attention all [Target Audience]!
Are you tired of [Common Pain Point]?
Introducing [Product Name] — the easiest way to [Key Benefit].
Tap the link below to get yours today!
            """.trimIndent()
        }

        updateContent(templateText)
    }

    fun toggleTeleprompter(active: Boolean) {
        _uiState.update { it.copy(isTeleprompterActive = active, isTeleprompterScrolling = false) }
    }

    fun toggleTeleprompterScrolling() {
        val willScroll = !_uiState.value.isTeleprompterScrolling
        _uiState.update { it.copy(isTeleprompterScrolling = willScroll) }
    }

    fun setTeleprompterSpeed(speed: Int) {
        _uiState.update { it.copy(teleprompterScrollSpeed = speed) }
    }

    fun setTeleprompterFontSize(sizeSp: Float) {
        _uiState.update { it.copy(teleprompterFontSizeSp = sizeSp) }
    }
}
