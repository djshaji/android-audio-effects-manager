package com.example.service

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

enum class PreviewTrack(val title: String, val subtitle: String) {
    ELECTRO_GROOVE("Electro Bassline", "Tests Bass Boost, Compressor & Limiter punch"),
    CINEMATIC_CHORD("Cinematic Atmosphere", "Tests Virtual Surround & Equalizer stage"),
    PODCAST_VOICE("Voice & Dialogue", "Tests Normalizer & Speech intelligibility"),
    FREQUENCY_SWEEP("Hi-Fi Sweep", "Full spectrum 20Hz - 20kHz harmonic test")
}

/**
 * High quality real-time audio synthesizer for instant testing of all DSP effects.
 * Plays through an AudioTrack on the media stream so native Android AudioEffects process it.
 */
class AudioPreviewSynthesizer {

    private var audioTrack: AudioTrack? = null
    private var synthJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentTrack = MutableStateFlow(PreviewTrack.ELECTRO_GROOVE)
    val currentTrack: StateFlow<PreviewTrack> = _currentTrack.asStateFlow()

    private val _spectrumLevels = MutableStateFlow(listOf(0.2f, 0.2f, 0.2f, 0.2f, 0.2f))
    val spectrumLevels: StateFlow<List<Float>> = _spectrumLevels.asStateFlow()

    private val _vuMeterPeak = MutableStateFlow(0.3f)
    val vuMeterPeak: StateFlow<Float> = _vuMeterPeak.asStateFlow()

    private val _gainReductionDb = MutableStateFlow(0.0f)
    val gainReductionDb: StateFlow<Float> = _gainReductionDb.asStateFlow()

    fun getAudioSessionId(): Int {
        return audioTrack?.audioSessionId ?: 0
    }

    fun selectTrack(track: PreviewTrack) {
        _currentTrack.value = track
        if (_isPlaying.value) {
            stop()
            start()
        }
    }

    fun togglePlay() {
        if (_isPlaying.value) {
            stop()
        } else {
            start()
        }
    }

    fun start() {
        if (_isPlaying.value) return

        try {
            val sampleRate = 44100
            val channelConfig = AudioFormat.CHANNEL_OUT_STEREO
            val audioFormat = AudioFormat.ENCODING_PCM_16BIT
            val minBufferSize = AudioTrack.getMinBufferSize(sampleRate, channelConfig, audioFormat)
            val bufferSize = (minBufferSize * 2).coerceAtLeast(8192)

            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setSampleRate(sampleRate)
                        .setChannelMask(channelConfig)
                        .setEncoding(audioFormat)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            audioTrack = track
            track.play()
            _isPlaying.value = true

            synthJob = scope.launch {
                synthesizeLoop(track, sampleRate, bufferSize)
            }
        } catch (_: Exception) {
            _isPlaying.value = false
        }
    }

    fun stop() {
        _isPlaying.value = false
        synthJob?.cancel()
        synthJob = null
        try {
            audioTrack?.apply {
                pause()
                flush()
                stop()
                release()
            }
        } catch (_: Exception) {
        }
        audioTrack = null
        _spectrumLevels.value = listOf(0.05f, 0.05f, 0.05f, 0.05f, 0.05f)
        _vuMeterPeak.value = 0.0f
        _gainReductionDb.value = 0.0f
    }

    private suspend fun synthesizeLoop(track: AudioTrack, sampleRate: Int, bufferSize: Int) {
        val shortBuffer = ShortArray(bufferSize / 2)
        var sampleIndex = 0L
        val frames = shortBuffer.size / 2

        while (scope.isActive && _isPlaying.value) {
            val mode = _currentTrack.value
            var peakLevel = 0.0f

            for (i in 0 until frames) {
                val t = (sampleIndex + i).toDouble() / sampleRate.toDouble()
                var left = 0.0
                var right = 0.0

                when (mode) {
                    PreviewTrack.ELECTRO_GROOVE -> {
                        // 120 BPM Kick + Sub-bass (55 Hz - 110 Hz) + Hi-hat (8 kHz) + Synth chords
                        val beatT = (t * 2.0) % 1.0
                        val kickDecay = (1.0 - beatT).coerceAtLeast(0.0)
                        val kick = sin(2.0 * PI * (55.0 + 80.0 * kickDecay) * t) * kickDecay * kickDecay
                        val bass = sin(2.0 * PI * 65.4 * t) * 0.45
                        val chord = (sin(2.0 * PI * 261.6 * t) + sin(2.0 * PI * 329.6 * t) + sin(2.0 * PI * 392.0 * t)) * 0.15
                        val hihat = ((Math.random() - 0.5) * 2.0) * if (beatT in 0.5..0.65) 0.25 else 0.05
                        left = kick * 0.7 + bass * 0.5 + chord * 0.8 + hihat * 0.4
                        right = kick * 0.7 + bass * 0.5 + chord * 0.9 - hihat * 0.4
                    }
                    PreviewTrack.CINEMATIC_CHORD -> {
                        // Deep evolving lush spatial pads with subtle binaural panning
                        val chord1 = sin(2.0 * PI * 130.81 * t) * 0.3
                        val chord2 = sin(2.0 * PI * 196.00 * t) * 0.25
                        val chord3 = sin(2.0 * PI * 293.66 * t) * 0.2
                        val chord4 = sin(2.0 * PI * 440.00 * t) * 0.15
                        val panL = 0.5 + 0.4 * sin(2.0 * PI * 0.25 * t)
                        val panR = 1.0 - panL
                        left = (chord1 + chord2 + chord3 + chord4) * panL
                        right = (chord1 + chord2 + chord3 + chord4) * panR
                    }
                    PreviewTrack.PODCAST_VOICE -> {
                        // Simulated speech formant fundamentals (120Hz fundamental with 700Hz and 2400Hz vowel resonance)
                        val speechEnvelope = (sin(2.0 * PI * 1.8 * t) * 0.5 + 0.5).coerceIn(0.1, 0.95)
                        val f0 = sin(2.0 * PI * 130.0 * t) * 0.35
                        val f1 = sin(2.0 * PI * 680.0 * t) * 0.4
                        val f2 = sin(2.0 * PI * 2100.0 * t) * 0.25
                        val voice = (f0 + f1 + f2) * speechEnvelope
                        left = voice
                        right = voice
                    }
                    PreviewTrack.FREQUENCY_SWEEP -> {
                        // Smooth logarithmic sweep 40Hz to 16kHz
                        val sweepCycle = (t * 0.2) % 1.0
                        val freq = 40.0 * Math.pow(16000.0 / 40.0, sweepCycle)
                        val sig = sin(2.0 * PI * freq * t) * 0.4
                        left = sig
                        right = sig
                    }
                }

                left = left.coerceIn(-1.0, 1.0)
                right = right.coerceIn(-1.0, 1.0)

                val shortL = (left * 32767.0).toInt().toShort()
                val shortR = (right * 32767.0).toInt().toShort()

                shortBuffer[i * 2] = shortL
                shortBuffer[i * 2 + 1] = shortR

                val amp = Math.max(Math.abs(left), Math.abs(right)).toFloat()
                if (amp > peakLevel) peakLevel = amp
            }

            sampleIndex += frames
            track.write(shortBuffer, 0, shortBuffer.size)

            // Update UI spectrum and meters
            val baseTime = (sampleIndex / 44100.0).toFloat()
            val b0 = (0.35f + 0.45f * sin(baseTime * 4.0f).coerceAtLeast(0f) * peakLevel).coerceIn(0.05f, 1f)
            val b1 = (0.25f + 0.5f * sin(baseTime * 5.2f + 1.2f).coerceAtLeast(0f) * peakLevel).coerceIn(0.05f, 1f)
            val b2 = (0.30f + 0.4f * sin(baseTime * 6.5f + 2.1f).coerceAtLeast(0f) * peakLevel).coerceIn(0.05f, 1f)
            val b3 = (0.20f + 0.45f * sin(baseTime * 7.8f + 0.8f).coerceAtLeast(0f) * peakLevel).coerceIn(0.05f, 1f)
            val b4 = (0.15f + 0.55f * sin(baseTime * 9.1f + 3.0f).coerceAtLeast(0f) * peakLevel).coerceIn(0.05f, 1f)

            _spectrumLevels.value = listOf(b0, b1, b2, b3, b4)
            _vuMeterPeak.value = peakLevel.coerceIn(0f, 1f)

            // Gain reduction calculation simulated from peak vs limiter/compressor
            val gr = if (peakLevel > 0.65f) (peakLevel - 0.65f) * 12.0f else 0.0f
            _gainReductionDb.value = gr
        }
    }
}
