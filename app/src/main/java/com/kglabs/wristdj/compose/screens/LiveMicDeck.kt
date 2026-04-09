package com.kglabs.wristdj.compose.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

import com.kglabs.wristdj.utils.BandColorConstants
import com.kglabs.wristdj.utils.IRUtils
import com.kglabs.wristdj.utils.MicAnalyzer
import com.kglabs.wristdj.utils.ToneType

@Composable
fun LiveMicDeck() {
    val context = LocalContext.current
    val micAnalyzer = remember { MicAnalyzer(context) }

    var isListening by remember { mutableStateOf(false) }
    var sensitivity by remember { mutableStateOf(0.7f) }
    var currentThreshold by remember { mutableStateOf(2500) }

    val colorToSignalMap = remember { BandColorConstants.buttons.toMap() }

    var hasPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        hasPermission = isGranted
        if (!isGranted) {
            isListening = false
            micAnalyzer.stopListening()
        }
    }

    LaunchedEffect(Unit) {
        if (!hasPermission) permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    DisposableEffect(Unit) {
        onDispose {
            micAnalyzer.stopListening()
            isListening = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF1A1A24), Color(0xFF050505)),
                    radius = 1500f
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "PixMob DJ Studio - Live Mic",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 16.dp, bottom = 32.dp)
            )

            // Transparent Waveform + Stable Dotted Rings
            FullWidthLiveVisualizer(isListening = isListening)

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
                    isListening = !isListening
                    if (isListening) {
                        micAnalyzer.startListening(
                            getSensitivity = { sensitivity }
                        ) { toneType ->
                            val selectedColorName = when (toneType) {
                                ToneType.BASS -> BandColorConstants.bassColors.random()
                                ToneType.MID -> BandColorConstants.midColors.random()
                                ToneType.HIGH -> BandColorConstants.highColors.random()
                            }
                            colorToSignalMap[selectedColorName]?.let { IRUtils.transmitSignal(it) }
                        }
                    } else {
                        micAnalyzer.stopListening()
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
                    thumbColor = Color.White,
                    activeTrackColor = Color.White,
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

// --- REBUILT COMPONENTS ---

@Composable
fun FullWidthLiveVisualizer(isListening: Boolean) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
    ) {
        BackgroundWaveform(isListening = isListening)
        StableRingsVisualizer(isListening = isListening)
    }
}

@Composable
fun BackgroundWaveform(isListening: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(3500, easing = LinearEasing)),
        label = "phase"
    )

    val rainbowColors = listOf(
        Color(0xFFFF007F), Color(0xFF7F00FF), Color(0xFF00E5FF),
        Color(0xFF00FF00), Color(0xFFFFFF00), Color(0xFFFF0000), Color(0xFFFF007F)
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
fun StableRingsVisualizer(isListening: Boolean) {
    val rainbowColors = listOf(
        Color(0xFFFF007F), Color(0xFF7F00FF), Color(0xFF00E5FF),
        Color(0xFF00FF00), Color(0xFFFFFF00), Color(0xFFFF0000), Color(0xFFFF007F)
    )

    Canvas(modifier = Modifier.size(240.dp)) {
        val center = Offset(size.width / 2, size.height / 2)
        val maxRadius = size.width / 2

        // --- 4 RINGS & ULTRA-TIGHT SPACING ---
        val outerRadius = maxRadius - 12.dp.toPx()
        val baseInnerRadius = outerRadius - 12.dp.toPx()
        val ringGap = 3.dp.toPx() // Razor thin gap

        val step1Radius = baseInnerRadius - ringGap
        val step2Radius = step1Radius - ringGap
        val step3Radius = step2Radius - ringGap

        val ringBrush = Brush.sweepGradient(rainbowColors, center)
        val dotStrokeWidth = 2.dp.toPx()

        // FIXED: Stable static dashes, completely removing the "marching" animation phase
        val stableDottedEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 8f), 0f)

        // Dark mask behind the rings so they pop out against the waveform
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF000000).copy(alpha = 0.9f), Color.Transparent),
                center = center,
                radius = maxRadius
            )
        )

        // 1. The Main Static Rainbow Ring
        drawCircle(
            brush = ringBrush,
            radius = outerRadius,
            style = Stroke(width = 12.dp.toPx())
        )

        // The Inner Dotted Rings (Completely Stable)
        if (isListening) {
            // Base Inner Ring (100% Brightness)
            drawCircle(brush = ringBrush, radius = baseInnerRadius, alpha = 1.0f, style = Stroke(width = dotStrokeWidth, cap = StrokeCap.Round, pathEffect = stableDottedEffect))

            // Step 1 Ring (75% Brightness)
            drawCircle(brush = ringBrush, radius = step1Radius, alpha = 0.75f, style = Stroke(width = dotStrokeWidth, cap = StrokeCap.Round, pathEffect = stableDottedEffect))

            // Step 2 Ring (50% Brightness)
            drawCircle(brush = ringBrush, radius = step2Radius, alpha = 0.50f, style = Stroke(width = dotStrokeWidth, cap = StrokeCap.Round, pathEffect = stableDottedEffect))

            // Step 3 Ring (25% Brightness)
            drawCircle(brush = ringBrush, radius = step3Radius, alpha = 0.25f, style = Stroke(width = dotStrokeWidth, cap = StrokeCap.Round, pathEffect = stableDottedEffect))

        } else {
            // When OFF, only the base inner ring shows as static dark grey
            drawCircle(color = Color.DarkGray, radius = baseInnerRadius, style = Stroke(width = dotStrokeWidth, cap = StrokeCap.Round, pathEffect = stableDottedEffect))
        }
    }
}

@Composable
fun RedGreenToggleSwitch(isOn: Boolean, onToggle: () -> Unit) {
    val borderColor by animateColorAsState(targetValue = if (isOn) Color(0xFF00FF00) else Color(0xFFFF0000), label = "border")
    val textColor by animateColorAsState(targetValue = if (isOn) Color(0xFF00FF00) else Color(0xFFFF0000), label = "text")
    val thumbGlow by animateColorAsState(targetValue = if (isOn) Color(0xFF00FF00) else Color(0xFFFF0000), label = "thumbGlow")

    Box(
        modifier = Modifier
            .width(130.dp)
            .height(56.dp)
            .clip(RoundedCornerShape(50))
            .background(Color(0xFF111111)) // Hollow center
            .border(2.dp, borderColor, RoundedCornerShape(50))
            .clickable { onToggle() }
            .shadow(if (isOn) 12.dp else 0.dp, RoundedCornerShape(50), spotColor = Color(0xFF00FF00))
            .padding(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (isOn) {
                Text("ON", color = textColor, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp))
                Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.White).shadow(8.dp, CircleShape, spotColor = thumbGlow))
            } else {
                Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.White).shadow(8.dp, CircleShape, spotColor = thumbGlow))
                Text("OFF", color = textColor, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 12.dp))
            }
        }
    }
}