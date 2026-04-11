package com.kglabs.wristdj.compose.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "hero_color"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isTransmitting) 1.08f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(240.dp)
            .scale(pulseScale)
            .drawBehind {
                if (isTransmitting) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(animatedColor.copy(alpha = 0.6f), Color.Transparent),
                            center = center,
                            radius = size.width / 1.5f
                        ),
                        radius = size.width / 1.5f
                    )
                }
            }
            .clip(CircleShape)
            .background(Color(0xFF050505))
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            animatedColor.copy(alpha = 0.4f),
                            Color.Transparent
                        )
                    )
                )
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(width = 12.dp, color = animatedColor, shape = CircleShape)
                .padding(24.dp)
                .border(width = 4.dp, color = Color(0x40FFFFFF), shape = CircleShape)
        )

        if (isTransmitting) {
            Text(
                text = "GLOWING...",
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 16.sp
            )
        } else {
            Text(
                text = "TAP TO\nHOLD GLOW",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}
