package com.example.audio

import java.io.ByteArrayOutputStream
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * Pure-Kotlin procedural 16-bit PCM synthesizer for crypto mining game sound effects.
 * Produces smooth, click-free WAV buffers for SoundPool without external binary audio assets.
 */
object SoundSynthesizer {
    const val SAMPLE_RATE = 22050

    /**
     * Crisp metallic pickaxe / gold coin clink for manual mining taps (~85ms).
     */
    fun generateMineTapPcm(sampleRate: Int = SAMPLE_RATE): ShortArray {
        val durationSec = 0.085
        val numSamples = (sampleRate * durationSec).toInt()
        val samples = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            // Fast 2ms attack to eliminate speaker pop
            val attack = (t / 0.002).coerceIn(0.0, 1.0)

            // Percussive strike transient (rapidly decaying high-mid thump)
            val strikeEnv = exp(-t * 110.0)
            val strike = sin(2.0 * PI * 320.0 * t) * 0.45 +
                sin(2.0 * PI * 640.0 * t) * 0.25

            // Bright metallic golden ring (fundamental + harmonic overtone)
            val ringEnv = exp(-t * 42.0)
            val ring = sin(2.0 * PI * 1480.0 * t) * 0.55 +
                sin(2.0 * PI * 2960.0 * t) * 0.25

            val combined = attack * (strike * strikeEnv + ring * ringEnv) * 0.58
            samples[i] = (combined.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
        }
        return samples
    }

    /**
     * Uplifting ascending arpeggio chime for upgrade & miner hire completion (~360ms).
     */
    fun generateUpgradeCompletePcm(sampleRate: Int = SAMPLE_RATE): ShortArray {
        val durationSec = 0.36
        val numSamples = (sampleRate * durationSec).toInt()
        val samples = ShortArray(numSamples)

        // Bright C-major ascending arpeggio: C5, E5, G5, C6
        val notes = doubleArrayOf(523.25, 659.25, 783.99, 1046.50)
        val noteStarts = doubleArrayOf(0.0, 0.065, 0.13, 0.195)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            var mix = 0.0

            for (n in notes.indices) {
                val localT = t - noteStarts[n]
                if (localT >= 0.0) {
                    val attack = (localT / 0.004).coerceIn(0.0, 1.0)
                    val decayRate = if (n == notes.lastIndex) 12.0 else 18.0
                    val env = attack * exp(-localT * decayRate)
                    val freq = notes[n]
                    val tone = sin(2.0 * PI * freq * localT) * 0.72 +
                        sin(2.0 * PI * freq * 2.0 * localT) * 0.28
                    mix += tone * env * 0.38
                }
            }

            samples[i] = (mix.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
        }
        return samples
    }

    /**
     * Shimmering reward chime for daily crypto bonus, block mined, and task claims (~420ms).
     */
    fun generateRewardChimePcm(sampleRate: Int = SAMPLE_RATE): ShortArray {
        val durationSec = 0.42
        val numSamples = (sampleRate * durationSec).toInt()
        val samples = ShortArray(numSamples)

        // Sparkling D-major chord cascade: D5 -> F#5 -> A5 -> D6
        val notes = doubleArrayOf(587.33, 739.99, 880.00, 1174.66)
        val noteStarts = doubleArrayOf(0.0, 0.055, 0.11, 0.165)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            var mix = 0.0

            for (n in notes.indices) {
                val localT = t - noteStarts[n]
                if (localT >= 0.0) {
                    val attack = (localT / 0.004).coerceIn(0.0, 1.0)
                    val decay = if (n == notes.lastIndex) 9.5 else 15.0
                    val env = attack * exp(-localT * decay)
                    val freq = notes[n]
                    val tone = sin(2.0 * PI * freq * localT) * 0.65 +
                        sin(2.0 * PI * freq * 2.0 * localT) * 0.25 +
                        sin(2.0 * PI * freq * 3.0 * localT) * 0.10
                    mix += tone * env * 0.36
                }
            }

            samples[i] = (mix.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
        }
        return samples
    }

    /**
     * Subtle low muted double-thud for insufficient balance or invalid action (~120ms).
     */
    fun generateErrorThudPcm(sampleRate: Int = SAMPLE_RATE): ShortArray {
        val durationSec = 0.12
        val numSamples = (sampleRate * durationSec).toInt()
        val samples = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val attack = (t / 0.003).coerceIn(0.0, 1.0)
            val env = attack * exp(-t * 28.0)
            val tone = sin(2.0 * PI * 165.0 * t) * 0.65 + sin(2.0 * PI * 110.0 * t) * 0.35
            val combined = tone * env * 0.45
            samples[i] = (combined.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
        }
        return samples
    }

    /**
     * Soft UI pop for audio confirmation toggle (~35ms).
     */
    fun generateUiTickPcm(sampleRate: Int = SAMPLE_RATE): ShortArray {
        val durationSec = 0.035
        val numSamples = (sampleRate * durationSec).toInt()
        val samples = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val attack = (t / 0.002).coerceIn(0.0, 1.0)
            val env = attack * exp(-t * 85.0)
            val tone = sin(2.0 * PI * 880.0 * t)
            val combined = tone * env * 0.40
            samples[i] = (combined.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
        }
        return samples
    }

    /**
     * Encodes 16-bit mono PCM samples into a standard RIFF/WAV byte array.
     */
    fun wrapPcmAsWav(pcm: ShortArray, sampleRate: Int = SAMPLE_RATE): ByteArray {
        val dataByteSize = pcm.size * 2
        val totalSize = 44 + dataByteSize
        val out = ByteArrayOutputStream(totalSize)

        fun writeString(s: String) {
            for (ch in s) out.write(ch.code)
        }

        fun writeIntLE(v: Int) {
            out.write(v and 0xFF)
            out.write((v shr 8) and 0xFF)
            out.write((v shr 16) and 0xFF)
            out.write((v shr 24) and 0xFF)
        }

        fun writeShortLE(v: Int) {
            out.write(v and 0xFF)
            out.write((v shr 8) and 0xFF)
        }

        // RIFF header
        writeString("RIFF")
        writeIntLE(36 + dataByteSize)
        writeString("WAVE")

        // fmt sub-chunk
        writeString("fmt ")
        writeIntLE(16) // PCM subchunk size
        writeShortLE(1) // PCM audio format
        writeShortLE(1) // Mono
        writeIntLE(sampleRate)
        writeIntLE(sampleRate * 2) // Byte rate (SampleRate * NumChannels * BitsPerSample/8)
        writeShortLE(2) // Block align
        writeShortLE(16) // Bits per sample

        // data sub-chunk
        writeString("data")
        writeIntLE(dataByteSize)
        for (sample in pcm) {
            val v = sample.toInt()
            out.write(v and 0xFF)
            out.write((v shr 8) and 0xFF)
        }

        return out.toByteArray()
    }
}
