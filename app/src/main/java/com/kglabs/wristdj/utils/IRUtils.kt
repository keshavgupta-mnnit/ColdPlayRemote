package com.kglabs.wristdj.utils

import android.content.Context
import android.hardware.ConsumerIrManager
import android.os.*
import androidx.compose.runtime.mutableStateOf
import com.kglabs.wristdj.MainApplication
import timber.log.Timber

object IRUtils {
    var isManualTransmitting = mutableStateOf(false)
    
    fun stopManualTransmission() {
        isManualTransmitting.value = false
    }

    private val irManager: ConsumerIrManager? by lazy {
        MainApplication.getInstance().let {
            it.getSystemService(Context.CONSUMER_IR_SERVICE) as? ConsumerIrManager
        }
    }

    private val vibrator: Vibrator? by lazy {
        val context = MainApplication.getInstance()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    private val signalCache = mutableMapOf<String, IntArray>()

    fun transmitSignal(codeString: String, frequency: Int = 38000) {
        if (codeString.isEmpty()) return
        
        try {
            val code = signalCache.getOrPut(codeString) {
                codeString.split(",").mapNotNull { it.trim().toIntOrNull() }.toIntArray()
            }
            if (code.isNotEmpty()) {
                transmitSignal(code, frequency)
            } else {
                Timber.w("Parsed IR code is empty: $codeString")
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to parse or transmit IR signal: $codeString")
        }
    }

    fun hasIrEmitter(): Boolean {
        return irManager?.hasIrEmitter() == true
    }

    fun vibrate(duration: Long = 50) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(duration)
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to vibrate")
        }
    }

    fun transmitSignal(code: IntArray, frequency: Int = 38000) {
        if (hasIrEmitter()) {
            try {
                irManager?.transmit(frequency, code)
                Timber.d("Signal transmitted: ${code.size} pulses at $frequency Hz")
            } catch (e: Exception) {
                Timber.e(e, "Failed to transmit IR signal")
            }
        }
    }
}
