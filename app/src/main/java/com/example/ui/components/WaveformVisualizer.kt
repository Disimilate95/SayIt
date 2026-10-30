package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun WaveformVisualizer(
    amplitudes: List<Float>,
    modifier: Modifier = Modifier,
    isRecording: Boolean = false,
    playbackProgress: Float = 0f,
    activeColor: Color = Color.White,
    inactiveColor: Color = Color(0x66FFFFFF),
    recordColor: Color = Color(0xFFFF5A4E),
    maxBarHeight: Dp = 36.dp,
    barWidth: Dp = 3.dp,
    barSpacing: Dp = 2.dp
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(maxBarHeight),
        horizontalArrangement = Arrangement.spacedBy(barSpacing, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val totalBars = amplitudes.size
        amplitudes.forEachIndexed { index, amp ->
            val animatedHeightFraction by animateFloatAsState(
                targetValue = amp.coerceIn(0.12f, 1f),
                animationSpec = tween(durationMillis = 80),
                label = "WaveBar_$index"
            )

            val isPlayed = if (!isRecording && totalBars > 0) {
                (index.toFloat() / totalBars.toFloat()) <= playbackProgress
            } else false

            val barColor = when {
                isRecording -> recordColor
                isPlayed -> activeColor
                else -> inactiveColor
            }

            Box(
                modifier = Modifier
                    .width(barWidth)
                    .fillMaxHeight(fraction = animatedHeightFraction)
                    .background(
                        color = barColor,
                        shape = RoundedCornerShape(2.dp)
                    )
            )
        }
    }
}
