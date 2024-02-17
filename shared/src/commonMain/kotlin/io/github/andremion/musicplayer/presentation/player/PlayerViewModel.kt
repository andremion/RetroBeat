/*
 *    Copyright 2024. André Luiz Oliveira Rêgo
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package io.github.andremion.musicplayer.presentation.player

import io.github.andremion.musicplayer.component.player.AudioPlayer
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
