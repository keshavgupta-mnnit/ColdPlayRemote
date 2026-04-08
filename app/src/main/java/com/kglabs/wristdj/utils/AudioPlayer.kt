package com.kglabs.wristdj.utils

import android.content.Context
import android.media.MediaPlayer
import android.media.audiofx.Visualizer
import android.net.Uri
import kotlin.math.hypot

// Tells the UI what kind of frequency triggered the beat
enum class ToneType { BASS, MID, HIGH }

class AudioPlayer(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    private var visualizer: Visualizer? = null

    var isPlaying: Boolean = false
        private set

    private var lastBeatTime = 0L

    // Updated to pass the ToneType back to the UI
    fun loadAndPlay(uri: Uri, onBeatDetected: (ToneType) -> Unit) {
        stop()
        mediaPlayer = MediaPlayer.create(context, uri).apply {
            start()
            setOnCompletionListener {
                this@AudioPlayer.isPlaying = false
            }
        }
        isPlaying = true
        setupVisualizer(onBeatDetected)
    }

    private fun setupVisualizer(onBeatDetected: (ToneType) -> Unit) {
        mediaPlayer?.let { player ->
            visualizer = Visualizer(player.audioSessionId).apply {
                captureSize = Visualizer.getCaptureSizeRange()[1]

                setDataCaptureListener(object : Visualizer.OnDataCaptureListener {
                    override fun onWaveFormDataCapture(v: Visualizer, waveform: ByteArray, rate: Int) {}

                    override fun onFftDataCapture(v: Visualizer, fft: ByteArray, rate: Int) {
                        if (fft.isEmpty() || !isPlaying) return

                        // --- ADVANCED 3-BAND FFT FREQUENCY ANALYSIS ---
                        var bassMag = 0.0   // Bins 2 to 6 (Low End / Kick Drums)
                        var midMag = 0.0    // Bins 12 to 24 (Vocals / Synths)
                        var highMag = 0.0   // Bins 36 to 56 (Hi-hats / Cymbals)

                        for (i in 2 until fft.size step 2) {
                            if (i + 1 < fft.size) {
                                val real = fft[i].toDouble()
                                val imaginary = fft[i + 1].toDouble()
                                val magnitude = hypot(real, imaginary)

                                when (i) {
                                    in 2..6 -> bassMag += magnitude
                                    in 12..24 -> midMag += magnitude
                                    in 36..56 -> highMag += magnitude
                                }
                            }
                        }

                        val currentTime = System.currentTimeMillis()

                        // Cooldown prevents the IR blaster from jamming
                        if (currentTime - lastBeatTime > 250) {
                            // Determine which frequency band is currently dominating
                            // Treble/Highs naturally have lower energy, so their threshold is lower
                            if (bassMag > 120.0) {
                                lastBeatTime = currentTime
                                onBeatDetected(ToneType.BASS)
                            } else if (midMag > 90.0) {
                                lastBeatTime = currentTime
                                onBeatDetected(ToneType.MID)
                            } else if (highMag > 60.0) {
                                lastBeatTime = currentTime
                                onBeatDetected(ToneType.HIGH)
                            }
                        }
                    }
                }, Visualizer.getMaxCaptureRate() / 2, false, true)

                enabled = true
            }
        }
    }

    fun play() { mediaPlayer?.let { if (!it.isPlaying) { it.start(); isPlaying = true } } }
    fun pause() { mediaPlayer?.let { if (it.isPlaying) { it.pause(); isPlaying = false } } }

    fun stop() {
        visualizer?.enabled = false
        visualizer?.release()
        visualizer = null

        mediaPlayer?.let {
            if (it.isPlaying) it.stop()
            it.release()
        }
        mediaPlayer = null
        isPlaying = false
    }

    fun release() { stop() }
}