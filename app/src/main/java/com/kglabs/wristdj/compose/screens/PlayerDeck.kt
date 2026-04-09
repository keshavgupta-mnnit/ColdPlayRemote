package com.kglabs.wristdj.compose.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.kglabs.wristdj.compose.components.GlowingMediaButton
import com.kglabs.wristdj.compose.components.StudioBackground
import com.kglabs.wristdj.compose.components.VinylRecord
import com.kglabs.wristdj.models.AudioTrack
import com.kglabs.wristdj.utils.BandColorConstants
import com.kglabs.wristdj.utils.GlobalAudioPlayer
import com.kglabs.wristdj.utils.IRUtils
import com.kglabs.wristdj.utils.ToneType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerDeck() {
    val context = LocalContext.current
    val isPlaying by GlobalAudioPlayer.isPlaying
    val playlist = GlobalAudioPlayer.playlist
    val currentIndex by GlobalAudioPlayer.currentTrackIndex
    val currentPosition by GlobalAudioPlayer.currentPosition

    val currentTrack = if (currentIndex in playlist.indices) playlist[currentIndex] else null
    val trackDuration = currentTrack?.duration ?: 0
    val progressPercent = if (trackDuration > 0) currentPosition.toFloat() / trackDuration.toFloat() else 0f

    // Slider Dragging State
    var isDragging by remember { mutableStateOf(false) }
    var sliderPosition by remember { mutableFloatStateOf(0f) }

    var showPlaylist by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val colorToSignalMap = remember { BandColorConstants.buttons.toMap() }

    val filePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        if (uris.isNotEmpty()) GlobalAudioPlayer.addTracks(context, uris)
    }
    val folderPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        uri?.let { GlobalAudioPlayer.addTracksFromFolder(context, it) }
    }

    var hasPermission by remember { mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { hasPermission = it }
    LaunchedEffect(Unit) { if (!hasPermission) permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) }

    val onBeat: (ToneType) -> Unit = { toneType ->
        val colorName = when (toneType) {
            ToneType.BASS -> BandColorConstants.bassColors.random()
            ToneType.MID -> BandColorConstants.midColors.random()
            ToneType.HIGH -> BandColorConstants.highColors.random()
        }
        colorToSignalMap[colorName]?.let { IRUtils.transmitSignal(it) }
    }

    StudioBackground(
        showTopGlow = isPlaying,
        glowColor = currentTrack?.glowColor ?: Color(0xFFFFA500)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Wrist DJ - Player", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 16.dp, bottom = 32.dp))
            
            VinylRecord(
                albumArt = currentTrack?.albumArt,
                progress = if (isDragging) sliderPosition else progressPercent,
                isPlaying = isPlaying,
                glowColor = currentTrack?.glowColor ?: Color(0xFFFFA500)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(currentTrack?.title ?: "No Track Selected", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(0.9f),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ActionIconButton(Icons.Default.LibraryMusic, "Files") { filePickerLauncher.launch(arrayOf("audio/*")) }
                ActionIconButton(Icons.Default.FolderSpecial, "Folder") { folderPickerLauncher.launch(null) }
                ActionIconButton(Icons.Default.QueueMusic, "Playlist (${playlist.size})") { showPlaylist = true }
            }

            Spacer(modifier = Modifier.weight(1f))

            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                Slider(
                    value = if (isDragging) sliderPosition else progressPercent,
                    onValueChange = {
                        isDragging = true
                        sliderPosition = it
                    },
                    onValueChangeFinished = {
                        isDragging = false
                        val seekTarget = (sliderPosition * trackDuration).toInt()
                        GlobalAudioPlayer.seekTo(seekTarget)
                    },
                    colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = Color.White, inactiveTrackColor = Color.DarkGray)
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    val displayTime = if (isDragging) (sliderPosition * trackDuration).toInt() else currentPosition
                    Text(formatTime(displayTime), color = Color.Gray, fontSize = 12.sp)
                    Text(formatTime(trackDuration), color = Color.Gray, fontSize = 12.sp)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(0.85f),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlowingMediaButton(icon = Icons.Default.SkipPrevious, glowColor = Color(0xFF00FF00), size = 60.dp, iconSize = 34.dp) {
                    GlobalAudioPlayer.prev(context, onBeat)
                }
                GlowingMediaButton(icon = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, glowColor = Color(0xFF00E5FF), secondaryColor = Color(0xFFFF007F), size = 80.dp, iconSize = 48.dp) {
                    GlobalAudioPlayer.togglePlayPause(context, onBeat)
                }
                GlowingMediaButton(icon = Icons.Default.SkipNext, glowColor = Color(0xFFFFA500), size = 60.dp, iconSize = 34.dp) {
                    GlobalAudioPlayer.next(context, onBeat)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showPlaylist) {
        PlaylistBottomSheet(
            playlist = playlist,
            currentIndex = currentIndex,
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            onDismiss = { showPlaylist = false },
            onTrackSelected = { GlobalAudioPlayer.playTrackAt(context, it, onBeat) },
            onRemoveTrack = { GlobalAudioPlayer.removeTrack(context, it) },
            onAddFiles = { filePickerLauncher.launch(arrayOf("audio/*")) },
            onAddFolder = { folderPickerLauncher.launch(null) }
        )
    }
}

@Composable
private fun ActionIconButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(onClick = onClick, modifier = Modifier.size(56.dp)) {
            Icon(icon, label, tint = Color.White, modifier = Modifier.size(32.dp))
        }
        Text(label, color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlaylistBottomSheet(
    playlist: List<AudioTrack>,
    currentIndex: Int,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onTrackSelected: (Int) -> Unit,
    onRemoveTrack: (Int) -> Unit,
    onAddFiles: () -> Unit,
    onAddFolder: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Color(0xFF111111)) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = searchQuery, onValueChange = onSearchQueryChange,
                    placeholder = { Text("Search songs...", color = Color.Gray) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.Gray) },
                    trailingIcon = { if (searchQuery.isNotEmpty()) { IconButton(onClick = { onSearchQueryChange("") }) { Icon(Icons.Default.Close, "Clear", tint = Color.Gray) } } },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF00E5FF), unfocusedBorderColor = Color.DarkGray, focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                    singleLine = true, modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Row(
                    modifier = Modifier.background(Color(0xFF222222), RoundedCornerShape(12.dp)).padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onAddFiles) { Icon(Icons.Default.LibraryMusic, "Add Files", tint = Color.White) }
                    Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color.DarkGray))
                    IconButton(onClick = onAddFolder) { Icon(Icons.Default.FolderSpecial, "Add Folder", tint = Color.White) }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            val filteredList = playlist.withIndex().filter { it.value.title.contains(searchQuery, ignoreCase = true) }
            if (playlist.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) { Text("No songs in playlist.\nTap the + icon to add some!", color = Color.Gray, textAlign = TextAlign.Center) }
            } else if (filteredList.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) { Text("No matching songs found.", color = Color.Gray) }
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(filteredList) { indexedValue ->
                        val originalIndex = indexedValue.index
                        val track = indexedValue.value
                        val isCurrent = originalIndex == currentIndex
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).background(if (isCurrent) Color(0xFF222222) else Color.Transparent, RoundedCornerShape(8.dp)).clickable { onTrackSelected(originalIndex) }.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(track.title, color = if (isCurrent) Color(0xFF00FF00) else Color.White, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            IconButton(onClick = { onRemoveTrack(originalIndex) }) { Icon(Icons.Default.DeleteOutline, "Remove", tint = Color(0xFFFF5555)) }
                        }
                    }
                }
            }
        }
    }
}

private fun formatTime(ms: Int): String {
    val totalSeconds = ms / 1000
    return String.format("%d:%02d", totalSeconds / 60, totalSeconds % 60)
}
