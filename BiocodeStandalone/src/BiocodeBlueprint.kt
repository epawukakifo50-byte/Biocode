package com.biocode.engine

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * ИНЖЕНЕРНАЯ КООРДИНАТНАЯ СЕТКА BIOCODE (РЕФЕРЕНС 1)
 * Рисует техническую сетку с перекрестиями, диагоналями и точками привязки.
 */
@Composable
fun BiocodeBlueprintCanvas(
    modifier: Modifier = Modifier,
    gridPitchDp: Dp = 28.dp,
    lineColor: Color = Color(0xFF133B31).copy(alpha = 0.45f),
    crossColor: Color = Color(0xFF4AE3B5).copy(alpha = 0.35f),
    crossSizeDp: Dp = 4.dp
) {
    val density = LocalDensity.current

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val step = with(density) { gridPitchDp.toPx() }
        val crossArm = with(density) { crossSizeDp.toPx() }

        var x = step
        while (x < w) {
            drawLine(
                color = lineColor,
                start = Offset(x, 0f),
                end = Offset(x, h),
                strokeWidth = 1f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 6f))
            )
            x += step
        }

        var y = step
        while (y < h) {
            drawLine(
                color = lineColor,
                start = Offset(0f, y),
                end = Offset(w, y),
                strokeWidth = 1f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 6f))
            )
            y += step
        }

        // Перекрестия в узлах сетки
        x = step
        while (x < w) {
            y = step
            while (y < h) {
                drawLine(
                    color = crossColor,
                    start = Offset(x - crossArm, y),
                    end = Offset(x + crossArm, y),
                    strokeWidth = 1.25f
                )
                drawLine(
                    color = crossColor,
                    start = Offset(x, y - crossArm),
                    end = Offset(x, y + crossArm),
                    strokeWidth = 1.25f
                )
                y += step
            }
            x += step
        }
    }
}
