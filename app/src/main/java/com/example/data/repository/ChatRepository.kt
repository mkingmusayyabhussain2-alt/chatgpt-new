package com.example.data.repository

import com.example.data.local.ChatDao
import com.example.data.local.ChatMessageEntity
import com.example.data.local.ChatSessionEntity
import com.example.data.model.AiMode
import com.example.data.model.ChatMessage
import com.example.data.model.ChatSession
import com.example.data.model.MessageSender
import com.example.data.model.MessageType
import com.example.data.model.VideoMetadata
import com.example.data.remote.GeminiClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.util.UUID

class ChatRepository(
    private val chatDao: ChatDao,
    private val geminiClient: GeminiClient
) {

    fun getAllSessions(): Flow<List<ChatSession>> {
        return chatDao.getAllSessions().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun getMessagesForSession(sessionId: String): Flow<List<ChatMessage>> {
        return chatDao.getMessagesForSession(sessionId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun createNewSession(mode: AiMode = AiMode.OMNI, title: String = "New Chat"): ChatSession {
        val session = ChatSession(
            id = UUID.randomUUID().toString(),
            title = title,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            mode = mode,
            previewSnippet = "Start a conversation"
        )
        chatDao.insertSession(ChatSessionEntity.fromDomain(session))
        return session
    }

    suspend fun updateSessionTitle(sessionId: String, title: String, snippet: String) {
        val existing = chatDao.getSessionById(sessionId) ?: return
        chatDao.updateSession(
            existing.copy(
                title = title,
                previewSnippet = snippet,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteSession(sessionId: String) {
        chatDao.deleteMessagesForSession(sessionId)
        chatDao.deleteSession(sessionId)
    }

    suspend fun clearAll() {
        chatDao.deleteAllMessages()
        chatDao.deleteAllSessions()
    }

    suspend fun saveUserMessage(sessionId: String, text: String): ChatMessage {
        val userMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            sessionId = sessionId,
            sender = MessageSender.USER,
            content = text,
            timestamp = System.currentTimeMillis(),
            type = MessageType.TEXT
        )
        chatDao.insertMessage(ChatMessageEntity.fromDomain(userMsg))
        return userMsg
    }

    suspend fun saveAiMessage(message: ChatMessage) {
        chatDao.insertMessage(ChatMessageEntity.fromDomain(message))
    }

    suspend fun deleteMessage(messageId: String) {
        chatDao.deleteMessage(messageId)
    }

    suspend fun generateAiChatStream(
        sessionId: String,
        history: List<ChatMessage>,
        prompt: String,
        mode: AiMode,
        onChunkReceived: (accumulatedText: String) -> Unit
    ): String {
        return geminiClient.streamChat(history, prompt, mode, onChunkReceived)
    }

    suspend fun generateImage(
        prompt: String,
        aspectRatio: String = "1:1"
    ) = geminiClient.generateImage(prompt, aspectRatio)

    suspend fun generateVideo(
        prompt: String,
        aspectRatio: String = "16:9",
        duration: Int = 5
    ) = geminiClient.generateVideo(prompt, aspectRatio, duration)
}
