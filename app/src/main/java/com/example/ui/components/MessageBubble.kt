package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.ChatMessage
import com.example.data.model.MessageSender
import com.example.data.model.MessageType
import com.example.ui.theme.GptEmerald


sealed class ContentSegment {
    data class TextSegment(val text: String) : ContentSegment()
    data class CodeSegment(val language: String, val code: String) : ContentSegment()
}

fun parseMessageSegments(content: String): List<ContentSegment> {
    val segments = mutableListOf<ContentSegment>()
    val codeBlockRegex = Regex("```([a-zA-Z0-9_-]*)\\s*\\n?([\\s\\S]*?)```")

    var lastIndex = 0
    val matches = codeBlockRegex.findAll(content)

    for (match in matches) {
        if (match.range.first > lastIndex) {
            val textBefore = content.substring(lastIndex, match.range.first)
            if (textBefore.isNotBlank()) {
                segments.add(ContentSegment.TextSegment(textBefore.trim()))
            }
        }
        val language = match.groupValues[1].ifBlank { "code" }
        val code = match.groupValues[2]
        segments.add(ContentSegment.CodeSegment(language, code))
        lastIndex = match.range.last + 1
    }

    if (lastIndex < content.length) {
        val remaining = content.substring(lastIndex)
        if (remaining.isNotBlank()) {
            segments.add(ContentSegment.TextSegment(remaining.trim()))
        }
    }

    if (segments.isEmpty() && content.isNotBlank()) {
        segments.add(ContentSegment.TextSegment(content))
    }

    return segments
}

@Composable
fun MessageBubble(
    message: ChatMessage,
    onMediaClick: (ChatMessage) -> Unit,
    modifier: Modifier = Modifier,
    isSpeaking: Boolean = false,
    onSpeakClick: () -> Unit = {},
    onRegenerateClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    onEditClick: (String) -> Unit = {}
) {
    val isUser = message.sender == MessageSender.USER
    val context = LocalContext.current
    var showUserOptions by remember { mutableStateOf(false) }
    var feedbackSelected by remember { mutableStateOf<Int?>(null) } // 1 for up, -1 for down

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .testTag(if (isUser) "user_message_row" else "ai_message_row"),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        if (!isUser) {
            // ChatGPT Avatar Emblem
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .testTag("ai_avatar"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_chatgpt_symbol),
                    contentDescription = "ChatGPT",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
        }

        Column(
            modifier = Modifier.weight(1f, fill = false),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            if (isUser) {
                // User Message Pill (ChatGPT rounded capsule)
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .clickable { showUserOptions = !showUserOptions }
                        .testTag("user_message_bubble")
                ) {
                    Text(
                        text = message.content,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    )
                }

                // Optional User Message actions (Copy / Edit)
                if (showUserOptions) {
                    Row(
                        modifier = Modifier.padding(top = 4.dp, end = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("User Prompt", message.content)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Copied prompt", Toast.LENGTH_SHORT).show()
                                showUserOptions = false
                            },
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy prompt",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        IconButton(
                            onClick = {
                                onEditClick(message.content)
                                showUserOptions = false
                            },
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit prompt",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            } else {
                // AI Response Content
                when (message.type) {
                    MessageType.IMAGE -> {
                        GeneratedImageView(
                            message = message,
                            onExpandClick = { onMediaClick(message) }
                        )
                    }

                    MessageType.VIDEO -> {
                        GeneratedVideoView(
                            message = message,
                            onExpandClick = { onMediaClick(message) }
                        )
                    }

                    else -> {
                        val segments = remember(message.content) { parseMessageSegments(message.content) }
                        Column(modifier = Modifier.fillMaxWidth()) {
                            segments.forEach { segment ->
                                when (segment) {
                                    is ContentSegment.TextSegment -> {
                                        MarkdownText(
                                            text = segment.text,
                                            modifier = Modifier.padding(vertical = 4.dp)
                                        )
                                    }

                                    is ContentSegment.CodeSegment -> {
                                        CodeBlockView(
                                            code = segment.code,
                                            language = segment.language,
                                            modifier = Modifier.padding(vertical = 6.dp)
                                        )
                                    }
                                }
                            }

                            // Blinking Cursor if streaming
                            if (message.isStreaming) {
                                val infiniteTransition = rememberInfiniteTransition(label = "cursor")
                                val alpha by infiniteTransition.animateFloat(
                                    initialValue = 1f,
                                    targetValue = 0f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(500, easing = LinearEasing),
                                        repeatMode = RepeatMode.Reverse
                                    ),
                                    label = "cursorAlpha"
                                )
                                Box(
                                    modifier = Modifier
                                        .padding(top = 4.dp)
                                        .size(width = 8.dp, height = 16.dp)
                                        .alpha(alpha)
                                        .background(GptEmerald)
                                )
                            }
                        }
                    }
                }

                // AI Action Row (Authentic ChatGPT action buttons: Speak, Copy, Regenerate, Share, Thumbs, Delete)
                if (!message.isStreaming && message.content.isNotBlank()) {
                    Row(
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        // 1. Read Aloud / Speak
                        IconButton(
                            onClick = onSpeakClick,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (isSpeaking) Icons.Default.Stop else Icons.Default.VolumeUp,
                                contentDescription = if (isSpeaking) "Stop reading" else "Read aloud",
                                tint = if (isSpeaking) GptEmerald else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // 2. Copy Message
                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("ChatGPT Response", message.content)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy message",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                                modifier = Modifier.size(15.dp)
                            )
                        }

                        // 3. Regenerate Response
                        IconButton(
                            onClick = onRegenerateClick,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Regenerate response",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // 4. Share Message
                        IconButton(
                            onClick = {
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, message.content)
                                    type = "text/plain"
                                }
                                val shareIntent = Intent.createChooser(sendIntent, "Share via")
                                context.startActivity(shareIntent)
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                                modifier = Modifier.size(15.dp)
                            )
                        }

                        // 5. Thumbs Up
                        IconButton(
                            onClick = {
                                feedbackSelected = if (feedbackSelected == 1) null else 1
                                Toast.makeText(context, "Feedback received!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ThumbUp,
                                contentDescription = "Good response",
                                tint = if (feedbackSelected == 1) GptEmerald else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                                modifier = Modifier.size(15.dp)
                            )
                        }

                        // 6. Thumbs Down
                        IconButton(
                            onClick = {
                                feedbackSelected = if (feedbackSelected == -1) null else -1
                                Toast.makeText(context, "Feedback received!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ThumbDown,
                                contentDescription = "Bad response",
                                tint = if (feedbackSelected == -1) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                                modifier = Modifier.size(15.dp)
                            )
                        }

                        // 7. Delete Message
                        IconButton(
                            onClick = onDeleteClick,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
