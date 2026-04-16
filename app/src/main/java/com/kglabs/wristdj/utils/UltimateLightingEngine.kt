package com.kglabs.wristdj.utils

enum class EnergyLevel {
    PEAK, INTENSE, SMOOTH, SUBTLE
}

object UltimateLightingEngine {

    // 🔥 IR Protection
    private var lastIrTime = 0L
    private const val MIN_IR_INTERVAL = 90L

    // 🎯 State Memory
    private var lastColor: String? = null
    private var lastEnergy: EnergyLevel = EnergyLevel.SUBTLE
    private var streak = 0

    // 📈 Momentum
    private var energyTrend = 0 // +1 rising, -1 falling

    // 🌊 Color Flow (The Journey)
    private val colorFlow = listOf(
        "Blue", "Cyan", "Green", "Mint",
        "Yellow", "Orange", "Red", "Magenta"
    )
    private var flowIndex = 0

    private fun nextFlowColor(): String {
        flowIndex = (flowIndex + 1) % colorFlow.size
        return colorFlow[flowIndex]
    }

    private fun prevFlowColor(): String {
        flowIndex = if (flowIndex <= 0) colorFlow.size - 1 else flowIndex - 1
        return colorFlow[flowIndex]
    }

    // 🚫 Smart Pools for accents (No back-to-back repeats)
    private class ColorPool(private val base: List<String>) {
        private var seq = base.shuffled().toMutableList()
        private var index = 0

        fun next(last: String?): String {
            if (index >= seq.size) {
                val newSeq = base.shuffled().toMutableList()
                if (last != null && newSeq.first() == last && newSeq.size > 1) {
                    val tmp = newSeq[0]; newSeq[0] = newSeq[1]; newSeq[1] = tmp
                }
                seq = newSeq
                index = 0
            }
            return seq[index++]
        }
        fun reset() { seq = base.shuffled().toMutableList(); index = 0 }
    }

    private val peakPool = ColorPool(listOf("Red", "Magenta", "Amber", "Red Orange"))
    private val intensePool = ColorPool(listOf("Orange", "Pink", "Yellow", "Turquoise", "Green"))
    private val smoothPool = ColorPool(listOf("Mint", "Pistacchio", "Green 2", "Light Red", "Warm Yellow"))
    private val subtlePool = ColorPool(listOf("Blue", "Cyan", "Ice", "Little Blue", "Cream", "Light Mint"))

    @Synchronized
    fun onAudioEvent(energy: EnergyLevel, tone: ToneType): String? {
        val now = System.currentTimeMillis()

        if (now - lastIrTime < MIN_IR_INTERVAL) return null

        // 📈 Track momentum (Note: Lower ordinal = higher energy)
        energyTrend = when {
            energy.ordinal < lastEnergy.ordinal -> 1  // Rising!
            energy.ordinal > lastEnergy.ordinal -> -1 // Falling!
            else -> energyTrend
        }

        // 🔥 Streak logic (build-up detection)
        if (energy == EnergyLevel.PEAK || energy == EnergyLevel.INTENSE) {
            streak++
        } else {
            streak = 0
        }

        val color = when (energy) {
            EnergyLevel.PEAK -> {
                // 💥 DROP DETECTED: Massive flash on impact
                if (streak == 3 && energyTrend > 0) {
                    streak++
                    flowIndex = colorFlow.indexOf("Red")
                    "White"
                } else {
                    if (Math.random() < 0.2) "White" else peakPool.next(lastColor)
                }
            }
            EnergyLevel.INTENSE -> {
                when (tone) {
                    ToneType.BASS -> nextFlowColor()
                    ToneType.MID -> intensePool.next(lastColor)
                    ToneType.HIGH -> nextFlowColor()
                }
            }
            EnergyLevel.SMOOTH -> {
                when (tone) {
                    ToneType.BASS -> nextFlowColor()
                    ToneType.MID -> smoothPool.next(lastColor)
                    ToneType.HIGH -> prevFlowColor()
                }
            }
            EnergyLevel.SUBTLE -> {
                prevFlowColor()
            }
        }

        lastIrTime = now
        lastColor = color
        lastEnergy = energy

        return color
    }

    @Synchronized
    fun reset() {
        lastColor = null
        lastEnergy = EnergyLevel.SUBTLE
        lastIrTime = 0L
        streak = 0
        flowIndex = 0
        energyTrend = 0
        peakPool.reset(); intensePool.reset(); smoothPool.reset(); subtlePool.reset()
    }
}