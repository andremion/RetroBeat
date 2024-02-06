package io.github.andremion.musicplayer.ui.home

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateInt
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kotlin.math.min
import kotlin.math.roundToInt


enum class MusicCoverState { Paused, Playing }

@Composable
fun MusicCover(
    modifier: Modifier,
    currentState: MusicCoverState,
    uri: String
) {
    val transition = updateTransition(targetState = currentState, label = "MusicCover")

    val transitionFraction by transition.animateFloat(label = "transitionFraction") { state ->
        when (state) {
            MusicCoverState.Paused -> 0f
            MusicCoverState.Playing -> 1f
        }
    }

    val widthFraction by transition.animateFloat(label = "widthFraction") { state ->
        when (state) {
            MusicCoverState.Paused -> 1f
            MusicCoverState.Playing -> 0.5f
        }
    }

    val shapeCornerSize by transition.animateInt(label = "shapeCornerSize") { state ->
        when (state) {
            MusicCoverState.Paused -> 0
            MusicCoverState.Playing -> 50
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "infiniteTransition")

    val rotation by infiniteTransition.animateFloat(
        label = "rotation",
        initialValue = 0f,
        targetValue = if (currentState == MusicCoverState.Playing) 360f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 2_000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        )
    )

    var parentSize by remember { mutableStateOf(IntSize.Zero) }
    var bounds by remember { mutableStateOf(IntSize.Zero) }
    var trackCount by remember { mutableIntStateOf(0) }

    AsyncImage(
        modifier = modifier
            .fillMaxWidth(widthFraction)
            .onPlaced { coordinates ->
                parentSize = coordinates.parentLayoutCoordinates?.size ?: IntSize.Zero
                bounds = coordinates.size
                val w = bounds.width
                val h = bounds.height
                val trackRadius = min(w, h)
                trackCount = (trackRadius / 50.dp.value).roundToInt()
            }
            .graphicsLayer {
                translationX = (parentSize.width - bounds.width) / 2f// * transitionFraction
                translationY = (parentSize.height - bounds.height) / 2f * transitionFraction
                rotationZ = rotation
                shape = RoundedCornerShape(shapeCornerSize)
                clip = true
            }
            .drawWithContent {
                drawContent()
                drawTrack(trackCount, transitionFraction)
            },
        model = uri,
        contentScale = ContentScale.FillWidth,
        contentDescription = "Music Cover"
    )
}

private fun ContentDrawScope.drawTrack(trackCount: Int, alpha: Float) {
    val radius = size.minDimension / 2.0f
    for (i in 0 until trackCount) {
        drawCircle(
            color = TrackColor,
            radius = radius * (i / trackCount.toFloat()),
            alpha = alpha,
            style = Stroke(width = 1.dp.toPx())
        )
    }
}

private val TrackColor = Color(0x56FFFFFF)
