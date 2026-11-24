package com.creatorsclub.feature.ai_assistant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.creatorsclub.engine.ai.matcher.AiAssistantResponse
import com.creatorsclub.engine.ai.matcher.IntentMatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class ChatMessage(
    val id: String,
    val isUser: Boolean,
    val text: String,
    val assistantResponse: AiAssistantResponse? = null
)

data class CreatorAiUiState(
    val messages: List<ChatMessage> = emptyList(),
    val currentInput: String = "",
    val isSpeaking: Boolean = false
)

@HiltViewModel
class CreatorAiViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(
        CreatorAiUiState(
            messages = listOf(
                ChatMessage(
                    id = "msg_welcome",
                    isUser = false,
                    text = "Hello! I am Creator AI, your creative companion. How can I help you create today?",
                    assistantResponse = IntentMatcher.matchQuery("how to create product ad")
                )
            )
        )
    )
    val uiState: StateFlow<CreatorAiUiState> = _uiState.asStateFlow()

    fun updateInput(text: String) {
        _uiState.update { it.copy(currentInput = text) }
    }

    fun submitQuery(query: String? = null) {
        val q = query ?: _uiState.value.currentInput
        if (q.isBlank()) return

        val userMessage = ChatMessage(
            id = "msg_user_${System.currentTimeMillis()}",
            isUser = true,
            text = q
        )

        val aiResult = IntentMatcher.matchQuery(q)

        val aiMessage = ChatMessage(
            id = "msg_ai_${System.currentTimeMillis()}",
            isUser = false,
            text = aiResult.explanationText,
            assistantResponse = aiResult
        )

        _uiState.update {
            it.copy(
                messages = it.messages + userMessage + aiMessage,
                currentInput = ""
            )
        }
    }
}
