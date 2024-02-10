package io.github.andremion.lplayer.domain

interface AudioPlayer {
    fun setMediaUri(uri: String)
    fun play()
    fun pause()
    fun stop()
    fun seekTo(position: Long)
    fun release()
    fun addListener(listener: Listener)
    fun removeListener(listener: Listener)

    interface Listener {
        fun onPlay()
        fun onPause()
        fun onStop()
        fun onSeekTo(position: Long)
        fun onRelease()
    }
}
