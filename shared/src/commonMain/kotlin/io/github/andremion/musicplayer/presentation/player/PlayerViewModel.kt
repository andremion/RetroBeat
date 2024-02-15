package io.github.andremion.musicplayer.presentation.player

import io.github.andremion.musicplayer.domain.AudioPlayer
import io.github.andremion.musicplayer.domain.MusicRepository
import io.github.andremion.musicplayer.domain.entity.Music
import io.github.andremion.musicplayer.domain.entity.Playlist
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
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

    private val playlist = repository.getPlaylist().onEach { playlist ->
        initializePlayer(playlist)
    }

    private val playerState = audioPlayer.state.onEach { state ->
        updateProgressJob?.cancel()
        if (state.isPlaying) {
            requestDelayedProgressUpdate()
        }
    }

    private val currentTrack = audioPlayer.currentTrack

    val uiState = combine(
        playlist,
        playerState,
        currentTrack,
    ) { playlist, playerState, currentTrack ->
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

    fun onUiEvent(event: PlayerUiEvent) {
        when (event) {
            is PlayerUiEvent.PlayClick -> {
                audioPlayer.play()
            }

            is PlayerUiEvent.PauseClick -> {
                audioPlayer.pause()
            }

            PlayerUiEvent.SkipToPrevious -> {
                audioPlayer.skipToPrevious()
            }

            PlayerUiEvent.SkipToNext -> {
                audioPlayer.skipToNext()
            }

            PlayerUiEvent.SeekBackward -> {
                audioPlayer.seekBackward()
            }

            PlayerUiEvent.SeekForward -> {
                audioPlayer.seekForward()
            }

            PlayerUiEvent.RepeatClick -> {
                audioPlayer.toggleRepeatMode()
            }

            PlayerUiEvent.ShuffleClick -> {
                audioPlayer.toggleShuffleMode()
            }
        }
    }

    override fun onCleared() {
        updateProgressJob?.cancel()
        audioPlayer.releasePlayer()
    }

    private fun initializePlayer(playlist: Playlist) {
        audioPlayer.initialize {
            audioPlayer.setTracks(playlist.musics.toTracks())
        }
    }

    private fun requestDelayedProgressUpdate() {
        updateProgressJob = viewModelScope.launch {
            delay(1000)
            audioPlayer.updateProgress()
        }
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
