package com.kglabs.wristdj.components

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sqrt

@MainNavGraph
@Destination
@Composable
fun MusicSyncScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // State to toggle listening on/off
    var isListening by remember { mutableStateOf(false) }
    var hasMicPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)
    }

    // Permission Requester
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        hasMicPermission = isGranted
    }

    // List of your working colors to randomly choose from
    val allColors = remember {
        RemoteSignalConstants.ColdPlayBand.buttons.map { it.second }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (!hasMicPermission) {
            Button(onClick = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) }) {
                Text("Grant Microphone Permission")
            }
        } else {
            // The massive Sync Button
            Button(
                onClick = { isListening = !isListening },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isListening) Color.Red else Color.Green
                ),
                modifier = Modifier.size(200.dp)
            ) {
                Text(if (isListening) "STOP SYNC" else "START MUSIC SYNC")
            }

            // The Audio Processing Loop
            LaunchedEffect(isListening) {
                if (isListening) {
                    launch(Dispatchers.IO) { // Run on background thread
                        val sampleRate = 44100
                        val channelConfig = AudioFormat.CHANNEL_IN_MONO
                        val audioFormat = AudioFormat.ENCODING_PCM_16BIT
                        val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

                        try {
                            val audioRecord = AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, channelConfig, audioFormat, bufferSize)
                            audioRecord.startRecording()

                            val buffer = ShortArray(bufferSize)
                            var lastFireTime = 0L

                            // Adjust this number! Lower = more sensitive to quiet music. Higher = only heavy bass triggers it.
                            val volumeThreshold = 2000.0

                            while (isListening) {
                                val readSize = audioRecord.read(buffer, 0, bufferSize)
                                if (readSize > 0) {
                                    // Calculate RMS (Root Mean Square) to find the volume level
                                    var sum = 0.0
                                    for (i in 0 until readSize) {
                                        sum += buffer[i] * buffer[i]
                                    }
                                    val rms = sqrt(sum / readSize)

                                    val currentTime = System.currentTimeMillis()

                                    // If there is a beat AND the blaster has rested for 300ms
                                    if (rms > volumeThreshold && (currentTime - lastFireTime) > 300) {

                                        // Pick a random color from your list
                                        val randomColor = allColors.random()

                                        // Fire the code
                                        RemoteUtils.transmitSignal(randomColor)

                                        lastFireTime = currentTime
                                    }
                                }
                            }
                            audioRecord.stop()
                            audioRecord.release()
                        } catch (e: SecurityException) {
                            // Handle missing permission just in case
                        }
                    }
                }
            }
        }
    }
}
