package io.github.andremion.musicplayer.presentation.player

import io.github.andremion.musicplayer.domain.AudioPlayer
import io.github.andremion.musicplayer.domain.MusicRepository
import io.github.andremion.musicplayer.domain.entity.Music
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope

class PlayerViewModel(
    repository: MusicRepository,
    private val audioPlayer: AudioPlayer
) : ViewModel() {

    private var updateProgressJob: Job? = null

    private val playlist = repository.getPlaylist()

    private val playerState = audioPlayer.state.onEach { state ->
        updateProgressJob?.cancel()
        // If the player is playing, request a new progress update after a delay of 1 second
        if (state.isPlaying) {
            updateProgressJob = viewModelScope.launch {
                delay(1000)
                audioPlayer.updateProgress()
            }
        }
    }

    private val currentTrack = audioPlayer.currentTrack

    val uiState = combine(playlist, playerState, currentTrack) { playlist, playerState, currentTrack ->
        PlayerUiState(
            playlist = playlist,
            playerState = playerState,
            currentTrack = currentTrack,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PlayerUiState()
    )

    init {
        audioPlayer.events.onEach { event ->
            when (event) {
                AudioPlayer.Event.PlayerInitialized -> {
                    uiState.value.playlist?.musics?.toTracks()
                        ?.also(audioPlayer::setTracks)
                }
            }
        }.launchIn(viewModelScope)
    }

    fun onUiEvent(event: PlayerUiEvent) {
        when (event) {
            is PlayerUiEvent.PlayClick -> {
                audioPlayer.play()
            }

            is PlayerUiEvent.PauseClick -> {
                audioPlayer.pause()
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

private fun List<Music>.toTracks(): List<AudioPlayer.Track> =
    map { music ->
        AudioPlayer.Track(
            id = music.id,
            uri = music.uri,
            metadata = AudioPlayer.Track.Metadata(
                title = music.title,
                artist = music.artist,
                albumTitle = music.album.title,
                artworkUri = music.album.art
            )
        )
    }
