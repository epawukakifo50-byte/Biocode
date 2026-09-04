package com.biocode.engine

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 1. БЕНТО-МОДУЛЬ КАЛОРИЙНОСТИ С ТОЧЕЧНОЙ LED-МАТРИЦЕЙ (РЕФЕРЕНС 3 И 5)
 */
@Composable
fun BiocodeCalorieBentoModule(
    consumedCalories: Int,
    targetCalories: Int,
    modifier: Modifier = Modifier
) {
    BiocodeBentoCard(
        modifier = modifier,
        title = "Энергобаланс // Ккал",
        badgeText = "АКТИВЕН",
        badgeColor = BiocodePalette.BioLime,
        backgroundColor = BiocodePalette.DarkMoss,
        borderColor = BiocodePalette.NoguchiBorder
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                BiocodeDotMatrixText(
                    text = "$consumedCalories",
                    dotSize = 3.dp,
                    dotSpacing = 1.2.dp,
                    activeColor = BiocodePalette.BioLime,
                    inactiveColor = Color(0xFF141C0F)
                )

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "ЦЕЛЬ",
                        style = BiocodeTypography.TelemetryLabel,
                        color = BiocodePalette.NoguchiCream.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "$targetCalories KCAL",
                        style = BiocodeTypography.ValueNumber,
                        color = BiocodePalette.NoguchiCream
                    )
                }
            }

            val ratio = (consumedCalories.toFloat() / targetCalories.toFloat()).coerceIn(0f, 1f)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(Color(0xFF151D12))
                    .border(1.dp, BiocodePalette.NoguchiBorder, RoundedCornerShape(5.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fraction = ratio)
                        .clip(RoundedCornerShape(5.dp))
                        .background(BiocodePalette.BioLime)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "ОСТАТОК: ${targetCalories - consumedCalories} KCAL",
                    style = BiocodeTypography.TelemetryLabel,
                    color = BiocodePalette.BioLime
                )
                Text(
                    text = "${(ratio * 100).toInt()}% НОРМЫ",
                    style = BiocodeTypography.TelemetryLabel,
                    color = BiocodePalette.NoguchiCream.copy(alpha = 0.7f)
                )
            }
        }
    }
}

/**
 * 2. ПОЛНОСТЬЮ ПЕРЕРАБОТАННЫЙ МОДУЛЬ БЖУ ПОД ВИЗУАЛЬНЫЙ КОД BIOCODE (РЕФЕРЕНСЫ 3, 4, 5)
 *
 * Сложный модульный полиомино-комплекс с 3 выделенными ячейками телеметрии,
 * инженерными перекрестиями, бейджами норм и энерго-лентой баланса.
 */
@Composable
fun BiocodeMacroBentoModule(
    proteinG: Float,
    targetProteinG: Float,
    fatG: Float,
    targetFatG: Float,
    carbsG: Float,
    targetCarbsG: Float,
    modifier: Modifier = Modifier
) {
    BiocodeBentoCard(
        modifier = modifier,
        title = "БИОХИМИЧЕСКИЙ ПРОФИЛЬ БЖУ",
        badgeText = "БАЛАНС 100%",
        badgeColor = BiocodePalette.BioLime,
        backgroundColor = BiocodePalette.PineTeal,
        borderColor = BiocodePalette.DeckBorder,
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 3 МОДУЛЬНЫХ ЯЧЕЙКИ В СТИЛЕ РЕФЕРЕНСА 5 (БЕЛКИ / ЖИРЫ / УГЛЕВОДЫ)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // ЯЧЕЙКА 1: БЕЛКИ (Электрический Био-лайм)
                MacroModularCell(
                    label = "БЕЛКИ",
                    current = proteinG,
                    target = targetProteinG,
                    accentColor = BiocodePalette.BioLime,
                    modifier = Modifier.weight(1f)
                )

                // ЯЧЕЙКА 2: ЖИРЫ (Липидный Янтарь)
                MacroModularCell(
                    label = "ЖИРЫ",
                    current = fatG,
                    target = targetFatG,
                    accentColor = BiocodePalette.MacroFat,
                    modifier = Modifier.weight(1f)
                )

                // ЯЧЕЙКА 3: УГЛЕВОДЫ (Керамический Крем)
                MacroModularCell(
                    label = "УГЛЕВОДЫ",
                    current = carbsG,
                    target = targetCarbsG,
                    accentColor = BiocodePalette.NoguchiCream,
                    modifier = Modifier.weight(1f)
                )
            }

            // ОБЪЕДИНЕННАЯ ЭНЕРГО-ЛЕНТА БАЛАНСА КБЖУ (НАТИВНАЯ ТЕЛЕМЕТРИЯ)
            val pCals = (proteinG * 4f).coerceAtLeast(0f)
            val fCals = (fatG * 9f).coerceAtLeast(0f)
            val cCals = (carbsG * 4f).coerceAtLeast(0f)
            val totalCals = (pCals + fCals + cCals).coerceAtLeast(1f)

            val pPct = ((pCals / totalCals) * 100).toInt()
            val fPct = ((fCals / totalCals) * 100).toInt()
            val cPct = (100 - pPct - fPct).coerceAtLeast(0)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(BiocodePalette.DeckBorder.copy(alpha = 0.4f))
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight((pCals / totalCals).coerceAtLeast(0.05f))
                            .background(BiocodePalette.BioLime)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight((fCals / totalCals).coerceAtLeast(0.05f))
                            .background(BiocodePalette.MacroFat)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight((cCals / totalCals).coerceAtLeast(0.05f))
                            .background(BiocodePalette.NoguchiCream)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "БЕЛОК $pPct%",
                    style = BiocodeTypography.TelemetryLabel.copy(fontSize = 8.sp),
                    color = BiocodePalette.BioLime,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "ЖИРЫ $fPct%",
                    style = BiocodeTypography.TelemetryLabel.copy(fontSize = 8.sp),
                    color = BiocodePalette.MacroFat,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "УГЛЕВОДЫ $cPct%",
                    style = BiocodeTypography.TelemetryLabel.copy(fontSize = 8.sp),
                    color = BiocodePalette.NoguchiCream,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun MacroModularCell(
    label: String,
    current: Float,
    target: Float,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val ratio = (current / target).coerceIn(0f, 1f)
    val percent = ((current / target) * 100).toInt()

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(BiocodePalette.DarkMoss)
            .border(1.dp, BiocodePalette.NoguchiBorder, RoundedCornerShape(16.dp))
            .padding(10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = BiocodeTypography.TelemetryLabel.copy(fontSize = 7.5.sp),
                    color = accentColor,
                    fontWeight = FontWeight.Bold
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(accentColor.copy(alpha = 0.15f))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = "$percent%",
                        style = BiocodeTypography.TelemetryLabel.copy(fontSize = 7.sp),
                        color = accentColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Цифра граммовки
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "${current.toInt()}",
                    style = BiocodeTypography.MonospaceTitle.copy(fontSize = 17.sp),
                    color = accentColor
                )
                Text(
                    text = "/${target.toInt()}г",
                    style = BiocodeTypography.TelemetryLabel.copy(fontSize = 8.sp),
                    color = BiocodePalette.NoguchiCream.copy(alpha = 0.5f),
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }

            // Индикатор
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(BiocodePalette.DeckBorder.copy(alpha = 0.35f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(ratio)
                        .clip(RoundedCornerShape(2.dp))
                        .background(accentColor)
                )
            }
        }
    }
}

/**
 * Типы габаритов адаптивной карточки приема пищи.
 */
enum class MealCardTier {
    SINGLE_LINE, // <= 16 символов: 1 строка, тонкий флюидный мостик (52dp)
    DOUBLE_LINE, // 17..34 символов: 2 строки, высотка tier 1 (76dp)
    TRIPLE_LINE  // > 34 символов: 3 строки, высотка tier 2 (98dp)
}

fun getMealCardTier(name: String): MealCardTier {
    return when {
        name.length <= 16 -> MealCardTier.SINGLE_LINE
        name.length <= 34 -> MealCardTier.DOUBLE_LINE
        else -> MealCardTier.TRIPLE_LINE
    }
}

/**
 * 3. АДАПТИВНАЯ БИОМОРФНАЯ ФОРМА ПЛАШКИ ПРИЕМА ПИЩИ (РЕФЕРЕНСЫ 1, 4, 5)
 *
 * - При 1 строчке: тонкий флюидный мостик (Dumbbell Waist) между узлами
 * - При 2 строчках: центральный контейнер вырастает в «высотку» (Stepped Tower)
 * - При 3 строчках: высотка расширяется по вертикали для 3-строчного заголовка
 */
class BiocodeAdaptiveMealShape(
    val tier: MealCardTier,
    val leftNodeWidthDp: Dp = 52.dp,
    val rightNodeWidthDp: Dp = 96.dp,
    val cornerRadiusDp: Dp = 18.dp
) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val w = size.width
        val h = size.height
        val leftW = with(density) { leftNodeWidthDp.toPx() }
        val rightW = with(density) { rightNodeWidthDp.toPx() }
        val r = with(density) { cornerRadiusDp.toPx() }

        val path = Path()

        if (tier == MealCardTier.SINGLE_LINE) {
            // ОДНА СТРОКА: Тонкий флюидный мостик между левым и правым узлами
            val waistH = with(density) { 34.dp.toPx() }
            val yTop = (h - waistH) / 2f
            val yBot = (h + waistH) / 2f
            val dx = with(density) { 14.dp.toPx() }

            path.moveTo(r, 0f)
            path.lineTo(leftW, 0f)

            // Плавный спуск в тонкий мостик (C1 Bezier)
            path.cubicTo(leftW + dx * 0.5f, 0f, leftW + dx * 0.5f, yTop, leftW + dx, yTop)

            // Мостик поверху до правого узла
            path.lineTo(w - rightW - dx, yTop)

            // Плавный подъем на правый узел
            path.cubicTo(w - rightW - dx * 0.5f, yTop, w - rightW - dx * 0.5f, 0f, w - rightW, 0f)

            // Правый узел
            path.lineTo(w - r, 0f)
            path.arcTo(Rect(w - 2 * r, 0f, w, 2 * r), 270f, 90f, false)
            path.lineTo(w, h - r)
            path.arcTo(Rect(w - 2 * r, h - 2 * r, w, h), 0f, 90f, false)
            path.lineTo(w - rightW, h)

            // Плавный подъем снизу в мостик
            path.cubicTo(w - rightW - dx * 0.5f, h, w - rightW - dx * 0.5f, yBot, w - rightW - dx, yBot)

            // Мостик понизу до левого узла
            path.lineTo(leftW + dx, yBot)

            // Плавный спуск из мостика на дно левого узла
            path.cubicTo(leftW + dx * 0.5f, yBot, leftW + dx * 0.5f, h, leftW, h)

            path.lineTo(r, h)
            path.arcTo(Rect(0f, h - 2 * r, 2 * r, h), 90f, 90f, false)
            path.lineTo(0f, r)
            path.arcTo(Rect(0f, 0f, 2 * r, 2 * r), 180f, 90f, false)
            path.close()
        } else {
            // ДВЕ ИЛИ ТРИ СТРОКИ: Центральный контейнер вырастает в «высотку» со скруглениями
            val sideH = with(density) { 50.dp.toPx() }
            val ySideTop = (h - sideH) / 2f
            val ySideBot = (h + sideH) / 2f
            val dx = with(density) { 16.dp.toPx() }

            path.moveTo(r, ySideTop)
            path.lineTo(leftW, ySideTop)

            // Плавный подъем вверх на «высотку» (C1 Bezier)
            path.cubicTo(leftW + dx * 0.5f, ySideTop, leftW + dx * 0.5f, 0f, leftW + dx, 0f)

            // Крыша высотки
            path.lineTo(w - rightW - dx, 0f)

            // Плавный спуск с высотки на правый узел
            path.cubicTo(w - rightW - dx * 0.5f, 0f, w - rightW - dx * 0.5f, ySideTop, w - rightW, ySideTop)

            // Правый узел
            path.lineTo(w - r, ySideTop)
            path.arcTo(Rect(w - 2 * r, ySideTop, w, ySideTop + 2 * r), 270f, 90f, false)
            path.lineTo(w, ySideBot - r)
            path.arcTo(Rect(w - 2 * r, ySideBot - 2 * r, w, ySideBot), 0f, 90f, false)
            path.lineTo(w - rightW, ySideBot)

            // Плавный спуск на дно высотки
            path.cubicTo(w - rightW - dx * 0.5f, ySideBot, w - rightW - dx * 0.5f, h, w - rightW - dx, h)

            // Дно высотки
            path.lineTo(leftW + dx, h)

            // Плавный подъем из дна высотки на левый узел
            path.cubicTo(leftW + dx * 0.5f, h, leftW + dx * 0.5f, ySideBot, leftW, ySideBot)

            path.lineTo(r, ySideBot)
            path.arcTo(Rect(0f, ySideBot - 2 * r, 2 * r, ySideBot), 90f, 90f, false)
            path.lineTo(0f, ySideTop + r)
            path.arcTo(Rect(0f, ySideTop, 2 * r, ySideTop + 2 * r), 180f, 90f, false)
            path.close()
        }

        return Outline.Generic(path)
    }
}

/**
 * 4. ХРОНОЛОГИЯ ПРИЕМОВ ПИЩИ: АДАПТИВНАЯ ПЛАШКА С ИКОНКАМИ ВРЕМЕНИ СУТОК И LED-ТОЧКАМИ
 */
@Composable
fun BiocodeMealItemRow(
    meal: MealEntry,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val tier = getMealCardTier(meal.name)
    val cardHeight = when (tier) {
        MealCardTier.SINGLE_LINE -> 52.dp
        MealCardTier.DOUBLE_LINE -> 76.dp
        MealCardTier.TRIPLE_LINE -> 98.dp
    }
    val shape = remember(tier) { BiocodeAdaptiveMealShape(tier = tier) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(cardHeight)
            .clip(shape)
            .background(BiocodePalette.DarkMoss)
            .border(1.2.dp, BiocodePalette.NoguchiBorder, shape)
            .clickable { onClick() }
            .padding(horizontal = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. ЛЕВЫЙ УЗЕЛ: Иконка типа приема пищи (вместо цифры)
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(BiocodePalette.SpruceDeck)
                    .border(1.dp, BiocodePalette.DeckBorder, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                when (meal.type) {
                    MealType.BREAKFAST -> LucideSun(modifier = Modifier.size(19.dp), tint = BiocodePalette.BioLime)
                    MealType.LUNCH -> LucideUtensils(modifier = Modifier.size(19.dp), tint = BiocodePalette.BioLime)
                    MealType.DINNER -> LucideMoon(modifier = Modifier.size(19.dp), tint = BiocodePalette.BioLime)
                    MealType.SNACK -> LucideApple(modifier = Modifier.size(19.dp), tint = BiocodePalette.BioLime)
                }
            }

            // Отступ от левого края ~2-3 мм для визуальной чистоты
            Spacer(modifier = Modifier.width(16.dp))

            // 2. ЦЕНТРАЛЬНАЯ ЧАСТЬ: Название блюда с адаптацией под 1, 2 или 3 строки
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp, end = 6.dp, top = 2.dp, bottom = 2.dp),
                verticalArrangement = Arrangement.Center
            ) {
                when (tier) {
                    MealCardTier.SINGLE_LINE -> {
                        Text(
                            text = meal.name,
                            style = BiocodeTypography.TabLabel.copy(
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = BiocodePalette.NoguchiCream,
                            maxLines = 1
                        )
                    }
                    MealCardTier.DOUBLE_LINE -> {
                        Text(
                            text = meal.name,
                            style = BiocodeTypography.TabLabel.copy(
                                fontSize = 12.sp,
                                lineHeight = 15.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = BiocodePalette.NoguchiCream,
                            maxLines = 2
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "Б:${meal.protein.toInt()}г  Ж:${meal.fat.toInt()}г  У:${meal.carbs.toInt()}г",
                            style = BiocodeTypography.TelemetryLabel.copy(fontSize = 8.5.sp),
                            color = BiocodePalette.BioLime
                        )
                    }
                    MealCardTier.TRIPLE_LINE -> {
                        Text(
                            text = meal.name,
                            style = BiocodeTypography.TabLabel.copy(
                                fontSize = 11.sp,
                                lineHeight = 14.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = BiocodePalette.NoguchiCream,
                            maxLines = 3
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "Б:${meal.protein.toInt()}г  Ж:${meal.fat.toInt()}г  У:${meal.carbs.toInt()}г",
                            style = BiocodeTypography.TelemetryLabel.copy(fontSize = 8.5.sp),
                            color = BiocodePalette.BioLime
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // 3. ПРАВЫЙ УЗЕЛ: Калории крупнее и в виде точек как в терминале
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                BiocodeDotMatrixText(
                    text = "${meal.calories}",
                    dotSize = 2.4.dp,
                    dotSpacing = 0.9.dp,
                    activeColor = BiocodePalette.BioLime,
                    inactiveColor = BiocodePalette.SpruceDeck.copy(alpha = 0.55f)
                )
                Text(
                    text = "KCAL",
                    style = BiocodeTypography.TelemetryLabel.copy(fontSize = 7.5.sp),
                    color = BiocodePalette.NoguchiCream.copy(alpha = 0.5f)
                )
            }
        }
    }
}

/**
 * 5. АДАПТИВНЫЙ СПИСОК ПРИЕМОВ ПИЩИ С УМНЫМИ ДИСТАНЦИЯМИ
 * Если блюдо занимает 1 строку — расстояние с соседним контейнером сокращается до 6.dp!
 */
@Composable
fun BiocodeMealList(
    meals: List<MealEntry>,
    modifier: Modifier = Modifier,
    onMealClick: (MealEntry) -> Unit = {}
) {
    Column(modifier = modifier) {
        meals.forEachIndexed { index, meal ->
            BiocodeMealItemRow(
                meal = meal,
                onClick = { onMealClick(meal) },
                modifier = Modifier.fillMaxWidth()
            )
            if (index < meals.lastIndex) {
                val currentTier = getMealCardTier(meal.name)
                val nextTier = getMealCardTier(meals[index + 1].name)
                // Если хотя бы одно из соседних блюд однострочное — расстояние сокращается
                val spacing = if (currentTier == MealCardTier.SINGLE_LINE || nextTier == MealCardTier.SINGLE_LINE) {
                    6.dp
                } else {
                    14.dp
                }
                Spacer(modifier = Modifier.height(spacing))
            }
        }
    }
}
