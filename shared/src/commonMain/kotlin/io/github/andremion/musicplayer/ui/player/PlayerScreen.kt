package io.github.andremion.musicplayer.ui.player

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.material.icons.rounded.FastRewind
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import io.github.andremion.musicplayer.domain.AudioPlayer
import io.github.andremion.musicplayer.domain.entity.Music
import io.github.andremion.musicplayer.domain.entity.Playlist
import io.github.andremion.musicplayer.presentation.player.PlayerUiEvent
import io.github.andremion.musicplayer.presentation.player.PlayerUiState
import io.github.andremion.musicplayer.presentation.player.PlayerViewModel
import io.github.andremion.musicplayer.ui.animation.Fade
import io.github.andremion.musicplayer.ui.animation.SceneRoot
import io.github.andremion.musicplayer.ui.animation.SlideFromBottom
import io.github.andremion.musicplayer.ui.animation.rememberMovableContent
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle
import moe.tlaster.precompose.koin.koinViewModel

@Composable
fun PlayerScreen() {
    val viewModel = koinViewModel(PlayerViewModel::class)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ScreenContent(uiState, viewModel::onUiEvent)
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

            val isPlaying = uiState.player.isPlaying

            val sceneTransition by animateFloatAsState(
                targetValue = if (isPlaying) 1f else 0f,
            )

            val cover = rememberMovableContent { modifier ->
                MusicCover(
                    modifier = modifier.animateBounds(),
                    uri = uiState.currentTrack?.album?.art.toString(),
                    transition = sceneTransition,
                    rotate = uiState.player == PlayerUiState.Player.Playing,
                    onRotationEnd = { onUiEvent(PlayerUiEvent.CoverRotationEnd) }
                )
            }

            val playButton = rememberMovableContent { modifier ->
                FloatingActionButton(
                    modifier = modifier.animateBounds(),
                    onClick = {
                        if (isPlaying) {
                            onUiEvent(PlayerUiEvent.PauseClick)
                        } else {
                            onUiEvent(PlayerUiEvent.PlayClick)
                        }
                    },
                ) {
                    Icon(
                        imageVector = if (isPlaying) {
                            Icons.Rounded.Pause
                        } else {
                            Icons.Rounded.PlayArrow
                        },
                        contentDescription = if (isPlaying) {
                            "Pause"
                        } else {
                            "Play"
                        },
                    )
                }
            }

            val headline = rememberMovableContent { modifier ->
                Row(
                    modifier = modifier.animateBounds(),
                ) {
                    Text(
                        text = uiState.currentTrack?.artist.toString(),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = " - " + uiState.currentTrack?.title.toString(),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            val time = rememberMovableContent { modifier ->
                Text(
                    modifier = modifier.animateBounds(),
                    text = uiState.time,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            val duration = rememberMovableContent { modifier ->
                Text(
                    modifier = modifier.animateBounds(),
                    text = uiState.duration,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            val timeBar = rememberMovableContent { modifier ->
                TimeBar(
                    modifier = modifier.animateBounds(),
                    position = uiState.position,
                    transition = sceneTransition
                )
            }

            uiState.playlist?.let { playlist ->
                SlideFromBottom(
                    modifier = Modifier.padding(top = CoverHeight),
                    visible = !isPlaying
                ) {
                    Playlist(playlist)
                }
            }

            Fade(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(top = CoverHeight * 1.3f),
                visible = isPlaying,
            ) {
                Row {
                    IconToggleButton(
                        checked = uiState.repeatMode != AudioPlayer.RepeatMode.Off,
                        onCheckedChange = { onUiEvent(PlayerUiEvent.RepeatClick) }
                    ) {
                        Icon(
                            imageVector = when (uiState.repeatMode) {
                                AudioPlayer.RepeatMode.Off -> Icons.Rounded.Repeat
                                AudioPlayer.RepeatMode.One -> Icons.Rounded.RepeatOne
                                AudioPlayer.RepeatMode.All -> Icons.Rounded.Repeat
                            },
                            contentDescription = when (uiState.repeatMode) {
                                AudioPlayer.RepeatMode.Off -> "Repeat off"
                                AudioPlayer.RepeatMode.One -> "Repeat one"
                                AudioPlayer.RepeatMode.All -> "Repeat all"
                            },
                        )
                    }
                    IconButton(
                        onClick = {

                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Shuffle,
                            contentDescription = "Shuffle",
                        )
                    }
                }
            }

            SlideFromBottom(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                visible = isPlaying,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                ) {
                    IconButton(
                        onClick = {

                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SkipPrevious,
                            contentDescription = "Skip to previous",
                        )
                    }
                    IconButton(
                        onClick = {

                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.FastRewind,
                            contentDescription = "Fast rewind",
                        )
                    }
                    IconButton(
                        onClick = {

                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.FastForward,
                            contentDescription = "Fast forward",
                        )
                    }
                    IconButton(
                        onClick = {

                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SkipNext,
                            contentDescription = "Skip to next",
                        )
                    }
                }
            }

            if (isPlaying) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center),
                    contentAlignment = Alignment.Center,
                ) {
                    cover(
                        Modifier
                            .padding(16.dp)
                            .height(CoverHeight)
                            .aspectRatio(1f)
                    )
                    Row(
                        modifier = Modifier
                            .matchParentSize()
                            .padding(horizontal = 50.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        time(Modifier)
                        duration(Modifier)
                    }
                    timeBar(Modifier.matchParentSize())
                    playButton(Modifier)
                }
                headline(
                    Modifier
                        .statusBarsPadding()
                        .padding(16.dp)
                        .align(Alignment.TopCenter)
                )
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
                            .background(color = MaterialTheme.colorScheme.surface.copy(alpha = 0.2f))
                            .padding(
                                horizontal = 16.dp,
                                vertical = 8.dp
                            )
                    ) {
                        headline(Modifier)
                        Row(
                            modifier = Modifier.padding(end = 16.dp + PlayButtonSize),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            time(Modifier)
                            timeBar(
                                Modifier.weight(1f)
                            )
                            duration(Modifier)
                        }
                    }
                    playButton(
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
private val PlayButtonSize = 56.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Playlist(playlist: Playlist) {
    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(top = PlayButtonSize / 2),
                title = {
                    Column {
                        Text(
                            text = playlist.title,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                        )
                        Text(
                            text = "${playlist.tracks.size} tracks",
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                },
                actions = {
                    IconButton(
                        modifier = Modifier.padding(end = 16.dp),
                        onClick = {

                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.MoreVert,
                            contentDescription = "Playlist options",
                        )
                    }
                }
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding),
        ) {
            items(
                items = playlist.tracks,
                key = Music::id
            ) { music ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {

                        }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AsyncImage(
                        modifier = Modifier
                            .size(56.dp),
                        model = music.album.art,
                        contentDescription = "Album art",
                    )
                    Text(
                        text = music.title,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}
