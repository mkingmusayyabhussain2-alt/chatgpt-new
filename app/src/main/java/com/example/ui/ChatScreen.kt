package com.example.ui

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.ApiKeyInfoDialog
import com.example.ui.components.ChatDrawerContent
import com.example.ui.components.ChatInputBar
import com.example.ui.components.ChatTopBar
import com.example.ui.components.EmptyChatSuggestions
import com.example.ui.components.MediaViewerDialog
import com.example.ui.components.MessageBubble
import com.example.ui.components.ModeSelectorRow
import com.example.ui.util.TtsManager
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(
    viewModel: ChatViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val context = LocalContext.current

    // Text to Speech Manager
    val ttsManager = remember { TtsManager(context) }
    val speakingMessageId by ttsManager.speakingMessageId.collectAsState()

    DisposableEffect(Unit) {
        onDispose {
            ttsManager.shutdown()
        }
    }

    // Voice Input (Speech Recognizer Launcher)
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                viewModel.updateInputText(spokenText)
            }
        }
    }

    // Auto-scroll to bottom when new messages arrive or when streaming updates
    val totalMessageCount = uiState.messages.size + (if (uiState.streamingMessage != null) 1 else 0)
    LaunchedEffect(totalMessageCount, uiState.streamingMessage?.content?.length) {
        if (totalMessageCount > 0) {
            listState.animateScrollToItem(totalMessageCount - 1)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ChatDrawerContent(
                sessions = uiState.sessions,
                currentSessionId = uiState.currentSession?.id,
                onSessionClick = { session ->
                    viewModel.selectSession(session)
                    coroutineScope.launch { drawerState.close() }
                },
                onNewChatClick = { mode ->
                    viewModel.createNewSession(mode)
                    coroutineScope.launch { drawerState.close() }
                },
                onDeleteSession = { sessionId ->
                    viewModel.deleteSession(sessionId)
                },
                onClearAllSessions = {
                    viewModel.clearAllSessions()
                    coroutineScope.launch { drawerState.close() }
                }
            )
        },
        modifier = modifier.fillMaxSize()
    ) {
        Scaffold(
            topBar = {
                ChatTopBar(
                    currentMode = uiState.currentMode,
                    isApiKeyConfigured = uiState.isApiKeyPresent,
                    onMenuClick = {
                        coroutineScope.launch {
                            if (drawerState.isClosed) drawerState.open() else drawerState.close()
                        }
                    },
                    onNewChatClick = {
                        viewModel.createNewSession()
                    },
                    onApiKeyClick = {
                        viewModel.toggleApiKeyDialog(true)
                    },
                    onModeSelected = { mode ->
                        viewModel.setMode(mode)
                    }
                )
            },
            bottomBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .imePadding()
                ) {
                    ModeSelectorRow(
                        selectedMode = uiState.currentMode,
                        onModeSelected = { viewModel.setMode(it) }
                    )
                    ChatInputBar(
                        text = uiState.inputText,
                        onTextChange = { viewModel.updateInputText(it) },
                        onSendClick = { viewModel.sendMessage() },
                        onStopClick = { viewModel.stopGenerating() },
                        isGenerating = uiState.isGenerating,
                        currentMode = uiState.currentMode,
                        onQuickPromptInsert = { prompt ->
                            viewModel.sendMessage(prompt)
                        },
                        onVoiceClick = {
                            try {
                                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to ChatGPT...")
                                }
                                speechLauncher.launch(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Voice input not available on this device", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (uiState.messages.isEmpty() && uiState.streamingMessage == null) {
                    EmptyChatSuggestions(
                        onSuggestionClick = { prompt ->
                            viewModel.sendMessage(prompt)
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 8.dp)
                            .testTag("chat_messages_list")
                    ) {
                        items(uiState.messages, key = { it.id }) { message ->
                            MessageBubble(
                                message = message,
                                onMediaClick = { viewModel.openMediaViewer(it) },
                                isSpeaking = (speakingMessageId == message.id),
                                onSpeakClick = {
                                    ttsManager.toggleSpeak(message.id, message.content)
                                },
                                onRegenerateClick = {
                                    viewModel.regenerateMessage(message.id)
                                },
                                onDeleteClick = {
                                    viewModel.deleteMessage(message.id)
                                },
                                onEditClick = { prompt ->
                                    viewModel.updateInputText(prompt)
                                }
                            )
                        }

                        // Active streaming message if any
                        val streaming = uiState.streamingMessage
                        if (streaming != null) {
                            item(key = "streaming_current") {
                                MessageBubble(
                                    message = streaming,
                                    onMediaClick = { viewModel.openMediaViewer(it) },
                                    isSpeaking = false,
                                    onSpeakClick = {},
                                    onRegenerateClick = {},
                                    onDeleteClick = {},
                                    onEditClick = {}
                                )
                            }
                        }
                    }
                }

                // Error Banner if present
                uiState.errorBanner?.let { errorText ->
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter)
                            .padding(12.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = errorText,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { viewModel.dismissErrorBanner() },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Dismiss",
                                    tint = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Full screen media viewer dialog
    uiState.activeMediaViewer?.let { mediaMsg ->
        MediaViewerDialog(
            message = mediaMsg,
            onDismiss = { viewModel.openMediaViewer(null) }
        )
    }

    // API Key status dialog
    if (uiState.showApiKeyDialog) {
        ApiKeyInfoDialog(
            isConfigured = uiState.isApiKeyPresent,
            onDismiss = { viewModel.toggleApiKeyDialog(false) }
        )
    }
}

