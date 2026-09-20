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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PresetEntity
import com.example.ui.theme.StudioAmber
import com.example.ui.theme.StudioCard
import com.example.ui.theme.StudioCardBorder
import com.example.ui.theme.StudioCyan
import com.example.ui.theme.StudioGreen
import com.example.ui.theme.StudioRed
import com.example.ui.theme.StudioSurfaceVariant
import com.example.ui.theme.StudioViolet
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun PresetsSection(
    presets: List<PresetEntity>,
    activePresetId: Long,
    onSelectPreset: (PresetEntity) -> Unit,
    onOpenSaveDialog: () -> Unit,
    onDeletePreset: (Long) -> Unit,
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
        // Section Header with Save Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "LISTENING SCENARIOS",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Acoustically tuned profiles & custom presets",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }

            Button(
                onClick = onOpenSaveDialog,
                colors = ButtonDefaults.buttonColors(
                    containerColor = StudioCyan,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("save_current_preset_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Save Preset",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Save Preset", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        presets.forEach { preset ->
            val isActive = preset.id == activePresetId

            PresetCard(
                preset = preset,
                isActive = isActive,
                onSelect = { onSelectPreset(preset) },
                onDelete = { onDeletePreset(preset.id) }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun PresetCard(
    preset: PresetEntity,
    isActive: Boolean,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    val icon = getIconForTag(preset.iconTag)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isActive) StudioCyan.copy(alpha = 0.08f) else StudioSurfaceVariant)
            .border(
                1.dp,
                if (isActive) StudioCyan else StudioCardBorder,
                RoundedCornerShape(12.dp)
            )
            .clickable { onSelect() }
            .padding(12.dp)
            .testTag("preset_card_${preset.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (isActive) StudioCyan else StudioCard)
                .border(1.dp, if (isActive) StudioCyan else StudioCardBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = preset.name,
                tint = if (isActive) Color.Black else StudioCyan,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = preset.name,
                    color = if (isActive) StudioCyan else TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(6.dp))
                if (isActive) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(StudioCyan)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("ACTIVE", color = Color.Black, fontSize = 8.sp, fontWeight = FontWeight.Black)
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = preset.description,
                color = TextSecondary,
                fontSize = 11.sp,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Parameter Snapshot Chips
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (preset.bassBoostEnabled && preset.bassBoostStrength > 0) {
                    SnapshotChip("Bass +${preset.bassBoostStrength / 10}%", StudioCyan)
                }
                if (preset.virtualizerEnabled && preset.virtualizerStrength > 0) {
                    SnapshotChip("Surround ${preset.virtualizerStrength / 10}%", StudioGreen)
                }
                if (preset.normalizerEnabled) {
                    SnapshotChip("Normalizer", StudioAmber)
                }
                if (preset.compressorEnabled) {
                    SnapshotChip("Comp ${String.format("%.1f", preset.compressorRatio)}:1", StudioViolet)
                }
                if (preset.limiterEnabled) {
                    SnapshotChip("Limiter ${preset.limiterThresholdDb}dB", StudioRed)
                }
            }
        }

        if (!preset.isFactory) {
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("delete_preset_button_${preset.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete preset",
                    tint = TextTertiary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun SnapshotChip(label: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 5.dp, vertical = 2.dp)
    ) {
        Text(text = label, color = color, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
    }
}

fun getIconForTag(tag: String): ImageVector {
    return when (tag.lowercase()) {
        "headphones" -> Icons.Default.Headphones
        "speaker" -> Icons.Default.Speaker
        "movie", "cinema" -> Icons.Default.Movie
        "mic", "voice", "podcast" -> Icons.Default.Mic
        "night" -> Icons.Default.Nightlight
        "game", "gaming" -> Icons.Default.SportsEsports
        "audiophile" -> Icons.Default.Tune
        else -> Icons.Default.Bookmark
    }
}
