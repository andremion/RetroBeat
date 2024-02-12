package io.github.andremion.musicplayer.presentation.player

import io.github.andremion.musicplayer.domain.AudioPlayer
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope

class PlayerViewModel(
    private val audioPlayer: AudioPlayer
) : ViewModel() {

    private val mutableState = MutableStateFlow(PlayerUiState())
    val uiState = mutableState.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PlayerUiState()
    )

    private val updateProgressJob: Job? = null

    init {
        audioPlayer.events.onEach { event ->
            when (event) {
                is AudioPlayer.Event.IsPlayingChanged -> {
                    mutableState.update { state ->
                        state.copy(
                            isPlaying = event.isPlaying
                        )
                    }
                }

                is AudioPlayer.Event.ProgressChanged -> {
                    updateProgress(event.position, event.time, event.duration)
                }
            }
        }.launchIn(viewModelScope)
    }

    private fun updateProgress(position: Float, time: String, duration: String) {
        mutableState.update { state ->
            state.copy(
                position = position,
                time = time,
                duration = duration,
            )
        }
        updateProgressJob?.cancel()
        if (uiState.value.isPlaying) {
            viewModelScope.launch {
                delay(1000)
                updateProgress(audioPlayer.position, audioPlayer.time, audioPlayer.duration)
            }
        }
    }

    fun onUiEvent(event: PlayerUiEvent) {
        when (event) {
            is PlayerUiEvent.PlayClick -> {
                audioPlayer.play()
            }

            is PlayerUiEvent.PauseClick -> {
                audioPlayer.pause()
            }
        }
    }

    override fun onCleared() {
        updateProgressJob?.cancel()
        audioPlayer.release()
    }
}
