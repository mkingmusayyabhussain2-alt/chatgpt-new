package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.SubcomposeAsyncImage
import com.example.R
import com.example.data.model.ChatMessage
import com.example.data.model.MessageType
import java.io.File

@Composable
fun MediaViewerDialog(
    message: ChatMessage,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.95f))
                .testTag("media_viewer_dialog")
        ) {
            // Close Button
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(20.dp)
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f))
                    .clickable { onDismiss() }
                    .testTag("close_media_viewer"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Media Content
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (message.type == MessageType.IMAGE) {
                    if (message.mediaUrl != null && File(message.mediaUrl).exists()) {
                        SubcomposeAsyncImage(
                            model = File(message.mediaUrl),
                            contentDescription = message.mediaPrompt,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f, fill = false)
                        )
                    } else {
                        val drawableRes = message.localDrawableRes ?: R.drawable.img_sample_ai_art
                        Image(
                            painter = painterResource(id = drawableRes),
                            contentDescription = message.mediaPrompt,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f, fill = false)
                        )
                    }
                } else if (message.type == MessageType.VIDEO) {
                    val drawableRes = message.videoMetadata?.sampleDrawableRes ?: message.localDrawableRes ?: R.drawable.img_video_preview
                    Image(
                        painter = painterResource(id = drawableRes),
                        contentDescription = message.videoMetadata?.prompt,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = message.mediaPrompt ?: message.content,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}
