package com.kglabs.wristdj.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import timber.log.Timber
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.*
import kotlin.math.sqrt

class MicAnalyzer(private val context: Context) {
    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private var scope: CoroutineScope? = null

    private var lastBeatTime = 0L

    fun startListening(
        getSensitivity: () -> Float,
        onBeatDetected: (ToneType) -> Unit
    ) {
        if (isRecording) return
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
        scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

        scope?.launch {
            val buffer = ShortArray(bufferSize)

            while (isActive && isRecording) {
                val readResult = audioRecord?.read(buffer, 0, bufferSize) ?: 0
                if (readResult > 0) {
                    var sum = 0.0
                    for (i in 0 until readResult) {
                        sum += buffer[i] * buffer[i]
                    }
                    val rms = sqrt(sum / readResult)

                    val sensitivity = getSensitivity()
                    val baseThreshold = 3000.0 
                    val currentThreshold = baseThreshold * (1.1f - sensitivity)

                    val currentTime = System.currentTimeMillis()

                    if (rms > currentThreshold && (currentTime - lastBeatTime) > 250) {
                        lastBeatTime = currentTime

                        val tone = when {
                            rms > currentThreshold * 2.0 -> ToneType.BASS 
                            rms > currentThreshold * 1.4 -> ToneType.MID  
                            else -> ToneType.HIGH                         
                        }

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
        scope?.cancel()
        scope = null
        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (e: Exception) {
            Timber.e(e, "Failed to stop/release AudioRecord")
        } finally {
            audioRecord = null
        }
    }
}