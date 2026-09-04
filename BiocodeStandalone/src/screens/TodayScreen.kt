package com.biocode.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.biocode.engine.BiocodeBlueprintCanvas
import com.biocode.engine.BiocodeCompoundBioHub
import com.biocode.engine.BiocodeMacroBentoModule
import com.biocode.engine.BiocodeMealItemRow
import com.biocode.engine.BiocodeMealList
import com.biocode.engine.BiocodePalette
import com.biocode.engine.BiocodeTypography
import com.biocode.engine.DailyNutritionState
import com.biocode.engine.MealEntry
import com.biocode.engine.MealType

/**
 * 🌟 1. ЭКРАН «СЕГОДНЯ» (TODAY DASHBOARD)
 *
 * Сверху крупная надпись BIOCODE фирменным шрифтом LIQUIDASI.
 * Метаболический хаб с LED-матрицей, БЖУ-модуль, таймлайн приемов пищи.
 */
@Composable
fun TodayScreen(
    state: DailyNutritionState,
    onStateUpdate: (DailyNutritionState) -> Unit,
    onThemeToggle: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BiocodePalette.DarkMoss)
    ) {
        // Фоновая инженерная сетка
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
                    .padding(top = 4.dp, bottom = 10.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = "BIOCODE",
                    style = BiocodeTypography.HeaderBrand.copy(fontSize = 38.sp),
                    color = BiocodePalette.NoguchiCream
                )
            }

            // СКРОЛЛЯЩИЙСЯ КОНТЕНТ
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. МЕТАБОЛИЧЕСКИЙ ХАБ С ВЫХОДАМИ (РЕФЕРЕНС 4)
                BiocodeCompoundBioHub(
                    state = state,
                    onProteinBoost = {
                        onStateUpdate(
                            state.copy(
                                currentProteinGrams = state.currentProteinGrams + 25f,
                                consumedCalories = state.consumedCalories + 100
                            )
                        )
                    },
                    onScanClick = {
                        val newMeal = MealEntry(
                            id = System.currentTimeMillis().toString(),
                            name = "Боул с лососем и киноа",
                            type = MealType.LUNCH,
                            calories = 490,
                            protein = 44f,
                            fat = 16f,
                            carbs = 42f,
                            time = "14:15"
                        )
                        onStateUpdate(
                            state.copy(
                                consumedCalories = state.consumedCalories + newMeal.calories,
                                currentProteinGrams = state.currentProteinGrams + newMeal.protein,
                                currentFatGrams = state.currentFatGrams + newMeal.fat,
                                currentCarbsGrams = state.currentCarbsGrams + newMeal.carbs,
                                recentMeals = listOf(newMeal) + state.recentMeals
                            )
                        )
                    },
                    onThemeToggle = onThemeToggle,
                    modifier = Modifier.fillMaxWidth()
                )

                // 2. БЕНТО-МОДУЛЬ МАКРОНУТРИЕНТОВ БЖУ (БЕЗ ГИДРАТАЦИИ!)
                BiocodeMacroBentoModule(
                    proteinG = state.currentProteinGrams,
                    targetProteinG = state.targetProteinGrams,
                    fatG = state.currentFatGrams,
                    targetFatG = state.targetFatGrams,
                    carbsG = state.currentCarbsGrams,
                    targetCarbsG = state.targetCarbsGrams,
                    modifier = Modifier.fillMaxWidth()
                )

                // 3. ХРОНОЛОГИЯ ПРИЕМОВ ПИЩИ
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ПРИЕМЫ ПИЩИ // БИОЦИКЛ",
                        style = BiocodeTypography.TabLabel,
                        color = BiocodePalette.NoguchiCream.copy(alpha = 0.9f)
                    )
                    Text(
                        text = "ФАЗА 02",
                        style = BiocodeTypography.TelemetryLabel,
                        color = BiocodePalette.BioLime
                    )
                }

                BiocodeMealList(
                    meals = state.recentMeals,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(86.dp)) // Отступ под нижний бар
            }
        }
    }
}
