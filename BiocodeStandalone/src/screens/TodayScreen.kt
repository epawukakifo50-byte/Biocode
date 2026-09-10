package com.biocode.app.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.biocode.engine.BiocodeBlueprintCanvas
import com.biocode.engine.BiocodeCompoundBioHub
import com.biocode.engine.BiocodeDotMatrixText
import com.biocode.engine.BiocodeMacroBentoModule
import com.biocode.engine.BiocodeMealList
import com.biocode.engine.BiocodePalette
import com.biocode.engine.BiocodeTypography
import com.biocode.engine.DailyNutritionState
import com.biocode.engine.ExerciseItem
import com.biocode.engine.HubMode
import com.biocode.engine.LucideCheck
import com.biocode.engine.LucideDumbbell
import com.biocode.engine.LucideFlame
import com.biocode.engine.LucidePlus
import com.biocode.engine.LucideTrendingUp
import com.biocode.engine.LucideUtensils
import com.biocode.engine.LucideZap
import com.biocode.engine.MealEntry
import com.biocode.engine.WorkoutSession
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 🌟 1. ЭКРАН «СЕГОДНЯ» (TODAY DASHBOARD)
 *
 * Поддерживает два взаимосвязанных режима:
 * - METABOLIC HUB: Питание, нутриенты, БЖУ, дневной энергобаланс, приемы пищи за сегодня.
 * - KINETIC HUB: Тренировочные комплексы, энергозатраты на активность, упражнения, ИИ расчет расхода.
 */
@Composable
fun TodayScreen(
    state: DailyNutritionState,
    onStateUpdate: (DailyNutritionState) -> Unit,
    onMealClick: (MealEntry) -> Unit = {},
    onAddMealWithPhoto: () -> Unit = {},
    onThemeToggle: () -> Unit = {},
    hubMode: HubMode = HubMode.METABOLIC,
    onToggleHubMode: () -> Unit = {},
    bmrCalories: Int = 1750,
    workoutBurnCalories: Int = 460,
    kineticWorkouts: List<WorkoutSession> = emptyList(),
    onAddActivityClick: () -> Unit = {},
    onExerciseClick: ((ExerciseItem) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val todayDate = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }

    // Фильтрация приемов пищи СТРОГО за сегодняшний день (Требование 1.1)
    val todayMeals = remember(state.recentMeals, todayDate) {
        state.recentMeals.filter { it.date == todayDate }
    }

    // Фильтрация тренировочных сессий за сегодняшний день (Требование 2.1)
    val todayWorkouts = remember(kineticWorkouts, todayDate) {
        kineticWorkouts.filter { it.date == todayDate }
    }

    val dynamicTodayBurn = if (todayWorkouts.isNotEmpty()) {
        todayWorkouts.sumOf { it.totalCaloriesBurned }
    } else {
        workoutBurnCalories
    }

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
            // КРУПНЫЙ ЗАГОЛОВОК BIOCODE + ПЕРЕКЛЮЧАТЕЛЬ ХАБОВ
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "BIOCODE",
                    style = BiocodeTypography.HeaderBrand.copy(fontSize = 34.sp),
                    color = BiocodePalette.NoguchiCream
                )

                // ПЕРЕКЛЮЧАТЕЛЬ METABOLIC / KINETIC HUB
                val toggleBorderColor by animateColorAsState(
                    targetValue = if (hubMode == HubMode.METABOLIC) BiocodePalette.BioLime else BiocodePalette.LipidAmber,
                    animationSpec = tween(300),
                    label = "hub_border"
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(BiocodePalette.SpruceDeck)
                        .border(1.2.dp, toggleBorderColor, RoundedCornerShape(20.dp))
                        .clickable { onToggleHubMode() }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        if (hubMode == HubMode.METABOLIC) {
                            LucideUtensils(modifier = Modifier.size(13.dp), tint = BiocodePalette.BioLime)
                        } else {
                            LucideDumbbell(modifier = Modifier.size(13.dp), tint = BiocodePalette.LipidAmber)
                        }

                        Text(
                            text = if (hubMode == HubMode.METABOLIC) "METABOLIC HUB" else "KINETIC HUB",
                            style = BiocodeTypography.TelemetryLabel.copy(
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = BiocodePalette.NoguchiCream
                        )

                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(toggleBorderColor)
                        )
                    }
                }
            }

            // СКРОЛЛЯЩИЙСЯ КОНТЕНТ
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Crossfade(
                    targetState = hubMode,
                    animationSpec = tween(350),
                    label = "hub_content_crossfade"
                ) { mode ->
                    when (mode) {
                        HubMode.METABOLIC -> {
                            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                // 1. МЕТАБОЛИЧЕСКИЙ ХАБ С ОПЕРАТИВНЫМ КАЛЬКУЛЯТОРОМ РАСХОДА
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
                                    onScanClick = onAddMealWithPhoto,
                                    onThemeToggle = onThemeToggle,
                                    bmrCalories = bmrCalories,
                                    workoutBurnCalories = dynamicTodayBurn,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                // 2. БЕНТО-МОДУЛЬ МАКРОНУТРИЕНТОВ БЖУ
                                BiocodeMacroBentoModule(
                                    proteinG = state.currentProteinGrams,
                                    targetProteinG = state.targetProteinGrams,
                                    fatG = state.currentFatGrams,
                                    targetFatG = state.targetFatGrams,
                                    carbsG = state.currentCarbsGrams,
                                    targetCarbsG = state.targetCarbsGrams,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                // 3. ХРОНОЛОГИЯ ПРИЕМОВ ПИЩИ СТРОГО ЗА СЕГОДНЯ
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 4.dp, vertical = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "ПРИЕМЫ ПИЩИ ЗА СЕГОДНЯ // ${todayMeals.size} ПОЗИЦИИ",
                                        style = BiocodeTypography.TabLabel,
                                        color = BiocodePalette.NoguchiCream.copy(alpha = 0.9f)
                                    )
                                    Text(
                                        text = todayDate,
                                        style = BiocodeTypography.TelemetryLabel,
                                        color = BiocodePalette.BioLime
                                    )
                                }

                                if (todayMeals.isNotEmpty()) {
                                    BiocodeMealList(
                                        meals = todayMeals,
                                        onMealClick = onMealClick,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                } else {
                                    // ПУСТОЕ СОСТОЯНИЕ ДЛЯ СЕГОДНЯШНЕГО ДНЯ
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(BiocodePalette.SpruceDeck)
                                            .border(1.dp, BiocodePalette.DeckBorder, RoundedCornerShape(16.dp))
                                            .padding(24.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = "СЕГОДНЯ ПРИЕМОВ ПИЩИ ЕЩЕ НЕТ",
                                                style = BiocodeTypography.MonospaceTitle.copy(fontSize = 13.sp),
                                                color = BiocodePalette.NoguchiCream
                                            )
                                            Text(
                                                text = "Нажмите + в нижнем баре или PHOTO для регистрации первого приема",
                                                style = BiocodeTypography.TelemetryLabel.copy(fontSize = 9.sp),
                                                color = BiocodePalette.NoguchiCream.copy(alpha = 0.6f)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        HubMode.KINETIC -> {
                            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                // 1. ГЛАВНАЯ КАРТОЧКА KINETIC HUB: СУММАРНЫЕ ЗАТРАТЫ И ТРЕНИРОВОЧНАЯ АКТИВНОСТЬ
                                KineticTodayHeroCard(
                                    todayBurnCalories = dynamicTodayBurn,
                                    bmrCalories = bmrCalories,
                                    workoutsCount = todayWorkouts.size,
                                    onAddActivityClick = onAddActivityClick,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                // 2. ТЕЛЕМЕТРИЧЕСКИЙ БЕНТО-БЛОК КИНЕТИЧЕСКИХ МЕТРИК
                                KineticMetricsBento(
                                    todayWorkouts = todayWorkouts,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                // 3. СПИСОК ТРЕНИРОВОЧНЫХ КОМПЛЕКСОВ ЗА СЕГОДНЯ
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 4.dp, vertical = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "ТРЕНИРОВОЧНЫЕ КОМПЛЕКСЫ // СЕГОДНЯ",
                                        style = BiocodeTypography.TabLabel,
                                        color = BiocodePalette.NoguchiCream.copy(alpha = 0.9f)
                                    )
                                    Text(
                                        text = "${todayWorkouts.size} СЕССИИ",
                                        style = BiocodeTypography.TelemetryLabel,
                                        color = BiocodePalette.LipidAmber
                                    )
                                }

                                if (todayWorkouts.isNotEmpty()) {
                                    todayWorkouts.forEach { session ->
                                        WorkoutSessionCard(
                                            session = session,
                                            onExerciseClick = onExerciseClick,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(BiocodePalette.SpruceDeck)
                                            .border(1.dp, BiocodePalette.DeckBorder, RoundedCornerShape(16.dp))
                                            .padding(24.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            LucideDumbbell(modifier = Modifier.size(24.dp), tint = BiocodePalette.LipidAmber)
                                            Text(
                                                text = "НА СЕГОДНЯ ТРЕНИРОВОК ЕЩЕ НЕТ",
                                                style = BiocodeTypography.MonospaceTitle.copy(fontSize = 13.sp),
                                                color = BiocodePalette.NoguchiCream
                                            )
                                            Text(
                                                text = "Добавьте упражнение или комплекс с ИИ расчетом энергозатрат",
                                                style = BiocodeTypography.TelemetryLabel.copy(fontSize = 9.sp),
                                                color = BiocodePalette.NoguchiCream.copy(alpha = 0.6f)
                                            )
                                        }
                                    }
                                }

                                // КНОПКА ДОБАВЛЕНИЯ АКТИВНОСТИ
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(BiocodePalette.SpruceDeck)
                                        .border(1.2.dp, BiocodePalette.LipidAmber.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
                                        .clickable { onAddActivityClick() }
                                        .padding(vertical = 14.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        LucidePlus(modifier = Modifier.size(16.dp), tint = BiocodePalette.LipidAmber)
                                        Text(
                                            text = "+ ДОБАВИТЬ АКТИВНОСТЬ (ИИ РАСЧЕТ)",
                                            style = BiocodeTypography.MonospaceTitle.copy(
                                                fontSize = 11.5.sp,
                                                letterSpacing = 1.sp
                                            ),
                                            color = BiocodePalette.LipidAmber,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(86.dp)) // Отступ под нижний бар
            }
        }
    }
}

/**
 * ⚡ ГЛАВНАЯ КАРТОЧКА KINETIC HUB
 */
@Composable
fun KineticTodayHeroCard(
    todayBurnCalories: Int,
    bmrCalories: Int,
    workoutsCount: Int,
    onAddActivityClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalExpended = bmrCalories + todayBurnCalories

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(BiocodePalette.SpruceDeck)
            .border(1.2.dp, BiocodePalette.LipidAmber.copy(alpha = 0.75f), RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    LucideZap(modifier = Modifier.size(15.dp), tint = BiocodePalette.LipidAmber)
                    Text(
                        text = "KINETIC ENERGY // РАСХОД СЕГОДНЯ",
                        style = BiocodeTypography.TelemetryLabel.copy(
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = BiocodePalette.LipidAmber
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(BiocodePalette.LipidAmber.copy(alpha = 0.18f))
                        .border(1.dp, BiocodePalette.LipidAmber, RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "$workoutsCount СЕССИИ",
                        style = BiocodeTypography.TelemetryLabel.copy(fontSize = 8.sp),
                        color = BiocodePalette.LipidAmber
                    )
                }
            }

            // Дисплей активного расхода с матричным шрифтом
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    BiocodeDotMatrixText(
                        text = "$todayBurnCalories",
                        dotSize = 3.2.dp,
                        dotSpacing = 1.1.dp,
                        activeColor = BiocodePalette.LipidAmber,
                        inactiveColor = BiocodePalette.PineTeal.copy(alpha = 0.4f)
                    )
                    Text(
                        text = "ТРЕНИРОВОЧНЫЙ РАСХОД (ККАЛ)",
                        style = BiocodeTypography.TelemetryLabel.copy(fontSize = 7.5.sp),
                        color = BiocodePalette.LipidAmber
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "ИТОГО: $totalExpended KCAL",
                        style = BiocodeTypography.MonospaceTitle.copy(fontSize = 13.sp),
                        color = BiocodePalette.NoguchiCream,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "BMR $bmrCalories + ТРЕН $todayBurnCalories",
                        style = BiocodeTypography.TelemetryLabel.copy(fontSize = 8.sp),
                        color = BiocodePalette.NoguchiCream.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "АКТИВНОСТЬ: ВЫСОКАЯ",
                        style = BiocodeTypography.TelemetryLabel.copy(fontSize = 8.sp),
                        color = BiocodePalette.BioLime
                    )
                }
            }

            // Быстрая кнопка расчета
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .clip(RoundedCornerShape(19.dp))
                    .background(BiocodePalette.DarkMoss)
                    .border(1.dp, BiocodePalette.DeckBorder, RoundedCornerShape(19.dp))
                    .clickable { onAddActivityClick() }
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    LucideFlame(modifier = Modifier.size(14.dp), tint = BiocodePalette.LipidAmber)
                    Text(
                        text = "+ РАССЧИТАТЬ НАГРУЗКУ (ИИ)",
                        style = BiocodeTypography.TelemetryLabel.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                        color = BiocodePalette.NoguchiCream
                    )
                }
            }
        }
    }
}

/**
 * 📊 БЕНТО-БЛОК КИНЕТИЧЕСКИХ МЕТРИК
 */
@Composable
fun KineticMetricsBento(
    todayWorkouts: List<WorkoutSession>,
    modifier: Modifier = Modifier
) {
    val totalMinutes = todayWorkouts.sumOf { it.durationMinutes }
    val totalExercises = todayWorkouts.sumOf { it.exercises.size }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Минуты активности
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(18.dp))
                .background(BiocodePalette.SpruceDeck)
                .border(1.dp, BiocodePalette.DeckBorder, RoundedCornerShape(18.dp))
                .padding(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "ВРЕМЯ В НАГРУЗКЕ",
                    style = BiocodeTypography.TelemetryLabel.copy(fontSize = 7.5.sp),
                    color = BiocodePalette.NoguchiCream.copy(alpha = 0.6f)
                )
                Text(
                    text = "$totalMinutes МИН",
                    style = BiocodeTypography.MonospaceTitle.copy(fontSize = 16.sp),
                    color = BiocodePalette.BioLime,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Число упражнений
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(18.dp))
                .background(BiocodePalette.SpruceDeck)
                .border(1.dp, BiocodePalette.DeckBorder, RoundedCornerShape(18.dp))
                .padding(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "УПРАЖНЕНИЙ",
                    style = BiocodeTypography.TelemetryLabel.copy(fontSize = 7.5.sp),
                    color = BiocodePalette.NoguchiCream.copy(alpha = 0.6f)
                )
                Text(
                    text = "$totalExercises ДВИЖЕНИЙ",
                    style = BiocodeTypography.MonospaceTitle.copy(fontSize = 16.sp),
                    color = BiocodePalette.LipidAmber,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * 🏋️ КАРТОЧКА ЦЕЛЬНОЙ ТРЕНИРОВОЧНОЙ СЕССИИ (КОМПЛЕКСА)
 */
@Composable
fun WorkoutSessionCard(
    session: WorkoutSession,
    onExerciseClick: ((ExerciseItem) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(BiocodePalette.SpruceDeck)
            .border(1.2.dp, BiocodePalette.DeckBorder, RoundedCornerShape(20.dp))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Заголовок сессии
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = session.title,
                        style = BiocodeTypography.MonospaceTitle.copy(fontSize = 14.sp),
                        color = BiocodePalette.NoguchiCream,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${session.category.label} • ${session.time} • ${session.durationMinutes} мин",
                        style = BiocodeTypography.TelemetryLabel.copy(fontSize = 8.sp),
                        color = BiocodePalette.NoguchiCream.copy(alpha = 0.6f)
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(BiocodePalette.LipidAmber.copy(alpha = 0.15f))
                        .border(1.dp, BiocodePalette.LipidAmber, RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "-${session.totalCaloriesBurned} KCAL",
                        style = BiocodeTypography.TelemetryLabel.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = BiocodePalette.LipidAmber
                    )
                }
            }

            if (session.description.isNotBlank()) {
                Text(
                    text = session.description,
                    style = BiocodeTypography.TelemetryLabel.copy(fontSize = 8.5.sp),
                    color = BiocodePalette.NoguchiCream.copy(alpha = 0.75f)
                )
            }

            // Список упражнений внутри комплекса
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                session.exercises.forEachIndexed { index, ex ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(BiocodePalette.DarkMoss.copy(alpha = 0.7f))
                            .border(0.8.dp, BiocodePalette.DeckBorderSubtle, RoundedCornerShape(12.dp))
                            .clickable { onExerciseClick?.invoke(ex) }
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(BiocodePalette.PineTeal.copy(alpha = 0.5f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${index + 1}",
                                        style = BiocodeTypography.TelemetryLabel.copy(fontSize = 7.5.sp),
                                        color = BiocodePalette.BioLime
                                    )
                                }

                                Column {
                                    Text(
                                        text = ex.name,
                                        style = BiocodeTypography.MonospaceTitle.copy(fontSize = 11.sp),
                                        color = BiocodePalette.NoguchiCream,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "${ex.sets} сетов × ${ex.repsOrTime} ${ex.weightKg?.let { "• ${it.toInt()} кг" } ?: ""} • ${ex.feelingsRpe}",
                                        style = BiocodeTypography.TelemetryLabel.copy(fontSize = 7.5.sp),
                                        color = BiocodePalette.NoguchiCream.copy(alpha = 0.6f)
                                    )
                                }
                            }

                            Text(
                                text = "-${ex.caloriesBurned} kcal",
                                style = BiocodeTypography.TelemetryLabel.copy(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = BiocodePalette.BioLime
                            )
                        }
                    }
                }
            }
        }
    }
}
