package com.kglabs.wristdj.compose.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kglabs.wristdj.compose.components.ColorHeroVisualizer
import com.kglabs.wristdj.compose.components.ColorSwatchButton
import com.kglabs.wristdj.compose.components.StudioBackground
import com.kglabs.wristdj.utils.BandColorConstants
import com.kglabs.wristdj.utils.IRUtils
import com.kglabs.wristdj.utils.GlobalMicAnalyzer
import com.kglabs.wristdj.utils.GlobalAudioPlayer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay

@Composable
fun ManualColorsDeck() {
    val colorToSignalMap = remember { BandColorConstants.buttons.toMap() }

    // --- STATE MANAGEMENT ---
    var activeUiColor by remember { mutableStateOf(Color(0xFF00E5FF)) } // Defaults to Cyan
    var activeSignal by remember { mutableStateOf(colorToSignalMap["Cyan"]) }

    // Toggle for continuous transmission
    var isTransmittingContinuously by IRUtils.isManualTransmitting

    val scrollState = rememberScrollState()

    // --- CONTINUOUS FIRING LOOP ---
    LaunchedEffect(isTransmittingContinuously, activeSignal) {
        if (isTransmittingContinuously && activeSignal != null) {
            // Stop other sources
            GlobalMicAnalyzer.stopListening()
            GlobalAudioPlayer.pause()

            while (true) {
                IRUtils.transmitSignal(activeSignal!!)
                delay(300)
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            isTransmittingContinuously = false
        }
    }

    StudioBackground(
        showTopGlow = isTransmittingContinuously,
        glowColor = activeUiColor
    ) {
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
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp, bottom = 32.dp)
            )

            ColorHeroVisualizer(
                targetColor = activeUiColor,
                isTransmitting = isTransmittingContinuously,
                onClick = {
                    isTransmittingContinuously = !isTransmittingContinuously
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(scrollState)
            ) {
                ColorCategoryGrid(
                    title = "Bass & Warmth",
                    colorNames = BandColorConstants.bassColors,
                    activeUiColor = activeUiColor,
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
                    activeUiColor = activeUiColor,
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
                    activeUiColor = activeUiColor,
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
}

@Composable
fun ColorCategoryGrid(
    title: String,
    colorNames: List<String>,
    activeUiColor: Color,
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
                            isSelected = uiColor == activeUiColor,
                            onClick = {
                                val signalString = colorToSignalMap[colorName]
                                onColorSelected(uiColor, signalString)
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
