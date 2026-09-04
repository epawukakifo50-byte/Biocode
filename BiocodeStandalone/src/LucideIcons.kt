package com.biocode.engine

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * БИБЛИОТЕКА ВЕКТОРНЫХ ИКОНОК LUCIDE (LUCIDE.DEV)
 *
 * 24x24 viewBox, stroke width 2.0, скругленные концы линий (Round Cap & Round Join).
 */

@Composable
fun LucideIcon(
    modifier: Modifier = Modifier.size(24.dp),
    tint: Color = Color.White,
    strokeWidthDp: Dp = 2.dp,
    drawIcon: DrawScope.(Float) -> Unit
) {
    Canvas(modifier = modifier) {
        val s = size.minDimension / 24f
        val strokePx = strokeWidthDp.toPx()
        drawIcon(s)
    }
}

/**
 * 1. ИКОНКА «СЕГОДНЯ» (LUCIDE CALENDAR)
 */
@Composable
fun LucideCalendar(
    modifier: Modifier = Modifier.size(24.dp),
    tint: Color = Color.White,
    strokeWidthDp: Dp = 2.dp
) {
    Canvas(modifier = modifier) {
        val s = size.minDimension / 24f
        val stroke = Stroke(width = strokeWidthDp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)

        // Rect 3,4 to 21,22 (rx=2)
        val rectPath = Path().apply {
            addRoundRect(
                RoundRect(
                    rect = Rect(3f * s, 4f * s, 21f * s, 22f * s),
                    cornerRadius = CornerRadius(2f * s, 2f * s)
                )
            )
        }
        drawPath(rectPath, tint, style = stroke)

        // Top pins
        drawLine(tint, Offset(16f * s, 2f * s), Offset(16f * s, 6f * s), strokeWidthDp.toPx(), cap = StrokeCap.Round)
        drawLine(tint, Offset(8f * s, 2f * s), Offset(8f * s, 6f * s), strokeWidthDp.toPx(), cap = StrokeCap.Round)

        // Header line
        drawLine(tint, Offset(3f * s, 10f * s), Offset(21f * s, 10f * s), strokeWidthDp.toPx(), cap = StrokeCap.Round)
    }
}

/**
 * 2. ИКОНКА «ЧТО ПОЕСТЬ» (LUCIDE UTENSILS)
 */
@Composable
fun LucideUtensils(
    modifier: Modifier = Modifier.size(24.dp),
    tint: Color = Color.White,
    strokeWidthDp: Dp = 2.dp
) {
    Canvas(modifier = modifier) {
        val s = size.minDimension / 24f
        val stroke = Stroke(width = strokeWidthDp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)

        // Fork / Spoon paths
        val forkPath = Path().apply {
            moveTo(18f * s, 2f * s)
            lineTo(18f * s, 8f * s)
            arcTo(Rect(12f * s, 5f * s, 18f * s, 11f * s), 0f, 90f, false)
            lineTo(15f * s, 22f * s)
        }
        drawPath(forkPath, tint, style = stroke)

        // Fork left tine
        val forkTine = Path().apply {
            moveTo(12f * s, 2f * s)
            lineTo(12f * s, 8f * s)
            arcTo(Rect(12f * s, 5f * s, 18f * s, 11f * s), 180f, -90f, false)
        }
        drawPath(forkTine, tint, style = stroke)

        // Knife
        val knifePath = Path().apply {
            moveTo(6f * s, 2f * s)
            lineTo(6f * s, 22f * s)
            moveTo(9f * s, 2f * s)
            lineTo(9f * s, 6f * s)
            arcTo(Rect(3f * s, 2f * s, 9f * s, 8f * s), 0f, 90f, false)
            lineTo(6f * s, 8f * s)
        }
        drawPath(knifePath, tint, style = stroke)
    }
}

/**
 * 3. ЦЕНТРАЛЬНАЯ АКЦЕНТНАЯ ЗВЕЗДА (LUCIDE SPARKLES)
 */
@Composable
fun LucideSparkles(
    modifier: Modifier = Modifier.size(24.dp),
    tint: Color = Color.White,
    strokeWidthDp: Dp = 2.dp
) {
    Canvas(modifier = modifier) {
        val s = size.minDimension / 24f
        val stroke = Stroke(width = strokeWidthDp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)

        val starPath = Path().apply {
            moveTo(12f * s, 2f * s)
            quadraticTo(12f * s, 12f * s, 22f * s, 12f * s)
            quadraticTo(12f * s, 12f * s, 12f * s, 22f * s)
            quadraticTo(12f * s, 12f * s, 2f * s, 12f * s)
            quadraticTo(12f * s, 12f * s, 12f * s, 2f * s)
            close()
        }
        drawPath(starPath, tint)
        drawPath(starPath, tint, style = stroke)
    }
}

/**
 * 4. ИКОНКА «РАСЧЕТ» (LUCIDE CALCULATOR)
 */
@Composable
fun LucideCalculator(
    modifier: Modifier = Modifier.size(24.dp),
    tint: Color = Color.White,
    strokeWidthDp: Dp = 2.dp
) {
    Canvas(modifier = modifier) {
        val s = size.minDimension / 24f
        val stroke = Stroke(width = strokeWidthDp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)

        // Body 4, 2 to 20, 22
        val body = Path().apply {
            addRoundRect(
                RoundRect(
                    rect = Rect(4f * s, 2f * s, 20f * s, 22f * s),
                    cornerRadius = CornerRadius(2f * s, 2f * s)
                )
            )
        }
        drawPath(body, tint, style = stroke)

        // Display line 8,6 to 16,6
        drawLine(tint, Offset(8f * s, 6f * s), Offset(16f * s, 6f * s), strokeWidthDp.toPx(), cap = StrokeCap.Round)

        // Buttons
        drawCircle(tint, 1f * s, Offset(8f * s, 10f * s))
        drawCircle(tint, 1f * s, Offset(12f * s, 10f * s))
        drawCircle(tint, 1f * s, Offset(16f * s, 10f * s))

        drawCircle(tint, 1f * s, Offset(8f * s, 14f * s))
        drawCircle(tint, 1f * s, Offset(12f * s, 14f * s))
        drawCircle(tint, 1f * s, Offset(16f * s, 14f * s))

        drawCircle(tint, 1f * s, Offset(8f * s, 18f * s))
        drawCircle(tint, 1f * s, Offset(12f * s, 18f * s))
        drawCircle(tint, 1f * s, Offset(16f * s, 18f * s))
    }
}

/**
 * 5. ИКОНКА «ДНЕВНИК» (LUCIDE BOOK OPEN)
 */
@Composable
fun LucideBookOpen(
    modifier: Modifier = Modifier.size(24.dp),
    tint: Color = Color.White,
    strokeWidthDp: Dp = 2.dp
) {
    Canvas(modifier = modifier) {
        val s = size.minDimension / 24f
        val stroke = Stroke(width = strokeWidthDp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)

        // Left page
        val leftPage = Path().apply {
            moveTo(2f * s, 3f * s)
            lineTo(8f * s, 3f * s)
            quadraticTo(12f * s, 3f * s, 12f * s, 7f * s)
            lineTo(12f * s, 21f * s)
            quadraticTo(12f * s, 17f * s, 9f * s, 17f * s)
            lineTo(2f * s, 17f * s)
            close()
        }
        drawPath(leftPage, tint, style = stroke)

        // Right page
        val rightPage = Path().apply {
            moveTo(22f * s, 3f * s)
            lineTo(16f * s, 3f * s)
            quadraticTo(12f * s, 3f * s, 12f * s, 7f * s)
            lineTo(12f * s, 21f * s)
            quadraticTo(12f * s, 17f * s, 15f * s, 17f * s)
            lineTo(22f * s, 17f * s)
            close()
        }
        drawPath(rightPage, tint, style = stroke)
    }
}

/**
 * 6. ИКОНКА ДОБАВЛЕНИЯ (LUCIDE PLUS)
 */
@Composable
fun LucidePlus(
    modifier: Modifier = Modifier.size(24.dp),
    tint: Color = Color.White,
    strokeWidthDp: Dp = 2.dp
) {
    Canvas(modifier = modifier) {
        val s = size.minDimension / 24f
        val sw = strokeWidthDp.toPx()
        drawLine(tint, Offset(12f * s, 5f * s), Offset(12f * s, 19f * s), sw, cap = StrokeCap.Round)
        drawLine(tint, Offset(5f * s, 12f * s), Offset(19f * s, 12f * s), sw, cap = StrokeCap.Round)
    }
}

/**
 * 7. ИКОНКА СКАНИРОВАНИЯ (LUCIDE SCAN)
 */
@Composable
fun LucideScan(
    modifier: Modifier = Modifier.size(24.dp),
    tint: Color = Color.White,
    strokeWidthDp: Dp = 2.dp
) {
    Canvas(modifier = modifier) {
        val s = size.minDimension / 24f
        val stroke = Stroke(width = strokeWidthDp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)

        // Top-left
        val tl = Path().apply {
            moveTo(3f * s, 8f * s)
            lineTo(3f * s, 5f * s)
            arcTo(Rect(3f * s, 3f * s, 7f * s, 7f * s), 180f, 90f, false)
            lineTo(8f * s, 3f * s)
        }
        drawPath(tl, tint, style = stroke)

        // Top-right
        val tr = Path().apply {
            moveTo(16f * s, 3f * s)
            lineTo(19f * s, 3f * s)
            arcTo(Rect(17f * s, 3f * s, 21f * s, 7f * s), 270f, 90f, false)
            lineTo(21f * s, 8f * s)
        }
        drawPath(tr, tint, style = stroke)

        // Bottom-right
        val br = Path().apply {
            moveTo(21f * s, 16f * s)
            lineTo(21f * s, 19f * s)
            arcTo(Rect(17f * s, 17f * s, 21f * s, 21f * s), 0f, 90f, false)
            lineTo(16f * s, 21f * s)
        }
        drawPath(br, tint, style = stroke)

        // Bottom-left
        val bl = Path().apply {
            moveTo(8f * s, 21f * s)
            lineTo(5f * s, 21f * s)
            arcTo(Rect(3f * s, 17f * s, 7f * s, 21f * s), 90f, 90f, false)
            lineTo(3f * s, 16f * s)
        }
        drawPath(bl, tint, style = stroke)
    }
}

/**
 * 8. ИКОНКА ЗАКРЫТИЯ (LUCIDE X)
 */
@Composable
fun LucideX(
    modifier: Modifier = Modifier.size(24.dp),
    tint: Color = Color.White,
    strokeWidthDp: Dp = 2.dp
) {
    Canvas(modifier = modifier) {
        val s = size.minDimension / 24f
        val sw = strokeWidthDp.toPx()
        drawLine(tint, Offset(18f * s, 6f * s), Offset(6f * s, 18f * s), sw, cap = StrokeCap.Round)
        drawLine(tint, Offset(6f * s, 6f * s), Offset(18f * s, 18f * s), sw, cap = StrokeCap.Round)
    }
}

/**
 * 9. ИКОНКА ПОДТВЕРЖДЕНИЯ (LUCIDE CHECK)
 */
@Composable
fun LucideCheck(
    modifier: Modifier = Modifier.size(24.dp),
    tint: Color = Color.White,
    strokeWidthDp: Dp = 2.dp
) {
    Canvas(modifier = modifier) {
        val s = size.minDimension / 24f
        val stroke = Stroke(width = strokeWidthDp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val path = Path().apply {
            moveTo(20f * s, 6f * s)
            lineTo(9f * s, 17f * s)
            lineTo(4f * s, 12f * s)
        }
        drawPath(path, tint, style = stroke)
    }
}

/**
 * 10. ИКОНКА ОГНЯ / ЭНЕРГИИ (LUCIDE FLAME)
 */
@Composable
fun LucideFlame(
    modifier: Modifier = Modifier.size(24.dp),
    tint: Color = Color.White,
    strokeWidthDp: Dp = 2.dp
) {
    Canvas(modifier = modifier) {
        val s = size.minDimension / 24f
        val stroke = Stroke(width = strokeWidthDp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val path = Path().apply {
            moveTo(8.5f * s, 14.5f * s)
            arcTo(Rect(8.5f * s, 11f * s, 15.5f * s, 18f * s), 180f, 180f, false)
            quadraticTo(15.5f * s, 6f * s, 12f * s, 2f * s)
            quadraticTo(5f * s, 7f * s, 5f * s, 14f * s)
            arcTo(Rect(5f * s, 9f * s, 19f * s, 23f * s), 180f, -180f, false)
            quadraticTo(19f * s, 15f * s, 15f * s, 12f * s)
            close()
        }
        drawPath(path, tint, style = stroke)
    }
}

/**
 * 11. ИКОНКА ТРЕНДА / РОСТА (LUCIDE TRENDING UP)
 */
@Composable
fun LucideTrendingUp(
    modifier: Modifier = Modifier.size(24.dp),
    tint: Color = Color.White,
    strokeWidthDp: Dp = 2.dp
) {
    Canvas(modifier = modifier) {
        val s = size.minDimension / 24f
        val sw = strokeWidthDp.toPx()
        val stroke = Stroke(width = sw, cap = StrokeCap.Round, join = StrokeJoin.Round)
        val poly = Path().apply {
            moveTo(23f * s, 6f * s)
            lineTo(13.5f * s, 15.5f * s)
            lineTo(8.5f * s, 10.5f * s)
            lineTo(1f * s, 18f * s)
        }
        drawPath(poly, tint, style = stroke)
        drawLine(tint, Offset(17f * s, 6f * s), Offset(23f * s, 6f * s), sw, cap = StrokeCap.Round)
        drawLine(tint, Offset(23f * s, 6f * s), Offset(23f * s, 12f * s), sw, cap = StrokeCap.Round)
    }
}

/**
 * 12. ИКОНКА КАМЕРЫ (LUCIDE CAMERA)
 */
@Composable
fun LucideCamera(
    modifier: Modifier = Modifier.size(24.dp),
    tint: Color = Color.White,
    strokeWidthDp: Dp = 2.dp
) {
    Canvas(modifier = modifier) {
        val s = size.minDimension / 24f
        val sw = strokeWidthDp.toPx()
        val stroke = Stroke(width = sw, cap = StrokeCap.Round, join = StrokeJoin.Round)

        // Camera body path: M14.5 4h-5L7.5 7H4a2 2 0 0 0-2 2v10a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2V9a2 2 0 0 0-2-2h-3.5L14.5 4z
        val body = Path().apply {
            moveTo(14.5f * s, 4f * s)
            lineTo(9.5f * s, 4f * s)
            lineTo(7.5f * s, 7f * s)
            lineTo(4f * s, 7f * s)
            arcTo(Rect(2f * s, 7f * s, 6f * s, 11f * s), 270f, -90f, false)
            lineTo(2f * s, 19f * s)
            arcTo(Rect(2f * s, 17f * s, 6f * s, 21f * s), 180f, -90f, false)
            lineTo(20f * s, 21f * s)
            arcTo(Rect(18f * s, 17f * s, 22f * s, 21f * s), 90f, -90f, false)
            lineTo(22f * s, 9f * s)
            arcTo(Rect(18f * s, 7f * s, 22f * s, 11f * s), 0f, -90f, false)
            lineTo(16.5f * s, 7f * s)
            close()
        }
        drawPath(body, tint, style = stroke)

        // Center lens circle (cx=12, cy=13, r=3)
        drawCircle(
            color = tint,
            radius = 3.2f * s,
            center = Offset(12f * s, 13.5f * s),
            style = stroke
        )
    }
}

/**
 * 13. ИКОНКА УТРА / ЗАВТРАКА (LUCIDE SUN)
 */
@Composable
fun LucideSun(
    modifier: Modifier = Modifier.size(24.dp),
    tint: Color = Color.White,
    strokeWidthDp: Dp = 2.dp
) {
    Canvas(modifier = modifier) {
        val s = size.minDimension / 24f
        val sw = strokeWidthDp.toPx()
        val stroke = Stroke(width = sw, cap = StrokeCap.Round, join = StrokeJoin.Round)

        // Center circle (r=4)
        drawCircle(
            color = tint,
            radius = 4.2f * s,
            center = Offset(12f * s, 12f * s),
            style = stroke
        )

        // Cardinal rays
        drawLine(tint, Offset(12f * s, 2f * s), Offset(12f * s, 4.5f * s), sw, cap = StrokeCap.Round)
        drawLine(tint, Offset(12f * s, 19.5f * s), Offset(12f * s, 22f * s), sw, cap = StrokeCap.Round)
        drawLine(tint, Offset(2f * s, 12f * s), Offset(4.5f * s, 12f * s), sw, cap = StrokeCap.Round)
        drawLine(tint, Offset(19.5f * s, 12f * s), Offset(22f * s, 12f * s), sw, cap = StrokeCap.Round)

        // Diagonal rays
        drawLine(tint, Offset(4.93f * s, 4.93f * s), Offset(6.7f * s, 6.7f * s), sw, cap = StrokeCap.Round)
        drawLine(tint, Offset(17.3f * s, 17.3f * s), Offset(19.07f * s, 19.07f * s), sw, cap = StrokeCap.Round)
        drawLine(tint, Offset(4.93f * s, 19.07f * s), Offset(6.7f * s, 17.3f * s), sw, cap = StrokeCap.Round)
        drawLine(tint, Offset(17.3f * s, 6.7f * s), Offset(19.07f * s, 4.93f * s), sw, cap = StrokeCap.Round)
    }
}

/**
 * 14. ИКОНКА ВЕЧЕРА / УЖИНА (LUCIDE MOON)
 */
@Composable
fun LucideMoon(
    modifier: Modifier = Modifier.size(24.dp),
    tint: Color = Color.White,
    strokeWidthDp: Dp = 2.dp
) {
    Canvas(modifier = modifier) {
        val s = size.minDimension / 24f
        val sw = strokeWidthDp.toPx()
        val stroke = Stroke(width = sw, cap = StrokeCap.Round, join = StrokeJoin.Round)

        // Moon path: M12 3a6 6 0 0 0 9 9 9 9 0 1 1-9-9Z
        val path = Path().apply {
            moveTo(12f * s, 3f * s)
            cubicTo(
                12f * s, 6.31f * s,
                14.69f * s, 9f * s,
                18f * s, 9f * s
            )
            cubicTo(
                19.08f * s, 9f * s,
                20.1f * s, 8.71f * s,
                21f * s, 8.2f * s
            )
            cubicTo(
                20.5f * s, 15.1f * s,
                14.8f * s, 21f * s,
                7.8f * s, 21f * s
            )
            cubicTo(
                4.2f * s, 21f * s,
                2.5f * s, 18.2f * s,
                2.5f * s, 15f * s
            )
            cubicTo(
                2.5f * s, 8.5f * s,
                7.2f * s, 3.2f * s,
                12f * s, 3f * s
            )
            close()
        }
        drawPath(path, tint, style = stroke)
    }
}

/**
 * 15. ИКОНКА ПЕРЕКУСА / СНЕКА (LUCIDE APPLE)
 */
@Composable
fun LucideApple(
    modifier: Modifier = Modifier.size(24.dp),
    tint: Color = Color.White,
    strokeWidthDp: Dp = 2.dp
) {
    Canvas(modifier = modifier) {
        val s = size.minDimension / 24f
        val sw = strokeWidthDp.toPx()
        val stroke = Stroke(width = sw, cap = StrokeCap.Round, join = StrokeJoin.Round)

        // Apple stem
        val stem = Path().apply {
            moveTo(12f * s, 2f * s)
            cubicTo(12f * s, 5f * s, 14f * s, 6f * s, 14f * s, 6f * s)
        }
        drawPath(stem, tint, style = stroke)

        // Apple body
        val body = Path().apply {
            moveTo(12f * s, 7f * s)
            cubicTo(10f * s, 5f * s, 4f * s, 5.5f * s, 4f * s, 12f * s)
            cubicTo(4f * s, 17f * s, 7.5f * s, 21.5f * s, 11f * s, 21.5f * s)
            cubicTo(11.8f * s, 21.5f * s, 12f * s, 20.8f * s, 12f * s, 20.8f * s)
            cubicTo(12f * s, 20.8f * s, 12.2f * s, 21.5f * s, 13f * s, 21.5f * s)
            cubicTo(16.5f * s, 21.5f * s, 20f * s, 17f * s, 20f * s, 12f * s)
            cubicTo(20f * s, 5.5f * s, 14f * s, 5f * s, 12f * s, 7f * s)
            close()
        }
        drawPath(body, tint, style = stroke)
    }
}


