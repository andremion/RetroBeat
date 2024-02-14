package io.github.andremion.musicplayer.presentation.player

sealed interface PlayerUiEvent {
    data object PlayClick : PlayerUiEvent
    data object PauseClick : PlayerUiEvent
    data object SkipToPrevious : PlayerUiEvent
    data object SkipToNext : PlayerUiEvent
    data object SeekBackward : PlayerUiEvent
    data object SeekForward : PlayerUiEvent
    data object RepeatClick : PlayerUiEvent
    data object ShuffleClick : PlayerUiEvent
}
