package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = GptEmeraldLight,
    onPrimary = Color.Black,
    primaryContainer = GptEmeraldDark,
    onPrimaryContainer = Color.White,
    secondary = Color(0xFF64B5F6),
    onSecondary = Color.Black,
    tertiary = Color(0xFFCE93D8),
    onTertiary = Color.Black,
    background = GptDarkBackground,
    onBackground = Color(0xFFEDEDED),
    surface = GptDarkSurface,
    onSurface = Color(0xFFEDEDED),
    surfaceVariant = GptDarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFB0B3B8),
    outline = GptDarkBorder
)

private val LightColorScheme = lightColorScheme(
    primary = GptEmerald,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD1F2E5),
    onPrimaryContainer = Color(0xFF003822),
    secondary = Color(0xFF1976D2),
    onSecondary = Color.White,
    tertiary = Color(0xFF7B1FA2),
    onTertiary = Color.White,
    background = GptLightBackground,
    onBackground = Color(0xFF1A1A1A),
    surface = GptLightSurface,
    onSurface = Color(0xFF1A1A1A),
    surfaceVariant = GptLightSurfaceVariant,
    onSurfaceVariant = Color(0xFF4A4C50),
    outline = GptLightBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

