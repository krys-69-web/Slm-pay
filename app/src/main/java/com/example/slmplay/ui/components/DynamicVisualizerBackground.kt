package com.example.slmplay.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.example.slmplay.data.model.BackgroundMode
import com.example.ui.theme.AppleCrimson
import com.example.ui.theme.AppleIndigo
import com.example.ui.theme.ApplePurple
import com.example.ui.theme.DarkCanvas
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun DynamicVisualizerBackground(
    mode: BackgroundMode,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "BackgroundAnimation")

    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(if (isPlaying) 6000 else 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "WavePhase"
    )

    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isPlaying) 1500 else 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GlowPulse"
    )

    val orbMove by infiniteTransition.animateFloat(
        initialValue = -50f,
        targetValue = 50f,
        animationSpec = infiniteRepeatable(
            animation = tween(9000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "OrbMove"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkCanvas)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Base atmospheric glow of Elegant Dark theme (#050505 base with crimson & indigo blur auras)
            drawRadialGlow(
                center = Offset(width * 0.05f + orbMove * 0.4f, height * 0.92f),
                radius = width * 0.95f * pulse,
                color = AppleCrimson.copy(alpha = if (isPlaying) 0.22f else 0.14f)
            )
            drawRadialGlow(
                center = Offset(width * 0.95f - orbMove * 0.4f, height * 0.12f),
                radius = width * 0.85f * pulse,
                color = AppleIndigo.copy(alpha = if (isPlaying) 0.16f else 0.09f)
            )

            when (mode) {
                BackgroundMode.DYNAMIC_MESH -> {
                    // HOpE Living Wave / Fluid Liquid Mesh
                    drawRadialGlow(
                        center = Offset(width * 0.35f + orbMove, height * 0.30f),
                        radius = width * 0.80f * pulse,
                        color = ApplePurple.copy(alpha = if (isPlaying) 0.22f else 0.12f)
                    )

                    // Draw animated fluid flowing waves
                    drawFluidWave(
                        phase = phase,
                        amplitude = if (isPlaying) 35f else 15f,
                        yOffset = height * 0.72f,
                        color = AppleCrimson.copy(alpha = 0.10f)
                    )
                    drawFluidWave(
                        phase = phase + 1.5f,
                        amplitude = if (isPlaying) 25f else 12f,
                        yOffset = height * 0.78f,
                        color = ApplePurple.copy(alpha = 0.08f)
                    )
                }

                BackgroundMode.ARTWORK_AURA -> {
                    // Apple Glass Soft Diffuse Aura
                    drawRadialGlow(
                        center = Offset(width * 0.5f, height * 0.38f),
                        radius = width * 1.05f * pulse,
                        color = AppleCrimson.copy(alpha = if (isPlaying) 0.32f else 0.18f)
                    )

                    drawRadialGlow(
                        center = Offset(width * 0.2f, height * 0.65f),
                        radius = width * 0.75f,
                        color = ApplePurple.copy(alpha = 0.18f)
                    )
                }

                BackgroundMode.NEON_PULSE -> {
                    // Cyber SLM High Contrast Neon Glow
                    drawRadialGlow(
                        center = Offset(width * 0.5f, height * 0.42f),
                        radius = width * 0.80f * pulse,
                        color = Color(0xFFFF2D55).copy(alpha = if (isPlaying) 0.38f else 0.20f)
                    )
                    drawRadialGlow(
                        center = Offset(width * 0.85f, height * 0.20f),
                        radius = width * 0.65f,
                        color = Color(0xFF007AFF).copy(alpha = 0.15f)
                    )
                }

                BackgroundMode.DEEP_GLASS -> {
                    // Minimalist Dark Obsidian with subtle glass shimmer
                    drawRadialGlow(
                        center = Offset(width * 0.5f, height * 0.25f),
                        radius = width * 0.65f,
                        color = AppleCrimson.copy(alpha = 0.12f)
                    )
                }
            }

            // Top glass sheen vignette
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = 0.35f),
                        Color.Transparent,
                        Color.Black.copy(alpha = 0.80f)
                    )
                )
            )
        }
    }
}

private fun DrawScope.drawRadialGlow(center: Offset, radius: Float, color: Color) {
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(color, color.copy(alpha = 0f)),
            center = center,
            radius = radius
        ),
        center = center,
        radius = radius
    )
}

private fun DrawScope.drawFluidWave(phase: Float, amplitude: Float, yOffset: Float, color: Color) {
    val path = Path()
    val width = size.width
    val height = size.height

    path.moveTo(0f, height)
    path.lineTo(0f, yOffset)

    val step = 20f
    var x = 0f
    while (x <= width) {
        val y = yOffset + sin((x / width * 2 * PI + phase).toFloat()) * amplitude
        path.lineTo(x, y)
        x += step
    }

    path.lineTo(width, height)
    path.close()

    drawPath(path = path, color = color)
}
