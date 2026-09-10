package com.biocode.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.biocode.engine.BiocodeBentoCard
import com.biocode.engine.BiocodeBlueprintCanvas
import com.biocode.engine.BiocodeMealList
import com.biocode.engine.BiocodePalette
import com.biocode.engine.BiocodeTypography
import com.biocode.engine.DailyNutritionState
import com.biocode.engine.LucideBookOpen
import com.biocode.engine.LucideCalendar
import com.biocode.engine.LucidePlus
import com.biocode.engine.MealEntry
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * 📖 5. ЭКРАН «ДНЕВНИК» (NUTRITION DIARY & BIOCYCLE LOG)
 *
 * Содержит 12-недельную тепловую карту (84 дня = 12 столбцов x 7 строк)
 * в стиле GitHub contribution matrix без цифр внутри ячеек,
 * с чистой цветовой индикацией соответствия калоражу.
 */
@Composable
fun DiaryScreen(
    state: DailyNutritionState,
    onAddMealClick: () -> Unit,
    onDeleteMeal: (String) -> Unit,
    onMealClick: (MealEntry) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    var selectedDate by remember { mutableStateOf(todayStr) }

    // Данные для выбранного пользователем дня
    val mealsForSelectedDate = remember(state.recentMeals, selectedDate) {
        state.recentMeals.filter { it.date == selectedDate }
    }
    val dayCalories = mealsForSelectedDate.sumOf { it.calories }
    val dayProtein = mealsForSelectedDate.sumOf { it.protein.toDouble() }.toFloat()
    val dayFat = mealsForSelectedDate.sumOf { it.fat.toDouble() }.toFloat()
    val dayCarbs = mealsForSelectedDate.sumOf { it.carbs.toDouble() }.toFloat()

    // Форматированная строка даты
    val formattedSelectedDate = remember(selectedDate) {
        try {
            val d = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(selectedDate)
            if (d != null) {
                val cal = Calendar.getInstance().apply { time = d }
                val dayOfWeekRu = when (cal.get(Calendar.DAY_OF_WEEK)) {
                    Calendar.MONDAY -> "ПОНЕДЕЛЬНИК"
                    Calendar.TUESDAY -> "ВТОРНИК"
                    Calendar.WEDNESDAY -> "СРЕДА"
                    Calendar.THURSDAY -> "ЧЕТВЕРГ"
                    Calendar.FRIDAY -> "ПЯТНИЦА"
                    Calendar.SATURDAY -> "СУББОТА"
                    Calendar.SUNDAY -> "ВОСКРЕСЕНЬЕ"
                    else -> ""
                }
                val datePart = SimpleDateFormat("dd MMMM yyyy", Locale("ru")).format(d).uppercase()
                if (selectedDate == todayStr) "$datePart • $dayOfWeekRu [СЕГОДНЯ]" else "$datePart • $dayOfWeekRu"
            } else {
                selectedDate
            }
        } catch (e: Exception) {
            selectedDate
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BiocodePalette.DarkMoss)
    ) {
        BiocodeBlueprintCanvas(
            modifier = Modifier.fillMaxSize(),
            gridPitchDp = 24.dp,
            lineColor = BiocodePalette.PineTeal.copy(alpha = 0.25f),
            crossColor = BiocodePalette.BioLime.copy(alpha = 0.20f)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            // КРУПНЫЙ ЗАГОЛОВОК BIOCODE
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 10.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "BIOCODE",
                        style = BiocodeTypography.HeaderBrand.copy(fontSize = 38.sp),
                        color = BiocodePalette.NoguchiCream
                    )

                    // Бейдж перехода к сегодняшнему дню
                    if (selectedDate != todayStr) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(BiocodePalette.SpruceDeck)
                                .border(1.dp, BiocodePalette.BioLime, RoundedCornerShape(16.dp))
                                .clickable { selectedDate = todayStr }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                LucideCalendar(modifier = Modifier.size(12.dp), tint = BiocodePalette.BioLime)
                                Text(
                                    text = "СЕГОДНЯ",
                                    style = BiocodeTypography.TelemetryLabel.copy(
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = BiocodePalette.BioLime
                                )
                            }
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. ТЕПЛОВАЯ КАРТА 12 НЕДЕЛЬ (84 ЯЧЕЙКИ/ДНЕЙ) БЕЗ ЦИФР ВНУТРИ
                Biocode12WeekHeatmapMatrix(
                    selectedDate = selectedDate,
                    onSelectDate = { selectedDate = it },
                    allMeals = state.recentMeals,
                    targetCalories = state.targetCalories,
                    todayStr = todayStr,
                    modifier = Modifier.fillMaxWidth()
                )

                // 2. СУММАРНЫЙ БАЛАНС ВЫБРАННЫХ СУТОК
                BiocodeBentoCard(
                    title = "СУТОЧНЫЙ ИТОГ // $formattedSelectedDate",
                    badgeText = "$dayCalories / ${state.targetCalories} KCAL",
                    badgeColor = if (dayCalories >= state.targetCalories) BiocodePalette.BioLime else BiocodePalette.NoguchiCream,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Калорийная полоса
                        val ratio = if (state.targetCalories > 0) (dayCalories.toFloat() / state.targetCalories.toFloat()).coerceIn(0f, 1f) else 0f
                        val progressColor = when {
                            dayCalories == 0 -> Color(0xFF2C3E3B)
                            dayCalories < (state.targetCalories * 0.8f) -> BiocodePalette.PineTeal
                            dayCalories <= (state.targetCalories * 1.15f) -> BiocodePalette.BioLime
                            else -> BiocodePalette.LipidAmber
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(BiocodePalette.DarkMoss)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(ratio)
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(progressColor)
                            )
                        }

                        // Сводка БЖУ
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("БЕЛОК", style = BiocodeTypography.TelemetryLabel.copy(fontSize = 8.sp), color = BiocodePalette.BioLime)
                                Text("${dayProtein.toInt()} / ${state.targetProteinGrams.toInt()}г", style = BiocodeTypography.MonospaceTitle.copy(fontSize = 13.sp), color = BiocodePalette.BioLime)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("ЖИРЫ", style = BiocodeTypography.TelemetryLabel.copy(fontSize = 8.sp), color = BiocodePalette.MacroFat)
                                Text("${dayFat.toInt()} / ${state.targetFatGrams.toInt()}г", style = BiocodeTypography.MonospaceTitle.copy(fontSize = 13.sp), color = BiocodePalette.MacroFat)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("УГЛЕВОДЫ", style = BiocodeTypography.TelemetryLabel.copy(fontSize = 8.sp), color = BiocodePalette.NoguchiCream)
                                Text("${dayCarbs.toInt()} / ${state.targetCarbsGrams.toInt()}г", style = BiocodeTypography.MonospaceTitle.copy(fontSize = 13.sp), color = BiocodePalette.NoguchiCream)
                            }
                        }
                    }
                }

                // 3. ЗАГОЛОВОК СПИСКА ПРИЕМОВ ПИЩИ
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ПРИЕМЫ ПИЩИ ЗА ДЕНЬ (${mealsForSelectedDate.size})",
                        style = BiocodeTypography.TabLabel,
                        color = BiocodePalette.NoguchiCream.copy(alpha = 0.9f)
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(BiocodePalette.BioLime)
                            .clickable { onAddMealClick() }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            LucidePlus(modifier = Modifier.size(12.dp), tint = BiocodePalette.DarkMoss)
                            Text(
                                text = "ДОБАВИТЬ",
                                style = BiocodeTypography.TelemetryLabel.copy(fontSize = 8.5.sp),
                                color = BiocodePalette.DarkMoss,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // 4. СПИСОК ПРИЕМОВ ПИЩИ ЗА ВЫБРАННЫЙ ДЕНЬ
                if (mealsForSelectedDate.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(BiocodePalette.SpruceDeck)
                            .border(1.dp, BiocodePalette.DeckBorder, RoundedCornerShape(20.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            LucideBookOpen(modifier = Modifier.size(24.dp), tint = BiocodePalette.PineTeal)
                            Text(
                                text = "НЕТ ЗАПИСЕЙ ЗА ЭТУ ДАТУ",
                                style = BiocodeTypography.MonospaceTitle.copy(fontSize = 12.sp),
                                color = BiocodePalette.NoguchiCream
                            )
                            Text(
                                text = "Нажмите «+ ДОБАВИТЬ» для внесения блюда в этот день",
                                style = BiocodeTypography.TelemetryLabel.copy(fontSize = 8.5.sp),
                                color = BiocodePalette.NoguchiCream.copy(alpha = 0.55f)
                            )
                        }
                    }
                } else {
                    BiocodeMealList(
                        meals = mealsForSelectedDate,
                        onMealClick = onMealClick,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(86.dp))
            }
        }
    }
}

/**
 * 🗓 12-НЕДЕЛЬНАЯ МАТРИЦА ТЕПЛОВОЙ КАРТЫ (84 ДНЯ = 12 СТОЛБЦОВ x 7 СТРОК)
 *
 * Каждый столбец — отдельная неделя (сверху вниз: ПН .. ВС).
 * Внутри ячеек нет цифр, только чистые цветные тайлы в зависимости от калоража.
 */
@Composable
fun Biocode12WeekHeatmapMatrix(
    selectedDate: String,
    onSelectDate: (String) -> Unit,
    allMeals: List<MealEntry>,
    targetCalories: Int,
    todayStr: String,
    modifier: Modifier = Modifier
) {
    val dayLabels = listOf("ПН", "ВТ", "СР", "ЧТ", "ПТ", "СБ", "ВС")

    // Вычисляем сетку из 12 недель (84 дня)
    val columnsData = remember(allMeals, targetCalories, todayStr) {
        val todayCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // Понедельник текущей недели
        val dow = todayCal.get(Calendar.DAY_OF_WEEK)
        val daysFromMon = when (dow) {
            Calendar.MONDAY -> 0
            Calendar.TUESDAY -> 1
            Calendar.WEDNESDAY -> 2
            Calendar.THURSDAY -> 3
            Calendar.FRIDAY -> 4
            Calendar.SATURDAY -> 5
            Calendar.SUNDAY -> 6
            else -> 0
        }

        val currentMonday = (todayCal.clone() as Calendar).apply {
            add(Calendar.DAY_OF_YEAR, -daysFromMon)
        }

        // 11 недель до текущего понедельника (итого 12 недель)
        val startGridMonday = (currentMonday.clone() as Calendar).apply {
            add(Calendar.WEEK_OF_YEAR, -11)
        }

        (0 until 12).map { colIndex ->
            (0 until 7).map { rowIndex ->
                val cellCal = (startGridMonday.clone() as Calendar).apply {
                    add(Calendar.DAY_OF_YEAR, colIndex * 7 + rowIndex)
                }
                val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cellCal.time)
                val isToday = (dateStr == todayStr)
                val isFuture = cellCal.after(todayCal)

                val dayCals = allMeals.filter { it.date == dateStr }.sumOf { it.calories }
                val pct = if (targetCalories > 0) ((dayCals.toFloat() / targetCalories.toFloat()) * 100).toInt() else 0

                val cellColor = when {
                    isFuture -> Color(0xFF152220).copy(alpha = 0.35f)
                    dayCals == 0 -> Color(0xFF172824) // 0% незакрашенная / пустая плашка
                    pct <= 45 -> Color(0xFF1E4638)    // 30%
                    pct <= 85 -> Color(0xFF387A54)    // 70%
                    pct <= 115 -> BiocodePalette.BioLime // 100% НОРМА
                    pct <= 150 -> Color(0xFFD4E622)   // 130%
                    pct <= 185 -> BiocodePalette.LipidAmber // 170%
                    else -> Color(0xFFFF2255)         // 200%+
                }

                HeatmapCellModel(
                    dateStr = dateStr,
                    isToday = isToday,
                    isFuture = isFuture,
                    calories = dayCals,
                    adherencePct = pct,
                    color = cellColor
                )
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(BiocodePalette.SpruceDeck)
            .border(1.2.dp, BiocodePalette.DeckBorder, RoundedCornerShape(20.dp))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Заголовок хитмапа
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(BiocodePalette.BioLime)
                    )
                    Text(
                        text = "МАТРИЦА КАЛОРИЙ // 12 НЕДЕЛЬ (84 ДНЯ)",
                        style = BiocodeTypography.TelemetryLabel.copy(
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = BiocodePalette.BioLime
                    )
                }

                Text(
                    text = "НОРМА: $targetCalories KCAL",
                    style = BiocodeTypography.TelemetryLabel.copy(fontSize = 8.sp),
                    color = BiocodePalette.NoguchiCream.copy(alpha = 0.6f)
                )
            }

            // САМА 12-НЕДЕЛЬНАЯ СЕТКА (12 СТОЛБЦОВ x 7 СТРОК)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Левая колонка: дни недели (ПН .. ВС)
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.5.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    dayLabels.forEach { label ->
                        Box(
                            modifier = Modifier
                                .size(width = 16.dp, height = 18.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = BiocodeTypography.TelemetryLabel.copy(
                                    fontSize = 7.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = BiocodePalette.NoguchiCream.copy(alpha = 0.5f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                // 12 столбцов недель
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    columnsData.forEach { columnDays ->
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.5.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            columnDays.forEach { cell ->
                                val isSelected = (cell.dateStr == selectedDate)

                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(RoundedCornerShape(3.5.dp))
                                        .background(cell.color)
                                        .border(
                                            width = if (isSelected) 1.5.dp else if (cell.isToday) 1.dp else 0.6.dp,
                                            color = when {
                                                isSelected -> BiocodePalette.NoguchiCream
                                                cell.isToday -> BiocodePalette.BioLime
                                                else -> BiocodePalette.DeckBorderSubtle
                                            },
                                            shape = RoundedCornerShape(3.5.dp)
                                        )
                                        .clickable {
                                            if (!cell.isFuture) {
                                                onSelectDate(cell.dateStr)
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    // Маркер для сегодняшнего дня (микро-точка)
                                    if (cell.isToday) {
                                        Box(
                                            modifier = Modifier
                                                .size(3.5.dp)
                                                .clip(CircleShape)
                                                .background(if (isSelected) BiocodePalette.NoguchiCream else BiocodePalette.BioLime)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ЛЕГЕНДА СООТВЕТСТВИЯ НОРМЕ (0%, 30%, 70%, 100%, 130%, 170%, 200%)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val tiers = listOf(
                    "0%" to Color(0xFF172824),
                    "30%" to Color(0xFF1E4638),
                    "70%" to Color(0xFF387A54),
                    "100%" to BiocodePalette.BioLime,
                    "130%" to Color(0xFFD4E622),
                    "170%" to BiocodePalette.LipidAmber,
                    "200%" to Color(0xFFFF2255)
                )

                tiers.forEach { (lbl, col) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(col)
                                .border(0.5.dp, BiocodePalette.DeckBorderSubtle, RoundedCornerShape(2.dp))
                        )
                        Text(
                            text = lbl,
                            style = BiocodeTypography.TelemetryLabel.copy(
                                fontSize = 7.sp,
                                fontWeight = if (lbl == "100%") FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (lbl == "100%") BiocodePalette.BioLime else BiocodePalette.NoguchiCream.copy(alpha = 0.65f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Модель ячейки 12-недельного хитмапа.
 */
data class HeatmapCellModel(
    val dateStr: String,
    val isToday: Boolean,
    val isFuture: Boolean,
    val calories: Int,
    val adherencePct: Int,
    val color: Color
)
