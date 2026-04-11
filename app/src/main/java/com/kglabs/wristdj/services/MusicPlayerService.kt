package com.kglabs.wristdj.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
import com.kglabs.wristdj.MainActivity
import com.kglabs.wristdj.utils.BandColorConstants
import com.kglabs.wristdj.utils.GlobalAudioPlayer
import com.kglabs.wristdj.utils.IRUtils
import com.kglabs.wristdj.utils.ToneType

class MusicPlayerService : Service() {

    private val binder = LocalBinder()
    private lateinit var mediaSession: MediaSessionCompat
    private val CHANNEL_ID = "wrist_dj_media_channel"
    private val NOTIFICATION_ID = 1

    // This is where your FFT Analyzer from the previous step will live
    // val musicAnalyzer = MusicPlayerAnalyzer(this)

    inner class LocalBinder : Binder() {
        fun getService(): MusicPlayerService = this@MusicPlayerService
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        mediaSession = MediaSessionCompat(this, "WristDJMediaSession").apply {
            isActive = true
            setCallback(object : MediaSessionCompat.Callback() {
                override fun onPlay() {
                    GlobalAudioPlayer.togglePlayPause(this@MusicPlayerService, onBeat)
                    updateNotification()
                }

                override fun onPause() {
                    GlobalAudioPlayer.pause()
                    updateNotification()
                }

                override fun onSkipToNext() {
                    GlobalAudioPlayer.next(this@MusicPlayerService, onBeat)
                    updateNotification()
                }

                override fun onSkipToPrevious() {
                    GlobalAudioPlayer.prev(this@MusicPlayerService, onBeat)
                    updateNotification()
                }

                override fun onSeekTo(pos: Long) {
                    GlobalAudioPlayer.seekTo(pos.toInt())
                    updateNotification()
                }
            })
        }
    }

    private val onBeat: (ToneType) -> Unit = { toneType ->
        val colorToSignalMap = BandColorConstants.buttons.toMap()
        val colorName = when (toneType) {
            ToneType.BASS -> BandColorConstants.bassColors.random()
            ToneType.MID -> BandColorConstants.midColors.random()
            ToneType.HIGH -> BandColorConstants.highColors.random()
        }
        colorToSignalMap[colorName]?.let { IRUtils.transmitSignal(it) }
    }

    private fun updateNotification() {
        val currentTrack = if (GlobalAudioPlayer.currentTrackIndex.value in GlobalAudioPlayer.playlist.indices) {
            GlobalAudioPlayer.playlist[GlobalAudioPlayer.currentTrackIndex.value]
        } else null
        
        promoteToForeground(
            title = currentTrack?.title ?: "Wrist DJ",
            albumArt = currentTrack?.albumArt
        )
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        androidx.media.session.MediaButtonReceiver.handleIntent(mediaSession, intent)
        return super.onStartCommand(intent, flags, startId)
    }

    // --- Lifecycle Promotion Methods ---

    fun promoteToForeground(title: String = "Wrist DJ", albumArt: Bitmap? = null) {
        val isPlaying = GlobalAudioPlayer.isPlaying.value
        val currentTrack = if (GlobalAudioPlayer.currentTrackIndex.value in GlobalAudioPlayer.playlist.indices) {
            GlobalAudioPlayer.playlist[GlobalAudioPlayer.currentTrackIndex.value]
        } else null
        
        val duration = currentTrack?.duration ?: 0
        val position = GlobalAudioPlayer.mediaPlayer?.currentPosition ?: 0

        updateMediaSession(title, albumArt, isPlaying, duration, position)
        val notification = buildNotification(title, isPlaying = isPlaying, albumArt = albumArt)

        // Android 10+ requires explicit foreground types for Media
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun updateMediaSession(title: String, albumArt: Bitmap?, isPlaying: Boolean, duration: Int, position: Int) {
        val metadataBuilder = MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, "Wrist DJ")
            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration.toLong())

        if (albumArt != null) {
            metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, albumArt)
        }
        mediaSession.setMetadata(metadataBuilder.build())

        val state = if (isPlaying) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED
        mediaSession.setPlaybackState(
            PlaybackStateCompat.Builder()
                .setState(state, position.toLong(), 1.0f)
                .setActions(PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_PAUSE or PlaybackStateCompat.ACTION_PLAY_PAUSE or PlaybackStateCompat.ACTION_SKIP_TO_NEXT or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or PlaybackStateCompat.ACTION_SEEK_TO)
                .build()
        )
    }

    fun demoteToBackground() {
        // Hide the notification but keep the service alive
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            stopForeground(true)
        }
    }

    // --- Notification Builder ---

    private fun buildNotification(title: String, isPlaying: Boolean, albumArt: Bitmap?): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java)
        val pendingOpenAppIntent = PendingIntent.getActivity(
            this, 0, openAppIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val playPauseAction = if (isPlaying) {
            NotificationCompat.Action(
                android.R.drawable.ic_media_pause, "Pause",
                androidx.media.session.MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_PAUSE)
            )
        } else {
            NotificationCompat.Action(
                android.R.drawable.ic_media_play, "Play",
                androidx.media.session.MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_PLAY)
            )
        }

        val prevAction = NotificationCompat.Action(
            android.R.drawable.ic_media_previous, "Previous",
            androidx.media.session.MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)
        )

        val nextAction = NotificationCompat.Action(
            android.R.drawable.ic_media_next, "Next",
            androidx.media.session.MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_SKIP_TO_NEXT)
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setLargeIcon(albumArt)
            .setContentTitle(title)
            .setContentText("Syncing lights to music...")
            .setContentIntent(pendingOpenAppIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(prevAction)
            .addAction(playPauseAction)
            .addAction(nextAction)
            .setStyle(MediaStyle()
                .setMediaSession(mediaSession.sessionToken)
                .setShowActionsInCompactView(0, 1, 2)
            )
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(isPlaying)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Media Playback",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaSession.release()
    }
}