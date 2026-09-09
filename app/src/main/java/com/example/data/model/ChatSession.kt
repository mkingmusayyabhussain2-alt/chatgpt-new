package com.example.data.model

data class ChatSession(
    val id: String,
    val title: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val mode: AiMode = AiMode.OMNI,
    val previewSnippet: String = "Start a conversation"
)
