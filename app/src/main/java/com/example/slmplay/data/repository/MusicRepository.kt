package com.example.slmplay.data.repository

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.provider.OpenableColumns
import com.example.slmplay.data.db.MusicDao
import com.example.slmplay.data.db.PlaylistEntity
import com.example.slmplay.data.db.PlaylistTrackCrossRef
import com.example.slmplay.data.db.TrackEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.UUID

class MusicRepository(
    private val context: Context,
    private val musicDao: MusicDao
) {

    val allTracks: Flow<List<TrackEntity>> = musicDao.getAllTracks()
    val favoriteTracks: Flow<List<TrackEntity>> = musicDao.getFavoriteTracks()
    val allPlaylists: Flow<List<PlaylistEntity>> = musicDao.getAllPlaylists()

    fun getTracksForPlaylist(playlistId: String): Flow<List<TrackEntity>> =
        musicDao.getTracksForPlaylist(playlistId)

    fun getPlaylistTrackCount(playlistId: String): Flow<Int> =
        musicDao.getPlaylistTrackCount(playlistId)

    suspend fun initializeDefaultDataIfNeeded() = withContext(Dispatchers.IO) {
        val existingTracks = musicDao.getAllTracks().first()
        if (existingTracks.isEmpty()) {
            val demoTracks = listOf(
                TrackEntity(
                    id = "demo_synthwave_01",
                    title = "SLM Neon Dream",
                    artist = "Small Language Model AI",
                    album = "HOpE°pLaY Volume 1",
                    durationMs = 180000L,
                    uriString = "procedural://synthwave",
                    coverResName = "cover_neon",
                    isFavorite = true,
                    genre = "Synthwave / Cyberpunk",
                    isProcedural = true,
                    proceduralPreset = "synthwave",
                    year = 2024,
                    playCount = 14
                ),
                TrackEntity(
                    id = "demo_lofi_02",
                    title = "Apple Glass Horizon",
                    artist = "Vibe Synthesizer",
                    album = "Pure Translucency",
                    durationMs = 180000L,
                    uriString = "procedural://lofi",
                    coverResName = "cover_ambient",
                    isFavorite = false,
                    genre = "Lofi Melodic",
                    isProcedural = true,
                    proceduralPreset = "lofi",
                    year = 2023,
                    playCount = 9
                ),
                TrackEntity(
                    id = "demo_cyberpunk_03",
                    title = "Cyberpunk Pulse 2077",
                    artist = "SLM Core",
                    album = "Neon Grid Beats",
                    durationMs = 180000L,
                    uriString = "procedural://cyberpunk",
                    coverResName = "cover_neon",
                    isFavorite = true,
                    genre = "Cyberpunk Bass",
                    isProcedural = true,
                    proceduralPreset = "cyberpunk",
                    year = 2024,
                    playCount = 21
                ),
                TrackEntity(
                    id = "demo_ambient_04",
                    title = "HOpE Ambient Flow",
                    artist = "Acoustic AI Mind",
                    album = "HOpE°pLaY Relax",
                    durationMs = 180000L,
                    uriString = "procedural://ambient",
                    coverResName = "cover_ambient",
                    isFavorite = false,
                    genre = "Ambient Chill",
                    isProcedural = true,
                    proceduralPreset = "ambient",
                    year = 2022,
                    playCount = 5
                )
            )

            musicDao.insertTracks(demoTracks)

            // Setup default playlists
            val playlists = listOf(
                PlaylistEntity(
                    id = "playlist_all_music",
                    name = "Vos musiques",
                    description = "Toutes les pistes importées et détectées",
                    gradientIndex = 0,
                    isSystem = true
                ),
                PlaylistEntity(
                    id = "playlist_favorites",
                    name = "Coups de cœur",
                    description = "Vos morceaux préférés avec #ff2d55",
                    gradientIndex = 1,
                    isSystem = true
                ),
                PlaylistEntity(
                    id = "playlist_smart_favorites",
                    name = "Tes favoris intelligents",
                    description = "Sélection basée sur vos habitudes d'écoute SLM",
                    gradientIndex = 3,
                    isSystem = true
                ),
                PlaylistEntity(
                    id = "playlist_hope_vibes",
                    name = "Ambiance HOpE & Chill",
                    description = "Sélection relaxante générée par le SLM",
                    gradientIndex = 2,
                    isSystem = false
                )
            )

            playlists.forEach { musicDao.insertPlaylist(it) }

            // Add demo tracks to playlists
            demoTracks.forEachIndexed { index, track ->
                musicDao.addTrackToPlaylist(PlaylistTrackCrossRef("playlist_all_music", track.id, index))
                if (track.isFavorite) {
                    musicDao.addTrackToPlaylist(PlaylistTrackCrossRef("playlist_favorites", track.id, index))
                }
                if (track.playCount > 10 || track.isFavorite) {
                    musicDao.addTrackToPlaylist(PlaylistTrackCrossRef("playlist_smart_favorites", track.id, index))
                }
            }
            musicDao.addTrackToPlaylist(PlaylistTrackCrossRef("playlist_hope_vibes", "demo_lofi_02", 0))
            musicDao.addTrackToPlaylist(PlaylistTrackCrossRef("playlist_hope_vibes", "demo_ambient_04", 1))
        }
    }

    suspend fun scanDeviceAudioFiles(): Int = withContext(Dispatchers.IO) {
        val scannedTracks = mutableListOf<TrackEntity>()
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.YEAR
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        try {
            val cursor = context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                sortOrder
            )

            cursor?.use {
                val idCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val durCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val albumIdCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val yearCol = it.getColumnIndex(MediaStore.Audio.Media.YEAR)

                while (it.moveToNext()) {
                    val id = it.getLong(idCol)
                    val title = it.getString(titleCol) ?: "Piste inconnue"
                    val artist = it.getString(artistCol) ?: "Artiste inconnu"
                    val album = it.getString(albumCol) ?: "Album"
                    val duration = it.getLong(durCol)
                    val albumId = it.getLong(albumIdCol)
                    val year = if (yearCol != -1) it.getInt(yearCol).coerceAtLeast(1900) else 2024

                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        id
                    )

                    val albumArtUri = "content://media/external/audio/albumart/$albumId"

                    val track = TrackEntity(
                        id = "local_media_$id",
                        title = title,
                        artist = if (artist == "<unknown>") "Artiste inconnu" else artist,
                        album = album,
                        durationMs = duration,
                        uriString = contentUri.toString(),
                        coverUri = albumArtUri,
                        isFavorite = false,
                        isProcedural = false,
                        genre = "Local Audio",
                        year = year,
                        originalTitle = title,
                        originalArtist = artist,
                        originalAlbum = album,
                        originalCoverUri = albumArtUri
                    )
                    scannedTracks.add(track)
                }
            }

            if (scannedTracks.isNotEmpty()) {
                musicDao.insertTracks(scannedTracks)
                scannedTracks.forEachIndexed { idx, trk ->
                    musicDao.addTrackToPlaylist(
                        PlaylistTrackCrossRef("playlist_all_music", trk.id, idx)
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        scannedTracks.size
    }

    suspend fun importAudioUris(uris: List<Uri>): Int = withContext(Dispatchers.IO) {
        val imported = mutableListOf<TrackEntity>()
        val resolver: ContentResolver = context.contentResolver

        uris.forEach { uri ->
            try {
                var displayName = "Morceau ${imported.size + 1}"
                var fileSize = 0L

                resolver.query(uri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                        if (nameIndex != -1) displayName = cursor.getString(nameIndex) ?: displayName
                        if (sizeIndex != -1) fileSize = cursor.getLong(sizeIndex)
                    }
                }

                val cleanTitle = displayName.substringBeforeLast(".")

                val track = TrackEntity(
                    id = "import_" + UUID.randomUUID().toString(),
                    title = cleanTitle,
                    artist = "Fichier importé",
                    album = "Importations SLM",
                    durationMs = 210000L,
                    uriString = uri.toString(),
                    isFavorite = false,
                    isProcedural = false,
                    genre = "Import local",
                    originalTitle = cleanTitle,
                    originalArtist = "Fichier importé",
                    originalAlbum = "Importations SLM"
                )
                imported.add(track)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if (imported.isNotEmpty()) {
            musicDao.insertTracks(imported)
            imported.forEachIndexed { idx, trk ->
                musicDao.addTrackToPlaylist(
                    PlaylistTrackCrossRef("playlist_all_music", trk.id, idx)
                )
            }
        }

        imported.size
    }

    suspend fun insertCustomTrack(track: TrackEntity) = withContext(Dispatchers.IO) {
        musicDao.insertTrack(track)
        musicDao.addTrackToPlaylist(PlaylistTrackCrossRef("playlist_all_music", track.id))
    }

    suspend fun updateTrackMetadata(
        id: String,
        title: String,
        artist: String,
        album: String,
        genre: String,
        year: Int,
        coverUri: String?
    ) = withContext(Dispatchers.IO) {
        musicDao.updateTrackMetadata(id, title, artist, album, genre, year, coverUri)
    }

    suspend fun restoreTrackMetadata(id: String) = withContext(Dispatchers.IO) {
        musicDao.restoreTrackMetadata(id)
    }

    suspend fun recordPlay(id: String) = withContext(Dispatchers.IO) {
        musicDao.recordPlay(id)
        // Check if track qualifies for smart favorites playlist
        val track = musicDao.getTrackById(id)
        if (track != null && (track.playCount >= 3 || track.isFavorite)) {
            musicDao.addTrackToPlaylist(PlaylistTrackCrossRef("playlist_smart_favorites", track.id))
        }
    }

    suspend fun toggleFavorite(trackId: String, currentStatus: Boolean) = withContext(Dispatchers.IO) {
        val newStatus = !currentStatus
        musicDao.updateFavorite(trackId, newStatus)
        if (newStatus) {
            musicDao.addTrackToPlaylist(PlaylistTrackCrossRef("playlist_favorites", trackId))
            musicDao.addTrackToPlaylist(PlaylistTrackCrossRef("playlist_smart_favorites", trackId))
        } else {
            musicDao.removeTrackFromPlaylist("playlist_favorites", trackId)
        }
    }

    suspend fun createPlaylist(name: String, description: String, gradientIndex: Int): String = withContext(Dispatchers.IO) {
        val id = "playlist_" + UUID.randomUUID().toString()
        val playlist = PlaylistEntity(
            id = id,
            name = name,
            description = description,
            gradientIndex = gradientIndex,
            isSystem = false
        )
        musicDao.insertPlaylist(playlist)
        id
    }

    suspend fun deletePlaylist(playlistId: String) = withContext(Dispatchers.IO) {
        musicDao.deletePlaylist(playlistId)
    }

    suspend fun addTrackToPlaylist(playlistId: String, trackId: String) = withContext(Dispatchers.IO) {
        val existing = musicDao.getTrackIdsInPlaylist(playlistId).toSet()
        if (trackId !in existing) {
            musicDao.addTrackToPlaylist(PlaylistTrackCrossRef(playlistId, trackId, position = existing.size))
        }
    }

    suspend fun addTracksToPlaylist(playlistId: String, trackIds: List<String>): Int = withContext(Dispatchers.IO) {
        val existing = musicDao.getTrackIdsInPlaylist(playlistId).toSet()
        val toAdd = trackIds.filter { it !in existing }
        if (toAdd.isNotEmpty()) {
            val crossRefs = toAdd.mapIndexed { idx, trackId ->
                PlaylistTrackCrossRef(playlistId = playlistId, trackId = trackId, position = existing.size + idx)
            }
            musicDao.addTracksToPlaylist(crossRefs)
        }
        toAdd.size
    }

    suspend fun removeTrackFromPlaylist(playlistId: String, trackId: String) = withContext(Dispatchers.IO) {
        musicDao.removeTrackFromPlaylist(playlistId, trackId)
    }

    suspend fun removeTracksFromPlaylist(playlistId: String, trackIds: List<String>) = withContext(Dispatchers.IO) {
        if (trackIds.isNotEmpty()) {
            musicDao.removeTracksFromPlaylist(playlistId, trackIds)
        }
    }

    suspend fun deleteTrack(trackId: String) = withContext(Dispatchers.IO) {
        musicDao.deleteTrack(trackId)
    }
}
