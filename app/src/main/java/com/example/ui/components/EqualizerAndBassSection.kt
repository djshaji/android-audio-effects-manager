package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.SurroundSound
import androidx.compose.material.icons.filled.Waves
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.StudioAmber
import com.example.ui.theme.StudioCard
import com.example.ui.theme.StudioCardBorder
import com.example.ui.theme.StudioCyan
import com.example.ui.theme.StudioGreen
import com.example.ui.theme.StudioSurfaceVariant
import com.example.ui.theme.StudioViolet
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun EqualizerAndBassSection(
    equalizerEnabled: Boolean,
    eqBands: List<Int>,
    bassBoostEnabled: Boolean,
    bassBoostStrength: Int,
    virtualizerEnabled: Boolean,
    virtualizerStrength: Int,
    onToggleEqualizer: () -> Unit,
    onSetEqBand: (Int, Int) -> Unit,
    onResetEqFlat: () -> Unit,
    onToggleBassBoost: () -> Unit,
    onSetBassBoostStrength: (Int) -> Unit,
    onToggleVirtualizer: () -> Unit,
    onSetVirtualizerStrength: (Int) -> Unit,
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
        // Equalizer Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Equalizer,
                    contentDescription = "Equalizer",
                    tint = StudioViolet,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "5-BAND STUDIO EQUALIZER",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Hardware filter bands with ±15 dB range",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Flat Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(StudioSurfaceVariant)
                        .clickable { onResetEqFlat() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("eq_flat_reset_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = "Reset EQ Flat",
                            tint = TextSecondary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "Flat",
                            color = TextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Switch(
                    checked = equalizerEnabled,
                    onCheckedChange = { onToggleEqualizer() },
                    modifier = Modifier.testTag("equalizer_toggle_switch"),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = StudioViolet,
                        checkedTrackColor = StudioViolet.copy(alpha = 0.35f),
                        uncheckedThumbColor = TextTertiary,
                        uncheckedTrackColor = StudioSurfaceVariant
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 5 Equalizer Faders
        val freqLabels = listOf("60 Hz", "230 Hz", "910 Hz", "3.6 kHz", "14 kHz")
        val freqRoles = listOf("Sub Bass", "Punch", "Body", "Presence", "Air")

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(StudioSurfaceVariant)
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            eqBands.forEachIndexed { index, levelMb ->
                val freqLabel = freqLabels.getOrElse(index) { "Band $index" }
                val role = freqRoles.getOrElse(index) { "" }
                val levelDb = levelMb / 100.0f

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.width(72.dp)) {
                        Text(
                            text = freqLabel,
                            color = TextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = role,
                            color = TextTertiary,
                            fontSize = 9.sp
                        )
                    }

                    Slider(
                        value = levelMb.toFloat(),
                        onValueChange = { onSetEqBand(index, it.toInt()) },
                        valueRange = -1500f..1500f,
                        enabled = equalizerEnabled,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("eq_slider_band_$index"),
                        colors = SliderDefaults.colors(
                            thumbColor = if (equalizerEnabled) StudioViolet else TextTertiary,
                            activeTrackColor = if (equalizerEnabled) StudioViolet else TextTertiary,
                            inactiveTrackColor = StudioCardBorder
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "${if (levelDb >= 0) "+" else ""}${String.format("%.1f", levelDb)} dB",
                        color = when {
                            !equalizerEnabled -> TextTertiary
                            levelDb > 0.0f -> StudioViolet
                            levelDb < 0.0f -> StudioCyan
                            else -> TextSecondary
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(48.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Bass Booster Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Waves,
                    contentDescription = "Bass Booster",
                    tint = StudioCyan,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "BASS BOOSTER",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Sub-harmonic resonance and low-end punch",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${(bassBoostStrength / 10)}%",
                    color = if (bassBoostEnabled) StudioCyan else TextTertiary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = bassBoostEnabled,
                    onCheckedChange = { onToggleBassBoost() },
                    modifier = Modifier.testTag("bass_boost_toggle_switch"),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = StudioCyan,
                        checkedTrackColor = StudioCyan.copy(alpha = 0.35f),
                        uncheckedThumbColor = TextTertiary,
                        uncheckedTrackColor = StudioSurfaceVariant
                    )
                )
            }
        }

        Slider(
            value = bassBoostStrength.toFloat(),
            onValueChange = { onSetBassBoostStrength(it.toInt()) },
            valueRange = 0f..1000f,
            enabled = bassBoostEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("bass_boost_slider"),
            colors = SliderDefaults.colors(
                thumbColor = if (bassBoostEnabled) StudioCyan else TextTertiary,
                activeTrackColor = if (bassBoostEnabled) StudioCyan else TextTertiary,
                inactiveTrackColor = StudioSurfaceVariant
            )
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Virtual Surround Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.SurroundSound,
                    contentDescription = "Virtual Surround",
                    tint = StudioGreen,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "VIRTUAL SURROUND",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "3D spatializer & stereo stage expansion",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${(virtualizerStrength / 10)}%",
                    color = if (virtualizerEnabled) StudioGreen else TextTertiary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = virtualizerEnabled,
                    onCheckedChange = { onToggleVirtualizer() },
                    modifier = Modifier.testTag("virtualizer_toggle_switch"),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = StudioGreen,
                        checkedTrackColor = StudioGreen.copy(alpha = 0.35f),
                        uncheckedThumbColor = TextTertiary,
                        uncheckedTrackColor = StudioSurfaceVariant
                    )
                )
            }
        }

        Slider(
            value = virtualizerStrength.toFloat(),
            onValueChange = { onSetVirtualizerStrength(it.toInt()) },
            valueRange = 0f..1000f,
            enabled = virtualizerEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("virtualizer_slider"),
            colors = SliderDefaults.colors(
                thumbColor = if (virtualizerEnabled) StudioGreen else TextTertiary,
                activeTrackColor = if (virtualizerEnabled) StudioGreen else TextTertiary,
                inactiveTrackColor = StudioSurfaceVariant
            )
        )
    }
}
