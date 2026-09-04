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
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import com.biocode.engine.BiocodePalette
import com.biocode.engine.BiocodeTypography
import com.biocode.engine.DailyNutritionState
import com.biocode.engine.LucideCalculator
import com.biocode.engine.LucideCheck
import com.biocode.engine.LucideFlame
import com.biocode.engine.LucideTrendingUp

/**
 * 🧮 4. ЭКРАН «РАСЧЕТ» (CALCULATOR & METABOLIC PROFILER)
 */
@Composable
fun CalculatorScreen(
    state: DailyNutritionState,
    onUpdateTargets: (calories: Int, protein: Float, fat: Float, carbs: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    var selectedGoalIndex by remember { mutableIntStateOf(1) } // 0 = дефицит, 1 = баланс, 2 = профицит
    var targetCals by remember { mutableIntStateOf(state.targetCalories) }
    var proteinRatio by remember { mutableFloatStateOf(2.0f) } // г на кг
    var weightKg by remember { mutableFloatStateOf(80f) }

    val bmr = (10 * weightKg + 6.25f * 180f - 5 * 28 + 5).toInt() // Mifflin-St Jeor = ~1790
    val tdee = (bmr * 1.35f).toInt() // ~2416

    val calcProtein = (weightKg * proteinRatio).coerceAtLeast(60f)
    val calcFat = (weightKg * 0.9f).coerceAtLeast(40f)
    val remainingCalsForCarbs = (targetCals - (calcProtein * 4 + calcFat * 9)).coerceAtLeast(200f)
    val calcCarbs = (remainingCalsForCarbs / 4f)

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

            Text(
                text = "БИОМЕТРИЧЕСКИЙ КАЛЬКУЛЯТОР // BMR & TDEE",
                style = BiocodeTypography.TelemetryLabel,
                color = BiocodePalette.BioLime,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. ЦЕЛЕВОЙ МЕТАБОЛИЧЕСКИЙ РЕЖИМ (Pills)
                BiocodeBentoCard(
                    title = "МЕТАБОЛИЧЕСКАЯ СТРАТЕГИЯ",
                    badgeText = "MIFFLIN-ST JEOR",
                    badgeColor = BiocodePalette.BioLime,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val goals = listOf(
                                "ДЕФИЦИТ (-15%)" to (tdee * 0.85f).toInt(),
                                "ГОМЕОСТАЗ" to tdee,
                                "ПРОФИЦИТ (+15%)" to (tdee * 1.15f).toInt()
                            )

                            goals.forEachIndexed { idx, (label, cals) ->
                                val isSelected = selectedGoalIndex == idx
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) BiocodePalette.BioLime else BiocodePalette.SpruceDeck)
                                        .border(
                                            1.dp,
                                            if (isSelected) BiocodePalette.BioLime else BiocodePalette.DeckBorder,
                                            RoundedCornerShape(12.dp)
                                        )
                                        .clickable {
                                            selectedGoalIndex = idx
                                            targetCals = cals
                                        }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = label,
                                            style = BiocodeTypography.TelemetryLabel.copy(fontSize = 7.5.sp),
                                            color = if (isSelected) BiocodePalette.DarkMoss else BiocodePalette.NoguchiCream,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = "$cals",
                                            style = BiocodeTypography.MonospaceTitle.copy(fontSize = 13.sp),
                                            color = if (isSelected) BiocodePalette.DarkMoss else BiocodePalette.BioLime
                                        )
                                    }
                                }
                            }
                        }

                        // Текущий расчет
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(BiocodePalette.DarkMoss)
                                .border(1.dp, BiocodePalette.NoguchiBorder, RoundedCornerShape(14.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "БАЗОВЫЙ ОБМЕН (BMR)",
                                    style = BiocodeTypography.TelemetryLabel.copy(fontSize = 8.sp),
                                    color = BiocodePalette.NoguchiCream.copy(alpha = 0.6f)
                                )
                                Text(
                                    text = "$bmr ккал/сутки",
                                    style = BiocodeTypography.MonospaceTitle.copy(fontSize = 14.sp),
                                    color = BiocodePalette.NoguchiCream
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "АКТИВНОСТЬ (TDEE)",
                                    style = BiocodeTypography.TelemetryLabel.copy(fontSize = 8.sp),
                                    color = BiocodePalette.NoguchiCream.copy(alpha = 0.6f)
                                )
                                Text(
                                    text = "$tdee ккал/сутки",
                                    style = BiocodeTypography.MonospaceTitle.copy(fontSize = 14.sp),
                                    color = BiocodePalette.BioLime
                                )
                            }
                        }
                    }
                }

                // 2. ДЕТАЛЬНАЯ НАСТРОЙКА КБЖУ
                BiocodeBentoCard(
                    title = "РАСПРЕДЕЛЕНИЕ МАКРОНУТРИЕНТОВ",
                    badgeText = "$targetCals KCAL",
                    badgeColor = BiocodePalette.BioLime,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Слайдер калорийности
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "ЦЕЛЬ КАЛОРИЙ",
                                style = BiocodeTypography.TelemetryLabel,
                                color = BiocodePalette.NoguchiCream
                            )
                            Text(
                                text = "$targetCals ккал",
                                style = BiocodeTypography.MonospaceTitle.copy(fontSize = 14.sp),
                                color = BiocodePalette.BioLime
                            )
                        }

                        Slider(
                            value = targetCals.toFloat(),
                            onValueChange = { targetCals = it.toInt() },
                            valueRange = 1400f..3500f,
                            steps = 41,
                            colors = SliderDefaults.colors(
                                thumbColor = BiocodePalette.BioLime,
                                activeTrackColor = BiocodePalette.BioLime,
                                inactiveTrackColor = BiocodePalette.PineTeal
                            )
                        )

                        // 3-х цветная полоса баланса макросов
                        val pKcal = calcProtein * 4f
                        val fKcal = calcFat * 9f
                        val cKcal = calcCarbs * 4f
                        val totalKcal = (pKcal + fKcal + cKcal).coerceAtLeast(1f)

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(BiocodePalette.DarkMoss)
                        ) {
                            Row(modifier = Modifier.fillMaxSize()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .weight((pKcal / totalKcal).coerceAtLeast(0.05f))
                                        .background(BiocodePalette.BioLime)
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .weight((fKcal / totalKcal).coerceAtLeast(0.05f))
                                        .background(BiocodePalette.MacroFat)
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .weight((cKcal / totalKcal).coerceAtLeast(0.05f))
                                        .background(BiocodePalette.NoguchiCream)
                                )
                            }
                        }

                        // Показатели БЖУ
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("БЕЛОК (2.0г/кг)", style = BiocodeTypography.TelemetryLabel.copy(fontSize = 8.sp), color = BiocodePalette.BioLime)
                                Text("${calcProtein.toInt()}г", style = BiocodeTypography.MonospaceTitle.copy(fontSize = 16.sp), color = BiocodePalette.BioLime)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("ЖИРЫ (0.9г/кг)", style = BiocodeTypography.TelemetryLabel.copy(fontSize = 8.sp), color = BiocodePalette.MacroFat)
                                Text("${calcFat.toInt()}г", style = BiocodeTypography.MonospaceTitle.copy(fontSize = 16.sp), color = BiocodePalette.MacroFat)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("УГЛЕВОДЫ", style = BiocodeTypography.TelemetryLabel.copy(fontSize = 8.sp), color = BiocodePalette.NoguchiCream)
                                Text("${calcCarbs.toInt()}г", style = BiocodeTypography.MonospaceTitle.copy(fontSize = 16.sp), color = BiocodePalette.NoguchiCream)
                            }
                        }
                    }
                }

                // 3. БИОРИТМИЧЕСКИЕ ОКНА ПРИЕМА ПИЩИ
                BiocodeBentoCard(
                    title = "БИОРИТМ // ТАЙМИНГ ФАЗ",
                    badgeText = "ФАЗЫ 01-03",
                    badgeColor = BiocodePalette.BioLime,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("ФАЗА 01 // УТРО (08:00 - 10:00)", style = BiocodeTypography.TelemetryLabel, color = BiocodePalette.NoguchiCream)
                            Text("25% КБЖУ", style = BiocodeTypography.TelemetryLabel, color = BiocodePalette.BioLime)
                        }
                        Text(
                            text = "Активация метаболизма: сложные полисахариды + гидролизат протеина.",
                            style = BiocodeTypography.TelemetryLabel.copy(fontSize = 8.5.sp),
                            color = BiocodePalette.NoguchiCream.copy(alpha = 0.7f)
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("ФАЗА 02 // ПИК (13:00 - 15:00)", style = BiocodeTypography.TelemetryLabel, color = BiocodePalette.NoguchiCream)
                            Text("45% КБЖУ", style = BiocodeTypography.TelemetryLabel, color = BiocodePalette.BioLime)
                        }
                        Text(
                            text = "Максимальный синтез гликогена: основной объем белков и полиненасыщенных липидов.",
                            style = BiocodeTypography.TelemetryLabel.copy(fontSize = 8.5.sp),
                            color = BiocodePalette.NoguchiCream.copy(alpha = 0.7f)
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("ФАЗА 03 // ВЕЧЕР (19:00 - 21:00)", style = BiocodeTypography.TelemetryLabel, color = BiocodePalette.NoguchiCream)
                            Text("30% КБЖУ", style = BiocodeTypography.TelemetryLabel, color = BiocodePalette.BioLime)
                        }
                        Text(
                            text = "Регенерация: чистые аминокислоты, магний и клетчатка без резких инсулиновых пиков.",
                            style = BiocodeTypography.TelemetryLabel.copy(fontSize = 8.5.sp),
                            color = BiocodePalette.NoguchiCream.copy(alpha = 0.7f)
                        )
                    }
                }

                // КНОПКА ПРИМЕНЕНИЯ ЦЕЛЕЙ
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(25.dp))
                        .background(BiocodePalette.BioLime)
                        .clickable {
                            onUpdateTargets(targetCals, calcProtein, calcFat, calcCarbs)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LucideCheck(modifier = Modifier.size(18.dp), tint = BiocodePalette.DarkMoss)
                        Text(
                            text = "ПРИМЕНИТЬ МЕТАБОЛИЧЕСКИЙ ПРОФИЛЬ",
                            style = BiocodeTypography.TabLabel,
                            color = BiocodePalette.DarkMoss,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(86.dp))
            }
        }
    }
}
