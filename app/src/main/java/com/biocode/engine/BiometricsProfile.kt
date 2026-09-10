package com.biocode.engine

/**
 * Биологический пол для биохимического профилирования.
 */
enum class Gender(val label: String) {
    MALE("Мужской"),
    FEMALE("Женский")
}

/**
 * Целевой метаболический режим организма.
 */
enum class TargetMetabolicMode(val label: String, val multiplier: Float, val desc: String) {
    DEFICIT("ДЕФИЦИТ", 0.85f, "-15% Снижение жирового депо"),
    HOMEOSTASIS("ГОМЕОСТАЗ", 1.0f, "Поддержание биологического баланса"),
    SURPLUS("ПРОФИЦИТ", 1.15f, "+15% Набор сухой мышечной массы")
}

/**
 * Биометрический профиль пользователя Biocode.
 */
data class BiometricsProfile(
    val gender: Gender = Gender.MALE,
    val age: Int = 28,
    val weightKg: Float = 78f,
    val heightCm: Float = 182f,
    val targetMode: TargetMetabolicMode = TargetMetabolicMode.HOMEOSTASIS,
    val bmrCalories: Int = 1810, // Калораж покоя (базовый метаболизм в состоянии покоя, будто лежит без движения)
    val targetDailyCalories: Int = 2400, // Целевое поступление калорий
    val targetBurnCalories: Int = 600, // Рекомендуемый активный расход калорий за день
    val targetProteinGrams: Float = 140f,
    val targetFatGrams: Float = 75f,
    val targetCarbsGrams: Float = 280f
) {
    fun calculateBmr(): Int = calculateBmr(gender, weightKg, heightCm, age)

    companion object {
        /**
         * Расчет BMR по формуле Миффлина-Сан Жеора (расход организма в состоянии полного покоя).
         */
        fun calculateBmr(gender: Gender, weightKg: Float, heightCm: Float, age: Int): Int {
            val base = 10f * weightKg + 6.25f * heightCm - 5f * age
            return if (gender == Gender.MALE) {
                (base + 5).toInt()
            } else {
                (base - 161).toInt()
            }
        }

        /**
         * Локальный биохимический расчет целевого профиля.
         */
        fun computeDefaultProfile(
            gender: Gender,
            weightKg: Float,
            heightCm: Float,
            age: Int,
            mode: TargetMetabolicMode
        ): BiometricsProfile {
            val bmr = calculateBmr(gender, weightKg, heightCm, age)
            val baseTdee = (bmr * 1.35f).toInt() // Средняя активность
            val targetCalories = (baseTdee * mode.multiplier).toInt()
            val targetBurn = (bmr * 0.35f).toInt().coerceAtLeast(400) // Желательный активный расход

            val protein = (weightKg * 1.8f).coerceIn(90f, 220f)
            val fat = (weightKg * 0.9f).coerceIn(45f, 110f)
            val remainingCalForCarbs = (targetCalories - (protein * 4 + fat * 9)).coerceAtLeast(150f)
            val carbs = remainingCalForCarbs / 4f

            return BiometricsProfile(
                gender = gender,
                age = age,
                weightKg = weightKg,
                heightCm = heightCm,
                targetMode = mode,
                bmrCalories = bmr,
                targetDailyCalories = targetCalories,
                targetBurnCalories = targetBurn,
                targetProteinGrams = protein,
                targetFatGrams = fat,
                targetCarbsGrams = carbs
            )
        }
    }
}
