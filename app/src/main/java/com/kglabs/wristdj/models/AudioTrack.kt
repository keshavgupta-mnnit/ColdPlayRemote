package com.kglabs.wristdj.models

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.ui.graphics.Color

/**
 * Represents an audio track in the playlist.
 */
data class AudioTrack(
    val uri: Uri,
    val title: String,
    val duration: Int,
    val albumArt: Bitmap?,
    val glowColor: Color = Color(0xFFFFA500)
)
