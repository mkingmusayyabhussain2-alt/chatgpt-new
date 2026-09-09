package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.AiMode
import com.example.data.model.ChatMessage
import com.example.data.model.ChatSession
import com.example.data.model.MessageSender
import com.example.data.model.MessageType
import com.example.data.model.VideoMetadata

@Entity(tableName = "chat_sessions")
data class ChatSessionEntity(
    @PrimaryKey val id: String,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long,
    val mode: String,
    val previewSnippet: String
) {
    fun toDomain(): ChatSession = ChatSession(
        id = id,
        title = title,
        createdAt = createdAt,
        updatedAt = updatedAt,
        mode = runCatching { AiMode.valueOf(mode) }.getOrDefault(AiMode.OMNI),
        previewSnippet = previewSnippet
    )

    companion object {
        fun fromDomain(session: ChatSession): ChatSessionEntity = ChatSessionEntity(
            id = session.id,
            title = session.title,
            createdAt = session.createdAt,
            updatedAt = session.updatedAt,
            mode = session.mode.name,
            previewSnippet = session.previewSnippet
        )
    }
}

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val sender: String,
    val content: String,
    val timestamp: Long,
    val type: String,
    val mediaUrl: String? = null,
    val mediaPrompt: String? = null,
    val codeLanguage: String? = null,
    val localDrawableRes: Int? = null
) {
    fun toDomain(): ChatMessage {
        val msgType = runCatching { MessageType.valueOf(type) }.getOrDefault(MessageType.TEXT)
        val videoMeta = if (msgType == MessageType.VIDEO) {
            VideoMetadata(
                prompt = mediaPrompt ?: content,
                resolution = "1080p",
                aspectRatio = "16:9",
                durationSeconds = 5,
                sampleDrawableRes = localDrawableRes
            )
        } else null

        return ChatMessage(
            id = id,
            sessionId = sessionId,
            sender = runCatching { MessageSender.valueOf(sender) }.getOrDefault(MessageSender.AI),
            content = content,
            timestamp = timestamp,
            type = msgType,
            mediaUrl = mediaUrl,
            mediaPrompt = mediaPrompt,
            codeLanguage = codeLanguage,
            videoMetadata = videoMeta,
            isStreaming = false,
            isError = false,
            localDrawableRes = localDrawableRes
        )
    }

    companion object {
        fun fromDomain(msg: ChatMessage): ChatMessageEntity = ChatMessageEntity(
            id = msg.id,
            sessionId = msg.sessionId,
            sender = msg.sender.name,
            content = msg.content,
            timestamp = msg.timestamp,
            type = msg.type.name,
            mediaUrl = msg.mediaUrl,
            mediaPrompt = msg.mediaPrompt,
            codeLanguage = msg.codeLanguage,
            localDrawableRes = msg.localDrawableRes
        )
    }
}
