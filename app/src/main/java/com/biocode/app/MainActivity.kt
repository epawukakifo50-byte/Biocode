package com.biocode.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.biocode.app.navigation.BiocodeBottomBar
import com.biocode.app.navigation.BiocodeTab
import com.biocode.app.screens.AddMealModal
import com.biocode.app.screens.CalculatorScreen
import com.biocode.app.screens.DiaryScreen
import com.biocode.app.screens.MealBuilderScreen
import com.biocode.app.screens.TodayScreen
import com.biocode.engine.BiocodeColors
import com.biocode.engine.BiocodePalette
import com.biocode.engine.BiocodeThemeMode
import com.biocode.engine.BiocodeTypography
import com.biocode.engine.DailyNutritionState
import com.biocode.engine.LucideSparkles
import com.biocode.engine.MealEntry
import com.biocode.engine.getBiocodeThemeColors
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BiocodeAppRoot()
        }
    }
}

@Composable
fun BiocodeAppRoot() {
    var currentThemeMode by remember { mutableStateOf(BiocodeThemeMode.NOGUCHI_GREEN) }
    var shockwaveTrigger by remember { mutableStateOf(0) }
    var showThemeHud by remember { mutableStateOf(false) }

    val targetColors = remember(currentThemeMode) { getBiocodeThemeColors(currentThemeMode) }
    val animSpec = tween<Color>(durationMillis = 450, easing = FastOutSlowInEasing)

    // Плавная интерполяция всех токенов темы
    val animCream by animateColorAsState(targetColors.noguchiCream, animSpec, label = "cream")
    val animLime by animateColorAsState(targetColors.bioLime, animSpec, label = "lime")
    val animTeal by animateColorAsState(targetColors.pineTeal, animSpec, label = "teal")
    val animMoss by animateColorAsState(targetColors.darkMoss, animSpec, label = "moss")
    val animDeepPine by animateColorAsState(targetColors.deepPineBg, animSpec, label = "deepPine")
    val animSpruce by animateColorAsState(targetColors.spruceDeck, animSpec, label = "spruce")
    val animDeckBorder by animateColorAsState(targetColors.deckBorder, animSpec, label = "deckBorder")
    val animDeckSubtle by animateColorAsState(targetColors.deckBorderSubtle, animSpec, label = "deckSubtle")
    val animNoguchiBorder by animateColorAsState(targetColors.noguchiBorder, animSpec, label = "noguchiBorder")
    val animLimeBorder by animateColorAsState(targetColors.limeBorder, animSpec, label = "limeBorder")
    val animPillActive by animateColorAsState(targetColors.pillActiveBg, animSpec, label = "pillActive")
    val animPillInactive by animateColorAsState(targetColors.pillInactiveBg, animSpec, label = "pillInactive")
    val animPillCream by animateColorAsState(targetColors.pillCreamBg, animSpec, label = "pillCream")
    val animPillDark by animateColorAsState(targetColors.pillDarkBg, animSpec, label = "pillDark")
    val animProtein by animateColorAsState(targetColors.macroProtein, animSpec, label = "protein")
    val animFat by animateColorAsState(targetColors.macroFat, animSpec, label = "fat")
    val animLipidAmber by animateColorAsState(targetColors.lipidAmber, animSpec, label = "lipidAmber")
    val animCarbs by animateColorAsState(targetColors.macroCarbs, animSpec, label = "carbs")
    val animCarbsLight by animateColorAsState(targetColors.macroCarbsLight, animSpec, label = "carbsLight")
    val animWater by animateColorAsState(targetColors.macroWater, animSpec, label = "water")
    val animCalories by animateColorAsState(targetColors.macroCalories, animSpec, label = "calories")

    // Обновляем глобальную палитру
    LaunchedEffect(
        animCream, animLime, animTeal, animMoss, animSpruce, animDeckBorder, animNoguchiBorder
    ) {
        BiocodePalette.current = BiocodeColors(
            noguchiCream = animCream,
            bioLime = animLime,
            pineTeal = animTeal,
            darkMoss = animMoss,
            deepPineBg = animDeepPine,
            spruceDeck = animSpruce,
            deckBorder = animDeckBorder,
            deckBorderSubtle = animDeckSubtle,
            noguchiBorder = animNoguchiBorder,
            limeBorder = animLimeBorder,
            pillActiveBg = animPillActive,
            pillInactiveBg = animPillInactive,
            pillCreamBg = animPillCream,
            pillDarkBg = animPillDark,
            macroProtein = animProtein,
            macroFat = animFat,
            lipidAmber = animLipidAmber,
            macroCarbs = animCarbs,
            macroCarbsLight = animCarbsLight,
            macroWater = animWater,
            macroCalories = animCalories
        )
    }

    // Авто-скрытие HUD-бейджа темы
    LaunchedEffect(shockwaveTrigger) {
        if (shockwaveTrigger > 0) {
            showThemeHud = true
            delay(1900)
            showThemeHud = false
        }
    }

    val colorScheme = darkColorScheme(
        primary = animLime,
        secondary = animTeal,
        background = animMoss,
        surface = animSpruce,
        onPrimary = animMoss,
        onSecondary = animCream,
        onBackground = animCream,
        onSurface = animCream
    )

    MaterialTheme(colorScheme = colorScheme) {
        BiocodeMainContainer(
            currentThemeMode = currentThemeMode,
            shockwaveTrigger = shockwaveTrigger,
            showThemeHud = showThemeHud,
            onThemeToggle = {
                currentThemeMode = currentThemeMode.next()
                shockwaveTrigger++
            }
        )
    }
}

@Composable
fun BiocodeMainContainer(
    currentThemeMode: BiocodeThemeMode,
    shockwaveTrigger: Int,
    showThemeHud: Boolean,
    onThemeToggle: () -> Unit
) {
    var diaryState by remember { mutableStateOf(DailyNutritionState()) }
    var currentTab by remember { mutableStateOf(BiocodeTab.TODAY) }
    var showAddMealModal by remember { mutableStateOf(false) }

    fun handleAddMeal(meal: MealEntry) {
        diaryState = diaryState.copy(
            consumedCalories = diaryState.consumedCalories + meal.calories,
            currentProteinGrams = diaryState.currentProteinGrams + meal.protein,
            currentFatGrams = diaryState.currentFatGrams + meal.fat,
            currentCarbsGrams = diaryState.currentCarbsGrams + meal.carbs,
            recentMeals = listOf(meal) + diaryState.recentMeals
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BiocodePalette.DarkMoss)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // 1. КОНТЕНТ АКТИВНОГО ЭКРАНА
        Crossfade(
            targetState = currentTab,
            label = "tab_crossfade",
            modifier = Modifier.fillMaxSize()
        ) { tab ->
            when (tab) {
                BiocodeTab.TODAY -> TodayScreen(
                    state = diaryState,
                    onStateUpdate = { diaryState = it },
                    onThemeToggle = onThemeToggle,
                    modifier = Modifier.fillMaxSize()
                )
                BiocodeTab.MEAL_BUILDER -> MealBuilderScreen(
                    state = diaryState,
                    onAddMeal = { handleAddMeal(it) },
                    modifier = Modifier.fillMaxSize()
                )
                BiocodeTab.CALCULATOR -> CalculatorScreen(
                    state = diaryState,
                    onUpdateTargets = { cals, prot, fat, carbs ->
                        diaryState = diaryState.copy(
                            targetCalories = cals,
                            targetProteinGrams = prot,
                            targetFatGrams = fat,
                            targetCarbsGrams = carbs
                        )
                    },
                    modifier = Modifier.fillMaxSize()
                )
                BiocodeTab.DIARY -> DiaryScreen(
                    state = diaryState,
                    onAddMealClick = { showAddMealModal = true },
                    onDeleteMeal = { mealId ->
                        val mealToRemove = diaryState.recentMeals.find { it.id == mealId }
                        if (mealToRemove != null) {
                            diaryState = diaryState.copy(
                                consumedCalories = (diaryState.consumedCalories - mealToRemove.calories).coerceAtLeast(0),
                                currentProteinGrams = (diaryState.currentProteinGrams - mealToRemove.protein).coerceAtLeast(0f),
                                currentFatGrams = (diaryState.currentFatGrams - mealToRemove.fat).coerceAtLeast(0f),
                                currentCarbsGrams = (diaryState.currentCarbsGrams - mealToRemove.carbs).coerceAtLeast(0f),
                                recentMeals = diaryState.recentMeals.filterNot { it.id == mealId }
                            )
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // 2. РАДИАЛЬНАЯ БИО-ВОЛНА (SHOCKWAVE) ОТ КНОПКИ ЗВЕЗДЫ
        BiocodeShockwaveOverlay(
            trigger = shockwaveTrigger,
            accentColor = BiocodePalette.BioLime,
            modifier = Modifier.fillMaxSize()
        )

        // 3. 5-КНОПОЧНЫЙ НАВИГАЦИОННЫЙ БАР
        BiocodeBottomBar(
            selectedTab = currentTab,
            onTabSelected = { currentTab = it },
            onAddMealClick = { showAddMealModal = true },
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        // 4. ВСПЛЫВАЮЩИЙ HUD-БЕЙДЖ СМЕНЫ ТЕМЫ
        AnimatedVisibility(
            visible = showThemeHud,
            enter = fadeIn() + slideInVertically { -it },
            exit = fadeOut() + slideOutVertically { -it },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 10.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(BiocodePalette.DarkMoss.copy(alpha = 0.94f))
                    .border(1.2.dp, BiocodePalette.BioLime, RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 7.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LucideSparkles(modifier = Modifier.size(15.dp), tint = BiocodePalette.BioLime)
                    Column {
                        Text(
                            text = "ТЕМА: ${currentThemeMode.title} [${currentThemeMode.codeIndex}]",
                            style = BiocodeTypography.TelemetryLabel.copy(
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = BiocodePalette.BioLime
                        )
                        Text(
                            text = currentThemeMode.subtitle,
                            style = BiocodeTypography.TelemetryLabel.copy(fontSize = 7.5.sp),
                            color = BiocodePalette.NoguchiCream.copy(alpha = 0.85f)
                        )
                    }
                }
            }
        }

        // 5. МОДАЛЬНЫЙ ДИАЛОГ ДОБАВЛЕНИЯ ПРИЕМА ПИЩИ
        if (showAddMealModal) {
            AddMealModal(
                onDismiss = { showAddMealModal = false },
                onSaveMeal = { newMeal ->
                    handleAddMeal(newMeal)
                }
            )
        }
    }
}

/**
 * 🌊 РАДИАЛЬНАЯ БИО-ВОЛНА (SHOCKWAVE), РАСХОДЯЩАЯСЯ ОТ ЗВЕЗДЫ В СТЫКОВОЧНОМ УЗЛЕ
 */
@Composable
fun BiocodeShockwaveOverlay(
    trigger: Int,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val progress = remember { Animatable(1f) }
    val density = LocalDensity.current

    // Центр стыковочного узла на экране (~45.dp от левого края, ~225.dp от верха контента)
    val originX = with(density) { 45.dp.toPx() }
    val originY = with(density) { 225.dp.toPx() }

    LaunchedEffect(trigger) {
        if (trigger > 0) {
            progress.snapTo(0.01f)
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing)
            )
        }
    }

    if (progress.value < 1f) {
        val p = progress.value
        val radius = (p * 1800f).coerceAtLeast(10f)
        val strokeW = with(density) { (12.dp + (24.dp * (1f - p))).toPx() }
        val alpha = (1f - p).coerceIn(0f, 1f) * 0.75f

        Canvas(modifier = modifier) {
            val centerOffset = Offset(originX, originY)

            // 1. Мягкое биоморфное световое поле внутри фронта (радиус строго > 0)
            if (p < 0.75f) {
                val glowRadius = (radius * 1.1f).coerceAtLeast(20f)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            accentColor.copy(alpha = (0.75f - p) * 0.35f),
                            Color.Transparent
                        ),
                        center = centerOffset,
                        radius = glowRadius
                    ),
                    radius = glowRadius,
                    center = centerOffset
                )
            }

            // 2. Главная неоновая ударная волна
            drawCircle(
                color = accentColor.copy(alpha = alpha),
                radius = radius,
                center = centerOffset,
                style = Stroke(width = strokeW)
            )

            // 3. Высокочастотный вторичный гребень волны (белый импульс)
            if (p in 0.05f..0.85f) {
                val innerRadius = (radius * 0.94f).coerceAtLeast(5f)
                drawCircle(
                    color = Color.White.copy(alpha = alpha * 0.85f),
                    radius = innerRadius,
                    center = centerOffset,
                    style = Stroke(width = 2.5.dp.toPx())
                )
            }
        }
    }
}
