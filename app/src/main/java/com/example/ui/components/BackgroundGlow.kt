package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.LocalSayItColors
import kotlin.random.Random

private data class CelestialStar(
    val xRatio: Float,
    val yRatio: Float,
    val size: Float,
    val baseAlpha: Float
)

@Composable
fun BackgroundGlow(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val customColors = LocalSayItColors.current
    val infiniteTransition = rememberInfiniteTransition(label = "BackgroundGlowTransition")

    // Slow organic floating animations for ambient light orbs
    val animOffset1X by infiniteTransition.animateFloat(
        initialValue = 0.20f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 16000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Orb1X"
    )

    val animOffset1Y by infiniteTransition.animateFloat(
        initialValue = 0.18f,
        targetValue = 0.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 20000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Orb1Y"
    )

    val animOffset2X by infiniteTransition.animateFloat(
        initialValue = 0.82f,
        targetValue = 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 18000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Orb2X"
    )

    val animOffset2Y by infiniteTransition.animateFloat(
        initialValue = 0.78f,
        targetValue = 0.48f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 22000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Orb2Y"
    )

    // Gentle twinkle
    val twinkleFactor by infiniteTransition.animateFloat(
        initialValue = 0.65f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "StarTwinkle"
    )

    val stars = remember {
        val rand = Random(42)
        List(24) {
            CelestialStar(
                xRatio = rand.nextFloat(),
                yRatio = rand.nextFloat() * 0.85f + 0.05f,
                size = rand.nextFloat() * 1.5f + 1.0f,
                baseAlpha = rand.nextFloat() * 0.35f + 0.15f
            )
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Main vertical gradient background
            drawRect(
                brush = Brush.verticalGradient(
                    colors = customColors.bgGradient,
                    startY = 0f,
                    endY = height
                ),
                size = size
            )

            // Distant subtle stars for a tranquil cosmic sensation
            val starBaseColor = if (customColors.isDark) Color.White else Color(0xFF64748B)
            stars.forEach { star ->
                val starX = width * star.xRatio
                val starY = height * star.yRatio
                val alpha = (star.baseAlpha * twinkleFactor).coerceIn(0f, 1f)
                drawCircle(
                    color = starBaseColor.copy(alpha = alpha),
                    radius = star.size,
                    center = Offset(starX, starY)
                )
            }

            // First floating ethereal glowing orb
            val orb1Center = Offset(width * animOffset1X, height * animOffset1Y)
            val orb1Radius = width * 0.85f
            val orb1Color = if (customColors.isDark) Color(0x2038BDF8) else Color(0x180284C7)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(orb1Color, Color.Transparent),
                    center = orb1Center,
                    radius = orb1Radius
                ),
                center = orb1Center,
                radius = orb1Radius
            )

            // Second floating glowing orb
            val orb2Center = Offset(width * animOffset2X, height * animOffset2Y)
            val orb2Radius = width * 0.90f
            val orb2Color = if (customColors.isDark) Color(0x18818CF8) else Color(0x146366F1)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(orb2Color, Color.Transparent),
                    center = orb2Center,
                    radius = orb2Radius
                ),
                center = orb2Center,
                radius = orb2Radius
            )
        }

        content()
    }
}
