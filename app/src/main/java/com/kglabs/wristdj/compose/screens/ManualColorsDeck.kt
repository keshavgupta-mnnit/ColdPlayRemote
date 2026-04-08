package com.kglabs.wristdj.compose.screens


import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kglabs.wristdj.utils.BandColorConstants
import com.kglabs.wristdj.utils.IRUtils
import kotlinx.coroutines.delay

@Composable
fun ManualColorsDeck() {
    val colorToSignalMap = remember { BandColorConstants.buttons.toMap() }

    // --- STATE MANAGEMENT ---
    var activeUiColor by remember { mutableStateOf(Color(0xFF00E5FF)) } // Defaults to Cyan
    var activeSignal by remember { mutableStateOf(colorToSignalMap["Cyan"]) }

    // Toggle for continuous transmission
    var isTransmittingContinuously by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    // --- CONTINUOUS FIRING LOOP ---
    // This coroutine runs while isTransmittingContinuously is true
    LaunchedEffect(isTransmittingContinuously, activeSignal) {
        if (isTransmittingContinuously && activeSignal != null) {
            while (true) {
                IRUtils.transmitSignal(activeSignal!!)
                delay(300) // Delay between shots. 300ms creates a smooth, continuous effect
            }
        }
    }

    // Stop transmitting if the user navigates away from this tab
    DisposableEffect(Unit) {
        onDispose {
            isTransmittingContinuously = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Wrist DJ - Colors",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 16.dp, bottom = 32.dp)
        )

        // 1. The Hero Visualizer (Now interactive!)
        ColorHeroVisualizer(
            targetColor = activeUiColor,
            isTransmitting = isTransmittingContinuously,
            onClick = {
                // Toggle the continuous firing mode when tapped
                isTransmittingContinuously = !isTransmittingContinuously
            }
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 2. Scrollable Category Grids
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(scrollState)
        ) {
            ColorCategoryGrid(
                title = "Bass & Warmth",
                colorNames = BandColorConstants.bassColors,
                colorToSignalMap = colorToSignalMap,
                onColorSelected = { uiColor, signal ->
                    activeUiColor = uiColor
                    activeSignal = signal
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            ColorCategoryGrid(
                title = "Mid & Synth",
                colorNames = BandColorConstants.midColors,
                colorToSignalMap = colorToSignalMap,
                onColorSelected = { uiColor, signal ->
                    activeUiColor = uiColor
                    activeSignal = signal
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            ColorCategoryGrid(
                title = "High Frequencies",
                colorNames = BandColorConstants.highColors,
                colorToSignalMap = colorToSignalMap,
                onColorSelected = { uiColor, signal ->
                    activeUiColor = uiColor
                    activeSignal = signal
                }
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

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

    // The pulsing "heartbeat" animation when continuous mode is active
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

    // --- THE PERFECTED VISUALIZER ---
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(200.dp)
            .scale(pulseScale)
            // 1. Cast the shadow on the solid parent container (Fixes the octagon bug entirely!)
            .shadow(elevation = 24.dp, shape = CircleShape, spotColor = animatedColor)
            .clip(CircleShape) // Ensures the touch ripple is a perfect circle
            .background(Color(0xFF050505)) // A solid, near-black base to cast the perfect shadow
            .clickable { onClick() }
    ) {
        // 2. The Inner Gradient Glow (The effect you wanted back)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            animatedColor.copy(alpha = 0.4f), // Bright glowing center
                            Color.Transparent // Fades smoothly to the edges
                        )
                    )
                )
        )

        // 3. The Thick Outer Rings (Exactly from your original code)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(width = 12.dp, color = animatedColor, shape = CircleShape)
                .padding(24.dp)
                .border(width = 4.dp, color = Color(0x40FFFFFF), shape = CircleShape)
        )

        // 4. Text Feedback
        if (isTransmitting) {
            Text(
                text = "GLOWING...",
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 16.sp
            )
        } else {
            androidx.compose.material3.Text(
                text = "TAP TO\nHOLD GLOW",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun ColorCategoryGrid(
    title: String,
    colorNames: List<String>,
    colorToSignalMap: Map<String, String>,
    onColorSelected: (Color, String?) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        val chunkedColors = colorNames.chunked(4)

        chunkedColors.forEach { rowColors ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                rowColors.forEach { colorName ->
                    val uiColor = BandColorConstants.getColorByName(colorName)

                    Box(modifier = Modifier.padding(end = 16.dp)) {
                        ColorSwatchButton(
                            uiColor = uiColor,
                            onClick = {
                                val signalString = colorToSignalMap[colorName]

                                // 1. Update the UI state and active signal
                                onColorSelected(uiColor, signalString)

                                // 2. Fire one immediate shot on tap
                                if (signalString != null) {
                                    IRUtils.transmitSignal(signalString)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ColorSwatchButton(uiColor: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(uiColor)
            .border(2.dp, Color(0x20FFFFFF), CircleShape)
            .clickable { onClick() }
    )
}