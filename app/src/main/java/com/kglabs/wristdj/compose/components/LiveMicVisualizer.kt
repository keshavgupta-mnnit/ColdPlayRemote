package com.kglabs.wristdj.compose.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

private val rainbowColors = listOf(
    Color(0xFFFF007F), Color(0xFF7F00FF), Color(0xFF00E5FF),
    Color(0xFF00FF00), Color(0xFFFFFF00), Color(0xFFFF0000), Color(0xFFFF007F)
)

@Composable
fun LiveMicVisualizer(isListening: Boolean, illuminationAlpha: Float) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
    ) {
        BackgroundWaveform(isListening = isListening)
        
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(240.dp)
        ) {
            StableRingsVisualizer(isListening = isListening, illuminationAlpha = illuminationAlpha)
        }
    }
}

@Composable
private fun BackgroundWaveform(isListening: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(3500, easing = LinearEasing)),
        label = "phase"
    )

    val waveBrush = Brush.horizontalGradient(colors = rainbowColors)

    Canvas(modifier = Modifier.fillMaxSize()) {
        val barWidth = 3.dp.toPx()
        val gap = 3.dp.toPx()
        val step = barWidth + gap
        val totalBars = (size.width / step).toInt()
        val maxLineHeight = size.height * 0.4f

        for (i in 0 until totalBars) {
            val normalizedPos = (i.toFloat() / (totalBars - 1)) * 2 - 1
            val masterTaper = cos(normalizedPos * Math.PI / 2).toFloat()
            val repeatingBumps = abs(cos(normalizedPos * Math.PI * 2.5 - phase)).toFloat()
            val envelope = masterTaper * repeatingBumps

            val heightMultiplier = if (isListening) {
                val noise = abs(sin((i * 0.3f) - (phase * 1.2f))) * 0.6f + 0.4f
                envelope * noise
            } else {
                envelope * 0.15f
            }

            val currentHeight = maxLineHeight * heightMultiplier
            val xPos = i * step + (barWidth / 2)

            drawLine(
                brush = waveBrush,
                start = Offset(xPos, size.height / 2 - (currentHeight / 2)),
                end = Offset(xPos, size.height / 2 + (currentHeight / 2)),
                strokeWidth = barWidth,
                cap = StrokeCap.Round,
                alpha = if (isListening) 1.0f else 0.2f
            )
        }
    }
}

@Composable
private fun StableRingsVisualizer(isListening: Boolean, illuminationAlpha: Float) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2, size.height / 2)
        val maxRadius = size.width / 2

        // Standardized 12dp stroke touching the edge
        val ringStrokeWidth = 12.dp.toPx()
        val outerRadius = maxRadius - (ringStrokeWidth / 2)
        
        val baseInnerRadius = maxRadius - ringStrokeWidth - 3.dp.toPx()
        val ringGap = 3.dp.toPx()

        val step1Radius = baseInnerRadius - ringGap
        val step2Radius = step1Radius - ringGap
        val step3Radius = step2Radius - ringGap

        val ringBrush = Brush.sweepGradient(rainbowColors, center)
        val dotStrokeWidth = 2.dp.toPx()

        val stableDottedEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 8f), 0f)

        if (isListening) {
            drawCircle(
                brush = Brush.radialGradient(
                    0.0f to Color.Transparent,
                    0.7f to Color.Transparent,
                    0.85f to Color.White.copy(alpha = illuminationAlpha * 0.3f),
                    1.0f to Color.Transparent,
                    center = center,
                    radius = maxRadius + 40.dp.toPx()
                ),
                radius = maxRadius + 40.dp.toPx()
            )

            drawCircle(
                brush = Brush.sweepGradient(rainbowColors, center),
                radius = maxRadius,
                alpha = illuminationAlpha * 0.3f,
                style = Stroke(width = 32.dp.toPx())
            )
        }

        drawCircle(
            brush = Brush.radialGradient(
                0.0f to Color.Transparent,
                0.55f to Color.Transparent, 
                0.7f to Color.Black.copy(alpha = 0.8f),
                0.95f to Color.Black.copy(alpha = 0.8f),
                1.0f to Color.Transparent,
                center = center,
                radius = maxRadius
            )
        )

        drawCircle(
            brush = ringBrush,
            radius = outerRadius,
            style = Stroke(width = 12.dp.toPx())
        )

        if (isListening) {
            drawCircle(brush = ringBrush, radius = baseInnerRadius, alpha = 1.0f, style = Stroke(width = dotStrokeWidth, cap = StrokeCap.Round, pathEffect = stableDottedEffect))
            drawCircle(brush = ringBrush, radius = step1Radius, alpha = 0.75f, style = Stroke(width = dotStrokeWidth, cap = StrokeCap.Round, pathEffect = stableDottedEffect))
            drawCircle(brush = ringBrush, radius = step2Radius, alpha = 0.50f, style = Stroke(width = dotStrokeWidth, cap = StrokeCap.Round, pathEffect = stableDottedEffect))
            drawCircle(brush = ringBrush, radius = step3Radius, alpha = 0.25f, style = Stroke(width = dotStrokeWidth, cap = StrokeCap.Round, pathEffect = stableDottedEffect))
        } else {
            drawCircle(color = Color.DarkGray, radius = baseInnerRadius, style = Stroke(width = dotStrokeWidth, cap = StrokeCap.Round, pathEffect = stableDottedEffect))
        }
    }
}
