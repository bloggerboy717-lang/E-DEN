package com.example.data.repository

import com.example.data.local.ChatDao
import com.example.data.preferences.SettingsRepository
import com.example.data.remote.GeminiApiService
import com.example.model.ChatMessage
import com.example.model.GeminiContent
import com.example.model.GeminiPart
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class ChatRepository(
    private val chatDao: ChatDao,
    private val apiService: GeminiApiService,
    private val settingsRepository: SettingsRepository
) {

    fun getMessagesForSession(sessionId: String): Flow<List<ChatMessage>> {
        return chatDao.getMessagesBySession(sessionId)
    }

    suspend fun clearSession(sessionId: String) {
        chatDao.clearSession(sessionId)
    }

    suspend fun clearAll() {
        chatDao.clearAll()
    }

    suspend fun sendMessage(
        sessionId: String,
        userPrompt: String,
        onChunkReceived: suspend (partialText: String) -> Unit = {}
    ): Result<String> {
        val userMessage = ChatMessage(
            sessionId = sessionId,
            role = "user",
            content = userPrompt.trim(),
            timestamp = System.currentTimeMillis()
        )
        chatDao.insertMessage(userMessage)

        val prefs = settingsRepository.userPreferencesFlow.first()
        val apiKey = prefs.effectiveApiKey
        val model = prefs.selectedModel

        if (apiKey.isBlank()) {
            val errorMsg = "एपीआई की नहीं डाला गया है! कृपया सेटिंग्स में जाकर अपना Google Gemini API Key दर्ज करें।"
            val assistantError = ChatMessage(
                sessionId = sessionId,
                role = "model",
                content = "त्रुटि: $errorMsg",
                timestamp = System.currentTimeMillis(),
                isError = true
            )
            chatDao.insertMessage(assistantError)
            return Result.failure(IllegalStateException(errorMsg))
        }

        // Fetch past conversation for context
        val existingMessages = chatDao.getMessagesBySession(sessionId).first()
        // Take recent conversation context (last 10 messages excluding the newly added user message)
        val conversationHistory = existingMessages
            .filter { !it.isError && it.id != userMessage.id }
            .takeLast(10)
            .map {
                GeminiContent(
                    role = if (it.role == "user") "user" else "model",
                    parts = listOf(GeminiPart(text = it.content))
                )
            }

        // Insert initial placeholder assistant message
        val assistantPlaceholder = ChatMessage(
            sessionId = sessionId,
            role = "model",
            content = "",
            timestamp = System.currentTimeMillis(),
            isStreaming = true
        )
        val assistantMsgId = chatDao.insertMessage(assistantPlaceholder)

        val fullResponseBuilder = StringBuilder()

        return try {
            apiService.streamGenerateContent(
                apiKey = apiKey,
                model = model,
                history = conversationHistory,
                prompt = userPrompt
            ).collect { chunk ->
                fullResponseBuilder.append(chunk)
                val currentText = fullResponseBuilder.toString()
                chatDao.updateMessage(
                    ChatMessage(
                        id = assistantMsgId,
                        sessionId = sessionId,
                        role = "model",
                        content = currentText,
                        timestamp = assistantPlaceholder.timestamp,
                        isStreaming = true
                    )
                )
                onChunkReceived(currentText)
            }

            val finalOutput = fullResponseBuilder.toString().ifEmpty {
                "I am here to assist you."
            }

            chatDao.updateMessage(
                ChatMessage(
                    id = assistantMsgId,
                    sessionId = sessionId,
                    role = "model",
                    content = finalOutput,
                    timestamp = assistantPlaceholder.timestamp,
                    isStreaming = false
                )
            )

            Result.success(finalOutput)
        } catch (e: Exception) {
            val errorText = e.message ?: "An unexpected error occurred while communicating with Gemini."
            chatDao.updateMessage(
                ChatMessage(
                    id = assistantMsgId,
                    sessionId = sessionId,
                    role = "model",
                    content = "Error: $errorText",
                    timestamp = assistantPlaceholder.timestamp,
                    isStreaming = false,
                    isError = true
                )
            )
            Result.failure(e)
        }
    }
}
