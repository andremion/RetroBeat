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

package io.github.andremion.musicplayer.ui.player

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.alexzhirkevich.compottie.LottieAnimation
import io.github.andremion.musicplayer.component.player.AudioPlayer
import io.github.andremion.musicplayer.component.time.format
import io.github.andremion.musicplayer.presentation.player.PlayerUiEvent
import io.github.andremion.musicplayer.presentation.player.PlayerUiState
import io.github.andremion.musicplayer.presentation.player.PlayerViewModel
import io.github.andremion.musicplayer.ui.animation.Fade
import io.github.andremion.musicplayer.ui.animation.LottieCompositionSpec
import io.github.andremion.musicplayer.ui.animation.SceneRoot
import io.github.andremion.musicplayer.ui.animation.SlideFromBottom
import io.github.andremion.musicplayer.ui.animation.rememberLottieComposition
import io.github.andremion.musicplayer.ui.animation.rememberMovableContent
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle
import moe.tlaster.precompose.koin.koinViewModel
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource
import org.koin.core.parameter.parametersOf
import retrobeat.shared.generated.resources.Res
import retrobeat.shared.generated.resources.player_seek_backwards_content_description
import retrobeat.shared.generated.resources.player_seek_forwards_content_description
import retrobeat.shared.generated.resources.player_skip_to_next_content_description
import retrobeat.shared.generated.resources.player_skip_to_previous_content_description

@Composable
fun PlayerScreen(playlistId: String) {
    val viewModel = koinViewModel(PlayerViewModel::class) {
        parametersOf(playlistId)
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ScreenContent(uiState, viewModel::onUiEvent)
}

private enum class TransitionState {
    Initial, ToPlaying, Playing, ToPaused, Paused;

    val isPlaying: Boolean
        get() = this == Playing || this == ToPlaying || this == ToPaused

    val isTransitioning: Boolean
        get() = this == ToPlaying || this == ToPaused
}

@Composable
private fun ScreenContent(
    uiState: PlayerUiState,
    onUiEvent: (PlayerUiEvent) -> Unit
) {
    SceneRoot {
        Box(
            modifier = Modifier
                .navigationBarsPadding()
                .fillMaxSize(),
        ) {

            var transitionState by remember { mutableStateOf(TransitionState.Initial) }
            LaunchedEffect(uiState.playerState.isPlaying) {
                if (uiState.playerState.isPlaying) {
                    transitionState = TransitionState.ToPlaying
                } else {
                    if (transitionState != TransitionState.Initial) {
                        transitionState = TransitionState.ToPaused
                    }
                }
            }

            val sceneTransition by animateFloatAsState(
                targetValue = if (transitionState.isPlaying) 1f else 0f,
            )

            LaunchedEffect(sceneTransition, transitionState) {
                if (sceneTransition == 1f && transitionState == TransitionState.ToPlaying) {
                    transitionState = TransitionState.Playing
                }
            }

            val trackArtwork = uiState.currentTrack?.metadata?.artworkUri.toString()
            val cover = rememberMovableContent(trackArtwork) { modifier ->
                MusicCover(
                    modifier = modifier.animateBounds(),
                    uri = trackArtwork,
                    transition = sceneTransition,
                    rotate = transitionState == TransitionState.Playing,
                    onRotationEnd = { transitionState = TransitionState.Paused }
                )
            }

            val playPauseButton = rememberMovableContent { modifier ->
                FloatingActionButton(
                    modifier = modifier
                        .size(PlayButtonSize)
                        .animateBounds(),
                    shape = CircleShape,
                    onClick = { onUiEvent(PlayerUiEvent.PlayPauseClick) }
                ) {
                    val composition by rememberLottieComposition(
                        spec = LottieCompositionSpec.AnimationRes(name = "play_pause_animation")
                    )
                    LottieAnimation(
                        modifier = Modifier.size(SmallIconSize),
                        composition = composition,
                        // Negative speed to reverse the animation when pausing.
                        speed = if (transitionState.isPlaying) 1f else -1f,
                    )
                }
            }

            val title = uiState.currentTrack?.metadata?.title.toString()
            val titleText = rememberMovableContent(title) { modifier ->
                Text(
                    modifier = modifier.animateBounds(),
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            val artist = uiState.currentTrack?.metadata?.artist.toString()
            val artistText = rememberMovableContent(artist) { modifier ->
                Text(
                    modifier = modifier.animateBounds(),
                    text = artist,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }

            val time = uiState.playerState.time.format()
            val timeText = rememberMovableContent(time) { modifier ->
                Text(
                    modifier = modifier.animateBounds(),
                    text = time,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            val duration = uiState.playerState.duration.format()
            val durationText = rememberMovableContent(duration) { modifier ->
                Text(
                    modifier = modifier.animateBounds(),
                    text = duration,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            var position by remember { mutableStateOf(0f) }
            LaunchedEffect(uiState.playerState.position) {
                // Only update the position if it is not transitioning.
                // This is to avoid the UI glitching during the transition,
                // because the movable content is gonna re-compose whenever the position changes.
                if (!transitionState.isTransitioning) {
                    position = uiState.playerState.position
                }
            }
            val timeBar = rememberMovableContent(position) { modifier ->
                TimeBar(
                    modifier = modifier.animateBounds(),
                    position = position,
                    transition = sceneTransition
                )
            }

            SlideFromBottom(
                modifier = Modifier
                    .padding(top = CoverHeight)
                    .align(Alignment.Center),
                visible = !transitionState.isPlaying
            ) {
                uiState.playlist
                    .onLoading { CircularProgressIndicator() }
                    .onSuccess { playlist ->
                        Playlist(
                            playlist = playlist,
                            selectedMusicId = uiState.currentTrack?.id,
                            topBarPaddingTop = PlayButtonSize / 2,
                            onMusicClick = { musicIndex ->
                                onUiEvent(PlayerUiEvent.MusicClick(musicIndex))
                            }
                        )
                    }
            }

            Fade(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(top = CoverHeight * 1.3f),
                visible = transitionState.isPlaying,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    IconToggleButton(
                        checked = uiState.playerState.repeatMode != AudioPlayer.RepeatMode.Off,
                        onCheckedChange = { onUiEvent(PlayerUiEvent.RepeatClick) }
                    ) {
                        Icon(
                            modifier = Modifier.size(SmallIconSize),
                            imageVector = when (uiState.playerState.repeatMode) {
                                AudioPlayer.RepeatMode.Off -> Icons.Rounded.Repeat
                                AudioPlayer.RepeatMode.One -> Icons.Rounded.RepeatOne
                                AudioPlayer.RepeatMode.All -> Icons.Rounded.Repeat
                            },
                            contentDescription = when (uiState.playerState.repeatMode) {
                                AudioPlayer.RepeatMode.Off -> "Repeat mode off"
                                AudioPlayer.RepeatMode.One -> "Repeat mode one"
                                AudioPlayer.RepeatMode.All -> "Repeat mode all"
                            },
                        )
                    }
                    IconToggleButton(
                        checked = uiState.playerState.isShuffleModeOn,
                        onCheckedChange = { onUiEvent(PlayerUiEvent.ShuffleClick) },
                    ) {
                        Icon(
                            modifier = Modifier.size(SmallIconSize),
                            imageVector = Icons.Rounded.Shuffle,
                            contentDescription = if (uiState.playerState.isShuffleModeOn) {
                                "Shuffle mode on"
                            } else {
                                "Shuffle mode off"
                            },
                        )
                    }
                }
            }

            SlideFromBottom(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                visible = transitionState.isPlaying,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                ) {
                    IconButton(
                        onClick = { onUiEvent(PlayerUiEvent.SkipToPrevious) }
                    ) {
                        Icon(
                            modifier = Modifier.size(DefaultIconSize),
                            imageVector = Icons.Rounded.SkipPrevious,
                            contentDescription = stringResource(Res.string.player_skip_to_previous_content_description)
                        )
                    }
                    IconButton(
                        onClick = { onUiEvent(PlayerUiEvent.SeekBackward) }
                    ) {
                        Box(
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                modifier = Modifier.size(DefaultIconSize),
                                imageVector = Icons.Rounded.Replay,
                                contentDescription = stringResource(Res.string.player_seek_backwards_content_description)
                            )
                            Text(
                                modifier = Modifier.padding(top = 6.dp),
                                text = uiState.seekBackIncrement,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                    IconButton(
                        onClick = { onUiEvent(PlayerUiEvent.SeekForward) }
                    ) {
                        Box(
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                modifier = Modifier
                                    .size(DefaultIconSize)
                                    .graphicsLayer { rotationY = 180f },
                                imageVector = Icons.Rounded.Replay,
                                contentDescription = stringResource(Res.string.player_seek_forwards_content_description)
                            )
                            Text(
                                modifier = Modifier.padding(top = 6.dp),
                                text = uiState.seekForwardIncrement,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                    IconButton(
                        onClick = { onUiEvent(PlayerUiEvent.SkipToNext) }
                    ) {
                        Icon(
                            modifier = Modifier.size(DefaultIconSize),
                            imageVector = Icons.Rounded.SkipNext,
                            contentDescription = stringResource(Res.string.player_skip_to_next_content_description)
                        )
                    }
                }
            }

            if (transitionState.isPlaying) {
                Box(
                    modifier = Modifier.align(Alignment.Center),
                    contentAlignment = Alignment.Center,
                ) {
                    cover(
                        Modifier
                            .padding(16.dp)
                            .height(CoverHeight)
                            .aspectRatio(1f)
                    )
                    Row(
                        modifier = Modifier.align(Alignment.BottomCenter),
                        horizontalArrangement = Arrangement.spacedBy(CoverHeight / 2),
                    ) {
                        timeText(Modifier)
                        durationText(Modifier)
                    }
                    timeBar(Modifier.matchParentSize())
                    playPauseButton(Modifier)
                }
                Column(
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(16.dp)
                        .fillMaxWidth()
                        .align(Alignment.TopCenter),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    titleText(Modifier)
                    artistText(Modifier)
                }
            } else {
                Box {
                    cover(
                        Modifier
                            .height(CoverHeight)
                            .fillMaxWidth()
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .background(color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                            .padding(
                                horizontal = 16.dp,
                                vertical = 8.dp
                            )
                    ) {
                        titleText(Modifier)
                        artistText(Modifier)
                        Row(
                            modifier = Modifier.padding(end = 16.dp + PlayButtonSize),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            timeText(Modifier)
                            timeBar(Modifier.weight(1f))
                            durationText(Modifier)
                        }
                    }
                    playPauseButton(
                        Modifier
                            .align(Alignment.BottomEnd)
                            .padding(horizontal = 16.dp)
                            .offset(y = (56 / 2f).dp)
                    )
                }
            }
        }
    }
}

private val CoverHeight = 256.dp
private val PlayButtonSize = 64.dp
private val DefaultIconSize = 48.dp
private val SmallIconSize = 32.dp
