package com.kglabs.wristdj.compose.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.PauseCircleFilled
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import com.kglabs.wristdj.utils.AudioPlayer
import com.kglabs.wristdj.utils.ColdPlayBand
import com.kglabs.wristdj.utils.IRUtils
import com.kglabs.wristdj.utils.ToneType

@Composable
fun PlayerDeck() {
    val context = LocalContext.current
    val audioPlayer = remember { AudioPlayer(context) }

    var trackName by remember { mutableStateOf("No Track Selected") }
    var isPlaying by remember { mutableStateOf(false) }

    // --- FREQUENCY-BASED COLOR PALETTES ---
    val bassColors = remember {
        ColdPlayBand.bassColors
    }

    val midColors = remember {
        ColdPlayBand.midColors
    }

    val highColors = remember {
        ColdPlayBand.highColors
    }

    // Map color names to their IR signals once for easy lookup
    val colorToSignalMap = remember {
        ColdPlayBand.buttons.toMap()
    }

    // --- PERMISSION HANDLING ---
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            hasPermission = isGranted
        }

    LaunchedEffect(Unit) {
        if (!hasPermission) permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    DisposableEffect(Unit) { onDispose { audioPlayer.release() } }

    // --- FILE PICKER & BEAT SYNC LOGIC ---
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            trackName = "Local Audio Track"
            isPlaying = true

            // Start playing and listen for the specific ToneType
            audioPlayer.loadAndPlay(selectedUri) { toneType ->
                // Choose the color palette based on the frequency of the sound
                val selectedColorName = when (toneType) {
                    ToneType.BASS -> bassColors.random()
                    ToneType.MID -> midColors.random()
                    ToneType.HIGH -> highColors.random()
                }

                // Get the IR signal for the chosen color
                val signal = colorToSignalMap[selectedColorName]

                // Blast the chosen color to the wristband (Frequency is 38000 by default)
                if (signal != null) {
                    IRUtils.transmitSignal(signal)
                }
            }
        }
    }

    // --- THE UI ---
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!hasPermission) {
            Text(
                "Microphone permission required for beat sync.",
                color = Color.Red,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Button(onClick = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) }) {
                Text("Grant Permission")
            }
        } else {
            OutlinedButton(
                onClick = { filePickerLauncher.launch("audio/*") },
                border = BorderStroke(1.dp, Color.DarkGray),
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                Icon(
                    Icons.Default.FolderOpen,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("SELECT TRACK", color = Color.White, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                trackName,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                maxLines = 1
            )
            Text(
                if (isPlaying) "Playing & Syncing..." else "Ready",
                color = if (isPlaying) Color(0xFF00E5FF) else Color.Gray,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Media Controls Row
            Row(
                modifier = Modifier.fillMaxWidth(0.9f),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { audioPlayer.play(); isPlaying = true },
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        Icons.Default.PlayCircleFilled,
                        contentDescription = "Play",
                        tint = Color(0xFF00C853),
                        modifier = Modifier.fillMaxSize()
                    )
                }
                IconButton(
                    onClick = { audioPlayer.pause(); isPlaying = false },
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        Icons.Default.PauseCircleFilled,
                        contentDescription = "Pause",
                        tint = Color(0xFFFFD600),
                        modifier = Modifier.fillMaxSize()
                    )
                }
                IconButton(onClick = {
                    audioPlayer.stop(); isPlaying = false; trackName = "No Track Selected"
                }, modifier = Modifier.size(56.dp)) {
                    Icon(
                        Icons.Default.StopCircle,
                        contentDescription = "Stop",
                        tint = Color(0xFFD50000),
                        modifier = Modifier.fillMaxSize()
                    )
                }
                IconButton(
                    onClick = { /* Implement Skip logic later */ },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        Icons.Default.SkipNext,
                        contentDescription = "Next",
                        tint = Color.White,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}