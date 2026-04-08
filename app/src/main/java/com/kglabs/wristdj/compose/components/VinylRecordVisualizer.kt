package com.kglabs.wristdj.compose.components

// Add this to your imports at the top
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun VinylRecordVisualizer(isPlaying: Boolean, trackName: String) {
    // The rotation animation for the "vinyl" record
    val infiniteTransition = rememberInfiniteTransition(label = "vinyl_rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (isPlaying) 360f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(240.dp)
    ) {
        // Outer Glowing Orange Ring
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(
                    width = 4.dp,
                    color = Color(0xFFFFA500), // Orange
                    shape = CircleShape
                )
                .shadow(
                    elevation = if (isPlaying) 16.dp else 0.dp,
                    shape = CircleShape,
                    spotColor = Color(0xFFFFA500)
                )
        )

        // The Inner "Vinyl" Grooves and Label
        Box(
            modifier = Modifier
                .size(200.dp)
                .clip(CircleShape)
                .background(Color(0xFF1A1A1A)) // Dark grey vinyl color
                .drawBehind {
                    // Draw the faint vinyl grooves
                    drawCircle(color = Color(0xFF333333), radius = size.minDimension / 2.2f, style = Stroke(width = 2f))
                    drawCircle(color = Color(0xFF333333), radius = size.minDimension / 2.6f, style = Stroke(width = 2f))
                    drawCircle(color = Color(0xFF333333), radius = size.minDimension / 3.2f, style = Stroke(width = 2f))
                }
                .graphicsLayer { rotationZ = rotation },
            contentAlignment = Alignment.Center
        ) {
            // The center record label (you could put album art here later!)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (trackName == "No Track Selected") "WRIST DJ" else "PLAYING",
                    color = Color(0xFFFFA500),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                // A decorative center hole and planetary dots matching the mockup
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFFF007F)))
                    Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(Color(0xFFFFA500))) // Center hole
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF00E5FF)))
                }
            }
        }
    }
}

@Composable
fun GlowingMediaButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    glowColor: Color,
    onClick: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .border(2.dp, glowColor, CircleShape)
            .clickable { onClick() }
            .shadow(8.dp, CircleShape, spotColor = glowColor)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = glowColor, modifier = Modifier.size(32.dp))
    }
}