package com.kglabs.wristdj.compose.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

@Composable
fun StudioBackground(
    showTopGlow: Boolean = false,
    glowColor: Color = Color.White,
    isRainbowGlow: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    val rainbowColors = listOf(
        Color(0xFFFF007F), Color(0xFF7F00FF), Color(0xFF00E5FF),
        Color(0xFF00FF00), Color(0xFFFFFF00), Color(0xFFFF0000), Color(0xFFFF007F)
    )

    val infiniteTransition = rememberInfiniteTransition(label = "illumination")
    val illuminationAlpha by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF1A1A24), Color(0xFF050505)),
                    radius = 1500f
                )
            )
    ) {
        if (showTopGlow) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .graphicsLayer(alpha = 0.99f)
                    .drawWithContent {
                        drawContent()
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color.White, Color.Transparent),
                                startY = 0f,
                                endY = size.height
                            ),
                            blendMode = BlendMode.DstIn
                        )
                    }
                    .background(
                        brush = if (isRainbowGlow) {
                            Brush.sweepGradient(
                                colors = rainbowColors.map { it.copy(alpha = illuminationAlpha * 0.6f) }
                            )
                        } else {
                            Brush.verticalGradient(
                                colors = listOf(
                                    glowColor.copy(alpha = illuminationAlpha),
                                    Color.Transparent
                                )
                            )
                        }
                    )
            )
        }
        content()
    }
}
