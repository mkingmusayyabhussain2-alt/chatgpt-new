package com.example.data.model

enum class MessageSender {
    USER,
    AI
}

enum class MessageType {
    TEXT,
    CODE,
    IMAGE,
    VIDEO
}

data class VideoMetadata(
    val prompt: String,
    val resolution: String = "1080p",
    val aspectRatio: String = "16:9",
    val durationSeconds: Int = 5,
    val cameraMovement: String = "Cinematic forward drone pan",
    val fps: Int = 30,
    val isPlaying: Boolean = false,
    val sampleDrawableRes: Int? = null
)

data class ChatMessage(
    val id: String,
    val sessionId: String,
    val sender: MessageSender,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val type: MessageType = MessageType.TEXT,
    val mediaUrl: String? = null,
    val mediaPrompt: String? = null,
    val codeLanguage: String? = null,
    val videoMetadata: VideoMetadata? = null,
    val isStreaming: Boolean = false,
    val isError: Boolean = false,
    val localDrawableRes: Int? = null
)
