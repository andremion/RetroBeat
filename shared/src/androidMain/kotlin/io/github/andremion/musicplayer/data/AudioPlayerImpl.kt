package io.github.andremion.musicplayer.data

import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import io.github.andremion.musicplayer.domain.AudioPlayer

internal class AudioPlayerImpl(private val player: ExoPlayer) : AudioPlayer {

    override fun setMediaUri(uri: String) {
        player.setMediaItem(MediaItem.fromUri(uri))
        player.prepare()
    }

    /**
     * You shouldn't perform these steps before the app is in the foreground.
     * If your player is in an Activity or Fragment,
     * this means preparing the player in the onStart() lifecycle method on API level 24 and higher
     * or the onResume() lifecycle method on API level 23 and below.
     * For a player that's in a Service, you can prepare it in onCreate().
     */
    override fun addMediaUris(uris: List<String>) {
        uris.forEach { uri ->
            player.addMediaItem(MediaItem.fromUri(uri))
        }
        player.prepare()
    }

    override fun play() {
        player.play()
    }

    override fun pause() {
        player.pause()
    }

    override fun stop() {
        player.stop()
    }

    override fun seekTo(position: Long) {
        player.seekTo(position)
    }

    override fun release() {
        player.release()
    }

    override fun addListener(listener: AudioPlayer.Listener) {
        TODO("Not yet implemented")
    }

    override fun removeListener(listener: AudioPlayer.Listener) {
        TODO("Not yet implemented")
    }
}
