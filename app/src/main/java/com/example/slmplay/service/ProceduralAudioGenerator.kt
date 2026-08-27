package com.example.slmplay.service

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

object ProceduralAudioGenerator {

    private const val SAMPLE_RATE = 44100
    private const val DURATION_SECONDS = 180 // 3 minutes full loopable track

    suspend fun getOrCreateAudioFile(context: Context, preset: String): File = withContext(Dispatchers.IO) {
        val audioDir = File(context.cacheDir, "procedural_tracks").apply { if (!exists()) mkdirs() }
        val targetFile = File(audioDir, "track_$preset.wav")
        if (targetFile.exists() && targetFile.length() > 1000) {
            return@withContext targetFile
        }

        val totalSamples = SAMPLE_RATE * DURATION_SECONDS
        val pcmBuffer = ShortArray(totalSamples)

        val chords = when (preset) {
            "synthwave" -> listOf(
                listOf(220.0, 261.63, 329.63), // Am
                listOf(174.61, 220.0, 261.63), // F
                listOf(261.63, 329.63, 392.0), // C
                listOf(196.0, 246.94, 293.66)  // G
            )
            "lofi" -> listOf(
                listOf(130.81, 196.0, 246.94, 311.13), // Cmaj7
                listOf(110.0, 164.81, 220.0, 261.63),  // Am7
                listOf(87.31, 130.81, 174.61, 220.0),   // Fmaj7
                listOf(98.0, 146.83, 196.0, 246.94)    // G7
            )
            "cyberpunk" -> listOf(
                listOf(110.0, 130.81, 164.81), // Am bass
                listOf(98.0, 123.47, 146.83),  // G bass
                listOf(87.31, 110.0, 130.81),  // F bass
                listOf(82.41, 103.83, 123.47)  // E bass
            )
            "ambient" -> listOf(
                listOf(174.61, 261.63, 349.23, 440.0), // Fmaj9
                listOf(196.0, 293.66, 392.0, 493.88),  // Gsus4
                listOf(220.0, 329.63, 440.0, 523.25),  // Am9
                listOf(130.81, 196.0, 261.63, 329.63)  // Cmaj7
            )
            else -> listOf(
                listOf(261.63, 329.63, 392.0),
                listOf(220.0, 261.63, 329.63),
                listOf(174.61, 220.0, 261.63),
                listOf(196.0, 246.94, 293.66)
            )
        }

        val tempoBpm = when (preset) {
            "synthwave" -> 118.0
            "lofi" -> 78.0
            "cyberpunk" -> 130.0
            "ambient" -> 64.0
            else -> 100.0
        }

        val beatDurationSec = 60.0 / tempoBpm
        val chordDurationBeats = 4.0
        val chordDurationSec = beatDurationSec * chordDurationBeats

        var time = 0.0
        val dt = 1.0 / SAMPLE_RATE

        for (i in 0 until totalSamples) {
            val currentChordIndex = ((time / chordDurationSec).toInt()) % chords.size
            val chord = chords[currentChordIndex]
            val chordLocalTime = time % chordDurationSec

            var signal = 0.0

            // 1. Warm Pad / Chord harmony with gentle detuned chorus
            chord.forEach { freq ->
                val osc1 = sin(2.0 * PI * freq * time)
                val osc2 = sin(2.0 * PI * (freq * 1.004) * time) * 0.7
                val lfo = (1.0 + 0.3 * sin(2.0 * PI * 0.4 * time))
                signal += (osc1 + osc2) * 0.12 * lfo
            }

            // 2. Rhythmic Arpeggio / Melody
            val arpStep = ((time / (beatDurationSec / 2.0)).toInt()) % (chord.size * 2)
            val arpNoteIndex = arpStep % chord.size
            val arpFreq = chord[arpNoteIndex] * (if (arpStep >= chord.size) 2.0 else 1.0)
            val arpLocalTime = (time % (beatDurationSec / 2.0))
            val arpEnvelope = exp(-arpLocalTime * (if (preset == "lofi") 4.0 else 7.0))
            signal += sin(2.0 * PI * arpFreq * time) * 0.22 * arpEnvelope

            // 3. Sub Bass Pulse
            val bassFreq = chord[0] / 2.0
            val bassEnv = if (preset == "cyberpunk") {
                val beatTime = time % (beatDurationSec / 2.0)
                exp(-beatTime * 3.0)
            } else {
                val beatTime = time % beatDurationSec
                exp(-beatTime * 2.0)
            }
            signal += sin(2.0 * PI * bassFreq * time) * 0.30 * bassEnv

            // 4. Soft Beat / Percussion simulation
            val beatTime = time % beatDurationSec
            val isKick = (beatTime < 0.08)
            val isSnare = ((time % (beatDurationSec * 2)) >= beatDurationSec && beatTime < 0.06)

            if (isKick) {
                val kickEnv = exp(-beatTime * 35.0)
                val kickFreq = 120.0 * exp(-beatTime * 30.0)
                signal += sin(2.0 * PI * kickFreq * time) * 0.4 * kickEnv
            } else if (isSnare) {
                val snareEnv = exp(-beatTime * 25.0)
                // White noise pulse
                val noise = ((Math.random() * 2.0) - 1.0)
                signal += noise * 0.15 * snareEnv
            }

            // Soft saturation limiter
            val clamped = (signal.coerceIn(-0.95, 0.95) * Short.MAX_VALUE).toInt().toShort()
            pcmBuffer[i] = clamped
            time += dt
        }

        writeWavFile(targetFile, pcmBuffer, SAMPLE_RATE)
        targetFile
    }

    private fun writeWavFile(file: File, pcmData: ShortArray, sampleRate: Int) {
        val totalAudioLen = (pcmData.size * 2).toLong()
        val totalDataLen = totalAudioLen + 36
        val byteRate = (sampleRate * 1 * 16 / 8).toLong()

        val header = ByteArray(44)
        val byteBuffer = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN)

        // RIFF header
        byteBuffer.put("RIFF".toByteArray())
        byteBuffer.putInt(totalDataLen.toInt())
        byteBuffer.put("WAVE".toByteArray())

        // fmt chunk
        byteBuffer.put("fmt ".toByteArray())
        byteBuffer.putInt(16) // Sub-chunk size (16 for PCM)
        byteBuffer.putShort(1.toShort()) // AudioFormat (1 for PCM)
        byteBuffer.putShort(1.toShort()) // NumChannels (1 = Mono)
        byteBuffer.putInt(sampleRate)
        byteBuffer.putInt(byteRate.toInt())
        byteBuffer.putShort(2.toShort()) // BlockAlign (1 * 16 / 8)
        byteBuffer.putShort(16.toShort()) // BitsPerSample

        // data chunk
        byteBuffer.put("data".toByteArray())
        byteBuffer.putInt(totalAudioLen.toInt())

        FileOutputStream(file).use { fos ->
            fos.write(header)
            val bytePcm = ByteArray(pcmData.size * 2)
            val pcmBuf = ByteBuffer.wrap(bytePcm).order(ByteOrder.LITTLE_ENDIAN)
            for (sample in pcmData) {
                pcmBuf.putShort(sample)
            }
            fos.write(bytePcm)
        }
    }
}
