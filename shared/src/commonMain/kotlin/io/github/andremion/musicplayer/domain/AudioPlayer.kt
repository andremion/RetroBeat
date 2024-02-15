package io.github.andremion.musicplayer.domain

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface AudioPlayer {

    val state: StateFlow<State>
    val currentTrack: StateFlow<Track?>
    val events: SharedFlow<Event>

    fun setTracks(tracks: List<Track>)
    fun play()
    fun updateProgress()
    fun pause()
    fun skipToPrevious()
    fun skipToNext()
    fun seekBackward()
    fun seekForward()
    fun toggleRepeatMode()
    fun toggleShuffleMode()
    fun releasePlayer()

    data class State(
        val isPlaying: Boolean = false,
        val position: Float = -0f,
        val time: String = "",
        val duration: String = "",
        val repeatMode: RepeatMode = RepeatMode.Off,
        val isShuffleModeOn: Boolean = false,
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
