package io.github.andremion.musicplayer.ui.home

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.updateTransition
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp

@Composable
fun PlayButton(
    currentState: MusicCoverState,
    anchorBounds: Rect,
    onClick: () -> Unit,
) {
    var parentSize by remember { mutableStateOf(IntSize.Zero) }
    var buttonSize by remember { mutableStateOf(IntSize.Zero) }

    val transition = updateTransition(targetState = currentState, label = "PlayButton")
    val translationX by transition.animateFloat(label = "translationX") { state ->
        with(LocalDensity.current) {
            when (state) {
                MusicCoverState.Paused -> anchorBounds.right - buttonSize.width - Padding.toPx()
                MusicCoverState.Playing -> (parentSize.width - buttonSize.width) / 2f
            }
        }
    }
    val translationY by transition.animateFloat(label = "translationY") { state ->
        when (state) {
            MusicCoverState.Paused -> anchorBounds.bottom - buttonSize.height / 2
            MusicCoverState.Playing -> (parentSize.height - buttonSize.height) / 2f
        }
    }

    FloatingActionButton(
        modifier = Modifier
            .onPlaced { coordinates ->
                parentSize = coordinates.parentLayoutCoordinates?.size ?: IntSize.Zero
                buttonSize = coordinates.size
            }
            .graphicsLayer {
                this.translationX = translationX
                this.translationY = translationY
            },
        onClick = onClick,
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

private val Padding = 16.dp
