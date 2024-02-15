package io.github.andremion.musicplayer.ui.player

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.dp
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import kotlin.math.min
import kotlin.math.roundToInt

private const val HALF_OF_FULL_ANGLE = 180f
private const val FULL_ANGLE = 360f

@Composable
fun MusicCover(
    modifier: Modifier,
    uri: String,
    transition: Float,
    rotate: Boolean,
    onRotationEnd: () -> Unit = {}
) {
    val rotation by rememberInfiniteTransition().animateFloat(
        label = "rotation",
        initialValue = 0f,
        targetValue = if (rotate) FULL_ANGLE else 0f,
        animationSpec = infiniteRepeatable(animation = tween(durationMillis = 2_000, easing = LinearEasing))
    )

    var endRotationAnimation by remember { mutableStateOf(Animatable(0f)) }

    LaunchedEffect(rotate) {
        // Choose the shortest distance to the 0 rotation
        val targetAngle = if (rotation > HALF_OF_FULL_ANGLE) FULL_ANGLE else 0f
        if (!rotate && targetAngle != rotation) {
            endRotationAnimation = Animatable(rotation)
            endRotationAnimation.animateTo(targetAngle, animationSpec = tween(durationMillis = 500))
            onRotationEnd()
        }
    }

    KamelImage(
        modifier = modifier
            .graphicsLayer {
                rotationZ = if (endRotationAnimation.isRunning) endRotationAnimation.value else rotation
                shape = RoundedCornerShape((SHAPE_CORNER_PERCENT * transition).toInt())
                clip = true
            }
            .drawWithContent {
                val trackRadius = min(size.width, size.height)
                val trackCount = (trackRadius / TrackWidth.toPx()).roundToInt()
                drawContent()
                drawTrack(
                    trackCount = trackCount,
                    alpha = transition
                )
            },
        resource = asyncPainterResource(uri),
        contentScale = ContentScale.FillWidth,
        contentDescription = "Music Cover",
        animationSpec = tween()
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

private const val SHAPE_CORNER_PERCENT = 50
private val TrackColor = Color(0x56FFFFFF)
private val TrackWidth = 20.dp
