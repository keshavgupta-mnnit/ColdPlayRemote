package com.kglabs.wristdj.compose.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat

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

    // --- ILLUMINATION ANIMATION ---
    val infiniteTransition = rememberInfiniteTransition(label = "illumination")
    val illuminationAlpha by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

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

    // --- MAIN UI ---
    Box(modifier = Modifier.fillMaxSize()) {
        // Full-screen illumination effect
        if (isPlaying) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFFFA500).copy(alpha = illuminationAlpha),
                                Color.Transparent,
                                Color(0xFFFFA500).copy(alpha = illuminationAlpha * 0.5f)
                            )
                        )
                    )
            )
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Wrist DJ - Player", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 16.dp, bottom = 24.dp))
            
            // Hardware Accelerated Rotating Vinyl
            VinylRecordVisualizer(
                albumArt = currentTrack?.albumArt,
                progress = if (isDragging) sliderPosition else progressPercent,
                isPlaying = isPlaying
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Metadata
            Text(currentTrack?.title ?: "No Track Selected", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center)
            Text(currentTrack?.artist ?: "Unknown Artist", color = Color.Gray, fontSize = 16.sp, modifier = Modifier.padding(top = 4.dp))
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Actions (Direct access to Files/Folder)
            Row(
                modifier = Modifier.fillMaxWidth(0.9f),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(onClick = { filePickerLauncher.launch(arrayOf("audio/*")) }, modifier = Modifier.size(56.dp)) {
                        Icon(Icons.Default.LibraryMusic, "Add Files", tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                    Text("Files", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(onClick = { folderPickerLauncher.launch(null) }, modifier = Modifier.size(56.dp)) {
                        Icon(Icons.Default.FolderSpecial, "Add Folder", tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                    Text("Folder", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(onClick = { showPlaylist = true }, modifier = Modifier.size(56.dp)) {
                        Icon(Icons.Default.QueueMusic, "Playlist", tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                    Text("Playlist (${playlist.size})", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Interactable Seek Bar
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

            // Media Controls
            Row(
                modifier = Modifier.fillMaxWidth(0.85f),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlowingMediaButton(
                    icon = Icons.Default.SkipPrevious,
                    glowColor = Color(0xFF00FF00), // Neon Green
                    size = 60.dp,
                    iconSize = 34.dp
                ) {
                    GlobalAudioPlayer.prev(context, onBeat)
                }

                GlowingMediaButton(
                    icon = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    glowColor = Color(0xFF00E5FF),
                    secondaryColor = Color(0xFFFF007F),
                    size = 80.dp,
                    iconSize = 48.dp
                ) {
                    GlobalAudioPlayer.togglePlayPause(context, onBeat)
                }

                GlowingMediaButton(
                    icon = Icons.Default.SkipNext,
                    glowColor = Color(0xFFFFA500), // Orange
                    size = 60.dp,
                    iconSize = 34.dp
                ) {
                    GlobalAudioPlayer.next(context, onBeat)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // --- PLAYLIST BOTTOM SHEET ---
    if (showPlaylist) {
        ModalBottomSheet(onDismissRequest = { showPlaylist = false }, containerColor = Color(0xFF111111)) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = searchQuery, onValueChange = { searchQuery = it },
                        placeholder = { Text("Search songs...", color = Color.Gray) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.Gray) },
                        trailingIcon = { if (searchQuery.isNotEmpty()) { IconButton(onClick = { searchQuery = "" }) { Icon(Icons.Default.Close, "Clear", tint = Color.Gray) } } },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF00E5FF), unfocusedBorderColor = Color.DarkGray, focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        singleLine = true, modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    // Quick-Add Actions in Sheet
                    Row(
                        modifier = Modifier
                            .background(Color(0xFF222222), RoundedCornerShape(12.dp))
                            .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { filePickerLauncher.launch(arrayOf("audio/*")) }) {
                            Icon(Icons.Default.LibraryMusic, "Add Files", tint = Color.White)
                        }
                        Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color.DarkGray))
                        IconButton(onClick = { folderPickerLauncher.launch(null) }) {
                            Icon(Icons.Default.FolderSpecial, "Add Folder", tint = Color.White)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                val filteredList = playlist.withIndex().filter { it.value.title.contains(searchQuery, ignoreCase = true) || it.value.artist.contains(searchQuery, ignoreCase = true) }
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
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).background(if (isCurrent) Color(0xFF222222) else Color.Transparent, RoundedCornerShape(8.dp)).clickable { GlobalAudioPlayer.playTrackAt(context, originalIndex, onBeat) }.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(track.title, color = if (isCurrent) Color(0xFF00FF00) else Color.White, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(track.artist, color = Color.Gray, fontSize = 12.sp)
                                }
                                IconButton(onClick = { GlobalAudioPlayer.removeTrack(context, originalIndex) }) { Icon(Icons.Default.DeleteOutline, "Remove", tint = Color(0xFFFF5555)) }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun formatTime(ms: Int): String {
    val totalSeconds = ms / 1000
    return String.format("%d:%02d", totalSeconds / 60, totalSeconds % 60)
}

// --- VISUAL UI COMPONENTS ---

@Composable
fun VinylRecordVisualizer(albumArt: Bitmap?, progress: Float, isPlaying: Boolean) {
    var currentRotation by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            kotlinx.coroutines.delay(16)
            currentRotation = (currentRotation + 1.5f) % 360f
        }
    }

    val activeGlow = if (isPlaying) Color(0xFFFFA500) else Color.DarkGray

    // The Octagon Bug Fix: Strict Shadow -> Clip -> Solid Background ordering
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(220.dp)
            .shadow(elevation = if (isPlaying) 24.dp else 0.dp, shape = CircleShape, spotColor = activeGlow)
            .clip(CircleShape) // Explicit clip forces the bounds to be perfectly round
            .background(Color(0xFF050505))
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            drawArc(color = Color(0xFF222222), startAngle = 0f, sweepAngle = 360f, useCenter = false, style = Stroke(width = 10.dp.toPx()))
            drawArc(color = activeGlow, startAngle = -90f, sweepAngle = 360f * progress, useCenter = false, style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round))
        }

        Box(
            modifier = Modifier
                .size(180.dp)
                .clip(CircleShape)
                .background(Color(0xFF1A1A1A))
                .drawBehind {
                    drawCircle(color = Color(0xFF333333), radius = size.minDimension / 2.2f, style = Stroke(width = 2f))
                    drawCircle(color = Color(0xFF333333), radius = size.minDimension / 2.6f, style = Stroke(width = 2f))
                    drawCircle(color = Color(0xFF333333), radius = size.minDimension / 3.2f, style = Stroke(width = 2f))
                }
                .graphicsLayer { rotationZ = currentRotation },
            contentAlignment = Alignment.Center
        ) {
            if (albumArt != null) {
                androidx.compose.foundation.Image(
                    bitmap = albumArt.asImageBitmap(),
                    contentDescription = "Album Art",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(80.dp).clip(CircleShape).border(2.dp, Color(0xFF111111), CircleShape)
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = if (isPlaying) "PLAYING" else "WRIST DJ", color = Color(0xFFFFA500), fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFFF007F)))
                        Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(Color(0xFFFFA500)))
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF00E5FF)))
                    }
                }
            }
        }
    }
}

// Updated to match Manual Color Deck style
@Composable
fun GlowingMediaButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    glowColor: Color,
    secondaryColor: Color? = null,
    size: Dp = 60.dp,
    iconSize: Dp = 28.dp,
    onClick: () -> Unit
) {
    val brush = if (secondaryColor != null) {
        Brush.linearGradient(listOf(glowColor, secondaryColor))
    } else {
        SolidColor(glowColor)
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(size)
            .shadow(
                elevation = if (glowColor != Color.Transparent) 16.dp else 0.dp,
                shape = CircleShape,
                spotColor = glowColor
            )
            .clip(CircleShape)
            .background(Color(0xFF050505))
            .clickable { onClick() }
    ) {
        // Inner Gradient Glow
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            glowColor.copy(alpha = 0.4f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Boundary - similar to ColorSwatchButton (2dp) or ColorHeroVisualizer (12dp)
        // I'll use 4dp for a balance, keeping it "Studio" style but substantial
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(width = 4.dp, brush = brush, shape = CircleShape)
        )

        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(iconSize)
        )
    }
}