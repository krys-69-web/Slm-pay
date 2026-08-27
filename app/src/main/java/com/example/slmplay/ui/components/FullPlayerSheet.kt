package com.example.slmplay.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.slmplay.data.db.TrackEntity
import com.example.slmplay.data.model.BackgroundMode
import com.example.slmplay.data.model.RepeatMode
import com.example.slmplay.data.model.VisualizerType
import com.example.ui.theme.*

@Composable
fun FullPlayerSheet(
    isOpen: Boolean,
    currentTrack: TrackEntity?,
    isPlaying: Boolean,
    currentPositionMs: Long,
    durationMs: Long,
    repeatMode: RepeatMode,
    isShuffle: Boolean,
    playbackSpeed: Float,
    backgroundMode: BackgroundMode,
    isLyricsOpen: Boolean,
    accentColor: Color = AppleCrimson,
    audioAmplitudes: FloatArray = FloatArray(16) { 0.25f },
    visualizerType: VisualizerType = VisualizerType.BARS,
    onClose: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onToggleRepeat: () -> Unit,
    onToggleShuffle: () -> Unit,
    onSetSpeed: (Float) -> Unit,
    onSetBackgroundMode: (BackgroundMode) -> Unit,
    onToggleFavorite: (TrackEntity) -> Unit,
    onToggleLyrics: () -> Unit,
    onOpenEqualizer: () -> Unit = {},
    onOpenFullscreenVisualizer: () -> Unit = {},
    onOpenArtworkStudio: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isOpen && currentTrack != null,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = modifier
    ) {
        if (currentTrack == null) return@AnimatedVisibility

        var isDraggingSlider by remember { mutableStateOf(false) }
        var sliderDragPosition by remember { mutableFloatStateOf(0f) }
        var showSpeedMenu by remember { mutableStateOf(false) }

        val artworkScale by animateFloatAsState(
            targetValue = if (isPlaying) 1.0f else 0.88f,
            animationSpec = spring(dampingRatio = 0.7f),
            label = "ArtworkScale"
        )

        val currentProgress = if (isDraggingSlider) {
            sliderDragPosition
        } else {
            if (durationMs > 0) (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkCanvas)
                .pointerInput(Unit) {
                    detectVerticalDragGestures { _, dragAmount ->
                        if (dragAmount > 35) {
                            onClose()
                        }
                    }
                }
                .testTag("full_player_sheet")
        ) {
            // Dynamic blurred halo background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(accentColor.copy(alpha = 0.22f), DarkCanvas),
                            center = androidx.compose.ui.geometry.Offset(500f, 600f),
                            radius = 1200f
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(10.dp))

                // Drag indicator pill
                Box(
                    modifier = Modifier
                        .width(42.dp)
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color.White.copy(alpha = 0.25f))
                        .clickable(onClick = onClose)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Top bar: Dismiss Arrow & Feature Action Icons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(DarkGlassCard)
                            .testTag("full_player_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Réduire le lecteur",
                            tint = TextPrimary,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "LECTURE EN COURS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextTertiary,
                            letterSpacing = 1.5.sp
                        )
                        Text(
                            text = currentTrack.album,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Equalizer quick button
                        IconButton(
                            onClick = onOpenEqualizer,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(DarkGlassCard)
                        ) {
                            Icon(Icons.Default.Tune, contentDescription = "Égaliseur", tint = accentColor, modifier = Modifier.size(18.dp))
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Fullscreen Visualizer button
                        IconButton(
                            onClick = onOpenFullscreenVisualizer,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(DarkGlassCard)
                        ) {
                            Icon(Icons.Default.GraphicEq, contentDescription = "Visualiseur 3D", tint = TextPrimary, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Main Album Artwork / Visualizer Card
                if (!isLyricsOpen) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .scale(artworkScale)
                            .shadow(
                                elevation = if (isPlaying) 32.dp else 12.dp,
                                shape = RoundedCornerShape(26.dp),
                                spotColor = accentColor.copy(alpha = 0.45f),
                                ambientColor = Color.Black
                            )
                            .clip(RoundedCornerShape(26.dp))
                            .background(DarkGlassElevated)
                            .clickable { onOpenArtworkStudio() },
                        contentAlignment = Alignment.Center
                    ) {
                        if (currentTrack.coverUri != null) {
                            AsyncImage(
                                model = currentTrack.coverUri,
                                contentDescription = "Pochette de ${currentTrack.title}",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            val resId = when (currentTrack.coverResName) {
                                "cover_neon" -> R.drawable.cover_neon
                                "cover_ambient" -> R.drawable.cover_ambient
                                else -> R.drawable.slm_logo
                            }
                            Image(
                                painter = painterResource(id = resId),
                                contentDescription = "Pochette par défaut",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }

                        // Bottom mini wave overlay on cover
                        ReactiveVisualizerView(
                            visualizerType = visualizerType,
                            amplitudes = audioAmplitudes,
                            accentColor = accentColor,
                            isPlaying = isPlaying,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .align(Alignment.BottomCenter)
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.65f))
                                    )
                                )
                        )
                    }
                } else {
                    // SLM Vibe & Paroles Card
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .testTag("full_player_lyrics_box"),
                        shape = RoundedCornerShape(26.dp),
                        backgroundColor = DarkGlassElevated,
                        borderColor = GlassBorder
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(20.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "🤖 SLM Vibe Assistant",
                                    color = accentColor,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Modèle Local Actif",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "« Cette piste diffuse une énergie ${currentTrack.genre}. La spatialisation dynamique s'adapte à vos mouvements avec une résonance feutrée. »",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                lineHeight = 20.sp
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            GlassCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                backgroundColor = DarkGlassCard
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("PROFIL ACOUSTIQUE", fontSize = 10.sp, color = TextTertiary, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("• Fréquence : 44.1 kHz Hi-Fi", color = TextSecondary, fontSize = 12.sp)
                                    Text("• Ambiance : ${currentTrack.genre}", color = TextSecondary, fontSize = 12.sp)
                                    Text("• Traitement SLM : Optimisation spatiale stéréo", color = accentColor, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Title, Artist, and Favorite button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                        Text(
                            text = currentTrack.title,
                            fontSize = 23.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            letterSpacing = (-0.5).sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${currentTrack.artist} • ${currentTrack.album}",
                            fontSize = 14.sp,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    IconButton(
                        onClick = { onToggleFavorite(currentTrack) },
                        modifier = Modifier.testTag("full_player_favorite_button")
                    ) {
                        Icon(
                            imageVector = if (currentTrack.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (currentTrack.isFavorite) "Retirer des favoris" else "Ajouter aux favoris",
                            tint = if (currentTrack.isFavorite) accentColor else TextSecondary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Progress scrub slider
                Slider(
                    value = currentProgress,
                    onValueChange = {
                        isDraggingSlider = true
                        sliderDragPosition = it
                    },
                    onValueChangeFinished = {
                        isDraggingSlider = false
                        onSeekTo((sliderDragPosition * durationMs).toLong())
                    },
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = accentColor,
                        inactiveTrackColor = Color.White.copy(alpha = 0.12f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("full_player_slider")
                )

                // Timestamp labels
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val activePosition = if (isDraggingSlider) {
                        (sliderDragPosition * durationMs).toLong()
                    } else currentPositionMs

                    Text(
                        text = formatTime(activePosition),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextTertiary
                    )
                    Text(
                        text = formatTime(durationMs),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextTertiary
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Primary Playback Controls Deck
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    IconButton(
                        onClick = onToggleShuffle,
                        modifier = Modifier.testTag("full_player_shuffle_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shuffle,
                            contentDescription = "Aléatoire",
                            tint = if (isShuffle) accentColor else TextTertiary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    IconButton(
                        onClick = onSkipPrevious,
                        modifier = Modifier.size(48.dp).testTag("full_player_skip_prev_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "Précédent",
                            tint = TextPrimary,
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .shadow(20.dp, CircleShape, spotColor = accentColor.copy(alpha = 0.4f), ambientColor = Color.Black)
                            .clip(CircleShape)
                            .background(Color.White)
                            .clickable(onClick = onTogglePlayPause)
                            .testTag("full_player_play_pause_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Lecture",
                            tint = Color.Black,
                            modifier = Modifier.size(38.dp)
                        )
                    }

                    IconButton(
                        onClick = onSkipNext,
                        modifier = Modifier.size(48.dp).testTag("full_player_skip_next_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Suivant",
                            tint = TextPrimary,
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    IconButton(
                        onClick = onToggleRepeat,
                        modifier = Modifier.testTag("full_player_repeat_button")
                    ) {
                        Icon(
                            imageVector = if (repeatMode == RepeatMode.ONE) Icons.Default.RepeatOne else Icons.Default.Repeat,
                            contentDescription = "Répéter",
                            tint = if (repeatMode != RepeatMode.OFF) accentColor else TextTertiary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Bottom Utilities bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box {
                        GlassCard(
                            modifier = Modifier.testTag("full_player_speed_pill"),
                            shape = RoundedCornerShape(14.dp),
                            backgroundColor = DarkGlassCard,
                            onClick = { showSpeedMenu = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Speed, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("${playbackSpeed}x", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        DropdownMenu(
                            expanded = showSpeedMenu,
                            onDismissRequest = { showSpeedMenu = false },
                            modifier = Modifier.background(DarkSurface)
                        ) {
                            listOf(0.8f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { spd ->
                                DropdownMenuItem(
                                    text = { Text("${spd}x", color = if (spd == playbackSpeed) accentColor else TextPrimary) },
                                    onClick = {
                                        onSetSpeed(spd)
                                        showSpeedMenu = false
                                    }
                                )
                            }
                        }
                    }

                    GlassCard(
                        modifier = Modifier.testTag("full_player_lyrics_button"),
                        shape = RoundedCornerShape(14.dp),
                        backgroundColor = if (isLyricsOpen) accentColor.copy(alpha = 0.3f) else DarkGlassCard,
                        borderColor = if (isLyricsOpen) accentColor else GlassBorder,
                        onClick = onToggleLyrics
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChatBubbleOutline,
                                contentDescription = "SLM Vibe & Paroles",
                                tint = if (isLyricsOpen) accentColor else TextPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "SLM Vibe",
                                color = if (isLyricsOpen) accentColor else TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    GlassCard(
                        shape = RoundedCornerShape(14.dp),
                        backgroundColor = DarkGlassCard,
                        onClick = onOpenArtworkStudio
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Palette, contentDescription = null, tint = accentColor, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Artwork", color = TextSecondary, fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}
