package com.biocode.engine

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * ГЕНЕРАТОР ФЛЮИДНЫХ СОПРЯЖЕНИЙ И МОСТОВ (РЕФЕРЕНС 1 И 2)
 *
 * Рассчитывает касательные дуги сопряжения (Tangential Fillet Bridges) между
 * соседними узлами сетки, создавая эффект монолитного биоморфного натяжения.
 */
object BiocodeFluidBridgeMath {

    /**
     * Создает Path для двух круглых узлов, соединенных вогнутым мостом натяжения.
     * @param c1 центр первого узла
     * @param r1 радиус первого узла
     * @param c2 центр второго узла
     * @param r2 радиус второго узла
     * @param bridgeRadius радиус вогнутой галтели моста
     */
    fun createBridgePath(
        c1: Offset,
        r1: Float,
        c2: Offset,
        r2: Float,
        bridgeRadius: Float = 16f
    ): Path {
        val path = Path()
        val dx = c2.x - c1.x
        val dy = c2.y - c1.y
        val d = sqrt(dx * dx + dy * dy)

        // Если узлы слишком далеко или накладываются
        if (d <= 0.001f || d >= (r1 + r2 + bridgeRadius * 2)) {
            path.addOval(Rect(c1.x - r1, c1.y - r1, c1.x + r1, c1.y + r1))
            path.addOval(Rect(c2.x - r2, c2.y - r2, c2.x + r2, c2.y + r2))
            return path
        }

        val baseAngle = atan2(dy, dx)

        // Радиусы фиктивных окружностей для поиска центров галтелей
        val R1 = r1 + bridgeRadius
        val R2 = r2 + bridgeRadius

        // Теорема косинусов для угла в треугольнике (c1, c2, центр галтели)
        val cosAlpha = ((d * d + R1 * R1 - R2 * R2) / (2f * d * R1)).coerceIn(-1f, 1f)
        val alpha = acos(cosAlpha)

        // Центры верхней и нижней вогнутых дуг
        val angleTop = baseAngle - alpha
        val filletCenterTop = Offset(
            c1.x + R1 * cos(angleTop),
            c1.y + R1 * sin(angleTop)
        )

        val angleBot = baseAngle + alpha
        val filletCenterBot = Offset(
            c1.x + R1 * cos(angleBot),
            c1.y + R1 * sin(angleBot)
        )

        // Точки касания на c1 и c2 для верхней галтели
        val t1Top = Offset(
            c1.x + r1 * cos(angleTop),
            c1.y + r1 * sin(angleTop)
        )
        val t2TopAngle = atan2(filletCenterTop.y - c2.y, filletCenterTop.x - c2.x)
        val t2Top = Offset(
            c2.x + r2 * cos(t2TopAngle),
            c2.y + r2 * sin(t2TopAngle)
        )

        // Точки касания для нижней галтели
        val t1Bot = Offset(
            c1.x + r1 * cos(angleBot),
            c1.y + r1 * sin(angleBot)
        )
        val t2BotAngle = atan2(filletCenterBot.y - c2.y, filletCenterBot.x - c2.x)
        val t2Bot = Offset(
            c2.x + r2 * cos(t2BotAngle),
            c2.y + r2 * sin(t2BotAngle)
        )

        // Строим замкнутый контур сопряжения
        path.moveTo(t1Top.x, t1Top.y)
        // Вогнутая дуга верхнего моста
        path.quadraticTo(
            (c1.x + c2.x) * 0.5f,
            (t1Top.y + t2Top.y) * 0.5f + bridgeRadius * 0.4f,
            t2Top.x,
            t2Top.y
        )
        // Обход узла c2
        val r2AngleStart = Math.toDegrees(atan2(t2Top.y - c2.y, t2Top.x - c2.x).toDouble()).toFloat()
        val r2AngleEnd = Math.toDegrees(atan2(t2Bot.y - c2.y, t2Bot.x - c2.x).toDouble()).toFloat()
        var sweep2 = r2AngleEnd - r2AngleStart
        if (sweep2 < 0) sweep2 += 360f

        path.arcTo(
            rect = Rect(c2.x - r2, c2.y - r2, c2.x + r2, c2.y + r2),
            startAngleDegrees = r2AngleStart,
            sweepAngleDegrees = sweep2,
            forceMoveTo = false
        )

        // Вогнутая дуга нижнего моста
        path.quadraticTo(
            (c1.x + c2.x) * 0.5f,
            (t1Bot.y + t2Bot.y) * 0.5f - bridgeRadius * 0.4f,
            t1Bot.x,
            t1Bot.y
        )

        // Обход узла c1
        val r1AngleStart = Math.toDegrees(atan2(t1Bot.y - c1.y, t1Bot.x - c1.x).toDouble()).toFloat()
        val r1AngleEnd = Math.toDegrees(atan2(t1Top.y - c1.y, t1Top.x - c1.x).toDouble()).toFloat()
        var sweep1 = r1AngleEnd - r1AngleStart
        if (sweep1 < 0) sweep1 += 360f

        path.arcTo(
            rect = Rect(c1.x - r1, c1.y - r1, c1.x + r1, c1.y + r1),
            startAngleDegrees = r1AngleStart,
            sweepAngleDegrees = sweep1,
            forceMoveTo = false
        )

        path.close()
        return path
    }

    /**
     * Контур сопряжения для плотно прилегающей горизонтальной цепочки сегментов (референс 1).
     * Создает непрерывную плашку с органическими перетяжками между ячейками.
     */
    fun createLinkedPillRowPath(
        width: Float,
        height: Float,
        segmentRatios: List<Float>,
        waistDepthRatio: Float = 0.22f
    ): Path {
        val path = Path()
        val r = height / 2f
        val waistH = height * (1f - waistDepthRatio)
        val yWaistTop = (height - waistH) / 2f
        val yWaistBot = height - yWaistTop

        // Вычисляем X-координаты разделителей
        val totalWeight = segmentRatios.sum()
        val xDivs = mutableListOf<Float>()
        var acc = 0f
        for (i in 0 until segmentRatios.size - 1) {
            acc += segmentRatios[i]
            xDivs.add((acc / totalWeight) * width)
        }

        // Стартуем с верхнего левого угла (скругление)
        path.moveTo(r, 0f)

        // Верхний край с вогнутыми ямками над разделителями
        for (xDiv in xDivs) {
            path.lineTo(xDiv - r * 0.6f, 0f)
            path.quadraticTo(xDiv, yWaistTop, xDiv + r * 0.6f, 0f)
        }
        path.lineTo(width - r, 0f)

        // Правое полукруглое скругление
        path.arcTo(
            rect = Rect(width - 2 * r, 0f, width, height),
            startAngleDegrees = 270f,
            sweepAngleDegrees = 180f,
            forceMoveTo = false
        )

        // Нижний край с вогнутыми ямками под разделителями
        for (xDiv in xDivs.reversed()) {
            path.lineTo(xDiv + r * 0.6f, height)
            path.quadraticTo(xDiv, yWaistBot, xDiv - r * 0.6f, height)
        }
        path.lineTo(r, height)

        // Левое полукруглое скругление
        path.arcTo(
            rect = Rect(0f, 0f, 2 * r, height),
            startAngleDegrees = 90f,
            sweepAngleDegrees = 180f,
            forceMoveTo = false
        )

        path.close()
        return path
    }
}

/**
 * Shape для цепочки пилюль с флюидными перетяжками.
 */
class BiocodeLinkedPillsShape(
    val segmentRatios: List<Float> = listOf(1f, 1.5f, 2f),
    val waistDepthRatio: Float = 0.22f
) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val path = BiocodeFluidBridgeMath.createLinkedPillRowPath(
            width = size.width,
            height = size.height,
            segmentRatios = segmentRatios,
            waistDepthRatio = waistDepthRatio
        )
        return Outline.Generic(path)
    }
}

/**
 * Горизонтальная цепочка взаимосвязанных пилюль в стиле Референса 1 (#8, #7, #6...).
 */
@Composable
fun BiocodeLinkedPillRow(
    modifier: Modifier = Modifier,
    height: Dp = 46.dp,
    segmentWeights: List<Float> = listOf(1f, 1.6f, 2.2f),
    backgroundColor: Color = BiocodePalette.SpruceDeck,
    borderColor: Color = BiocodePalette.DeckBorder,
    slot1: @Composable BoxScope.() -> Unit,
    slot2: @Composable BoxScope.() -> Unit,
    slot3: @Composable BoxScope.() -> Unit
) {
    val shape = BiocodeLinkedPillsShape(segmentWeights)
    Box(
        modifier = modifier
            .height(height)
            .clip(shape)
            .background(backgroundColor)
            .border(1.dp, borderColor, shape)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(segmentWeights[0])
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center,
                content = slot1
            )
            Box(
                modifier = Modifier
                    .weight(segmentWeights[1])
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center,
                content = slot2
            )
            Box(
                modifier = Modifier
                    .weight(segmentWeights[2])
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center,
                content = slot3
            )
        }
    }
}
