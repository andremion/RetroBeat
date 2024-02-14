package io.github.andremion.musicplayer.data.player

import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.Player.RepeatMode
import androidx.media3.common.Player.State
import androidx.media3.common.Timeline
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.session.MediaController
import io.github.andremion.musicplayer.domain.AudioPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import java.util.Formatter
import java.util.Locale

internal class MediaControllerListener(
    private val mediaController: MediaController,
    private val mutableState: MutableStateFlow<AudioPlayer.State>,
    private val mutableTrack: MutableStateFlow<AudioPlayer.Track?>
) : Player.Listener {

    private val formatBuilder = StringBuilder()
    private val formatter = Formatter(formatBuilder, Locale.getDefault())

    override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
        updateCurrentTrack()
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        updateIsPlaying(isPlaying)
        updateProgress()
    }

    override fun onPlaybackStateChanged(@State playbackState: Int) {
        updateProgress()
    }

    override fun onTimelineChanged(timeline: Timeline, @Player.TimelineChangeReason reason: Int) {
        updateTimeline()
    }

    override fun onRepeatModeChanged(@RepeatMode repeatMode: Int) {
        updateRepeatModeButton()
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
                mutableTrack.update { track ->
                    updateTrack(track) ?: createTrack()
                }
            }
        } else {
            println("COMMAND_GET_CURRENT_MEDIA_ITEM or COMMAND_GET_METADATA is not available")
        }
    }

    private fun MediaItem.updateTrack(track: AudioPlayer.Track?): AudioPlayer.Track? =
        track?.copy(
            id = mediaId,
            uri = localConfiguration?.uri.toString(),
            metadata = track.metadata.copy(
                title = mediaMetadata.title.toString(),
                artist = mediaMetadata.artist.toString(),
                albumTitle = mediaMetadata.albumTitle.toString(),
                artworkUri = mediaMetadata.artworkUri.toString(),
            )
        )

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

    private fun updateIsPlaying(isPlaying: Boolean) {
        if (mediaController.isCommandAvailable(Player.COMMAND_PLAY_PAUSE)) {
            mutableState.update { state ->
                state.copy(isPlaying = isPlaying)
            }
        } else {
            println("COMMAND_PLAY_PAUSE is not available")
        }
    }

    @OptIn(UnstableApi::class)
    fun updateProgress() {
        if (mediaController.isCommandAvailable(Player.COMMAND_GET_CURRENT_MEDIA_ITEM)) {
            mutableState.update { state ->
                state.copy(
                    position = (mediaController.currentPosition / mediaController.duration.toFloat()).coerceIn(0f, 1f),
                    time = Util.getStringForTime(formatBuilder, formatter, mediaController.currentPosition),
                    duration = Util.getStringForTime(formatBuilder, formatter, mediaController.duration)
                )
            }
        } else {
            println("COMMAND_GET_CURRENT_MEDIA_ITEM is not available")
        }
    }

    private fun updateRepeatModeButton() {
        if (mediaController.isCommandAvailable(Player.COMMAND_SET_REPEAT_MODE)) {
            mutableState.update { state ->
                state.copy(repeatMode = map(mediaController.repeatMode))
            }
        } else {
            println("COMMAND_SET_REPEAT_MODE is not available")
        }
    }

    private fun updateTimeline() {
        updateProgress()
    }
}

private fun log(events: Player.Events) {
    (0 until events.size())
        .map { events[it] }
        .joinToString(",")
        .also { println("Player.Events=$it") }
}

private fun map(@RepeatMode repeatMode: Int): AudioPlayer.RepeatMode =
    when (repeatMode) {
        Player.REPEAT_MODE_OFF -> AudioPlayer.RepeatMode.Off
        Player.REPEAT_MODE_ONE -> AudioPlayer.RepeatMode.One
        Player.REPEAT_MODE_ALL -> AudioPlayer.RepeatMode.All
        else -> throw IllegalArgumentException("Unknown repeat mode: $repeatMode")
    }
