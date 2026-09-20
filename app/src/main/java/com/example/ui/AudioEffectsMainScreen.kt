package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.service.PreviewTrack
import com.example.ui.components.DynamicsSection
import com.example.ui.components.EqualizerAndBassSection
import com.example.ui.components.MasterGainSection
import com.example.ui.components.PresetsSection
import com.example.ui.components.SavePresetDialog
import com.example.ui.components.StudioHeader
import com.example.ui.components.StudioMeterAndPreview
import com.example.ui.theme.StudioBlack
import com.example.ui.theme.StudioCard
import com.example.ui.theme.StudioCardBorder
import com.example.ui.theme.StudioCyan
import com.example.ui.theme.StudioSurfaceVariant
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun AudioEffectsMainScreen(
    viewModel: AudioEffectsViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.dismissUserMessage()
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(StudioBlack),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = StudioBlack
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // 1. Studio Header with Master Switch & A/B Bypass
            StudioHeader(
                config = uiState.config,
                onToggleMaster = { viewModel.toggleMasterPower() },
                onToggleBypass = { viewModel.toggleBypass() }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Real-Time Spectrum & VU Meter & Preview Player
            StudioMeterAndPreview(
                isPlaying = uiState.isPlayingPreview,
                currentTrack = uiState.previewTrack,
                spectrumLevels = uiState.spectrumLevels,
                vuPeak = uiState.vuMeterPeak,
                gainReductionDb = uiState.gainReductionDb,
                onTogglePlay = { viewModel.toggleAudioPreview() },
                onSelectNextTrack = {
                    val tracks = PreviewTrack.values()
                    val nextIndex = (uiState.previewTrack.ordinal + 1) % tracks.size
                    viewModel.selectPreviewTrack(tracks[nextIndex])
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Navigation Tabs
            StudioTabBar(
                selectedTab = uiState.activeTab,
                onSelectTab = { viewModel.setTab(it) }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 4. Tab Content
            when (uiState.activeTab) {
                AudioTab.MASTER -> {
                    MasterGainSection(
                        fineVolumePercent = uiState.config.fineVolumePercent,
                        masterGainDb = uiState.config.masterGainDb,
                        balance = uiState.config.balance,
                        onSetFineVolume = { viewModel.setFineVolume(it) },
                        onAdjustFineVolumeStep = { viewModel.adjustFineVolumeStep(it) },
                        onSetMasterGain = { viewModel.setMasterGain(it) },
                        onSetBalance = { viewModel.setBalance(it) }
                    )
                }

                AudioTab.EQ_BASS -> {
                    EqualizerAndBassSection(
                        equalizerEnabled = uiState.config.equalizerEnabled,
                        eqBands = uiState.config.eqBandLevels,
                        bassBoostEnabled = uiState.config.bassBoostEnabled,
                        bassBoostStrength = uiState.config.bassBoostStrength,
                        virtualizerEnabled = uiState.config.virtualizerEnabled,
                        virtualizerStrength = uiState.config.virtualizerStrength,
                        onToggleEqualizer = { viewModel.toggleEqualizer() },
                        onSetEqBand = { index, mb -> viewModel.setEqBandLevel(index, mb) },
                        onResetEqFlat = { viewModel.resetEqFlat() },
                        onToggleBassBoost = { viewModel.toggleBassBoost() },
                        onSetBassBoostStrength = { viewModel.setBassBoostStrength(it) },
                        onToggleVirtualizer = { viewModel.toggleVirtualizer() },
                        onSetVirtualizerStrength = { viewModel.setVirtualizerStrength(it) }
                    )
                }

                AudioTab.DYNAMICS -> {
                    DynamicsSection(
                        // Normalizer
                        normalizerEnabled = uiState.config.normalizerEnabled,
                        normalizerGainMb = uiState.config.normalizerGainMb,
                        onToggleNormalizer = { viewModel.toggleNormalizer() },
                        onSetNormalizerGain = { viewModel.setNormalizerGain(it) },

                        // Compressor
                        compressorEnabled = uiState.config.compressorEnabled,
                        compressorThresholdDb = uiState.config.compressorThresholdDb,
                        compressorRatio = uiState.config.compressorRatio,
                        compressorAttackMs = uiState.config.compressorAttackMs,
                        compressorReleaseMs = uiState.config.compressorReleaseMs,
                        compressorMakeupGainDb = uiState.config.compressorMakeupGainDb,
                        onToggleCompressor = { viewModel.toggleCompressor() },
                        onSetCompressorThreshold = { viewModel.setCompressorThreshold(it) },
                        onSetCompressorRatio = { viewModel.setCompressorRatio(it) },
                        onSetCompressorAttack = { viewModel.setCompressorAttack(it) },
                        onSetCompressorRelease = { viewModel.setCompressorRelease(it) },
                        onSetCompressorMakeupGain = { viewModel.setCompressorMakeupGain(it) },

                        // Limiter
                        limiterEnabled = uiState.config.limiterEnabled,
                        limiterThresholdDb = uiState.config.limiterThresholdDb,
                        limiterAttackMs = uiState.config.limiterAttackMs,
                        limiterReleaseMs = uiState.config.limiterReleaseMs,
                        limiterRatio = uiState.config.limiterRatio,
                        onToggleLimiter = { viewModel.toggleLimiter() },
                        onSetLimiterThreshold = { viewModel.setLimiterThreshold(it) },
                        onSetLimiterAttack = { viewModel.setLimiterAttack(it) },
                        onSetLimiterRelease = { viewModel.setLimiterRelease(it) },
                        onSetLimiterRatio = { viewModel.setLimiterRatio(it) }
                    )
                }

                AudioTab.PRESETS -> {
                    PresetsSection(
                        presets = uiState.presets,
                        activePresetId = uiState.config.activePresetId,
                        onSelectPreset = { viewModel.selectPreset(it) },
                        onOpenSaveDialog = { viewModel.openSavePresetDialog() },
                        onDeletePreset = { viewModel.deletePreset(it) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Save Custom Preset Dialog
        if (uiState.showSavePresetDialog) {
            SavePresetDialog(
                onDismiss = { viewModel.closeSavePresetDialog() },
                onConfirmSave = { name, scenario, desc, icon ->
                    viewModel.savePreset(name, scenario, desc, icon)
                }
            )
        }
    }
}

@Composable
private fun StudioTabBar(
    selectedTab: AudioTab,
    onSelectTab: (AudioTab) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(StudioCard)
            .border(1.dp, StudioCardBorder, RoundedCornerShape(14.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        AudioTab.values().forEach { tab ->
            val isSelected = selectedTab == tab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) StudioCyan else Color.Transparent)
                    .clickable { onSelectTab(tab) }
                    .padding(vertical = 10.dp)
                    .testTag("tab_${tab.name.lowercase()}"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tab.title,
                    color = if (isSelected) Color.Black else TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold,
                    maxLines = 1
                )
            }
        }
    }
}
