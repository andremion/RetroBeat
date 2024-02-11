package io.github.andremion.musicplayer.ui.animation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import kotlinx.coroutines.launch

@Composable
fun SceneRoot(
    content: @Composable SceneScope.() -> Unit
) {
    LookaheadScope {
        content(SceneScopeImpl())
    }
}

interface SceneScope {
    fun Modifier.animateBounds(
        onTransitionStart: () -> Unit = {},
        onTransitionUpdate: (fraction: Float) -> Unit = {},
        onTransitionEnd: () -> Unit = {},
    ): Modifier
}

private class SceneScopeImpl : SceneScope {

    /**
     * Modifier to animate the bounds (position and size) of the layout within the given LookaheadScope,
     * whenever the relative position changes.
     */
    @OptIn(ExperimentalComposeUiApi::class)
    override fun Modifier.animateBounds(
        onTransitionStart: () -> Unit,
        onTransitionUpdate: (fraction: Float) -> Unit,
        onTransitionEnd: () -> Unit,
    ): Modifier = composed {

        var positionAnimation by remember {
            // We use a custom VectorConverter to animate the IntOffset (x and y)
            mutableStateOf<Animatable<IntOffset, AnimationVector2D>?>(null)
        }

        var sizeAnimation by remember {
            // We use a custom VectorConverter to animate the IntSize (width and height)
            mutableStateOf<Animatable<IntSize, AnimationVector2D>?>(null)
        }

        // Animation used only to trigger the transition update callback
        val transitionUpdateAnimation by remember {
            mutableStateOf(Animatable(0f))
        }
        // Tracks the initial bounds target to know which target should be used for the transition update animation
        var initialPositionTarget by remember { mutableStateOf(IntOffset.Zero) }
        var initialSizeTarget by remember { mutableStateOf(IntSize.Zero) }

        // Tracks the running flag of the animations to trigger the start and end callbacks
        var isAnimationRunning by remember { mutableStateOf<Boolean?>(null) }
        LaunchedEffect(isAnimationRunning) {
            if (isAnimationRunning == true) {
                onTransitionStart()
            } else if (isAnimationRunning == false) {
                onTransitionEnd()
            }
        }

        val positionAnimationSpec = tween<IntOffset>(2_000)
        val sizeAnimationSpec = tween<IntSize>(2_000)

        intermediateLayout { measurable, _ ->

            // When layout changes, the lookahead pass will calculate a new final size for the child layout.
            // This lookahead size can be used to animate the size change,
            // such that the animation starts from the current size and gradually change towards `lookaheadSize`.
            val sizeAnim = sizeAnimation
                ?: Animatable(lookaheadSize, IntSize.VectorConverter).also { sizeAnimation = it }
            if (lookaheadSize != sizeAnim.targetValue) {
                launch { sizeAnim.animateTo(lookaheadSize, sizeAnimationSpec) }
                // Update the initial size target if it's not set yet
                if (initialSizeTarget == IntSize.Zero) {
                    initialSizeTarget = lookaheadSize
                }
                // Launches an animation and triggers the transition callbacks
                launch {
                    isAnimationRunning = true
                    transitionUpdateAnimation.animateTo(
                        targetValue = if (sizeAnim.targetValue == initialSizeTarget) 1f else 0f,
                        animationSpec = tween(2_000)
                    ) {
                        // Callback with the current fraction of the size animation
                        onTransitionUpdate(value)
                    }
                    // Once the animation ends, updates the flag to trigger the end callback
                    isAnimationRunning = false
                }
            }
            val (width, height) = sizeAnim.value
            val animatedConstraints = Constraints.fixed(width, height)

            val placeable = measurable.measure(animatedConstraints)
            layout(placeable.width, placeable.height) {
                // Converts coordinates of the current layout to LookaheadCoordinates
                val coordinates = coordinates
                if (coordinates != null) {

                    // Calculates the target offset within the lookaheadScope
                    val target = lookaheadScopeCoordinates
                        .localLookaheadPositionOf(coordinates)
                        .round()

                    // Uses the target offset to start an offset animation
                    val posAnim = positionAnimation
                        ?: Animatable(target, IntOffset.VectorConverter).also { positionAnimation = it }
                    if (target != posAnim.targetValue) {
                        launch { posAnim.animateTo(target, positionAnimationSpec) }
                        // Update the initial position target if it's not set yet
                        if (initialPositionTarget == IntOffset.Zero) {
                            initialPositionTarget = target
                        }
                        // Launches an animation and triggers the transition callbacks
                        launch {
                            isAnimationRunning = true
                            // Reusing the same transitionUpdateAnimation.
                            // It will cancel the previous animation and start a new one.
                            // This is safe because we are interested in a single animation at a time
                            // to trigger the transition update callback.
                            transitionUpdateAnimation.animateTo(
                                targetValue = if (posAnim.targetValue == initialPositionTarget) 1f else 0f,
                                animationSpec = tween(2_000)
                            ) {
                                // Callback with the current fraction of the position animation
                                onTransitionUpdate(value)
                            }
                            // Once the animation ends, updates the flag to trigger the end callback
                            isAnimationRunning = false
                        }
                    }
                    // Calculates the *current* offset within the given LookaheadScope
                    val placementOffset = lookaheadScopeCoordinates.localPositionOf(
                        sourceCoordinates = coordinates,
                        relativeToSource = Offset.Zero
                    ).round()
                    // Calculates the delta between animated position in scope and current position in scope,
                    // and places the child at the delta offset.
                    // This puts the child layout at the animated position.
                    val (x, y) = posAnim.run { value - placementOffset }
                    placeable.place(x, y)
                } else {
                    placeable.place(0, 0)
                }
            }
        }
    }
}
