package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val StudioDarkColorScheme = darkColorScheme(
    primary = StudioCyan,
    onPrimary = Color.Black,
    primaryContainer = StudioCard,
    onPrimaryContainer = StudioCyan,
    secondary = StudioViolet,
    onSecondary = Color.White,
    secondaryContainer = StudioSurfaceVariant,
    onSecondaryContainer = StudioViolet,
    tertiary = StudioAmber,
    onTertiary = Color.Black,
    background = StudioBlack,
    onBackground = TextPrimary,
    surface = StudioCard,
    onSurface = TextPrimary,
    surfaceVariant = StudioSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = StudioCardBorder
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = StudioDarkColorScheme,
        typography = Typography,
        content = content
    )
}
