package io.github.andremion.musicplayer.presentation.player

import io.github.andremion.musicplayer.domain.AudioPlayer
import io.github.andremion.musicplayer.domain.entity.Playlist

data class PlayerUiState(
    val playlist: Playlist? = null,
    val playerState: AudioPlayer.State = AudioPlayer.State(),
    val currentTrack: AudioPlayer.Track? = null,
)
