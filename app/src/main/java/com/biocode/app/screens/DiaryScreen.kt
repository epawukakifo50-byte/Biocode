package com.biocode.app.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.biocode.engine.BiocodeBentoCard
import com.biocode.engine.BiocodeBlueprintCanvas
import com.biocode.engine.BiocodeMealItemRow
import com.biocode.engine.BiocodeMealList
import com.biocode.engine.BiocodePalette
import com.biocode.engine.BiocodeTypography
import com.biocode.engine.DailyNutritionState
import com.biocode.engine.LucideBookOpen
import com.biocode.engine.LucideCalendar
import com.biocode.engine.LucidePlus
import com.biocode.engine.LucideSparkles
import com.biocode.engine.MealEntry

/**
 * 📖 5. ЭКРАН «ДНЕВНИК» (NUTRITION DIARY & BIOCYCLE LOG)
 */
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * 📖 5. ЭКРАН «ДНЕВНИК» (NUTRITION DIARY & BIOCYCLE LOG) С КАЛЕНДАРЕМ-ХИТМАПОМ
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

    // Данные для выбранного дня
    val mealsForSelectedDate = remember(state.recentMeals, selectedDate) {
        state.recentMeals.filter { it.date == selectedDate }
    }
    val dayCalories = mealsForSelectedDate.sumOf { it.calories }
    val dayProtein = mealsForSelectedDate.sumOf { it.protein.toDouble() }.toFloat()
    val dayFat = mealsForSelectedDate.sumOf { it.fat.toDouble() }.toFloat()
    val dayCarbs = mealsForSelectedDate.sumOf { it.carbs.toDouble() }.toFloat()

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
            // КРУПНЫЙ ЗАГОЛОВОК BIOCODE ШРИФТОМ LIQUIDASI
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 4.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = "BIOCODE",
                    style = BiocodeTypography.HeaderBrand.copy(fontSize = 38.sp),
                    color = BiocodePalette.NoguchiCream
                )
            }

            // Навигация по дате
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ХРОНИКА МЕТАБОЛИЗМА // БИО-ЖУРНАЛ",
                    style = BiocodeTypography.TelemetryLabel,
                    color = BiocodePalette.BioLime
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(BiocodePalette.SpruceDeck)
                        .border(1.dp, BiocodePalette.DeckBorder, RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        LucideCalendar(modifier = Modifier.size(12.dp), tint = BiocodePalette.BioLime)
                        Text(
                            text = if (selectedDate == todayStr) "СЕГОДНЯ" else selectedDate,
                            style = BiocodeTypography.TelemetryLabel.copy(fontSize = 8.5.sp),
                            color = BiocodePalette.NoguchiCream
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. ТЕПЛОВАЯ КАРТА (CALENDAR HEATMAP 0% .. 200%)
                BiocodeCalendarHeatmap(
                    selectedDate = selectedDate,
                    onSelectDate = { selectedDate = it },
                    allMeals = state.recentMeals,
                    targetCalories = state.targetCalories
                )

                // 2. СУММАРНЫЙ БАЛАНС ВЫБРАННЫХ СУТОК
                BiocodeBentoCard(
                    title = "СУТОЧНЫЙ ИТОГ (${if (selectedDate == todayStr) "СЕГОДНЯ" else selectedDate})",
                    badgeText = "$dayCalories / ${state.targetCalories} KCAL",
                    badgeColor = if (dayCalories >= state.targetCalories) BiocodePalette.BioLime else BiocodePalette.NoguchiCream,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Калорийная полоса
                        val ratio = if (state.targetCalories > 0) (dayCalories.toFloat() / state.targetCalories.toFloat()).coerceIn(0f, 1f) else 0f
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
                                    .background(BiocodePalette.BioLime)
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
                        text = "ХРОНОЛОГИЯ ПРИЕМОВ ПИЩИ (${mealsForSelectedDate.size})",
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
                            .height(130.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(BiocodePalette.SpruceDeck)
                            .border(1.dp, BiocodePalette.DeckBorder, RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            LucideBookOpen(modifier = Modifier.size(28.dp), tint = BiocodePalette.BioLime.copy(alpha = 0.5f))
                            Text(
                                text = "В ЭТОТ ДЕНЬ НЕТ ЗАПИСЕЙ",
                                style = BiocodeTypography.TabLabel,
                                color = BiocodePalette.NoguchiCream.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "Выберите другой день в календаре или добавьте прием",
                                style = BiocodeTypography.TelemetryLabel.copy(fontSize = 8.5.sp),
                                color = BiocodePalette.BioLime
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
 * 🗓 БИОМОРФНЫЙ КАЛЕНДАРЬ-ХИТМАП (0%, 30%, 70%, 100%, 130%, 170%, 200%)
 */
@Composable
fun BiocodeCalendarHeatmap(
    selectedDate: String,
    onSelectDate: (String) -> Unit,
    allMeals: List<MealEntry>,
    targetCalories: Int,
    modifier: Modifier = Modifier
) {
    // Формируем 7 дней: от 6 дней назад до сегодняшнего
    val days = remember(allMeals, targetCalories) {
        (6 downTo 0).map { daysAgo ->
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -daysAgo)
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
            val dayName = SimpleDateFormat("EE", Locale("ru")).format(cal.time).uppercase().replace(".", "")
            val dayNumber = SimpleDateFormat("dd", Locale.getDefault()).format(cal.time)

            val totalCals = allMeals.filter { it.date == dateStr }.sumOf { it.calories }
            val pct = if (targetCalories > 0) ((totalCals.toFloat() / targetCalories.toFloat()) * 100).toInt() else 0

            val (tierPct, tierColor) = when {
                pct <= 5 -> 0 to Color(0xFF1B2625)
                pct in 6..45 -> 30 to Color(0xFF2C5645)
                pct in 46..85 -> 70 to Color(0xFF5A942E)
                pct in 86..115 -> 100 to BiocodePalette.BioLime
                pct in 116..150 -> 130 to Color(0xFFE4F524)
                pct in 151..185 -> 170 to BiocodePalette.LipidAmber
                else -> 200 to Color(0xFFFF3366) // 200%+
            }

            CalendarDayModel(
                dateStr = dateStr,
                dayName = dayName,
                dayNumber = dayNumber,
                actualPct = pct,
                tierPct = tierPct,
                color = tierColor,
                isToday = (daysAgo == 0)
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(BiocodePalette.SpruceDeck)
            .border(1.2.dp, BiocodePalette.DeckBorder, RoundedCornerShape(18.dp))
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Заголовок хитмапа
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "МАТРИЦА КАЛОРИЙ // ТЕПЛОВАЯ КАРТА",
                    style = BiocodeTypography.TelemetryLabel.copy(fontSize = 8.5.sp),
                    color = BiocodePalette.BioLime
                )
                Text(
                    text = "7-ДНЕВНЫЙ ЦИКЛ",
                    style = BiocodeTypography.TelemetryLabel.copy(fontSize = 8.sp),
                    color = BiocodePalette.NoguchiCream.copy(alpha = 0.6f)
                )
            }

            // Ряд 7 плашек дней
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                days.forEach { day ->
                    val isSelected = (day.dateStr == selectedDate)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) BiocodePalette.PineTeal else BiocodePalette.DarkMoss.copy(alpha = 0.6f))
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) BiocodePalette.BioLime else BiocodePalette.DeckBorder.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { onSelectDate(day.dateStr) }
                            .padding(vertical = 8.dp, horizontal = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Text(
                                text = day.dayName,
                                style = BiocodeTypography.TelemetryLabel.copy(fontSize = 7.5.sp),
                                color = if (day.isToday) BiocodePalette.BioLime else BiocodePalette.NoguchiCream.copy(alpha = 0.6f),
                                fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Normal
                            )

                            Text(
                                text = day.dayNumber,
                                style = BiocodeTypography.MonospaceTitle.copy(fontSize = 11.sp),
                                color = if (isSelected) BiocodePalette.BioLime else BiocodePalette.NoguchiCream,
                                fontWeight = FontWeight.Bold
                            )

                            // Плашка тепловой карты с градиентным био-цветом
                            Box(
                                modifier = Modifier
                                    .size(width = 24.dp, height = 12.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(day.color)
                                    .border(
                                        0.5.dp,
                                        if (day.tierPct == 0) BiocodePalette.DeckBorder.copy(alpha = 0.5f) else day.color.copy(alpha = 0.9f),
                                        RoundedCornerShape(4.dp)
                                    )
                            )

                            // Текстовый процент выполнения
                            Text(
                                text = "${day.tierPct}%",
                                style = BiocodeTypography.TelemetryLabel.copy(fontSize = 7.5.sp),
                                color = if (day.tierPct >= 100) day.color else BiocodePalette.NoguchiCream.copy(alpha = 0.7f),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Шкала-легенда градиента
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(BiocodePalette.DarkMoss.copy(alpha = 0.5f))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "0% ПУСТО",
                    style = BiocodeTypography.TelemetryLabel.copy(fontSize = 7.sp),
                    color = Color(0xFF6B807B)
                )
                Text(
                    text = "• 30% • 70%",
                    style = BiocodeTypography.TelemetryLabel.copy(fontSize = 7.sp),
                    color = Color(0xFF5A942E)
                )
                Text(
                    text = "100% НОРМА",
                    style = BiocodeTypography.TelemetryLabel.copy(fontSize = 7.sp),
                    color = BiocodePalette.BioLime,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "130% • 170% • 200%",
                    style = BiocodeTypography.TelemetryLabel.copy(fontSize = 7.sp),
                    color = Color(0xFFFF3366)
                )
            }
        }
    }
}

private data class CalendarDayModel(
    val dateStr: String,
    val dayName: String,
    val dayNumber: String,
    val actualPct: Int,
    val tierPct: Int,
    val color: Color,
    val isToday: Boolean
)
