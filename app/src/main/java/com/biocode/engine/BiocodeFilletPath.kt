package com.biocode.engine

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.min

/**
 * БИОМОРФНЫЙ АЛГОРИТМ СОПРЯЖЕНИЙ BIOCODE (CONCAVE & CONVEX FILLETS)
 *
 * Создает математически непрерывный контур (C1-smooth) для модульных бенто-контейнеров:
 * - Внешние углы (90°): скругляются выпуклой дугой заданного радиуса R_convex.
 * - Внутренние углы (270°): скругляются отрицательной галтелью R_concave (как на референсах 3 и 4).
 *
 * Обеспечивает 100% векторную резкость, идеальные 1px границы и аппаратную производительность.
 */
object BiocodeFilletAlgorithm {

    /**
     * Генерирует Path для L-образного модульного бенто-контейнера.
     */
    fun createLShapePath(
        width: Float,
        height: Float,
        notchWidth: Float,
        notchHeight: Float,
        notchCorner: CornerPosition = CornerPosition.BOTTOM_RIGHT,
        convexRadius: Float = 24f,
        concaveRadius: Float = 20f
    ): Path {
        val path = Path()

        // Проверка: если вырез равен 0, возвращаем обычный скругленный прямоугольник
        if (notchWidth <= 0f || notchHeight <= 0f) {
            path.addRoundRect(
                RoundRect(
                    rect = Rect(0f, 0f, width, height),
                    radiusX = convexRadius,
                    radiusY = convexRadius
                )
            )
            return path
        }

        // Ограничиваем радиусы допустимыми геометрическими пределами
        val rConvex = min(convexRadius, min(width, height) * 0.4f)
        val rConcave = min(concaveRadius, min(notchWidth, notchHeight) * 0.8f)

        when (notchCorner) {
            CornerPosition.BOTTOM_RIGHT -> {
                val nw = min(notchWidth, width - rConvex * 2)
                val nh = min(notchHeight, height - rConvex * 2)
                val xCut = width - nw
                val yCut = height - nh

                // Стартуем с верхнего левого угла
                path.moveTo(rConvex, 0f)

                // 1. Верхняя грань -> Верхний правый угол
                path.lineTo(width - rConvex, 0f)
                path.arcTo(
                    rect = Rect(width - 2 * rConvex, 0f, width, 2 * rConvex),
                    startAngleDegrees = 270f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo = false
                )

                // 2. Правая грань до начала выреза -> Выпуклый угол выреза
                path.lineTo(width, yCut - rConvex)
                path.arcTo(
                    rect = Rect(width - 2 * rConvex, yCut - 2 * rConvex, width, yCut),
                    startAngleDegrees = 0f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo = false
                )

                // 3. Горизонтальная грань выреза до внутреннего угла
                path.lineTo(xCut + rConcave, yCut)

                // === ВОГНУТАЯ ГАЛТЕЛЬ (CONCAVE FILLET) ВНУТРЕННЕГО УГЛА ===
                path.arcTo(
                    rect = Rect(xCut, yCut, xCut + 2 * rConcave, yCut + 2 * rConcave),
                    startAngleDegrees = 270f,
                    sweepAngleDegrees = -90f,
                    forceMoveTo = false
                )

                // 4. Вертикальная грань выреза до нижнего выступа -> Выпуклый угол
                path.lineTo(xCut, height - rConvex)
                path.arcTo(
                    rect = Rect(xCut - 2 * rConvex, height - 2 * rConvex, xCut, height),
                    startAngleDegrees = 0f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo = false
                )

                // 5. Нижняя грань до левого нижнего угла
                path.lineTo(rConvex, height)
                path.arcTo(
                    rect = Rect(0f, height - 2 * rConvex, 2 * rConvex, height),
                    startAngleDegrees = 90f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo = false
                )

                // 6. Левая грань до верхнего левого угла
                path.lineTo(0f, rConvex)
                path.arcTo(
                    rect = Rect(0f, 0f, 2 * rConvex, 2 * rConvex),
                    startAngleDegrees = 180f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo = false
                )

                path.close()
            }

            CornerPosition.BOTTOM_LEFT -> {
                val nw = min(notchWidth, width - rConvex * 2)
                val nh = min(notchHeight, height - rConvex * 2)
                val xCut = nw
                val yCut = height - nh

                path.moveTo(rConvex, 0f)

                // Верх
                path.lineTo(width - rConvex, 0f)
                path.arcTo(
                    rect = Rect(width - 2 * rConvex, 0f, width, 2 * rConvex),
                    startAngleDegrees = 270f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo = false
                )

                // Право
                path.lineTo(width, height - rConvex)
                path.arcTo(
                    rect = Rect(width - 2 * rConvex, height - 2 * rConvex, width, height),
                    startAngleDegrees = 0f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo = false
                )

                // Низ до выреза
                path.lineTo(xCut + rConvex, height)
                path.arcTo(
                    rect = Rect(xCut, height - 2 * rConvex, xCut + 2 * rConvex, height),
                    startAngleDegrees = 90f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo = false
                )

                // Вертикаль выреза вверх
                path.lineTo(xCut, yCut + rConcave)

                // ВОГНУТАЯ ГАЛТЕЛЬ
                path.arcTo(
                    rect = Rect(xCut - 2 * rConcave, yCut, xCut, yCut + 2 * rConcave),
                    startAngleDegrees = 0f,
                    sweepAngleDegrees = -90f,
                    forceMoveTo = false
                )

                // Горизонталь выреза влево
                path.lineTo(rConvex, yCut)
                path.arcTo(
                    rect = Rect(0f, yCut - 2 * rConvex, 2 * rConvex, yCut),
                    startAngleDegrees = 90f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo = false
                )

                // Лево
                path.lineTo(0f, rConvex)
                path.arcTo(
                    rect = Rect(0f, 0f, 2 * rConvex, 2 * rConvex),
                    startAngleDegrees = 180f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo = false
                )

                path.close()
            }

            CornerPosition.TOP_RIGHT -> {
                val nw = min(notchWidth, width - rConvex * 2)
                val nh = min(notchHeight, height - rConvex * 2)
                val xCut = width - nw
                val yCut = nh

                path.moveTo(rConvex, 0f)
                path.lineTo(xCut - rConvex, 0f)
                path.arcTo(
                    rect = Rect(xCut - 2 * rConvex, 0f, xCut, 2 * rConvex),
                    startAngleDegrees = 270f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo = false
                )

                path.lineTo(xCut, yCut - rConcave)
                // ВОГНУТАЯ ГАЛТЕЛЬ
                path.arcTo(
                    rect = Rect(xCut, yCut - 2 * rConcave, xCut + 2 * rConcave, yCut),
                    startAngleDegrees = 180f,
                    sweepAngleDegrees = -90f,
                    forceMoveTo = false
                )

                path.lineTo(width - rConvex, yCut)
                path.arcTo(
                    rect = Rect(width - 2 * rConvex, yCut, width, yCut + 2 * rConvex),
                    startAngleDegrees = 270f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo = false
                )

                path.lineTo(width, height - rConvex)
                path.arcTo(
                    rect = Rect(width - 2 * rConvex, height - 2 * rConvex, width, height),
                    startAngleDegrees = 0f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo = false
                )

                path.lineTo(rConvex, height)
                path.arcTo(
                    rect = Rect(0f, height - 2 * rConvex, 2 * rConvex, height),
                    startAngleDegrees = 90f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo = false
                )

                path.lineTo(0f, rConvex)
                path.arcTo(
                    rect = Rect(0f, 0f, 2 * rConvex, 2 * rConvex),
                    startAngleDegrees = 180f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo = false
                )
                path.close()
            }

            CornerPosition.TOP_LEFT -> {
                val nw = min(notchWidth, width - rConvex * 2)
                val nh = min(notchHeight, height - rConvex * 2)
                val xCut = nw
                val yCut = nh

                path.moveTo(xCut + rConvex, 0f)
                path.lineTo(width - rConvex, 0f)
                path.arcTo(
                    rect = Rect(width - 2 * rConvex, 0f, width, 2 * rConvex),
                    startAngleDegrees = 270f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo = false
                )

                path.lineTo(width, height - rConvex)
                path.arcTo(
                    rect = Rect(width - 2 * rConvex, height - 2 * rConvex, width, height),
                    startAngleDegrees = 0f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo = false
                )

                path.lineTo(rConvex, height)
                path.arcTo(
                    rect = Rect(0f, height - 2 * rConvex, 2 * rConvex, height),
                    startAngleDegrees = 90f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo = false
                )

                path.lineTo(0f, yCut + rConvex)
                path.arcTo(
                    rect = Rect(0f, yCut, 2 * rConvex, yCut + 2 * rConvex),
                    startAngleDegrees = 180f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo = false
                )

                path.lineTo(xCut - rConcave, yCut)
                // ВОГНУТАЯ ГАЛТЕЛЬ
                path.arcTo(
                    rect = Rect(xCut - 2 * rConcave, yCut - 2 * rConcave, xCut, yCut),
                    startAngleDegrees = 90f,
                    sweepAngleDegrees = -90f,
                    forceMoveTo = false
                )

                path.lineTo(xCut, rConvex)
                path.arcTo(
                    rect = Rect(xCut, 0f, xCut + 2 * rConvex, 2 * rConvex),
                    startAngleDegrees = 180f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo = false
                )
                path.close()
            }
        }

        return path
    }
}

/**
 * Shape для применения L-контейнера в Modifier.clip() и Modifier.border().
 */
class BiocodeLShape(
    val notchWidthDp: Dp,
    val notchHeightDp: Dp,
    val notchCorner: CornerPosition = CornerPosition.BOTTOM_RIGHT,
    val convexRadiusDp: Dp = 24.dp,
    val concaveRadiusDp: Dp = 18.dp
) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val nwPx = with(density) { notchWidthDp.toPx() }
        val nhPx = with(density) { notchHeightDp.toPx() }
        val rConvexPx = with(density) { convexRadiusDp.toPx() }
        val rConcavePx = with(density) { concaveRadiusDp.toPx() }

        val path = BiocodeFilletAlgorithm.createLShapePath(
            width = size.width,
            height = size.height,
            notchWidth = nwPx,
            notchHeight = nhPx,
            notchCorner = notchCorner,
            convexRadius = rConvexPx,
            concaveRadius = rConcavePx
        )
        return Outline.Generic(path)
    }
}
