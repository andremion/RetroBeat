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

package io.github.andremion.musicplayer.presentation.discovery

import io.github.andremion.musicplayer.domain.MusicRepository
import io.github.andremion.musicplayer.presentation.AsyncContent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope

class DiscoveryViewModel(
    musicRepository: MusicRepository,
) : ViewModel() {

    private val retry = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    private val playlists = musicRepository.getPlaylists()
        .map(AsyncContent.Companion::success)
        .retryWhen { cause, _ ->
            emit(AsyncContent.failure(cause))
            retry.first()
            emit(AsyncContent.loading())
            true
        }

    val uiState: StateFlow<DiscoveryUiState> = combine(playlists) { (playlists) ->
        DiscoveryUiState(playlists)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DiscoveryUiState()
    )

    private val mutableUiEffect = MutableSharedFlow<DiscoveryUiEffect>(extraBufferCapacity = 1)
    val uiEffect: SharedFlow<DiscoveryUiEffect> = mutableUiEffect
        .shareIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
        )

    fun onUiEvent(event: DiscoveryUiEvent) {
        when (event) {
            is DiscoveryUiEvent.PlaylistClick -> {
                mutableUiEffect.tryEmit(DiscoveryUiEffect.NavigateToPlayer(event.playlistId))
            }

            DiscoveryUiEvent.RetryClick -> retry.tryEmit(Unit)
        }
    }
}
