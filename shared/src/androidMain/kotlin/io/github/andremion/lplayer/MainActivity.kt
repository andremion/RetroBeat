package io.github.andremion.lplayer

import android.content.ComponentName
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.media3.ui.PlayerView
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import io.github.andremion.lplayer.data.service.MusicService

class MainActivity : ComponentActivity() {

    private lateinit var controllerFuture: ListenableFuture<MediaController>
    private lateinit var playerView: PlayerView

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
//        setContent { LPlayer() }
        playerView = PlayerView(this).also(::setContentView)
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
        controllerFuture.addListener(
            {
                controllerFuture.get().apply {
                    playerView.player = this
                    setMediaItem(
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
                    prepare()
                    play()
                }
            },
            MoreExecutors.directExecutor()
        )
    }

    private fun releaseMediaController() {
        MediaController.releaseFuture(controllerFuture)
    }
}
