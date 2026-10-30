package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SayItStrings
import com.example.ui.theme.AccentRedRecord
import com.example.ui.theme.LocalSayItColors

@Composable
fun VoiceRecordBar(
    isRecording: Boolean,
    hasRecording: Boolean,
    isPlaying: Boolean,
    durationSeconds: Int,
    playbackProgress: Float,
    amplitudes: List<Float>,
    volume: Float,
    strings: SayItStrings,
    onStopRecording: () -> Unit,
    onTogglePlay: () -> Unit,
    onRerecord: () -> Unit,
    onDelete: () -> Unit,
    onVolumeChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val customColors = LocalSayItColors.current
    var showVolumeControl by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "RecordPulse")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "DotBlink"
    )

    val minutes = durationSeconds / 60
    val seconds = durationSeconds % 60
    val formattedTime = String.format("%02d:%02d", minutes, seconds)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(if (customColors.isDark) Color(0xD9111827) else Color(0xF2FFFFFF))
            .border(1.dp, customColors.cardBorder, RoundedCornerShape(20.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .testTag("voice_record_bar")
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header: status + timer + action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Recording status indicator or playback status
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (isRecording) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .alpha(dotAlpha)
                                .clip(CircleShape)
                                .background(AccentRedRecord)
                        )
                        Text(
                            text = strings.recording,
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = AccentRedRecord,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    } else if (hasRecording) {
                        // Play/Pause button
                        IconButton(
                            onClick = onTogglePlay,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(customColors.releaseButtonColor)
                                .testTag("play_pause_button")
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) strings.pause else strings.play,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = customColors.textPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    )
                }

                // Controls row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (isRecording) {
                        // Stop button
                        IconButton(
                            onClick = onStopRecording,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(AccentRedRecord)
                                .testTag("stop_recording_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = strings.stopRecording,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    } else if (hasRecording) {
                        // Volume toggle
                        IconButton(
                            onClick = { showVolumeControl = !showVolumeControl },
                            modifier = Modifier
                                .size(34.dp)
                                .testTag("volume_toggle_button")
                        ) {
                            Icon(
                                imageVector = if (volume > 0.5f) Icons.Default.VolumeUp else Icons.Default.VolumeDown,
                                contentDescription = strings.volumeLabel,
                                tint = if (showVolumeControl) customColors.releaseButtonColor else customColors.textSecondary,
                                modifier = Modifier.size(19.dp)
                            )
                        }

                        // Re-record button
                        IconButton(
                            onClick = onRerecord,
                            modifier = Modifier
                                .size(34.dp)
                                .testTag("rerecord_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = strings.rerecord,
                                tint = customColors.textSecondary,
                                modifier = Modifier.size(19.dp)
                            )
                        }

                        // Delete button
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier
                                .size(34.dp)
                                .testTag("delete_recording_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = strings.deleteRecording,
                                tint = customColors.textSecondary,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }
                }
            }

            // Waveform display
            WaveformVisualizer(
                amplitudes = amplitudes,
                isRecording = isRecording,
                playbackProgress = playbackProgress,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
            )

            // Optional volume slider
            AnimatedVisibility(
                visible = showVolumeControl && hasRecording,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeDown,
                        contentDescription = null,
                        tint = customColors.textSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Slider(
                        value = volume,
                        onValueChange = onVolumeChange,
                        valueRange = 0f..1f,
                        modifier = Modifier
                            .weight(1f)
                            .height(24.dp)
                            .testTag("volume_slider"),
                        colors = SliderDefaults.colors(
                            thumbColor = customColors.releaseButtonColor,
                            activeTrackColor = customColors.releaseButtonColor,
                            inactiveTrackColor = customColors.pillBorder
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = null,
                        tint = customColors.textSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
