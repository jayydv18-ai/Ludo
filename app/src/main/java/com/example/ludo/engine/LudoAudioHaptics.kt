package com.example.ludo.engine

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.ToneGenerator
import android.media.AudioManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

class LudoAudioHaptics(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.Default)
    private var toneGenerator: ToneGenerator? = null
    private var vibrator: Vibrator? = null

    var isSoundEnabled: Boolean = true
    var isMusicEnabled: Boolean = true
    var isVibrationEnabled: Boolean = true

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 80)
        } catch (_: Exception) {}

        try {
            vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (_: Exception) {}
    }

    fun playButtonClick() {
        if (!isSoundEnabled) return
        scope.launch {
            playTone(800.0, 40)
        }
        vibrate(20)
    }

    fun playDiceRoll() {
        if (!isSoundEnabled) return
        scope.launch {
            // Rapid clicking tones simulating dice tumbling
            val freqs = listOf(500.0, 750.0, 600.0, 900.0, 450.0, 850.0)
            for (f in freqs) {
                playTone(f, 35)
                kotlinx.coroutines.delay(40)
            }
        }
        vibrate(40)
    }

    fun playTokenMove() {
        if (!isSoundEnabled) return
        scope.launch {
            playTone(650.0, 60)
        }
        vibrate(25)
    }

    fun playCapture() {
        if (!isSoundEnabled) return
        scope.launch {
            // Dramatic explosion / capture sequence
            playTone(400.0, 80)
            kotlinx.coroutines.delay(90)
            playTone(250.0, 140)
        }
        vibratePattern(longArrayOf(0, 50, 50, 100))
    }

    fun playTurnAlert() {
        if (!isSoundEnabled) return
        scope.launch {
            playTone(523.25, 70) // C5
            kotlinx.coroutines.delay(80)
            playTone(659.25, 100) // E5
        }
        vibrate(45)
    }

    fun playVictory() {
        if (!isSoundEnabled) return
        scope.launch {
            val victoryNotes = listOf(
                523.25 to 100L, // C5
                659.25 to 100L, // E5
                783.99 to 100L, // G5
                1046.50 to 250L // C6
            )
            for ((freq, dur) in victoryNotes) {
                playTone(freq, dur.toInt())
                kotlinx.coroutines.delay(dur + 20)
            }
        }
        vibratePattern(longArrayOf(0, 80, 80, 80, 80, 200))
    }

    private fun playTone(freqHz: Double, durationMs: Int) {
        try {
            val sampleRate = 22050
            val numSamples = (sampleRate * (durationMs / 1000.0)).toInt().coerceAtLeast(1)
            val buffer = ShortArray(numSamples)

            for (i in 0 until numSamples) {
                val angle = 2.0 * Math.PI * i / (sampleRate / freqHz)
                val envelope = 1.0 - (i.toDouble() / numSamples) // gentle fade out
                buffer[i] = (sin(angle) * Short.MAX_VALUE * 0.6 * envelope).toInt().toShort()
            }

            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(numSamples * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(buffer, 0, numSamples)
            audioTrack.play()

            // Release after playing
            scope.launch {
                kotlinx.coroutines.delay(durationMs.toLong() + 100)
                try {
                    audioTrack.release()
                } catch (_: Exception) {}
            }
        } catch (_: Exception) {
            // Fallback to ToneGenerator if AudioTrack isn't available
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, durationMs)
            } catch (_: Exception) {}
        }
    }

    private fun vibrate(durationMs: Long) {
        if (!isVibrationEnabled || vibrator == null) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMs)
            }
        } catch (_: Exception) {}
    }

    private fun vibratePattern(pattern: LongArray) {
        if (!isVibrationEnabled || vibrator == null) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(pattern, -1)
            }
        } catch (_: Exception) {}
    }

    fun release() {
        try {
            toneGenerator?.release()
            toneGenerator = null
        } catch (_: Exception) {}
    }
}
