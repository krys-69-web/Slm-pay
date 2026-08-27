package com.example.slmplay.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.slmplay.data.db.TrackEntity
import com.example.ui.theme.*

@Composable
fun ArtworkStudioDialog(
    isOpen: Boolean,
    track: TrackEntity?,
    onDismiss: () -> Unit,
    onSaveMetadata: (id: String, title: String, artist: String, album: String, genre: String, year: Int, coverUri: String?) -> Unit,
    onRestoreOriginal: (id: String) -> Unit
) {
    if (!isOpen || track == null) return

    var title by remember(track) { mutableStateOf(track.title) }
    var artist by remember(track) { mutableStateOf(track.artist) }
    var album by remember(track) { mutableStateOf(track.album) }
    var genre by remember(track) { mutableStateOf(track.genre) }
    var yearStr by remember(track) { mutableStateOf(track.year.toString()) }
    var selectedCoverUri by remember(track) { mutableStateOf(track.coverUri) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedCoverUri = uri.toString()
        }
    }

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
                    .testTag("artwork_studio_dialog"),
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
                                Icon(Icons.Default.Palette, contentDescription = null, tint = AppleCrimson, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Artwork & Tag Studio",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Édition de pochette & métadonnées du morceau",
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
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        item {
                            // Cover Preview & Change Button
                            Box(
                                modifier = Modifier
                                    .size(160.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(DarkGlassCard)
                                    .border(2.dp, GlassBorder, RoundedCornerShape(20.dp))
                                    .clickable { imagePicker.launch("image/*") },
                                contentAlignment = Alignment.Center
                            ) {
                                if (selectedCoverUri != null) {
                                    AsyncImage(
                                        model = selectedCoverUri,
                                        contentDescription = "Pochette",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    val resId = when (track.coverResName) {
                                        "cover_neon" -> com.example.R.drawable.cover_neon
                                        "cover_ambient" -> com.example.R.drawable.cover_ambient
                                        else -> com.example.R.drawable.slm_logo
                                    }
                                    Image(
                                        painter = painterResource(id = resId),
                                        contentDescription = "Pochette par défaut",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }

                                // Overlay edit badge
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(8.dp)
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(AppleCrimson),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.PhotoCamera, contentDescription = "Changer la pochette", tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Toucher pour importer une nouvelle image", fontSize = 12.sp, color = TextTertiary)

                            Spacer(modifier = Modifier.height(20.dp))

                            // Form Fields
                            OutlinedTextField(
                                value = title,
                                onValueChange = { title = it },
                                label = { Text("Titre du morceau") },
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

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = artist,
                                onValueChange = { artist = it },
                                label = { Text("Artiste") },
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

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = album,
                                onValueChange = { album = it },
                                label = { Text("Album") },
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

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedTextField(
                                    value = genre,
                                    onValueChange = { genre = it },
                                    label = { Text("Genre") },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AppleCrimson,
                                        unfocusedBorderColor = GlassBorder,
                                        focusedLabelColor = AppleCrimson,
                                        unfocusedLabelColor = TextTertiary,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    ),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.weight(1f)
                                )

                                OutlinedTextField(
                                    value = yearStr,
                                    onValueChange = { yearStr = it },
                                    label = { Text("Année") },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AppleCrimson,
                                        unfocusedBorderColor = GlassBorder,
                                        focusedLabelColor = AppleCrimson,
                                        unfocusedLabelColor = TextTertiary,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    ),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.weight(0.7f)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Restore original metadata button
                            OutlinedButton(
                                onClick = {
                                    onRestoreOriginal(track.id)
                                    onDismiss()
                                },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = AppleCrimson),
                                border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(GlassBorder)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Restore, contentDescription = null, tint = AppleCrimson)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Restaurer les informations originales", color = AppleCrimson, fontSize = 13.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            val year = yearStr.toIntOrNull() ?: 2024
                            onSaveMetadata(track.id, title, artist, album, genre, year, selectedCoverUri)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AppleCrimson),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("save_artwork_studio_button")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Enregistrer les modifications", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
