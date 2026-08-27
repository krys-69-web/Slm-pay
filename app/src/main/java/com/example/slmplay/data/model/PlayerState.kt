package com.example.slmplay.data.model

enum class RepeatMode {
    OFF,
    ALL,
    ONE
}

enum class BackgroundMode(val displayName: String, val description: String) {
    DYNAMIC_MESH("Ondes Vivantes (HOpE)", "Fluide dynamique animé et particules réactives"),
    ARTWORK_AURA("Aura Pochette (Apple Glass)", "Couleurs diffuses synchronisées avec l'album"),
    NEON_PULSE("Pulsation Cyber SLM", "Lueurs néon vibrantes #ff2d55 au rythme de la musique"),
    DEEP_GLASS("Obsidian Glass Minimal", "Fond sombre épuré avec reflets de verre dépoli")
}

data class SlmVibe(
    val id: String,
    val title: String,
    val subtitle: String,
    val emoji: String,
    val promptContext: String,
    val gradientColors: List<Long>,
    val targetBpmRange: String
)
