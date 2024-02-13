package io.github.andremion.musicplayer.data.player

import androidx.annotation.OptIn
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.session.MediaController
import io.github.andremion.musicplayer.domain.AudioPlayer
import kotlinx.coroutines.flow.MutableSharedFlow
import java.util.Formatter
import java.util.Locale

class MediaControllerListener(
    private val mediaController: MediaController,
    private val mutableEvents: MutableSharedFlow<AudioPlayer.Event>
) : Player.Listener {

    private val formatBuilder = StringBuilder()
    private val formatter = Formatter(formatBuilder, Locale.getDefault())

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        mutableEvents.tryEmit(
            AudioPlayer.Event.IsPlayingChanged(isPlaying)
        )
    }

    @OptIn(UnstableApi::class)
    override fun onEvents(player: Player, events: Player.Events) {
        log(events)
        if (events.containsAny(
                Player.EVENT_PLAYBACK_STATE_CHANGED,
                Player.EVENT_PLAY_WHEN_READY_CHANGED,
                Player.EVENT_AVAILABLE_COMMANDS_CHANGED
            )
        ) {
//            updatePlayPauseButton()
        }
        if (events.containsAny(
                Player.EVENT_PLAYBACK_STATE_CHANGED,
                Player.EVENT_PLAY_WHEN_READY_CHANGED,
                Player.EVENT_AVAILABLE_COMMANDS_CHANGED,
                Player.EVENT_IS_PLAYING_CHANGED
            )
        ) {
            updateProgress()
        }
        if (events.containsAny(
                Player.EVENT_REPEAT_MODE_CHANGED,
                Player.EVENT_AVAILABLE_COMMANDS_CHANGED
            )
        ) {
            updateRepeatModeButton()
        }
        if (events.containsAny(
                Player.EVENT_SHUFFLE_MODE_ENABLED_CHANGED,
                Player.EVENT_AVAILABLE_COMMANDS_CHANGED
            )
        ) {
            updateShuffleButton()
        }
        if (events.containsAny(
                Player.EVENT_REPEAT_MODE_CHANGED,
                Player.EVENT_SHUFFLE_MODE_ENABLED_CHANGED,
                Player.EVENT_POSITION_DISCONTINUITY,
                Player.EVENT_TIMELINE_CHANGED,
                Player.EVENT_SEEK_BACK_INCREMENT_CHANGED,
                Player.EVENT_SEEK_FORWARD_INCREMENT_CHANGED,
                Player.EVENT_AVAILABLE_COMMANDS_CHANGED
            )
        ) {
            updateNavigation()
        }
        if (events.containsAny(
                Player.EVENT_POSITION_DISCONTINUITY,
                Player.EVENT_TIMELINE_CHANGED,
                Player.EVENT_AVAILABLE_COMMANDS_CHANGED
            )
        ) {
            updateTimeline()
        }
        if (events.containsAny(
                Player.EVENT_PLAYBACK_PARAMETERS_CHANGED,
                Player.EVENT_AVAILABLE_COMMANDS_CHANGED
            )
        ) {
            updatePlaybackSpeedList()
        }
        if (events.containsAny(
                Player.EVENT_TRACKS_CHANGED,
                Player.EVENT_AVAILABLE_COMMANDS_CHANGED
            )
        ) {
            updateTrackLists()
        }
    }

    private fun log(events: Player.Events) {
        (0 until events.size())
            .map { events[it] }
            .joinToString(",")
            .also { println("Player.Events=$it") }
    }

    private fun updatePlayPauseButton() {
        // Returns whether a play button should be presented on a UI element for playback control.
        // If false, a pause button should be shown instead.
        val shouldShowPlayButton = Util.shouldShowPlayButton(mediaController)
        println("shouldShowPlayButton=$shouldShowPlayButton")
        mutableEvents.tryEmit(
            AudioPlayer.Event.IsPlayingChanged(isPlaying = !shouldShowPlayButton)
        )
    }

    @OptIn(UnstableApi::class)
    private fun updateProgress() {
        if (mediaController.isCommandAvailable(Player.COMMAND_GET_CURRENT_MEDIA_ITEM)) {
            mutableEvents.tryEmit(
                AudioPlayer.Event.ProgressChanged(
                    time = Util.getStringForTime(
                        formatBuilder,
                        formatter,
                        mediaController.currentPosition
                    ),
                    duration = Util.getStringForTime(
                        formatBuilder,
                        formatter,
                        mediaController.duration
                    ),
                    position = (mediaController.currentPosition / mediaController.duration.toFloat()).coerceIn(0f, 1f)
                )
            )
        } else {
            println("COMMAND_GET_CURRENT_MEDIA_ITEM is not available")
        }
    }

    private fun updateRepeatModeButton() {
        if (mediaController.isCommandAvailable(Player.COMMAND_SET_REPEAT_MODE)) {
            val repeatMode = mediaController.repeatMode
            mutableEvents.tryEmit(
                AudioPlayer.Event.RepeatModeChanged(
                    when (repeatMode) {
                        Player.REPEAT_MODE_OFF -> AudioPlayer.RepeatMode.Off
                        Player.REPEAT_MODE_ONE -> AudioPlayer.RepeatMode.One
                        Player.REPEAT_MODE_ALL -> AudioPlayer.RepeatMode.All
                        else -> throw IllegalArgumentException("Unknown repeat mode: $repeatMode")
                    }
                )
            )
        } else {
            println("COMMAND_SET_REPEAT_MODE is not available")
        }
    }

    private fun updateShuffleButton() {
        // TODO("Not yet implemented")
    }

    private fun updateNavigation() {
        // TODO("Not yet implemented")
    }

    private fun updateTimeline() {
        updateProgress()
    }

    private fun updatePlaybackSpeedList() {
        // TODO("Not yet implemented")
    }

    private fun updateTrackLists() {
        // TODO("Not yet implemented")
    }
}