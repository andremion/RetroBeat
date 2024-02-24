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

package io.github.andremion.musicplayer.ui.player

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animate
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
import kotlinx.coroutines.isActive
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.roundToInt

private const val INITIAL_ROTATION_ANGLE = 0f
private const val FULL_ROTATION_ANGLE = 360f
private const val HALF_OF_FULL_ROTATION_ANGLE = FULL_ROTATION_ANGLE / 2f
private const val ROTATION_END_DURATION = 500

@Composable
fun MusicCover(
    modifier: Modifier,
    uri: String,
    transition: Float,
    rotate: Boolean,
    onRotationEnd: () -> Unit = {}
) {
    var rotation by remember { mutableStateOf(INITIAL_ROTATION_ANGLE) }

    // This is used to avoid the rotation end callback when the composable is first composed
    // with rotate = false.
    var isInitialComposition by remember { mutableStateOf(true) }

    LaunchedEffect(rotate) {
        if (rotate) {
            // Rotate clockwise repeatedly
            while (isActive) {
                animate(
                    initialValue = 0f,
                    targetValue = FULL_ROTATION_ANGLE,
                    animationSpec = tween(durationMillis = 2000, easing = LinearEasing)
                ) { value, _ ->
                    rotation = value
                }
            }
        } else if (!isInitialComposition) {
            // Choose the shortest distance to the 0 rotation.
            // If the current rotation is greater than 180, rotate clockwise.
            // Otherwise rotate counter-clockwise.
            val targetAngle = if (rotation > HALF_OF_FULL_ROTATION_ANGLE) FULL_ROTATION_ANGLE else 0f
            // Make sure the duration is proportional to the remaining rotation angle.
            val durationMillis = (abs(targetAngle - rotation)
                * ROTATION_END_DURATION
                / HALF_OF_FULL_ROTATION_ANGLE
                ).roundToInt()
            animate(
                initialValue = rotation,
                targetValue = targetAngle,
                animationSpec = tween(durationMillis = durationMillis, easing = LinearEasing)
            ) { value, _ ->
                rotation = value
            }
            onRotationEnd()
        }
    }

    LaunchedEffect(true) {
        // After the initial composition we can set this to false and keep it that way.
        isInitialComposition = false
    }

    KamelImage(
        modifier = modifier
            .graphicsLayer {
                rotationZ = rotation
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
