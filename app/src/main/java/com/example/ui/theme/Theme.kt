package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = AppleCrimson,
    onPrimary = Color.White,
    primaryContainer = Color(0x33FF2D55),
    onPrimaryContainer = Color(0xFFFF728E),
    secondary = ApplePurple,
    onSecondary = Color.White,
    secondaryContainer = Color(0x33AF52DE),
    onSecondaryContainer = Color(0xFFD497F8),
    tertiary = ApplePink,
    background = DarkCanvas,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = GlassBorder,
    outlineVariant = GlassHighlight
)

private val LightColorScheme = darkColorScheme(
    primary = AppleCrimson,
    onPrimary = Color.White,
    primaryContainer = Color(0x33FF2D55),
    onPrimaryContainer = Color(0xFFFF728E),
    secondary = ApplePurple,
    onSecondary = Color.White,
    secondaryContainer = Color(0x33AF52DE),
    onSecondaryContainer = Color(0xFFD497F8),
    tertiary = ApplePink,
    background = DarkCanvas,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = GlassBorder,
    outlineVariant = GlassHighlight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // SLM Play is crafted with a signature Apple Glass dark aesthetic
    val colorScheme = DarkColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
