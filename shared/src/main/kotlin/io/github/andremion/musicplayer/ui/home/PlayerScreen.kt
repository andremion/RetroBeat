package io.github.andremion.musicplayer.ui.home

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.material.icons.rounded.FastRewind
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.andremion.musicplayer.ui.animation.Fade
import io.github.andremion.musicplayer.ui.animation.SceneRoot
import io.github.andremion.musicplayer.ui.animation.SlideFromBottom
import io.github.andremion.musicplayer.ui.animation.rememberMovableContent
import kotlinx.coroutines.delay

@Composable
fun PlayerScreen() {
    var currentState by remember { mutableStateOf(MusicCoverState.Paused) }
    SceneRoot {
        Box(
            modifier = Modifier
                .navigationBarsPadding()
                .fillMaxSize(),
        ) {

            var rotateCover by remember { mutableStateOf(false) }

            val cover = rememberMovableContent { modifier ->
                var transition by remember { mutableFloatStateOf(0f) }
                MusicCover(
                    modifier = modifier.animateBounds(
                        onTransitionUpdate = { fraction -> transition = fraction },
                        onTransitionEnd = { rotateCover = currentState == MusicCoverState.Playing }
                    ),
                    uri = "https://e-cdns-images.dzcdn.net/images/cover/3071378af24d789b8fc69e95162041e4/500x500-000000-80-0-0.jpg",
                    rotate = rotateCover,
                    transition = transition,
                    onEndRotation = { currentState = MusicCoverState.Paused }
                )
            }

            val playButton = rememberMovableContent { modifier ->
                FloatingActionButton(
                    modifier = modifier.animateBounds(),
                    onClick = {
                        if (currentState == MusicCoverState.Paused) {
                            currentState = MusicCoverState.Playing
                        } else {
                            rotateCover = false
                        }
                    },
                ) {
                    Icon(
                        imageVector = when (currentState) {
                            MusicCoverState.Paused -> Icons.Rounded.PlayArrow
                            MusicCoverState.Playing -> Icons.Rounded.Pause
                        },
                        contentDescription = "Play/Pause"
                    )
                }
            }

            val headline = rememberMovableContent { modifier ->
                Row(
                    modifier = modifier.animateBounds(),
                ) {
                    Text(
                        text = "Artist",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = " - Title",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            val time = rememberMovableContent { modifier ->
                Text(
                    modifier = modifier.animateBounds(),
                    text = "00:10",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            val duration = rememberMovableContent { modifier ->
                Text(
                    modifier = modifier.animateBounds(),
                    text = "03:30",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            var progressValue by remember { mutableFloatStateOf(0f) }
            LaunchedEffect(true) {
                while (progressValue < 1f) {
                    progressValue += .03f
                    delay(1000)
                }
            }

            val progress = rememberMovableContent { modifier ->
                var transition by remember { mutableFloatStateOf(0f) }
                ProgressBar(
                    modifier = modifier
                        .animateBounds(
                            onTransitionUpdate = { fraction -> transition = fraction },
                        ),
                    progress = progressValue,
                    transition = transition
                )
            }

            SlideFromBottom(
                modifier = Modifier
                    .padding(top = CoverHeight),
                visible = currentState == MusicCoverState.Paused,
            ) {
                Playlist(Modifier)
            }

            Fade(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(top = CoverHeight * 1.3f),
                visible = currentState == MusicCoverState.Playing,
            ) {
                Row {
                    IconButton(
                        onClick = {

                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Repeat,
                            contentDescription = "Repeat",
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
                visible = currentState == MusicCoverState.Playing,
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

            when (currentState) {
                MusicCoverState.Paused -> {
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
                                progress(
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

                MusicCoverState.Playing -> {
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
                        progress(Modifier.matchParentSize())
                        playButton(Modifier)
                    }
                    headline(
                        Modifier
                            .statusBarsPadding()
                            .padding(16.dp)
                            .align(Alignment.TopCenter)
                    )
                }
            }
        }

    }

//    ConstraintLayout(
//        modifier = Modifier
//            .navigationBarsPadding()
//            .fillMaxSize(),
//    ) {
//
//        val (musicCover, playButton) = createRefs()
//
//        MusicCover(
//            modifier = Modifier
//                .constrainAs(musicCover) {
//                    when (currentState) {
//                        MusicCoverState.Paused -> {
//                            top.linkTo(parent.top)
//                        }
//
//                        MusicCoverState.Playing -> {
//                            top.linkTo(parent.top)
//                            bottom.linkTo(parent.bottom)
//                        }
//                    }
//                    start.linkTo(parent.start)
//                    end.linkTo(parent.end)
//                }
//                .height(CoverHeight),
//            currentState = currentState,
//            uri = "https://e-cdns-images.dzcdn.net/images/cover/3071378af24d789b8fc69e95162041e4/500x500-000000-80-0-0.jpg",
//        )
//    }
}

private val CoverHeight = 256.dp
private val PlayButtonSize = 56.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Playlist(modifier: Modifier) {
    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "My favourite",
                            style = MaterialTheme.typography.titleSmall,
                        )
                        Text(
                            text = "128 songs",
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                },
                actions = {
                    IconButton(
                        modifier = modifier.padding(end = 16.dp),
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
            items(10) { item ->
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clickable {

                        }
                        .padding(16.dp),
                    text = "Song $item",
                )
            }
        }
    }
}
