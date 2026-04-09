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
    var currentRotation by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            delay(16)
            currentRotation = (currentRotation + 1.5f) % 360f
        }
    }

    val activeGlow = if (isPlaying) glowColor else Color.DarkGray
    val activeSecondary = if (isPlaying) secondaryColor else Color.DarkGray
    
    val infiniteTransition = rememberInfiniteTransition(label = "vinylGlow")
    val glowIntensity by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "intensity"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(240.dp)
            .clip(CircleShape)
            .background(Color(0xFF050505))
            .drawBehind {
                if (isPlaying) {
                    // Backlit glow with dual colors matching the play button
                    drawCircle(
                        brush = Brush.radialGradient(
                            0.0f to activeGlow.copy(alpha = 0.5f * glowIntensity),
                            0.6f to activeSecondary.copy(alpha = 0.3f * glowIntensity),
                            0.9f to activeGlow.copy(alpha = 0.1f * glowIntensity),
                            1.0f to Color.Transparent,
                            center = center,
                            radius = 118.dp.toPx()
                        ),
                        radius = 118.dp.toPx()
                    )
                }
            }
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 12.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2
            
            drawArc(
                color = Color(0xFF222222),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset((size.width - radius * 2) / 2, (size.height - radius * 2) / 2),
                size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth)
            )
            drawArc(
                color = activeGlow,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                topLeft = Offset((size.width - radius * 2) / 2, (size.height - radius * 2) / 2),
                size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        Box(
            modifier = Modifier
                .size(200.dp)
                .clip(CircleShape)
                .background(Color(0xFF0A0A0A))
                .drawBehind {
                    drawCircle(color = Color(0xFF1A1A1A), radius = size.minDimension / 2.2f, style = Stroke(width = 2f))
                    drawCircle(color = Color(0xFF1A1A1A), radius = size.minDimension / 2.6f, style = Stroke(width = 2f))
                    drawCircle(color = Color(0xFF1A1A1A), radius = size.minDimension / 3.2f, style = Stroke(width = 2f))
                }
                .graphicsLayer { rotationZ = currentRotation },
            contentAlignment = Alignment.Center
        ) {
            if (albumArt != null) {
                androidx.compose.foundation.Image(
                    bitmap = albumArt.asImageBitmap(),
                    contentDescription = "Album Art",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(90.dp).clip(CircleShape).border(2.dp, Color(0xFF111111), CircleShape)
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = if (isPlaying) "PLAYING" else "WRIST DJ", color = activeGlow, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(activeSecondary))
                        Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(activeGlow))
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(activeSecondary))
                    }
                }
            }
        }
    }
}
