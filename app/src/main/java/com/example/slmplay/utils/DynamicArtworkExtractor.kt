package com.example.slmplay.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object DynamicArtworkExtractor {

    data class ArtworkPalette(
        val dominantColor: Color,
        val accentColor: Color,
        val surfaceColor: Color,
        val isDark: Boolean
    )

    private val defaultPalette = ArtworkPalette(
        dominantColor = Color(0xFFFF2D55),
        accentColor = Color(0xFFAF52DE),
        surfaceColor = Color(0xFF12050A),
        isDark = true
    )

    suspend fun extractPalette(context: Context, coverUri: String?, coverResName: String?): ArtworkPalette = withContext(Dispatchers.IO) {
        try {
            var bitmap: Bitmap? = null

            if (!coverUri.isNullOrBlank()) {
                val uri = Uri.parse(coverUri)
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val options = BitmapFactory.Options().apply {
                        inSampleSize = 8 // Downsample for ultra-fast and RAM-safe palette analysis
                    }
                    bitmap = BitmapFactory.decodeStream(stream, null, options)
                }
            }

            if (bitmap == null && coverResName != null) {
                val resId = when (coverResName) {
                    "cover_neon" -> com.example.R.drawable.cover_neon
                    "cover_ambient" -> com.example.R.drawable.cover_ambient
                    else -> com.example.R.drawable.slm_logo
                }
                val options = BitmapFactory.Options().apply { inSampleSize = 8 }
                bitmap = BitmapFactory.decodeResource(context.resources, resId, options)
            }

            val bmp = bitmap ?: return@withContext defaultPalette

            // Sample colors from corners and center
            val w = bmp.width
            val h = bmp.height
            if (w <= 0 || h <= 0) return@withContext defaultPalette

            val samplePixels = intArrayOf(
                bmp.getPixel(w / 2, h / 2),
                bmp.getPixel(w / 4, h / 4),
                bmp.getPixel(3 * w / 4, h / 4),
                bmp.getPixel(w / 4, 3 * h / 4),
                bmp.getPixel(3 * w / 4, 3 * h / 4)
            )

            var rSum = 0
            var gSum = 0
            var bSum = 0
            var maxSatColor = samplePixels[0]
            var maxSat = -1f

            val hsv = FloatArray(3)
            for (pixel in samplePixels) {
                val r = (pixel shr 16) and 0xFF
                val g = (pixel shr 8) and 0xFF
                val b = pixel and 0xFF
                rSum += r
                gSum += g
                bSum += b

                android.graphics.Color.RGBToHSV(r, g, b, hsv)
                if (hsv[1] > maxSat) {
                    maxSat = hsv[1]
                    maxSatColor = pixel
                }
            }

            val avgR = (rSum / samplePixels.size).coerceIn(0, 255)
            val avgG = (gSum / samplePixels.size).coerceIn(0, 255)
            val avgB = (bSum / samplePixels.size).coerceIn(0, 255)

            val domR = ((maxSatColor shr 16) and 0xFF)
            val domG = ((maxSatColor shr 8) and 0xFF)
            val domB = (maxSatColor and 0xFF)

            val dominant = Color(domR, domG, domB)
            val accent = Color(avgR, avgG, avgB)
            val surface = Color((domR * 0.15f).toInt(), (domG * 0.15f).toInt(), (domB * 0.15f).toInt())

            ArtworkPalette(
                dominantColor = dominant,
                accentColor = accent,
                surfaceColor = surface,
                isDark = true
            )
        } catch (e: Exception) {
            defaultPalette
        }
    }
}
