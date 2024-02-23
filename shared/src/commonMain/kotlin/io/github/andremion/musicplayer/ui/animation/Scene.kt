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

package io.github.andremion.musicplayer.ui.animation

import androidx.compose.animation.core.VectorConverter
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.LookaheadScope
import androidx.compose.ui.layout.intermediateLayout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.round

@Composable
fun SceneRoot(
    content: @Composable SceneScope.() -> Unit
) {
    LookaheadScope {
        content(SceneScopeImpl())
    }
}

interface SceneScope {
    /**
     * Animates the bounds (position and size) of the layout within the given LookaheadScope,
     * whenever the relative position changes.
     */
    fun Modifier.animateBounds(): Modifier
}

private class SceneScopeImpl : SceneScope {

    @OptIn(ExperimentalComposeUiApi::class)
    override fun Modifier.animateBounds(): Modifier = composed {
        val coroutineScope = rememberCoroutineScope()

        val positionAnimation = remember {
            // IntOffset.VectorConverter to animate the x and y position
            DeferredTargetAnimation(IntOffset.VectorConverter)
        }
        val sizeAnimation = remember {
            // IntSize.VectorConverter to animate the width and height
            DeferredTargetAnimation(IntSize.VectorConverter)
        }

        intermediateLayout { measurable, _ ->
            // When layout changes, the lookahead pass will calculate a new final size for the child layout.
            // This lookahead size can be used to animate the size change,
            // such that the animation starts from the current size and gradually change towards `lookaheadSize`.
            val animatedSize = sizeAnimation.updateTarget(
                target = lookaheadSize,
                coroutineScope = coroutineScope,
            )
            val (width, height) = animatedSize
            val animatedConstraints = Constraints.fixed(width, height)

            val placeable = measurable.measure(animatedConstraints)
            layout(placeable.width, placeable.height) {
                // Converts coordinates of the current layout to LookaheadCoordinates
                val coordinates = coordinates
                if (coordinates != null) {
                    // Calculates the target position within the lookaheadScope
                    val target = lookaheadScopeCoordinates
                        .localLookaheadPositionOf(coordinates)
                        .round()
                    // Uses the target position to start a position animation
                    val animatedPosition = positionAnimation.updateTarget(
                        target = target,
                        coroutineScope = coroutineScope,
                    )
                    // Calculates the *current* position within the given LookaheadScope
                    val placementOffset = lookaheadScopeCoordinates.localPositionOf(
                        sourceCoordinates = coordinates,
                        relativeToSource = Offset.Zero
                    ).round()
                    // Calculates the delta between animated position in scope and current position in scope,
                    // and places the child at the delta offset.
                    // This puts the child layout at the animated position.
                    val (x, y) = animatedPosition - placementOffset
                    placeable.place(x, y)
                } else {
                    placeable.place(0, 0)
                }
            }
        }
    }
}
