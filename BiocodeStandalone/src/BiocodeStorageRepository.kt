package com.biocode.engine

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Целевые суточные показатели нутриентов.
 */
data class DailyTargets(
    val calories: Int = 2400,
    val protein: Float = 140f,
    val fat: Float = 75f,
    val carbs: Float = 280f
)

/**
 * 💾 СИСТЕМНОЕ ХРАНИЛИЩЕ BIOCODE (PERSISTENCE REPOSITORY)
 *
 * Обеспечивает персистентность всех параметров между перезапусками приложения:
 * - Приемы пищи (добавление, редактирование, удаление навсегда без «воскрешения»);
 * - Выбранная цветовая тема (1 из 5 тем);
 * - Активный хаб (Metabolic / Kinetic);
 * - Биометрический профиль пользователя (пол, возраст, вес, рост, BMR, режим);
 * - Целевые показатели калорий и БЖУ;
 * - Кинетические тренировочные сессии и упражнения.
 */
class BiocodeStorageRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "biocode_storage_v1"
        private const val KEY_INIT = "biocode_initialized_v2"
        private const val KEY_THEME = "biocode_theme_mode"
        private const val KEY_HUB = "biocode_hub_mode"
        private const val KEY_BIOMETRICS = "biocode_biometrics_json"
        private const val KEY_TARGETS = "biocode_targets_json"
        private const val KEY_MEALS = "biocode_meals_json"
        private const val KEY_WORKOUTS = "biocode_workouts_json"

        @Volatile
        private var INSTANCE: BiocodeStorageRepository? = null

        fun getInstance(context: Context): BiocodeStorageRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: BiocodeStorageRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    init {
        ensureInitialData()
    }

    /**
     * Первичная инициализация: заполняет демо-данные ровно один раз.
     * При последующих запусках любые изменения пользователя (включая удаление) сохраняются навсегда.
     */
    private fun ensureInitialData() {
        if (!prefs.getBoolean(KEY_INIT, false)) {
            // 1. Стартовые приемы пищи (сегодня + история для 84-дневной матрицы)
            val demoMeals = generateInitialDemoMeals()
            saveMeals(demoMeals)

            // 2. Стартовая биометрия
            val defaultBio = BiometricsProfile()
            saveBiometricsProfile(defaultBio)

            // 3. Стартовые цели
            val defaultTargets = DailyTargets(
                calories = defaultBio.targetDailyCalories,
                protein = defaultBio.targetProteinGrams,
                fat = defaultBio.targetFatGrams,
                carbs = defaultBio.targetCarbsGrams
            )
            saveDailyTargets(defaultTargets)

            // 4. Тема и хаб
            saveTheme(BiocodeThemeMode.NOGUCHI_GREEN)
            saveHubMode(HubMode.METABOLIC)

            // 5. Тренировки
            saveWorkouts(DefaultKineticData.createInitialWorkouts())

            prefs.edit().putBoolean(KEY_INIT, true).apply()
        }
    }

    // ==========================================
    // 1. ПРИЕМЫ ПИЩИ (MEALS)
    // ==========================================

    fun saveMeals(meals: List<MealEntry>) {
        val array = JSONArray()
        for (m in meals) {
            val obj = JSONObject().apply {
                put("id", m.id)
                put("name", m.name)
                put("type", m.type.name)
                put("calories", m.calories)
                put("protein", m.protein.toDouble())
                put("fat", m.fat.toDouble())
                put("carbs", m.carbs.toDouble())
                put("time", m.time)
                put("date", m.date)
                put("isBiocodeVerified", m.isBiocodeVerified)
                put("description", m.description)
                put("weightGrams", m.weightGrams ?: -1)
                put("photoUri", m.photoUri ?: "")
            }
            array.put(obj)
        }
        prefs.edit().putString(KEY_MEALS, array.toString()).apply()
    }

    fun loadMeals(): List<MealEntry> {
        val raw = prefs.getString(KEY_MEALS, null) ?: return emptyList()
        val list = mutableListOf<MealEntry>()
        try {
            val array = JSONArray(raw)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val typeStr = obj.optString("type", MealType.LUNCH.name)
                val mealType = runCatching { MealType.valueOf(typeStr) }.getOrDefault(MealType.LUNCH)
                val weight = obj.optInt("weightGrams", -1)
                val photo = obj.optString("photoUri", "")

                list.add(
                    MealEntry(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        type = mealType,
                        calories = obj.getInt("calories"),
                        protein = obj.getDouble("protein").toFloat(),
                        fat = obj.getDouble("fat").toFloat(),
                        carbs = obj.getDouble("carbs").toFloat(),
                        time = obj.optString("time", "12:00"),
                        date = obj.optString("date", SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())),
                        isBiocodeVerified = obj.optBoolean("isBiocodeVerified", true),
                        description = obj.optString("description", ""),
                        weightGrams = if (weight >= 0) weight else null,
                        photoUri = if (photo.isNotBlank()) photo else null
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    // ==========================================
    // 2. ЦВЕТОВАЯ ТЕМА (THEME)
    // ==========================================

    fun saveTheme(theme: BiocodeThemeMode) {
        prefs.edit().putString(KEY_THEME, theme.name).apply()
    }

    fun loadTheme(): BiocodeThemeMode {
        val name = prefs.getString(KEY_THEME, BiocodeThemeMode.NOGUCHI_GREEN.name)
        return runCatching { BiocodeThemeMode.valueOf(name!!) }.getOrDefault(BiocodeThemeMode.NOGUCHI_GREEN)
    }

    // ==========================================
    // 3. ХАБ (HUB MODE)
    // ==========================================

    fun saveHubMode(mode: HubMode) {
        prefs.edit().putString(KEY_HUB, mode.name).apply()
    }

    fun loadHubMode(): HubMode {
        val name = prefs.getString(KEY_HUB, HubMode.METABOLIC.name)
        return runCatching { HubMode.valueOf(name!!) }.getOrDefault(HubMode.METABOLIC)
    }

    // ==========================================
    // 4. БИОМЕТРИЧЕСКИЙ ПРОФИЛЬ (BIOMETRICS)
    // ==========================================

    fun saveBiometricsProfile(profile: BiometricsProfile) {
        val obj = JSONObject().apply {
            put("gender", profile.gender.name)
            put("age", profile.age)
            put("weightKg", profile.weightKg.toDouble())
            put("heightCm", profile.heightCm.toDouble())
            put("targetMode", profile.targetMode.name)
            put("bmrCalories", profile.bmrCalories)
            put("targetDailyCalories", profile.targetDailyCalories)
            put("targetBurnCalories", profile.targetBurnCalories)
            put("targetProteinGrams", profile.targetProteinGrams.toDouble())
            put("targetFatGrams", profile.targetFatGrams.toDouble())
            put("targetCarbsGrams", profile.targetCarbsGrams.toDouble())
        }
        prefs.edit().putString(KEY_BIOMETRICS, obj.toString()).apply()
    }

    fun loadBiometricsProfile(): BiometricsProfile {
        val raw = prefs.getString(KEY_BIOMETRICS, null) ?: return BiometricsProfile()
        return try {
            val obj = JSONObject(raw)
            val genderStr = obj.optString("gender", Gender.MALE.name)
            val modeStr = obj.optString("targetMode", TargetMetabolicMode.HOMEOSTASIS.name)

            BiometricsProfile(
                gender = runCatching { Gender.valueOf(genderStr) }.getOrDefault(Gender.MALE),
                age = obj.optInt("age", 28),
                weightKg = obj.optDouble("weightKg", 78.0).toFloat(),
                heightCm = obj.optDouble("heightCm", 182.0).toFloat(),
                targetMode = runCatching { TargetMetabolicMode.valueOf(modeStr) }.getOrDefault(TargetMetabolicMode.HOMEOSTASIS),
                bmrCalories = obj.optInt("bmrCalories", 1810),
                targetDailyCalories = obj.optInt("targetDailyCalories", 2400),
                targetBurnCalories = obj.optInt("targetBurnCalories", 600),
                targetProteinGrams = obj.optDouble("targetProteinGrams", 140.0).toFloat(),
                targetFatGrams = obj.optDouble("targetFatGrams", 75.0).toFloat(),
                targetCarbsGrams = obj.optDouble("targetCarbsGrams", 280.0).toFloat()
            )
        } catch (e: Exception) {
            BiometricsProfile()
        }
    }

    // ==========================================
    // 5. СУТОЧНЫЕ ЦЕЛИ (DAILY TARGETS)
    // ==========================================

    fun saveDailyTargets(targets: DailyTargets) {
        val obj = JSONObject().apply {
            put("calories", targets.calories)
            put("protein", targets.protein.toDouble())
            put("fat", targets.fat.toDouble())
            put("carbs", targets.carbs.toDouble())
        }
        prefs.edit().putString(KEY_TARGETS, obj.toString()).apply()
    }

    fun loadDailyTargets(): DailyTargets {
        val raw = prefs.getString(KEY_TARGETS, null) ?: return DailyTargets()
        return try {
            val obj = JSONObject(raw)
            DailyTargets(
                calories = obj.optInt("calories", 2400),
                protein = obj.optDouble("protein", 140.0).toFloat(),
                fat = obj.optDouble("fat", 75.0).toFloat(),
                carbs = obj.optDouble("carbs", 280.0).toFloat()
            )
        } catch (e: Exception) {
            DailyTargets()
        }
    }

    // ==========================================
    // 6. ТРЕНИРОВКИ (WORKOUT SESSIONS)
    // ==========================================

    fun saveWorkouts(workouts: List<WorkoutSession>) {
        val array = JSONArray()
        for (w in workouts) {
            val wObj = JSONObject().apply {
                put("id", w.id)
                put("title", w.title)
                put("description", w.description)
                put("date", w.date)
                put("time", w.time)
                put("category", w.category.name)
                put("totalCaloriesBurned", w.totalCaloriesBurned)
                put("durationMinutes", w.durationMinutes)
                put("isCompleted", w.isCompleted)

                val exArray = JSONArray()
                for (ex in w.exercises) {
                    val exObj = JSONObject().apply {
                        put("id", ex.id)
                        put("name", ex.name)
                        put("description", ex.description)
                        put("feelingsRpe", ex.feelingsRpe)
                        put("sets", ex.sets)
                        put("repsOrTime", ex.repsOrTime)
                        put("weightKg", ex.weightKg?.toDouble() ?: -1.0)
                        put("durationMinutes", ex.durationMinutes)
                        put("caloriesBurned", ex.caloriesBurned)
                    }
                    exArray.put(exObj)
                }
                put("exercises", exArray)
            }
            array.put(wObj)
        }
        prefs.edit().putString(KEY_WORKOUTS, array.toString()).apply()
    }

    fun loadWorkouts(): List<WorkoutSession> {
        val raw = prefs.getString(KEY_WORKOUTS, null) ?: return DefaultKineticData.createInitialWorkouts()
        val list = mutableListOf<WorkoutSession>()
        try {
            val array = JSONArray(raw)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val catStr = obj.optString("category", ExerciseCategory.STRENGTH.name)
                val cat = runCatching { ExerciseCategory.valueOf(catStr) }.getOrDefault(ExerciseCategory.STRENGTH)

                val exList = mutableListOf<ExerciseItem>()
                val exArray = obj.optJSONArray("exercises")
                if (exArray != null) {
                    for (j in 0 until exArray.length()) {
                        val exObj = exArray.getJSONObject(j)
                        val wKg = exObj.optDouble("weightKg", -1.0)
                        exList.add(
                            ExerciseItem(
                                id = exObj.getString("id"),
                                name = exObj.getString("name"),
                                description = exObj.optString("description", ""),
                                feelingsRpe = exObj.optString("feelingsRpe", "RPE 8 / Оптимально"),
                                sets = exObj.optInt("sets", 4),
                                repsOrTime = exObj.optString("repsOrTime", "10-12 повт"),
                                weightKg = if (wKg >= 0) wKg.toFloat() else null,
                                durationMinutes = exObj.optInt("durationMinutes", 15),
                                caloriesBurned = exObj.optInt("caloriesBurned", 100)
                            )
                        )
                    }
                }

                list.add(
                    WorkoutSession(
                        id = obj.getString("id"),
                        title = obj.getString("title"),
                        description = obj.optString("description", ""),
                        date = obj.getString("date"),
                        time = obj.optString("time", "12:00"),
                        category = cat,
                        totalCaloriesBurned = obj.getInt("totalCaloriesBurned"),
                        durationMinutes = obj.getInt("durationMinutes"),
                        exercises = exList,
                        isCompleted = obj.optBoolean("isCompleted", true)
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return DefaultKineticData.createInitialWorkouts()
        }
        return list
    }

    // ==========================================
    // ГЕНЕРАЦИЯ СТАРТОВЫХ ДАННЫХ ДЛЯ 84-ДНЕВНОЙ МАТРИЦЫ
    // ==========================================

    private fun generateInitialDemoMeals(): List<MealEntry> {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        fun pastDate(daysAgo: Int): String {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -daysAgo)
            return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
        }

        val meals = mutableListOf<MealEntry>()

        // Сегодняшние блюда (1840 kcal)
        meals.add(
            MealEntry(
                id = "m_today_1",
                name = "Овсянка с миндалем и изолятом",
                type = MealType.BREAKFAST,
                calories = 540,
                protein = 38f,
                fat = 14f,
                carbs = 62f,
                time = "08:30",
                date = todayStr,
                description = "Цельнозерновой овес длительной варки, миндальные лепестки, сывороточный изолят, корица",
                weightGrams = 320
            )
        )
        meals.add(
            MealEntry(
                id = "m_today_2",
                name = "Лосось на пару, киноа и спаржа",
                type = MealType.LUNCH,
                calories = 780,
                protein = 52f,
                fat = 26f,
                carbs = 74f,
                time = "13:45",
                date = todayStr,
                description = "Филе дикого лосося на пару, перуанское белое киноа с оливковым маслом",
                weightGrams = 420
            )
        )
        meals.add(
            MealEntry(
                id = "m_today_3",
                name = "Матча-смузи с семенами чиа",
                type = MealType.SNACK,
                calories = 220,
                protein = 8f,
                fat = 6f,
                carbs = 32f,
                time = "16:20",
                date = todayStr,
                description = "Церемониальная матча Uji, кокосовое молоко, шпинат",
                weightGrams = 280
            )
        )
        meals.add(
            MealEntry(
                id = "m_today_4",
                name = "Филе индейки с бататом",
                type = MealType.DINNER,
                calories = 300,
                protein = 35f,
                fat = 6f,
                carbs = 28f,
                time = "19:50",
                date = todayStr,
                description = "Грудка индейки su-vide с розмарином, печеный батат",
                weightGrams = 300
            )
        )

        // Разнообразные исторические данные по прошлой сетке (84 дня) для демонстрации хитмапа
        val demoPattern = listOf(
            1 to 2400,  // Вчера: 100% норма
            2 to 3100,  // 2 дня назад: 130%
            3 to 750,   // 3 дня назад: 30%
            4 to 1700,  // 4 дня назад: 70%
            5 to 4100,  // 5 дней назад: 170%
            6 to 4900,  // 6 дней назад: 200%+
            8 to 2400,
            9 to 1800,
            11 to 2450,
            14 to 3200,
            16 to 1600,
            18 to 2380,
            21 to 2400,
            24 to 2900,
            28 to 2400,
            35 to 2500,
            42 to 1900,
            49 to 2400,
            56 to 3400,
            63 to 2400,
            70 to 1700,
            77 to 2400
        )

        for ((daysAgo, cals) in demoPattern) {
            val d = pastDate(daysAgo)
            meals.add(
                MealEntry(
                    id = "hist_${daysAgo}_1",
                    name = "Комплексный рацион биоцикла",
                    type = MealType.LUNCH,
                    calories = (cals * 0.6).toInt(),
                    protein = 80f,
                    fat = 40f,
                    carbs = 120f,
                    time = "13:30",
                    date = d,
                    description = "Сбалансированный обеденный комплекс макронутриентов",
                    weightGrams = 500
                )
            )
            meals.add(
                MealEntry(
                    id = "hist_${daysAgo}_2",
                    name = "Вечерний восстановительный прием",
                    type = MealType.DINNER,
                    calories = (cals * 0.4).toInt(),
                    protein = 50f,
                    fat = 25f,
                    carbs = 60f,
                    time = "19:30",
                    date = d,
                    description = "Белковый ужин для ночного анаболизма",
                    weightGrams = 350
                )
            )
        }

        return meals
    }
}
