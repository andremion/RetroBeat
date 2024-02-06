package io.github.andremion.musicplayer.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PlayerScreen() {
    var isPlaying by remember { mutableStateOf(false) }
    var currentState by remember { mutableStateOf(MusicCoverState.Paused) }
    LaunchedEffect(isPlaying) {
        currentState = if (isPlaying) {
            MusicCoverState.Playing
        } else {
            MusicCoverState.Paused
        }
    }
    Box(
        modifier = Modifier
            .systemBarsPadding()
            .fillMaxSize(),
    ) {
        MusicCover(
            modifier = Modifier
                .height(200.dp),
            currentState = currentState,
            uri = "https://e-cdns-images.dzcdn.net/images/cover/3071378af24d789b8fc69e95162041e4/500x500-000000-80-0-0.jpg",
        )
        FloatingActionButton(
            modifier = Modifier.align(Alignment.BottomCenter),
            onClick = { isPlaying = !isPlaying },
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
}
