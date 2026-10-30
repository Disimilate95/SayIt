package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlin.math.sin
import kotlin.random.Random

private data class DissolveParticle(
    val initialXRatio: Float,
    val initialYRatio: Float,
    val size: Float,
    val maxAlpha: Float,
    val floatDistance: Float,
    val frequency: Float,
    val phase: Float,
    val color: Color
)

@Composable
fun DissolveParticlesOverlay(
    progress: Float,
    modifier: Modifier = Modifier
) {
    if (progress <= 0f || progress >= 1f) return

    val particles = remember {
        val rand = Random(1337)
        val palette = listOf(
            Color(0xFF80DEEA), // Soft cyan
            Color(0xFF90CAF9), // Ethereal sky blue
            Color(0xFFFFE082), // Golden warm light
            Color(0xFFFFFFFF), // Pure starlight white
            Color(0xFFB388FF)  // Gentle lavender
        )

        List(48) {
            val baseColor = palette[rand.nextInt(palette.size)]
            DissolveParticle(
                initialXRatio = rand.nextFloat() * 0.86f + 0.07f,
                initialYRatio = rand.nextFloat() * 0.70f + 0.20f,
                size = rand.nextFloat() * 4.0f + 2.0f,
                maxAlpha = rand.nextFloat() * 0.55f + 0.40f,
                floatDistance = rand.nextFloat() * 220f + 110f,
                frequency = rand.nextFloat() * 3.5f + 1.5f,
                phase = rand.nextFloat() * Math.PI.toFloat(),
                color = baseColor
            )
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        particles.forEach { p ->
            // Particles fade in softly, float upwards with slight acceleration, and fade out towards top
            val particleAlpha = when {
                progress < 0.15f -> (progress / 0.15f) * p.maxAlpha
                progress > 0.70f -> ((1f - progress) / 0.30f) * p.maxAlpha
                else -> p.maxAlpha
            }.coerceIn(0f, 1f)

            // Non-linear upward movement (gently accelerating like smoke/light)
            val easedProgress = progress * progress * 0.3f + progress * 0.7f
            val currentY = (height * p.initialYRatio) - (easedProgress * p.floatDistance * density)
            val swayX = sin((progress * p.frequency * Math.PI.toFloat()) + p.phase) * 16f * density
            val currentX = (width * p.initialXRatio) + swayX

            // Soft glowing outer corona
            drawCircle(
                color = p.color.copy(alpha = particleAlpha * 0.35f),
                radius = p.size * 2.8f,
                center = Offset(currentX, currentY)
            )

            // Inner bright core
            drawCircle(
                color = p.color.copy(alpha = particleAlpha),
                radius = p.size,
                center = Offset(currentX, currentY)
            )
        }
    }
}
