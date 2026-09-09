package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GptEmerald

@Composable
fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier
) {
    val lines = remember(text) { text.split("\n") }

    Column(modifier = modifier.fillMaxWidth()) {
        lines.forEachIndexed { _, line ->
            val trimmed = line.trim()
            when {
                trimmed.isBlank() -> {
                    Spacer(modifier = Modifier.height(6.dp))
                }

                trimmed.startsWith("### ") -> {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = parseInlineMarkdown(trimmed.removePrefix("### ")),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }

                trimmed.startsWith("## ") -> {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = parseInlineMarkdown(trimmed.removePrefix("## ")),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                trimmed.startsWith("# ") -> {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = parseInlineMarkdown(trimmed.removePrefix("# ")),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                trimmed.startsWith("- ") || trimmed.startsWith("* ") || trimmed.startsWith("• ") -> {
                    val bulletContent = trimmed.substring(2)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 6.dp, top = 2.dp, bottom = 2.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(GptEmerald)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = parseInlineMarkdown(bulletContent),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                            lineHeight = 22.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                trimmed.matches(Regex("^\\d+\\.\\s.*")) -> {
                    val dotIdx = trimmed.indexOf('.')
                    val numberPart = trimmed.substring(0, dotIdx + 1)
                    val restPart = trimmed.substring(dotIdx + 1).trim()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 4.dp, top = 2.dp, bottom = 2.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = numberPart,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = GptEmerald,
                            modifier = Modifier.width(24.dp)
                        )
                        Text(
                            text = parseInlineMarkdown(restPart),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                            lineHeight = 22.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                trimmed.startsWith("> ") -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(22.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(GptEmerald)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = parseInlineMarkdown(trimmed.removePrefix("> ")),
                            style = MaterialTheme.typography.bodyMedium,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 20.sp
                        )
                    }
                }

                else -> {
                    Text(
                        text = parseInlineMarkdown(trimmed),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        lineHeight = 24.sp,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }
    }
}

/**
 * Parses inline formatting: **bold**, *italic*, `code`
 */
@Composable
private fun parseInlineMarkdown(raw: String): AnnotatedString {
    val codeBg = MaterialTheme.colorScheme.surfaceVariant
    val codeColor = GptEmerald

    return remember(raw, codeBg, codeColor) {
        buildAnnotatedString {
            var i = 0
            val len = raw.length

            while (i < len) {
                when {
                    // Inline Code: `code`
                    raw[i] == '`' -> {
                        val end = raw.indexOf('`', i + 1)
                        if (end != -1) {
                            val codeContent = raw.substring(i + 1, end)
                            val startIdx = length
                            append(" $codeContent ")
                            addStyle(
                                SpanStyle(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 13.sp,
                                    background = codeBg,
                                    color = codeColor
                                ),
                                startIdx,
                                length
                            )
                            i = end + 1
                        } else {
                            append(raw[i])
                            i++
                        }
                    }

                    // Bold: **text**
                    i + 1 < len && raw[i] == '*' && raw[i + 1] == '*' -> {
                        val end = raw.indexOf("**", i + 2)
                        if (end != -1) {
                            val boldContent = raw.substring(i + 2, end)
                            val startIdx = length
                            append(boldContent)
                            addStyle(
                                SpanStyle(fontWeight = FontWeight.Bold),
                                startIdx,
                                length
                            )
                            i = end + 2
                        } else {
                            append(raw[i])
                            i++
                        }
                    }

                    // Italic: *text*
                    raw[i] == '*' -> {
                        val end = raw.indexOf('*', i + 1)
                        if (end != -1) {
                            val italicContent = raw.substring(i + 1, end)
                            val startIdx = length
                            append(italicContent)
                            addStyle(
                                SpanStyle(fontStyle = FontStyle.Italic),
                                startIdx,
                                length
                            )
                            i = end + 1
                        } else {
                            append(raw[i])
                            i++
                        }
                    }

                    else -> {
                        append(raw[i])
                        i++
                    }
                }
            }
        }
    }
}
