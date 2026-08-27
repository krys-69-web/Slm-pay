package com.example.slmplay.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
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
import com.example.ui.theme.*

@Composable
fun EqualizerDialog(
    isOpen: Boolean,
    isEnabled: Boolean,
    currentPreset: String,
    manualBands: List<Float>,
    bassBoost: Float,
    virtualizer: Float,
    onDismiss: () -> Unit,
    onToggleEnabled: (Boolean) -> Unit,
    onSelectPreset: (String) -> Unit,
    onBandChanged: (Int, Float) -> Unit,
    onBassBoostChanged: (Float) -> Unit,
    onVirtualizerChanged: (Float) -> Unit
) {
    if (!isOpen) return

    val presets = listOf(
        "Bass Boost",
        "Vocal",
        "Rock",
        "Pop",
        "Hip-Hop",
        "Lofi",
        "Classique",
        "Manuel"
    )

    val bandLabels = listOf("60 Hz", "230 Hz", "910 Hz", "3.6 kHz", "14 kHz")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .testTag("slm_equalizer_dialog"),
                backgroundColor = DarkGlassElevated,
                borderColor = GlassBorder,
                shape = RoundedCornerShape(28.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(AppleCrimson.copy(alpha = 0.25f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Tune, contentDescription = null, tint = AppleCrimson, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "SLM Equalizer",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Égaliseur matériel & effets audio studio",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Switch(
                                checked = isEnabled,
                                onCheckedChange = onToggleEnabled,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = AppleCrimson
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(DarkGlassCard)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Fermer", tint = TextSecondary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Presets Carousel
                    Text("PRÉSETS AUDIO SLM", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextTertiary, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(presets) { preset ->
                            val isSelected = currentPreset == preset
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) AppleCrimson else DarkGlassCard)
                                    .border(1.dp, if (isSelected) AppleCrimson else GlassBorder, RoundedCornerShape(12.dp))
                                    .clickable { onSelectPreset(preset) }
                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = preset,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else TextPrimary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 5-Band Equalizer Sliders
                    Text("ÉGALISEUR 5 BANDES (MANUEL)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextTertiary, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(DarkGlassCard)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        bandLabels.forEachIndexed { index, label ->
                            val gain = manualBands.getOrElse(index) { 0f }
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            ) {
                                Text(
                                    text = if (gain > 0) "+${gain.toInt()}dB" else "${gain.toInt()}dB",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (gain != 0f) AppleCrimson else TextTertiary
                                )

                                Slider(
                                    value = gain,
                                    onValueChange = { onBandChanged(index, it) },
                                    valueRange = -12f..12f,
                                    modifier = Modifier.weight(1f),
                                    colors = SliderDefaults.colors(
                                        thumbColor = Color.White,
                                        activeTrackColor = AppleCrimson,
                                        inactiveTrackColor = Color.White.copy(alpha = 0.15f)
                                    )
                                )

                                Text(
                                    text = label,
                                    fontSize = 10.sp,
                                    color = TextSecondary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Bass Boost & Virtualizer
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Bass Boost Card
                        GlassCard(
                            modifier = Modifier.weight(1f),
                            backgroundColor = DarkGlassCard,
                            borderColor = GlassBorder,
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("🔊 Bass Boost", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Text("${(bassBoost * 100).toInt()}%", fontSize = 12.sp, color = AppleCrimson, fontWeight = FontWeight.Bold)
                                }
                                Slider(
                                    value = bassBoost,
                                    onValueChange = onBassBoostChanged,
                                    valueRange = 0f..1f,
                                    colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = AppleCrimson)
                                )
                            }
                        }

                        // 3D Virtualizer Card
                        GlassCard(
                            modifier = Modifier.weight(1f),
                            backgroundColor = DarkGlassCard,
                            borderColor = GlassBorder,
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("🎧 Virtualizer 3D", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Text("${(virtualizer * 100).toInt()}%", fontSize = 12.sp, color = AppleIndigo, fontWeight = FontWeight.Bold)
                                }
                                Slider(
                                    value = virtualizer,
                                    onValueChange = onVirtualizerChanged,
                                    valueRange = 0f..1f,
                                    colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = AppleIndigo)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = AppleCrimson),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Appliquer & Fermer", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
