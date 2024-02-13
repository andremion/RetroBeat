package io.github.andremion.musicplayer.presentation.player

import io.github.andremion.musicplayer.domain.AudioPlayer
import io.github.andremion.musicplayer.domain.entity.Music
import io.github.andremion.musicplayer.domain.entity.Playlist

data class PlayerUiState(
    val player: Player = Player.Idle,
    val position: Float = 0f,
    val time: String = "",
    val duration: String = "",
    val currentTrack: Music? = null,
    val playlist: Playlist? = null,
    val repeatMode: AudioPlayer.RepeatMode = AudioPlayer.RepeatMode.Off
) {

    enum class Player {
        Idle, Playing, Pausing, Paused;

        val isPlaying: Boolean
            get() = this == Playing || this == Pausing
    }
}
