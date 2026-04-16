package com.kglabs.wristdj.utils

object BasicUtils {
    fun formatTitle(title: String): String {
        return title
            .replace("_", " ")
            .replace("-", " ")
            .replace(Regex("\\d+"), "")
            .trim()
    }

    fun formatTime(ms: Int): String {
        val totalSeconds = ms / 1000
        return String.format("%d:%02d", totalSeconds / 60, totalSeconds % 60)
    }
}