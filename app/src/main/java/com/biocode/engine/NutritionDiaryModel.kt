package com.biocode.engine

/**
 * Тип приема пищи в биометрическом цикле.
 */
enum class MealType(val label: String, val code: String) {
    BREAKFAST("Завтрак", "#01"),
    LUNCH("Обед", "#02"),
    DINNER("Ужин", "#03"),
    SNACK("Перекус", "#04")
}

/**
 * Запись приема пищи в дневнике питания Biocode.
 */
data class MealEntry(
    val id: String,
    val name: String,
    val type: MealType,
    val calories: Int,
    val protein: Float,
    val fat: Float,
    val carbs: Float,
    val time: String,
    val isBiocodeVerified: Boolean = true
)

/**
 * Агрегированное биометрическое состояние питания на текущий день.
 */
data class DailyNutritionState(
    val dateLabel: String = "04 СЕН 2026 // ТАКТ 18",
    val targetCalories: Int = 2400,
    val consumedCalories: Int = 1840,
    val targetProteinGrams: Float = 140f,
    val currentProteinGrams: Float = 112f,
    val targetFatGrams: Float = 75f,
    val currentFatGrams: Float = 54f,
    val targetCarbsGrams: Float = 280f,
    val currentCarbsGrams: Float = 224f,
    val targetWaterMl: Int = 2500,
    val currentWaterMl: Int = 1850,
    val glycemicScore: Int = 92, // 0..100
    val metabolicPhase: String = "АНАБОЛИЧЕСКИЙ ДОК // ФАЗА 2",
    val recentMeals: List<MealEntry> = listOf(
        MealEntry("1", "Овсянка с миндалем и изолятом", MealType.BREAKFAST, 540, 38f, 14f, 62f, "08:30"),
        MealEntry("2", "Лосось на пару, киноа и спаржа", MealType.LUNCH, 780, 52f, 26f, 74f, "13:45"),
        MealEntry("3", "Матча-смузи", MealType.SNACK, 220, 8f, 6f, 32f, "16:20"),
        MealEntry("4", "Запеченное филе индейки с пряными травами и бататом", MealType.DINNER, 500, 44f, 10f, 56f, "19:50")
    )
) {
    val remainingCalories: Int
        get() = (targetCalories - consumedCalories).coerceAtLeast(0)

    val calorieProgressRatio: Float
        get() = (consumedCalories.toFloat() / targetCalories.toFloat()).coerceIn(0f, 1.5f)

    val proteinProgressRatio: Float
        get() = (currentProteinGrams / targetProteinGrams).coerceIn(0f, 1.5f)

    val fatProgressRatio: Float
        get() = (currentFatGrams / targetFatGrams).coerceIn(0f, 1.5f)

    val carbsProgressRatio: Float
        get() = (currentCarbsGrams / targetCarbsGrams).coerceIn(0f, 1.5f)

    val waterProgressRatio: Float
        get() = (currentWaterMl.toFloat() / targetWaterMl.toFloat()).coerceIn(0f, 1.5f)
}
