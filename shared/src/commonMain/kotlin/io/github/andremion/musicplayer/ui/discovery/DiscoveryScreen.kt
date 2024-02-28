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

@file:OptIn(ExperimentalResourceApi::class)

package io.github.andremion.musicplayer.ui.discovery

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.andremion.musicplayer.domain.entity.Playlist
import io.github.andremion.musicplayer.presentation.discovery.DiscoveryUiEffect
import io.github.andremion.musicplayer.presentation.discovery.DiscoveryUiEvent
import io.github.andremion.musicplayer.presentation.discovery.DiscoveryUiState
import io.github.andremion.musicplayer.presentation.discovery.DiscoveryViewModel
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import io.ktor.utils.io.errors.IOException
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle
import moe.tlaster.precompose.koin.koinViewModel
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource
import retrobeat.shared.generated.resources.Res
import retrobeat.shared.generated.resources.error_retry_button
import retrobeat.shared.generated.resources.generic_error_message
import retrobeat.shared.generated.resources.generic_error_title
import retrobeat.shared.generated.resources.internet_connection_error_message
import retrobeat.shared.generated.resources.internet_connection_error_title

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
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            uiState.playlists
                .onLoading {
                    CircularProgressIndicator()
                }
                .onSuccess { playlists ->
                    LazyVerticalStaggeredGrid(
                        modifier = Modifier.fillMaxSize(),
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
                }.onFailure { cause ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            modifier = Modifier.size(48.dp),
                            imageVector = Icons.Rounded.ErrorOutline,
                            contentDescription = null,
                        )
                        when (cause) {
                            is IOException -> InternetConnectionError()
                            else -> GenericError()
                        }
                        TextButton(
                            onClick = { onUiEvent(DiscoveryUiEvent.RetryClick) }
                        ) {
                            Text(text = stringResource(Res.string.error_retry_button))
                        }
                    }
                }
        }
    }
}

@Composable
private fun InternetConnectionError() {
    Text(
        text = stringResource(Res.string.internet_connection_error_title),
        style = MaterialTheme.typography.bodyLarge
    )
    Text(
        text = stringResource(Res.string.internet_connection_error_message),
        style = MaterialTheme.typography.bodyMedium
    )
}

@Composable
private fun GenericError() {
    Text(
        text = stringResource(Res.string.generic_error_title),
        style = MaterialTheme.typography.bodyLarge
    )
    Text(
        text = stringResource(Res.string.generic_error_message),
        style = MaterialTheme.typography.bodyMedium
    )
}

private val GridColumnSize = 122.dp
