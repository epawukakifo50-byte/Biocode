package com.biocode.engine

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke

/**
 * 4-ЛУЧЕВАЯ ДОФАМИНОВАЯ ЗВЕЗДА (DOPAMINE STAR // РЕФЕРЕНС 4)
 */
@Composable
fun VectorDopamineStar(
    modifier: Modifier = Modifier,
    tint: Color = Color(0xFF4AE3B5)
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(w * 0.5f, 0f)
            quadraticTo(w * 0.5f, h * 0.5f, w, h * 0.5f)
            quadraticTo(w * 0.5f, h * 0.5f, w * 0.5f, h)
            quadraticTo(w * 0.5f, h * 0.5f, 0f, h * 0.5f)
            quadraticTo(w * 0.5f, h * 0.5f, w * 0.5f, 0f)
            close()
        }
        drawPath(path = path, color = tint)
    }
}

/**
 * БИОМЕТРИЧЕСКИЙ ЩИТ (PROTEIN SHIELD)
 */
@Composable
fun VectorBiocodeShield(
    modifier: Modifier = Modifier,
    tint: Color = Color(0xFFFF7A3D)
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokePx = (w * 0.1f).coerceAtLeast(1.5f)
        val path = Path().apply {
            moveTo(w * 0.5f, strokePx)
            lineTo(w - strokePx, h * 0.22f)
            lineTo(w - strokePx, h * 0.58f)
            quadraticTo(w * 0.5f, h * 0.88f, w * 0.5f, h - strokePx)
            quadraticTo(w * 0.5f, h * 0.88f, strokePx, h * 0.58f)
            lineTo(strokePx, h * 0.22f)
            close()
        }
        drawPath(
            path = path,
            color = tint,
            style = Stroke(width = strokePx, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

/**
 * ЭНЕРГЕТИЧЕСКАЯ МОЛНИЯ (ENERGY BOLT)
 */
@Composable
fun VectorBiocodeBolt(
    modifier: Modifier = Modifier,
    tint: Color = Color(0xFF4AE3B5)
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(w * 0.55f, 0f)
            lineTo(w * 0.15f, h * 0.55f)
            lineTo(w * 0.50f, h * 0.55f)
            lineTo(w * 0.45f, h)
            lineTo(w * 0.85f, h * 0.45f)
            lineTo(w * 0.50f, h * 0.45f)
            close()
        }
        drawPath(path = path, color = tint)
    }
}
