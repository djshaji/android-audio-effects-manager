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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import com.example.ui.theme.StudioRed
import com.example.ui.theme.StudioSurfaceVariant
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun MasterGainSection(
    fineVolumePercent: Float,
    masterGainDb: Float,
    balance: Float,
    onSetFineVolume: (Float) -> Unit,
    onAdjustFineVolumeStep: (Float) -> Unit,
    onSetMasterGain: (Float) -> Unit,
    onSetBalance: (Float) -> Unit,
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
        // 1. Fine Master Volume Control
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = "Master Volume",
                    tint = StudioCyan,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "FINE MASTER VOLUME",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "High-precision 0.1% media stream volume",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            Text(
                text = "${(fineVolumePercent * 100).toInt()}%",
                color = if (fineVolumePercent > 0.85f) StudioAmber else StudioCyan,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // -1% Button
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(StudioSurfaceVariant)
                    .clickable { onAdjustFineVolumeStep(-0.01f) }
                    .testTag("fine_volume_minus_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Decrease 1%",
                    tint = TextPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Slider
            Slider(
                value = fineVolumePercent,
                onValueChange = onSetFineVolume,
                valueRange = 0.0f..1.0f,
                modifier = Modifier
                    .weight(1f)
                    .testTag("fine_volume_slider"),
                colors = SliderDefaults.colors(
                    thumbColor = StudioCyan,
                    activeTrackColor = StudioCyan,
                    inactiveTrackColor = StudioSurfaceVariant
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            // +1% Button
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(StudioSurfaceVariant)
                    .clickable { onAdjustFineVolumeStep(0.01f) }
                    .testTag("fine_volume_plus_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Increase 1%",
                    tint = TextPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 2. Master Gain (-24 dB to +18 dB)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "MASTER DSP GAIN",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Pre-limiter hardware dynamic input gain",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Reset to 0 dB button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(StudioSurfaceVariant)
                        .clickable { onSetMasterGain(0.0f) }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("master_gain_reset_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = "0 dB",
                            tint = TextSecondary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "0 dB",
                            color = TextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "${if (masterGainDb >= 0) "+" else ""}${String.format("%.1f", masterGainDb)} dB",
                    color = when {
                        masterGainDb > 6.0f -> StudioRed
                        masterGainDb > 0.0f -> StudioAmber
                        else -> StudioCyan
                    },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Slider(
            value = masterGainDb,
            onValueChange = onSetMasterGain,
            valueRange = -24.0f..18.0f,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("master_gain_slider"),
            colors = SliderDefaults.colors(
                thumbColor = if (masterGainDb > 6.0f) StudioAmber else StudioCyan,
                activeTrackColor = if (masterGainDb > 6.0f) StudioAmber else StudioCyan,
                inactiveTrackColor = StudioSurfaceVariant
            )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("-24 dB", color = TextTertiary, fontSize = 10.sp)
            Text("Unity (0 dB)", color = TextTertiary, fontSize = 10.sp)
            Text("+18 dB", color = TextTertiary, fontSize = 10.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Stereo Balance
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "STEREO BALANCE",
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            val balanceText = when {
                balance < -0.05f -> "L ${(-balance * 100).toInt()}%"
                balance > 0.05f -> "R ${(balance * 100).toInt()}%"
                else -> "Center"
            }
            Text(
                text = balanceText,
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Slider(
            value = balance,
            onValueChange = onSetBalance,
            valueRange = -1.0f..1.0f,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("balance_slider"),
            colors = SliderDefaults.colors(
                thumbColor = StudioCyan,
                activeTrackColor = StudioCyan,
                inactiveTrackColor = StudioSurfaceVariant
            )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Left", color = TextTertiary, fontSize = 10.sp)
            Text("Center (0)", color = TextTertiary, fontSize = 10.sp, modifier = Modifier.clickable { onSetBalance(0.0f) })
            Text("Right", color = TextTertiary, fontSize = 10.sp)
        }
    }
}
