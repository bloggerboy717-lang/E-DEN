package com.example.ui.orb

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.preferences.OrbSettings
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CosmicOrbView(
    state: OrbState,
    settings: OrbSettings,
    modifier: Modifier = Modifier,
    size: Dp = 260.dp,
    audioRms: Float = 0f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "OrbCosmicAnimations")

    // Dynamic rotation speed based on state and user settings
    val speedMultiplier = when (state) {
        OrbState.Thinking -> 4.5f * settings.rotationSpeed
        OrbState.Listening -> 1.5f * settings.rotationSpeed
        OrbState.Speaking -> 2.2f * settings.rotationSpeed
        OrbState.Idle -> 1.0f * settings.rotationSpeed
    }

    val rotationPeriodMs = (24000 / speedMultiplier.coerceAtLeast(0.1f)).toInt().coerceIn(1200, 40000)

    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = rotationPeriodMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "OrbRotation"
    )

    // Breathing / Pulsing animation
    val pulseDuration = when (state) {
        OrbState.Thinking -> 800
        OrbState.Speaking -> 450
        OrbState.Listening -> 1200
        OrbState.Idle -> 2800
    }

    val basePulse by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = pulseDuration, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "OrbPulse"
    )

    // Ripple wave for listening / speaking
    val rippleProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "OrbRipples"
    )

    val rippleProgressSecondary by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, delayMillis = 800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "OrbRipplesSecondary"
    )

    // Smooth reactive scale target
    val reactiveScale = remember { Animatable(1.0f) }
    LaunchedEffect(state, audioRms) {
        val targetScale = when (state) {
            OrbState.Listening -> 1.06f + (audioRms * 0.12f)
            OrbState.Speaking -> 1.04f + (audioRms * 0.08f)
            OrbState.Thinking -> 1.02f
            OrbState.Idle -> 1.0f
        }
        reactiveScale.animateTo(
            targetValue = targetScale,
            animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing)
        )
    }

    val combinedScale = basePulse * reactiveScale.value * settings.sizeFactor

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val baseRadius = (this.size.minDimension / 2.85f) * combinedScale

            // 1. Draw outer ripples when Listening or Speaking
            if (state == OrbState.Listening || state == OrbState.Speaking) {
                drawRipples(
                    center = center,
                    baseRadius = baseRadius,
                    progress = rippleProgress,
                    color = settings.glowColor,
                    intensity = settings.glowIntensity
                )
                drawRipples(
                    center = center,
                    baseRadius = baseRadius,
                    progress = rippleProgressSecondary,
                    color = settings.primaryColor,
                    intensity = settings.glowIntensity * 0.8f
                )
            }

            // 2. Multi-layered soft outer glow / Corona
            drawGlowLayers(
                center = center,
                radius = baseRadius,
                glowColor = settings.glowColor,
                primaryColor = settings.primaryColor,
                intensity = settings.glowIntensity,
                isThinking = state == OrbState.Thinking
            )

            // 3. Back half of the 3D planetary ring (if rings enabled)
            val ringTiltAngle = -22f
            if (settings.showRings) {
                drawPlanetRings(
                    center = center,
                    radius = baseRadius,
                    primaryColor = settings.primaryColor,
                    secondaryColor = settings.secondaryColor,
                    glowColor = settings.glowColor,
                    tiltAngle = ringTiltAngle,
                    isBackSide = true
                )
            }

            // 4. Planet Sphere with 3D lighting, surface bands & rotation
            drawPlanetSphere(
                center = center,
                radius = baseRadius,
                primaryColor = settings.primaryColor,
                secondaryColor = settings.secondaryColor,
                glowColor = settings.glowColor,
                rotationAngle = rotationAngle,
                state = state
            )

            // 5. Front half of the 3D planetary ring (covers bottom half of sphere)
            if (settings.showRings) {
                drawPlanetRings(
                    center = center,
                    radius = baseRadius,
                    primaryColor = settings.primaryColor,
                    secondaryColor = settings.secondaryColor,
                    glowColor = settings.glowColor,
                    tiltAngle = ringTiltAngle,
                    isBackSide = false
                )
            }

            // 6. Thinking core swirl / Starlight burst
            if (state == OrbState.Thinking) {
                drawThinkingAura(
                    center = center,
                    radius = baseRadius,
                    rotationAngle = rotationAngle,
                    glowColor = settings.glowColor
                )
            }
        }
    }
}

private fun DrawScope.drawRipples(
    center: Offset,
    baseRadius: Float,
    progress: Float,
    color: Color,
    intensity: Float
) {
    val rippleRadius = baseRadius + (progress * baseRadius * 0.95f)
    val alpha = ((1f - progress) * 0.45f * intensity).coerceIn(0f, 1f)
    drawCircle(
        color = color.copy(alpha = alpha),
        radius = rippleRadius,
        center = center,
        style = Stroke(width = (2.5f * (1f - progress) + 1f).dp.toPx())
    )
}

private fun DrawScope.drawGlowLayers(
    center: Offset,
    radius: Float,
    glowColor: Color,
    primaryColor: Color,
    intensity: Float,
    isThinking: Boolean
) {
    val outerGlowRadius = radius * (1.65f + if (isThinking) 0.15f else 0.0f)

    // Outer soft aura
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                glowColor.copy(alpha = (0.35f * intensity).coerceIn(0f, 0.9f)),
                primaryColor.copy(alpha = (0.15f * intensity).coerceIn(0f, 0.7f)),
                Color.Transparent
            ),
            center = center,
            radius = outerGlowRadius
        ),
        radius = outerGlowRadius,
        center = center
    )

    // Inner bright corona
    val innerCoronaRadius = radius * 1.18f
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                glowColor.copy(alpha = (0.50f * intensity).coerceIn(0f, 0.95f)),
                glowColor.copy(alpha = (0.20f * intensity).coerceIn(0f, 0.8f)),
                Color.Transparent
            ),
            center = center,
            radius = innerCoronaRadius
        ),
        radius = innerCoronaRadius,
        center = center
    )
}

private fun DrawScope.drawPlanetSphere(
    center: Offset,
    radius: Float,
    primaryColor: Color,
    secondaryColor: Color,
    glowColor: Color,
    rotationAngle: Float,
    state: OrbState
) {
    // Clip drawing to a perfect sphere
    val spherePath = Path().apply {
        addOval(androidx.compose.ui.geometry.Rect(center.x - radius, center.y - radius, center.x + radius, center.y + radius))
    }

    clipPath(spherePath) {
        // Base sphere surface color
        drawCircle(
            color = primaryColor,
            radius = radius,
            center = center
        )

        // Draw animated rotating atmospheric bands / surface streaks
        val bandCount = 7
        val bandHeight = (radius * 2f) / (bandCount + 1)
        val rotationOffsetRad = (rotationAngle * PI.toFloat() / 180f)

        for (i in 0..bandCount) {
            val yPos = (center.y - radius) + (i * bandHeight)
            val bandPhase = (i * 0.9f) + rotationOffsetRad
            val shiftX = sin(bandPhase) * (radius * 0.45f)

            val bandColor = if (i % 2 == 0) {
                secondaryColor.copy(alpha = 0.55f)
            } else {
                glowColor.copy(alpha = 0.35f)
            }

            // Draw sinusoidal atmospheric strip
            val bandPath = Path().apply {
                moveTo(center.x - radius * 1.2f, yPos)
                cubicTo(
                    center.x - radius * 0.5f + shiftX, yPos - (bandHeight * 0.4f),
                    center.x + radius * 0.5f - shiftX, yPos + (bandHeight * 0.4f),
                    center.x + radius * 1.2f, yPos
                )
                lineTo(center.x + radius * 1.2f, yPos + bandHeight * 0.65f)
                cubicTo(
                    center.x + radius * 0.5f - shiftX, yPos + (bandHeight * 0.9f),
                    center.x - radius * 0.5f + shiftX, yPos + (bandHeight * 0.2f),
                    center.x - radius * 1.2f, yPos + bandHeight * 0.65f
                )
                close()
            }
            drawPath(path = bandPath, color = bandColor)
        }

        // 3D Directional Lighting: Light source coming from top-left (illuminated crest)
        val lightCenter = Offset(center.x - (radius * 0.35f), center.y - (radius * 0.35f))
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.65f),
                    glowColor.copy(alpha = 0.30f),
                    Color.Transparent
                ),
                center = lightCenter,
                radius = radius * 0.95f
            ),
            radius = radius,
            center = center
        )

        // 3D Terminator Shadow (darkness on bottom-right side of sphere)
        val shadowCenter = Offset(center.x + (radius * 0.45f), center.y + (radius * 0.45f))
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.Transparent,
                    Color.Black.copy(alpha = 0.35f),
                    Color.Black.copy(alpha = 0.85f)
                ),
                center = shadowCenter,
                radius = radius * 1.25f
            ),
            radius = radius,
            center = center
        )

        // Atmospheric rim specular light (edge halo)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.Transparent,
                    glowColor.copy(alpha = 0.15f),
                    glowColor.copy(alpha = 0.75f)
                ),
                center = center,
                radius = radius
            ),
            radius = radius,
            center = center
        )
    }
}

private fun DrawScope.drawPlanetRings(
    center: Offset,
    radius: Float,
    primaryColor: Color,
    secondaryColor: Color,
    glowColor: Color,
    tiltAngle: Float,
    isBackSide: Boolean
) {
    val ringWidth = radius * 2.5f
    val ringHeight = radius * 0.68f

    rotate(degrees = tiltAngle, pivot = center) {
        val ringRect = androidx.compose.ui.geometry.Rect(
            center.x - ringWidth,
            center.y - ringHeight,
            center.x + ringWidth,
            center.y + ringHeight
        )

        // Clip to top half for back-side, or bottom half for front-side
        val clipBound = if (isBackSide) {
            Path().apply {
                addRect(
                    androidx.compose.ui.geometry.Rect(
                        center.x - ringWidth * 1.2f,
                        center.y - ringHeight * 1.5f,
                        center.x + ringWidth * 1.2f,
                        center.y
                    )
                )
            }
        } else {
            Path().apply {
                addRect(
                    androidx.compose.ui.geometry.Rect(
                        center.x - ringWidth * 1.2f,
                        center.y,
                        center.x + ringWidth * 1.2f,
                        center.y + ringHeight * 1.5f
                    )
                )
            }
        }

        clipPath(clipBound) {
            // Main wide planetary ring
            drawOval(
                brush = Brush.linearGradient(
                    colors = listOf(
                        glowColor.copy(alpha = 0.15f),
                        primaryColor.copy(alpha = 0.75f),
                        glowColor.copy(alpha = 0.90f),
                        secondaryColor.copy(alpha = 0.65f),
                        glowColor.copy(alpha = 0.15f)
                    ),
                    start = Offset(ringRect.left, center.y),
                    end = Offset(ringRect.right, center.y)
                ),
                topLeft = Offset(ringRect.left, ringRect.top),
                size = Size(ringRect.width, ringRect.height),
                style = Stroke(width = radius * 0.16f, cap = StrokeCap.Round)
            )

            // Inner razor thin ring
            val innerRingRect = androidx.compose.ui.geometry.Rect(
                center.x - (ringWidth * 0.82f),
                center.y - (ringHeight * 0.82f),
                center.x + (ringWidth * 0.82f),
                center.y + (ringHeight * 0.82f)
            )
            drawOval(
                color = glowColor.copy(alpha = 0.50f),
                topLeft = Offset(innerRingRect.left, innerRingRect.top),
                size = Size(innerRingRect.width, innerRingRect.height),
                style = Stroke(width = 2.dp.toPx())
            )

            // Outer starlight ring particle border
            val outerRingRect = androidx.compose.ui.geometry.Rect(
                center.x - (ringWidth * 1.08f),
                center.y - (ringHeight * 1.08f),
                center.x + (ringWidth * 1.08f),
                center.y + (ringHeight * 1.08f)
            )
            drawOval(
                color = glowColor.copy(alpha = 0.30f),
                topLeft = Offset(outerRingRect.left, outerRingRect.top),
                size = Size(outerRingRect.width, outerRingRect.height),
                style = Stroke(width = 1.5.dp.toPx())
            )
        }
    }
}

private fun DrawScope.drawThinkingAura(
    center: Offset,
    radius: Float,
    rotationAngle: Float,
    glowColor: Color
) {
    rotate(degrees = -rotationAngle * 2.5f, pivot = center) {
        val rayCount = 8
        for (i in 0 until rayCount) {
            val angleDeg = (i * 360f / rayCount)
            val rad = angleDeg * PI.toFloat() / 180f
            val endX = center.x + cos(rad) * (radius * 1.35f)
            val endY = center.y + sin(rad) * (radius * 1.35f)

            drawLine(
                brush = Brush.linearGradient(
                    colors = listOf(Color.White.copy(alpha = 0.85f), glowColor.copy(alpha = 0.5f), Color.Transparent),
                    start = center,
                    end = Offset(endX, endY)
                ),
                start = center,
                end = Offset(endX, endY),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}
