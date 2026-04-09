package com.kglabs.wristdj.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.media.audiofx.Visualizer
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import timber.log.Timber
import kotlin.math.hypot

data class AudioTrack(
    val uri: Uri,
    val title: String,
    val artist: String,
    val duration: Int,
    val albumArt: Bitmap?,
    val glowColor: Color = Color(0xFFFFA500)
)

object GlobalAudioPlayer {
    var mediaPlayer: MediaPlayer? = null
    private var visualizer: Visualizer? = null

    // --- APP WIDE STATE ---
    val playlist = mutableStateListOf<AudioTrack>()
    var currentTrackIndex = mutableStateOf(-1)
    var isPlaying = mutableStateOf(false)
    var currentPosition = mutableStateOf(0)

    private var progressJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)
    private var lastBeatTime = 0L

    private const val PREFS_NAME = "WristDJ_Playlist"
    private const val KEY_URIS = "playlist_uris"

    fun init(context: Context) {
        loadPlaylist(context)
    }

    private fun savePlaylist(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonArray = JSONArray()
        playlist.forEach { jsonArray.put(it.uri.toString()) }
        prefs.edit().putString(KEY_URIS, jsonArray.toString()).apply()
    }

    private fun loadPlaylist(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonString = prefs.getString(KEY_URIS, null) ?: return
        try {
            val jsonArray = JSONArray(jsonString)
            val uris = mutableListOf<Uri>()
            for (i in 0 until jsonArray.length()) {
                uris.add(Uri.parse(jsonArray.getString(i)))
            }
            if (uris.isNotEmpty()) addTracks(context, uris, isInitialLoad = true)
        } catch (e: Exception) {
            Timber.e(e, "Failed to load playlist")
        }
    }

    // 1. Scan and add files natively
    fun addTracks(context: Context, uris: List<Uri>, isInitialLoad: Boolean = false) {
        scope.launch(Dispatchers.IO) {
            val newTracks = mutableListOf<AudioTrack>()
            
            for (uri in uris) {
                // Prevent exact URI duplicates first
                if (playlist.any { it.uri == uri }) continue

                // Take persistable permission for the URI
                try {
                    val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    context.contentResolver.takePersistableUriPermission(uri, takeFlags)
                } catch (e: Exception) {}

                val mmr = MediaMetadataRetriever()
                var title = ""
                var artist = "Unknown Artist"
                var duration = 0
                var art: Bitmap? = null

                try {
                    mmr.setDataSource(context, uri)
                    title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) ?: ""
                    artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) ?: "Unknown Artist"
                    duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toIntOrNull() ?: 0
                    val artBytes = mmr.embeddedPicture
                    art = artBytes?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }
                } catch (e: Exception) {
                } finally {
                    try { mmr.release() } catch (e: Exception) {}
                }

                // If Title is empty, get the actual filename
                if (title.isBlank()) {
                    try {
                        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                            if (cursor.moveToFirst()) {
                                title = cursor.getString(nameIndex)
                            }
                        }
                    } catch (e: Exception) {}
                    
                    if (title.isBlank()) {
                        title = uri.lastPathSegment ?: "Unknown Track"
                    }
                }
                
                // Clean up technical names like "msf:100..." or "primary:Music/..."
                if (title.contains(":") || title.contains("/")) {
                    val decoded = Uri.decode(title)
                    title = decoded.substringAfterLast("/")
                    if (title.contains(":")) title = title.substringAfterLast(":")
                }
                
                // Remove file extension if present
                if (title.contains(".") && title.length > 4) {
                    title = title.substringBeforeLast(".")
                }

                // Robust Duplicate Check: Check if this song (title, artist, duration) is already in playlist
                val isAlreadyInPlaylist = playlist.any { 
                    it.title.equals(title, ignoreCase = true) && 
                    it.artist.equals(artist, ignoreCase = true) && 
                    Math.abs(it.duration - duration) < 1000 // allow 1s difference
                }
                val isAlreadyInNewBatch = newTracks.any { 
                    it.title.equals(title, ignoreCase = true) && 
                    it.artist.equals(artist, ignoreCase = true)
                }

                if (!isAlreadyInPlaylist && !isAlreadyInNewBatch) {
                    newTracks.add(AudioTrack(uri, title.ifBlank { "Unknown Track" }, artist, duration, art))
                }
            }

            withContext(Dispatchers.Main) {
                playlist.addAll(newTracks)
                if (!isInitialLoad && newTracks.isNotEmpty()) savePlaylist(context)
                if (currentTrackIndex.value == -1 && playlist.isNotEmpty()) {
                    currentTrackIndex.value = 0
                }
            }
        }
    }

    // 2. Scan entire folders securely
    fun addTracksFromFolder(context: Context, treeUri: Uri) {
        scope.launch(Dispatchers.IO) {
            val audioUris = mutableListOf<Uri>()
            try {
                val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(
                    treeUri, DocumentsContract.getTreeDocumentId(treeUri)
                )

                context.contentResolver.query(
                    childrenUri,
                    arrayOf(DocumentsContract.Document.COLUMN_DOCUMENT_ID, DocumentsContract.Document.COLUMN_MIME_TYPE),
                    null, null, null
                )?.use { cursor ->
                    while (cursor.moveToNext()) {
                        val docId = cursor.getString(0)
                        val mimeType = cursor.getString(1)
                        if (mimeType?.startsWith("audio/") == true) {
                            val fileUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, docId)
                            audioUris.add(fileUri)
                        }
                    }
                }
                if (audioUris.isNotEmpty()) {
                    withContext(Dispatchers.Main) { addTracks(context, audioUris) }
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to add tracks from folder")
            }
        }
    }

    // 3. Playback Controls
    fun playTrackAt(context: Context, index: Int, onBeatDetected: (ToneType) -> Unit) {
        if (index !in playlist.indices) return
        stop()
        currentTrackIndex.value = index
        val track = playlist[index]
        try {
            mediaPlayer = MediaPlayer.create(context, track.uri)?.apply {
                setOnCompletionListener { next(context, onBeatDetected) }
                start()
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to create or start MediaPlayer for uri: ${track.uri}")
            mediaPlayer = null
        }
        
        if (mediaPlayer == null) {
            isPlaying.value = false
            return
        }
        isPlaying.value = true
        startProgressTracker()
        setupVisualizer(onBeatDetected)
    }

    fun togglePlayPause(context: Context, onBeatDetected: (ToneType) -> Unit) {
        mediaPlayer?.let { player ->
            if (player.isPlaying) {
                player.pause()
                isPlaying.value = false
            } else {
                player.start()
                isPlaying.value = true
                startProgressTracker()
            }
        } ?: run {
            if (playlist.isNotEmpty()) {
                playTrackAt(context, currentTrackIndex.value.takeIf { it != -1 } ?: 0, onBeatDetected)
            }
        }
    }

    fun next(context: Context, onBeatDetected: (ToneType) -> Unit) {
        if (playlist.isEmpty()) return
        var nextIndex = currentTrackIndex.value + 1
        if (nextIndex >= playlist.size) nextIndex = 0
        playTrackAt(context, nextIndex, onBeatDetected)
    }

    fun prev(context: Context, onBeatDetected: (ToneType) -> Unit) {
        if (playlist.isEmpty()) return
        var prevIndex = currentTrackIndex.value - 1
        if (prevIndex < 0) prevIndex = playlist.size - 1
        playTrackAt(context, prevIndex, onBeatDetected)
    }

    fun stop() {
        progressJob?.cancel()
        try {
            visualizer?.enabled = false
            visualizer?.release()
        } catch (e: Exception) {
            Timber.e(e, "Error releasing visualizer")
        }
        visualizer = null
        
        mediaPlayer?.let {
            try {
                if (it.isPlaying) it.stop()
                it.release()
            } catch (e: Exception) {
                Timber.e(e, "Error stopping/releasing MediaPlayer")
            }
        }
        mediaPlayer = null
        isPlaying.value = false
        currentPosition.value = 0
    }

    fun removeTrack(context: Context, index: Int) {
        if (index in playlist.indices) {
            playlist.removeAt(index)
            savePlaylist(context)
            if (currentTrackIndex.value == index) {
                stop()
                currentTrackIndex.value = if (playlist.isNotEmpty()) 0 else -1
            } else if (currentTrackIndex.value > index) {
                currentTrackIndex.value -= 1
            }
        }
    }

    // 4. Progress & Visualizer Data
    private fun startProgressTracker() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isPlaying.value) {
                try {
                    val player = mediaPlayer ?: break
                    if (player.isPlaying) {
                        currentPosition.value = player.currentPosition
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Error in progress tracker")
                    break
                }
                delay(500)
            }
        }
    }

    private fun setupVisualizer(onBeatDetected: (ToneType) -> Unit) {
        mediaPlayer?.let { player ->
            try {
                visualizer = Visualizer(player.audioSessionId).apply {
                    captureSize = Visualizer.getCaptureSizeRange()[1]
                    setDataCaptureListener(object : Visualizer.OnDataCaptureListener {
                        override fun onWaveFormDataCapture(v: Visualizer, waveform: ByteArray, rate: Int) {}
                        override fun onFftDataCapture(v: Visualizer, fft: ByteArray, rate: Int) {
                            if (fft.isEmpty() || !isPlaying.value) return
                            var bassMag = 0.0; var midMag = 0.0; var highMag = 0.0
                            for (i in 2 until fft.size step 2) {
                                if (i + 1 < fft.size) {
                                    val mag = hypot(fft[i].toDouble(), fft[i + 1].toDouble())
                                    when (i) {
                                        in 2..6 -> bassMag += mag
                                        in 12..24 -> midMag += mag
                                        in 36..56 -> highMag += mag
                                    }
                                }
                            }
                            val now = System.currentTimeMillis()
                            if (now - lastBeatTime > 250) {
                                if (bassMag > 120.0) { lastBeatTime = now; onBeatDetected(ToneType.BASS) }
                                else if (midMag > 90.0) { lastBeatTime = now; onBeatDetected(ToneType.MID) }
                                else if (highMag > 60.0) { lastBeatTime = now; onBeatDetected(ToneType.HIGH) }
                            }
                        }
                    }, Visualizer.getMaxCaptureRate() / 2, false, true)
                    enabled = true
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to initialize visualizer")
                visualizer = null
            }
        }
    }

    fun seekTo(position: Int) {
        mediaPlayer?.let {
            it.seekTo(position)
            currentPosition.value = position
        }
    }
}