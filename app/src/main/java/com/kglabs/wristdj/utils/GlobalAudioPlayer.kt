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
import com.kglabs.wristdj.models.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import kotlin.math.hypot

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

    private const val PREFS_NAME = "WristDJ_Playlist_V3"
    private const val KEY_PLAYLIST_JSON = "playlist_data"
    private const val KEY_FOLDERS_JSON = "authorized_folders"

    private val authorizedFolders = mutableSetOf<String>()

    fun init(context: Context) {
        loadPlaylist(context)
    }

    private fun savePlaylist(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonArray = JSONArray()
        
        playlist.forEach { track ->
            try {
                val obj = JSONObject().apply {
                    put("uri", track.uri.toString())
                    put("title", track.title)
                    put("duration", track.duration)
                }
                jsonArray.put(obj)
                
                // Save album art to disk
                track.albumArt?.let { bitmap ->
                    val artFile = File(context.cacheDir, "art_${track.uri.toString().hashCode()}.jpg")
                    if (!artFile.exists()) {
                        FileOutputStream(artFile).use { out ->
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, out)
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Save failed for ${track.title}")
            }
        }
        
        val folderArray = JSONArray(authorizedFolders.toList())
        
        prefs.edit()
            .putString(KEY_PLAYLIST_JSON, jsonArray.toString())
            .putString(KEY_FOLDERS_JSON, folderArray.toString())
            .apply()
    }

    private fun loadPlaylist(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonString = prefs.getString(KEY_PLAYLIST_JSON, null) ?: return
        val folderString = prefs.getString(KEY_FOLDERS_JSON, "[]")
        
        scope.launch(Dispatchers.IO) {
            try {
                // Restore Folder Permissions First
                val folderArray = JSONArray(folderString)
                for (i in 0 until folderArray.length()) {
                    val folderUriStr = folderArray.getString(i)
                    authorizedFolders.add(folderUriStr)
                    try {
                        context.contentResolver.takePersistableUriPermission(
                            Uri.parse(folderUriStr),
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )
                        Timber.d("Re-persisted folder: $folderUriStr")
                    } catch (e: Exception) {
                        Timber.w("Failed to re-persist folder: $folderUriStr")
                    }
                }

                val jsonArray = JSONArray(jsonString)
                val loadedTracks = mutableListOf<AudioTrack>()
                
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val uri = Uri.parse(obj.getString("uri"))
                    
                    // Note: individual file takePersistableUriPermission often fails for Tree URIs,
                    // but we've refreshed the parent folder permission above.
                    
                    val title = obj.getString("title")
                    val duration = obj.getInt("duration")
                    
                    val artFile = File(context.cacheDir, "art_${uri.toString().hashCode()}.jpg")
                    val art = if (artFile.exists()) BitmapFactory.decodeFile(artFile.absolutePath) else null
                    
                    loadedTracks.add(AudioTrack(uri, title, duration, art))
                }
                
                withContext(Dispatchers.Main) {
                    playlist.clear()
                    playlist.addAll(loadedTracks)
                    if (playlist.isNotEmpty()) currentTrackIndex.value = 0
                }
            } catch (e: Exception) {
                Timber.e(e, "Load failed")
            }
        }
    }

    private fun extractTrackInfo(context: Context, uri: Uri, preResolvedName: String? = null): AudioTrack {
        val mmr = MediaMetadataRetriever()
        var title = preResolvedName ?: ""
        var duration = 0
        var art: Bitmap? = null

        try {
            mmr.setDataSource(context, uri)
            val metaTitle = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
            if (!metaTitle.isNullOrBlank()) title = metaTitle
            
            duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toIntOrNull() ?: 0
            val artBytes = mmr.embeddedPicture
            art = artBytes?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }
        } catch (e: Exception) {
            Timber.w("MMR failed for $uri")
        } finally {
            try { mmr.release() } catch (e: Exception) {}
        }

        if (title.isBlank() || isTechnicalId(title)) {
            val fileName = getFileNameFromUri(context, uri)
            if (!fileName.isNullOrBlank() && !isTechnicalId(fileName)) title = fileName
        }

        title = cleanTitle(title, uri)
        return AudioTrack(uri, title, duration, art)
    }

    private fun isTechnicalId(text: String): Boolean = 
        text.isBlank() || text.all { it.isDigit() || it == ':' || it == '_' || it == '-' || it == '.' }

    private fun getFileNameFromUri(context: Context, uri: Uri): String? {
        var name: String? = null
        try {
            context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) name = cursor.getString(0)
            }
        } catch (e: Exception) {}
        return name ?: uri.lastPathSegment
    }

    private fun cleanTitle(title: String, uri: Uri): String {
        var cleaned = title
        if (cleaned.contains(":") || cleaned.contains("/")) {
            cleaned = Uri.decode(cleaned).substringAfterLast("/").substringAfterLast(":")
        }
        if (cleaned.contains(".") && cleaned.length > 4) cleaned = cleaned.substringBeforeLast(".")
        if (isTechnicalId(cleaned)) {
            val fromUri = Uri.decode(uri.toString()).substringAfterLast("/").substringBeforeLast(".")
            if (!isTechnicalId(fromUri)) cleaned = fromUri
        }
        return cleaned.ifBlank { "Track ${UUID.randomUUID().toString().take(4)}" }
    }

    fun addTracks(context: Context, uris: List<Uri>, preResolvedNames: Map<Uri, String>? = null, isInitialLoad: Boolean = false) {
        scope.launch(Dispatchers.IO) {
            val newTracks = mutableListOf<AudioTrack>()
            for (uri in uris) {
                if (playlist.any { it.uri == uri }) continue
                // Note: If uri is a child of a TreeUri, takePersistableUriPermission on it will fail.
                // We rely on the parent treeUri permission instead.
                
                val track = extractTrackInfo(context, uri, preResolvedNames?.get(uri))
                if (playlist.none { it.title == track.title && it.duration == track.duration }) {
                    newTracks.add(track)
                }
            }

            if (newTracks.isNotEmpty()) {
                withContext(Dispatchers.Main) {
                    playlist.addAll(newTracks)
                    savePlaylist(context) // Always save if we added tracks
                    if (currentTrackIndex.value == -1) currentTrackIndex.value = 0
                }
            }
        }
    }

    fun addTracksFromFolder(context: Context, treeUri: Uri) {
        // Persist the root folder permission
        try {
            context.contentResolver.takePersistableUriPermission(
                treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            authorizedFolders.add(treeUri.toString())
        } catch (e: Exception) {
            Timber.e(e, "Failed to persist folder permission for $treeUri")
        }

        scope.launch(Dispatchers.IO) {
            val audioUris = mutableListOf<Uri>()
            val preResolvedNames = mutableMapOf<Uri, String>()
            try {
                val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(
                    treeUri, DocumentsContract.getTreeDocumentId(treeUri)
                )

                context.contentResolver.query(
                    childrenUri,
                    arrayOf(
                        DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                        DocumentsContract.Document.COLUMN_MIME_TYPE,
                        DocumentsContract.Document.COLUMN_DISPLAY_NAME
                    ),
                    null, null, null
                )?.use { cursor ->
                    while (cursor.moveToNext()) {
                        val docId = cursor.getString(0)
                        val mimeType = cursor.getString(1)
                        val displayName = cursor.getString(2)
                        
                        if (mimeType?.startsWith("audio/") == true || mimeType?.contains("ogg") == true || mimeType?.contains("flac") == true) {
                            val fileUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, docId)
                            audioUris.add(fileUri)
                            if (!displayName.isNullOrBlank()) preResolvedNames[fileUri] = displayName
                        }
                    }
                }
                if (audioUris.isNotEmpty()) withContext(Dispatchers.Main) { addTracks(context, audioUris, preResolvedNames) }
            } catch (e: Exception) {
                Timber.e(e, "Folder scan failed")
            }
        }
    }

    private var consecutiveErrors = 0
    private const val MAX_CONSECUTIVE_ERRORS = 5

    fun playTrackAt(context: Context, index: Int, onBeatDetected: (ToneType) -> Unit) {
        if (index !in playlist.indices) {
            Timber.e("Index $index out of bounds for playlist size ${playlist.size}")
            consecutiveErrors = 0 // Reset
            return
        }
        stop()
        
        // Stop Mic if it's running
        GlobalMicAnalyzer.stopListening()

        currentTrackIndex.value = index
        val track = playlist[index]
        
        try {
            Timber.d("Attempting to play: ${track.title} (URI: ${track.uri})")
            
            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, track.uri)
                setOnCompletionListener { 
                    Timber.d("Playback completed for ${track.title}. Moving to next.")
                    consecutiveErrors = 0
                    next(context, onBeatDetected) 
                }
                setOnErrorListener { _, what, extra ->
                    Timber.e("MediaPlayer Error: what=$what, extra=$extra")
                    consecutiveErrors++
                    if (consecutiveErrors < MAX_CONSECUTIVE_ERRORS) {
                        next(context, onBeatDetected)
                    } else {
                        Timber.e("Too many consecutive errors, stopping playback.")
                        GlobalAudioPlayer.isPlaying.value = false
                        consecutiveErrors = 0
                    }
                    true // error handled
                }
                prepare()
                start()
            }
            isPlaying.value = true
            currentPosition.value = 0
            startProgressTracker()
            setupVisualizer(onBeatDetected)
        } catch (e: Exception) {
            Timber.e(e, "Play failed for index $index: ${track.title}")
            isPlaying.value = false
            
            consecutiveErrors++
            if (consecutiveErrors < MAX_CONSECUTIVE_ERRORS) {
                // Attempt to recover by going to next track after a delay
                scope.launch {
                    delay(500)
                    next(context, onBeatDetected)
                }
            } else {
                Timber.e("Too many consecutive errors in catch block, stopping.")
                consecutiveErrors = 0
            }
        }
    }

    fun togglePlayPause(context: Context, onBeatDetected: (ToneType) -> Unit) {
        mediaPlayer?.let { player ->
            if (player.isPlaying) {
                player.pause()
                isPlaying.value = false
            } else {
                // Stop Mic if it's running when resuming music
                GlobalMicAnalyzer.stopListening()
                player.start()
                isPlaying.value = true
                startProgressTracker()
            }
        } ?: run {
            if (playlist.isNotEmpty()) playTrackAt(context, currentTrackIndex.value.coerceAtLeast(0), onBeatDetected)
        }
    }

    fun pause() {
        mediaPlayer?.let { if (it.isPlaying) { it.pause(); isPlaying.value = false } }
    }

    fun next(context: Context, onBeatDetected: (ToneType) -> Unit) {
        if (playlist.isEmpty()) return
        playTrackAt(context, (currentTrackIndex.value + 1) % playlist.size, onBeatDetected)
    }

    fun prev(context: Context, onBeatDetected: (ToneType) -> Unit) {
        if (playlist.isEmpty()) return
        val newIndex = if (currentTrackIndex.value <= 0) playlist.size - 1 else currentTrackIndex.value - 1
        playTrackAt(context, newIndex, onBeatDetected)
    }

    fun stop() {
        progressJob?.cancel()
        visualizer?.apply { try { enabled = false; release() } catch (e: Exception) {} }
        visualizer = null
        mediaPlayer?.apply { try { if (isPlaying) stop(); release() } catch (e: Exception) {} }
        mediaPlayer = null
        isPlaying.value = false
    }

    fun removeTrack(context: Context, index: Int) {
        if (index in playlist.indices) {
            val track = playlist.removeAt(index)
            File(context.cacheDir, "art_${track.uri.hashCode()}.jpg").delete()
            savePlaylist(context)
            if (currentTrackIndex.value == index) stop()
            if (playlist.isEmpty()) currentTrackIndex.value = -1
            else if (currentTrackIndex.value >= playlist.size) currentTrackIndex.value = playlist.size - 1
        }
    }

    private fun startProgressTracker() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isPlaying.value) {
                mediaPlayer?.let { if (it.isPlaying) currentPosition.value = it.currentPosition }
                delay(500)
            }
        }
    }

    private fun setupVisualizer(onBeatDetected: (ToneType) -> Unit) {
        val sessionId = mediaPlayer?.audioSessionId ?: return
        try {
            visualizer = Visualizer(sessionId).apply {
                captureSize = Visualizer.getCaptureSizeRange()[1]
                setDataCaptureListener(object : Visualizer.OnDataCaptureListener {
                    override fun onWaveFormDataCapture(v: Visualizer, w: ByteArray, r: Int) {}
                    override fun onFftDataCapture(v: Visualizer, fft: ByteArray, r: Int) {
                        if (fft.isEmpty() || !isPlaying.value) return
                        var b = 0.0; var m = 0.0; var h = 0.0
                        for (i in 2 until fft.size step 2) {
                            val mag = hypot(fft[i].toDouble(), fft[i + 1].toDouble())
                            when (i) { in 2..6 -> b += mag; in 12..24 -> m += mag; in 36..56 -> h += mag }
                        }
                        val now = System.currentTimeMillis()
                        if (now - lastBeatTime > 250) {
                            if (b > 120.0) { lastBeatTime = now; onBeatDetected(ToneType.BASS) }
                            else if (m > 90.0) { lastBeatTime = now; onBeatDetected(ToneType.MID) }
                            else if (h > 60.0) { lastBeatTime = now; onBeatDetected(ToneType.HIGH) }
                        }
                    }
                }, Visualizer.getMaxCaptureRate() / 2, false, true)
                enabled = true
            }
        } catch (e: Exception) { visualizer = null }
    }

    fun seekTo(position: Int) {
        mediaPlayer?.seekTo(position)
        currentPosition.value = position
    }
}