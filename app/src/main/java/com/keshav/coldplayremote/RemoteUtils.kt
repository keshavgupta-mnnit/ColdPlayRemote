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
        // The exact Carrier Frequency for the NEC Protocol
        val frequency = 38000

// The cleaned Volume Up pattern
        val volumeUpPattern = intArrayOf(
            9000, 4400,
            600, 1650, 600, 1650, 550, 550, 550, 550,
            550, 600, 550, 550, 550, 600, 550, 1700,
            550, 550, 550, 1700, 550, 1650, 550, 550,
            550, 1700, 550, 1650, 600, 550, 550, 1650,
            600, 1650, 550, 1650, 600, 550, 550, 1650,
            600, 550, 550, 550, 550, 600, 550, 550,
            550, 600, 550, 550, 550, 1700, 550, 550,
            550, 1700, 550, 1650, 600, 1600, 600, 1650,
            600
        )

// Fire the signal!

        val pattern = necHexToPattern(code)
        if (irManager?.hasIrEmitter() == true) {
            Log.d("Keshav","Signal transmitted with frequency = $frequency")
            Log.d("Keshav","Signal transmitted with signal = $volumeUpPattern")
            irManager?.transmit(frequency, volumeUpPattern)

        }

    }
}