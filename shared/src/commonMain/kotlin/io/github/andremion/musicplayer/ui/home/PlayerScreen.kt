package io.github.andremion.musicplayer.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onPlaced
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
            .navigationBarsPadding()
            .fillMaxSize(),
    ) {
        ProgressBar()

        var musicCoverBounds by remember { mutableStateOf(Rect.Zero) }

        MusicCover(
            modifier = Modifier
                .height(200.dp)
                .onPlaced { coordinates ->
                    musicCoverBounds = coordinates.boundsInParent()
                },
            currentState = currentState,
            uri = "https://e-cdns-images.dzcdn.net/images/cover/3071378af24d789b8fc69e95162041e4/500x500-000000-80-0-0.jpg",
        )

        PlayButton(
            currentState = currentState,
            anchorBounds = musicCoverBounds,
            onClick = { isPlaying = !isPlaying }
        )
    }
}

