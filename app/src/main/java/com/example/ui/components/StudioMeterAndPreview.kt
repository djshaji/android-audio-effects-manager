package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.service.PreviewTrack
import com.example.ui.theme.MeterBackground
import com.example.ui.theme.StudioAmber
import com.example.ui.theme.StudioCard
import com.example.ui.theme.StudioCardBorder
import com.example.ui.theme.StudioCyan
import com.example.ui.theme.StudioGreen
import com.example.ui.theme.StudioRed
import com.example.ui.theme.StudioViolet
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun StudioMeterAndPreview(
    isPlaying: Boolean,
    currentTrack: PreviewTrack,
    spectrumLevels: List<Float>,
    vuPeak: Float,
    gainReductionDb: Float,
    onTogglePlay: () -> Unit,
    onSelectNextTrack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MeterBackground)
            .border(1.dp, StudioCardBorder, RoundedCornerShape(18.dp))
            .padding(14.dp)
    ) {
        // Upper Visualizer: Spectrum & VU & Limiter GR
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 5-Band Spectrum Display
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "REAL-TIME DSP SPECTRUM",
                        color = TextTertiary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = if (isPlaying) "ANALYZING" else "IDLE",
                        color = if (isPlaying) StudioCyan else TextTertiary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    val bandLabels = listOf("60", "230", "910", "3.6k", "14k")
                    val bandColors = listOf(StudioCyan, StudioViolet, StudioGreen, StudioAmber, StudioCyan)

                    spectrumLevels.forEachIndexed { index, level ->
                        val animatedHeight by animateFloatAsState(
                            targetValue = if (isPlaying) level.coerceIn(0.08f, 1.0f) else 0.08f,
                            animationSpec = tween(durationMillis = 60),
                            label = "spectrum_bar_$index"
                        )

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom,
                            modifier = Modifier.width(28.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(18.dp)
                                    .height((32 * animatedHeight).dp)
                                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                bandColors.getOrElse(index) { StudioCyan },
                                                bandColors.getOrElse(index) { StudioCyan }.copy(alpha = 0.35f)
                                            )
                                        )
                                    )
                                    .testTag("spectrum_bar_$index")
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = bandLabels.getOrElse(index) { "" },
                                color = TextTertiary,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // VU & Gain Reduction Meters
            Column(
                modifier = Modifier
                    .width(100.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Peak Output Level
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("PEAK VU", color = TextTertiary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = if (vuPeak > 0.01f) "${(vuPeak * 100).toInt()}%" else "-∞",
                            color = if (vuPeak > 0.85f) StudioRed else StudioGreen,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(StudioCard)
                    ) {
                        val animatedVu by animateFloatAsState(
                            targetValue = vuPeak.coerceIn(0.02f, 1f),
                            animationSpec = tween(50),
                            label = "vu_meter"
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(animatedVu)
                                .fillMaxHeight()
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(StudioGreen, StudioAmber, StudioRed)
                                    )
                                )
                                .testTag("vu_peak_bar")
                        )
                    }
                }

                // Limiter Gain Reduction (GR)
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("LIMITER GR", color = TextTertiary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = if (gainReductionDb > 0.1f) "-${String.format("%.1f", gainReductionDb)}dB" else "0 dB",
                            color = if (gainReductionDb > 0.1f) StudioAmber else TextTertiary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(StudioCard)
                    ) {
                        val grRatio = (gainReductionDb / 12.0f).coerceIn(0f, 1f)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(grRatio)
                                .fillMaxHeight()
                                .background(StudioAmber)
                                .testTag("gr_meter_bar")
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Lower Audio Preview bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(StudioCard)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isPlaying) StudioCyan else StudioCardBorder)
                        .clickable { onTogglePlay() }
                        .testTag("preview_play_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause Demo" else "Play Demo",
                        tint = if (isPlaying) Color.Black else TextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = currentTrack.title,
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = currentTrack.subtitle,
                        color = TextSecondary,
                        fontSize = 10.sp,
                        maxLines = 1
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(StudioCardBorder)
                    .clickable { onSelectNextTrack() }
                    .padding(horizontal = 8.dp, vertical = 6.dp)
                    .testTag("next_track_button"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next Track",
                        tint = TextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "Next Track",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
