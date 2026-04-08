package com.kglabs.wristdj.compose.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun RainbowWaveformVisualizer(isSyncing: Boolean) {
    // The sweep gradient matching your mockup
    val rainbowBrush = Brush.sweepGradient(
        listOf(
            Color(0xFFFF007F), // Pink
            Color(0xFF7F00FF), // Purple
            Color(0xFF00E5FF), // Cyan
            Color(0xFF00FF00), // Green
            Color(0xFFFFFF00), // Yellow
            Color(0xFFFF0000), // Red
            Color(0xFFFF007F)  // Back to Pink for smooth loop
        )
    )

    // A subtle rotation animation for the outer ring when active
    val infiniteTransition = rememberInfiniteTransition(label = "ring_rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (isSyncing) 360f else 0f,
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
        // Outer Glowing Ring
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(width = 12.dp, brush = rainbowBrush, shape = CircleShape)
                .padding(24.dp)
                .border(width = 4.dp, color = Color(0x40FFFFFF), shape = CircleShape) // Inner subtle ring
        )

        // Animated Waveform Bars in the center
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.height(80.dp)
        ) {
            // Generate 11 bars to match the mockup style
            val barHeights = listOf(0.3f, 0.5f, 0.8f, 0.4f, 1.0f, 0.6f, 0.9f, 0.5f, 0.7f, 0.4f, 0.2f)
            barHeights.forEachIndexed { index, targetHeight ->
                AnimatedWaveformBar(
                    targetHeight = targetHeight,
                    isSyncing = isSyncing,
                    delayMs = index * 50
                )
            }
        }
    }
}

@Composable
fun AnimatedWaveformBar(targetHeight: Float, isSyncing: Boolean, delayMs: Int) {
    var heightMultiplier by remember { mutableStateOf(0.1f) }

    LaunchedEffect(isSyncing) {
        if (isSyncing) {
            delay(delayMs.toLong())
            while (true) {
                heightMultiplier = targetHeight
                delay(150)
                heightMultiplier = 0.2f
                delay(150)
            }
        } else {
            heightMultiplier = 0.1f
        }
    }

    val animatedHeight by animateFloatAsState(
        targetValue = heightMultiplier,
        animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
        label = "bar_height"
    )

    // Using a gradient for the bars to match the left-to-right color shift
    Box(
        modifier = Modifier
            .width(6.dp)
            .fillMaxHeight(animatedHeight)
            .clip(RoundedCornerShape(50))
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF00E5FF), Color(0xFFFF007F))
                )
            )
    )
}

@Composable
fun CustomGlowingToggle(isOn: Boolean, onToggle: () -> Unit) {
    val trackColor by animateColorAsState(
        targetValue = if (isOn) Color(0xFF004D00) else Color(0xFF222222),
        label = "track_color"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isOn) Color(0xFF00FF00) else Color.DarkGray,
        label = "border_color"
    )

    Box(
        contentAlignment = if (isOn) Alignment.CenterStart else Alignment.CenterEnd,
        modifier = Modifier
            .width(140.dp)
            .height(60.dp)
            .clip(RoundedCornerShape(50))
            .background(trackColor)
            .border(2.dp, borderColor, RoundedCornerShape(50))
            .clickable { onToggle() }
            .padding(8.dp)
    ) {
        if (isOn) {
            Text(
                text = "ON",
                color = Color(0xFF00FF00),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp)
            )
        }

        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color.White)
                .shadow(if (isOn) 8.dp else 0.dp, CircleShape, spotColor = Color(0xFF00FF00))
        )
    }
}