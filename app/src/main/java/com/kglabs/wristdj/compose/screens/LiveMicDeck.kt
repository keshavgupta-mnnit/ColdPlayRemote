package com.kglabs.wristdj.compose.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.kglabs.wristdj.compose.components.CustomGlowingToggle
import com.kglabs.wristdj.compose.components.RainbowWaveformVisualizer
import com.kglabs.wristdj.utils.BandColorConstants
import com.kglabs.wristdj.utils.IRUtils
import com.kglabs.wristdj.utils.MicAnalyzer
import com.kglabs.wristdj.utils.ToneType

@Composable
fun LiveMicDeck() {
    val context = LocalContext.current
    val micAnalyzer = remember { MicAnalyzer(context) }

    // --- STATE MANAGEMENT ---
    var isSyncing by remember { mutableStateOf(false) }
    var sensitivity by remember { mutableStateOf(0.7f) }
    var currentThreshold by remember { mutableStateOf(2500) }

    // Look up map for IR strings (Remembered so it doesn't rebuild constantly)
    val colorToSignalMap = remember { BandColorConstants.buttons.toMap() }

    // --- PERMISSION HANDLING ---
    var hasPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        hasPermission = isGranted
        if (!isGranted) {
            isSyncing = false
            micAnalyzer.stopListening()
        }
    }

    LaunchedEffect(Unit) {
        if (!hasPermission) permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    DisposableEffect(Unit) {
        onDispose {
            micAnalyzer.stopListening()
            isSyncing = false
        }
    }

    // --- MAIN UI LAYOUT ---
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Wrist DJ - Live Mic",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 16.dp, bottom = 32.dp)
        )

        RainbowWaveformVisualizer(isSyncing = isSyncing)

        Spacer(modifier = Modifier.height(24.dp))

        if (!hasPermission) {
            Text("Mic Permission Required", color = Color.Red, fontSize = 16.sp)
        } else {
            Text(
                text = if (isSyncing) "Syncing to Music..." else "Waiting to Sync...",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "(Threshold: $currentThreshold)",
                color = Color.LightGray,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )
        }

        CustomGlowingToggle(
            isOn = isSyncing,
            onToggle = {
                if (!hasPermission) {
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    return@CustomGlowingToggle
                }

                isSyncing = !isSyncing

                if (isSyncing) {
                    micAnalyzer.startListening(
                        getSensitivity = { sensitivity }
                    ) { toneType ->

                        // 1. Pick a color name based on the sound frequency
                        val selectedColorName = when (toneType) {
                            ToneType.BASS -> BandColorConstants.bassColors.random()
                            ToneType.MID -> BandColorConstants.midColors.random()
                            ToneType.HIGH -> BandColorConstants.highColors.random()
                        }

                        // 2. Fetch the IR String from your mapped list
                        val signalString = colorToSignalMap[selectedColorName]

                        // 3. Blast the code using your Utils
                        if (signalString != null) {
                            IRUtils.transmitSignal(signalString)
                        }
                    }
                } else {
                    micAnalyzer.stopListening()
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))
        Text("Start Microphone Sync", color = Color.White, fontSize = 14.sp)
        Spacer(modifier = Modifier.weight(1f))

        // --- SLIDER ---
        Text("Sensitivity", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
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

// ------------------------------------------------------------------
// (Keep your RainbowWaveformVisualizer, AnimatedWaveformBar,
// and CustomGlowingToggle composables right here at the bottom!)
// ------------------------------------------------------------------