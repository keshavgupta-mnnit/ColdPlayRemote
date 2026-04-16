package com.kglabs.wristdj.compose.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kglabs.wristdj.R
import com.kglabs.wristdj.compose.components.HeaderWithIcon
import com.kglabs.wristdj.compose.components.ColorHeroVisualizer
import com.kglabs.wristdj.compose.components.ColorSwatchButton
import com.kglabs.wristdj.compose.components.StudioBackground
import com.kglabs.wristdj.utils.BandColorConstants
import com.kglabs.wristdj.utils.GlobalAudioPlayer
import com.kglabs.wristdj.utils.GlobalMicAnalyzer
import com.kglabs.wristdj.utils.IRUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun ManualColorsDeck() {
    val colorToSignalMap = remember { BandColorConstants.buttons.toMap() }

    // --- STATE ---
    var activeUiColor by remember { mutableStateOf(Color(0xFF00E5FF)) }
    var activeSignal by remember { mutableStateOf(colorToSignalMap["Cyan"]) }

    var isTransmittingContinuously by IRUtils.isManualTransmitting

    val scrollState = rememberScrollState()

    // --- CONTINUOUS TRANSMISSION (FIXED LOOP) ---
    LaunchedEffect(isTransmittingContinuously, activeSignal) {
        while (isActive && isTransmittingContinuously && activeSignal != null) {

            // stop other systems
            GlobalMicAnalyzer.stopListening()
            GlobalAudioPlayer.pause()

            IRUtils.transmitSignal(activeSignal!!)
            delay(300)
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
                .padding(horizontal = 20.dp), // tighter padding
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // HEADER
            HeaderWithIcon(
                title = "Colors",
                iconRes = R.drawable.ic_colors_neon
            )

            // HERO VISUAL
            ColorHeroVisualizer(
                targetColor = activeUiColor,
                isTransmitting = isTransmittingContinuously,
                onClick = {
                    isTransmittingContinuously = !isTransmittingContinuously
                }
            )

            Spacer(modifier = Modifier.height(28.dp))

            // SCROLL CONTENT
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
                    colorToSignalMap = colorToSignalMap
                ) { uiColor, signal ->
                    activeUiColor = uiColor
                    activeSignal = signal
                }

                Spacer(modifier = Modifier.height(20.dp))

                ColorCategoryGrid(
                    title = "Mid & Synth",
                    colorNames = BandColorConstants.midColors,
                    activeUiColor = activeUiColor,
                    colorToSignalMap = colorToSignalMap
                ) { uiColor, signal ->
                    activeUiColor = uiColor
                    activeSignal = signal
                }

                Spacer(modifier = Modifier.height(20.dp))

                ColorCategoryGrid(
                    title = "High Frequencies",
                    colorNames = BandColorConstants.highColors,
                    activeUiColor = activeUiColor,
                    colorToSignalMap = colorToSignalMap
                ) { uiColor, signal ->
                    activeUiColor = uiColor
                    activeSignal = signal
                }

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
