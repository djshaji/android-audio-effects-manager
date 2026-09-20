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
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.example.model.AudioEffectsConfig
import com.example.ui.theme.StudioAmber
import com.example.ui.theme.StudioCard
import com.example.ui.theme.StudioCardBorder
import com.example.ui.theme.StudioCyan
import com.example.ui.theme.StudioGreen
import com.example.ui.theme.StudioRed
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun StudioHeader(
    config: AudioEffectsConfig,
    onToggleMaster: () -> Unit,
    onToggleBypass: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDSPAffecting = config.masterEnabled && !config.bypassAll

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(StudioCard)
            .border(1.dp, StudioCardBorder, RoundedCornerShape(18.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                !config.masterEnabled -> StudioRed
                                config.bypassAll -> StudioAmber
                                else -> StudioGreen
                            }
                        )
                        .testTag("status_indicator_dot")
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "AUDIO EFFECTS",
                    color = TextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = when {
                    !config.masterEnabled -> "System DSP Disabled"
                    config.bypassAll -> "A/B Raw Audio (Bypassed)"
                    else -> "System-Wide DSP Active • ${config.activePresetName}"
                },
                color = when {
                    !config.masterEnabled -> TextTertiary
                    config.bypassAll -> StudioAmber
                    else -> StudioCyan
                },
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            // A/B Bypass Button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (config.bypassAll) StudioAmber.copy(alpha = 0.2f) else StudioCardBorder)
                    .border(
                        1.dp,
                        if (config.bypassAll) StudioAmber else Color.Transparent,
                        RoundedCornerShape(10.dp)
                    )
                    .clickable { onToggleBypass() }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .testTag("ab_bypass_button"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CompareArrows,
                        contentDescription = "A/B Compare Bypass",
                        tint = if (config.bypassAll) StudioAmber else TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (config.bypassAll) "BYPASS" else "A/B",
                        color = if (config.bypassAll) StudioAmber else TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Master Power Switch
            Switch(
                checked = config.masterEnabled,
                onCheckedChange = { onToggleMaster() },
                modifier = Modifier.testTag("master_power_switch"),
                thumbContent = {
                    Icon(
                        imageVector = Icons.Default.PowerSettingsNew,
                        contentDescription = "Power",
                        modifier = Modifier.size(14.dp),
                        tint = if (config.masterEnabled) Color.Black else TextTertiary
                    )
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = StudioCyan,
                    checkedTrackColor = StudioCyan.copy(alpha = 0.35f),
                    uncheckedThumbColor = TextTertiary,
                    uncheckedTrackColor = StudioCardBorder
                )
            )
        }
    }
}
