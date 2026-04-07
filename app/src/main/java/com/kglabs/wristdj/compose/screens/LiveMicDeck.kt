package com.kglabs.wristdj.compose.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.kglabs.wristdj.utils.ColdPlayBand
import com.kglabs.wristdj.utils.MicAnalyzer
import com.kglabs.wristdj.utils.RemoteUtils
import com.kglabs.wristdj.utils.ToneType

@Composable
fun LiveMicDeck() {
    val context = LocalContext.current
    val micAnalyzer = remember { MicAnalyzer(context) }

    // --- STATE MANAGEMENT ---
    var isSyncing by remember { mutableStateOf(false) }
    var sensitivity by remember { mutableStateOf(0.7f) } // Default slightly high

    // --- FREQUENCY-BASED COLOR PALETTES ---
    // (Replace these with your actual IntArrays from your 33 colors)
    val bassColors = remember {
        ColdPlayBand.bassColors
    }

    val midColors = remember {
        ColdPlayBand.midColors
    }

    val highColors = remember {
        ColdPlayBand.highColors
    }

    val colorToSignalMap = remember {
        ColdPlayBand.buttons.toMap()
    }

    // --- PERMISSION HANDLING ---
    var hasPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        hasPermission = isGranted
        if (!isGranted) isSyncing = false
    }

    // Request permission automatically if needed
    LaunchedEffect(Unit) {
        if (!hasPermission) permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    // Safely shut off the microphone if the user navigates away from the Mic tab
    DisposableEffect(Unit) {
        onDispose {
            micAnalyzer.stopListening()
            isSyncing = false
        }
    }

    // --- THE UI ---
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!hasPermission) {
            Text("Microphone permission required for Live Sync.", color = Color.Red, modifier = Modifier.padding(bottom = 16.dp))
            Button(onClick = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) }) {
                Text("Grant Permission")
            }
        } else {
            // Massive Chunky Toggle Button
            Button(
                onClick = {
                    isSyncing = !isSyncing
                    if (isSyncing) {
                        // Pass a lambda for sensitivity so the analyzer gets live updates when slider moves
                        micAnalyzer.startListening(
                            getSensitivity = { sensitivity }
                        ) { toneType ->
                            // Pick color based on how loud the mic input was
                            val selectedArray = when (toneType) {
                                ToneType.BASS -> bassColors.random()
                                ToneType.MID -> midColors.random()
                                ToneType.HIGH -> highColors.random()
                            }
                            // Get the IR signal for the chosen color
                            val signal = colorToSignalMap[selectedArray]
                            signal?.let { RemoteUtils.transmitSignal(it)}
                        }
                    } else {
                        micAnalyzer.stopListening()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSyncing) Color(0xFF00C853) else Color.DarkGray
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(120.dp)
            ) {
                Text(
                    text = if (isSyncing) "SYNCING LIVE..." else "START SYNC",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(64.dp))

            // Sensitivity Control
            Text("MIC SENSITIVITY", color = Color.LightGray, fontSize = 14.sp, fontWeight = FontWeight.Bold)

            Slider(
                value = sensitivity,
                onValueChange = { sensitivity = it },
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = if (isSyncing) Color(0xFF00C853) else Color(0xFF00E5FF),
                    inactiveTrackColor = Color.DarkGray
                ),
                modifier = Modifier.fillMaxWidth(0.85f).padding(top = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(0.85f),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Low", color = Color.Gray, fontSize = 12.sp)
                Text("High", color = Color.Gray, fontSize = 12.sp)
            }
        }
    }
}