package io.github.andremion.musicplayer.domain

import kotlinx.coroutines.flow.SharedFlow

interface AudioPlayer {

    val position: Float
    val time: String
    val duration: String

    val events: SharedFlow<Event>

    fun play()
    fun pause()
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
    }
}
