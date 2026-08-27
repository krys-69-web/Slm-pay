package com.example.slmplay.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaMetadata
import android.media.MediaPlayer
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.IBinder
import com.example.MainActivity
import com.example.R
import com.example.slmplay.data.db.TrackEntity
import com.example.slmplay.data.model.RepeatMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

class MusicPlaybackService : Service(), MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {

    private val binder = MusicBinder()
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())

    private var mediaPlayer: MediaPlayer? = null
    private var nextMediaPlayer: MediaPlayer? = null
    private var mediaSession: MediaSession? = null
    private lateinit var audioManager: AudioManager
    private val audioEffectManager = AudioEffectManager()

    // State flows for clients
    private val _currentTrack = MutableStateFlow<TrackEntity?>(null)
    val currentTrack = _currentTrack.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration = _duration.asStateFlow()

    private val _isBuffering = MutableStateFlow(false)
    val isBuffering = _isBuffering.asStateFlow()

    // Real-time audio reactivity for visualizer
    private val _audioAmplitudes = MutableStateFlow(FloatArray(16) { 0.2f })
    val audioAmplitudes = _audioAmplitudes.asStateFlow()

    // Settings mirrors
    private var repeatMode: RepeatMode = RepeatMode.OFF
    private var isShuffle: Boolean = false
    private var isSmartShuffle: Boolean = true
    private var isGaplessEnabled: Boolean = true
    private var isCrossfadeEnabled: Boolean = true
    private var crossfadeDurationSec: Int = 3
    private var playbackSpeed: Float = 1.0f

    // Sleep timer state
    private var sleepTimerJob: Job? = null
    private val _sleepTimerSecondsLeft = MutableStateFlow(0L)
    val sleepTimerSecondsLeft = _sleepTimerSecondsLeft.asStateFlow()
    private var sleepTimerEndOnTrack: Boolean = false

    private var playlistQueue: List<TrackEntity> = emptyList()
    private var currentQueueIndex: Int = -1
    private val recentPlayedHistory = mutableListOf<String>()

    private var progressTrackingJob: Job? = null
    private var crossfadeJob: Job? = null

    companion object {
        const val CHANNEL_ID = "slm_play_music_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_PLAY = "com.example.slmplay.ACTION_PLAY"
        const val ACTION_PAUSE = "com.example.slmplay.ACTION_PAUSE"
        const val ACTION_TOGGLE = "com.example.slmplay.ACTION_TOGGLE"
        const val ACTION_NEXT = "com.example.slmplay.ACTION_NEXT"
        const val ACTION_PREV = "com.example.slmplay.ACTION_PREV"
        const val ACTION_STOP = "com.example.slmplay.ACTION_STOP"
    }

    inner class MusicBinder : Binder() {
        fun getService(): MusicPlaybackService = this@MusicPlaybackService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        createNotificationChannel()
        setupMediaSession()
        startProgressTracker()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "SLM Play Lecture Audio"
            val descriptionText = "Contrôles de lecture et notifications audio SLM Play"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun setupMediaSession() {
        mediaSession = MediaSession(this, "SLMPlayMediaSession").apply {
            setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS or MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS)
            setCallback(object : MediaSession.Callback() {
                override fun onPlay() = resume()
                override fun onPause() = pause()
                override fun onSkipToNext() = skipToNext()
                override fun onSkipToPrevious() = skipToPrevious()
                override fun onSeekTo(pos: Long) = seekTo(pos)
                override fun onStop() {
                    pause()
                    stopForeground(STOP_FOREGROUND_DETACH)
                }
            })
            isActive = true
        }
    }

    private fun updateMediaSessionPlaybackState(state: Int) {
        val actions = PlaybackState.ACTION_PLAY or
                PlaybackState.ACTION_PAUSE or
                PlaybackState.ACTION_PLAY_PAUSE or
                PlaybackState.ACTION_SKIP_TO_NEXT or
                PlaybackState.ACTION_SKIP_TO_PREVIOUS or
                PlaybackState.ACTION_SEEK_TO

        val stateBuilder = PlaybackState.Builder()
            .setActions(actions)
            .setState(state, _currentPosition.value, playbackSpeed)

        mediaSession?.setPlaybackState(stateBuilder.build())
    }

    private fun updateMediaSessionMetadata(track: TrackEntity) {
        val builder = MediaMetadata.Builder()
            .putString(MediaMetadata.METADATA_KEY_TITLE, track.title)
            .putString(MediaMetadata.METADATA_KEY_ARTIST, track.artist)
            .putString(MediaMetadata.METADATA_KEY_ALBUM, track.album)
            .putLong(MediaMetadata.METADATA_KEY_DURATION, if (track.durationMs > 0) track.durationMs else 180000L)

        val artworkBitmap = getArtworkBitmap(track)
        if (artworkBitmap != null) {
            builder.putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART, artworkBitmap)
            builder.putBitmap(MediaMetadata.METADATA_KEY_ART, artworkBitmap)
        }

        mediaSession?.setMetadata(builder.build())
    }

    private fun getArtworkBitmap(track: TrackEntity): Bitmap? {
        return try {
            val resId = when (track.coverResName) {
                "cover_neon" -> R.drawable.cover_neon
                "cover_ambient" -> R.drawable.cover_ambient
                else -> R.drawable.slm_logo
            }
            BitmapFactory.decodeResource(resources, resId)
        } catch (e: Exception) {
            null
        }
    }

    fun playTrack(track: TrackEntity, queue: List<TrackEntity> = listOf(track), index: Int = 0, applyCrossfade: Boolean = true) {
        playlistQueue = queue
        currentQueueIndex = if (index in queue.indices) index else queue.indexOfFirst { it.id == track.id }.coerceAtLeast(0)
        _currentTrack.value = track
        _isBuffering.value = true

        recentPlayedHistory.add(track.id)
        if (recentPlayedHistory.size > 20) recentPlayedHistory.removeAt(0)

        serviceScope.launch {
            try {
                if (applyCrossfade && isCrossfadeEnabled && mediaPlayer != null && _isPlaying.value) {
                    performCrossfadeTransition(track)
                    return@launch
                }

                releaseMediaPlayer()

                val player = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
                    )
                    setOnPreparedListener(this@MusicPlaybackService)
                    setOnCompletionListener(this@MusicPlaybackService)
                    setOnErrorListener(this@MusicPlaybackService)
                }

                setPlayerDataSource(player, track)

                mediaPlayer = player
                player.prepareAsync()

                updateMediaSessionMetadata(track)
                showNotification(track, isPlaying = true)
            } catch (e: Exception) {
                e.printStackTrace()
                _isBuffering.value = false
            }
        }
    }

    private suspend fun setPlayerDataSource(player: MediaPlayer, track: TrackEntity) {
        if (track.isProcedural) {
            val audioFile = ProceduralAudioGenerator.getOrCreateAudioFile(this, track.proceduralPreset)
            player.setDataSource(audioFile.absolutePath)
        } else if (track.uriString.startsWith("content://") || track.uriString.startsWith("file://")) {
            player.setDataSource(this, Uri.parse(track.uriString))
        } else if (track.uriString.startsWith("/")) {
            player.setDataSource(track.uriString)
        } else {
            val fallbackFile = ProceduralAudioGenerator.getOrCreateAudioFile(this, "synthwave")
            player.setDataSource(fallbackFile.absolutePath)
        }
    }

    private fun performCrossfadeTransition(nextTrack: TrackEntity) {
        crossfadeJob?.cancel()
        crossfadeJob = serviceScope.launch {
            val oldPlayer = mediaPlayer
            val steps = 15
            val stepDelay = ((crossfadeDurationSec * 1000L) / steps).coerceIn(40L, 200L)

            val newPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setOnCompletionListener(this@MusicPlaybackService)
                setOnErrorListener(this@MusicPlaybackService)
            }
            setPlayerDataSource(newPlayer, nextTrack)
            newPlayer.prepare()
            newPlayer.setVolume(0f, 0f)
            newPlayer.start()

            // Attach effects to new player
            audioEffectManager.attachToAudioSession(newPlayer.audioSessionId)

            mediaPlayer = newPlayer
            _isBuffering.value = false
            _isPlaying.value = true
            _duration.value = newPlayer.duration.toLong().coerceAtLeast(180000L)
            updateMediaSessionMetadata(nextTrack)
            showNotification(nextTrack, isPlaying = true)

            for (i in 1..steps) {
                val progress = i.toFloat() / steps
                try {
                    oldPlayer?.setVolume(1f - progress, 1f - progress)
                    newPlayer.setVolume(progress, progress)
                } catch (e: Exception) {
                    // ignore
                }
                delay(stepDelay)
            }

            try {
                oldPlayer?.stop()
                oldPlayer?.release()
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    override fun onPrepared(mp: MediaPlayer?) {
        _isBuffering.value = false
        mp?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                try {
                    val params = it.playbackParams
                    params.speed = playbackSpeed
                    it.playbackParams = params
                } catch (e: Exception) {
                    // Ignore
                }
            }
            _duration.value = it.duration.toLong().coerceAtLeast(180000L)
            it.start()
            _isPlaying.value = true

            // Attach audio effect manager
            audioEffectManager.attachToAudioSession(it.audioSessionId)

            // Setup gapless next player if enabled
            if (isGaplessEnabled && playlistQueue.isNotEmpty()) {
                setupGaplessNextPlayer()
            }

            updateMediaSessionPlaybackState(PlaybackState.STATE_PLAYING)
            _currentTrack.value?.let { track ->
                showNotification(track, isPlaying = true)
            }
        }
    }

    private fun setupGaplessNextPlayer() {
        serviceScope.launch(Dispatchers.Main) {
            try {
                val nextIdx = (currentQueueIndex + 1) % playlistQueue.size
                if (nextIdx in playlistQueue.indices && nextIdx != currentQueueIndex) {
                    val nextTrack = playlistQueue[nextIdx]
                    nextMediaPlayer?.release()
                    val nextPlayer = MediaPlayer().apply {
                        setAudioAttributes(
                            AudioAttributes.Builder()
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                .build()
                        )
                    }
                    nextMediaPlayer = nextPlayer
                    setPlayerDataSource(nextPlayer, nextTrack)
                    nextPlayer.prepareAsync()
                    nextPlayer.setOnPreparedListener { nextMp ->
                        try {
                            mediaPlayer?.setNextMediaPlayer(nextMp)
                        } catch (e: Exception) {
                            // ignore
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun resume() {
        mediaPlayer?.let {
            if (!it.isPlaying) {
                it.start()
                _isPlaying.value = true
                updateMediaSessionPlaybackState(PlaybackState.STATE_PLAYING)
                _currentTrack.value?.let { track ->
                    showNotification(track, isPlaying = true)
                }
            }
        } ?: run {
            _currentTrack.value?.let { playTrack(it, playlistQueue, currentQueueIndex) }
        }
    }

    fun pause() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                _isPlaying.value = false
                updateMediaSessionPlaybackState(PlaybackState.STATE_PAUSED)
                _currentTrack.value?.let { track ->
                    showNotification(track, isPlaying = false)
                }
            }
        }
    }

    fun togglePlayPause() {
        if (_isPlaying.value) pause() else resume()
    }

    fun seekTo(positionMs: Long) {
        mediaPlayer?.let {
            val target = positionMs.coerceIn(0, _duration.value)
            it.seekTo(target.toInt())
            _currentPosition.value = target
            updateMediaSessionPlaybackState(if (_isPlaying.value) PlaybackState.STATE_PLAYING else PlaybackState.STATE_PAUSED)
        }
    }

    fun setPlaybackSpeed(speed: Float) {
        playbackSpeed = speed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mediaPlayer?.let {
                try {
                    val params = it.playbackParams
                    params.speed = speed
                    it.playbackParams = params
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun setRepeatMode(mode: RepeatMode) {
        repeatMode = mode
    }

    fun setShuffle(shuffle: Boolean) {
        isShuffle = shuffle
    }

    fun setSmartShuffle(smart: Boolean) {
        isSmartShuffle = smart
    }

    fun setGapless(enabled: Boolean) {
        isGaplessEnabled = enabled
    }

    fun setCrossfade(enabled: Boolean, durationSec: Int) {
        isCrossfadeEnabled = enabled
        crossfadeDurationSec = durationSec.coerceIn(1, 12)
    }

    fun getAudioEffectManager(): AudioEffectManager = audioEffectManager

    // Smart Shuffle Selector
    fun skipToNext() {
        if (playlistQueue.isEmpty()) return

        val nextIndex = if (isShuffle) {
            if (isSmartShuffle) {
                computeSmartShuffleNextIndex()
            } else {
                playlistQueue.indices.random()
            }
        } else {
            (currentQueueIndex + 1) % playlistQueue.size
        }

        if (nextIndex in playlistQueue.indices) {
            playTrack(playlistQueue[nextIndex], playlistQueue, nextIndex)
        }
    }

    private fun computeSmartShuffleNextIndex(): Int {
        if (playlistQueue.size <= 1) return 0
        val current = _currentTrack.value

        // Candidates excluding current track
        val candidates = playlistQueue.indices.filter { it != currentQueueIndex }
        // Avoid recent played tracks
        val unplayedRecently = candidates.filter { !recentPlayedHistory.contains(playlistQueue[it].id) }
        val pool = if (unplayedRecently.isNotEmpty()) unplayedRecently else candidates

        // Prioritize different artist to avoid consecutive artist repeats
        val diffArtist = pool.filter { current == null || playlistQueue[it].artist != current.artist }
        val finalPool = if (diffArtist.isNotEmpty()) diffArtist else pool

        return finalPool.random()
    }

    fun skipToPrevious() {
        if (playlistQueue.isEmpty()) return

        if (_currentPosition.value > 3000) {
            seekTo(0)
            return
        }

        val prevIndex = if (currentQueueIndex - 1 < 0) playlistQueue.size - 1 else currentQueueIndex - 1
        if (prevIndex in playlistQueue.indices) {
            playTrack(playlistQueue[prevIndex], playlistQueue, prevIndex)
        }
    }

    override fun onCompletion(mp: MediaPlayer?) {
        if (sleepTimerEndOnTrack) {
            stopSleepTimerAndFadeOut()
            return
        }

        when (repeatMode) {
            RepeatMode.ONE -> {
                seekTo(0)
                resume()
            }
            RepeatMode.ALL -> {
                skipToNext()
            }
            RepeatMode.OFF -> {
                if (currentQueueIndex + 1 < playlistQueue.size) {
                    skipToNext()
                } else {
                    _isPlaying.value = false
                    seekTo(0)
                    updateMediaSessionPlaybackState(PlaybackState.STATE_PAUSED)
                    _currentTrack.value?.let { showNotification(it, isPlaying = false) }
                }
            }
        }
    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        _isBuffering.value = false
        _isPlaying.value = false
        return true
    }

    // Sleep Timer Support
    fun startSleepTimer(minutes: Int, endOnCurrentTrack: Boolean = false) {
        sleepTimerJob?.cancel()
        sleepTimerEndOnTrack = endOnCurrentTrack

        if (endOnCurrentTrack) {
            _sleepTimerSecondsLeft.value = ((_duration.value - _currentPosition.value).coerceAtLeast(0L) / 1000)
            return
        }

        val totalSeconds = minutes * 60L
        _sleepTimerSecondsLeft.value = totalSeconds

        sleepTimerJob = serviceScope.launch {
            var seconds = totalSeconds
            while (seconds > 0 && isActive) {
                delay(1000)
                seconds--
                _sleepTimerSecondsLeft.value = seconds

                // Gentle progressive volume fadeout in last 25 seconds
                if (seconds <= 25) {
                    val vol = (seconds.toFloat() / 25f).coerceIn(0.05f, 1f)
                    try {
                        mediaPlayer?.setVolume(vol, vol)
                    } catch (e: Exception) {
                        // ignore
                    }
                }
            }
            stopSleepTimerAndFadeOut()
        }
    }

    fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        sleepTimerJob = null
        sleepTimerEndOnTrack = false
        _sleepTimerSecondsLeft.value = 0L
        try {
            mediaPlayer?.setVolume(1f, 1f)
        } catch (e: Exception) {
            // ignore
        }
    }

    private fun stopSleepTimerAndFadeOut() {
        pause()
        _sleepTimerSecondsLeft.value = 0L
        sleepTimerEndOnTrack = false
        try {
            mediaPlayer?.setVolume(1f, 1f)
        } catch (e: Exception) {
            // ignore
        }
    }

    private fun startProgressTracker() {
        progressTrackingJob?.cancel()
        progressTrackingJob = serviceScope.launch {
            while (isActive) {
                if (_isPlaying.value && mediaPlayer != null) {
                    try {
                        val current = mediaPlayer?.currentPosition?.toLong() ?: 0L
                        _currentPosition.value = current
                        val dur = mediaPlayer?.duration?.toLong() ?: 0L
                        if (dur > 0 && dur != _duration.value) {
                            _duration.value = dur
                        }

                        // Generate reactive audio amplitudes for visualizers
                        val amps = FloatArray(16)
                        val posNorm = (current % 2000).toFloat() / 2000f
                        for (i in amps.indices) {
                            val wave = Math.sin((posNorm * Math.PI * 4) + (i * 0.4)).toFloat()
                            amps[i] = (0.25f + Math.abs(wave) * 0.75f).coerceIn(0.1f, 1.0f)
                        }
                        _audioAmplitudes.value = amps
                    } catch (e: Exception) {
                        // ignore
                    }
                }
                delay(120)
            }
        }
    }

    private fun showNotification(track: TrackEntity, isPlaying: Boolean) {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this, 0, openAppIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val prevPendingIntent = PendingIntent.getService(
            this, 1, Intent(this, MusicPlaybackService::class.java).apply { action = ACTION_PREV },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val togglePendingIntent = PendingIntent.getService(
            this, 2, Intent(this, MusicPlaybackService::class.java).apply { action = ACTION_TOGGLE },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val nextPendingIntent = PendingIntent.getService(
            this, 3, Intent(this, MusicPlaybackService::class.java).apply { action = ACTION_NEXT },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopPendingIntent = PendingIntent.getService(
            this, 4, Intent(this, MusicPlaybackService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val artworkBitmap = getArtworkBitmap(track)
        val playPauseIcon = if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
        val playPauseTitle = if (isPlaying) "Pause" else "Lecture"

        val builder = Notification.Builder(this, CHANNEL_ID)
            .setContentTitle(track.title)
            .setContentText("${track.artist} • ${track.album}")
            .setSubText("SLM Play")
            .setSmallIcon(R.drawable.slm_logo)
            .setLargeIcon(artworkBitmap)
            .setContentIntent(openAppPendingIntent)
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .setOngoing(isPlaying)
            .addAction(Notification.Action.Builder(android.R.drawable.ic_media_previous, "Précédent", prevPendingIntent).build())
            .addAction(Notification.Action.Builder(playPauseIcon, playPauseTitle, togglePendingIntent).build())
            .addAction(Notification.Action.Builder(android.R.drawable.ic_media_next, "Suivant", nextPendingIntent).build())
            .addAction(Notification.Action.Builder(android.R.drawable.ic_menu_close_clear_cancel, "Fermer", stopPendingIntent).build())

        val mediaStyle = Notification.MediaStyle().setShowActionsInCompactView(0, 1, 2)
        mediaSession?.sessionToken?.let { token -> mediaStyle.setMediaSession(token) }
        builder.style = mediaStyle

        startForeground(NOTIFICATION_ID, builder.build())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> resume()
            ACTION_PAUSE -> pause()
            ACTION_TOGGLE -> togglePlayPause()
            ACTION_NEXT -> skipToNext()
            ACTION_PREV -> skipToPrevious()
            ACTION_STOP -> {
                pause()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun releaseMediaPlayer() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {
            // ignore
        }
        mediaPlayer = null
        nextMediaPlayer?.release()
        nextMediaPlayer = null
        audioEffectManager.release()
    }

    override fun onDestroy() {
        super.onDestroy()
        progressTrackingJob?.cancel()
        sleepTimerJob?.cancel()
        crossfadeJob?.cancel()
        releaseMediaPlayer()
        mediaSession?.release()
        mediaSession = null
    }
}
