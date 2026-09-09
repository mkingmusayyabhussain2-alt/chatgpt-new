package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AiMode
import com.example.ui.theme.GptEmerald

@Composable
fun ChatInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onStopClick: () -> Unit,
    isGenerating: Boolean,
    currentMode: AiMode,
    onQuickPromptInsert: (String) -> Unit,
    modifier: Modifier = Modifier,
    onVoiceClick: () -> Unit = {}
) {
    var showQuickMenu by remember { mutableStateOf(false) }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                // Attach/Tools Quick Menu Button
                Box {
                    IconButton(
                        onClick = { showQuickMenu = true },
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("quick_tools_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Quick Tools",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    DropdownMenu(
                        expanded = showQuickMenu,
                        onDismissRequest = { showQuickMenu = false },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                    ) {
                        DropdownMenuItem(
                            text = { Text("🎨 Generate Image") },
                            leadingIcon = {
                                Icon(Icons.Default.Image, contentDescription = null, tint = GptEmerald)
                            },
                            onClick = {
                                showQuickMenu = false
                                onQuickPromptInsert("/image Generate a photorealistic cyberpunk neon city")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("🎬 Generate Video") },
                            leadingIcon = {
                                Icon(Icons.Default.Videocam, contentDescription = null, tint = Color(0xFF00E5FF))
                            },
                            onClick = {
                                showQuickMenu = false
                                onQuickPromptInsert("/video Cinematic drone shot flying through mist over mountains")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("💻 Write Code") },
                            leadingIcon = {
                                Icon(Icons.Default.Code, contentDescription = null, tint = Color(0xFF64B5F6))
                            },
                            onClick = {
                                showQuickMenu = false
                                onQuickPromptInsert("Write a clean Kotlin Jetpack Compose composable for a custom audio visualizer card")
                            }
                        )
                    }
                }

                // Input Text Field
                OutlinedTextField(
                    value = text,
                    onValueChange = onTextChange,
                    placeholder = {
                        Text(
                            text = "Message ChatGPT...",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            fontSize = 15.sp,
                            maxLines = 1
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 44.dp, max = 130.dp)
                        .testTag("chat_text_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        cursorColor = GptEmerald
                    ),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Send
                    ),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (!isGenerating && text.isNotBlank()) {
                                onSendClick()
                            }
                        }
                    ),
                    maxLines = 5
                )

                // Send or Stop Generating Button (Authentic ChatGPT button)
                val hasContent = text.isNotBlank()
                val isDark = isSystemInDarkTheme()
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .padding(bottom = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isGenerating) {
                        // Stop button (Square inside circle)
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(if (isDark) Color.White else Color.Black)
                                .clickable { onStopClick() }
                                .testTag("stop_generating_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(if (isDark) Color.Black else Color.White)
                            )
                        }
                    } else if (hasContent) {
                        // Send button (ChatGPT White/Black circle with ArrowUpward)
                        val buttonColor = when {
                            isDark -> Color.White
                            else -> Color.Black
                        }
                        val iconColor = when {
                            isDark -> Color.Black
                            else -> Color.White
                        }
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(buttonColor)
                                .clickable { onSendClick() }
                                .testTag("send_message_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = "Send Message",
                                tint = iconColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    } else {
                        // Microphone Voice Dictation Button (ChatGPT Mic)
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .clickable { onVoiceClick() }
                                .testTag("voice_input_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Voice Input",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Disclaimer Footer
            Text(
                text = "ChatGPT can make mistakes. Verify critical details.",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 4.dp)
            )
        }
    }
}
