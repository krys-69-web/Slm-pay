package com.example.slmplay.ui.viewmodel

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.IBinder
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.slmplay.data.db.MusicDatabase
import com.example.slmplay.data.db.PlaylistEntity
import com.example.slmplay.data.db.TrackEntity
import com.example.slmplay.data.model.*
import com.example.slmplay.data.repository.MusicRepository
import com.example.slmplay.service.MusicPlaybackService
import com.example.slmplay.utils.DynamicArtworkExtractor
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MusicViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MusicRepository
    private var playbackService: MusicPlaybackService? = null
    private var isBound = false

    // App Global Settings for all 13 features
    private val _appSettings = MutableStateFlow(AppSettings())
    val appSettings = _appSettings.asStateFlow()

    // User Profile & Authentication State
    private val prefs = application.getSharedPreferences("slm_play_user_profile", Context.MODE_PRIVATE)
    private val _userProfile = MutableStateFlow(
        UserProfile(
            isLoggedIn = prefs.getBoolean("is_logged_in", false),
            username = prefs.getString("username", "Artiste SLM") ?: "Artiste SLM",
            emailOrId = prefs.getString("email", "") ?: "",
            avatarUri = prefs.getString("avatar_uri", null)
        )
    )
    val userProfile = _userProfile.asStateFlow()

    private val _isProfileAuthDialogOpen = MutableStateFlow(false)
    val isProfileAuthDialogOpen = _isProfileAuthDialogOpen.asStateFlow()

    // Dialog & Tool Screens visibility
    private val _isSettingsOpen = MutableStateFlow(false)
    val isSettingsOpen = _isSettingsOpen.asStateFlow()

    private val _isMediaConverterOpen = MutableStateFlow(false)
    val isMediaConverterOpen = _isMediaConverterOpen.asStateFlow()

    private val _isAudioEditorOpen = MutableStateFlow(false)
    val isAudioEditorOpen = _isAudioEditorOpen.asStateFlow()

    private val _isEqualizerOpen = MutableStateFlow(false)
    val isEqualizerOpen = _isEqualizerOpen.asStateFlow()

    private val _isArtworkStudioOpen = MutableStateFlow(false)
    val isArtworkStudioOpen = _isArtworkStudioOpen.asStateFlow()

    private val _isFullscreenVisualizerOpen = MutableStateFlow(false)
    val isFullscreenVisualizerOpen = _isFullscreenVisualizerOpen.asStateFlow()

    private val _editingTrackForStudio = MutableStateFlow<TrackEntity?>(null)
    val editingTrackForStudio = _editingTrackForStudio.asStateFlow()

    // UI & Navigation State
    private val _isFullPlayerOpen = MutableStateFlow(false)
    val isFullPlayerOpen = _isFullPlayerOpen.asStateFlow()

    private val _isLyricsOpen = MutableStateFlow(false)
    val isLyricsOpen = _isLyricsOpen.asStateFlow()

    private val _isCreatePlaylistDialogOpen = MutableStateFlow(false)
    val isCreatePlaylistDialogOpen = _isCreatePlaylistDialogOpen.asStateFlow()

    private val _isAddToPlaylistDialogOpen = MutableStateFlow(false)
    val isAddToPlaylistDialogOpen = _isAddToPlaylistDialogOpen.asStateFlow()

    private val _trackForPlaylistAction = MutableStateFlow<TrackEntity?>(null)
    val trackForPlaylistAction = _trackForPlaylistAction.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _backgroundMode = MutableStateFlow(BackgroundMode.DYNAMIC_MESH)
    val backgroundMode = _backgroundMode.asStateFlow()

    private val _repeatMode = MutableStateFlow(RepeatMode.OFF)
    val repeatMode = _repeatMode.asStateFlow()

    private val _isShuffle = MutableStateFlow(false)
    val isShuffle = _isShuffle.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed = _playbackSpeed.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage = _statusMessage.asStateFlow()

    private val _activeVibe = MutableStateFlow<SlmVibe?>(null)
    val activeVibe = _activeVibe.asStateFlow()

    // Real-time audio amplitudes for reactive visualizer
    private val _audioAmplitudes = MutableStateFlow(FloatArray(16) { 0.25f })
    val audioAmplitudes = _audioAmplitudes.asStateFlow()

    // Dynamic Artwork Palette
    private val _dynamicArtworkPalette = MutableStateFlow(
        DynamicArtworkExtractor.ArtworkPalette(
            dominantColor = Color(0xFFFF2D55),
            accentColor = Color(0xFFAF52DE),
            surfaceColor = Color(0xFF12050A),
            isDark = true
        )
    )
    val dynamicArtworkPalette = _dynamicArtworkPalette.asStateFlow()

    // Service bound state mirrors
    val currentTrack = MutableStateFlow<TrackEntity?>(null)
    val isPlaying = MutableStateFlow(false)
    val currentPositionMs = MutableStateFlow(0L)
    val durationMs = MutableStateFlow(180000L)
    val isBuffering = MutableStateFlow(false)

    // Repository Flows
    val allTracks: StateFlow<List<TrackEntity>>
    val favoriteTracks: StateFlow<List<TrackEntity>>
    val allPlaylists: StateFlow<List<PlaylistEntity>>

    val filteredTracks: StateFlow<List<TrackEntity>>

    private val _selectedPlaylist = MutableStateFlow<PlaylistEntity?>(null)
    val selectedPlaylist = _selectedPlaylist.asStateFlow()

    private val _selectedPlaylistTracks = MutableStateFlow<List<TrackEntity>>(emptyList())
    val selectedPlaylistTracks = _selectedPlaylistTracks.asStateFlow()

    val presetVibes = listOf(
        SlmVibe(
            id = "vibe_cyberpunk",
            title = "Cyberpunk Energy",
            subtitle = "Bassline percutante & néon rouge #ff2d55",
            emoji = "⚡",
            promptContext = "Énergie brute, synthétiseurs agressifs, idéal pour séance de sport ou concentration intense.",
            gradientColors = listOf(0xFFFF2D55, 0xFFAF52DE, 0xFF007AFF),
            targetBpmRange = "120 - 135 BPM"
        ),
        SlmVibe(
            id = "vibe_lofi",
            title = "Apple Glass Lofi",
            subtitle = "Détente acoustique et mélodies feutrées",
            emoji = "☕",
            promptContext = "Accords chauds, rythme décontracté, parfait pour étudier, coder et se relaxer.",
            gradientColors = listOf(0xFF5856D6, 0xFFFF2D55, 0xFFFF9500),
            targetBpmRange = "70 - 85 BPM"
        ),
        SlmVibe(
            id = "vibe_ambient",
            title = "HOpE Immersion 3D",
            subtitle = "Nappes spatiales & évasion sonore",
            emoji = "🌌",
            promptContext = "Sons éthérés, flou acoustique, sensation de lévitation et méditation profonde.",
            gradientColors = listOf(0xFF00C7BE, 0xFF30B0C7, 0xFFAF52DE),
            targetBpmRange = "60 - 75 BPM"
        ),
        SlmVibe(
            id = "vibe_sunset",
            title = "Sunset Drive",
            subtitle = "Groove rétro-futuriste & mélancolie",
            emoji = "🌇",
            promptContext = "Ambiance route de nuit sous les néons, basses soyeuses et riffs analogiques.",
            gradientColors = listOf(0xFFFF375F, 0xFFFF9500, 0xFFFF2D55),
            targetBpmRange = "105 - 118 BPM"
        )
    )

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as? MusicPlaybackService.MusicBinder
            playbackService = binder?.getService()
            isBound = true

            // Bind StateFlows
            playbackService?.let { s ->
                viewModelScope.launch {
                    s.currentTrack.collect { trk ->
                        currentTrack.value = trk
                        if (trk != null) {
                            repository.recordPlay(trk.id)
                            if (_appSettings.value.isDynamicArtworkEnabled) {
                                val palette = DynamicArtworkExtractor.extractPalette(
                                    getApplication(),
                                    trk.coverUri,
                                    trk.coverResName
                                )
                                _dynamicArtworkPalette.value = palette
                            }
                        }
                    }
                }
                viewModelScope.launch {
                    s.isPlaying.collect { isPlaying.value = it }
                }
                viewModelScope.launch {
                    s.currentPosition.collect { currentPositionMs.value = it }
                }
                viewModelScope.launch {
                    s.duration.collect { durationMs.value = it }
                }
                viewModelScope.launch {
                    s.isBuffering.collect { isBuffering.value = it }
                }
                viewModelScope.launch {
                    s.audioAmplitudes.collect { _audioAmplitudes.value = it }
                }
                viewModelScope.launch {
                    s.sleepTimerSecondsLeft.collect { sec ->
                        _appSettings.value = _appSettings.value.copy(
                            isSleepTimerActive = sec > 0,
                            sleepTimerRemainingSeconds = sec
                        )
                    }
                }

                // Sync initial settings to service
                applySettingsToService(_appSettings.value)
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            playbackService = null
            isBound = false
        }
    }

    init {
        val database = MusicDatabase.getDatabase(application)
        repository = MusicRepository(application, database.musicDao())

        allTracks = repository.allTracks.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        favoriteTracks = repository.favoriteTracks.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        allPlaylists = repository.allPlaylists.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        filteredTracks = combine(
            allTracks,
            _searchQuery,
            _appSettings
        ) { tracks, query, settings ->
            var list = if (query.isBlank()) {
                tracks
            } else {
                tracks.filter {
                    it.title.contains(query, ignoreCase = true) ||
                            it.artist.contains(query, ignoreCase = true) ||
                            it.album.contains(query, ignoreCase = true) ||
                            it.genre.contains(query, ignoreCase = true)
                }
            }

            // Apply Smart Library sorting
            if (settings.isSmartLibraryEnabled) {
                list = when (settings.librarySortOption) {
                    LibrarySortOption.ADDED_RECENT -> list.sortedByDescending { it.addedDate }
                    LibrarySortOption.TITLE_AZ -> list.sortedBy { it.title.lowercase() }
                    LibrarySortOption.ARTIST -> list.sortedBy { it.artist.lowercase() }
                    LibrarySortOption.ALBUM -> list.sortedBy { it.album.lowercase() }
                    LibrarySortOption.GENRE -> list.sortedBy { it.genre.lowercase() }
                    LibrarySortOption.YEAR -> list.sortedByDescending { it.year }
                    LibrarySortOption.DURATION -> list.sortedByDescending { it.durationMs }
                    LibrarySortOption.FAVORITES -> list.sortedByDescending { it.isFavorite }
                    LibrarySortOption.RECENTLY_PLAYED -> list.sortedByDescending { it.lastPlayedDate }
                }
            }

            list
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        // Preload default demo tracks and playlists
        viewModelScope.launch {
            repository.initializeDefaultDataIfNeeded()
        }

        bindPlaybackService()
    }

    private fun bindPlaybackService() {
        val context = getApplication<Application>()
        val intent = Intent(context, MusicPlaybackService::class.java)
        context.startService(intent)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    // Settings Updates
    fun updateSettings(newSettings: AppSettings) {
        _appSettings.value = newSettings
        applySettingsToService(newSettings)
    }

    private fun applySettingsToService(settings: AppSettings) {
        playbackService?.let { s ->
            s.setSmartShuffle(settings.isSmartShuffleEnabled)
            s.setGapless(settings.isGaplessPlaybackEnabled)
            s.setCrossfade(settings.isCrossfadeEnabled, settings.crossfadeDurationSeconds)
            val effectMgr = s.getAudioEffectManager()
            effectMgr.setEqualizerEnabled(settings.isEqualizerEnabled)
            if (settings.isEqualizerEnabled) {
                if (settings.selectedEqPreset == "Manuel") {
                    settings.manualBands.forEachIndexed { i, gain ->
                        effectMgr.setManualBand(i, gain)
                    }
                } else {
                    effectMgr.applyPreset(settings.selectedEqPreset)
                }
                effectMgr.setBassBoost(settings.bassBoostStrength)
                effectMgr.setVirtualizer(settings.virtualizerStrength)
            }
        }
    }

    // Settings Dialog Navigation
    fun openSettings() { _isSettingsOpen.value = true }
    fun closeSettings() { _isSettingsOpen.value = false }

    fun openMediaConverter() { _isMediaConverterOpen.value = true }
    fun closeMediaConverter() { _isMediaConverterOpen.value = false }

    fun openAudioEditor(track: TrackEntity? = null) {
        _editingTrackForStudio.value = track ?: currentTrack.value ?: allTracks.value.firstOrNull()
        _isAudioEditorOpen.value = true
    }
    fun closeAudioEditor() { _isAudioEditorOpen.value = false }

    fun openEqualizer() { _isEqualizerOpen.value = true }
    fun closeEqualizer() { _isEqualizerOpen.value = false }

    fun openArtworkStudio(track: TrackEntity? = null) {
        _editingTrackForStudio.value = track ?: currentTrack.value ?: allTracks.value.firstOrNull()
        _isArtworkStudioOpen.value = true
    }
    fun closeArtworkStudio() { _isArtworkStudioOpen.value = false }

    fun openFullscreenVisualizer() { _isFullscreenVisualizerOpen.value = true }
    fun closeFullscreenVisualizer() { _isFullscreenVisualizerOpen.value = false }

    // Equalizer controls
    fun setEqualizerPreset(preset: String) {
        val updated = _appSettings.value.copy(selectedEqPreset = preset)
        updateSettings(updated)
    }

    fun setEqualizerBand(bandIndex: Int, gainDb: Float) {
        val bands = _appSettings.value.manualBands.toMutableList()
        if (bandIndex in bands.indices) {
            bands[bandIndex] = gainDb
            val updated = _appSettings.value.copy(
                manualBands = bands,
                selectedEqPreset = "Manuel"
            )
            updateSettings(updated)
        }
    }

    fun setBassBoost(strength: Float) {
        val updated = _appSettings.value.copy(bassBoostStrength = strength)
        updateSettings(updated)
    }

    fun setVirtualizer(strength: Float) {
        val updated = _appSettings.value.copy(virtualizerStrength = strength)
        updateSettings(updated)
    }

    fun setVisualizerType(type: VisualizerType) {
        val updated = _appSettings.value.copy(visualizerType = type)
        updateSettings(updated)
    }

    fun setLibrarySortOption(option: LibrarySortOption) {
        val updated = _appSettings.value.copy(librarySortOption = option)
        updateSettings(updated)
    }

    // Sleep Timer
    fun startSleepTimer(minutes: Int, endOnTrack: Boolean = false) {
        playbackService?.startSleepTimer(minutes, endOnTrack)
        _statusMessage.value = if (endOnTrack) "Minuteur : Arrêt à la fin du morceau" else "Minuteur activé : $minutes minutes"
    }

    fun cancelSleepTimer() {
        playbackService?.cancelSleepTimer()
        _appSettings.value = _appSettings.value.copy(isSleepTimerActive = false, sleepTimerRemainingSeconds = 0L)
        _statusMessage.value = "Minuteur de sommeil annulé"
    }

    // Custom Track actions
    fun addConvertedTrack(track: TrackEntity) {
        viewModelScope.launch {
            repository.insertCustomTrack(track)
            _statusMessage.value = "« ${track.title} » ajoutée à la bibliothèque"
        }
    }

    fun saveTrackMetadata(
        id: String,
        title: String,
        artist: String,
        album: String,
        genre: String,
        year: Int,
        coverUri: String?
    ) {
        viewModelScope.launch {
            repository.updateTrackMetadata(id, title, artist, album, genre, year, coverUri)
            _statusMessage.value = "Métadonnées mises à jour avec succès"
        }
    }

    fun restoreTrackMetadata(id: String) {
        viewModelScope.launch {
            repository.restoreTrackMetadata(id)
            _statusMessage.value = "Informations originales restaurées"
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun playTrack(track: TrackEntity, queue: List<TrackEntity> = allTracks.value, index: Int = 0) {
        playbackService?.playTrack(track, queue, index)
    }

    fun togglePlayPause() {
        playbackService?.togglePlayPause()
    }

    fun seekTo(positionMs: Long) {
        playbackService?.seekTo(positionMs)
    }

    fun skipToNext() {
        playbackService?.skipToNext()
    }

    fun skipToPrevious() {
        playbackService?.skipToPrevious()
    }

    fun toggleRepeatMode() {
        val next = when (_repeatMode.value) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        _repeatMode.value = next
        playbackService?.setRepeatMode(next)
    }

    fun toggleShuffle() {
        val next = !_isShuffle.value
        _isShuffle.value = next
        playbackService?.setShuffle(next)
    }

    fun setPlaybackSpeed(speed: Float) {
        _playbackSpeed.value = speed
        playbackService?.setPlaybackSpeed(speed)
    }

    fun setBackgroundMode(mode: BackgroundMode) {
        _backgroundMode.value = mode
    }

    fun toggleFavorite(track: TrackEntity) {
        viewModelScope.launch {
            repository.toggleFavorite(track.id, track.isFavorite)
        }
    }

    fun openFullPlayer() {
        _isFullPlayerOpen.value = true
    }

    fun closeFullPlayer() {
        _isFullPlayerOpen.value = false
    }

    fun toggleLyrics() {
        _isLyricsOpen.value = !_isLyricsOpen.value
    }

    fun openCreatePlaylistDialog() {
        _isCreatePlaylistDialogOpen.value = true
    }

    fun closeCreatePlaylistDialog() {
        _isCreatePlaylistDialogOpen.value = false
    }

    fun openAddToPlaylistDialog(track: TrackEntity) {
        _trackForPlaylistAction.value = track
        _isAddToPlaylistDialogOpen.value = true
    }

    fun closeAddToPlaylistDialog() {
        _isAddToPlaylistDialogOpen.value = false
        _trackForPlaylistAction.value = null
    }

    fun selectPlaylist(playlist: PlaylistEntity?) {
        _selectedPlaylist.value = playlist
        if (playlist != null) {
            viewModelScope.launch {
                repository.getTracksForPlaylist(playlist.id).collect {
                    _selectedPlaylistTracks.value = it
                }
            }
        } else {
            _selectedPlaylistTracks.value = emptyList()
        }
    }

    fun createPlaylist(name: String, description: String, gradientIndex: Int) {
        viewModelScope.launch {
            if (name.isNotBlank()) {
                repository.createPlaylist(name, description, gradientIndex)
                _statusMessage.value = "Playlist « $name » créée"
                closeCreatePlaylistDialog()
            }
        }
    }

    fun deletePlaylist(playlist: PlaylistEntity) {
        viewModelScope.launch {
            repository.deletePlaylist(playlist.id)
            if (_selectedPlaylist.value?.id == playlist.id) {
                _selectedPlaylist.value = null
            }
            _statusMessage.value = "Playlist supprimée"
        }
    }

    // Profile & Auth Methods
    fun openProfileOrAuth() {
        _isProfileAuthDialogOpen.value = true
    }

    fun closeProfileOrAuth() {
        _isProfileAuthDialogOpen.value = false
    }

    fun loginUser(username: String, emailOrId: String) {
        prefs.edit()
            .putBoolean("is_logged_in", true)
            .putString("username", username)
            .putString("email", emailOrId)
            .apply()
        _userProfile.value = _userProfile.value.copy(
            isLoggedIn = true,
            username = username,
            emailOrId = emailOrId
        )
        _statusMessage.value = "Connecté : Bienvenue $username"
    }

    fun registerUser(username: String, emailOrId: String) {
        prefs.edit()
            .putBoolean("is_logged_in", true)
            .putString("username", username)
            .putString("email", emailOrId)
            .apply()
        _userProfile.value = _userProfile.value.copy(
            isLoggedIn = true,
            username = username,
            emailOrId = emailOrId
        )
        _statusMessage.value = "Profil « $username » créé avec succès"
    }

    fun updateProfile(username: String, avatarUri: String?) {
        prefs.edit()
            .putString("username", username)
            .putString("avatar_uri", avatarUri)
            .apply()
        _userProfile.value = _userProfile.value.copy(
            username = username,
            avatarUri = avatarUri
        )
        _statusMessage.value = "Profil mis à jour"
    }

    fun logoutUser() {
        prefs.edit()
            .putBoolean("is_logged_in", false)
            .apply()
        _userProfile.value = _userProfile.value.copy(
            isLoggedIn = false
        )
        _isProfileAuthDialogOpen.value = false
        _statusMessage.value = "Déconnexion effectuée"
    }

    fun deleteUserAccount() {
        prefs.edit().clear().apply()
        _userProfile.value = UserProfile(
            isLoggedIn = false,
            username = "Utilisateur SLM",
            emailOrId = "",
            avatarUri = null
        )
        _isProfileAuthDialogOpen.value = false
        _statusMessage.value = "Compte supprimé (fichiers locaux conservés)"
    }

    fun exportCloudData() {
        viewModelScope.launch {
            try {
                val now = System.currentTimeMillis()
                prefs.edit().putLong("last_cloud_backup", now).apply()
                _userProfile.value = _userProfile.value.copy(lastCloudBackupDate = now)

                val exportText = buildString {
                    appendLine("=== SLM PLAY BACKUP & METADATA EXPORT ===")
                    appendLine("Date: ${java.util.Date(now)}")
                    appendLine("Utilisateur: ${_userProfile.value.username}")
                    appendLine("Total Pistes: ${allTracks.value.size}")
                    appendLine("Favoris: ${favoriteTracks.value.size}")
                    appendLine("Playlists (${allPlaylists.value.size}):")
                    allPlaylists.value.forEach { pl ->
                        appendLine(" - ${pl.name} (${pl.description})")
                    }
                    appendLine("Réglages Equalizer: ${_appSettings.value.selectedEqPreset}")
                    appendLine("=========================================")
                }

                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, "SLM_Play_Cloud_Export_${System.currentTimeMillis()}.txt")
                    putExtra(Intent.EXTRA_TEXT, exportText)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                getApplication<Application>().startActivity(Intent.createChooser(intent, "Sauvegarder vers le Cloud").apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                })

                _statusMessage.value = "Export Cloud préparé avec succès"
            } catch (e: Exception) {
                _statusMessage.value = "Sauvegarde locale préparée"
            }
        }
    }

    fun addTrackToPlaylist(playlistId: String, trackId: String) {
        viewModelScope.launch {
            repository.addTrackToPlaylist(playlistId, trackId)
            _statusMessage.value = "Morceau ajouté à la playlist"
            closeAddToPlaylistDialog()
        }
    }

    fun addMultipleTracksToPlaylist(playlistId: String, trackIds: List<String>) {
        viewModelScope.launch {
            val addedCount = repository.addTracksToPlaylist(playlistId, trackIds)
            _statusMessage.value = if (addedCount > 0) "$addedCount morceau(x) ajouté(s) (anti-doublon vérifié)" else "Tous les morceaux sont déjà présents"
        }
    }

    fun removeTrackFromPlaylist(playlistId: String, trackId: String) {
        viewModelScope.launch {
            repository.removeTrackFromPlaylist(playlistId, trackId)
            _statusMessage.value = "Morceau retiré de la playlist"
        }
    }

    fun removeMultipleTracksFromPlaylist(playlistId: String, trackIds: List<String>) {
        viewModelScope.launch {
            repository.removeTracksFromPlaylist(playlistId, trackIds)
            _statusMessage.value = "${trackIds.size} morceau(x) retiré(s) de la playlist"
        }
    }

    fun deleteTrack(trackId: String) {
        viewModelScope.launch {
            repository.deleteTrack(trackId)
            _statusMessage.value = "Morceau supprimé"
        }
    }

    fun scanLocalMedia() {
        viewModelScope.launch {
            _statusMessage.value = "Scan des musiques locales en cours..."
            val count = repository.scanDeviceAudioFiles()
            _statusMessage.value = if (count > 0) "$count nouveaux morceaux détectés !" else "Aucune nouvelle piste trouvée"
        }
    }

    fun importAudioFiles(uris: List<Uri>) {
        viewModelScope.launch {
            _statusMessage.value = "Importation de ${uris.size} fichiers..."
            val count = repository.importAudioUris(uris)
            _statusMessage.value = "$count morceaux importés avec succès"
        }
    }

    fun selectVibe(vibe: SlmVibe) {
        _activeVibe.value = vibe
        val matchingTracks = allTracks.value.filter {
            it.genre.contains(vibe.title.split(" ").first(), ignoreCase = true) ||
                    it.genre.contains(vibe.id.substringAfter("vibe_"), ignoreCase = true)
        }.ifEmpty { allTracks.value }

        if (matchingTracks.isNotEmpty()) {
            playTrack(matchingTracks.first(), matchingTracks, 0)
            _statusMessage.value = "Ambiance « ${vibe.title} » activée"
        }
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    override fun onCleared() {
        super.onCleared()
        if (isBound) {
            try {
                getApplication<Application>().unbindService(serviceConnection)
            } catch (e: Exception) {
                // ignore
            }
            isBound = false
        }
    }
}
