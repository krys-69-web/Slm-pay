package com.example.slmplay.service

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.Virtualizer
import android.util.Log

class AudioEffectManager {

    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null

    val presetNames = listOf(
        "Bass Boost",
        "Vocal",
        "Rock",
        "Pop",
        "Hip-Hop",
        "Lofi",
        "Classique",
        "Manuel"
    )

    fun attachToAudioSession(audioSessionId: Int) {
        if (audioSessionId == 0) return
        release()

        try {
            equalizer = Equalizer(0, audioSessionId).apply {
                enabled = true
            }
            bassBoost = BassBoost(0, audioSessionId).apply {
                enabled = true
            }
            virtualizer = Virtualizer(0, audioSessionId).apply {
                enabled = true
            }
        } catch (e: Exception) {
            Log.e("AudioEffectManager", "Failed to init audio effects: ${e.message}")
        }
    }

    fun setEqualizerEnabled(enabled: Boolean) {
        try {
            equalizer?.enabled = enabled
            bassBoost?.enabled = enabled
            virtualizer?.enabled = enabled
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun applyPreset(presetName: String) {
        val eq = equalizer ?: return
        try {
            val numBands = eq.numberOfBands.toInt()
            val minEqLevel = eq.bandLevelRange[0]
            val maxEqLevel = eq.bandLevelRange[1]
            val step = (maxEqLevel - minEqLevel) / 24

            // Custom curve mappings in dB
            val curve = when (presetName) {
                "Bass Boost" -> listOf(8f, 5f, 1f, 0f, 2f)
                "Vocal" -> listOf(-2f, 1f, 6f, 7f, 3f)
                "Rock" -> listOf(5f, 3f, -1f, 4f, 6f)
                "Pop" -> listOf(2f, 4f, 5f, 3f, 2f)
                "Hip-Hop" -> listOf(7f, 5f, 1f, 3f, 5f)
                "Lofi" -> listOf(4f, 2f, 3f, -2f, -4f)
                "Classique" -> listOf(4f, 3f, 2f, 3f, 4f)
                else -> listOf(0f, 0f, 0f, 0f, 0f)
            }

            for (i in 0 until numBands.coerceAtMost(curve.size)) {
                val db = curve[i]
                val level = (db * step).toInt().coerceIn(minEqLevel.toInt(), maxEqLevel.toInt())
                eq.setBandLevel(i.toShort(), level.toShort())
            }

            // Set BassBoost based on preset
            val bbStrength = when (presetName) {
                "Bass Boost", "Hip-Hop" -> 800
                "Rock" -> 500
                "Lofi" -> 600
                else -> 200
            }
            bassBoost?.setStrength(bbStrength.toShort())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setManualBand(bandIndex: Int, dbGain: Float) {
        val eq = equalizer ?: return
        try {
            if (bandIndex < eq.numberOfBands) {
                val minEqLevel = eq.bandLevelRange[0]
                val maxEqLevel = eq.bandLevelRange[1]
                val step = (maxEqLevel - minEqLevel) / 24
                val level = (dbGain * step).toInt().coerceIn(minEqLevel.toInt(), maxEqLevel.toInt())
                eq.setBandLevel(bandIndex.toShort(), level.toShort())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setBassBoost(strengthFraction: Float) {
        try {
            val s = (strengthFraction.coerceIn(0f, 1f) * 1000).toInt().toShort()
            bassBoost?.setStrength(s)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setVirtualizer(strengthFraction: Float) {
        try {
            val s = (strengthFraction.coerceIn(0f, 1f) * 1000).toInt().toShort()
            virtualizer?.setStrength(s)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun release() {
        try {
            equalizer?.release()
            bassBoost?.release()
            virtualizer?.release()
        } catch (e: Exception) {
            // ignore
        } finally {
            equalizer = null
            bassBoost = null
            virtualizer = null
        }
    }
}
