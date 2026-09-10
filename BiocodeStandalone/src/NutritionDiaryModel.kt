package com.biocode.engine

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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
    val date: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
    val isBiocodeVerified: Boolean = true,
    val description: String = "",
    val weightGrams: Int? = null,
    val photoUri: String? = null
)

private fun getPastDate(daysAgo: Int): String {
    val cal = Calendar.getInstance()
    cal.add(Calendar.DAY_OF_YEAR, -daysAgo)
    return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
}

/**
 * Агрегированное биометрическое состояние питания на текущий день.
 */
data class DailyNutritionState(
    val dateLabel: String = "СЕГОДНЯ // ТАКТ 18",
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
        // Сегодняшние приемы пищи
        MealEntry(
            id = "1",
            name = "Овсянка с миндалем и изолятом",
            type = MealType.BREAKFAST,
            calories = 540,
            protein = 38f,
            fat = 14f,
            carbs = 62f,
            time = "08:30",
            date = getPastDate(0),
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
            date = getPastDate(0),
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
            date = getPastDate(0),
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
            date = getPastDate(0),
            description = "Грудка индейки su-vide с розмарином и чесноком, запеченный батат с морской солью",
            weightGrams = 360
        ),
        // Вчера (100% норма)
        MealEntry(
            id = "hist_1",
            name = "Омлет из 4 яиц со шпинатом",
            type = MealType.BREAKFAST,
            calories = 620,
            protein = 42f,
            fat = 38f,
            carbs = 18f,
            time = "09:00",
            date = getPastDate(1),
            description = "Фермерские яйца, молодой шпинат, пармезан",
            weightGrams = 300
        ),
        MealEntry(
            id = "hist_2",
            name = "Стейк из тунца с диким рисом",
            type = MealType.LUNCH,
            calories = 890,
            protein = 68f,
            fat = 22f,
            carbs = 85f,
            time = "14:15",
            date = getPastDate(1),
            description = "Тру-стейк yellowfin тунец средней прожарки",
            weightGrams = 450
        ),
        MealEntry(
            id = "hist_3",
            name = "Творожный мусс с ягодами асаи",
            type = MealType.DINNER,
            calories = 890,
            protein = 55f,
            fat = 18f,
            carbs = 90f,
            time = "20:00",
            date = getPastDate(1),
            description = "Обезжиренный творог, пюре асаи, черника",
            weightGrams = 380
        ),
        // 2 дня назад (130% легкий профицит)
        MealEntry(
            id = "hist_4",
            name = "Паста с морепродуктами в соусе песто",
            type = MealType.LUNCH,
            calories = 1450,
            protein = 75f,
            fat = 42f,
            carbs = 160f,
            time = "13:30",
            date = getPastDate(2),
            description = "Паста твердых сортов, тигровые креветки, кальмары",
            weightGrams = 550
        ),
        MealEntry(
            id = "hist_5",
            name = "Протеиновые панкейки с кленовым сиропом",
            type = MealType.BREAKFAST,
            calories = 1670,
            protein = 80f,
            fat = 35f,
            carbs = 210f,
            time = "10:00",
            date = getPastDate(2),
            description = "Овсяные блины с изолятом и ягодами",
            weightGrams = 480
        ),
        // 3 дня назад (70% дефицит)
        MealEntry(
            id = "hist_6",
            name = "Салат с цыпленком и авокадо",
            type = MealType.LUNCH,
            calories = 720,
            protein = 58f,
            fat = 32f,
            carbs = 35f,
            time = "13:00",
            date = getPastDate(3),
            description = "Микс салатов, филе на гриле, авокадо хасс",
            weightGrams = 380
        ),
        MealEntry(
            id = "hist_7",
            name = "Протеиновый шейк с миндальным молоком",
            type = MealType.SNACK,
            calories = 350,
            protein = 35f,
            fat = 8f,
            carbs = 24f,
            time = "17:00",
            date = getPastDate(3),
            description = "Изолят сывороточный",
            weightGrams = 350
        ),
        // 4 дня назад (30% минимальный прием)
        MealEntry(
            id = "hist_8",
            name = "Протеиновый батончик и кофе",
            type = MealType.BREAKFAST,
            calories = 720,
            protein = 35f,
            fat = 20f,
            carbs = 60f,
            time = "09:30",
            date = getPastDate(4),
            description = "Быстрый перекус в дороге",
            weightGrams = 120
        ),
        // 5 дней назад (170% повышенный профицит)
        MealEntry(
            id = "hist_9",
            name = "Читмил: Бургер с мраморной говядиной и батат фри",
            type = MealType.DINNER,
            calories = 2400,
            protein = 95f,
            fat = 110f,
            carbs = 220f,
            time = "19:00",
            date = getPastDate(5),
            description = "Котлета из блэк ангус, сыр чеддер, батат",
            weightGrams = 700
        ),
        MealEntry(
            id = "hist_10",
            name = "Сырники со сметаной и медом",
            type = MealType.BREAKFAST,
            calories = 1680,
            protein = 65f,
            fat = 58f,
            carbs = 175f,
            time = "10:30",
            date = getPastDate(5),
            description = "Сырники из фермерского творога 9%",
            weightGrams = 420
        ),
        // 6 дней назад (200%+ супер-профицит)
        MealEntry(
            id = "hist_11",
            name = "Загрузочный день: Пицца, паста, десерт",
            type = MealType.LUNCH,
            calories = 4900,
            protein = 135f,
            fat = 145f,
            carbs = 580f,
            time = "15:00",
            date = getPastDate(6),
            description = "Высокоуглеводная рекомпозиционная загрузка",
            weightGrams = 1200
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
