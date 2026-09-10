package com.biocode.engine

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.min

/**
 * МНОГОМОДУЛЬНАЯ ФОНОВАЯ ПЛАШКА С УНИКАЛЬНЫМИ ВЫХОДАМИ (РЕФЕРЕНС 4)
 *
 * Математически безупречный контур (C1-непрерывность, zero-spike):
 * 1. Верхняя головная часть (Upper Lobe)
 * 2. Нисходящая боковая шина (Lateral Spine) с органическим гладким сопряжением выреза
 * 3. Нижний горизонтальный выносной лоток-отросток (Bottom Tray Outgrowth)
 * 4. Внутренний огибающий карман (Embraced Pocket)
 */
class BiocodeCompoundPlateShape(
    val spineWidthDp: Dp = 54.dp,
    val topHeaderHeightDp: Dp = 64.dp,
    val bottomTrayHeightDp: Dp = 56.dp,
    val bottomTrayWidthDp: Dp = 240.dp,
    val notchCenterYDp: Dp = 150.dp,
    val notchDepthDp: Dp = 20.dp,
    val notchHalfHeightDp: Dp = 26.dp,
    val cornerRadiusDp: Dp = 24.dp,
    val concaveRadiusDp: Dp = 20.dp
) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val w = size.width
        val h = size.height
        val spineW = with(density) { spineWidthDp.toPx() }
        val topH = with(density) { topHeaderHeightDp.toPx() }
        val botTrayH = with(density) { bottomTrayHeightDp.toPx() }
        val botTrayW = with(density) { bottomTrayWidthDp.toPx().coerceAtMost(w) }
        val rConvex = with(density) { cornerRadiusDp.toPx() }
        val rConcave = with(density) { concaveRadiusDp.toPx() }
        val notchY = with(density) { notchCenterYDp.toPx() }
        val notchDepth = with(density) { notchDepthDp.toPx() }
        val notchHalfH = with(density) { notchHalfHeightDp.toPx() }

        val path = Path()

        // 1. Стартуем с верхнего левого угла
        path.moveTo(rConvex, 0f)

        // Верхняя грань головной части
        path.lineTo(w - rConvex, 0f)
        path.arcTo(
            rect = Rect(w - 2 * rConvex, 0f, w, 2 * rConvex),
            startAngleDegrees = 270f,
            sweepAngleDegrees = 90f,
            forceMoveTo = false
        )

        // Правая грань головной части вниз до начала кармана
        path.lineTo(w, topH - rConvex)
        path.arcTo(
            rect = Rect(w - 2 * rConvex, topH - 2 * rConvex, w, topH),
            startAngleDegrees = 0f,
            sweepAngleDegrees = 90f,
            forceMoveTo = false
        )

        // Горизонтальный срез внутрь кармана до левой шины
        path.lineTo(spineW + rConcave, topH)
        // Внутренняя вогнутая галтель (Concave Fillet) кармана
        path.arcTo(
            rect = Rect(spineW, topH, spineW + 2 * rConcave, topH + 2 * rConcave),
            startAngleDegrees = 270f,
            sweepAngleDegrees = -90f,
            forceMoveTo = false
        )

        // Внутренняя грань шины вниз до нижнего лотка
        val yTrayTop = h - botTrayH
        path.lineTo(spineW, yTrayTop - rConcave)
        // Внутренняя вогнутая галтель перехода шины в нижний лоток
        path.arcTo(
            rect = Rect(spineW, yTrayTop - 2 * rConcave, spineW + 2 * rConcave, yTrayTop),
            startAngleDegrees = 180f,
            sweepAngleDegrees = -90f,
            forceMoveTo = false
        )

        // Верхний край нижнего лотка вправо до скругления
        path.lineTo(botTrayW - rConvex, yTrayTop)
        path.arcTo(
            rect = Rect(botTrayW - 2 * rConvex, yTrayTop, botTrayW, yTrayTop + 2 * rConvex),
            startAngleDegrees = 270f,
            sweepAngleDegrees = 90f,
            forceMoveTo = false
        )

        // Правый торец нижнего лотка вниз
        path.lineTo(botTrayW, h - rConvex)
        path.arcTo(
            rect = Rect(botTrayW - 2 * rConvex, h - 2 * rConvex, botTrayW, h),
            startAngleDegrees = 0f,
            sweepAngleDegrees = 90f,
            forceMoveTo = false
        )

        // Нижняя грань основания влево
        path.lineTo(rConvex, h)
        path.arcTo(
            rect = Rect(0f, h - 2 * rConvex, 2 * rConvex, h),
            startAngleDegrees = 90f,
            sweepAngleDegrees = 90f,
            forceMoveTo = false
        )

        // Левая грань шины снизу вверх до стыковочного выреза (Dock Notch)
        val notchYBottom = notchY + notchHalfH
        val notchYTop = notchY - notchHalfH

        if (notchYTop > rConvex && notchYBottom < h - rConvex) {
            // Подъем до начала выреза
            path.lineTo(0f, notchYBottom)

            // Плавный C1-кубический вырез внутрь без изломов и острых углов
            path.cubicTo(
                0f, notchYBottom - notchHalfH * 0.45f,
                notchDepth, notchY + notchHalfH * 0.45f,
                notchDepth, notchY
            )
            path.cubicTo(
                notchDepth, notchY - notchHalfH * 0.45f,
                0f, notchYTop + notchHalfH * 0.45f,
                0f, notchYTop
            )
        }

        // Левая грань вверх до верхнего левого угла
        path.lineTo(0f, rConvex)
        path.arcTo(
            rect = Rect(0f, 0f, 2 * rConvex, 2 * rConvex),
            startAngleDegrees = 180f,
            sweepAngleDegrees = 90f,
            forceMoveTo = false
        )

        path.close()
        return Outline.Generic(path)
    }
}

/**
 * 🌟 МНОГОМОДУЛЬНЫЙ БИО-ХАБ (COMPOUND BIO-HUB BENTO CARD)
 */
@Composable
fun BiocodeCompoundBioHub(
    state: DailyNutritionState,
    onProteinBoost: () -> Unit,
    onScanClick: () -> Unit,
    onThemeToggle: () -> Unit = {},
    bmrCalories: Int = 1750,
    workoutBurnCalories: Int = 420,
    modifier: Modifier = Modifier
) {
    val totalExpended = bmrCalories + workoutBurnCalories
    val netBalance = state.consumedCalories - totalExpended
    val ratioBurnVsIntake = if (state.consumedCalories > 0) {
        ((totalExpended.toFloat() / state.consumedCalories.toFloat()) * 100).toInt()
    } else {
        100
    }

    val plateShape = BiocodeCompoundPlateShape(
        spineWidthDp = 48.dp,
        topHeaderHeightDp = 62.dp,
        bottomTrayHeightDp = 58.dp,
        bottomTrayWidthDp = 250.dp,
        notchCenterYDp = 150.dp,
        notchDepthDp = 18.dp,
        notchHalfHeightDp = 28.dp,
        cornerRadiusDp = 24.dp,
        concaveRadiusDp = 20.dp
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(316.dp)
    ) {
        // 1. МНОГОМОДУЛЬНАЯ ФОНОВАЯ ПЛАШКА С ВЫХОДАМИ
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(plateShape)
                .background(BiocodePalette.PineTeal)
                .border(1.2.dp, BiocodePalette.DeckBorder, plateShape)
        ) {
            // А. Верхняя голова плашки: только слово METABOLIC HUB с выключкой справа
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(62.dp)
                    .padding(horizontal = 18.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = "METABOLIC HUB",
                    style = BiocodeTypography.MonospaceTitle.copy(
                        fontSize = 15.sp,
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = BiocodePalette.NoguchiCream
                )
            }

            // Б. Нижний лоток-отросток: белковый шот (выключка справа, вровень с PHOTO, без плюса в тексте)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .width(250.dp)
                    .height(58.dp)
                    .padding(end = 16.dp, bottom = 10.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                Box(
                    modifier = Modifier
                        .height(38.dp)
                        .clip(RoundedCornerShape(19.dp))
                        .background(BiocodePalette.NoguchiCream)
                        .clickable { onProteinBoost() }
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        LucidePlus(modifier = Modifier.size(14.dp), tint = BiocodePalette.DarkMoss)
                        Text(
                            text = "25G PROTEIN",
                            style = BiocodeTypography.TelemetryLabel,
                            color = BiocodePalette.DarkMoss,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 2. ОРБИТАЛЬНЫЙ СТЫКОВОЧНЫЙ УЗЕЛ В ЛЕВОМ ВЫРЕЗЕ (КНОПКА ПЕРЕКЛЮЧЕНИЯ ТЕМ СО СПИНОМ И ПУЛЬСОМ)
        var clickCount by remember { mutableStateOf(0) }
        val starRotation by animateFloatAsState(
            targetValue = clickCount * 360f,
            animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessLow),
            label = "theme_star_spin"
        )
        val starScale by animateFloatAsState(
            targetValue = if (clickCount % 2 == 1) 1.25f else 1.0f,
            animationSpec = spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessMedium),
            label = "theme_star_scale"
        )

        Box(
            modifier = Modifier
                .offset(x = 10.dp, y = 132.dp)
                .size(36.dp)
                .clip(CircleShape)
                .background(BiocodePalette.DarkMoss)
                .border(1.2.dp, BiocodePalette.BioLime, CircleShape)
                .clickable {
                    clickCount++
                    onThemeToggle()
                },
            contentAlignment = Alignment.Center
        ) {
            LucideSparkles(
                modifier = Modifier
                    .size(17.dp)
                    .graphicsLayer {
                        rotationZ = starRotation
                        scaleX = starScale
                        scaleY = starScale
                    },
                tint = BiocodePalette.BioLime
            )
        }

        // 2. КНОПКА PHOTO
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 12.dp, bottom = 10.dp)
        ) {
            Box(
                modifier = Modifier
                    .height(38.dp)
                    .clip(RoundedCornerShape(19.dp))
                    .background(BiocodePalette.DarkMoss)
                    .border(1.2.dp, BiocodePalette.BioLime, RoundedCornerShape(19.dp))
                    .clickable { onScanClick() }
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    LucideCamera(modifier = Modifier.size(15.dp), tint = BiocodePalette.BioLime)
                    Text(
                        text = "PHOTO",
                        style = BiocodeTypography.TelemetryLabel,
                        color = BiocodePalette.BioLime,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 3. ВЛОЖЕННЫЙ МОДУЛЬ ЭНЕРГОРАСХОДА В КАРМАНЕ (ОПЕРАТИВНЫЙ КАЛЬКУЛЯТОР)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 54.dp, top = 68.dp, end = 12.dp, bottom = 64.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(BiocodePalette.DarkMoss)
                .border(1.dp, BiocodePalette.NoguchiBorder, RoundedCornerShape(20.dp))
                .padding(14.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "СУТОЧНЫЙ ЭНЕРГОРАСХОД",
                        style = BiocodeTypography.TelemetryLabel.copy(fontSize = 8.sp),
                        color = BiocodePalette.NoguchiCream.copy(alpha = 0.7f),
                        maxLines = 1
                    )
                    Text(
                        text = "БАЛАНС: ${if (netBalance >= 0) "+$netBalance" else "$netBalance"} KCAL",
                        style = BiocodeTypography.TelemetryLabel.copy(fontSize = 8.5.sp),
                        color = if (netBalance >= 0) BiocodePalette.BioLime else BiocodePalette.LipidAmber,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }

                // LED 5x7 растровые цифры и оперативная сводка расхода
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        BiocodeDotMatrixText(
                            text = "${state.consumedCalories}",
                            dotSize = 3.0.dp,
                            dotSpacing = 1.1.dp,
                            activeColor = BiocodePalette.BioLime,
                            inactiveColor = BiocodePalette.SpruceDeck.copy(alpha = 0.55f)
                        )
                        Text(
                            text = "ПОСТУПЛЕНИЕ (ЕДА)",
                            style = BiocodeTypography.TelemetryLabel.copy(fontSize = 7.sp),
                            color = BiocodePalette.BioLime
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "РАСХОД: $totalExpended KCAL",
                            style = BiocodeTypography.MonospaceTitle.copy(fontSize = 13.sp),
                            color = BiocodePalette.LipidAmber,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "BMR $bmrCalories + ТРЕН $workoutBurnCalories",
                            style = BiocodeTypography.TelemetryLabel.copy(fontSize = 7.5.sp),
                            color = BiocodePalette.NoguchiCream.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "ЦЕЛЬ: ${state.targetCalories} KCAL",
                            style = BiocodeTypography.TelemetryLabel.copy(fontSize = 7.5.sp),
                            color = BiocodePalette.NoguchiCream.copy(alpha = 0.5f)
                        )
                    }
                }

                // Инженерная био-энергетическая телеметрическая шкала
                BiocodeCalorieTelemetryBar(
                    consumed = state.consumedCalories,
                    target = state.targetCalories,
                    burnVsIntakePercent = ratioBurnVsIntake,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * ⚡ ИНЖЕНЕРНАЯ СЕГМЕНТИРОВАННАЯ ЭНЕРГЕТИЧЕСКАЯ ШКАЛА С БИО-ЗАРЯДОМ
 */
@Composable
fun BiocodeCalorieTelemetryBar(
    consumed: Int,
    target: Int,
    burnVsIntakePercent: Int = 100,
    modifier: Modifier = Modifier
) {
    val ratio = if (target > 0) (consumed.toFloat() / target.toFloat()).coerceIn(0f, 1.25f) else 0f
    val percentInt = (ratio * 100).toInt()

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        // 1. СЕГМЕНТИРОВАННАЯ ЭНЕРГЕТИЧЕСКАЯ ШКАЛА (28 МИКРО-ЯЧЕЕК LED)
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(13.dp)
        ) {
            val w = size.width
            val h = size.height
            val segmentCount = 28
            val gap = 2.dp.toPx()
            val totalGaps = gap * (segmentCount - 1)
            val segmentWidth = (w - totalGaps) / segmentCount
            val segmentCorner = 2.dp.toPx()

            val activeSegments = (ratio.coerceAtMost(1f) * segmentCount).toInt()
            val hasOvercharge = ratio > 1f

            val baseAccent = BiocodePalette.BioLime
            val leadingCursorColor = lerp(baseAccent, Color.White, 0.45f)
            val baseDeepTone = lerp(BiocodePalette.DarkMoss, baseAccent, 0.55f)
            val inactiveBg = BiocodePalette.SpruceDeck.copy(alpha = 0.5f)
            val inactiveBorderColor = BiocodePalette.DeckBorder.copy(alpha = 0.35f)

            for (i in 0 until segmentCount) {
                val segX = i * (segmentWidth + gap)
                val isFilled = i < activeSegments
                val isLeadingTip = i == activeSegments - 1 && isFilled

                val segColor = when {
                    hasOvercharge && i >= (segmentCount * 0.9f) -> BiocodePalette.LipidAmber
                    isLeadingTip -> leadingCursorColor
                    isFilled -> {
                        val frac = i.toFloat() / segmentCount.toFloat()
                        lerp(baseDeepTone, baseAccent, frac)
                    }
                    else -> inactiveBg
                }

                // Рисуем сегмент
                drawRoundRect(
                    color = segColor,
                    topLeft = Offset(segX, 0f),
                    size = Size(segmentWidth, h),
                    cornerRadius = CornerRadius(segmentCorner, segmentCorner)
                )

                // Если ячейка неактивна — тонкая обводка контура в тон активной темы
                if (!isFilled) {
                    drawRoundRect(
                        color = inactiveBorderColor,
                        topLeft = Offset(segX, 0f),
                        size = Size(segmentWidth, h),
                        cornerRadius = CornerRadius(segmentCorner, segmentCorner),
                        style = Stroke(width = 0.8.dp.toPx())
                    )
                }

                // Световой ореол на острие в тон темы
                if (isLeadingTip) {
                    drawRoundRect(
                        color = leadingCursorColor.copy(alpha = 0.4f),
                        topLeft = Offset(segX - 1.dp.toPx(), -1.dp.toPx()),
                        size = Size(segmentWidth + 2.dp.toPx(), h + 2.dp.toPx()),
                        cornerRadius = CornerRadius(segmentCorner + 1.dp.toPx(), segmentCorner + 1.dp.toPx())
                    )
                }
            }

            // Целевая риска 100%
            val targetX = w - 1.dp.toPx()
            drawLine(
                color = BiocodePalette.NoguchiCream.copy(alpha = 0.85f),
                start = Offset(targetX, -1.5.dp.toPx()),
                end = Offset(targetX, h + 1.5.dp.toPx()),
                strokeWidth = 1.5.dp.toPx()
            )
        }

        // 2. МИКРО-ТЕЛЕМЕТРИЯ ПОД ШКАЛОЙ
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "0%",
                style = BiocodeTypography.TelemetryLabel.copy(fontSize = 7.5.sp),
                color = BiocodePalette.NoguchiCream.copy(alpha = 0.4f)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(if (ratio >= 1f) BiocodePalette.LipidAmber else BiocodePalette.BioLime)
                )
                Text(
                    text = "$percentInt% БИО-ЗАРЯД  //  РАСХОД: $burnVsIntakePercent%",
                    style = BiocodeTypography.TelemetryLabel.copy(
                        fontSize = 7.5.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = if (ratio >= 1f) BiocodePalette.LipidAmber else BiocodePalette.BioLime
                )
            }

            Text(
                text = "ЦЕЛЬ 100%",
                style = BiocodeTypography.TelemetryLabel.copy(fontSize = 7.5.sp),
                color = BiocodePalette.NoguchiCream.copy(alpha = 0.4f)
            )
        }
    }
}
