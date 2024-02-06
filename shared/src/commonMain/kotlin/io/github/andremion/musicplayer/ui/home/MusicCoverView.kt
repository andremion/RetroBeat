package io.github.andremion.musicplayer.ui.home

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateInt
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.unit.IntSize
import coil.compose.AsyncImage

enum class MusicCoverState { Paused, Playing }

@Composable
fun MusicCover(
    modifier: Modifier,
    currentState: MusicCoverState,
    uri: String
) {
    val transition = updateTransition(targetState = currentState, label = "MusicCover")

    val widthFraction by transition.animateFloat(label = "widthFraction") { state ->
        when (state) {
            MusicCoverState.Paused -> 1f
            MusicCoverState.Playing -> 0.5f
        }
    }
    val translationFraction by transition.animateFloat(label = "translationFraction") { state ->
        when (state) {
            MusicCoverState.Paused -> 0f
            MusicCoverState.Playing -> 1f
        }
    }
    val shapeCornerSize by transition.animateInt(label = "shapeCornerSize") { state ->
        when (state) {
            MusicCoverState.Paused -> 0
            MusicCoverState.Playing -> 50
        }
    }

    var parentSize by remember { mutableStateOf(IntSize.Zero) }
    var bounds by remember { mutableStateOf(IntSize.Zero) }

    AsyncImage(
        modifier = modifier
            .fillMaxWidth(widthFraction)
            .onPlaced { coordinates ->
                parentSize = coordinates.parentLayoutCoordinates?.size ?: IntSize.Zero
                bounds = coordinates.size
                println("bounds: $bounds, parentSize: $parentSize")
            }
            .graphicsLayer {
                translationX = (parentSize.width - bounds.width) / 2f// * translation
                translationY = (parentSize.height - bounds.height) / 2f * translationFraction
                shape = RoundedCornerShape(shapeCornerSize)
                clip = true
            },
        model = uri,
        contentScale = ContentScale.FillWidth,
        contentDescription = "Music Cover"
    )
}
