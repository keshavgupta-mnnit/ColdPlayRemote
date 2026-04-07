package com.kglabs.wristdj.components

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.audiofx.Visualizer
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.kglabs.wristdj.RemoteSignalConstants
import com.kglabs.wristdj.RemoteUtils
import com.kglabs.wristdj.navigation.MainNavGraph
import com.ramcosta.composedestinations.annotation.Destination
import kotlin.math.hypot

@MainNavGraph
@Destination
@Composable
fun InternalMusicPlayerScreen() {
    val context = LocalContext.current

    // --- STATE MANAGEMENT ---
    var isPlaying by remember { mutableStateOf(false) }
    var audioUri by remember { mutableStateOf<Uri?>(null) } // Holds the picked file
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    var hasPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)
    }

    // 1. Microphone Permission Launcher (Still needed for the Visualizer!)
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        hasPermission = isGranted
    }

    // 2. Native File Picker Launcher (Filters for Audio files)
    val filePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        audioUri = uri
        isPlaying = false // Stop playing if a new file is picked
    }

    // List of your working colors (using the new buttons list)
    val allColors = remember {
        RemoteSignalConstants.ColdPlayBand.buttons.map { it.second }
    }

    // --- MEDIA PLAYER & VISUALIZER SETUP ---
    // This effect runs every time a new audioUri is selected
    DisposableEffect(audioUri) {
        if (audioUri == null) return@DisposableEffect onDispose {}

        // Load the file the user picked
        val mp = MediaPlayer().apply {
            setDataSource(context, audioUri!!)
            prepare()
            setOnCompletionListener {
                isPlaying = false // Reset UI when song ends
            }
        }

        mediaPlayer = mp
        var visualizer: Visualizer? = null
        var lastFireTime = 0L

        if (hasPermission) {
            visualizer = Visualizer(mp.audioSessionId).apply {
                captureSize = Visualizer.getCaptureSizeRange()[1]

                setDataCaptureListener(object : Visualizer.OnDataCaptureListener {
                    override fun onWaveFormDataCapture(visualizer: Visualizer?, waveform: ByteArray?, samplingRate: Int) {}

                    override fun onFftDataCapture(visualizer: Visualizer?, fft: ByteArray?, samplingRate: Int) {
                        if (fft == null || !isPlaying) return

                        // --- THE BASS DETECTION MATH ---
                        var bassMagnitude = 0.0
                        for (i in 2..8 step 2) {
                            val real = fft[i].toDouble()
                            val imaginary = fft[i + 1].toDouble()
                            bassMagnitude += hypot(real, imaginary)
                        }

                        val currentTime = System.currentTimeMillis()

                        // TUNE THIS: Lower number = more sensitive
                        val bassThreshold = 100.0

                        if (bassMagnitude > bassThreshold && (currentTime - lastFireTime) > 300) {
                            val randomColor = allColors.random()
                            RemoteUtils.transmitSignal(randomColor)
                            lastFireTime = currentTime
                        }
                    }
                }, Visualizer.getMaxCaptureRate() / 2, false, true)

                enabled = true
            }
        }

        onDispose {
            visualizer?.release()
            mp.release()
            mediaPlayer = null
        }
    }

    // Handle Play/Pause State changes safely
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            mediaPlayer?.start()
        } else {
            mediaPlayer?.pause()
        }
    }

    // --- THE UI ---
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (!hasPermission) {
            Button(onClick = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) }) {
                Text("Grant Audio Permission for Visualizer")
            }
        } else {
            Text(
                text = "PixMob DJ Studio",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // The File Picker Button
            Button(
                onClick = { filePickerLauncher.launch("audio/*") }, // Filters for .mp3, .wav, etc.
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                Text(if (audioUri == null) "Select Audio File" else "Change Song")
            }

            // The Play/Pause Button (Only shows if a file is loaded)
            if (audioUri != null) {
                Button(
                    onClick = { isPlaying = !isPlaying },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPlaying) Color.Red else Color.Green
                    ),
                    modifier = Modifier.size(150.dp)
                ) {
                    Text(if (isPlaying) "STOP" else "PLAY")
                }
            }
        }
    }
}
