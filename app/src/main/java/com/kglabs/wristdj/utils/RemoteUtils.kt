package com.kglabs.wristdj.utils

import android.content.Context
import android.hardware.ConsumerIrManager
import com.kglabs.wristdj.MainApplication
import timber.log.Timber

object RemoteUtils {
    private val irManager: ConsumerIrManager? by lazy {
        MainApplication.Companion.getInstance()?.let {
            it.getSystemService(Context.CONSUMER_IR_SERVICE) as ConsumerIrManager
        }
    }

    fun transmitSignal(codeString: String, frequency: Int = 38000) {
        if (codeString.isEmpty()) return
        val code = codeString.split(",").map { it.trim().toInt() }.toIntArray()
        transmitSignal(code, frequency)
    }

    fun transmitSignal(code: IntArray, frequency: Int = 38000) {
        if (irManager?.hasIrEmitter() == true) {
            Timber.d("Signal transmitted with frequency = $frequency")
            Timber.d("Signal transmitted with signal = $code")
            irManager?.transmit(frequency, code)
        }

    }
}
