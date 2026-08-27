package com.example.slmplay.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.R
import com.example.slmplay.data.db.PlaylistEntity
import com.example.slmplay.data.db.TrackEntity
import com.example.slmplay.ui.components.GlassButton
import com.example.slmplay.ui.components.GlassCard
import com.example.ui.theme.*

@Composable
fun PlaylistDetailScreen(
    playlist: PlaylistEntity,
    tracks: List<TrackEntity>,
    allTracks: List<TrackEntity>,
    currentTrack: TrackEntity?,
    isPlaying: Boolean,
    onBack: () -> Unit,
    onPlayTrack: (TrackEntity, List<TrackEntity>, Int) -> Unit,
    onPlayAll: () -> Unit,
    onShuffleAll: () -> Unit,
    onToggleFavorite: (TrackEntity) -> Unit,
    onRemoveTrack: (playlistId: String, trackId: String) -> Unit,
    onRemoveMultipleTracks: (playlistId: String, trackIds: List<String>) -> Unit,
    onAddTracksToPlaylist: (playlistId: String, trackIds: List<String>) -> Unit,
    onDeletePlaylist: (PlaylistEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val gradient = NeonMeshGradients.getOrElse(playlist.gradientIndex % NeonMeshGradients.size) { NeonMeshGradients[0] }
    var showMenu by remember { mutableStateOf(false) }
    var isMultiSelectMode by remember { mutableStateOf(false) }
    val selectedTrackIds = remember { mutableStateListOf<String>() }
    var showAddSongsDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 16.dp)
        ) {
            // Top navigation bar
            item {
                if (isMultiSelectMode) {
                    // Multi-select Active Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = {
                                isMultiSelectMode = false
                                selectedTrackIds.clear()
                            }
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Annuler", tint = TextPrimary)
                        }

                        Text(
                            text = "${selectedTrackIds.size} sélectionné(s)",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppleCrimson
                        )

                        Row {
                            TextButton(
                                onClick = {
                                    if (selectedTrackIds.size == tracks.size) {
                                        selectedTrackIds.clear()
                                    } else {
                                        selectedTrackIds.clear()
                                        selectedTrackIds.addAll(tracks.map { it.id })
                                    }
                                }
                            ) {
                                Text(
                                    text = if (selectedTrackIds.size == tracks.size) "Désélectionner" else "Tout",
                                    color = AppleCrimson,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                } else {
                    // Standard Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.testTag("playlist_detail_back_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Retour",
                                tint = TextPrimary
                            )
                        }

                        Text(
                            text = "Playlist",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Box {
                            IconButton(
                                onClick = { showMenu = true },
                                modifier = Modifier.testTag("playlist_menu_more_btn")
                            ) {
                                Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = TextPrimary)
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false },
                                modifier = Modifier.background(DarkSurface)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Ajouter des chansons", color = TextPrimary) },
                                    onClick = {
                                        showMenu = false
                                        showAddSongsDialog = true
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Add, contentDescription = null, tint = AppleCrimson)
                                    }
                                )

                                if (tracks.isNotEmpty()) {
                                    DropdownMenuItem(
                                        text = { Text("Sélection multiple", color = TextPrimary) },
                                        onClick = {
                                            showMenu = false
                                            isMultiSelectMode = true
                                            selectedTrackIds.clear()
                                        },
                                        leadingIcon = {
                                            Icon(Icons.Default.Checklist, contentDescription = null, tint = ApplePurple)
                                        }
                                    )
                                }

                                if (!playlist.isSystem) {
                                    DropdownMenuItem(
                                        text = { Text("Supprimer la playlist", color = AppleCrimson) },
                                        onClick = {
                                            showMenu = false
                                            onDeletePlaylist(playlist)
                                        },
                                        leadingIcon = {
                                            Icon(Icons.Default.Delete, contentDescription = null, tint = AppleCrimson)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Header Artwork Card
            item {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(24.dp),
                    backgroundColor = DarkGlassElevated
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(130.dp)
                                .shadow(16.dp, RoundedCornerShape(20.dp), spotColor = AppleCrimson.copy(alpha = 0.5f))
                                .clip(RoundedCornerShape(20.dp))
                                .background(Brush.linearGradient(gradient)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.QueueMusic,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(54.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = playlist.name,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = playlist.description,
                            fontSize = 13.sp,
                            color = TextSecondary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "${tracks.size} morceau${if (tracks.size > 1) "x" else ""}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AppleCrimson
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Actions Row: Play All, Shuffle & Add
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            GlassButton(
                                onClick = onPlayAll,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("playlist_play_all_btn"),
                                isPrimary = true
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Lire tout", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }

                            GlassButton(
                                onClick = onShuffleAll,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("playlist_shuffle_btn")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.Default.Shuffle, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Aléatoire", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                }
                            }

                            IconButton(
                                onClick = { showAddSongsDialog = true },
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(AppleCrimson.copy(alpha = 0.2f))
                                    .testTag("playlist_add_songs_fab")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Ajouter", tint = AppleCrimson, modifier = Modifier.size(22.dp))
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "MORCEAUX (${tracks.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextTertiary,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                    )

                    if (!isMultiSelectMode && tracks.isNotEmpty()) {
                        TextButton(
                            onClick = {
                                isMultiSelectMode = true
                                selectedTrackIds.clear()
                            }
                        ) {
                            Text("Sélectionner", color = TextSecondary, fontSize = 12.sp)
                        }
                    }
                }
            }

            if (tracks.isEmpty()) {
                item {
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        shape = RoundedCornerShape(20.dp),
                        backgroundColor = DarkGlassCard
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.MusicNote, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Aucun morceau dans cette playlist", color = TextSecondary, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            GlassButton(
                                onClick = { showAddSongsDialog = true },
                                isPrimary = true
                            ) {
                                Text("Ajouter des chansons", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                itemsIndexed(tracks, key = { _, trk -> trk.id }) { index, track ->
                    val isCurrentPlaying = currentTrack?.id == track.id
                    val isSelected = selectedTrackIds.contains(track.id)
                    var trackMenuOpen by remember { mutableStateOf(false) }

                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(16.dp),
                        backgroundColor = if (isSelected) AppleCrimson.copy(alpha = 0.25f) else if (isCurrentPlaying) AppleCrimson.copy(alpha = 0.18f) else DarkGlassCard,
                        borderColor = if (isSelected || isCurrentPlaying) AppleCrimson else GlassBorder,
                        onClick = {
                            if (isMultiSelectMode) {
                                if (isSelected) {
                                    selectedTrackIds.remove(track.id)
                                } else {
                                    selectedTrackIds.add(track.id)
                                }
                            } else {
                                onPlayTrack(track, tracks, index)
                            }
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isMultiSelectMode) {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = { checked ->
                                        if (checked) selectedTrackIds.add(track.id) else selectedTrackIds.remove(track.id)
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = AppleCrimson,
                                        uncheckedColor = TextSecondary,
                                        checkmarkColor = Color.White
                                    ),
                                    modifier = Modifier.padding(end = 6.dp)
                                )
                            } else {
                                // Index or Playing pulse
                                Text(
                                    text = if (isCurrentPlaying && isPlaying) "▶" else "${index + 1}",
                                    color = if (isCurrentPlaying) AppleCrimson else TextTertiary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.width(24.dp)
                                )
                            }

                            // Artwork thumbnail
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF222233))
                            ) {
                                if (track.coverUri != null) {
                                    AsyncImage(
                                        model = track.coverUri,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.size(44.dp)
                                    )
                                } else {
                                    val resId = when (track.coverResName) {
                                        "cover_neon" -> R.drawable.cover_neon
                                        "cover_ambient" -> R.drawable.cover_ambient
                                        else -> R.drawable.slm_logo
                                    }
                                    Image(
                                        painter = painterResource(id = resId),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.size(44.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Title & Artist
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = track.title,
                                    color = if (isCurrentPlaying) AppleCrimson else TextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${track.artist} • ${track.genre}",
                                    color = TextSecondary,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            if (!isMultiSelectMode) {
                                // Favorite Icon
                                IconButton(
                                    onClick = { onToggleFavorite(track) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = if (track.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = "Favori",
                                        tint = if (track.isFavorite) AppleCrimson else TextSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                // More menu
                                Box {
                                    IconButton(
                                        onClick = { trackMenuOpen = true },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = TextSecondary, modifier = Modifier.size(20.dp))
                                    }

                                    DropdownMenu(
                                        expanded = trackMenuOpen,
                                        onDismissRequest = { trackMenuOpen = false },
                                        modifier = Modifier.background(DarkSurface)
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Retirer de la playlist", color = AppleCrimson) },
                                            onClick = {
                                                trackMenuOpen = false
                                                onRemoveTrack(playlist.id, track.id)
                                            },
                                            leadingIcon = {
                                                Icon(Icons.Default.Delete, contentDescription = null, tint = AppleCrimson)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(140.dp))
            }
        }

        // Multi-select Bottom Action Bar
        if (isMultiSelectMode && selectedTrackIds.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp)
            ) {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    backgroundColor = DarkSurface,
                    borderColor = AppleCrimson
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${selectedTrackIds.size} sélectionné(s)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        GlassButton(
                            onClick = {
                                onRemoveMultipleTracks(playlist.id, selectedTrackIds.toList())
                                isMultiSelectMode = false
                                selectedTrackIds.clear()
                            },
                            shape = RoundedCornerShape(14.dp),
                            isPrimary = true
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Retirer (${selectedTrackIds.size})", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    // ================= ADD SONGS DIALOG (MULTI-SELECT WITH ANTI-DUPLICATE) =================
    if (showAddSongsDialog) {
        val existingTrackIds = remember(tracks) { tracks.map { it.id }.toSet() }
        var searchQuery by remember { mutableStateOf("") }
        val newlySelectedIds = remember { mutableStateListOf<String>() }

        val availableTracks = remember(allTracks, searchQuery) {
            if (searchQuery.isBlank()) {
                allTracks
            } else {
                allTracks.filter {
                    it.title.contains(searchQuery, ignoreCase = true) ||
                    it.artist.contains(searchQuery, ignoreCase = true) ||
                    it.album.contains(searchQuery, ignoreCase = true)
                }
            }
        }

        Dialog(
            onDismissRequest = { showAddSongsDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.94f)
                    .fillMaxHeight(0.85f)
                    .clip(RoundedCornerShape(28.dp))
                    .background(DarkCanvas)
                    .border(1.dp, GlassBorder, RoundedCornerShape(28.dp))
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
                        Text(
                            text = "Ajouter à « ${playlist.name} »",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        IconButton(onClick = { showAddSongsDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Fermer", tint = TextSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Search input
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Rechercher dans la bibliothèque...", color = TextSecondary, fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = DarkGlassCard,
                            unfocusedContainerColor = DarkGlassCard,
                            focusedBorderColor = AppleCrimson,
                            unfocusedBorderColor = GlassBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // List of songs with anti-duplicate status
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        items(availableTracks, key = { it.id }) { trk ->
                            val alreadyInPlaylist = existingTrackIds.contains(trk.id)
                            val isChecked = newlySelectedIds.contains(trk.id)

                            GlassCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                shape = RoundedCornerShape(14.dp),
                                backgroundColor = if (isChecked) AppleCrimson.copy(alpha = 0.25f) else DarkGlassCard,
                                borderColor = if (isChecked) AppleCrimson else GlassBorder,
                                onClick = {
                                    if (!alreadyInPlaylist) {
                                        if (isChecked) newlySelectedIds.remove(trk.id) else newlySelectedIds.add(trk.id)
                                    }
                                }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (alreadyInPlaylist) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(GlassBorder),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(14.dp))
                                        }
                                    } else {
                                        Checkbox(
                                            checked = isChecked,
                                            onCheckedChange = { checked ->
                                                if (checked) newlySelectedIds.add(trk.id) else newlySelectedIds.remove(trk.id)
                                            },
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = AppleCrimson,
                                                uncheckedColor = TextSecondary,
                                                checkmarkColor = Color.White
                                            )
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = trk.title,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (alreadyInPlaylist) TextTertiary else TextPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${trk.artist} • ${trk.genre}",
                                            fontSize = 11.sp,
                                            color = if (alreadyInPlaylist) TextTertiary else TextSecondary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    if (alreadyInPlaylist) {
                                        Text(
                                            text = "Déjà présent",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = TextTertiary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Validation Button
                    GlassButton(
                        onClick = {
                            if (newlySelectedIds.isNotEmpty()) {
                                onAddTracksToPlaylist(playlist.id, newlySelectedIds.toList())
                            }
                            showAddSongsDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        isPrimary = true,
                        shape = RoundedCornerShape(16.dp),
                        enabled = newlySelectedIds.isNotEmpty()
                    ) {
                        Text(
                            text = if (newlySelectedIds.isEmpty()) "Sélectionnez des morceaux" else "Ajouter ${newlySelectedIds.size} morceau(x)",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}
