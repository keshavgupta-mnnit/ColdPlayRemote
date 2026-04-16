package com.kglabs.wristdj.compose.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ColorHeroVisualizer(
    targetColor: Color,
    isTransmitting: Boolean,
    onClick: () -> Unit
) {
    // Smooth color transition
    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "hero_color"
    )

    // --- BREATHING ANIMATION (SLOW + PREMIUM) ---
    val infiniteTransition = rememberInfiniteTransition(label = "breathing")

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    // --- TAP FEEDBACK ---
    var tapped by remember { mutableStateOf(false) }

    val tapScale by animateFloatAsState(
        targetValue = if (tapped) 0.95f else 1f,
        animationSpec = tween(120),
        label = "tap"
    )

    LaunchedEffect(tapped) {
        if (tapped) {
            kotlinx.coroutines.delay(120)
            tapped = false
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(250.dp)
            .graphicsLayer {
                val finalScale = if (isTransmitting) scale else 1f
                scaleX = finalScale * tapScale
                scaleY = finalScale * tapScale
            }
            .drawBehind {
                if (isTransmitting) {
                    // OUTER GLOW (strong)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                animatedColor.copy(alpha = glowAlpha),
                                Color.Transparent
                            ),
                            center = center,
                            radius = size.width / 1.3f
                        ),
                        radius = size.width / 1.3f
                    )

                    // INNER SOFT GLOW
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                animatedColor.copy(alpha = 0.25f),
                                Color.Transparent
                            ),
                            center = center,
                            radius = size.width / 2f
                        ),
                        radius = size.width / 2f
                    )
                }
            }
            .clip(CircleShape)
            .background(Color(0xFF050505))
            .clickable {
                tapped = true
                onClick()
            }
    ) {

        // INNER AMBIENT LIGHT
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            animatedColor.copy(alpha = 0.35f),
                            Color.Transparent
                        )
                    )
                )
        )

        // RINGS (NEON STYLE)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(
                    width = 14.dp,
                    color = animatedColor,
                    shape = CircleShape
                )
                .padding(22.dp)
                .border(
                    width = 3.dp,
                    color = animatedColor.copy(alpha = 0.4f),
                    shape = CircleShape
                )
        )

        // TEXT
        if (isTransmitting) {
            Text(
                text = "GLOWING...",
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 18.sp
            )
        } else {
            Text(
                text = "TAP TO\nHOLD GLOW",
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}
