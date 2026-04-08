package com.kglabs.wristdj.compose.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.kglabs.wristdj.compose.components.GlowingMediaButton
import com.kglabs.wristdj.compose.components.VinylRecordVisualizer
import com.kglabs.wristdj.utils.AudioPlayer
import com.kglabs.wristdj.utils.BandColorConstants
import com.kglabs.wristdj.utils.IRUtils
import com.kglabs.wristdj.utils.ToneType

@Composable
fun PlayerDeck() { // Zero parameters needed!
    val context = LocalContext.current

    // 1. Initialize the AudioPlayer locally
    val audioPlayer = remember { AudioPlayer(context) }

    // 2. UI State
    var trackName by remember { mutableStateOf("No Track Selected") }
    var isPlaying by remember { mutableStateOf(false) }

    // Create the map once to look up IR strings quickly
    val colorToSignalMap = remember { BandColorConstants.buttons.toMap() }

    // 3. Permission Handling (Crucial for the Visualizer API to work on internal audio)
    var hasPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        hasPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasPermission) permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    // Clean up when leaving the tab
    DisposableEffect(Unit) {
        onDispose { audioPlayer.release() }
    }

    // 4. File Picker Launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            trackName = "Local Audio Track" // Or query ContentResolver for filename
            isPlaying = true

            // Start playing and pass the beat detection logic
            audioPlayer.loadAndPlay(selectedUri) { toneType ->
                // Map the frequency to your color lists
                val selectedColorName = when (toneType) {
                    ToneType.BASS -> BandColorConstants.bassColors.random()
                    ToneType.MID -> BandColorConstants.midColors.random()
                    ToneType.HIGH -> BandColorConstants.highColors.random()
                }

                // Look up the raw IR string
                val signalString = colorToSignalMap[selectedColorName]

                // Fire the signal using your new Singleton
                if (signalString != null) {
                    IRUtils.transmitSignal(signalString) // Frequency defaults to 38000 in your object
                }
            }
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
            text = "Wrist DJ - Player",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 16.dp, bottom = 32.dp)
        )

        // 1. The Vinyl Visualizer
        VinylRecordVisualizer(isPlaying = isPlaying, trackName = trackName)

        Spacer(modifier = Modifier.height(32.dp))

        // 2. Track Metadata
        Text(
            text = trackName,
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
        Text(
            text = if (isPlaying) "Syncing..." else "Ready",
            color = Color.Gray,
            fontSize = 16.sp,
            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
        )

        if (!hasPermission) {
            Text("Mic Permission Required for Beat Sync", color = Color.Red, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(8.dp))
        }

        // 3. Select Track Button
        OutlinedButton(
            onClick = {
                if (hasPermission) filePickerLauncher.launch("audio/*")
                else permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            },
            border = BorderStroke(1.dp, Color.DarkGray),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Text("Select Track", color = Color.White, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.weight(1f)) // Push controls to the bottom

        // 4. Progress Bar (Mockup representation)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("0:00", color = Color.Gray, fontSize = 12.sp)
            Slider(
                value = 0f, // You can link this to the MediaPlayer progress later
                onValueChange = {},
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = Color.White,
                    inactiveTrackColor = Color.DarkGray
                ),
                modifier = Modifier.weight(1f).padding(horizontal = 16.dp)
            )
            Text("-:--", color = Color.Gray, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 5. Glowing Media Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            GlowingMediaButton(
                icon = Icons.Default.PlayArrow,
                glowColor = Color(0xFF00FF00), // Green play
                onClick = { audioPlayer.play(); isPlaying = true }
            )

            GlowingMediaButton(
                icon = Icons.Default.Pause,
                glowColor = Color(0xFFFFA500), // Orange pause
                onClick = { audioPlayer.pause(); isPlaying = false }
            )

            GlowingMediaButton(
                icon = Icons.Default.Stop,
                glowColor = Color(0xFFFF0000), // Red stop
                onClick = { audioPlayer.stop(); isPlaying = false; trackName = "No Track Selected" }
            )

            GlowingMediaButton(
                icon = Icons.Default.SkipNext,
                glowColor = Color(0xFF00E5FF), // Cyan next
                onClick = { /* TODO: Next track logic */ }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}