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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

internal class AudioPlayerImpl(
    private val context: Context
) : AudioPlayer {

    private lateinit var controllerFuture: ListenableFuture<MediaController>
    private val listener: MediaControllerListener by lazy {
        MediaControllerListener(controllerFuture.get(), mutableState, mutableTrack)
    }
    private val player: Player
        get() = controllerFuture.get()

    private val mutableState = MutableStateFlow(AudioPlayer.State())
    override val state: StateFlow<AudioPlayer.State> = mutableState.asStateFlow()

    private val mutableTrack = MutableStateFlow<AudioPlayer.Track?>(null)
    override val currentTrack: StateFlow<AudioPlayer.Track?> = mutableTrack.asStateFlow()

    private val mutableEvents = MutableSharedFlow<AudioPlayer.Event>(extraBufferCapacity = 1)
    override val events: SharedFlow<AudioPlayer.Event> = mutableEvents.asSharedFlow()

    init {
        initializeMediaController()
    }

    override fun setTracks(tracks: List<AudioPlayer.Track>) {
        if (!controllerFuture.isDone) error("MediaController is not initialized yet")

        player.clearMediaItems()
        player.addMediaItems(tracks.map(AudioPlayer.Track::toMediaItem))
        player.prepare()
    }

    override fun play() {
        Util.handlePlayButtonAction(player)
    }

    override fun updateProgress() {
        listener.updateProgress()
    }

    override fun pause() {
        Util.handlePauseButtonAction(player)
    }

    override fun skipToPrevious() {
        if (player.isCommandAvailable(Player.COMMAND_SEEK_TO_PREVIOUS)) {
            player.seekToPrevious()
        }
    }

    override fun skipToNext() {
        if (player.isCommandAvailable(Player.COMMAND_SEEK_TO_NEXT)) {
            player.seekToNext()
        }
    }

    override fun seekBackward() {
        if (player.playbackState != Player.STATE_ENDED && player.isCommandAvailable(Player.COMMAND_SEEK_BACK)) {
            player.seekBack()
        }
    }

    override fun seekForward() {
        if (player.playbackState != Player.STATE_ENDED && player.isCommandAvailable(Player.COMMAND_SEEK_FORWARD)) {
            player.seekForward()
        }
    }

    @OptIn(UnstableApi::class)
    override fun toggleRepeatMode() {
        if (player.isCommandAvailable(Player.COMMAND_SET_REPEAT_MODE)) {
            player.repeatMode = RepeatModeUtil.getNextRepeatMode(
                /* currentMode = */ player.repeatMode,
                /* enabledModes = */ RepeatModeUtil.REPEAT_TOGGLE_MODE_ONE or RepeatModeUtil.REPEAT_TOGGLE_MODE_ALL
            )
        } else {
            println("COMMAND_SET_REPEAT_MODE is not available")
        }
    }

    override fun toggleShuffleMode() {
        if (player.isCommandAvailable(Player.COMMAND_SET_SHUFFLE_MODE)) {
            player.shuffleModeEnabled = !player.shuffleModeEnabled
        } else {
            println("COMMAND_SET_SHUFFLE_MODE is not available")
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
                controllerFuture.get().addListener(listener)
                onMediaControllerInitialized()
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

private fun AudioPlayer.Track.toMediaItem(): MediaItem =
    MediaItem.Builder()
        .setMediaId(id)
        .setUri(uri)
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(metadata.title)
                .setArtist(metadata.artist)
                .setAlbumTitle(metadata.albumTitle)
                .setArtworkUri(Uri.parse(metadata.artworkUri))
                .build()
        )
        .build()
