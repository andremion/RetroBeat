package io.github.andremion.musicplayer.domain

import io.github.andremion.musicplayer.domain.entity.Music
import io.github.andremion.musicplayer.domain.entity.Playlist
import kotlinx.coroutines.flow.SharedFlow

interface AudioPlayer {

    val position: Float
    val time: String
    val duration: String

    val events: SharedFlow<Event>

    fun setPlaylist(playlist: Playlist)
    fun play()
    fun pause()
    fun toggleRepeatMode()
    fun release()

    sealed interface Event {

        data object PlayerInitialized : Event

        data class PlaylistChanged(
            val currentTrack: Music,
            val tracks: List<Music>
        ) : Event

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
