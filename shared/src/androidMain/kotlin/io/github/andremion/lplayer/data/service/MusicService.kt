package io.github.andremion.lplayer.data.service

import android.content.Intent
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.R
import org.koin.android.ext.android.inject

class MusicService : MediaSessionService() {

    private val player: ExoPlayer by inject()

    // Media sessions provide a standardized way to interact with a media player across process boundaries.
    // Connecting a media session allows us to advertise your media playback externally
    // and to receive playback commands from external sources,
    // for example to integrate with system media controls on mobile and large screen devices.
    private var mediaSession: MediaSession? = null

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()
        mediaSession = MediaSession.Builder(this, player).build()
        DefaultMediaNotificationProvider.Builder(this)
            .setChannelId("music_player")
            .setChannelName(R.string.default_notification_channel_name)
            .build()
    }

    // Give other clients access to the media session.
    // Return a MediaSession to accept the connection request, or return null to reject the request.
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? =
    // We may wish to restrict access to the media session depending on the client,
    // For example, give access to a Bluetooth headset (com.android.bluetooth)
        // but restrict access to Android Auto.
        mediaSession

    // The user dismissed the app from the recent tasks
    override fun onTaskRemoved(rootIntent: Intent?) {
        if (!player.playWhenReady) {
            player.pause()
        }
        stopSelf()
    }

    override fun onDestroy() {
        player.release()
        mediaSession?.release()
        super.onDestroy()
    }
}
