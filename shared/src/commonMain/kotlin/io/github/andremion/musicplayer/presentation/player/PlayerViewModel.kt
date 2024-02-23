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
import io.github.andremion.musicplayer.presentation.AsyncContent
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope

class PlayerViewModel(
    playlistId: String,
    repository: MusicRepository,
    private val audioPlayer: AudioPlayer
) : ViewModel() {

    private var updateProgressJob: Job? = null

    private val playlist = repository.getPlaylist(playlistId)
        .onEach(::initializePlayer)
        .map(AsyncContent.Companion::success)

    private val currentTrack = audioPlayer.currentTrack

    private val playerState = audioPlayer.state.onEach { state ->
        updateProgressJob?.cancel()
        if (state.isPlaying) {
            requestDelayedProgressUpdate()
        }
    }

    val uiState = combine(
        playlist,
        currentTrack,
        playerState,
    ) { playlist, currentTrack, playerState ->
        PlayerUiState(
            seekBackIncrement = audioPlayer.seekBackIncrementInSeconds.toString(),
            seekForwardIncrement = audioPlayer.seekForwardIncrementInSeconds.toString(),
            playlist = playlist,
            currentTrack = currentTrack,
            playerState = playerState,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PlayerUiState()
    )

    fun onUiEvent(event: PlayerUiEvent) {
        when (event) {
            PlayerUiEvent.PlayPauseClick -> {
                audioPlayer.playPause()
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

            is PlayerUiEvent.MusicClick -> {
                audioPlayer.play(trackIndex = event.musicIndex)
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
            delay(50)
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
                artworkUri = music.album.picture.big
            )
        )
    }
