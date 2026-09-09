package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AiMode
import com.example.ui.theme.GptEmerald

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatTopBar(
    currentMode: AiMode,
    isApiKeyConfigured: Boolean,
    onMenuClick: () -> Unit,
    onNewChatClick: () -> Unit,
    onApiKeyClick: () -> Unit,
    modifier: Modifier = Modifier,
    onModeSelected: (AiMode) -> Unit = {}
) {
    var showModelMenu by remember { mutableStateOf(false) }

    val currentModelLabel = when (currentMode) {
        AiMode.OMNI -> "ChatGPT 4o"
        AiMode.CHAT -> "ChatGPT"
        AiMode.CODING -> "ChatGPT Pro"
        AiMode.IMAGE_GEN -> "ChatGPT Vision"
        AiMode.VIDEO_GEN -> "Veo Cinema"
    }

    TopAppBar(
        title = {
            Box {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { showModelMenu = true }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .testTag("model_selector_badge")
                ) {
                    Text(
                        text = currentModelLabel,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Select Model",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }

                DropdownMenu(
                    expanded = showModelMenu,
                    onDismissRequest = { showModelMenu = false },
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surface)
                        .width(260.dp)
                ) {
                    // GPT 4o
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text("ChatGPT 4o", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Great for most everyday tasks", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        },
                        leadingIcon = {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = GptEmerald)
                        },
                        trailingIcon = {
                            if (currentMode == AiMode.OMNI) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = GptEmerald)
                            }
                        },
                        onClick = {
                            showModelMenu = false
                            onModeSelected(AiMode.OMNI)
                        }
                    )

                    // GPT Pro / Coding
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text("ChatGPT Pro (Coding)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Deep reasoning & programming", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Code, contentDescription = null, tint = Color(0xFF64B5F6))
                        },
                        trailingIcon = {
                            if (currentMode == AiMode.CODING) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = GptEmerald)
                            }
                        },
                        onClick = {
                            showModelMenu = false
                            onModeSelected(AiMode.CODING)
                        }
                    )

                    // Vision / Image
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text("ChatGPT Vision (DALL·E)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Creative art & visual synthesis", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Image, contentDescription = null, tint = Color(0xFFFFB74D))
                        },
                        trailingIcon = {
                            if (currentMode == AiMode.IMAGE_GEN) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = GptEmerald)
                            }
                        },
                        onClick = {
                            showModelMenu = false
                            onModeSelected(AiMode.IMAGE_GEN)
                        }
                    )

                    // Veo Video
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text("Veo Cinema (Video)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Cinematic motion generation", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Videocam, contentDescription = null, tint = Color(0xFF00E5FF))
                        },
                        trailingIcon = {
                            if (currentMode == AiMode.VIDEO_GEN) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = GptEmerald)
                            }
                        },
                        onClick = {
                            showModelMenu = false
                            onModeSelected(AiMode.VIDEO_GEN)
                        }
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(
                onClick = onMenuClick,
                modifier = Modifier.testTag("menu_drawer_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Open Sidebar Menu",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        actions = {
            // API key status indicator / setup dialog
            IconButton(
                onClick = onApiKeyClick,
                modifier = Modifier.testTag("api_key_button")
            ) {
                Box {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = "API Key Info",
                        tint = if (isApiKeyConfigured) GptEmerald else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }

            // New chat compose icon (pencil/create like ChatGPT)
            IconButton(
                onClick = onNewChatClick,
                modifier = Modifier.testTag("new_chat_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Create,
                    contentDescription = "New Chat",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = modifier
    )
}
