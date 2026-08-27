package com.example.slmplay.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.slmplay.data.db.TrackEntity
import com.example.slmplay.data.model.VisualizerType
import com.example.ui.theme.*

@Composable
fun FullscreenVisualizerDialog(
    isOpen: Boolean,
    currentTrack: TrackEntity?,
    isPlaying: Boolean,
    amplitudes: FloatArray,
    selectedVisualizer: VisualizerType,
    accentColor: Color = AppleCrimson,
    onSelectVisualizer: (VisualizerType) -> Unit,
    onDismiss: () -> Unit
) {
    if (!isOpen) return

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .testTag("fullscreen_visualizer_dialog")
        ) {
            // Real-time visualizer canvas
            ReactiveVisualizerView(
                visualizerType = selectedVisualizer,
                amplitudes = amplitudes,
                accentColor = accentColor,
                isPlaying = isPlaying,
                modifier = Modifier.fillMaxSize()
            )

            // Top bar controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = currentTrack?.title ?: "SLM Visualizer Live",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = currentTrack?.artist ?: "Synchronisation audio en temps réel",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(DarkGlassElevated)
                ) {
                    Icon(Icons.Default.FullscreenExit, contentDescription = "Quitter plein écran", tint = TextPrimary)
                }
            }

            // Bottom Style Switcher Bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = DarkGlassElevated,
                    borderColor = GlassBorder,
                    shape = RoundedCornerShape(24.dp)
                ) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        items(VisualizerType.values()) { type ->
                            val isSelected = selectedVisualizer == type
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) accentColor else DarkGlassCard)
                                    .clickable { onSelectVisualizer(type) }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${type.emoji} ${type.displayName}",
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
