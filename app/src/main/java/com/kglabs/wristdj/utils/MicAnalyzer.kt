package com.kglabs.wristdj.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.*
import kotlin.math.sqrt

class MicAnalyzer(private val context: Context) {
    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private var scope = CoroutineScope(Dispatchers.IO + Job())

    private var lastBeatTime = 0L

    fun startListening(
        getSensitivity: () -> Float,
        onBeatDetected: (ToneType) -> Unit
    ) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        val sampleRate = 44100
        val bufferSize = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        )

        audioRecord?.startRecording()
        isRecording = true

        // Launch a background thread to process the audio buffer continuously
        scope.launch {
            val buffer = ShortArray(bufferSize)

            while (isRecording) {
                val readResult = audioRecord?.read(buffer, 0, bufferSize) ?: 0
                if (readResult > 0) {
                    // 1. Calculate RMS (Root Mean Square) to find the volume energy
                    var sum = 0.0
                    for (i in 0 until readResult) {
                        sum += buffer[i] * buffer[i]
                    }
                    val rms = sqrt(sum / readResult)

                    // 2. Fetch the live sensitivity from the UI slider (0.0 to 1.0)
                    val sensitivity = getSensitivity()

                    // Math: If sensitivity is 1.0 (High), threshold is low (easy to trigger).
                    // If sensitivity is 0.0 (Low), threshold is high (needs loud sounds).
                    val baseThreshold = 3000.0 // Baseline volume
                    val currentThreshold = baseThreshold * (1.1f - sensitivity)

                    val currentTime = System.currentTimeMillis()

                    // 3. Beat Detection & Color Mapping
                    if (rms > currentThreshold && (currentTime - lastBeatTime) > 250) {
                        lastBeatTime = currentTime

                        // Determine the intensity of the sound to pick the color type
                        val tone = when {
                            rms > currentThreshold * 2.0 -> ToneType.BASS // Extremely Loud
                            rms > currentThreshold * 1.4 -> ToneType.MID  // Very Loud
                            else -> ToneType.HIGH                         // Just passed threshold
                        }

                        // Switch back to the Main Thread to update the UI and fire IR
                        withContext(Dispatchers.Main) {
                            onBeatDetected(tone)
                        }
                    }
                }
            }
        }
    }

    fun stopListening() {
        isRecording = false
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
    }
}