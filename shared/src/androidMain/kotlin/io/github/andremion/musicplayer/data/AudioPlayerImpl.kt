package io.github.andremion.musicplayer.data

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import io.github.andremion.musicplayer.domain.AudioPlayer

internal class AudioPlayerImpl(context: Context) : AudioPlayer {

    private val player = ExoPlayer.Builder(context).build()

    override fun setMediaUri(uri: String) {
        player.setMediaItem(MediaItem.fromUri(uri))
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
