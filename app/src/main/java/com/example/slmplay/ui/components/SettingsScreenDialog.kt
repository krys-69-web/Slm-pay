package com.example.slmplay.ui.components

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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.slmplay.data.model.AppSettings
import com.example.slmplay.data.model.LibrarySortOption
import com.example.slmplay.data.model.VibeMood
import com.example.slmplay.data.model.VisualizerType
import com.example.ui.theme.*

@Composable
fun SettingsScreenDialog(
    isOpen: Boolean,
    settings: AppSettings,
    onDismiss: () -> Unit,
    onUpdateSettings: (AppSettings) -> Unit,
    onOpenMediaConverter: () -> Unit,
    onOpenAudioEditor: () -> Unit,
    onOpenEqualizer: () -> Unit,
    onOpenArtworkStudio: () -> Unit,
    onOpenFullscreenVisualizer: () -> Unit,
    onStartSleepTimer: (Int, Boolean) -> Unit,
    onCancelSleepTimer: () -> Unit
) {
    if (!isOpen) return

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
                    .fillMaxHeight(0.92f)
                    .testTag("settings_screen_dialog"),
                backgroundColor = DarkGlassElevated,
                borderColor = GlassBorder,
                shape = RoundedCornerShape(28.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
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
                                Icon(Icons.Default.Settings, contentDescription = null, tint = AppleCrimson, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Paramètres SLM Play",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Gestion des 13 fonctionnalités & modules",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                        }

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

                    Spacer(modifier = Modifier.height(16.dp))

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Section 1: Outils Multimédia & Édition
                        item {
                            SectionHeader(title = "OUTILS & CRÉATION AUDIO")
                        }

                        // 1. Convertisseur multimédia intégré
                        item {
                            FeatureSettingCard(
                                icon = Icons.Default.Transform,
                                iconColor = AppleCrimson,
                                title = "1. Convertisseur multimédia intégré",
                                subtitle = "Vidéo → MP3/M4A, Audio → MP4, rognage & qualité audio",
                                isChecked = settings.isMediaConverterEnabled,
                                onCheckedChange = { onUpdateSettings(settings.copy(isMediaConverterEnabled = it)) },
                                onActionClick = onOpenMediaConverter,
                                actionButtonText = "Ouvrir l'outil"
                            )
                        }

                        // 2. SLM Audio Editor
                        item {
                            FeatureSettingCard(
                                icon = Icons.Default.ContentCut,
                                iconColor = AppleIndigo,
                                title = "2. SLM Audio Editor",
                                subtitle = "Découper, rogner, fusionner plusieurs pistes, volume & fondus",
                                isChecked = settings.isAudioEditorEnabled,
                                onCheckedChange = { onUpdateSettings(settings.copy(isAudioEditorEnabled = it)) },
                                onActionClick = onOpenAudioEditor,
                                actionButtonText = "Ouvrir l'éditeur"
                            )
                        }

                        // Section 2: Audio & Rendu Sonore
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            SectionHeader(title = "AUDIO, ÉGALISATION & ENCHAÎNEMENTS")
                        }

                        // 3. SLM Equalizer
                        item {
                            FeatureSettingCard(
                                icon = Icons.Default.Tune,
                                iconColor = AppleCrimson,
                                title = "3. SLM Equalizer",
                                subtitle = "Égaliseur matériel (Bass Boost, Vocal, Rock, Pop, Hip-Hop, Lofi...)",
                                isChecked = settings.isEqualizerEnabled,
                                onCheckedChange = { onUpdateSettings(settings.copy(isEqualizerEnabled = it)) },
                                onActionClick = onOpenEqualizer,
                                actionButtonText = "Régler l'égaliseur"
                            )
                        }

                        // 11. Gapless Playback
                        item {
                            FeatureToggleCard(
                                icon = Icons.Default.FastForward,
                                iconColor = Color(0xFF34C759),
                                title = "11. Gapless Playback",
                                subtitle = "Lecture continue sans interruption de silence entre les pistes",
                                isChecked = settings.isGaplessPlaybackEnabled,
                                onCheckedChange = { onUpdateSettings(settings.copy(isGaplessPlaybackEnabled = it)) }
                            )
                        }

                        // 12. Crossfade
                        item {
                            GlassCard(
                                modifier = Modifier.fillMaxWidth(),
                                backgroundColor = DarkGlassCard,
                                borderColor = GlassBorder,
                                shape = RoundedCornerShape(18.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .clip(CircleShape)
                                                    .background(ApplePurple.copy(alpha = 0.2f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.Default.CompareArrows, contentDescription = null, tint = ApplePurple, modifier = Modifier.size(18.dp))
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text("12. Crossfade", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                                                Text("Fondu enchaîné (${settings.crossfadeDurationSeconds}s)", fontSize = 12.sp, color = TextSecondary)
                                            }
                                        }

                                        Switch(
                                            checked = settings.isCrossfadeEnabled,
                                            onCheckedChange = { onUpdateSettings(settings.copy(isCrossfadeEnabled = it)) },
                                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = ApplePurple)
                                        )
                                    }

                                    if (settings.isCrossfadeEnabled) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Durée de transition", fontSize = 11.sp, color = TextTertiary)
                                            Text("${settings.crossfadeDurationSeconds} secondes", fontSize = 11.sp, color = ApplePurple, fontWeight = FontWeight.Bold)
                                        }
                                        Slider(
                                            value = settings.crossfadeDurationSeconds.toFloat(),
                                            onValueChange = { onUpdateSettings(settings.copy(crossfadeDurationSeconds = it.toInt())) },
                                            valueRange = 1f..12f,
                                            steps = 10,
                                            colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = ApplePurple)
                                        )
                                    }
                                }
                            }
                        }

                        // Section 3: Immersion Visuelle & Ambiance
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            SectionHeader(title = "VISUELS, AMBIANCE & GRAPHISMES")
                        }

                        // 4. SLM Visualizer
                        item {
                            FeatureSettingCard(
                                icon = Icons.Default.GraphicEq,
                                iconColor = AppleCrimson,
                                title = "4. SLM Visualizer",
                                subtitle = "Réaction en temps réel (Barres, Cercles, Particules, Vagues, 3D)",
                                isChecked = settings.isVisualizerEnabled,
                                onCheckedChange = { onUpdateSettings(settings.copy(isVisualizerEnabled = it)) },
                                onActionClick = onOpenFullscreenVisualizer,
                                actionButtonText = "Plein écran 3D"
                            )
                        }

                        // 5. SLM Vibe
                        item {
                            GlassCard(
                                modifier = Modifier.fillMaxWidth(),
                                backgroundColor = DarkGlassCard,
                                borderColor = GlassBorder,
                                shape = RoundedCornerShape(18.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(settings.currentVibeMood.colorHex).copy(alpha = 0.2f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(settings.currentVibeMood.emoji, fontSize = 16.sp)
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text("5. SLM Vibe (${settings.currentVibeMood.displayName})", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                                                Text(settings.currentVibeMood.description, fontSize = 12.sp, color = TextSecondary)
                                            }
                                        }

                                        Switch(
                                            checked = settings.isSlmVibeEnabled,
                                            onCheckedChange = { onUpdateSettings(settings.copy(isSlmVibeEnabled = it)) },
                                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(settings.currentVibeMood.colorHex))
                                        )
                                    }

                                    if (settings.isSlmVibeEnabled) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            VibeMood.values().forEach { vibe ->
                                                val isSelected = settings.currentVibeMood == vibe
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .background(if (isSelected) Color(vibe.colorHex).copy(alpha = 0.35f) else DarkGlassElevated)
                                                        .border(1.dp, if (isSelected) Color(vibe.colorHex) else GlassBorder, RoundedCornerShape(10.dp))
                                                        .clickable { onUpdateSettings(settings.copy(currentVibeMood = vibe)) }
                                                        .padding(vertical = 6.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = "${vibe.emoji} ${vibe.displayName}",
                                                        fontSize = 10.sp,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                        color = if (isSelected) Color.White else TextSecondary
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // 6. Dynamic Artwork
                        item {
                            FeatureToggleCard(
                                icon = Icons.Default.Palette,
                                iconColor = Color(0xFFFF9500),
                                title = "6. Dynamic Artwork",
                                subtitle = "Adaptation des teintes de l'interface & flou Apple inspiré de la pochette",
                                isChecked = settings.isDynamicArtworkEnabled,
                                onCheckedChange = { onUpdateSettings(settings.copy(isDynamicArtworkEnabled = it)) }
                            )
                        }

                        // 7. Artwork Studio
                        item {
                            FeatureSettingCard(
                                icon = Icons.Default.Brush,
                                iconColor = Color(0xFF00C7BE),
                                title = "7. Artwork Studio",
                                subtitle = "Éditer ou importer les pochettes & métadonnées du morceau",
                                isChecked = settings.isArtworkStudioEnabled,
                                onCheckedChange = { onUpdateSettings(settings.copy(isArtworkStudioEnabled = it)) },
                                onActionClick = onOpenArtworkStudio,
                                actionButtonText = "Ouvrir le studio"
                            )
                        }

                        // Section 4: Intelligence & Bibliothèque
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            SectionHeader(title = "BIBLIOTHÈQUE & INTELLIGENCE")
                        }

                        // 8. Smart Library
                        item {
                            FeatureToggleCard(
                                icon = Icons.Default.LibraryMusic,
                                iconColor = Color(0xFF007AFF),
                                title = "8. Smart Library",
                                subtitle = "Tri & filtres instantanés (Artiste, Album, Genre, Année, Durée...)",
                                isChecked = settings.isSmartLibraryEnabled,
                                onCheckedChange = { onUpdateSettings(settings.copy(isSmartLibraryEnabled = it)) }
                            )
                        }

                        // 9. Smart Favorites
                        item {
                            FeatureToggleCard(
                                icon = Icons.Default.Favorite,
                                iconColor = AppleCrimson,
                                title = "9. Smart Favorites",
                                subtitle = "Playlist intelligente apprenant vos morceaux les plus écoutés",
                                isChecked = settings.isSmartFavoritesEnabled,
                                onCheckedChange = { onUpdateSettings(settings.copy(isSmartFavoritesEnabled = it)) }
                            )
                        }

                        // 10. Smart Shuffle
                        item {
                            FeatureToggleCard(
                                icon = Icons.Default.Shuffle,
                                iconColor = AppleIndigo,
                                title = "10. Smart Shuffle",
                                subtitle = "Mélange équilibré évitant les répétitions d'artistes consécutifs",
                                isChecked = settings.isSmartShuffleEnabled,
                                onCheckedChange = { onUpdateSettings(settings.copy(isSmartShuffleEnabled = it)) }
                            )
                        }

                        // 13. Sleep Timer
                        item {
                            GlassCard(
                                modifier = Modifier.fillMaxWidth(),
                                backgroundColor = DarkGlassCard,
                                borderColor = GlassBorder,
                                shape = RoundedCornerShape(18.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFF5856D6).copy(alpha = 0.2f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.Default.Bedtime, contentDescription = null, tint = Color(0xFF5856D6), modifier = Modifier.size(18.dp))
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text("13. Sleep Timer (Minuteur de sommeil)", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                                                Text(
                                                    if (settings.isSleepTimerActive) "Arrêt progressif dans ${(settings.sleepTimerRemainingSeconds / 60)}m ${(settings.sleepTimerRemainingSeconds % 60)}s" else "Fondu doux et arrêt automatique",
                                                    fontSize = 12.sp,
                                                    color = if (settings.isSleepTimerActive) AppleCrimson else TextSecondary
                                                )
                                            }
                                        }

                                        if (settings.isSleepTimerActive) {
                                            TextButton(onClick = onCancelSleepTimer) {
                                                Text("Annuler", color = AppleCrimson, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        listOf(15, 30, 45, 60).forEach { mins ->
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(DarkGlassElevated)
                                                    .clickable { onStartSleepTimer(mins, false) }
                                                    .padding(vertical = 8.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("${mins} min", fontSize = 11.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                                            }
                                        }
                                        Box(
                                            modifier = Modifier
                                                .weight(1.3f)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(DarkGlassElevated)
                                                .clickable { onStartSleepTimer(0, true) }
                                                .padding(vertical = 8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("Fin de piste", fontSize = 11.sp, color = AppleCrimson, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = AppleCrimson),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text("Fermer les Paramètres", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = TextTertiary,
        letterSpacing = 1.2.sp,
        modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
    )
}

@Composable
private fun FeatureToggleCard(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = DarkGlassCard,
        borderColor = GlassBorder,
        shape = RoundedCornerShape(18.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(iconColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(title, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                    Text(subtitle, fontSize = 12.sp, color = TextSecondary)
                }
            }

            Switch(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = iconColor)
            )
        }
    }
}

@Composable
private fun FeatureSettingCard(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onActionClick: () -> Unit,
    actionButtonText: String
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = DarkGlassCard,
        borderColor = GlassBorder,
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(iconColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(title, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                        Text(subtitle, fontSize = 12.sp, color = TextSecondary)
                    }
                }

                Switch(
                    checked = isChecked,
                    onCheckedChange = onCheckedChange,
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = iconColor)
                )
            }

            if (isChecked) {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onActionClick,
                    colors = ButtonDefaults.buttonColors(containerColor = iconColor.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(actionButtonText, color = iconColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}
