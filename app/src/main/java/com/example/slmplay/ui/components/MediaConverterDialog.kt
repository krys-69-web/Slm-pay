package com.example.slmplay.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.example.slmplay.service.ProceduralAudioGenerator
import com.example.slmplay.utils.MediaConverterUtil
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun MediaConverterDialog(
    isOpen: Boolean,
    tracks: List<TrackEntity>,
    onDismiss: () -> Unit,
    onTrackConverted: (TrackEntity) -> Unit
) {
    if (!isOpen) return

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var conversionType by remember { mutableStateOf("VIDEO_TO_AUDIO") } // "VIDEO_TO_AUDIO" or "AUDIO_TO_MP4"
    var selectedOutputFormat by remember { mutableStateOf("M4A") } // M4A, MP3, WAV
    var selectedBitrate by remember { mutableStateOf(256) } // 128, 192, 256, 320
    var trimStartSec by remember { mutableStateOf(0f) }
    var trimEndSec by remember { mutableStateOf(180f) }
    var isTrimmingEnabled by remember { mutableStateOf(false) }
    var customTitle by remember { mutableStateOf("") }
    var selectedVisualizerStyle by remember { mutableStateOf("Neon Wave") }

    var selectedUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var selectedTrackForMp4 by remember { mutableStateOf<TrackEntity?>(tracks.firstOrNull()) }

    var isConverting by remember { mutableStateOf(false) }
    var conversionStatus by remember { mutableStateOf<String?>(null) }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (!uris.isNullOrEmpty()) {
            selectedUris = uris
        }
    }

    Dialog(
        onDismissRequest = { if (!isConverting) onDismiss() },
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
                    .testTag("media_converter_dialog"),
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
                                    .background(AppleCrimson.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Transform, contentDescription = null, tint = AppleCrimson, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Convertisseur Multimédia",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Vidéo → MP3/M4A & Audio → MP4 Studio",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            enabled = !isConverting,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(DarkGlassCard)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Fermer", tint = TextSecondary, modifier = Modifier.size(18.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Mode Segment Switcher
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
                                .background(if (conversionType == "VIDEO_TO_AUDIO") AppleCrimson else Color.Transparent)
                                .clickable { conversionType = "VIDEO_TO_AUDIO" }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🎬 Vidéo → Audio",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (conversionType == "VIDEO_TO_AUDIO") Color.White else TextSecondary
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (conversionType == "AUDIO_TO_MP4") AppleCrimson else Color.Transparent)
                                .clickable { conversionType = "AUDIO_TO_MP4" }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🎵 Audio → Vidéo MP4",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (conversionType == "AUDIO_TO_MP4") Color.White else TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        if (conversionType == "VIDEO_TO_AUDIO") {
                            // Video to Audio Options
                            item {
                                Text("1. FICHIERS SOURCE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextTertiary, letterSpacing = 1.sp)
                                Spacer(modifier = Modifier.height(8.dp))

                                Button(
                                    onClick = { filePicker.launch(arrayOf("video/*", "audio/*")) },
                                    colors = ButtonDefaults.buttonColors(containerColor = DarkGlassCard),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.FileOpen, contentDescription = null, tint = AppleCrimson)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (selectedUris.isEmpty()) "Sélectionner vidéo(s) ou audio(s)" else "${selectedUris.size} fichier(s) sélectionné(s)",
                                        color = TextPrimary
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                                Text("2. FORMAT & QUALITÉ AUDIO", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextTertiary, letterSpacing = 1.sp)
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf("M4A", "MP3", "WAV").forEach { fmt ->
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(if (selectedOutputFormat == fmt) AppleCrimson.copy(alpha = 0.25f) else DarkGlassCard)
                                                .border(1.dp, if (selectedOutputFormat == fmt) AppleCrimson else GlassBorder, RoundedCornerShape(12.dp))
                                                .clickable { selectedOutputFormat = fmt }
                                                .padding(vertical = 10.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(fmt, color = if (selectedOutputFormat == fmt) AppleCrimson else TextPrimary, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    listOf(128, 192, 256, 320).forEach { kbps ->
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(if (selectedBitrate == kbps) DarkGlassCard else Color.Transparent)
                                                .border(1.dp, if (selectedBitrate == kbps) AppleCrimson else Color.Transparent, RoundedCornerShape(10.dp))
                                                .clickable { selectedBitrate = kbps }
                                                .padding(vertical = 6.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("$kbps kbps", fontSize = 11.sp, color = if (selectedBitrate == kbps) AppleCrimson else TextTertiary)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("✂️ DÉCOUPER UN PASSAGE (TRIM)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextTertiary, letterSpacing = 1.sp)
                                    Switch(
                                        checked = isTrimmingEnabled,
                                        onCheckedChange = { isTrimmingEnabled = it },
                                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = AppleCrimson)
                                    )
                                }

                                if (isTrimmingEnabled) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Début : ${trimStartSec.toInt()}s — Fin : ${trimEndSec.toInt()}s", fontSize = 12.sp, color = TextSecondary)
                                    RangeSlider(
                                        value = trimStartSec..trimEndSec,
                                        onValueChange = { range ->
                                            trimStartSec = range.start
                                            trimEndSec = range.endInclusive
                                        },
                                        valueRange = 0f..300f,
                                        colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = AppleCrimson)
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                                OutlinedTextField(
                                    value = customTitle,
                                    onValueChange = { customTitle = it },
                                    label = { Text("Titre de la nouvelle piste (Optionnel)") },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AppleCrimson,
                                        unfocusedBorderColor = GlassBorder,
                                        focusedLabelColor = AppleCrimson,
                                        unfocusedLabelColor = TextTertiary,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    ),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        } else {
                            // Audio to MP4 Video options
                            item {
                                Text("1. CHOISIR LE MORCEAU AUDIO", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextTertiary, letterSpacing = 1.sp)
                                Spacer(modifier = Modifier.height(8.dp))

                                tracks.forEach { track ->
                                    val isSelected = selectedTrackForMp4?.id == track.id
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isSelected) AppleCrimson.copy(alpha = 0.2f) else DarkGlassCard)
                                            .border(1.dp, if (isSelected) AppleCrimson else GlassBorder, RoundedCornerShape(12.dp))
                                            .clickable { selectedTrackForMp4 = track }
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.MusicNote,
                                            contentDescription = null,
                                            tint = if (isSelected) AppleCrimson else TextSecondary
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(track.title, fontWeight = FontWeight.SemiBold, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            Text(track.artist, fontSize = 12.sp, color = TextSecondary, maxLines = 1)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                                Text("2. STYLE DU VISUALISEUR INTÉGRÉ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextTertiary, letterSpacing = 1.sp)
                                Spacer(modifier = Modifier.height(8.dp))

                                listOf("Neon Wave Spectrum", "Apple Glass Diffuse Aura", "Cyberpunk Particle Burst", "HOpE 3D Horizon").forEach { style ->
                                    val isSelected = selectedVisualizerStyle == style
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isSelected) DarkGlassCard else Color.Transparent)
                                            .border(1.dp, if (isSelected) AppleCrimson else GlassBorder, RoundedCornerShape(12.dp))
                                            .clickable { selectedVisualizerStyle = style }
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = { selectedVisualizerStyle = style },
                                            colors = RadioButtonDefaults.colors(selectedColor = AppleCrimson)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(style, color = TextPrimary, fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (conversionStatus != null) {
                        Text(
                            text = conversionStatus!!,
                            fontSize = 12.sp,
                            color = AppleCrimson,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Convert Button
                    Button(
                        onClick = {
                            isConverting = true
                            conversionStatus = "Conversion et enregistrement dans la bibliothèque SLM..."
                            scope.launch {
                                if (conversionType == "VIDEO_TO_AUDIO") {
                                    val inputUri = selectedUris.firstOrNull() ?: Uri.fromFile(ProceduralAudioGenerator.getOrCreateAudioFile(context, "synthwave"))
                                    val result = MediaConverterUtil.convertToAudio(
                                        context = context,
                                        inputUri = inputUri,
                                        outputFormat = selectedOutputFormat,
                                        bitrateKbps = selectedBitrate,
                                        startMs = if (isTrimmingEnabled) (trimStartSec * 1000).toLong() else 0L,
                                        endMs = if (isTrimmingEnabled) (trimEndSec * 1000).toLong() else -1L,
                                        customTitle = customTitle.ifBlank { null }
                                    )
                                    if (result.success && result.track != null) {
                                        onTrackConverted(result.track)
                                        conversionStatus = "Conversion réussie !"
                                        onDismiss()
                                    } else {
                                        conversionStatus = "Erreur : ${result.errorMessage ?: "Échec de conversion"}"
                                    }
                                } else {
                                    val track = selectedTrackForMp4 ?: tracks.firstOrNull()
                                    if (track != null) {
                                        val result = MediaConverterUtil.convertAudioToMp4WithVisualizer(
                                            context = context,
                                            audioUri = Uri.parse(track.uriString),
                                            title = track.title,
                                            artist = track.artist,
                                            visualizerStyle = selectedVisualizerStyle
                                        )
                                        if (result.success && result.track != null) {
                                            onTrackConverted(result.track)
                                            conversionStatus = "Vidéo MP4 générée et enregistrée !"
                                            onDismiss()
                                        } else {
                                            conversionStatus = "Erreur : ${result.errorMessage ?: "Échec"}"
                                        }
                                    }
                                }
                                isConverting = false
                            }
                        },
                        enabled = !isConverting,
                        colors = ButtonDefaults.buttonColors(containerColor = AppleCrimson),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("convert_action_button")
                    ) {
                        if (isConverting) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Conversion en cours...", color = Color.White, fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Default.AutoMode, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Démarrer la conversion", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }
            }
        }
    }
}
