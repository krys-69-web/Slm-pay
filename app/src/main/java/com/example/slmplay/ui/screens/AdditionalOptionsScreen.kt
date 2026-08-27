package com.example.slmplay.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.slmplay.data.model.AppSettings
import com.example.slmplay.data.model.VisualizerType
import com.example.slmplay.ui.components.GlassCard
import com.example.ui.theme.*

@Composable
fun AdditionalOptionsScreen(
    appSettings: AppSettings,
    onOpenMediaConverter: () -> Unit,
    onOpenAudioEditor: () -> Unit,
    onOpenArtworkStudio: () -> Unit,
    onOpenEqualizer: () -> Unit,
    onOpenVisualizerFullscreen: () -> Unit,
    onOpenSettings: () -> Unit,
    onSelectVisualizerType: (VisualizerType) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            // Section Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(listOf(AppleCrimson, ApplePurple))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Widgets,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Options additionnelles",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Studio de production sonore & outils multimédias",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // 1. Convertisseur Multimédia Vidéo / Audio
        item {
            ToolActionCard(
                icon = Icons.Default.Transform,
                iconGradient = listOf(AppleCrimson, Color(0xFFFF9500)),
                title = "Convertisseur Vidéo / Audio",
                subtitle = "Vidéo → MP3 / M4A et Audio → MP4 avec visuel ou onde sonore",
                badgeText = "Studio Pro",
                testTag = "card_media_converter",
                onClick = onOpenMediaConverter
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // 2. SLM Audio Editor
        item {
            ToolActionCard(
                icon = Icons.Default.ContentCut,
                iconGradient = listOf(Color(0xFF007AFF), Color(0xFF5856D6)),
                title = "Éditeur Audio & Découpe",
                subtitle = "Rognage précis, fondu entrant/sortant, gain de volume et fusion",
                badgeText = "Non-destructif",
                testTag = "card_audio_editor",
                onClick = onOpenAudioEditor
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // 3. Gestionnaire de pochettes (Artwork Studio)
        item {
            ToolActionCard(
                icon = Icons.Default.Brush,
                iconGradient = listOf(ApplePurple, AppleCrimson),
                title = "Gestionnaire de Pochettes & Métadonnées",
                subtitle = "Personnalisation d'images, recadrage, tags ID3 et retour à l'original",
                badgeText = "Artwork Studio",
                testTag = "card_artwork_studio",
                onClick = onOpenArtworkStudio
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // 4. Visualiseur Réactif & Plein écran
        item {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_visualizer_section"),
                shape = RoundedCornerShape(22.dp),
                backgroundColor = DarkGlassElevated
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        Brush.linearGradient(listOf(Color(0xFF00C7BE), Color(0xFF30B0C7)))
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Waves, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Visualiseur de Fréquences", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text("Animations synchronisées au rythme", fontSize = 12.sp, color = TextSecondary)
                            }
                        }

                        // Fullscreen button
                        IconButton(
                            onClick = onOpenVisualizerFullscreen,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(AppleCrimson.copy(alpha = 0.2f))
                        ) {
                            Icon(Icons.Default.Fullscreen, contentDescription = "Plein écran", tint = AppleCrimson, modifier = Modifier.size(20.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Visualizer Type Selector Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        VisualizerType.entries.forEach { type ->
                            val isSelected = appSettings.visualizerType == type
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) AppleCrimson else DarkGlassCard)
                                    .border(1.dp, if (isSelected) AppleCrimson else GlassBorder, RoundedCornerShape(10.dp))
                                    .clickable { onSelectVisualizerType(type) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(type.emoji, fontSize = 14.sp)
                                    Text(
                                        text = type.displayName.take(8),
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // 5. SLM Equalizer & Sleep Timer Quick Actions
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Equalizer Card
                GlassCard(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onOpenEqualizer() }
                        .testTag("card_quick_equalizer"),
                    shape = RoundedCornerShape(20.dp),
                    backgroundColor = DarkGlassElevated
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(AppleCrimson.copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Equalizer, contentDescription = null, tint = AppleCrimson, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Égaliseur", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(
                            text = "Préset: ${appSettings.selectedEqPreset}",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                // Global Settings Card
                GlassCard(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onOpenSettings() }
                        .testTag("card_quick_settings"),
                    shape = RoundedCornerShape(20.dp),
                    backgroundColor = DarkGlassElevated
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(ApplePurple.copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Tune, contentDescription = null, tint = ApplePurple, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Paramètres SLM", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(
                            text = "13 fonctionnalités",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ToolActionCard(
    icon: ImageVector,
    iconGradient: List<Color>,
    title: String,
    subtitle: String,
    badgeText: String,
    testTag: String,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag(testTag),
        shape = RoundedCornerShape(22.dp),
        backgroundColor = DarkGlassElevated
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Brush.linearGradient(iconGradient)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(26.dp))
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(GlassBorder)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badgeText,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextTertiary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                Icons.Default.ArrowForwardIos,
                contentDescription = null,
                tint = TextTertiary,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
