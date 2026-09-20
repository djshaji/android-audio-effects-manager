package com.example.service

import android.content.Context
import android.media.AudioManager
import android.media.audiofx.BassBoost
import android.media.audiofx.DynamicsProcessing
import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import android.media.audiofx.Virtualizer
import android.os.Build
import android.util.Log
import com.example.model.AudioEffectsConfig
import java.util.concurrent.ConcurrentHashMap

/**
 * Core DSP engine that creates and configures Android AudioEffects on system audio sessions.
 * Manages AudioSession 0 (system output mix) as well as any active audio sessions.
 */
class AudioEffectsEngine(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    // Session-specific effect holders
    private val sessions = ConcurrentHashMap<Int, SessionEffects>()

    // Track active configuration
    @Volatile
    var currentConfig: AudioEffectsConfig = AudioEffectsConfig()
        private set

    init {
        // Initialize for session 0 (system wide global mix)
        attachSession(0)
    }

    class SessionEffects(val sessionId: Int) {
        var equalizer: Equalizer? = null
        var bassBoost: BassBoost? = null
        var virtualizer: Virtualizer? = null
        var loudnessEnhancer: LoudnessEnhancer? = null
        var dynamicsProcessing: DynamicsProcessing? = null
        var isDynamicsSupported: Boolean = true

        fun release() {
            try { equalizer?.release() } catch (_: Exception) {}
            try { bassBoost?.release() } catch (_: Exception) {}
            try { virtualizer?.release() } catch (_: Exception) {}
            try { loudnessEnhancer?.release() } catch (_: Exception) {}
            try { dynamicsProcessing?.release() } catch (_: Exception) {}
            equalizer = null
            bassBoost = null
            virtualizer = null
            loudnessEnhancer = null
            dynamicsProcessing = null
        }
    }

    fun attachSession(sessionId: Int) {
        if (sessions.containsKey(sessionId)) return

        val effects = SessionEffects(sessionId)
        try {
            effects.equalizer = Equalizer(1000, sessionId)
        } catch (e: Exception) {
            Log.w("AudioEffectsEngine", "Could not create Equalizer for session $sessionId: ${e.message}")
        }

        try {
            effects.bassBoost = BassBoost(1000, sessionId)
        } catch (e: Exception) {
            Log.w("AudioEffectsEngine", "Could not create BassBoost for session $sessionId: ${e.message}")
        }

        try {
            effects.virtualizer = Virtualizer(1000, sessionId)
        } catch (e: Exception) {
            Log.w("AudioEffectsEngine", "Could not create Virtualizer for session $sessionId: ${e.message}")
        }

        try {
            effects.loudnessEnhancer = LoudnessEnhancer(sessionId)
        } catch (e: Exception) {
            Log.w("AudioEffectsEngine", "Could not create LoudnessEnhancer for session $sessionId: ${e.message}")
        }

        // DynamicsProcessing is available on Android 9+ (API 28+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                // Initialize default DynamicsProcessing config
                val configBuilder = DynamicsProcessing.Config.Builder(
                    DynamicsProcessing.VARIANT_FAVOR_FREQUENCY_RESOLUTION,
                    2, // 2 channels (stereo)
                    false, // preEq
                    0,
                    true, // mbc (compressor) in use
                    1, // 1 broadband compressor band
                    false, // postEq
                    0,
                    true // limiter in use
                )
                effects.dynamicsProcessing = DynamicsProcessing(1000, sessionId, configBuilder.build())
            } catch (e: Exception) {
                effects.isDynamicsSupported = false
                Log.w("AudioEffectsEngine", "DynamicsProcessing not supported on session $sessionId: ${e.message}")
            }
        }

        sessions[sessionId] = effects
        applyConfigToSession(effects, currentConfig)
    }

    fun detachSession(sessionId: Int) {
        if (sessionId == 0) return // Keep system mix session 0 active
        sessions.remove(sessionId)?.release()
    }

    fun applyConfig(config: AudioEffectsConfig) {
        currentConfig = config
        for ((_, sessionEffects) in sessions) {
            applyConfigToSession(sessionEffects, config)
        }
    }

    private fun applyConfigToSession(effects: SessionEffects, config: AudioEffectsConfig) {
        val masterOn = config.masterEnabled && !config.bypassAll

        // 1. Equalizer
        effects.equalizer?.let { eq ->
            try {
                val shouldEnable = masterOn && config.equalizerEnabled
                if (eq.enabled != shouldEnable) {
                    eq.enabled = shouldEnable
                }
                if (shouldEnable) {
                    val numBands = eq.numberOfBands.toInt().coerceAtMost(config.eqBandLevels.size)
                    val range = eq.bandLevelRange // short[2] min and max in mB
                    val minMb = range?.getOrNull(0)?.toInt() ?: -1500
                    val maxMb = range?.getOrNull(1)?.toInt() ?: 1500

                    for (i in 0 until numBands) {
                        val level = config.eqBandLevels.getOrElse(i) { 0 }.coerceIn(minMb, maxMb)
                        eq.setBandLevel(i.toShort(), level.toShort())
                    }
                }
            } catch (e: Exception) {
                Log.w("AudioEffectsEngine", "Equalizer update failed: ${e.message}")
            }
        }

        // 2. Bass Booster
        effects.bassBoost?.let { bb ->
            try {
                val shouldEnable = masterOn && config.bassBoostEnabled
                if (bb.enabled != shouldEnable) {
                    bb.enabled = shouldEnable
                }
                if (shouldEnable) {
                    bb.setStrength(config.bassBoostStrength.toShort().coerceIn(0, 1000))
                }
            } catch (e: Exception) {
                Log.w("AudioEffectsEngine", "BassBoost update failed: ${e.message}")
            }
        }

        // 3. Virtual Surround (Virtualizer)
        effects.virtualizer?.let { virt ->
            try {
                val shouldEnable = masterOn && config.virtualizerEnabled
                if (virt.enabled != shouldEnable) {
                    virt.enabled = shouldEnable
                }
                if (shouldEnable) {
                    virt.setStrength(config.virtualizerStrength.toShort().coerceIn(0, 1000))
                }
            } catch (e: Exception) {
                Log.w("AudioEffectsEngine", "Virtualizer update failed: ${e.message}")
            }
        }

        // 4. Normalizer (Loudness Enhancer)
        effects.loudnessEnhancer?.let { le ->
            try {
                val shouldEnable = masterOn && config.normalizerEnabled
                if (le.enabled != shouldEnable) {
                    le.enabled = shouldEnable
                }
                if (shouldEnable) {
                    le.setTargetGain(config.normalizerGainMb.coerceIn(0, 3000))
                }
            } catch (e: Exception) {
                Log.w("AudioEffectsEngine", "LoudnessEnhancer update failed: ${e.message}")
            }
        }

        // 5. DynamicsProcessing (Compressor, Limiter, Master Gain)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && effects.isDynamicsSupported) {
            effects.dynamicsProcessing?.let { dp ->
                try {
                    val shouldEnable = masterOn && (config.compressorEnabled || config.limiterEnabled || config.masterGainDb != 0.0f)
                    if (dp.enabled != shouldEnable) {
                        dp.enabled = shouldEnable
                    }

                    if (shouldEnable) {
                        // Master Gain
                        dp.setInputGainAllChannelsTo(config.masterGainDb)

                        // Compressor (Multi-band compressor stage)
                        val mbcBand = DynamicsProcessing.MbcBand(
                            config.compressorEnabled,
                            20000.0f, // cutoffFrequency
                            config.compressorAttackMs,
                            config.compressorReleaseMs,
                            config.compressorRatio,
                            config.compressorThresholdDb,
                            0.0f, // kneeWidth
                            0.0f, // noiseGateThreshold
                            1.0f, // expanderRatio
                            0.0f, // preGain
                            config.compressorMakeupGainDb // postGain
                        )
                        dp.setMbcBandAllChannelsTo(0, mbcBand)

                        // Limiter stage
                        val limiter = DynamicsProcessing.Limiter(
                            true, // inUse
                            config.limiterEnabled,
                            0, // linkGroup
                            config.limiterAttackMs,
                            config.limiterReleaseMs,
                            config.limiterRatio,
                            config.limiterThresholdDb,
                            config.limiterPostGainDb
                        )
                        dp.setLimiterAllChannelsTo(limiter)
                    }
                } catch (e: Exception) {
                    Log.w("AudioEffectsEngine", "DynamicsProcessing parameter update failed: ${e.message}")
                }
            }
        }
    }

    fun release() {
        for ((_, effects) in sessions) {
            effects.release()
        }
        sessions.clear()
    }
}
