package com.keshav.coldplayremote

import android.content.Context
import android.hardware.ConsumerIrManager

object RemoteUtils {
    private val irManager: ConsumerIrManager? by lazy {
        MainApplication.getInstance()?.let {
            it.getSystemService(Context.CONSUMER_IR_SERVICE) as ConsumerIrManager
        }
    }

    fun necHexToPattern(code: Int): IntArray {
        val pattern = mutableListOf<Int>()

        // NEC lead pulse: 9000us on, 4500us off
        pattern.add(9000)
        pattern.add(4500)

        // NEC data bits: MSB first
        for (i in 31 downTo 0) {
            pattern.add(560) // mark always 560
            if ((code shr i) and 1 == 1) {
                pattern.add(1690) // logical 1
            } else {
                pattern.add(560)  // logical 0
            }
        }

        // NEC stop bit: final 560µs pulse (no off time needed)
        pattern.add(560)

        return pattern.toIntArray()
    }

    fun transmitSignal(code: Int, frequency: Int = 38000) {
        val pattern = necHexToPattern(code)
        if (irManager?.hasIrEmitter() == true) {
            irManager?.transmit(frequency, pattern)
        }

    }
}