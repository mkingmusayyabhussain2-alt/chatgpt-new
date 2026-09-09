package com.example.data.model

enum class AiMode(
    val title: String,
    val subtitle: String,
    val iconName: String,
    val defaultPromptPlaceholder: String
) {
    OMNI(
        title = "Omni (Auto)",
        subtitle = "Chat, code, generate images & videos",
        iconName = "auto_awesome",
        defaultPromptPlaceholder = "Ask anything, write code, create image or video..."
    ),
    CHAT(
        title = "Real-time Chat",
        subtitle = "Fast conversational AI reasoning",
        iconName = "chat",
        defaultPromptPlaceholder = "Message OmniAI..."
    ),
    CODING(
        title = "Coding Studio",
        subtitle = "Full-stack code, debug & explain",
        iconName = "code",
        defaultPromptPlaceholder = "Describe code to write or paste snippet to debug..."
    ),
    IMAGE_GEN(
        title = "Image Gen",
        subtitle = "Ultra-detailed visual art generation",
        iconName = "image",
        defaultPromptPlaceholder = "Describe the image you want to generate in detail..."
    ),
    VIDEO_GEN(
        title = "Video Gen",
        subtitle = "Cinematic video generation & concepts",
        iconName = "videocam",
        defaultPromptPlaceholder = "Describe cinematic scene, camera motion & mood..."
    )
}
