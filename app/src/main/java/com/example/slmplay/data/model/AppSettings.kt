package com.example.slmplay.data.model

data class AppSettings(
    // 1. Convertisseur multimédia intégré
    val isMediaConverterEnabled: Boolean = true,
    val defaultConversionBitrate: Int = 256, // kbps

    // 2. SLM Audio Editor
    val isAudioEditorEnabled: Boolean = true,

    // 3. SLM Equalizer
    val isEqualizerEnabled: Boolean = true,
    val selectedEqPreset: String = "Bass Boost",
    val manualBands: List<Float> = listOf(0f, 0f, 0f, 0f, 0f), // dB for 60Hz, 230Hz, 910Hz, 3600Hz, 14kHz
    val bassBoostStrength: Float = 0.6f, // 0.0 to 1.0
    val virtualizerStrength: Float = 0.4f,

    // 4. SLM Visualizer
    val isVisualizerEnabled: Boolean = true,
    val visualizerType: VisualizerType = VisualizerType.BARS,
    val visualizerSensitivity: Float = 1.0f,

    // 5. SLM Vibe
    val isSlmVibeEnabled: Boolean = true,
    val autoAdaptVibe: Boolean = true,
    val currentVibeMood: VibeMood = VibeMood.ENERGY,

    // 6. Dynamic Artwork
    val isDynamicArtworkEnabled: Boolean = true,
    val dynamicArtworkBlur: Float = 0.7f,

    // 7. Artwork Studio
    val isArtworkStudioEnabled: Boolean = true,

    // 8. Smart Library
    val isSmartLibraryEnabled: Boolean = true,
    val librarySortOption: LibrarySortOption = LibrarySortOption.ADDED_RECENT,

    // 9. Smart Favorites
    val isSmartFavoritesEnabled: Boolean = true,

    // 10. Smart Shuffle
    val isSmartShuffleEnabled: Boolean = true,

    // 11. Gapless Playback
    val isGaplessPlaybackEnabled: Boolean = true,

    // 12. Crossfade
    val isCrossfadeEnabled: Boolean = true,
    val crossfadeDurationSeconds: Int = 3, // 1 to 12s

    // 13. Sleep Timer
    val isSleepTimerActive: Boolean = false,
    val sleepTimerRemainingSeconds: Long = 0L,
    val sleepTimerEndOnTrackFinish: Boolean = false
)

enum class VisualizerType(val displayName: String, val emoji: String) {
    BARS("Barres audio", "📊"),
    PULSING_CIRCLE("Cercle pulsant", "⭕"),
    PARTICLES("Particules", "✨"),
    WAVES("Vagues fluides", "🌊"),
    ABSTRACT_SHAPES("Formes abstraites", "💠"),
    HOPE_3D("HOpE Immersion 3D", "🌌")
}

enum class VibeMood(val displayName: String, val emoji: String, val description: String, val colorHex: Long) {
    ENERGY("Energy", "⚡", "Bassline percutante & rythme soutenu", 0xFFFF2D55),
    CHILL("Chill", "🌙", "Mélodies douces & relaxation", 0xFF5856D6),
    LOFI("Lofi", "☕", "Ambiance feutrée & chaleur vintage", 0xFFFF9500),
    IMMERSION("Immersion", "🌌", "Spatialisation 3D & réverbération", 0xFF00C7BE),
    DRIVE("Drive", "🚗", "Groove rétro & synthés nocturnes", 0xFFFF375F)
}

enum class LibrarySortOption(val displayName: String) {
    ADDED_RECENT("Ajout récent"),
    TITLE_AZ("Titre (A-Z)"),
    ARTIST("Artiste"),
    ALBUM("Album"),
    GENRE("Genre"),
    YEAR("Année"),
    DURATION("Durée"),
    FAVORITES("Favoris"),
    RECENTLY_PLAYED("Lecture récente")
}
