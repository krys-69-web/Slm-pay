package com.example.slmplay.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AppleCrimson
import com.example.ui.theme.DarkGlassCard
import com.example.ui.theme.DarkGlassElevated
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassHighlight

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(26.dp),
    backgroundColor: Color = DarkGlassCard,
    borderColor: Color = GlassBorder,
    borderWidth: Dp = 1.dp,
    elevation: Dp = 12.dp,
    highlight: Boolean = true,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val clickModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = ripple(color = AppleCrimson.copy(alpha = 0.3f)),
            onClick = onClick
        )
    } else Modifier

    Box(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = 0.7f),
                spotColor = AppleCrimson.copy(alpha = 0.15f)
            )
            .clip(shape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        backgroundColor,
                        backgroundColor.copy(alpha = (backgroundColor.alpha * 0.7f).coerceAtLeast(0.04f))
                    )
                )
            )
            .border(
                width = borderWidth,
                brush = Brush.linearGradient(
                    colors = listOf(
                        borderColor,
                        GlassHighlight,
                        borderColor.copy(alpha = (borderColor.alpha * 0.4f).coerceAtLeast(0.03f))
                    )
                ),
                shape = shape
            )
            .then(clickModifier)
            .then(
                if (highlight) {
                    Modifier.drawBehind {
                        // Subtle specular curved light sheen mimicking Apple glass
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color.White.copy(alpha = 0.08f), Color.Transparent),
                                center = Offset(size.width / 2, 0f),
                                radius = size.width * 0.75f
                            ),
                            center = Offset(size.width / 2, 0f),
                            radius = size.width * 0.75f
                        )
                    }
                } else Modifier
            )
    ) {
        content()
    }
}

@Composable
fun GlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(20.dp),
    isPrimary: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    val bgBrush = if (isPrimary) {
        Brush.linearGradient(
            colors = listOf(Color(0xFFFF2D55), Color(0xFFFF375F), Color(0xFFAF52DE))
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(DarkGlassElevated, DarkGlassCard)
        )
    }

    val borderBrush = if (isPrimary) {
        Brush.linearGradient(
            colors = listOf(Color.White.copy(alpha = 0.5f), Color(0xFFFF2D55))
        )
    } else {
        Brush.linearGradient(
            colors = listOf(GlassBorder, GlassHighlight, GlassBorder.copy(alpha = 0.05f))
        )
    }

    Box(
        modifier = modifier
            .shadow(
                elevation = if (isPrimary) 14.dp else 4.dp,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = 0.6f),
                spotColor = if (isPrimary) AppleCrimson.copy(alpha = 0.6f) else Color.Black
            )
            .clip(shape)
            .background(bgBrush)
            .border(width = 1.dp, brush = borderBrush, shape = shape)
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = Color.White.copy(alpha = 0.3f)),
                onClick = onClick
            )
    ) {
        content()
    }
}

