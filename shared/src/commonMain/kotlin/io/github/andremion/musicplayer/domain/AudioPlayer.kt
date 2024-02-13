package io.github.andremion.musicplayer.domain

import kotlinx.coroutines.flow.SharedFlow

interface AudioPlayer {

    val position: Float
    val time: String
    val duration: String
    val repeatMode: Int

    val events: SharedFlow<Event>

    fun play()
    fun pause()
    fun toggleRepeatMode()
    fun release()

    sealed interface Event {
        data class IsPlayingChanged(
            val isPlaying: Boolean
        ) : Event

        data class ProgressChanged(
            val position: Float,
            val time: String,
            val duration: String,
        ) : Event

        data class RepeatModeChanged(
            val mode: RepeatMode
        ) : Event
    }

    enum class RepeatMode { Off, One, All }
}
