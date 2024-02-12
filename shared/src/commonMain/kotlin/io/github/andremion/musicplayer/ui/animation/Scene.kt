package io.github.andremion.musicplayer.ui.animation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
        val coroutineScope = rememberCoroutineScope()

        val positionAnimation = remember {
            // We use a custom VectorConverter to animate the IntOffset (x and y)
            DeferredTargetAnimation(IntOffset.VectorConverter)
        }
        val sizeAnimation = remember {
            // We use a custom VectorConverter to animate the IntSize (width and height)
            DeferredTargetAnimation(IntSize.VectorConverter)
        }
        // Animation used only to trigger the transition update callback
        val transitionUpdateAnimation by remember {
            mutableStateOf(Animatable(0f))
        }
        // Which target should be used for the transition update animation
        var reverseTransition by remember { mutableStateOf(false) }

        val positionAnimationSpec = tween<IntOffset>(2_000)
        val sizeAnimationSpec = tween<IntSize>(2_000)
        val transitionUpdateAnimationSpec = tween<Float>(2_000)

        // Tracks the running flag of the animations to trigger the callbacks
        var isAnimationRunning by remember { mutableStateOf<Boolean?>(null) }
        LaunchedEffect(isAnimationRunning) {
            if (isAnimationRunning == true) {
                onTransitionStart()
                coroutineScope.launch {
                    transitionUpdateAnimation.animateTo(
                        targetValue = if (reverseTransition) 0f else 1f,
                        animationSpec = transitionUpdateAnimationSpec
                    ) {
                        onTransitionUpdate(value)
                    }
                }
            } else if (isAnimationRunning == false) {
                coroutineScope.launch { transitionUpdateAnimation.stop() }
                onTransitionEnd()
                reverseTransition = !reverseTransition
            }
        }

        intermediateLayout { measurable, _ ->
            // When layout changes, the lookahead pass will calculate a new final size for the child layout.
            // This lookahead size can be used to animate the size change,
            // such that the animation starts from the current size and gradually change towards `lookaheadSize`.
            val animatedSize = sizeAnimation.updateTarget(
                target = lookaheadSize,
                coroutineScope = coroutineScope,
                animationSpec = sizeAnimationSpec,
                onAnimationStart = { isAnimationRunning = true },
                onAnimationEnd = { isAnimationRunning = false }
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
                        animationSpec = positionAnimationSpec,
                        onAnimationStart = { isAnimationRunning = true },
                        onAnimationEnd = { isAnimationRunning = false }
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
