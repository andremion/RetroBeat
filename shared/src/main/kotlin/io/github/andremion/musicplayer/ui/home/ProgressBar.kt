package io.github.andremion.musicplayer.ui.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.heightIn
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
fun ProgressBar(
    modifier: Modifier,
    progress: Float,
    transition: Float
) {
    val coercedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(2_000),
        label = "progress"
    )

    val progressColor = ProgressIndicatorDefaults.linearColor
    val trackColor = ProgressIndicatorDefaults.linearTrackColor

    Canvas(
        modifier = modifier
            .heightIn(min = DefaultHeight)
            .padding(DefaultHeight / 2) // Extra padding to avoid cuttings on arc
            .progressSemantics(coercedProgress)
    ) {
        val startAngle = FULL_PROGRESS_ANGLE - START_ANGLE * transition
        val sweepAngle = FULL_PROGRESS_ANGLE * transition
        val strokeWidth = DefaultHeight.toPx()

        if (size.height <= strokeWidth) {
            drawLine(
                progress = coercedProgress,
                trackColor = trackColor,
                progressColor = progressColor,
                strokeWidth = strokeWidth
            )
        } else if (startAngle < HALF_FULL_ANGLE) {
            drawArc(
                startAngle = startAngle,
                trackSweepAngle = sweepAngle,
                progressSweepAngle = sweepAngle * coercedProgress,
                trackColor = trackColor,
                progressColor = progressColor,
                strokeWidth = strokeWidth
            )
        } else {
            drawArc(
                startAngle = HALF_FULL_ANGLE,
                trackSweepAngle = HALF_FULL_ANGLE,
                progressSweepAngle = HALF_FULL_ANGLE * coercedProgress,
                trackColor = trackColor,
                progressColor = progressColor,
                strokeWidth = strokeWidth
            )
        }
    }
}

private fun DrawScope.drawLine(
    progress: Float,
    trackColor: Color,
    progressColor: Color,
    strokeWidth: Float
) {
    drawLine(
        color = trackColor,
        start = Offset.Zero,
        end = Offset(size.width, 0f),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )
    val endX = size.width * progress
    drawLine(
        color = progressColor,
        start = Offset.Zero,
        end = Offset(endX, 0f),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )
}

private fun DrawScope.drawArc(
    startAngle: Float,
    trackSweepAngle: Float,
    progressSweepAngle: Float,
    trackColor: Color,
    progressColor: Color,
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
        color = progressColor,
        startAngle = startAngle,
        sweepAngle = progressSweepAngle,
        useCenter = false,
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
    )
}

private val DefaultHeight = 4.dp
private const val GAP_ANGLE = 90f
private const val FULL_PROGRESS_ANGLE = 360 - GAP_ANGLE
private const val START_ANGLE = GAP_ANGLE * 1.5f
private const val HALF_FULL_ANGLE = 180f
