package io.github.andremion.musicplayer.data.player

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.RepeatModeUtil
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import io.github.andremion.musicplayer.data.service.MusicService
import io.github.andremion.musicplayer.domain.AudioPlayer
import io.github.andremion.musicplayer.domain.entity.Playlist
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.Formatter
import java.util.Locale

@OptIn(UnstableApi::class)
internal class AudioPlayerImpl(
    private val context: Context
) : AudioPlayer {

    private lateinit var controllerFuture: ListenableFuture<MediaController>
    private val formatBuilder = StringBuilder()
    private val formatter = Formatter(formatBuilder, Locale.getDefault())
    private val player: Player
        get() = controllerFuture.get()

    override val position: Float
        get() = (player.currentPosition / player.duration.toFloat()).coerceIn(0f, 1f)
    override val time: String
        get() = Util.getStringForTime(formatBuilder, formatter, player.currentPosition)
    override val duration: String
        get() = Util.getStringForTime(formatBuilder, formatter, player.duration)

    private val mutableEvents = MutableSharedFlow<AudioPlayer.Event>(extraBufferCapacity = 1)
    override val events: SharedFlow<AudioPlayer.Event> = mutableEvents.asSharedFlow()

    init {
        initializeMediaController()
    }

    override fun setPlaylist(playlist: Playlist) {
        if (!controllerFuture.isDone) error("MediaController is not initialized yet")

        player.clearMediaItems()
        playlist.tracks.forEach { track ->
            player.addMediaItem(
                MediaItem.Builder()
                    .setMediaId(track.id)
                    .setUri(track.uri)
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setTitle(track.title)
                            .setArtist(track.artist)
                            .setAlbumTitle(track.album.title)
                            .setArtworkUri(Uri.parse(track.album.art))
                            .build()
                    )
                    .build()
            )
        }
        player.prepare()
    }

    override fun play() {
        player.play()
    }

    override fun pause() {
        player.pause()
    }

    override fun toggleRepeatMode() {
        if (player.isCommandAvailable(Player.COMMAND_SET_REPEAT_MODE)) {
            player.repeatMode = RepeatModeUtil.getNextRepeatMode(
                /* currentMode = */ player.repeatMode,
                /* enabledModes = */ RepeatModeUtil.REPEAT_TOGGLE_MODE_ONE or RepeatModeUtil.REPEAT_TOGGLE_MODE_ALL
            )
        }
    }

    override fun release() {
        releaseMediaController()
    }

    /**
     * Attaches the media session from the service to the media controller,
     * so that the media controller can be used to control the media session.
     */
    private fun initializeMediaController() {
        val sessionToken = SessionToken(context, ComponentName(context, MusicService::class.java))
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture.addListener(
            {
                controllerFuture.get().apply {
                    addListener(MediaControllerListener(context, this, mutableEvents))
                    onMediaControllerInitialized()
                }
            },
            MoreExecutors.directExecutor()
        )
    }

    private fun onMediaControllerInitialized() {
        mutableEvents.tryEmit(AudioPlayer.Event.PlayerInitialized)
    }

    private fun releaseMediaController() {
        MediaController.releaseFuture(controllerFuture)
    }
}
