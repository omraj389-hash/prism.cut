package com.example.util

import androidx.compose.ui.graphics.ColorMatrix
import com.example.model.ColorGradeSettings
import kotlin.math.cos
import kotlin.math.sin

object ColorGradingEngine {

    /**
     * Converts industry-standard ColorGradeSettings into a Compose 4x5 ColorMatrix
     * for zero-latency, hardware-accelerated real-time rendering.
     */
    fun createColorMatrix(settings: ColorGradeSettings): ColorMatrix {
        val matrix = ColorMatrix()

        // 1. Exposure & Contrast
        // Exposure: -100..+100 maps to scale [0.2 .. 2.5]
        val exposureScale = when {
            settings.exposure >= 0 -> 1f + (settings.exposure / 100f) * 1.5f
            else -> 1f + (settings.exposure / 100f) * 0.8f
        }.coerceAtLeast(0.1f)

        // Contrast: -100..+100 maps to contrast factor
        val contrastFactor = when {
            settings.contrast >= 0 -> 1f + (settings.contrast / 100f) * 1.8f
            else -> 1f + (settings.contrast / 100f) * 0.7f
        }.coerceAtLeast(0.1f)
        val contrastOffset = 128f * (1f - contrastFactor)

        // 2. Saturation: 0..200 (100 = default 1.0)
        val sat = (settings.saturation / 100f).coerceIn(0f, 3f)

        // 3. Temperature & Tint
        // Temperature: -100 (Cool/Blue) to +100 (Warm/Amber)
        val tempNorm = settings.temperature / 100f
        val tempR = if (tempNorm > 0) 1f + tempNorm * 0.35f else 1f + tempNorm * 0.15f
        val tempB = if (tempNorm < 0) 1f - tempNorm * 0.35f else 1f - tempNorm * 0.20f

        // Tint: -100 (Green) to +100 (Magenta)
        val tintNorm = settings.tint / 100f
        val tintR = if (tintNorm > 0) 1f + tintNorm * 0.2f else 1f
        val tintG = if (tintNorm < 0) 1f - tintNorm * 0.25f else 1f - tintNorm * 0.15f
        val tintB = if (tintNorm > 0) 1f + tintNorm * 0.2f else 1f

        // 4. Color Wheels Contribution (Lift, Gamma, Gain)
        // Convert polar (hue, saturation) to RGB color tint offsets
        val (liftR, liftG, liftB) = wheelToRgbDeltas(settings.lift.hue, settings.lift.saturation, settings.lift.luminance)
        val (gainR, gainG, gainB) = wheelToRgbDeltas(settings.gain.hue, settings.gain.saturation, settings.gain.luminance)
        val (gammaR, gammaG, gammaB) = wheelToRgbDeltas(settings.gamma.hue, settings.gamma.saturation, settings.gamma.luminance)

        // Net RGB channel multipliers
        var multR = exposureScale * contrastFactor * tempR * tintR * (1f + gainR * 0.5f + gammaR * 0.3f)
        var multG = exposureScale * contrastFactor * tintG * (1f + gainG * 0.5f + gammaG * 0.3f)
        var multB = exposureScale * contrastFactor * tempB * tintB * (1f + gainB * 0.5f + gammaB * 0.3f)

        // Net RGB channel offsets
        var offR = contrastOffset + (liftR * 60f) + (settings.shadows * 0.3f) + (settings.highlights * 0.3f)
        var offG = contrastOffset + (liftG * 60f) + (settings.shadows * 0.3f) + (settings.highlights * 0.3f)
        var offB = contrastOffset + (liftB * 60f) + (settings.shadows * 0.3f) + (settings.highlights * 0.3f)

        // Apply saturation via Rec.709 luminance weights (0.2126 R + 0.7152 G + 0.0722 B)
        val lr = 0.2126f
        val lg = 0.7152f
        val lb = 0.0722f

        val invSat = 1f - sat
        val m00 = (invSat * lr + sat) * multR
        val m01 = (invSat * lg) * multR
        val m02 = (invSat * lb) * multR

        val m10 = (invSat * lr) * multG
        val m11 = (invSat * lg + sat) * multG
        val m12 = (invSat * lb) * multG

        val m20 = (invSat * lr) * multB
        val m21 = (invSat * lg) * multB
        val m22 = (invSat * lb + sat) * multB

        // Hue shift matrix rotation if non-zero
        val finalValues = if (settings.hueShift != 0f) {
            val angle = Math.toRadians(settings.hueShift.toDouble())
            val cosA = cos(angle).toFloat()
            val sinA = sin(angle).toFloat()
            floatArrayOf(
                (0.213f + cosA * 0.787f - sinA * 0.213f) * m00,
                (0.715f - cosA * 0.715f - sinA * 0.715f) * m01,
                (0.072f - cosA * 0.072f + sinA * 0.928f) * m02,
                0f, offR,

                (0.213f - cosA * 0.213f + sinA * 0.143f) * m10,
                (0.715f + cosA * 0.285f + sinA * 0.140f) * m11,
                (0.072f - cosA * 0.072f - sinA * 0.283f) * m12,
                0f, offG,

                (0.213f - cosA * 0.213f - sinA * 0.787f) * m20,
                (0.715f - cosA * 0.715f + sinA * 0.715f) * m21,
                (0.072f + cosA * 0.928f + sinA * 0.072f) * m22,
                0f, offB,

                0f, 0f, 0f, 1f, 0f
            )
        } else {
            floatArrayOf(
                m00, m01, m02, 0f, offR,
                m10, m11, m12, 0f, offG,
                m20, m21, m22, 0f, offB,
                0f,  0f,  0f,  1f, 0f
            )
        }

        matrix.set(ColorMatrix(finalValues))
        return matrix
    }

    private fun wheelToRgbDeltas(hue: Float, saturation: Float, luminance: Float): Triple<Float, Float, Float> {
        if (saturation == 0f && luminance == 0f) return Triple(0f, 0f, 0f)
        val rad = Math.toRadians(hue.toDouble())
        // Polar to RGB balance
        val rOffset = cos(rad).toFloat() * saturation + luminance
        val gOffset = cos(rad - 2.0944).toFloat() * saturation + luminance // -120 deg
        val bOffset = cos(rad + 2.0944).toFloat() * saturation + luminance // +120 deg
        return Triple(rOffset, gOffset, bOffset)
    }

    /**
     * Generates simulated representative RGB Histogram distributions
     * responsive to the active color grade settings (for real-time Scope display).
     */
    fun computeSimulatedHistogram(settings: ColorGradeSettings): HistogramData {
        val binCount = 48
        val rBins = FloatArray(binCount)
        val gBins = FloatArray(binCount)
        val bBins = FloatArray(binCount)

        // Center offsets influenced by lift/gamma/gain/exposure
        val expShift = (settings.exposure / 100f) * 8f
        val tempShift = (settings.temperature / 100f) * 6f
        val tintShift = (settings.tint / 100f) * 5f
        val contrast = 1f + (settings.contrast / 100f) * 0.5f

        val centerR = (24f + expShift + tempShift + tintShift).coerceIn(4f, 44f)
        val centerG = (24f + expShift - tintShift).coerceIn(4f, 44f)
        val centerB = (24f + expShift - tempShift + tintShift).coerceIn(4f, 44f)

        for (i in 0 until binCount) {
            val distR = (i - centerR) / (10f / contrast)
            val distG = (i - centerG) / (10f / contrast)
            val distB = (i - centerB) / (10f / contrast)

            rBins[i] = (kotlin.math.exp(-(distR * distR)) * 0.85f + 0.1f * kotlin.math.sin(i * 0.4f).coerceAtLeast(0f)).coerceIn(0.05f, 1f)
            gBins[i] = (kotlin.math.exp(-(distG * distG)) * 0.90f + 0.08f * kotlin.math.sin(i * 0.35f).coerceAtLeast(0f)).coerceIn(0.05f, 1f)
            bBins[i] = (kotlin.math.exp(-(distB * distB)) * 0.82f + 0.09f * kotlin.math.cos(i * 0.45f).coerceAtLeast(0f)).coerceIn(0.05f, 1f)
        }

        return HistogramData(rBins, gBins, bBins)
    }
}

data class HistogramData(
    val red: FloatArray,
    val green: FloatArray,
    val blue: FloatArray
)
