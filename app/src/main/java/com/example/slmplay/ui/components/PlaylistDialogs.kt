package com.example.slmplay.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.slmplay.data.db.PlaylistEntity
import com.example.slmplay.data.db.TrackEntity
import com.example.ui.theme.AppleCrimson
import com.example.ui.theme.DarkGlassElevated
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassBorderActive
import com.example.ui.theme.NeonMeshGradients
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePlaylistDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onCreate: (name: String, description: String, gradientIndex: Int) -> Unit
) {
    if (!isOpen) return

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedGradient by remember { mutableIntStateOf(0) }

    BasicAlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.padding(24.dp)
    ) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("create_playlist_dialog"),
            shape = RoundedCornerShape(28.dp),
            backgroundColor = DarkGlassElevated,
            borderColor = GlassBorderActive,
            elevation = 24.dp
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Nouvelle Playlist",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fermer", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Titre de la playlist", color = TextSecondary) },
                    placeholder = { Text("Ex: Mes Vibes du Soir", color = TextSecondary.copy(alpha = 0.5f)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppleCrimson,
                        unfocusedBorderColor = GlassBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("playlist_name_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (optionnel)", color = TextSecondary) },
                    placeholder = { Text("Ex: Ambiance néon et relaxation", color = TextSecondary.copy(alpha = 0.5f)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppleCrimson,
                        unfocusedBorderColor = GlassBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("Thème Visuel Glass", fontSize = 13.sp, color = TextSecondary, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    NeonMeshGradients.forEachIndexed { index, colors ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(colors))
                                .border(
                                    width = if (selectedGradient == index) 3.dp else 1.dp,
                                    color = if (selectedGradient == index) Color.White else GlassBorder,
                                    shape = CircleShape
                                )
                                .clickable { selectedGradient = index },
                            contentAlignment = Alignment.Center
                        ) {
                            if (selectedGradient == index) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    GlassButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "Annuler",
                            color = TextSecondary,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    GlassButton(
                        onClick = { onCreate(name, description, selectedGradient) },
                        enabled = name.isNotBlank(),
                        shape = RoundedCornerShape(16.dp),
                        isPrimary = true,
                        modifier = Modifier.testTag("save_playlist_button")
                    ) {
                        Text(
                            text = "Créer",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddToPlaylistDialog(
    isOpen: Boolean,
    track: TrackEntity?,
    playlists: List<PlaylistEntity>,
    onDismiss: () -> Unit,
    onAddToPlaylist: (playlistId: String, trackId: String) -> Unit,
    onCreateNewPlaylistClick: () -> Unit
) {
    if (!isOpen || track == null) return

    BasicAlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.padding(24.dp)
    ) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("add_to_playlist_dialog"),
            shape = RoundedCornerShape(28.dp),
            backgroundColor = DarkGlassElevated,
            borderColor = GlassBorderActive,
            elevation = 24.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Ajouter à une playlist",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = track.title,
                            fontSize = 13.sp,
                            color = AppleCrimson
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fermer", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // New Playlist Button
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onDismiss()
                            onCreateNewPlaylistClick()
                        },
                    shape = RoundedCornerShape(16.dp),
                    backgroundColor = DarkSurface
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = AppleCrimson)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Créer une nouvelle playlist...", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                ) {
                    val customPlaylists = playlists.filter { !it.isSystem }
                    if (customPlaylists.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Aucune playlist personnalisée", color = TextSecondary, fontSize = 13.sp)
                            }
                        }
                    } else {
                        items(customPlaylists) { pl ->
                            val gradient = NeonMeshGradients.getOrElse(pl.gradientIndex % NeonMeshGradients.size) { NeonMeshGradients[0] }
                            GlassCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                shape = RoundedCornerShape(14.dp),
                                backgroundColor = DarkSurface,
                                onClick = { onAddToPlaylist(pl.id, track.id) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Brush.linearGradient(gradient)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.QueueMusic, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(pl.name, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                        if (pl.description.isNotBlank()) {
                                            Text(pl.description, color = TextSecondary, fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
