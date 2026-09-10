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
    val isBiocodeVerified: Boolean = true,
    val description: String = "",
    val weightGrams: Int? = null,
    val photoUri: String? = null
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
        MealEntry(
            id = "1",
            name = "Овсянка с миндалем и изолятом",
            type = MealType.BREAKFAST,
            calories = 540,
            protein = 38f,
            fat = 14f,
            carbs = 62f,
            time = "08:30",
            description = "Цельнозерновой овес длительной варки, миндальные лепестки, сывороточный изолят, щепотка цейлонской корицы",
            weightGrams = 320
        ),
        MealEntry(
            id = "2",
            name = "Лосось на пару, киноа и спаржа",
            type = MealType.LUNCH,
            calories = 780,
            protein = 52f,
            fat = 26f,
            carbs = 74f,
            time = "13:45",
            description = "Филе дикого лосося на пару со спаржей аль денте, перуанское белое киноа с оливковым маслом первого отжима",
            weightGrams = 420
        ),
        MealEntry(
            id = "3",
            name = "Матча-смузи",
            type = MealType.SNACK,
            calories = 220,
            protein = 8f,
            fat = 6f,
            carbs = 32f,
            time = "16:20",
            description = "Церемониальная японская матча Uji, кокосовое молоко без сахара, семена чиа, шпинат",
            weightGrams = 280
        ),
        MealEntry(
            id = "4",
            name = "Запеченное филе индейки с пряными травами и бататом",
            type = MealType.DINNER,
            calories = 500,
            protein = 44f,
            fat = 10f,
            carbs = 56f,
            time = "19:50",
            description = "Грудка индейки su-vide с розмарином и чесноком, запеченный батат с морской солью",
            weightGrams = 360
        )
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
