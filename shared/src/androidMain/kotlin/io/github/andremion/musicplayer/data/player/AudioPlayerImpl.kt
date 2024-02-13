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
    override val repeatMode: Int
        get() = player.repeatMode

    private val mutableEvents = MutableSharedFlow<AudioPlayer.Event>(extraBufferCapacity = 1)
    override val events: SharedFlow<AudioPlayer.Event> = mutableEvents.asSharedFlow()

    init {
        initializeMediaController()
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
                    addListener(MediaControllerListener(this, mutableEvents))
                    onMediaControllerInitialized()
                }
            },
            MoreExecutors.directExecutor()
        )
    }

    /**
     * You shouldn't perform these steps before the app is in the foreground.
     * If your player is in an Activity or Fragment,
     * this means preparing the player in the onStart() lifecycle method on API level 24 and higher
     * or the onResume() lifecycle method on API level 23 and below.
     * For a player that's in a Service, you can prepare it in onCreate().
     */
    private fun onMediaControllerInitialized() {
        player.setMediaItem(
            MediaItem.Builder()
                .setMediaId("1644464022")
                .setUri("https://cdns-preview-d.dzcdn.net/stream/c-ddf3ecfe031b0e38be1f7cef597d6af1-7.mp3")
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle("Calm Down")
                        .setArtist("Rema")
                        .setAlbumTitle("Calm Down")
                        .setArtworkUri(Uri.parse("https://e-cdns-images.dzcdn.net/images/cover/3071378af24d789b8fc69e95162041e4/250x250-000000-80-0-0.jpg"))
                        .build()
                )
                .build()
        )
        player.prepare()
        player.play()
    }

    private fun releaseMediaController() {
        MediaController.releaseFuture(controllerFuture)
    }
}
