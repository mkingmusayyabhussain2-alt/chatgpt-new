package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.local.AppDatabase
import com.example.data.model.AiMode
import com.example.data.model.ChatMessage
import com.example.data.model.ChatSession
import com.example.data.model.MessageSender
import com.example.data.model.MessageType
import com.example.data.model.VideoMetadata
import com.example.data.remote.GeminiClient
import com.example.data.repository.ChatRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

data class ChatUiState(
    val currentSession: ChatSession? = null,
    val sessions: List<ChatSession> = emptyList(),
    val messages: List<ChatMessage> = emptyList(),
    val isGenerating: Boolean = false,
    val streamingMessage: ChatMessage? = null,
    val currentMode: AiMode = AiMode.OMNI,
    val inputText: String = "",
    val activeMediaViewer: ChatMessage? = null,
    val showApiKeyDialog: Boolean = false,
    val isApiKeyPresent: Boolean = false,
    val errorBanner: String? = null
)

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ChatRepository
    private var streamingJob: Job? = null

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        val db = AppDatabase.getInstance(application)
        val geminiClient = GeminiClient(application)
        repository = ChatRepository(db.chatDao(), geminiClient)

        val apiKey = BuildConfig.GEMINI_API_KEY
        val hasKey = apiKey.isNotBlank() && !apiKey.contains("MY_GEMINI_API_KEY")
        _uiState.value = _uiState.value.copy(isApiKeyPresent = hasKey)

        observeSessions()
    }

    private fun observeSessions() {
        viewModelScope.launch {
            repository.getAllSessions().collect { sessionList ->
                val current = _uiState.value.currentSession
                if (sessionList.isEmpty()) {
                    // Create default first session
                    val first = repository.createNewSession(AiMode.OMNI, "Welcome to OmniAI")
                    _uiState.value = _uiState.value.copy(
                        sessions = listOf(first),
                        currentSession = first
                    )
                    observeMessages(first.id)
                } else {
                    val active = if (current != null && sessionList.any { it.id == current.id }) {
                        sessionList.first { it.id == current.id }
                    } else {
                        sessionList.first()
                    }
                    _uiState.value = _uiState.value.copy(
                        sessions = sessionList,
                        currentSession = active
                    )
                    if (current?.id != active.id) {
                        observeMessages(active.id)
                    }
                }
            }
        }
    }

    private var messagesJob: Job? = null
    private fun observeMessages(sessionId: String) {
        messagesJob?.cancel()
        messagesJob = viewModelScope.launch {
            repository.getMessagesForSession(sessionId).collect { msgList ->
                _uiState.value = _uiState.value.copy(messages = msgList)
            }
        }
    }

    fun selectSession(session: ChatSession) {
        if (_uiState.value.currentSession?.id == session.id) return
        stopGenerating()
        _uiState.value = _uiState.value.copy(
            currentSession = session,
            currentMode = session.mode
        )
        observeMessages(session.id)
    }

    fun createNewSession(mode: AiMode = _uiState.value.currentMode) {
        viewModelScope.launch {
            stopGenerating()
            val newSession = repository.createNewSession(
                mode = mode,
                title = when (mode) {
                    AiMode.CODING -> "Code Workspace"
                    AiMode.IMAGE_GEN -> "Image Creation"
                    AiMode.VIDEO_GEN -> "Cinematic Video"
                    AiMode.CHAT -> "Quick Chat"
                    else -> "New Chat"
                }
            )
            _uiState.value = _uiState.value.copy(
                currentSession = newSession,
                currentMode = mode
            )
            observeMessages(newSession.id)
        }
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            repository.deleteSession(sessionId)
        }
    }

    fun clearAllSessions() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }

    fun setMode(mode: AiMode) {
        _uiState.value = _uiState.value.copy(currentMode = mode)
    }

    fun updateInputText(text: String) {
        _uiState.value = _uiState.value.copy(inputText = text)
    }

    fun openMediaViewer(message: ChatMessage?) {
        _uiState.value = _uiState.value.copy(activeMediaViewer = message)
    }

    fun toggleApiKeyDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showApiKeyDialog = show)
    }

    fun dismissErrorBanner() {
        _uiState.value = _uiState.value.copy(errorBanner = null)
    }

    fun switchMode(newMode: AiMode) {
        _uiState.value = _uiState.value.copy(currentMode = newMode)
    }

    fun deleteMessage(messageId: String) {
        viewModelScope.launch {
            repository.deleteMessage(messageId)
        }
    }

    fun regenerateMessage(aiMessageId: String) {
        val messages = _uiState.value.messages
        val aiIndex = messages.indexOfFirst { it.id == aiMessageId }
        if (aiIndex == -1) return

        val userMessage = messages.subList(0, aiIndex).lastOrNull { it.sender == MessageSender.USER } ?: return

        viewModelScope.launch {
            repository.deleteMessage(aiMessageId)
            sendMessage(userMessage.content)
        }
    }

    fun stopGenerating() {
        streamingJob?.cancel()
        val streamingMsg = _uiState.value.streamingMessage
        if (streamingMsg != null) {
            viewModelScope.launch {
                repository.saveAiMessage(streamingMsg.copy(isStreaming = false))
                _uiState.value = _uiState.value.copy(
                    isGenerating = false,
                    streamingMessage = null
                )
            }
        } else {
            _uiState.value = _uiState.value.copy(isGenerating = false)
        }
    }

    fun sendMessage(promptOverride: String? = null) {
        val text = (promptOverride ?: _uiState.value.inputText).trim()
        if (text.isBlank() || _uiState.value.isGenerating) return

        val session = _uiState.value.currentSession ?: return
        val mode = _uiState.value.currentMode

        // Clear input text
        _uiState.value = _uiState.value.copy(inputText = "", isGenerating = true)

        streamingJob = viewModelScope.launch {
            try {
                // 1. Save User Message
                val userMsg = repository.saveUserMessage(session.id, text)

                // Update session title if first user message
                if (_uiState.value.messages.isEmpty()) {
                    val preview = if (text.length > 30) text.take(27) + "..." else text
                    repository.updateSessionTitle(session.id, preview, preview)
                }

                // Determine whether request is Image, Video, Code, or Chat
                val isExplicitImage = mode == AiMode.IMAGE_GEN ||
                        text.startsWith("/image", ignoreCase = true) ||
                        text.contains("generate image", ignoreCase = true) ||
                        text.contains("create an image", ignoreCase = true) ||
                        text.contains("draw a", ignoreCase = true)

                val isExplicitVideo = mode == AiMode.VIDEO_GEN ||
                        text.startsWith("/video", ignoreCase = true) ||
                        text.contains("generate video", ignoreCase = true) ||
                        text.contains("create a video", ignoreCase = true) ||
                        text.contains("make a video", ignoreCase = true)

                when {
                    isExplicitImage -> {
                        val cleanPrompt = text
                            .replace("/image", "", ignoreCase = true)
                            .trim()

                        val aiMsgId = UUID.randomUUID().toString()
                        val placeholderAi = ChatMessage(
                            id = aiMsgId,
                            sessionId = session.id,
                            sender = MessageSender.AI,
                            content = "Synthesizing image with Gemini 2.5 Flash Image...",
                            type = MessageType.IMAGE,
                            mediaPrompt = cleanPrompt,
                            isStreaming = true
                        )
                        _uiState.value = _uiState.value.copy(streamingMessage = placeholderAi)

                        val result = repository.generateImage(cleanPrompt)

                        val finalAiMsg = ChatMessage(
                            id = aiMsgId,
                            sessionId = session.id,
                            sender = MessageSender.AI,
                            content = result.description,
                            type = MessageType.IMAGE,
                            mediaUrl = result.filePath,
                            localDrawableRes = result.localDrawableRes,
                            mediaPrompt = result.prompt,
                            isStreaming = false
                        )
                        repository.saveAiMessage(finalAiMsg)
                        _uiState.value = _uiState.value.copy(
                            isGenerating = false,
                            streamingMessage = null
                        )
                    }

                    isExplicitVideo -> {
                        val cleanPrompt = text
                            .replace("/video", "", ignoreCase = true)
                            .trim()

                        val aiMsgId = UUID.randomUUID().toString()
                        val placeholderAi = ChatMessage(
                            id = aiMsgId,
                            sessionId = session.id,
                            sender = MessageSender.AI,
                            content = "Directing scene & rendering video with Veo 3.1 Fast...",
                            type = MessageType.VIDEO,
                            mediaPrompt = cleanPrompt,
                            isStreaming = true
                        )
                        _uiState.value = _uiState.value.copy(streamingMessage = placeholderAi)

                        val result = repository.generateVideo(cleanPrompt)

                        val videoMeta = VideoMetadata(
                            prompt = result.prompt,
                            resolution = result.resolution,
                            aspectRatio = result.aspectRatio,
                            durationSeconds = result.durationSeconds,
                            cameraMovement = result.cameraMovement,
                            fps = result.fps,
                            sampleDrawableRes = result.localDrawableRes
                        )

                        val finalAiMsg = ChatMessage(
                            id = aiMsgId,
                            sessionId = session.id,
                            sender = MessageSender.AI,
                            content = "🎬 **Veo 3.1 Cinematic Generation**\n\n- **Prompt**: ${result.prompt}\n- **Resolution**: ${result.resolution}\n- **Camera**: ${result.cameraMovement}\n- **Status**: ${result.status}",
                            type = MessageType.VIDEO,
                            localDrawableRes = result.localDrawableRes,
                            mediaPrompt = result.prompt,
                            videoMetadata = videoMeta,
                            isStreaming = false
                        )
                        repository.saveAiMessage(finalAiMsg)
                        _uiState.value = _uiState.value.copy(
                            isGenerating = false,
                            streamingMessage = null
                        )
                    }

                    else -> {
                        // Standard Chat or Code streaming
                        val aiMsgId = UUID.randomUUID().toString()
                        val liveAiMsg = ChatMessage(
                            id = aiMsgId,
                            sessionId = session.id,
                            sender = MessageSender.AI,
                            content = "",
                            type = if (mode == AiMode.CODING || text.contains("code", ignoreCase = true)) MessageType.CODE else MessageType.TEXT,
                            isStreaming = true
                        )
                        _uiState.value = _uiState.value.copy(streamingMessage = liveAiMsg)

                        val fullText = repository.generateAiChatStream(
                            sessionId = session.id,
                            history = _uiState.value.messages,
                            prompt = text,
                            mode = mode
                        ) { accumulatedText ->
                            _uiState.value = _uiState.value.copy(
                                streamingMessage = liveAiMsg.copy(content = accumulatedText)
                            )
                        }

                        val finalAiMsg = liveAiMsg.copy(
                            content = fullText,
                            isStreaming = false
                        )
                        repository.saveAiMessage(finalAiMsg)
                        _uiState.value = _uiState.value.copy(
                            isGenerating = false,
                            streamingMessage = null
                        )
                    }
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                // Ignore normal coroutine cancellation (e.g. user clicked stop or switched session)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isGenerating = false,
                    streamingMessage = null,
                    errorBanner = "Generation interrupted: ${e.localizedMessage}"
                )
            }
        }
    }
}

