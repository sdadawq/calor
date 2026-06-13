package com.calor.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CalorColorScheme = lightColorScheme(
    primary = CoralPrimary,
    onPrimary = Color.White,
    secondary = MintSecondary,
    onSecondary = TextPrimary,
    background = CreamBackground,
    onBackground = TextPrimary,
    surface = SurfaceWhite,
    onSurface = TextPrimary,
    surfaceVariant = ProgressTrack,
    onSurfaceVariant = TextMuted,
)

@Composable
fun CalorTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CalorColorScheme,
        content = content,
    )
}
