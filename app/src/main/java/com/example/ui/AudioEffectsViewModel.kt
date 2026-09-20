package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.PresetEntity
import com.example.data.PresetRepository
import com.example.model.AudioEffectsConfig
import com.example.service.AudioEffectsEngine
import com.example.service.AudioEffectsService
import com.example.service.AudioPreviewSynthesizer
import com.example.service.PreviewTrack
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AudioTab(val title: String) {
    MASTER("Master Gain"),
    EQ_BASS("EQ & Bass"),
    DYNAMICS("Dynamics & Limiter"),
    PRESETS("Scenarios")
}

data class AudioEffectsUiState(
    val config: AudioEffectsConfig = AudioEffectsConfig(),
    val presets: List<PresetEntity> = emptyList(),
    val isPlayingPreview: Boolean = false,
    val previewTrack: PreviewTrack = PreviewTrack.ELECTRO_GROOVE,
    val spectrumLevels: List<Float> = listOf(0.1f, 0.1f, 0.1f, 0.1f, 0.1f),
    val vuMeterPeak: Float = 0.0f,
    val gainReductionDb: Float = 0.0f,
    val activeTab: AudioTab = AudioTab.MASTER,
    val showSavePresetDialog: Boolean = false,
    val userMessage: String? = null
)

private data class SynthUiState(
    val isPlaying: Boolean,
    val track: PreviewTrack,
    val spectrum: List<Float>,
    val vu: Float,
    val gr: Float
)

class AudioEffectsViewModel(
    application: Application,
    private val repository: PresetRepository
) : AndroidViewModel(application) {

    private val synthesizer = AudioPreviewSynthesizer()
    private val fallbackEngine = AudioEffectsEngine(application.applicationContext)

    private val _config = MutableStateFlow(AudioEffectsConfig())
    private val _activeTab = MutableStateFlow(AudioTab.MASTER)
    private val _showSavePresetDialog = MutableStateFlow(false)
    private val _userMessage = MutableStateFlow<String?>(null)

    val presets: StateFlow<List<PresetEntity>> = repository.allPresets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val synthState = combine(
        synthesizer.isPlaying,
        synthesizer.currentTrack,
        synthesizer.spectrumLevels,
        synthesizer.vuMeterPeak,
        synthesizer.gainReductionDb
    ) { isPlaying, track, spectrum, vu, gr ->
        SynthUiState(isPlaying, track, spectrum, vu, gr)
    }

    private val controlState = combine(
        _activeTab,
        _showSavePresetDialog,
        _userMessage
    ) { tab, showSave, msg ->
        Triple(tab, showSave, msg)
    }

    val uiState: StateFlow<AudioEffectsUiState> = combine(
        _config,
        presets,
        synthState,
        controlState
    ) { config, presetList, synth, controls ->
        AudioEffectsUiState(
            config = config,
            presets = presetList,
            isPlayingPreview = synth.isPlaying,
            previewTrack = synth.track,
            spectrumLevels = synth.spectrum,
            vuMeterPeak = synth.vu,
            gainReductionDb = synth.gr,
            activeTab = controls.first,
            showSavePresetDialog = controls.second,
            userMessage = controls.third
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        AudioEffectsUiState()
    )

    init {
        viewModelScope.launch {
            repository.ensureDefaultPresets()
            applyToEngines(_config.value)
            startForegroundService()
        }
    }

    private fun startForegroundService() {
        val intent = Intent(getApplication(), AudioEffectsService::class.java).apply {
            action = AudioEffectsService.ACTION_START_FOREGROUND
        }
        try {
            getApplication<Application>().startService(intent)
        } catch (_: Exception) {}
    }

    private fun applyToEngines(config: AudioEffectsConfig) {
        AudioEffectsService.instance?.applyConfig(config)
        fallbackEngine.applyConfig(config)
    }

    fun setTab(tab: AudioTab) {
        _activeTab.value = tab
    }

    fun dismissUserMessage() {
        _userMessage.value = null
    }

    fun toggleMasterPower() {
        _config.update { it.copy(masterEnabled = !it.masterEnabled) }
        applyToEngines(_config.value)
    }

    fun toggleBypass() {
        _config.update { it.copy(bypassAll = !it.bypassAll) }
        applyToEngines(_config.value)
        _userMessage.value = if (_config.value.bypassAll) "A/B Bypass Active (Raw Sound)" else "DSP Active"
    }

    fun setMasterGain(gainDb: Float) {
        val clamped = gainDb.coerceIn(-24.0f, 18.0f)
        _config.update { it.copy(masterGainDb = clamped) }
        applyToEngines(_config.value)
    }

    fun setBalance(balance: Float) {
        val clamped = balance.coerceIn(-1.0f, 1.0f)
        _config.update { it.copy(balance = clamped) }
        applyToEngines(_config.value)
    }

    // Normalizer (Loudness Enhancer)
    fun toggleNormalizer() {
        _config.update { it.copy(normalizerEnabled = !it.normalizerEnabled) }
        applyToEngines(_config.value)
    }

    fun setNormalizerGain(gainMb: Int) {
        val clamped = gainMb.coerceIn(0, 3000)
        _config.update { it.copy(normalizerGainMb = clamped) }
        applyToEngines(_config.value)
    }

    // Audio Compressor
    fun toggleCompressor() {
        _config.update { it.copy(compressorEnabled = !it.compressorEnabled) }
        applyToEngines(_config.value)
    }

    fun setCompressorThreshold(db: Float) {
        _config.update { it.copy(compressorThresholdDb = db.coerceIn(-40.0f, 0.0f)) }
        applyToEngines(_config.value)
    }

    fun setCompressorRatio(ratio: Float) {
        _config.update { it.copy(compressorRatio = ratio.coerceIn(1.0f, 20.0f)) }
        applyToEngines(_config.value)
    }

    fun setCompressorAttack(ms: Float) {
        _config.update { it.copy(compressorAttackMs = ms.coerceIn(1.0f, 200.0f)) }
        applyToEngines(_config.value)
    }

    fun setCompressorRelease(ms: Float) {
        _config.update { it.copy(compressorReleaseMs = ms.coerceIn(10.0f, 1000.0f)) }
        applyToEngines(_config.value)
    }

    fun setCompressorMakeupGain(db: Float) {
        _config.update { it.copy(compressorMakeupGainDb = db.coerceIn(0.0f, 18.0f)) }
        applyToEngines(_config.value)
    }

    // Audio Limiter
    fun toggleLimiter() {
        _config.update { it.copy(limiterEnabled = !it.limiterEnabled) }
        applyToEngines(_config.value)
    }

    fun setLimiterThreshold(db: Float) {
        _config.update { it.copy(limiterThresholdDb = db.coerceIn(-12.0f, 0.0f)) }
        applyToEngines(_config.value)
    }

    fun setLimiterAttack(ms: Float) {
        _config.update { it.copy(limiterAttackMs = ms.coerceIn(0.1f, 20.0f)) }
        applyToEngines(_config.value)
    }

    fun setLimiterRelease(ms: Float) {
        _config.update { it.copy(limiterReleaseMs = ms.coerceIn(5.0f, 300.0f)) }
        applyToEngines(_config.value)
    }

    fun setLimiterRatio(ratio: Float) {
        _config.update { it.copy(limiterRatio = ratio.coerceIn(10.0f, 100.0f)) }
        applyToEngines(_config.value)
    }

    fun setLimiterPostGain(db: Float) {
        _config.update { it.copy(limiterPostGainDb = db.coerceIn(-6.0f, 6.0f)) }
        applyToEngines(_config.value)
    }

    // Bass Booster
    fun toggleBassBoost() {
        _config.update { it.copy(bassBoostEnabled = !it.bassBoostEnabled) }
        applyToEngines(_config.value)
    }

    fun setBassBoostStrength(strength: Int) {
        val clamped = strength.coerceIn(0, 1000)
        _config.update { it.copy(bassBoostStrength = clamped) }
        applyToEngines(_config.value)
    }

    // Virtual Surround
    fun toggleVirtualizer() {
        _config.update { it.copy(virtualizerEnabled = !it.virtualizerEnabled) }
        applyToEngines(_config.value)
    }

    fun setVirtualizerStrength(strength: Int) {
        val clamped = strength.coerceIn(0, 1000)
        _config.update { it.copy(virtualizerStrength = clamped) }
        applyToEngines(_config.value)
    }

    // Equalizer
    fun toggleEqualizer() {
        _config.update { it.copy(equalizerEnabled = !it.equalizerEnabled) }
        applyToEngines(_config.value)
    }

    fun setEqBandLevel(bandIndex: Int, levelMb: Int) {
        val clamped = levelMb.coerceIn(-1500, 1500)
        _config.update { current ->
            val bands = current.eqBandLevels.toMutableList()
            if (bandIndex in bands.indices) {
                bands[bandIndex] = clamped
            }
            current.copy(eqBandLevels = bands)
        }
        applyToEngines(_config.value)
    }

    fun resetEqFlat() {
        _config.update { it.copy(eqBandLevels = listOf(0, 0, 0, 0, 0)) }
        applyToEngines(_config.value)
    }

    // Presets
    fun selectPreset(preset: PresetEntity) {
        val masterEnabled = _config.value.masterEnabled
        val newConfig = preset.toAudioEffectsConfig(masterEnabled = masterEnabled)
        _config.value = newConfig
        applyToEngines(newConfig)
        _userMessage.value = "Loaded preset: ${preset.name}"
    }

    fun openSavePresetDialog() {
        _showSavePresetDialog.value = true
    }

    fun closeSavePresetDialog() {
        _showSavePresetDialog.value = false
    }

    fun savePreset(name: String, scenario: String, description: String, iconTag: String) {
        viewModelScope.launch {
            val entity = PresetEntity.fromConfig(
                config = _config.value,
                name = name.ifBlank { "Custom Preset" },
                scenario = scenario.ifBlank { "Custom" },
                description = description.ifBlank { "User customized preset" },
                iconTag = iconTag,
                isFactory = false
            )
            val id = repository.savePreset(entity)
            _config.update { it.copy(activePresetId = id, activePresetName = entity.name) }
            _showSavePresetDialog.value = false
            _userMessage.value = "Preset '${entity.name}' saved"
        }
    }

    fun deletePreset(id: Long) {
        viewModelScope.launch {
            repository.deleteCustomPreset(id)
            _userMessage.value = "Preset deleted"
        }
    }

    // Audio Preview Testing
    fun toggleAudioPreview() {
        synthesizer.togglePlay()
        if (synthesizer.isPlaying.value) {
            val sessionId = synthesizer.getAudioSessionId()
            if (sessionId != 0) {
                fallbackEngine.attachSession(sessionId)
                fallbackEngine.applyConfig(_config.value)
            }
        }
    }

    fun selectPreviewTrack(track: PreviewTrack) {
        synthesizer.selectTrack(track)
        val sessionId = synthesizer.getAudioSessionId()
        if (sessionId != 0) {
            fallbackEngine.attachSession(sessionId)
            fallbackEngine.applyConfig(_config.value)
        }
    }

    override fun onCleared() {
        super.onCleared()
        synthesizer.stop()
        fallbackEngine.release()
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val app = context.applicationContext as Application
                    val db = AppDatabase.getDatabase(app, kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO))
                    val repository = PresetRepository(db.presetDao())
                    return AudioEffectsViewModel(app, repository) as T
                }
            }
    }
}
