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

package io.github.andremion.musicplayer.component.player

import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.Player.RepeatMode
import androidx.media3.common.Player.State
import androidx.media3.common.Timeline
import androidx.media3.session.MediaController
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

internal class MediaControllerListener(
    private val mediaController: MediaController,
    private val mutableTrack: MutableStateFlow<AudioPlayer.Track?>,
    private val mutablePlayback: MutableStateFlow<AudioPlayer.Playback>,
) : Player.Listener {

    override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
        updateCurrentTrack()
    }

    override fun onIsLoadingChanged(isLoading: Boolean) {
        updateIsLoading()
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        updateIsPlaying()
        updateProgress()
    }

    override fun onPlaybackStateChanged(@State playbackState: Int) {
        updateHasError()
        updateProgress()
    }

    override fun onTimelineChanged(timeline: Timeline, @Player.TimelineChangeReason reason: Int) {
        updateTimeline()
    }

    override fun onRepeatModeChanged(@RepeatMode repeatMode: Int) {
        updateRepeatMode()
    }

    override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
        updateShuffleMode()
    }

    override fun onEvents(player: Player, events: Player.Events) {
        log(events)
    }

    private fun updateCurrentTrack() {
        if (
            mediaController.isCommandAvailable(Player.COMMAND_GET_CURRENT_MEDIA_ITEM) &&
            mediaController.isCommandAvailable(Player.COMMAND_GET_METADATA)
        ) {
            with(requireNotNull(mediaController.currentMediaItem)) {
                mutableTrack.update { createTrack() }
            }
        } else {
            Napier.w(
                "COMMAND_GET_CURRENT_MEDIA_ITEM or COMMAND_GET_METADATA is not available",
                tag = AudioPlayerImpl.LogTag
            )
        }
    }

    private fun MediaItem.createTrack(): AudioPlayer.Track =
        AudioPlayer.Track(
            id = mediaId,
            uri = localConfiguration?.uri.toString(),
            metadata = AudioPlayer.Track.Metadata(
                title = mediaMetadata.title.toString(),
                artist = mediaMetadata.artist.toString(),
                albumTitle = mediaMetadata.albumTitle.toString(),
                artworkUri = mediaMetadata.artworkUri.toString(),
            )
        )

    private fun updateIsLoading() {
        mutablePlayback.update { playback ->
            playback.copy(
                isLoading = mediaController.isLoading
            )
        }
    }

    private fun updateIsPlaying() {
        if (mediaController.isCommandAvailable(Player.COMMAND_PLAY_PAUSE)) {
            mutablePlayback.update { playback ->
                playback.copy(isPlaying = mediaController.isPlaying)
            }
        } else {
            Napier.w("COMMAND_PLAY_PAUSE is not available", tag = AudioPlayerImpl.LogTag)
        }
    }

    private fun updateHasError() {
        mutablePlayback.update { playback ->
            playback.copy(hasError = mediaController.playerError != null)
        }
    }

    fun updateProgress() {
        if (mediaController.isCommandAvailable(Player.COMMAND_GET_CURRENT_MEDIA_ITEM)) {
            mutablePlayback.update { playback ->
                playback.copy(
                    progress = AudioPlayer.Playback.Progress(
                        position = (mediaController.currentPosition / mediaController.duration.toFloat())
                            .coerceIn(0f, 1f),
                        time = mediaController.currentPosition.milliseconds,
                        // Make sure the state is gonna be emitted even if the state is the same.
                        timestamp = System.currentTimeMillis(),
                    )
                )
            }
        } else {
            Napier.w("COMMAND_GET_CURRENT_MEDIA_ITEM is not available", tag = AudioPlayerImpl.LogTag)
        }
    }

    private fun updateRepeatMode() {
        if (mediaController.isCommandAvailable(Player.COMMAND_SET_REPEAT_MODE)) {
            mutablePlayback.update { playback ->
                playback.copy(repeatMode = map(mediaController.repeatMode))
            }
        } else {
            Napier.w("COMMAND_SET_REPEAT_MODE is not available", tag = AudioPlayerImpl.LogTag)
        }
    }

    private fun updateShuffleMode() {
        if (mediaController.isCommandAvailable(Player.COMMAND_SET_SHUFFLE_MODE)) {
            mutablePlayback.update { playback ->
                playback.copy(isShuffleModeOn = mediaController.shuffleModeEnabled)
            }
        } else {
            Napier.w("COMMAND_SET_SHUFFLE_MODE is not available", tag = AudioPlayerImpl.LogTag)
        }
    }

    private fun updateTimeline() {
        mutablePlayback.update { playback ->
            playback.copy(
                duration = if (mediaController.duration == C.TIME_UNSET) {
                    (-0).seconds
                } else {
                    mediaController.duration.milliseconds
                },
            )
        }
        updateProgress()
    }
}

private fun log(events: Player.Events) {
    (0 until events.size())
        .map { events[it] }
        .joinToString(",")
        .also { Napier.d("Player.Events=$it", tag = AudioPlayerImpl.LogTag) }
}

private fun map(@RepeatMode repeatMode: Int): AudioPlayer.RepeatMode =
    when (repeatMode) {
        Player.REPEAT_MODE_OFF -> AudioPlayer.RepeatMode.Off
        Player.REPEAT_MODE_ONE -> AudioPlayer.RepeatMode.One
        Player.REPEAT_MODE_ALL -> AudioPlayer.RepeatMode.All
        else -> throw IllegalArgumentException("Unknown repeat mode: $repeatMode")
    }
