package io.github.andremion.musicplayer.domain

interface AudioPlayer {
    fun setMediaUri(uri: String)
    fun addMediaUris(uris: List<String>)
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
