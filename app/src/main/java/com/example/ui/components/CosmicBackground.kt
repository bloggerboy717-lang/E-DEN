package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
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
import com.example.ui.theme.CosmicPurple
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.NebulaViolet
import com.example.ui.theme.SpaceBlack
import kotlin.math.sin
import kotlin.random.Random

private data class StarParticle(
    val xRatio: Float,
    val yRatio: Float,
    val radius: Float,
    val baseAlpha: Float,
    val twinklePhase: Float,
    val color: Color
)

@Composable
fun CosmicBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val stars = remember {
        val random = Random(42)
        val starColors = listOf(
            Color.White,
            Color(0xFFE0E7FF),
            Color(0xFFBAE6FD),
            Color(0xFFDDD6FE)
        )
        List(75) {
            StarParticle(
                xRatio = random.nextFloat(),
                yRatio = random.nextFloat(),
                radius = random.nextFloat() * 1.6f + 0.6f,
                baseAlpha = random.nextFloat() * 0.5f + 0.25f,
                twinklePhase = random.nextFloat() * 6.28f,
                color = starColors[random.nextInt(starColors.size)]
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "StarfieldTwinkle")
    val twinkleTimer by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "TwinkleTimer"
    )

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Deep background gradient (near-black navy to cosmic purple/indigo)
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        SpaceBlack,
                        DeepNavy,
                        CosmicPurple.copy(alpha = 0.85f),
                        SpaceBlack
                    ),
                    startY = 0f,
                    endY = height
                )
            )

            // Subtle nebula ambient glow clouds
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        NebulaViolet.copy(alpha = 0.12f),
                        Color.Transparent
                    ),
                    center = Offset(width * 0.25f, height * 0.35f),
                    radius = width * 0.65f
                ),
                radius = width * 0.65f,
                center = Offset(width * 0.25f, height * 0.35f)
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        DeepNavy.copy(alpha = 0.18f),
                        Color.Transparent
                    ),
                    center = Offset(width * 0.75f, height * 0.70f),
                    radius = width * 0.75f
                ),
                radius = width * 0.75f,
                center = Offset(width * 0.75f, height * 0.70f)
            )

            // Animated Star Particles
            stars.forEach { star ->
                val dynamicAlpha = (star.baseAlpha + sin(twinkleTimer + star.twinklePhase) * 0.35f)
                    .coerceIn(0.1f, 0.95f)
                drawCircle(
                    color = star.color.copy(alpha = dynamicAlpha),
                    radius = star.radius,
                    center = Offset(star.xRatio * width, star.yRatio * height)
                )
            }
        }

        content()
    }
}
