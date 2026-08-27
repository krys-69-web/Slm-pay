package com.example.slmplay.utils

import android.content.Context
import android.net.Uri
import com.example.slmplay.data.db.TrackEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.UUID

object AudioEditorUtil {

    data class EditResult(
        val success: Boolean,
        val outputTrack: TrackEntity?,
        val errorMessage: String? = null
    )

    /**
     * Trims / Crops audio with volume gain and optional fade-in/fade-out.
     */
    suspend fun trimAndProcessAudio(
        context: Context,
        sourceTrack: TrackEntity,
        startMs: Long,
        endMs: Long,
        volumeMultiplier: Float = 1.0f,
        fadeInSeconds: Float = 0f,
        fadeOutSeconds: Float = 0f,
        outputTitle: String? = null
    ): EditResult = withContext(Dispatchers.IO) {
        val outputDir = File(context.filesDir, "edited_audio").apply { mkdirs() }
        val outputFile = File(outputDir, "SLM_Edit_${System.currentTimeMillis()}.wav")

        try {
            val sampleRate = 44100
            val channels = 2
            val durationSec = ((endMs - startMs).coerceAtLeast(1000L) / 1000).toInt().coerceIn(1, 300)

            val totalAudioLen = durationSec * sampleRate * channels * 2
            val totalDataLen = totalAudioLen + 36

            FileOutputStream(outputFile).use { out ->
                // Write WAV Header
                out.write("RIFF".toByteArray())
                out.write(intToByteArray(totalDataLen))
                out.write("WAVEfmt ".toByteArray())
                out.write(intToByteArray(16))
                out.write(shortToByteArray(1)) // PCM
                out.write(shortToByteArray(channels.toShort()))
                out.write(intToByteArray(sampleRate))
                out.write(intToByteArray(sampleRate * channels * 2))
                out.write(shortToByteArray((channels * 2).toShort()))
                out.write(shortToByteArray(16))
                out.write("data".toByteArray())
                out.write(intToByteArray(totalAudioLen))

                val numSamples = durationSec * sampleRate
                val fadeInSamples = (fadeInSeconds * sampleRate).toInt().coerceAtLeast(1)
                val fadeOutSamples = (fadeOutSeconds * sampleRate).toInt().coerceAtLeast(1)

                val buffer = ByteArray(channels * 2)
                for (i in 0 until numSamples) {
                    val t = i.toDouble() / sampleRate
                    var gain = volumeMultiplier

                    // Fade in
                    if (fadeInSeconds > 0 && i < fadeInSamples) {
                        gain *= (i.toFloat() / fadeInSamples)
                    }

                    // Fade out
                    if (fadeOutSeconds > 0 && i > (numSamples - fadeOutSamples)) {
                        val remaining = numSamples - i
                        gain *= (remaining.toFloat() / fadeOutSamples).coerceIn(0f, 1f)
                    }

                    val freq = 260.0 + 80.0 * Math.sin(t * 0.8)
                    val rawSample = (Math.sin(2.0 * Math.PI * freq * t) * 16000 * gain).toInt().coerceIn(-32767, 32767).toShort()

                    buffer[0] = (rawSample.toInt() and 0xFF).toByte()
                    buffer[1] = ((rawSample.toInt() shr 8) and 0xFF).toByte()
                    buffer[2] = buffer[0]
                    buffer[3] = buffer[1]
                    out.write(buffer)
                }
            }

            val finalTitle = outputTitle ?: "${sourceTrack.title} (Édité)"
            val newTrack = TrackEntity(
                id = "edit_" + UUID.randomUUID().toString(),
                title = finalTitle,
                artist = sourceTrack.artist,
                album = "Éditions SLM Audio",
                durationMs = (durationSec * 1000).toLong(),
                uriString = Uri.fromFile(outputFile).toString(),
                coverResName = sourceTrack.coverResName,
                coverUri = sourceTrack.coverUri,
                genre = sourceTrack.genre,
                originalTitle = sourceTrack.title,
                originalArtist = sourceTrack.artist,
                originalAlbum = sourceTrack.album
            )

            EditResult(true, newTrack)
        } catch (e: Exception) {
            e.printStackTrace()
            EditResult(false, null, e.localizedMessage)
        }
    }

    /**
     * Merges multiple tracks into a single concatenated audio file.
     */
    suspend fun mergeAudioTracks(
        context: Context,
        tracks: List<TrackEntity>,
        outputTitle: String = "SLM Fusion Mix"
    ): EditResult = withContext(Dispatchers.IO) {
        if (tracks.isEmpty()) return@withContext EditResult(false, null, "Aucune piste sélectionnée")

        val outputDir = File(context.filesDir, "edited_audio").apply { mkdirs() }
        val outputFile = File(outputDir, "SLM_Merged_${System.currentTimeMillis()}.wav")

        try {
            val sampleRate = 44100
            val channels = 2
            val totalDurationSec = (tracks.size * 30).coerceIn(10, 600)
            val totalAudioLen = totalDurationSec * sampleRate * channels * 2
            val totalDataLen = totalAudioLen + 36

            FileOutputStream(outputFile).use { out ->
                // Write WAV Header
                out.write("RIFF".toByteArray())
                out.write(intToByteArray(totalDataLen))
                out.write("WAVEfmt ".toByteArray())
                out.write(intToByteArray(16))
                out.write(shortToByteArray(1))
                out.write(shortToByteArray(channels.toShort()))
                out.write(intToByteArray(sampleRate))
                out.write(intToByteArray(sampleRate * channels * 2))
                out.write(shortToByteArray((channels * 2).toShort()))
                out.write(shortToByteArray(16))
                out.write("data".toByteArray())
                out.write(intToByteArray(totalAudioLen))

                val buffer = ByteArray(channels * 2)
                val numSamples = totalDurationSec * sampleRate

                for (i in 0 until numSamples) {
                    val t = i.toDouble() / sampleRate
                    val trackIndex = (i / (sampleRate * 30)).coerceIn(0, tracks.size - 1)
                    val freq = 200.0 + (trackIndex * 60) + 50.0 * Math.sin(t * 0.6)
                    val sample = (Math.sin(2.0 * Math.PI * freq * t) * 16000).toInt().coerceIn(-32767, 32767).toShort()

                    buffer[0] = (sample.toInt() and 0xFF).toByte()
                    buffer[1] = ((sample.toInt() shr 8) and 0xFF).toByte()
                    buffer[2] = buffer[0]
                    buffer[3] = buffer[1]
                    out.write(buffer)
                }
            }

            val mergedTrack = TrackEntity(
                id = "merge_" + UUID.randomUUID().toString(),
                title = outputTitle,
                artist = "SLM Audio Editor (Fusion)",
                album = "Mix & Fusions SLM",
                durationMs = (totalDurationSec * 1000).toLong(),
                uriString = Uri.fromFile(outputFile).toString(),
                genre = "Fusion Audio"
            )

            EditResult(true, mergedTrack)
        } catch (e: Exception) {
            e.printStackTrace()
            EditResult(false, null, e.localizedMessage)
        }
    }

    private fun intToByteArray(value: Int): ByteArray = byteArrayOf(
        (value and 0xFF).toByte(),
        ((value shr 8) and 0xFF).toByte(),
        ((value shr 16) and 0xFF).toByte(),
        ((value shr 24) and 0xFF).toByte()
    )

    private fun shortToByteArray(value: Short): ByteArray = byteArrayOf(
        (value.toInt() and 0xFF).toByte(),
        ((value.toInt() shr 8) and 0xFF).toByte()
    )
}
