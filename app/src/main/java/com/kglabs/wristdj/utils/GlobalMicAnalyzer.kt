package com.kglabs.wristdj.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.*
import timber.log.Timber
import kotlin.math.sqrt

object GlobalMicAnalyzer {
    private var audioRecord: AudioRecord? = null
    var isListening = mutableStateOf(false)
    private var scope: CoroutineScope? = null
    private var lastBeatTime = 0L

    // --- ENGINE UPGRADES ---
    private var smoothedRms = 0.0
    private const val SMOOTHING_FACTOR = 0.35

    fun startListening(
        context: Context,
        getSensitivity: () -> Float,
        onBeatDetected: (EnergyLevel, ToneType) -> Unit
    ) {
        if (isListening.value) return
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) return

        GlobalAudioPlayer.pause()

        val sampleRate = 44100
        val bufferSize = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)

        audioRecord = AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize)

        try {
            audioRecord?.startRecording()
            isListening.value = true
            scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

            scope?.launch {
                val buffer = ShortArray(bufferSize)
                while (isActive && isListening.value) {
                    val readResult = audioRecord?.read(buffer, 0, bufferSize) ?: 0
                    if (readResult > 0) {
                        var sum = 0.0
                        for (i in 0 until readResult) { sum += buffer[i] * buffer[i] }

                        val rawRms = sqrt(sum / readResult)
                        smoothedRms = (smoothedRms * (1.0 - SMOOTHING_FACTOR)) + (rawRms * SMOOTHING_FACTOR)

                        val sensitivity = getSensitivity()
                        val baseThreshold = 3000.0
                        val currentThreshold = baseThreshold * (1.1f - sensitivity)
                        val currentTime = System.currentTimeMillis()

                        if (smoothedRms > currentThreshold && (currentTime - lastBeatTime) > 250) {
                            lastBeatTime = currentTime

                            val level = when {
                                smoothedRms > currentThreshold * 2.5 -> EnergyLevel.PEAK
                                smoothedRms > currentThreshold * 1.8 -> EnergyLevel.INTENSE
                                smoothedRms > currentThreshold * 1.3 -> EnergyLevel.SMOOTH
                                else -> EnergyLevel.SUBTLE
                            }

                            val tone = when {
                                rawRms > currentThreshold * 2.0 -> ToneType.BASS
                                rawRms > currentThreshold * 1.4 -> ToneType.MID
                                else -> ToneType.HIGH
                            }

                            withContext(Dispatchers.Main) {
                                onBeatDetected(level, tone)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) { Timber.e(e, "Failed to start recording"); stopListening() }
    }

    fun stopListening() {
        isListening.value = false; scope?.cancel(); scope = null
        try { audioRecord?.stop(); audioRecord?.release() } catch (e: Exception) { Timber.e(e, "Failed to stop/release AudioRecord") } finally { audioRecord = null }
    }
}