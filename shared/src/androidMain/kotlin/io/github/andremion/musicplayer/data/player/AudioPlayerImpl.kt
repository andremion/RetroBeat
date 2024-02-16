/*
 *    Copyright 2024. André Luiz Oliveira Rêgo
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

    override fun initialize(onInitialized: () -> Unit) {
        // Attaches the media session from the service to the media controller,
        // so that the media controller can be used to control the media session.
        val sessionToken = SessionToken(context, ComponentName(context, MusicService::class.java))
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture.addListener(
            {
                controllerFuture.get().addListener(listener)
                onInitialized()
            },
            MoreExecutors.directExecutor()
        )
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

    override fun releasePlayer() {
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
