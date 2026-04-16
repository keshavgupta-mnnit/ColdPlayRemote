package com.kglabs.wristdj.compose.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.kglabs.wristdj.R
import com.kglabs.wristdj.compose.components.HeaderWithIcon
import com.kglabs.wristdj.compose.components.LiveMicVisualizer
import com.kglabs.wristdj.compose.components.StudioBackground
import com.kglabs.wristdj.utils.BandColorConstants
import com.kglabs.wristdj.utils.IRUtils
import com.kglabs.wristdj.utils.GlobalMicAnalyzer
import com.kglabs.wristdj.utils.ToneType
import com.kglabs.wristdj.utils.UltimateLightingEngine

@Composable
fun LiveMicDeck() {
    val context = LocalContext.current
    val isListening by GlobalMicAnalyzer.isListening
    var sensitivity by remember { mutableStateOf(0.7f) }
    var currentThreshold by remember { mutableStateOf(2500) }

    val colorToSignalMap = remember { BandColorConstants.buttons.toMap() }

    val infiniteTransition = rememberInfiniteTransition(label = "illumination")
    val illuminationAlpha by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    var hasPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        hasPermission = isGranted
        if (!isGranted) {
            GlobalMicAnalyzer.stopListening()
        }
    }

    LaunchedEffect(Unit) {
        if (!hasPermission) permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    // Use a single LaunchedEffect to manage the mic based on isListening state
    LaunchedEffect(isListening) {
        if (isListening) {
            UltimateLightingEngine.reset()
            GlobalMicAnalyzer.startListening(
                context = context,
                getSensitivity = { sensitivity }
            ) { level, tone ->
                val colorName = UltimateLightingEngine.onAudioEvent(level, tone)
                colorName?.let {
                    colorToSignalMap[it]?.let { signal -> IRUtils.transmitSignal(signal) }
                }
            }
        } else {
            GlobalMicAnalyzer.stopListening()
        }
    }

    StudioBackground(
        showTopGlow = isListening,
        isRainbowGlow = true
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HeaderWithIcon(
                title = "Live Mic",
                iconRes = R.drawable.ic_mic_neon
            )

            LiveMicVisualizer(isListening = isListening, illuminationAlpha = illuminationAlpha)

            Spacer(modifier = Modifier.height(32.dp))

            if (!hasPermission) {
                Text("Mic Permission Required", color = Color.Red, fontSize = 16.sp)
            } else {
                Text(
                    text = if (isListening) "Listening to Music..." else "Waiting to Listen...",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "(Threshold: $currentThreshold)",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
                )
            }

            RedGreenToggleSwitch(
                isOn = isListening,
                onToggle = {
                    if (!hasPermission) {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        return@RedGreenToggleSwitch
                    }
                    if (isListening) {
                        GlobalMicAnalyzer.stopListening()
                    } else {
                        IRUtils.stopManualTransmission()
                        UltimateLightingEngine.reset()
                        GlobalMicAnalyzer.startListening(
                            context = context,
                            getSensitivity = { sensitivity }
                        ) { level, tone ->
                            val colorName = UltimateLightingEngine.onAudioEvent(level, tone)
                            colorName?.let {
                                colorToSignalMap[it]?.let { signal -> IRUtils.transmitSignal(signal) }
                            }
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text("Start Microphone to Listen", color = Color.White, fontSize = 14.sp)
            Spacer(modifier = Modifier.weight(1f))

            Text("Sensitivity", color = Color.White, fontSize = 14.sp)
            Slider(
                value = sensitivity,
                onValueChange = {
                    sensitivity = it
                    currentThreshold = (3000 * (1.1f - it)).toInt()
                },
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFFFF007F),
                    activeTrackColor = Color(0xFFFFA500),
                    inactiveTrackColor = Color.DarkGray
                ),
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Low", color = Color.Gray, fontSize = 12.sp)
                Text("High", color = Color.Gray, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun RedGreenToggleSwitch(isOn: Boolean, onToggle: () -> Unit) {
    val borderColor by animateColorAsState(targetValue = if (isOn) Color(0xFF00FF00) else Color(0xFFFF0000), label = "border")
    val textColor by animateColorAsState(targetValue = if (isOn) Color(0xFF00FF00) else Color(0xFFFF0000), label = "text")
    val thumbGlowColor by animateColorAsState(targetValue = if (isOn) Color(0xFF00FF00) else Color(0xFFFF0000), label = "thumbGlow")

    Box(
        modifier = Modifier
            .width(130.dp)
            .height(56.dp)
            .drawBehind {
                if (isOn) {
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            listOf(borderColor.copy(alpha = 0.15f), Color.Transparent)
                        ),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(28.dp.toPx())
                    )
                }
            }
            .clip(RoundedCornerShape(50))
            .background(Color(0xFF080808))
            .border(1.5.dp, borderColor.copy(alpha = 0.5f), RoundedCornerShape(50))
            .clickable { onToggle() }
            .padding(6.dp),
        contentAlignment = Alignment.Center
    ) {
        // Track Background Glow
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp)
                .drawBehind {
                    val glowRadius = 40.dp.toPx()
                    val centerOff = if (isOn) size.width - 24.dp.toPx() else 24.dp.toPx()
                    drawCircle(
                        brush = Brush.radialGradient(
                            0.0f to thumbGlowColor.copy(alpha = 0.4f),
                            1.0f to Color.Transparent,
                            center = androidx.compose.ui.geometry.Offset(centerOff, size.height / 2),
                            radius = glowRadius
                        ),
                        radius = glowRadius,
                        center = androidx.compose.ui.geometry.Offset(centerOff, size.height / 2)
                    )
                }
        )

        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (isOn) Arrangement.End else Arrangement.Start
        ) {
            if (isOn) {
                Text(
                    "ON", 
                    color = textColor, 
                    fontSize = 16.sp, 
                    fontWeight = FontWeight.Black, 
                    modifier = Modifier.padding(end = 16.dp).weight(1f),
                    textAlign = TextAlign.Center
                )
                // Glowy Thumb
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .drawBehind {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    0.5f to thumbGlowColor.copy(alpha = 0.9f),
                                    1.0f to Color.Transparent,
                                    center = center,
                                    radius = 21.dp.toPx()
                                ),
                                radius = 21.dp.toPx()
                            )
                        }
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                )
            } else {
                // Glowy Thumb
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .drawBehind {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    0.5f to thumbGlowColor.copy(alpha = 0.9f),
                                    1.0f to Color.Transparent,
                                    center = center,
                                    radius = 21.dp.toPx()
                                ),
                                radius = 21.dp.toPx()
                            )
                        }
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                )
                Text(
                    "OFF", 
                    color = textColor, 
                    fontSize = 16.sp, 
                    fontWeight = FontWeight.Black, 
                    modifier = Modifier.padding(start = 16.dp).weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
