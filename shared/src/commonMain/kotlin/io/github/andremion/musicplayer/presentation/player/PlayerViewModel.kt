package io.github.andremion.musicplayer.presentation.player

import io.github.andremion.musicplayer.domain.AudioPlayer
import io.github.andremion.musicplayer.domain.MusicRepository
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
    repository: MusicRepository,
    private val audioPlayer: AudioPlayer
) : ViewModel() {

    private val playlist = repository.getPlaylist().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    private val mutableState = MutableStateFlow(PlayerUiState())
    val uiState = mutableState.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PlayerUiState()
    )

    private val updateProgressJob: Job? = null

    init {
        playlist.launchIn(viewModelScope)

        audioPlayer.events.onEach { event ->
            when (event) {
                AudioPlayer.Event.PlayerInitialized -> {
                    mutableState.update { state ->
                        state.copy(
                            playlist = playlist.value
                        )
                    }
                    playlist.value?.let(audioPlayer::setPlaylist)
                }

                is AudioPlayer.Event.PlaylistChanged -> {
                    mutableState.update { state ->
                        state.copy(
                            currentTrack = event.currentTrack,
                            playlist = state.playlist?.copy(
                                tracks = event.tracks
                            )
                        )
                    }
                }

                is AudioPlayer.Event.IsPlayingChanged -> {
                    mutableState.update { state ->
                        state.copy(
                            player = if (event.isPlaying) {
                                PlayerUiState.Player.Playing
                            } else {
                                PlayerUiState.Player.Pausing
                            }
                        )
                    }
                }

                is AudioPlayer.Event.ProgressChanged -> {
                    updateProgress(event.position, event.time, event.duration)
                }

                is AudioPlayer.Event.RepeatModeChanged -> {
                    mutableState.update { state ->
                        state.copy(
                            repeatMode = event.mode
                        )
                    }
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
        if (uiState.value.player == PlayerUiState.Player.Playing) {
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

            is PlayerUiEvent.CoverRotationEnd -> {
                mutableState.update { state ->
                    state.copy(
                        player = PlayerUiState.Player.Paused,
                    )
                }
            }

            PlayerUiEvent.RepeatClick -> {
                audioPlayer.toggleRepeatMode()
            }
        }
    }

    override fun onCleared() {
        updateProgressJob?.cancel()
        audioPlayer.release()
    }
}
