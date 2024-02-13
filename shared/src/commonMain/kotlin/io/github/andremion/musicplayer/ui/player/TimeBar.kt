package io.github.andremion.musicplayer.ui.player

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.progressSemantics
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun TimeBar(
    modifier: Modifier,
    position: Float,
    transition: Float
) {
    val coercedPosition by animateFloatAsState(
        label = "position",
        targetValue = position.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 1000, easing = LinearEasing),
    )

    val trackColor = ProgressIndicatorDefaults.linearTrackColor
    val positionColor = ProgressIndicatorDefaults.linearColor

    Canvas(
        modifier = modifier
            .height(DefaultHeight)
            .padding(DefaultHeight / 2) // Extra padding to avoid cuttings on arc
            .progressSemantics(coercedPosition)
    ) {
        val startAngle = TIME_BAR_FULL_ANGLE - START_ANGLE * transition
        val sweepAngle = TIME_BAR_FULL_ANGLE * transition
        val strokeWidth = DefaultHeight.toPx()

        if (size.height <= strokeWidth) {
            drawLine(
                position = coercedPosition,
                trackColor = trackColor,
                positionColor = positionColor,
                strokeWidth = strokeWidth
            )
        } else if (startAngle < HALF_OF_FULL_ANGLE) {
            drawArc(
                startAngle = startAngle,
                trackSweepAngle = sweepAngle,
                positionSweepAngle = sweepAngle * coercedPosition,
                trackColor = trackColor,
                positionColor = positionColor,
                strokeWidth = strokeWidth
            )
        } else {
            drawArc(
                startAngle = HALF_OF_FULL_ANGLE,
                trackSweepAngle = HALF_OF_FULL_ANGLE,
                positionSweepAngle = HALF_OF_FULL_ANGLE * coercedPosition,
                trackColor = trackColor,
                positionColor = positionColor,
                strokeWidth = strokeWidth
            )
        }
    }
}

private fun DrawScope.drawLine(
    position: Float,
    trackColor: Color,
    positionColor: Color,
    strokeWidth: Float
) {
    drawLine(
        color = trackColor,
        start = Offset.Zero,
        end = Offset(size.width, 0f),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )
    if (position > 0f) {
        val endX = size.width * position
        drawLine(
            color = positionColor,
            start = Offset.Zero,
            end = Offset(endX, 0f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.drawArc(
    startAngle: Float,
    trackSweepAngle: Float,
    positionSweepAngle: Float,
    trackColor: Color,
    positionColor: Color,
    strokeWidth: Float,
) {
    drawArc(
        color = trackColor,
        startAngle = startAngle,
        sweepAngle = trackSweepAngle,
        useCenter = false,
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
    )
    drawArc(
        color = positionColor,
        startAngle = startAngle,
        sweepAngle = positionSweepAngle,
        useCenter = false,
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
    )
}

private val DefaultHeight = 4.dp
private const val GAP_ANGLE = 90f
private const val TIME_BAR_FULL_ANGLE = 360f - GAP_ANGLE
private const val START_ANGLE = GAP_ANGLE * 1.5f
private const val HALF_OF_FULL_ANGLE = 180f
