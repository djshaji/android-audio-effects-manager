package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.StudioAmber
import com.example.ui.theme.StudioCard
import com.example.ui.theme.StudioCardBorder
import com.example.ui.theme.StudioCyan
import com.example.ui.theme.StudioRed
import com.example.ui.theme.StudioSurfaceVariant
import com.example.ui.theme.StudioViolet
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun DynamicsSection(
    // Normalizer
    normalizerEnabled: Boolean,
    normalizerGainMb: Int,
    onToggleNormalizer: () -> Unit,
    onSetNormalizerGain: (Int) -> Unit,

    // Compressor
    compressorEnabled: Boolean,
    compressorThresholdDb: Float,
    compressorRatio: Float,
    compressorAttackMs: Float,
    compressorReleaseMs: Float,
    compressorMakeupGainDb: Float,
    onToggleCompressor: () -> Unit,
    onSetCompressorThreshold: (Float) -> Unit,
    onSetCompressorRatio: (Float) -> Unit,
    onSetCompressorAttack: (Float) -> Unit,
    onSetCompressorRelease: (Float) -> Unit,
    onSetCompressorMakeupGain: (Float) -> Unit,

    // Limiter
    limiterEnabled: Boolean,
    limiterThresholdDb: Float,
    limiterAttackMs: Float,
    limiterReleaseMs: Float,
    limiterRatio: Float,
    onToggleLimiter: () -> Unit,
    onSetLimiterThreshold: (Float) -> Unit,
    onSetLimiterAttack: (Float) -> Unit,
    onSetLimiterRelease: (Float) -> Unit,
    onSetLimiterRatio: (Float) -> Unit,

    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(StudioCard)
            .border(1.dp, StudioCardBorder, RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        // 1. NORMALIZER (LOUDNESS ENHANCER)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Hearing,
                    contentDescription = "Normalizer",
                    tint = StudioAmber,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "AUDIO NORMALIZER",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Auto-levels quiet audio tracks & dialogue",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                val gainDb = normalizerGainMb / 100.0f
                Text(
                    text = "${if (gainDb >= 0) "+" else ""}${String.format("%.1f", gainDb)} dB",
                    color = if (normalizerEnabled) StudioAmber else TextTertiary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = normalizerEnabled,
                    onCheckedChange = { onToggleNormalizer() },
                    modifier = Modifier.testTag("normalizer_toggle_switch"),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = StudioAmber,
                        checkedTrackColor = StudioAmber.copy(alpha = 0.35f),
                        uncheckedThumbColor = TextTertiary,
                        uncheckedTrackColor = StudioSurfaceVariant
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Slider(
            value = normalizerGainMb.toFloat(),
            onValueChange = { onSetNormalizerGain(it.toInt()) },
            valueRange = 0f..2500f,
            enabled = normalizerEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("normalizer_gain_slider"),
            colors = SliderDefaults.colors(
                thumbColor = if (normalizerEnabled) StudioAmber else TextTertiary,
                activeTrackColor = if (normalizerEnabled) StudioAmber else TextTertiary,
                inactiveTrackColor = StudioSurfaceVariant
            )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Gentle (0 dB)", color = TextTertiary, fontSize = 10.sp)
            Text("Broadcast (+12 dB)", color = TextTertiary, fontSize = 10.sp)
            Text("Max Boost (+25 dB)", color = TextTertiary, fontSize = 10.sp)
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 2. AUDIO COMPRESSOR
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Compress,
                    contentDescription = "Audio Compressor",
                    tint = StudioCyan,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "AUDIO COMPRESSOR",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Tames dynamic peaks & boosts punchiness",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            Switch(
                checked = compressorEnabled,
                onCheckedChange = { onToggleCompressor() },
                modifier = Modifier.testTag("compressor_toggle_switch"),
                colors = SwitchDefaults.colors(
                    checkedThumbColor = StudioCyan,
                    checkedTrackColor = StudioCyan.copy(alpha = 0.35f),
                    uncheckedThumbColor = TextTertiary,
                    uncheckedTrackColor = StudioSurfaceVariant
                )
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Compressor Parameters Grid
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(StudioSurfaceVariant)
                .padding(12.dp)
        ) {
            // Threshold
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Threshold", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text("${String.format("%.1f", compressorThresholdDb)} dB", color = StudioCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Slider(
                value = compressorThresholdDb,
                onValueChange = onSetCompressorThreshold,
                valueRange = -40.0f..0.0f,
                enabled = compressorEnabled,
                modifier = Modifier.testTag("compressor_threshold_slider"),
                colors = SliderDefaults.colors(thumbColor = StudioCyan, activeTrackColor = StudioCyan)
            )

            // Ratio
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Ratio", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text("${String.format("%.1f", compressorRatio)}:1", color = StudioCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Slider(
                value = compressorRatio,
                onValueChange = onSetCompressorRatio,
                valueRange = 1.0f..16.0f,
                enabled = compressorEnabled,
                modifier = Modifier.testTag("compressor_ratio_slider"),
                colors = SliderDefaults.colors(thumbColor = StudioCyan, activeTrackColor = StudioCyan)
            )

            // Attack & Release in a dual row
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Attack: ${compressorAttackMs.toInt()} ms", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Slider(
                        value = compressorAttackMs,
                        onValueChange = onSetCompressorAttack,
                        valueRange = 1.0f..150.0f,
                        enabled = compressorEnabled,
                        modifier = Modifier.testTag("compressor_attack_slider"),
                        colors = SliderDefaults.colors(thumbColor = StudioCyan, activeTrackColor = StudioCyan)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Release: ${compressorReleaseMs.toInt()} ms", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Slider(
                        value = compressorReleaseMs,
                        onValueChange = onSetCompressorRelease,
                        valueRange = 10.0f..600.0f,
                        enabled = compressorEnabled,
                        modifier = Modifier.testTag("compressor_release_slider"),
                        colors = SliderDefaults.colors(thumbColor = StudioCyan, activeTrackColor = StudioCyan)
                    )
                }
            }

            // Makeup Gain
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Makeup Gain", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text("+${String.format("%.1f", compressorMakeupGainDb)} dB", color = StudioCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Slider(
                value = compressorMakeupGainDb,
                onValueChange = onSetCompressorMakeupGain,
                valueRange = 0.0f..12.0f,
                enabled = compressorEnabled,
                modifier = Modifier.testTag("compressor_makeup_slider"),
                colors = SliderDefaults.colors(thumbColor = StudioCyan, activeTrackColor = StudioCyan)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 3. AUDIO LIMITER
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = "Audio Limiter",
                    tint = StudioRed,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "AUDIO LIMITER",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Brickwall protection against clipping & speaker distortion",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            Switch(
                checked = limiterEnabled,
                onCheckedChange = { onToggleLimiter() },
                modifier = Modifier.testTag("limiter_toggle_switch"),
                colors = SwitchDefaults.colors(
                    checkedThumbColor = StudioRed,
                    checkedTrackColor = StudioRed.copy(alpha = 0.35f),
                    uncheckedThumbColor = TextTertiary,
                    uncheckedTrackColor = StudioSurfaceVariant
                )
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(StudioSurfaceVariant)
                .padding(12.dp)
        ) {
            // Ceiling Threshold
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Ceiling Limit", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text("${String.format("%.1f", limiterThresholdDb)} dBFS", color = StudioRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Slider(
                value = limiterThresholdDb,
                onValueChange = onSetLimiterThreshold,
                valueRange = -12.0f..0.0f,
                enabled = limiterEnabled,
                modifier = Modifier.testTag("limiter_threshold_slider"),
                colors = SliderDefaults.colors(thumbColor = StudioRed, activeTrackColor = StudioRed)
            )

            // Attack & Release
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Attack: ${String.format("%.1f", limiterAttackMs)} ms", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Slider(
                        value = limiterAttackMs,
                        onValueChange = onSetLimiterAttack,
                        valueRange = 0.1f..10.0f,
                        enabled = limiterEnabled,
                        modifier = Modifier.testTag("limiter_attack_slider"),
                        colors = SliderDefaults.colors(thumbColor = StudioRed, activeTrackColor = StudioRed)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Release: ${limiterReleaseMs.toInt()} ms", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Slider(
                        value = limiterReleaseMs,
                        onValueChange = onSetLimiterRelease,
                        valueRange = 10.0f..200.0f,
                        enabled = limiterEnabled,
                        modifier = Modifier.testTag("limiter_release_slider"),
                        colors = SliderDefaults.colors(thumbColor = StudioRed, activeTrackColor = StudioRed)
                    )
                }
            }

            // Ratio
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Hard Clamp Ratio", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text("${limiterRatio.toInt()}:1 (Brickwall)", color = StudioRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Slider(
                value = limiterRatio,
                onValueChange = onSetLimiterRatio,
                valueRange = 10.0f..100.0f,
                enabled = limiterEnabled,
                modifier = Modifier.testTag("limiter_ratio_slider"),
                colors = SliderDefaults.colors(thumbColor = StudioRed, activeTrackColor = StudioRed)
            )
        }
    }
}
