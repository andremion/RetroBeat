package io.github.andremion.musicplayer

import android.content.ComponentName
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.media3.ui.PlayerView
import com.google.common.util.concurrent.ListenableFuture
import io.github.andremion.musicplayer.data.service.MusicService
import java.util.Formatter
import java.util.Locale

class MainActivity : ComponentActivity() {

    private lateinit var controllerFuture: ListenableFuture<MediaController>
    private lateinit var playerView: PlayerView

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent { MusicPlayer() }
//        playerView = PlayerView(this).also(::setContentView)
    }

    override fun onStart() {
        super.onStart()
        initializeMediaController()
    }

    override fun onStop() {
        releaseMediaController()
        super.onStop()
    }

    /** Attaches the media session from the service to the media controller,
     * so that the media controller can be used by the UI to control the media session.
     */
    private fun initializeMediaController() {
        val sessionToken = SessionToken(this, ComponentName(this, MusicService::class.java))
        controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()
//        controllerFuture.addListener(
//            {
//                controllerFuture.get().apply {
//                    println("controllerFuture.get()=${controllerFuture.get()}")
////                    addListener(playerListener)
//                    playerView.player = this
//                    setMediaItem(
//                        MediaItem.Builder()
//                            .setMediaId("1644464022")
//                            .setUri("https://cdns-preview-d.dzcdn.net/stream/c-ddf3ecfe031b0e38be1f7cef597d6af1-7.mp3")
//                            .setMediaMetadata(
//                                MediaMetadata.Builder()
//                                    .setTitle("Calm Down")
//                                    .setArtist("Rema")
//                                    .setAlbumTitle("Calm Down")
//                                    .setArtworkUri(Uri.parse("https://e-cdns-images.dzcdn.net/images/cover/3071378af24d789b8fc69e95162041e4/250x250-000000-80-0-0.jpg"))
//                                    .build()
//                            )
//                            .build()
//                    )
//                    prepare()
//                    play()
//                }
//            },
//            MoreExecutors.directExecutor()
//        )
    }

    private fun releaseMediaController() {
        MediaController.releaseFuture(controllerFuture)
    }

    private val playerListener = object : Player.Listener {

        private lateinit var player: Player
        private val formatBuilder = StringBuilder()
        private val formatter = Formatter(formatBuilder, Locale.getDefault())

        @OptIn(UnstableApi::class)
        override fun onEvents(player: Player, events: Player.Events) {
            this.player = player
            log(events)
            if (events.containsAny(
                    Player.EVENT_PLAYBACK_STATE_CHANGED,
                    Player.EVENT_PLAY_WHEN_READY_CHANGED,
                    Player.EVENT_AVAILABLE_COMMANDS_CHANGED
                )
            ) {
                updatePlayPauseButton()
            }
            if (events.containsAny(
                    Player.EVENT_PLAYBACK_STATE_CHANGED,
                    Player.EVENT_PLAY_WHEN_READY_CHANGED,
                    Player.EVENT_IS_PLAYING_CHANGED,
                    Player.EVENT_AVAILABLE_COMMANDS_CHANGED
                )
            ) {
                updateProgress()
            }
            if (events.containsAny(Player.EVENT_REPEAT_MODE_CHANGED, Player.EVENT_AVAILABLE_COMMANDS_CHANGED)) {
                updateRepeatModeButton()
            }
            if (events.containsAny(
                    Player.EVENT_SHUFFLE_MODE_ENABLED_CHANGED, Player.EVENT_AVAILABLE_COMMANDS_CHANGED
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
            if (events.containsAny(Player.EVENT_PLAYBACK_PARAMETERS_CHANGED, Player.EVENT_AVAILABLE_COMMANDS_CHANGED)) {
                updatePlaybackSpeedList()
            }
            if (events.containsAny(Player.EVENT_TRACKS_CHANGED, Player.EVENT_AVAILABLE_COMMANDS_CHANGED)) {
                updateTrackLists()
            }
        }

        override fun onTimelineChanged(timeline: Timeline, reason: Int) {
            // println("onTimelineChanged(${timeline}, ${reason})")
        }

        private fun log(events: Player.Events) {
            val eventString = buildString {
                for (i in 0 until events.size()) {
                    append(events[i].toString())
                }
            }
            println("events=$eventString")
        }

        private fun updatePlayPauseButton() {
            println("shouldShowPlayButton=${Util.shouldShowPlayButton(player)}")
        }

        @OptIn(UnstableApi::class)
        private fun updateProgress() {
            if (player.isCommandAvailable(Player.COMMAND_GET_CURRENT_MEDIA_ITEM)) {
                println(
                    "currentPosition=${
                        Util.getStringForTime(
                            formatBuilder,
                            formatter,
                            player.currentPosition
                        )
                    }, duration=${
                        Util.getStringForTime(
                            formatBuilder,
                            formatter,
                            player.duration
                        )
                    }"
                )
            } else {
                println("COMMAND_GET_CURRENT_MEDIA_ITEM is not available")
            }
        }

        private fun updateRepeatModeButton() {
            // TODO("Not yet implemented")
        }

        private fun updateShuffleButton() {
            // TODO("Not yet implemented")
        }

        private fun updateNavigation() {
            // TODO("Not yet implemented")
        }

        private fun updateTimeline() {
            // TODO("Not yet implemented")
        }

        private fun updatePlaybackSpeedList() {
            // TODO("Not yet implemented")
        }

        private fun updateTrackLists() {
            // TODO("Not yet implemented")
        }
    }
}
