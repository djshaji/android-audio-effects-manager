package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.AudioEffectsConfig

@Entity(tableName = "presets")
data class PresetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val scenario: String,           // e.g., "Reference", "Club / Bass", "Cinema / Dialogue", "Vocal Clarity", "Late Night", "Podcasts", "Gaming", "Custom"
    val description: String,
    val iconTag: String,            // e.g. "headphones", "speaker", "movie", "mic", "night", "game", "audiophile", "custom"
    val isFactory: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),

    // DSP Parameters
    val masterGainDb: Float = 0.0f,
    val fineVolumePercent: Float = 0.75f,
    val balance: Float = 0.0f,

    // Normalizer
    val normalizerEnabled: Boolean = true,
    val normalizerGainMb: Int = 600,

    // Compressor
    val compressorEnabled: Boolean = true,
    val compressorThresholdDb: Float = -16.0f,
    val compressorRatio: Float = 4.0f,
    val compressorAttackMs: Float = 20.0f,
    val compressorReleaseMs: Float = 120.0f,
    val compressorMakeupGainDb: Float = 3.0f,

    // Limiter
    val limiterEnabled: Boolean = true,
    val limiterThresholdDb: Float = -0.5f,
    val limiterAttackMs: Float = 1.5f,
    val limiterReleaseMs: Float = 50.0f,
    val limiterRatio: Float = 50.0f,
    val limiterPostGainDb: Float = 0.0f,

    // Virtual Surround
    val virtualizerEnabled: Boolean = false,
    val virtualizerStrength: Int = 350,

    // Bass Booster
    val bassBoostEnabled: Boolean = true,
    val bassBoostStrength: Int = 300,

    // Equalizer (stored as comma-separated string for simplicity and reliable serialization)
    val eqBandLevelsCsv: String = "0,0,0,0,0"
) {
    fun toAudioEffectsConfig(masterEnabled: Boolean = true): AudioEffectsConfig {
        val bands = try {
            eqBandLevelsCsv.split(",").map { it.trim().toInt() }
        } catch (_: Exception) {
            listOf(0, 0, 0, 0, 0)
        }
        val safeBands = if (bands.size >= 5) bands.take(5) else (bands + List(5 - bands.size) { 0 })

        return AudioEffectsConfig(
            masterEnabled = masterEnabled,
            masterGainDb = masterGainDb,
            fineVolumePercent = fineVolumePercent,
            balance = balance,
            normalizerEnabled = normalizerEnabled,
            normalizerGainMb = normalizerGainMb,
            compressorEnabled = compressorEnabled,
            compressorThresholdDb = compressorThresholdDb,
            compressorRatio = compressorRatio,
            compressorAttackMs = compressorAttackMs,
            compressorReleaseMs = compressorReleaseMs,
            compressorMakeupGainDb = compressorMakeupGainDb,
            limiterEnabled = limiterEnabled,
            limiterThresholdDb = limiterThresholdDb,
            limiterAttackMs = limiterAttackMs,
            limiterReleaseMs = limiterReleaseMs,
            limiterRatio = limiterRatio,
            limiterPostGainDb = limiterPostGainDb,
            virtualizerEnabled = virtualizerEnabled,
            virtualizerStrength = virtualizerStrength,
            bassBoostEnabled = bassBoostEnabled,
            bassBoostStrength = bassBoostStrength,
            eqBandLevels = safeBands,
            activePresetId = id,
            activePresetName = name
        )
    }

    companion object {
        fun fromConfig(
            config: AudioEffectsConfig,
            name: String,
            scenario: String,
            description: String,
            iconTag: String = "custom",
            isFactory: Boolean = false
        ): PresetEntity {
            return PresetEntity(
                name = name,
                scenario = scenario,
                description = description,
                iconTag = iconTag,
                isFactory = isFactory,
                masterGainDb = config.masterGainDb,
                fineVolumePercent = config.fineVolumePercent,
                balance = config.balance,
                normalizerEnabled = config.normalizerEnabled,
                normalizerGainMb = config.normalizerGainMb,
                compressorEnabled = config.compressorEnabled,
                compressorThresholdDb = config.compressorThresholdDb,
                compressorRatio = config.compressorRatio,
                compressorAttackMs = config.compressorAttackMs,
                compressorReleaseMs = config.compressorReleaseMs,
                compressorMakeupGainDb = config.compressorMakeupGainDb,
                limiterEnabled = config.limiterEnabled,
                limiterThresholdDb = config.limiterThresholdDb,
                limiterAttackMs = config.limiterAttackMs,
                limiterReleaseMs = config.limiterReleaseMs,
                limiterRatio = config.limiterRatio,
                limiterPostGainDb = config.limiterPostGainDb,
                virtualizerEnabled = config.virtualizerEnabled,
                virtualizerStrength = config.virtualizerStrength,
                bassBoostEnabled = config.bassBoostEnabled,
                bassBoostStrength = config.bassBoostStrength,
                eqBandLevelsCsv = config.eqBandLevels.joinToString(",")
            )
        }
    }
}
