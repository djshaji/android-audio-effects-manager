package com.example.model

/**
 * Complete DSP and audio effects configuration.
 * System volume is left untouched and managed independently by the device volume keys.
 */
data class AudioEffectsConfig(
    // Master DSP controls
    val masterEnabled: Boolean = true,
    val masterGainDb: Float = 0.0f,            // -24.0 dB to +18.0 dB
    val balance: Float = 0.0f,                 // -1.0f (Left) to +1.0f (Right)
    val bypassAll: Boolean = false,            // For A/B testing

    // Normalizer (Loudness Enhancer / Target Normalization)
    val normalizerEnabled: Boolean = true,
    val normalizerGainMb: Int = 600,           // Target gain: 0 to 2000 mB (0.0 to +20.0 dB)

    // Audio Compressor
    val compressorEnabled: Boolean = true,
    val compressorThresholdDb: Float = -16.0f, // -40.0 dB to 0.0 dB
    val compressorRatio: Float = 4.0f,         // 1.0 to 20.0 (e.g. 4:1)
    val compressorAttackMs: Float = 20.0f,     // 1.0 ms to 200.0 ms
    val compressorReleaseMs: Float = 120.0f,   // 10.0 ms to 1000.0 ms
    val compressorMakeupGainDb: Float = 3.0f,  // 0.0 dB to +18.0 dB

    // Audio Limiter
    val limiterEnabled: Boolean = true,
    val limiterThresholdDb: Float = -0.5f,     // -12.0 dB to 0.0 dB (Ceiling)
    val limiterAttackMs: Float = 1.5f,         // 0.1 ms to 20.0 ms
    val limiterReleaseMs: Float = 50.0f,       // 5.0 ms to 300.0 ms
    val limiterRatio: Float = 50.0f,           // 10.0 to 100.0 (Hard brickwall)
    val limiterPostGainDb: Float = 0.0f,       // Post-limiter trim (-6 to +6 dB)

    // Virtual Surround
    val virtualizerEnabled: Boolean = false,
    val virtualizerStrength: Int = 350,        // 0 to 1000

    // Bass Booster
    val bassBoostEnabled: Boolean = true,
    val bassBoostStrength: Int = 300,          // 0 to 1000

    // 5-Band Equalizer (mB values, typically -1500 to +1500 mB: -15dB to +15dB)
    val equalizerEnabled: Boolean = true,
    val eqBandLevels: List<Int> = listOf(0, 0, 0, 0, 0), // 60Hz, 230Hz, 910Hz, 3.6kHz, 14kHz

    // Active Preset metadata
    val activePresetId: Long = 1L,
    val activePresetName: String = "Studio Reference"
)
