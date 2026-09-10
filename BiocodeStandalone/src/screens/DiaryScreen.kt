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
@Composable
fun DiaryScreen(
    state: DailyNutritionState,
    onAddMealClick: () -> Unit,
    onDeleteMeal: (String) -> Unit,
    onMealClick: (MealEntry) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

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
                    .padding(bottom = 12.dp),
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
                            text = state.dateLabel,
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
                // 1. СУММАРНЫЙ БАЛАНС СУТОК
                BiocodeBentoCard(
                    title = "СУТОЧНЫЙ ИТОГ",
                    badgeText = "${state.consumedCalories} / ${state.targetCalories} KCAL",
                    badgeColor = BiocodePalette.BioLime,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Калорийная полоса
                        val ratio = (state.consumedCalories.toFloat() / state.targetCalories.toFloat()).coerceIn(0f, 1f)
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
                                Text("${state.currentProteinGrams.toInt()} / ${state.targetProteinGrams.toInt()}г", style = BiocodeTypography.MonospaceTitle.copy(fontSize = 13.sp), color = BiocodePalette.BioLime)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("ЖИРЫ", style = BiocodeTypography.TelemetryLabel.copy(fontSize = 8.sp), color = BiocodePalette.MacroFat)
                                Text("${state.currentFatGrams.toInt()} / ${state.targetFatGrams.toInt()}г", style = BiocodeTypography.MonospaceTitle.copy(fontSize = 13.sp), color = BiocodePalette.MacroFat)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("УГЛЕВОДЫ", style = BiocodeTypography.TelemetryLabel.copy(fontSize = 8.sp), color = BiocodePalette.NoguchiCream)
                                Text("${state.currentCarbsGrams.toInt()} / ${state.targetCarbsGrams.toInt()}г", style = BiocodeTypography.MonospaceTitle.copy(fontSize = 13.sp), color = BiocodePalette.NoguchiCream)
                            }
                        }
                    }
                }

                // 2. ЗАГОЛОВОК СПИСКА ПРИЕМОВ ПИЩИ
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ХРОНОЛОГИЯ // ПРИЕМЫ ПИЩИ (${state.recentMeals.size})",
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

                // 3. СПИСОК ПРИЕМОВ ПИЩИ
                if (state.recentMeals.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
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
                                text = "ЖУРНАЛ СЕГОДНЯ ПУСТ",
                                style = BiocodeTypography.TabLabel,
                                color = BiocodePalette.NoguchiCream.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "Нажмите + для добавления первого блюда",
                                style = BiocodeTypography.TelemetryLabel.copy(fontSize = 8.5.sp),
                                color = BiocodePalette.BioLime
                            )
                        }
                    }
                } else {
                    BiocodeMealList(
                        meals = state.recentMeals,
                        onMealClick = onMealClick,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(86.dp))
            }
        }
    }
}
