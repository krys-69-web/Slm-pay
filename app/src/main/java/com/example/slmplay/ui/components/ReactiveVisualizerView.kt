package com.example.slmplay.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.slmplay.data.model.VisualizerType
import com.example.ui.theme.AppleCrimson
import com.example.ui.theme.AppleIndigo
import com.example.ui.theme.ApplePurple
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ReactiveVisualizerView(
    visualizerType: VisualizerType,
    amplitudes: FloatArray,
    accentColor: Color = AppleCrimson,
    isPlaying: Boolean = true,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "VisualizerAnimation")

    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(if (isPlaying) 3500 else 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "VizPhase"
    )

    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isPlaying) 800 else 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "VizPulse"
    )

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val centerX = width / 2f
            val centerY = height / 2f

            when (visualizerType) {
                VisualizerType.BARS -> {
                    drawBarsVisualizer(amplitudes, accentColor, isPlaying, width, height)
                }
                VisualizerType.PULSING_CIRCLE -> {
                    drawCircleVisualizer(amplitudes, accentColor, pulse, phase, centerX, centerY)
                }
                VisualizerType.PARTICLES -> {
                    drawParticlesVisualizer(amplitudes, accentColor, phase, width, height)
                }
                VisualizerType.WAVES -> {
                    drawFluidWavesVisualizer(amplitudes, accentColor, phase, width, height)
                }
                VisualizerType.ABSTRACT_SHAPES -> {
                    drawAbstractShapesVisualizer(amplitudes, accentColor, phase, pulse, centerX, centerY)
                }
                VisualizerType.HOPE_3D -> {
                    drawHopeImmersion3D(amplitudes, accentColor, phase, pulse, width, height)
                }
            }
        }
    }
}

private fun DrawScope.drawBarsVisualizer(
    amplitudes: FloatArray,
    accentColor: Color,
    isPlaying: Boolean,
    width: Float,
    height: Float
) {
    val barCount = 24
    val gap = 6f
    val totalGap = gap * (barCount - 1)
    val barWidth = (width - 48f - totalGap) / barCount
    val startX = 24f
    val baseY = height * 0.85f

    for (i in 0 until barCount) {
        val ampIndex = (i % amplitudes.size)
        val rawAmp = if (isPlaying) amplitudes[ampIndex] else 0.15f
        val barHeight = (rawAmp * height * 0.65f).coerceIn(8f, height * 0.75f)

        val x = startX + i * (barWidth + gap)
        val y = baseY - barHeight

        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    accentColor,
                    Color(0xFFAF52DE),
                    Color(0xFF007AFF)
                ),
                startY = y,
                endY = baseY
            ),
            topLeft = Offset(x, y),
            size = Size(barWidth, barHeight),
            cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
        )

        // Top glow cap
        drawCircle(
            color = Color.White.copy(alpha = 0.8f),
            radius = (barWidth / 2.5f),
            center = Offset(x + barWidth / 2f, y + barWidth / 2.5f)
        )
    }
}

private fun DrawScope.drawCircleVisualizer(
    amplitudes: FloatArray,
    accentColor: Color,
    pulse: Float,
    phase: Float,
    centerX: Float,
    centerY: Float
) {
    val baseRadius = (size.minDimension / 4f) * pulse
    val numSpokes = 32

    // Outer glow aura
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(accentColor.copy(alpha = 0.35f), Color.Transparent),
            center = Offset(centerX, centerY),
            radius = baseRadius * 1.8f
        ),
        radius = baseRadius * 1.8f,
        center = Offset(centerX, centerY)
    )

    // Inner glowing core
    drawCircle(
        color = accentColor.copy(alpha = 0.15f),
        radius = baseRadius,
        center = Offset(centerX, centerY)
    )

    drawCircle(
        color = accentColor,
        radius = baseRadius,
        center = Offset(centerX, centerY),
        style = Stroke(width = 3f)
    )

    for (i in 0 until numSpokes) {
        val angle = (i.toFloat() / numSpokes) * 2 * PI + phase
        val amp = amplitudes[i % amplitudes.size]
        val spokeLen = 20f + amp * (baseRadius * 0.9f)

        val startX = centerX + cos(angle).toFloat() * baseRadius
        val startY = centerY + sin(angle).toFloat() * baseRadius
        val endX = centerX + cos(angle).toFloat() * (baseRadius + spokeLen)
        val endY = centerY + sin(angle).toFloat() * (baseRadius + spokeLen)

        drawLine(
            brush = Brush.linearGradient(
                colors = listOf(accentColor, Color.White),
                start = Offset(startX, startY),
                end = Offset(endX, endY)
            ),
            start = Offset(startX, startY),
            end = Offset(endX, endY),
            strokeWidth = 4f,
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.drawParticlesVisualizer(
    amplitudes: FloatArray,
    accentColor: Color,
    phase: Float,
    width: Float,
    height: Float
) {
    val particleCount = 48
    for (i in 0 until particleCount) {
        val amp = amplitudes[i % amplitudes.size]
        val speedFactor = (i % 5 + 1) * 0.2f
        val normY = ((phase * speedFactor + i.toFloat() / particleCount) % 1f)
        val x = (sin(i * 1.7f + phase) * 0.45f + 0.5f) * width
        val y = (1f - normY) * height

        val radius = (4f + amp * 16f * (i % 3 + 1))

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    if (i % 2 == 0) accentColor else Color(0xFFAF52DE),
                    Color.Transparent
                ),
                center = Offset(x, y),
                radius = radius
            ),
            radius = radius,
            center = Offset(x, y)
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.9f),
            radius = (radius * 0.25f).coerceAtLeast(1.5f),
            center = Offset(x, y)
        )
    }
}

private fun DrawScope.drawFluidWavesVisualizer(
    amplitudes: FloatArray,
    accentColor: Color,
    phase: Float,
    width: Float,
    height: Float
) {
    val layers = 3
    for (l in 0 until layers) {
        val path = Path()
        val baseY = height * (0.6f + l * 0.12f)
        val amp = amplitudes[l % amplitudes.size] * (40f + l * 15f)
        val layerPhase = phase + l * 1.2f

        path.moveTo(0f, height)
        path.lineTo(0f, baseY)

        var x = 0f
        while (x <= width) {
            val normX = x / width
            val y = baseY + sin(normX * 4 * PI + layerPhase).toFloat() * amp
            path.lineTo(x, y)
            x += 16f
        }

        path.lineTo(width, height)
        path.close()

        val layerColor = when (l) {
            0 -> accentColor.copy(alpha = 0.35f)
            1 -> Color(0xFFAF52DE).copy(alpha = 0.25f)
            else -> Color(0xFF007AFF).copy(alpha = 0.20f)
        }

        drawPath(path = path, color = layerColor)
    }
}

private fun DrawScope.drawAbstractShapesVisualizer(
    amplitudes: FloatArray,
    accentColor: Color,
    phase: Float,
    pulse: Float,
    centerX: Float,
    centerY: Float
) {
    val points = 6
    val baseRadius = (size.minDimension / 4.5f) * pulse

    for (ring in 1..3) {
        val path = Path()
        val ringPhase = phase * (if (ring % 2 == 0) -1 else 1) + ring * 0.8f
        val ringRadius = baseRadius * (ring * 0.55f)

        for (i in 0 until points) {
            val angle = (i.toFloat() / points) * 2 * PI + ringPhase
            val amp = amplitudes[(i + ring) % amplitudes.size]
            val r = ringRadius * (1f + amp * 0.4f)
            val px = centerX + cos(angle).toFloat() * r
            val py = centerY + sin(angle).toFloat() * r

            if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
        }
        path.close()

        drawPath(
            path = path,
            color = if (ring == 1) accentColor else if (ring == 2) Color(0xFFAF52DE) else Color(0xFF00C7BE),
            style = Stroke(width = 3.5f)
        )
    }
}

private fun DrawScope.drawHopeImmersion3D(
    amplitudes: FloatArray,
    accentColor: Color,
    phase: Float,
    pulse: Float,
    width: Float,
    height: Float
) {
    val horizonY = height * 0.45f

    // Starfield in upper half
    for (i in 0 until 40) {
        val sx = (sin(i * 2.3f + 1f) * 0.5f + 0.5f) * width
        val sy = (sin(i * 4.1f + 2f) * 0.5f + 0.5f) * horizonY
        val starPulse = (sin(phase * 2 + i) * 0.5f + 0.5f)
        drawCircle(
            color = Color.White.copy(alpha = 0.4f + starPulse * 0.6f),
            radius = (1.5f + starPulse * 2f),
            center = Offset(sx, sy)
        )
    }

    // Glowing Cyber Horizon Line
    drawLine(
        brush = Brush.horizontalGradient(
            colors = listOf(Color.Transparent, accentColor, Color.White, accentColor, Color.Transparent)
        ),
        start = Offset(0f, horizonY),
        end = Offset(width, horizonY),
        strokeWidth = 4f
    )

    // Perspective 3D Grid Lines
    val vanishingX = width / 2f
    val gridLines = 14
    for (i in 0..gridLines) {
        val bottomX = (i.toFloat() / gridLines) * width
        val amp = amplitudes[i % amplitudes.size]

        drawLine(
            color = accentColor.copy(alpha = 0.35f + amp * 0.4f),
            start = Offset(vanishingX, horizonY),
            end = Offset(bottomX, height),
            strokeWidth = 2f
        )
    }

    // Horizontal terrain wave bands
    val depthBands = 8
    for (d in 1..depthBands) {
        val norm = (d.toFloat() / depthBands)
        val y = horizonY + (height - horizonY) * (norm * norm)
        val amp = amplitudes[d % amplitudes.size] * (15f * norm)
        val path = Path()
        path.moveTo(0f, y)

        var gx = 0f
        while (gx <= width) {
            val wave = sin((gx / width) * 4 * PI + phase * 2).toFloat() * amp
            path.lineTo(gx, y + wave)
            gx += 20f
        }

        drawPath(
            path = path,
            color = Color(0xFFAF52DE).copy(alpha = 0.25f + (norm * 0.4f)),
            style = Stroke(width = (2f + norm * 2f))
        )
    }
}
