package com.kglabs.wristdj.compose.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kglabs.wristdj.utils.BandColorConstants
import com.kglabs.wristdj.utils.IRUtils

@Composable
fun ManualColorsDeck() { // Zero parameters!
    // State to track the currently active color for the top visualizer (Defaults to Cyan)
    var activeUiColor by remember { mutableStateOf(Color(0xFF00E5FF)) }

    // Look up map for IR strings
    val colorToSignalMap = remember { BandColorConstants.buttons.toMap() }

    // Scroll state for the color categories
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App Header
        Text(
            text = "Wrist DJ - Colors",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 16.dp, bottom = 32.dp)
        )

        // 1. The Hero Visualizer (Changes color smoothly when a button is tapped)
        ColorHeroVisualizer(targetColor = activeUiColor)

        Spacer(modifier = Modifier.height(32.dp))

        // 2. Scrollable Category Grids
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(scrollState)
        ) {
            // Category 1: Bass / Warm Colors (Mockup: "Fire & Ice" / "Classics")
            ColorCategoryGrid(
                title = "Bass & Warmth",
                colorNames = BandColorConstants.bassColors,
                colorToSignalMap = colorToSignalMap,
                onColorSelected = { uiColor -> activeUiColor = uiColor }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Category 2: Mid / Cool Colors (Mockup: "Synthwave")
            ColorCategoryGrid(
                title = "Mid & Synth",
                colorNames = BandColorConstants.midColors,
                colorToSignalMap = colorToSignalMap,
                onColorSelected = { uiColor -> activeUiColor = uiColor }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Category 3: High / Bright Colors
            ColorCategoryGrid(
                title = "High Frequencies",
                colorNames = BandColorConstants.highColors,
                colorToSignalMap = colorToSignalMap,
                onColorSelected = { uiColor -> activeUiColor = uiColor }
            )

            Spacer(modifier = Modifier.height(32.dp)) // Safe bottom padding
        }
    }
}

// --- CUSTOM UI COMPONENTS FOR COLORS DECK ---

@Composable
fun ColorHeroVisualizer(targetColor: Color) {
    // Smoothly animate the color transition when the user taps a new button
    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "hero_color"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(200.dp)
    ) {
        // Outer Glowing Ring
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(width = 12.dp, color = animatedColor, shape = CircleShape)
                .padding(24.dp)
                .border(width = 4.dp, color = Color(0x40FFFFFF), shape = CircleShape) // Inner subtle ring
                .shadow(elevation = 24.dp, shape = CircleShape, spotColor = animatedColor)
        )
    }
}

@Composable
fun ColorCategoryGrid(
    title: String,
    colorNames: List<String>,
    colorToSignalMap: Map<String, String>,
    onColorSelected: (Color) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Chunk the list into rows of 4 buttons to match the mockup grid
        val chunkedColors = colorNames.chunked(4)

        chunkedColors.forEach { rowColors ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.Start // Aligns items to the left, space between is handled by padding
            ) {
                rowColors.forEach { colorName ->
                    val uiColor = BandColorConstants.getColorByName(colorName)

                    Box(modifier = Modifier.padding(end = 16.dp)) {
                        ColorSwatchButton(
                            uiColor = uiColor,
                            onClick = {
                                // 1. Update the UI Visualizer Ring
                                onColorSelected(uiColor)

                                // 2. Fire the hardware IR signal
                                val signalString = colorToSignalMap[colorName]
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
            .border(2.dp, Color(0x40FFFFFF), CircleShape) // Slight inner highlight
            .clickable { onClick() }
            .shadow(elevation = 8.dp, shape = CircleShape, spotColor = uiColor)
    )
}