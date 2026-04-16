package com.kglabs.wristdj.compose.components

import android.graphics.Bitmap
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun VinylRecord(
    albumArt: Bitmap?,
    progress: Float,
    isPlaying: Boolean,
    glowColor: Color = Color(0xFF00E5FF),
    secondaryColor: Color = Color(0xFFFF007F)
) {

    // ---------------- ROTATION ----------------
    var currentRotation by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            delay(16)
            currentRotation = (currentRotation + 1.5f) % 360f
        }
    }

    // ---------------- PULSE EFFECT ----------------
    var scale by remember { mutableFloatStateOf(1f) }

    LaunchedEffect(progress) {
        if (isPlaying) {
            scale = 1.05f
            delay(80)
            scale = 1f
        }
    }

    // ---------------- GLOW ANIMATION ----------------
    val infiniteTransition = rememberInfiniteTransition(label = "vinylGlow")

    val glowIntensity by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    val activeGlow = if (isPlaying) glowColor else Color.DarkGray
    val activeSecondary = if (isPlaying) secondaryColor else Color.DarkGray

    // ---------------- MAIN CONTAINER ----------------
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(250.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(CircleShape)
            .background(Color(0xFF050505))
            .drawBehind {
                if (isPlaying) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            0.0f to activeGlow.copy(alpha = 0.6f * glowIntensity),
                            0.5f to activeSecondary.copy(alpha = 0.4f * glowIntensity),
                            0.8f to activeGlow.copy(alpha = 0.2f * glowIntensity),
                            1.0f to Color.Transparent
                        ),
                        radius = size.minDimension / 2 + 20.dp.toPx()
                    )
                }
            }
    ) {

        // ---------------- PROGRESS ARC ----------------
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 14.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2

            // Base track
            drawArc(
                color = Color(0xFF222222),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset((size.width - radius * 2) / 2, (size.height - radius * 2) / 2),
                size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth)
            )

            // Progress arc with gradient
            drawArc(
                brush = Brush.sweepGradient(
                    listOf(
                        activeGlow,
                        activeSecondary,
                        activeGlow
                    )
                ),
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                topLeft = Offset((size.width - radius * 2) / 2, (size.height - radius * 2) / 2),
                size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        // ---------------- INNER DISC ----------------
        Box(
            modifier = Modifier
                .size(210.dp)
                .clip(CircleShape)
                .background(Color(0xFF0A0A0A))
                .drawBehind {
                    // Vinyl grooves
                    repeat(5) { i ->
                        drawCircle(
                            color = Color(0xFF1A1A1A),
                            radius = size.minDimension / (2.2f + i * 0.5f),
                            style = Stroke(width = 2f)
                        )
                    }
                }
                .graphicsLayer {
                    rotationZ = currentRotation
                },
            contentAlignment = Alignment.Center
        ) {

            if (albumArt != null) {
                androidx.compose.foundation.Image(
                    bitmap = albumArt.asImageBitmap(),
                    contentDescription = "Album Art",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .border(2.dp, Color(0xFF111111), CircleShape)
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {

                    Text(
                        text = if (isPlaying) "PLAYING" else "WRIST DJ",
                        color = activeGlow,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(activeSecondary)
                        )
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(activeGlow)
                        )
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(activeSecondary)
                        )
                    }
                }
            }
        }
    }
}