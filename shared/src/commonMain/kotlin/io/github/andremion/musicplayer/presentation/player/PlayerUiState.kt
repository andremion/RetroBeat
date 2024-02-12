package io.github.andremion.musicplayer.presentation.player

data class PlayerUiState(
    val isPlaying: Boolean = false,
    val isPlayButtonEnabled: Boolean = true,
    val position: Float = 0f,
    val time: String = "",
    val duration: String = "",
)
