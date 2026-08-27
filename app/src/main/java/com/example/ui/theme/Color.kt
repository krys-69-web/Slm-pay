package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Elegant Dark Signature Accent
val AppleCrimson = Color(0xFFFF2D55)
val ApplePink = Color(0xFFFF375F)
val ApplePurple = Color(0xFFAF52DE)
val AppleIndigo = Color(0xFF5856D6)
val AppleBlue = Color(0xFF007AFF)
val AppleTeal = Color(0xFF30B0C7)
val AppleAmber = Color(0xFFFF9500)

// Elegant Dark Canvas & Glass Palette (#050505 base)
val DarkCanvas = Color(0xFF050505)
val DarkSurface = Color(0xFF0D0D11)
val DarkSurfaceVariant = Color(0xFF14141B)
val DarkGlassCard = Color(0x14FFFFFF)       // ~8% white frosted glass
val DarkGlassElevated = Color(0x22FFFFFF)   // ~13% white frosted glass
val GlassBorder = Color(0x1EFFFFFF)          // ~12% white refined border
val GlassBorderActive = Color(0x99FF2D55)
val GlassHighlight = Color(0x26FFFFFF)

// Text Colors (High Contrast on Elegant Dark)
val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0x99FFFFFF)        // 60% white
val TextTertiary = Color(0x66FFFFFF)         // 40% white
val TextSubtle = Color(0x40FFFFFF)           // 25% white

// Gradients
val GlassRedGradient = Brush.linearGradient(
    colors = listOf(Color(0xFFFF2D55), Color(0xFFFF375F), Color(0xFFAF52DE))
)

val GlassAmbientGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF150409), Color(0xFF0A050D), Color(0xFF050505))
)

val NeonMeshGradients = listOf(
    listOf(Color(0xFFFF2D55), Color(0xFFAF52DE), Color(0xFF007AFF)),
    listOf(Color(0xFF5856D6), Color(0xFFFF2D55), Color(0xFFFF9500)),
    listOf(Color(0xFF00C7BE), Color(0xFF30B0C7), Color(0xFFAF52DE)),
    listOf(Color(0xFFFF375F), Color(0xFFFF9500), Color(0xFFFF2D55))
)

