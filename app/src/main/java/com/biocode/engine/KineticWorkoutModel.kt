package com.biocode.engine

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Режим верхнего хаба приложения Biocode.
 */
enum class HubMode(val label: String, val badge: String) {
    METABOLIC("METABOLIC HUB", "ПИТАНИЕ // БИОЦИКЛ"),
    KINETIC("KINETIC HUB", "ТРЕНИРОВКИ // БИОМЕХАНИКА")
}

/**
 * Категория кинетической нагрузки.
 */
enum class ExerciseCategory(val label: String, val code: String) {
    STRENGTH("Силовой комплекс", "STR-01"),
    CARDIO("Кардио-интенсив", "CRD-02"),
    HYPERTROPHY("Гипертрофия", "HYP-03"),
    RECOVERY("Мобильность / Фасции", "REC-04")
}

/**
 * Отдельное упражнение внутри цельного тренировочного комплекса.
 */
data class ExerciseItem(
    val id: String,
    val name: String,
    val description: String = "",
    val feelingsRpe: String = "RPE 8 / Оптимально", // Ощущения / шкала воспринимаемого напряжения
    val sets: Int = 4,
    val repsOrTime: String = "10-12 повт",
    val weightKg: Float? = 70f,
    val durationMinutes: Int = 12,
    val caloriesBurned: Int = 95
)

/**
 * Цельный тренировочный комплекс (сессия) за день.
 */
data class WorkoutSession(
    val id: String,
    val title: String, // Название комплекса: e.g. "Грудные мышцы & Биомеханика жима"
    val description: String = "",
    val date: String, // YYYY-MM-DD
    val time: String = "11:30",
    val category: ExerciseCategory = ExerciseCategory.STRENGTH,
    val totalCaloriesBurned: Int,
    val durationMinutes: Int,
    val exercises: List<ExerciseItem> = emptyList(),
    val isCompleted: Boolean = true
)

/**
 * Запланированный тренировочный день в конструкторе сплитов.
 */
data class WorkoutPlanDay(
    val dayNumber: Int,
    val title: String,
    val isRestDay: Boolean = false,
    val session: WorkoutSession? = null,
    val aiRecommendation: String = ""
)

object DefaultKineticData {
    private fun getTodayDate(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    private fun getYesterdayDate(): String {
        val cal = java.util.Calendar.getInstance()
        cal.add(java.util.Calendar.DAY_OF_YEAR, -1)
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
    }

    fun createInitialWorkouts(): List<WorkoutSession> {
        val today = getTodayDate()
        val yesterday = getYesterdayDate()

        return listOf(
            WorkoutSession(
                id = "w1",
                title = "Грудной отдел & Дельты // Гипертрофия",
                description = "Акцент на верхнюю порцию пекторальных мышц и латеральные пучки дельт. Контроль эксцентрической фазы 3 сек.",
                date = today,
                time = "10:30",
                category = ExerciseCategory.HYPERTROPHY,
                totalCaloriesBurned = 460,
                durationMinutes = 55,
                exercises = listOf(
                    ExerciseItem(
                        id = "e1",
                        name = "Жим гантелей на наклонной скамье 30°",
                        description = "Угол 30°, сведение лопаток, пауза 1 сек в нижней точке",
                        feelingsRpe = "RPE 8.5 / Мощный памп",
                        sets = 4,
                        repsOrTime = "10 повт",
                        weightKg = 32f,
                        durationMinutes = 15,
                        caloriesBurned = 145
                    ),
                    ExerciseItem(
                        id = "e2",
                        name = "Кроссовер на блоках снизу вверх",
                        description = "Фокус на ключичную порцию большой грудной",
                        feelingsRpe = "RPE 8 / Пиковое сокращение",
                        sets = 3,
                        repsOrTime = "12 повт",
                        weightKg = 18f,
                        durationMinutes = 12,
                        caloriesBurned = 95
                    ),
                    ExerciseItem(
                        id = "e3",
                        name = "Махи гантелями в стороны стоя",
                        description = "Трапеции выключены, локти чуть согнуты",
                        feelingsRpe = "RPE 9 / Жжение в дельтах",
                        sets = 4,
                        repsOrTime = "15 повт",
                        weightKg = 14f,
                        durationMinutes = 14,
                        caloriesBurned = 110
                    ),
                    ExerciseItem(
                        id = "e4",
                        name = "Французский жим с EZ-штангой",
                        description = "Локти зафиксированы, глубокое растяжение",
                        feelingsRpe = "RPE 8 / Стабильно",
                        sets = 3,
                        repsOrTime = "12 повт",
                        weightKg = 35f,
                        durationMinutes = 14,
                        caloriesBurned = 110
                    )
                )
            ),
            WorkoutSession(
                id = "w2",
                title = "HIIT Спринт & Метаболическая кондиция",
                description = "Интервальный протокол 30/30 сек на гребном эргометре Concept2 и аэробайке",
                date = yesterday,
                time = "18:15",
                category = ExerciseCategory.CARDIO,
                totalCaloriesBurned = 520,
                durationMinutes = 40,
                exercises = listOf(
                    ExerciseItem(
                        id = "e5",
                        name = "Гребной эргометр Concept2",
                        description = "Интервалы 30с макс ускорение / 30с легкая гребля",
                        feelingsRpe = "RPE 9.5 / Максимальный пульс",
                        sets = 10,
                        repsOrTime = "30/30 сек",
                        weightKg = null,
                        durationMinutes = 20,
                        caloriesBurned = 280
                    ),
                    ExerciseItem(
                        id = "e6",
                        name = "AirBike Assault",
                        description = "Поддержание мощности > 400 Ватт",
                        feelingsRpe = "RPE 9 / Лактатный взрыв",
                        sets = 8,
                        repsOrTime = "20/40 сек",
                        weightKg = null,
                        durationMinutes = 20,
                        caloriesBurned = 240
                    )
                )
            )
        )
    }

    fun createInitialPlan(): List<WorkoutPlanDay> {
        return listOf(
            WorkoutPlanDay(
                dayNumber = 1,
                title = "ДЕНЬ 01: ГРУДЬ & ПЛЕЧИ",
                isRestDay = false,
                aiRecommendation = "Идеально после углеводного дня. Рекомендуется повышенное потребление натрия и гидратация."
            ),
            WorkoutPlanDay(
                dayNumber = 2,
                title = "ДЕНЬ 02: СПИНА & БИЦЕПС",
                isRestDay = false,
                aiRecommendation = "Тяговый вектор. Фокус на широчайшие и заднюю дельту."
            ),
            WorkoutPlanDay(
                dayNumber = 3,
                title = "ДЕНЬ 03: БИО-ВОССТАНОВЛЕНИЕ // REST",
                isRestDay = true,
                aiRecommendation = "Фаза синтеза гликогена. Контрастный душ, растяжка, прогулка на свежем воздухе."
            ),
            WorkoutPlanDay(
                dayNumber = 4,
                title = "ДЕНЬ 04: НОГИ // КВАДРИЦЕПС & ИКРЫ",
                isRestDay = false,
                aiRecommendation = "Высочайший энергорасход недели. Увеличьте сложные углеводы на 15%."
            ),
            WorkoutPlanDay(
                dayNumber = 5,
                title = "ДЕНЬ 05: HIIT КАРДИО & КОР",
                isRestDay = false,
                aiRecommendation = "Кардиореспираторный стимул. 35 мин интервалов."
            ),
            WorkoutPlanDay(
                dayNumber = 6,
                title = "ДЕНЬ 06: РУКИ // БИЦЕПС & ТРИЦЕПС СУПЕРСЕТЫ",
                isRestDay = false,
                aiRecommendation = "Антагонистические суперсеты для максимального капиллярного кровенаполнения."
            ),
            WorkoutPlanDay(
                dayNumber = 7,
                title = "ДЕНЬ 07: ПОЛНЫЙ ГОМЕОСТАЗ // REST",
                isRestDay = true,
                aiRecommendation = "Глубокая фаза суперкомпенсации. Сауна / миофасциальный релиз."
            )
        )
    }
}
