package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.api.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChatMessage(val content: String, val isUser: Boolean)

class AiChatViewModel : ViewModel() {
    private val _messages = MutableStateFlow<List<ChatMessage>>(
        listOf(ChatMessage("Hello! I'm your AI Botanist. Ask me any questions about your plants, diseases, or general care.", false))
    )
    val messages = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun sendMessage(userMessage: String) {
        if (userMessage.isBlank()) return

        _messages.update { it + ChatMessage(userMessage, true) }
        _isLoading.value = true

        viewModelScope.launch {
            try {
                // Build a prompt string including history could be better, but for simplicity we can send the conversation history or just the current message.
                // We'll construct a simple text prompt containing the last few messages for context.
                val historyText = _messages.value.takeLast(6).joinToString("\n") { 
                    (if (it.isUser) "User" else "Botanist") + ": " + it.content 
                }
                
                if (BuildConfig.GEMINI_API_KEY.isBlank()) {
                    _messages.update { it + ChatMessage("Error: Gemini API Key is missing. Please add it in Settings.", false) }
                    _isLoading.value = false
                    return@launch
                }

                val prompt = "You are an expert AI Botanist. Help the user with plant diagnostics, care tips, or recommendations.\n\nConversation so far:\n$historyText\nBotanist: "
                
                val request = GenerateContentRequest(
                    contents = listOf(
                        Content(
                            role = "user",
                            parts = listOf(Part(text = prompt))
                        )
                    ) // Assuming GenerationConfig isn't strictly required or defaults are fine
                )

                val response = RetrofitClient.service.generateContent(BuildConfig.GEMINI_API_KEY, request)
                val replyText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "I am having trouble connecting to my knowledge base right now."
                
                _messages.update { it + ChatMessage(replyText.trim(), false) }
            } catch (e: retrofit2.HttpException) {
                if (e.code() == 400 || e.code() == 401 || e.code() == 403) {
                     _messages.update { it + ChatMessage("Error: Invalid or missing API Key.", false) }
                } else {
                     _messages.update { it + ChatMessage("Error ${e.code()}: ${e.message}", false) }
                }
            } catch (e: Exception) {
                _messages.update { it + ChatMessage("Error: ${e.localizedMessage}", false) }
            } finally {
                _isLoading.value = false
            }
        }
    }
}
