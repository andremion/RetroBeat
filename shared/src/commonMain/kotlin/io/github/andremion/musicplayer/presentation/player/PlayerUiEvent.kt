package io.github.andremion.musicplayer.presentation.player

sealed interface PlayerUiEvent {
    data object PlayClick : PlayerUiEvent
    data object PauseClick : PlayerUiEvent
    data object CoverRotationEnd : PlayerUiEvent
    data object RepeatClick : PlayerUiEvent
}
