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

package io.github.andremion.musicplayer.ui.discovery

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.andremion.musicplayer.domain.entity.Playlist
import io.github.andremion.musicplayer.presentation.discovery.DiscoveryUiEffect
import io.github.andremion.musicplayer.presentation.discovery.DiscoveryUiEvent
import io.github.andremion.musicplayer.presentation.discovery.DiscoveryUiState
import io.github.andremion.musicplayer.presentation.discovery.DiscoveryViewModel
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle
import moe.tlaster.precompose.koin.koinViewModel

@Composable
fun DiscoveryScreen(
    onNavigateToPlayer: (playlistId: String) -> Unit
) {
    val viewModel = koinViewModel(DiscoveryViewModel::class)

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ScreenContent(uiState, viewModel::onUiEvent)

    LaunchedEffect(viewModel) {
        viewModel.uiEffect.onEach { effect ->
            when (effect) {
                is DiscoveryUiEffect.NavigateToPlayer -> {
                    onNavigateToPlayer(effect.playlistId)
                }
            }
        }.launchIn(this)
    }
}

@Composable
private fun ScreenContent(
    uiState: DiscoveryUiState,
    onUiEvent: (DiscoveryUiEvent) -> Unit
) {
    Scaffold { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            uiState.playlists
                .onLoading {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Center)
                    )
                }
                .onSuccess { playlists ->
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Adaptive(GridColumnSize),
                        verticalItemSpacing = 8.dp,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(16.dp),
                    ) {
                        items(playlists, key = Playlist::id) { playlist ->
                            Card(
                                modifier = Modifier.clickable {
                                    onUiEvent(DiscoveryUiEvent.PlaylistClick(playlist.id))
                                }
                            ) {
                                Column {
                                    KamelImage(
                                        modifier = Modifier.aspectRatio(1f),
                                        resource = asyncPainterResource(playlist.picture.big),
                                        contentDescription = null
                                    )
                                    Text(
                                        modifier = Modifier.padding(8.dp),
                                        text = playlist.title,
                                    )
                                }
                            }
                        }
                    }
                }
        }
    }
}

private val GridColumnSize = 122.dp
