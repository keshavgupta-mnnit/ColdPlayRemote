package com.keshav.coldplayremote

import android.content.Context
import android.hardware.ConsumerIrManager
import android.util.Log

object RemoteUtils {
    private val irManager: ConsumerIrManager? by lazy {
        MainApplication.getInstance()?.let {
            it.getSystemService(Context.CONSUMER_IR_SERVICE) as ConsumerIrManager
        }
    }

    fun transmitSignal(code: IntArray, frequency: Int = 40000) {
        if (irManager?.hasIrEmitter() == true) {
            Log.d("Keshav","Signal transmitted with frequency = $frequency")
            Log.d("Keshav","Signal transmitted with signal = $code")
            irManager?.transmit(frequency, code)

        }

    }
}