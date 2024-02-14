package io.github.andremion.musicplayer.domain

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface AudioPlayer {

    val state: StateFlow<State>
    val events: SharedFlow<Event>
    val currentTrack: StateFlow<Track?>

    fun setTracks(tracks: List<Track>)
    fun play()
    fun updateProgress()
    fun pause()
    fun toggleRepeatMode()
    fun release()

    data class State(
        val isPlaying: Boolean = false,
        val position: Float = -0f,
        val time: String = "",
        val duration: String = "",
        val repeatMode: RepeatMode = RepeatMode.Off,
    )

    data class Track(
        val id: String,
        val uri: String,
        val metadata: Metadata,
    ) {
        data class Metadata(
            val title: String,
            val artist: String,
            val albumTitle: String,
            val artworkUri: String,
        )
    }

    sealed interface Event {
        data object PlayerInitialized : Event
    }

    enum class RepeatMode { Off, One, All }
}
