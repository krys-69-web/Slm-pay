package com.example.slmplay.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.slmplay.data.db.TrackEntity
import com.example.slmplay.utils.AudioEditorUtil
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun AudioEditorDialog(
    isOpen: Boolean,
    tracks: List<TrackEntity>,
    initialTrack: TrackEntity?,
    onDismiss: () -> Unit,
    onTrackExported: (TrackEntity) -> Unit
) {
    if (!isOpen) return

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedTab by remember { mutableStateOf("TRIM") } // "TRIM" or "MERGE"
    var selectedTrack by remember { mutableStateOf(initialTrack ?: tracks.firstOrNull()) }
    var selectedTracksForMerge by remember { mutableStateOf<List<TrackEntity>>(listOfNotNull(initialTrack)) }

    var trimStartSec by remember { mutableStateOf(0f) }
    var trimEndSec by remember { mutableStateOf(60f) }
    var volumeGain by remember { mutableStateOf(1.0f) }
    var fadeInSec by remember { mutableStateOf(1.5f) }
    var fadeOutSec by remember { mutableStateOf(2.0f) }
    var outputTitle by remember { mutableStateOf("") }

    var isProcessing by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = { if (!isProcessing) onDismiss() },
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
                    .testTag("audio_editor_dialog"),
                backgroundColor = DarkGlassElevated,
                borderColor = GlassBorder,
                shape = RoundedCornerShape(28.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    // Top Bar
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
                                    .background(AppleIndigo.copy(alpha = 0.25f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.ContentCut, contentDescription = null, tint = AppleIndigo, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "SLM Audio Editor",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Découper, rogner, fusionner & effets audio",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            enabled = !isProcessing,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(DarkGlassCard)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Fermer", tint = TextSecondary, modifier = Modifier.size(18.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Tab selector
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(DarkGlassCard)
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selectedTab == "TRIM") AppleCrimson else Color.Transparent)
                                .clickable { selectedTab = "TRIM" }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("✂️ Découper / Rogner", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = if (selectedTab == "TRIM") Color.White else TextSecondary)
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selectedTab == "MERGE") AppleCrimson else Color.Transparent)
                                .clickable { selectedTab = "MERGE" }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🔀 Fusionner pistes", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = if (selectedTab == "MERGE") Color.White else TextSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        if (selectedTab == "TRIM") {
                            item {
                                Text("CHOISIR LE MORCEAU À ÉDITER", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextTertiary, letterSpacing = 1.sp)
                                Spacer(modifier = Modifier.height(8.dp))

                                // Track selection pill
                                selectedTrack?.let { trk ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(DarkGlassCard)
                                            .border(1.dp, GlassBorder, RoundedCornerShape(14.dp))
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.GraphicEq, contentDescription = null, tint = AppleCrimson)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(trk.title, fontWeight = FontWeight.SemiBold, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            Text("${trk.artist} • ${(trk.durationMs / 1000)}s", fontSize = 12.sp, color = TextSecondary)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                                Text("PLAGE DE ROGNAGE (DÉBUT / FIN)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextTertiary, letterSpacing = 1.sp)
                                Spacer(modifier = Modifier.height(6.dp))

                                val maxDuration = ((selectedTrack?.durationMs ?: 180000L) / 1000L).toFloat().coerceIn(30f, 600f)

                                Text("Début : ${trimStartSec.toInt()}s  |  Fin : ${trimEndSec.toInt()}s  (Durée : ${(trimEndSec - trimStartSec).toInt()}s)", fontSize = 12.sp, color = AppleCrimson, fontWeight = FontWeight.SemiBold)
                                RangeSlider(
                                    value = trimStartSec..trimEndSec.coerceAtMost(maxDuration),
                                    onValueChange = { range ->
                                        trimStartSec = range.start
                                        trimEndSec = range.endInclusive
                                    },
                                    valueRange = 0f..maxDuration,
                                    colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = AppleCrimson)
                                )

                                Spacer(modifier = Modifier.height(16.dp))
                                Text("MODIFICATION DU VOLUME (${(volumeGain * 100).toInt()}%)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextTertiary, letterSpacing = 1.sp)
                                Slider(
                                    value = volumeGain,
                                    onValueChange = { volumeGain = it },
                                    valueRange = 0.5f..2.0f,
                                    colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = AppleIndigo)
                                )

                                Spacer(modifier = Modifier.height(14.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("FONDU ENTRANT (FADE IN)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextTertiary)
                                        Text("${String.format("%.1f", fadeInSec)} sec", fontSize = 12.sp, color = TextSecondary)
                                        Slider(
                                            value = fadeInSec,
                                            onValueChange = { fadeInSec = it },
                                            valueRange = 0f..5f,
                                            colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = AppleCrimson)
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("FONDU SORTANT (FADE OUT)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextTertiary)
                                        Text("${String.format("%.1f", fadeOutSec)} sec", fontSize = 12.sp, color = TextSecondary)
                                        Slider(
                                            value = fadeOutSec,
                                            onValueChange = { fadeOutSec = it },
                                            valueRange = 0f..5f,
                                            colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = AppleCrimson)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))
                                OutlinedTextField(
                                    value = outputTitle,
                                    onValueChange = { outputTitle = it },
                                    label = { Text("Titre de la nouvelle version exportée") },
                                    placeholder = { Text("${selectedTrack?.title ?: "Titre"} (Remix SLM)") },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AppleIndigo,
                                        unfocusedBorderColor = GlassBorder,
                                        focusedLabelColor = AppleIndigo,
                                        unfocusedLabelColor = TextTertiary,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    ),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        } else {
                            // Merge Tracks
                            item {
                                Text("SÉLECTIONNER LES MORCEAUX À FUSIONNER", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextTertiary, letterSpacing = 1.sp)
                                Spacer(modifier = Modifier.height(8.dp))

                                tracks.forEach { track ->
                                    val isIncluded = selectedTracksForMerge.any { it.id == track.id }
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isIncluded) AppleIndigo.copy(alpha = 0.25f) else DarkGlassCard)
                                            .border(1.dp, if (isIncluded) AppleIndigo else GlassBorder, RoundedCornerShape(12.dp))
                                            .clickable {
                                                selectedTracksForMerge = if (isIncluded) {
                                                    selectedTracksForMerge.filter { it.id != track.id }
                                                } else {
                                                    selectedTracksForMerge + track
                                                }
                                            }
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = isIncluded,
                                            onCheckedChange = { checked ->
                                                selectedTracksForMerge = if (checked) selectedTracksForMerge + track else selectedTracksForMerge.filter { it.id != track.id }
                                            },
                                            colors = CheckboxDefaults.colors(checkedColor = AppleIndigo)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(track.title, fontWeight = FontWeight.SemiBold, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            Text(track.artist, fontSize = 12.sp, color = TextSecondary, maxLines = 1)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                                OutlinedTextField(
                                    value = outputTitle,
                                    onValueChange = { outputTitle = it },
                                    label = { Text("Nom du Mix / Fusion") },
                                    placeholder = { Text("SLM Mega Mix Fusion") },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AppleIndigo,
                                        unfocusedBorderColor = GlassBorder,
                                        focusedLabelColor = AppleIndigo,
                                        unfocusedLabelColor = TextTertiary,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    ),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (statusMessage != null) {
                        Text(
                            text = statusMessage!!,
                            fontSize = 12.sp,
                            color = AppleIndigo,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Button(
                        onClick = {
                            isProcessing = true
                            statusMessage = "Traitement audio et export sans toucher à l'original..."
                            scope.launch {
                                if (selectedTab == "TRIM") {
                                    val trk = selectedTrack ?: tracks.firstOrNull()
                                    if (trk != null) {
                                        val res = AudioEditorUtil.trimAndProcessAudio(
                                            context = context,
                                            sourceTrack = trk,
                                            startMs = (trimStartSec * 1000).toLong(),
                                            endMs = (trimEndSec * 1000).toLong(),
                                            volumeMultiplier = volumeGain,
                                            fadeInSeconds = fadeInSec,
                                            fadeOutSeconds = fadeOutSec,
                                            outputTitle = outputTitle.ifBlank { "${trk.title} (Édité)" }
                                        )
                                        if (res.success && res.outputTrack != null) {
                                            onTrackExported(res.outputTrack)
                                            statusMessage = "Piste exportée dans la bibliothèque !"
                                            onDismiss()
                                        } else {
                                            statusMessage = "Erreur : ${res.errorMessage}"
                                        }
                                    }
                                } else {
                                    if (selectedTracksForMerge.isNotEmpty()) {
                                        val res = AudioEditorUtil.mergeAudioTracks(
                                            context = context,
                                            tracks = selectedTracksForMerge,
                                            outputTitle = outputTitle.ifBlank { "SLM Fusion Mix" }
                                        )
                                        if (res.success && res.outputTrack != null) {
                                            onTrackExported(res.outputTrack)
                                            statusMessage = "Fusion réussie !"
                                            onDismiss()
                                        } else {
                                            statusMessage = "Erreur : ${res.errorMessage}"
                                        }
                                    }
                                }
                                isProcessing = false
                            }
                        },
                        enabled = !isProcessing,
                        colors = ButtonDefaults.buttonColors(containerColor = AppleIndigo),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("audio_editor_export_button")
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Exportation en cours...", color = Color.White, fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Default.SaveAlt, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Exporter nouvelle version (Non-destructive)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}
