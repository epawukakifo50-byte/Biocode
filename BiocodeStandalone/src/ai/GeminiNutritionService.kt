package com.biocode.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

/**
 * Результат биометрического анализа блюда нейросетью Gemini или оффлайн-движком
 */
data class AiNutritionResult(
    val name: String,
    val description: String,
    val weightGrams: Int,
    val calories: Int,
    val protein: Float,
    val fat: Float,
    val carbs: Float,
    val isRealAi: Boolean,
    val statusMessage: String
)

object GeminiNutritionService {
    private const val TAG = "GeminiNutritionService"
    private const val PREFS_NAME = "biocode_ai_prefs"
    private const val KEY_API_TOKEN = "gemini_api_key"

    private const val GEMINI_MODEL = "gemini-2.5-flash"
    private const val FALLBACK_MODEL = "gemini-1.5-flash"

    fun getApiKey(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_API_TOKEN, "")?.trim() ?: ""
    }

    fun saveApiKey(context: Context, key: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_API_TOKEN, key.trim()).apply()
    }

    /**
     * Анализ блюда по тексту, граммам и опциональному фото через Google Gemini API
     */
    suspend fun analyzeDish(
        context: Context,
        rawName: String,
        rawDescription: String,
        rawWeightGrams: Int?,
        photoUriStr: String?
    ): AiNutritionResult = withContext(Dispatchers.IO) {
        val apiKey = getApiKey(context)

        // Если ключ не задан — запускаем локальный биохимический движок
        if (apiKey.isBlank()) {
            return@withContext runBiochemicalHeuristic(rawName, rawDescription, rawWeightGrams, false)
        }

        try {
            val result = callGeminiApi(context, apiKey, GEMINI_MODEL, rawName, rawDescription, rawWeightGrams, photoUriStr)
                ?: callGeminiApi(context, apiKey, FALLBACK_MODEL, rawName, rawDescription, rawWeightGrams, photoUriStr)
                ?: runBiochemicalHeuristic(rawName, rawDescription, rawWeightGrams, true)
            result
        } catch (e: Exception) {
            Log.e(TAG, "Gemini API error: ${e.message}", e)
            runBiochemicalHeuristic(rawName, rawDescription, rawWeightGrams, true)
        }
    }

    private fun callGeminiApi(
        context: Context,
        apiKey: String,
        model: String,
        name: String,
        desc: String,
        weight: Int?,
        photoUriStr: String?
    ): AiNutritionResult? {
        val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

        val promptText = buildString {
            appendLine("Ты — высокоточный биохимический ИИ-нутрициолог приложения Biocode Engine.")
            appendLine("Твоя задача — детально проанализировать блюдо (по фото, если предоставлено, названию, описанию и весу) и рассчитать его пищевую ценность КБЖУ.")
            appendLine("Входные данные от пользователя:")
            val cleanName = if (name.isBlank()) "Блюдо на фото или не указано" else name
            val cleanDesc = if (desc.isBlank()) "Не указано" else desc
            val cleanWeight = if (weight != null) "$weight г" else "Определи визуально или типичную ресторанную порцию"
            appendLine("- Название: $cleanName")
            appendLine("- Описание/ингредиенты: $cleanDesc")
            appendLine("- Указанный вес: $cleanWeight")
            appendLine()
            appendLine("Верни СТРОГО валидный JSON следующей структуры (без лишнего markdown текста вокруг):")
            appendLine("{")
            appendLine("  \"name\": \"Четкое и аппетитное название блюда\",")
            appendLine("  \"description\": \"Краткое биохимическое описание ключевых нутриентов и ингредиентов\",")
            appendLine("  \"weightGrams\": 320,")
            appendLine("  \"calories\": 480,")
            appendLine("  \"protein\": 34.5,")
            appendLine("  \"fat\": 15.0,")
            appendLine("  \"carbs\": 52.0")
            appendLine("}")
        }

        val partsArray = JSONArray()
        partsArray.put(JSONObject().put("text", promptText))

        // Если передано фото — пережимаем и отправляем inline_data
        if (!photoUriStr.isNullOrBlank()) {
            val base64Image = encodeImageUriToBase64(context, Uri.parse(photoUriStr))
            if (base64Image != null) {
                val imagePart = JSONObject().apply {
                    put("inline_data", JSONObject().apply {
                        put("mime_type", "image/jpeg")
                        put("data", base64Image)
                    })
                }
                partsArray.put(imagePart)
            }
        }

        val contentObj = JSONObject().put("parts", partsArray)
        val requestBody = JSONObject().apply {
            put("contents", JSONArray().put(contentObj))
            put("generationConfig", JSONObject().apply {
                put("response_mime_type", "application/json")
                put("temperature", 0.2)
            })
        }

        val url = URL(endpoint)
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            connectTimeout = 15000
            readTimeout = 25000
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
        }

        try {
            OutputStreamWriter(conn.outputStream, "UTF-8").use { writer ->
                writer.write(requestBody.toString())
                writer.flush()
            }

            val responseCode = conn.responseCode
            if (responseCode !in 200..299) {
                val err = conn.errorStream?.bufferedReader()?.readText()
                Log.w(TAG, "API returned $responseCode: $err")
                return null
            }

            val responseText = conn.inputStream.bufferedReader().readText()
            val root = JSONObject(responseText)
            val candidates = root.optJSONArray("candidates") ?: return null
            if (candidates.length() == 0) return null

            val candidate = candidates.getJSONObject(0)
            val candidateContent = candidate.optJSONObject("content") ?: return null
            val respParts = candidateContent.optJSONArray("parts") ?: return null
            if (respParts.length() == 0) return null

            val rawOutput = respParts.getJSONObject(0).optString("text", "")
            if (rawOutput.isBlank()) return null

            val cleanJson = rawOutput.trim()
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()

            val parsed = JSONObject(cleanJson)
            val resName = parsed.optString("name", if (name.isBlank()) "Прием пищи" else name)
            val resDesc = parsed.optString("description", desc)
            val resWeight = parsed.optInt("weightGrams", weight ?: 300)
            val resCal = parsed.optInt("calories", 350)
            val resProt = parsed.optDouble("protein", 20.0).toFloat()
            val resFat = parsed.optDouble("fat", 10.0).toFloat()
            val resCarbs = parsed.optDouble("carbs", 35.0).toFloat()

            return AiNutritionResult(
                name = resName,
                description = resDesc,
                weightGrams = resWeight,
                calories = resCal,
                protein = resProt,
                fat = resFat,
                carbs = resCarbs,
                isRealAi = true,
                statusMessage = "Рассчитано нейросетью Gemini (Vision)"
            )
        } finally {
            conn.disconnect()
        }
    }

    /**
     * Интеллектуальный биохимический движок (эвристика) на случай отсутствия сети / ключа.
     * Анализирует ключевые слова блюда, граммовку и рассчитывает реальные макросы.
     */
    private fun runBiochemicalHeuristic(
        rawName: String,
        rawDescription: String,
        rawWeight: Int?,
        hadApiKeyError: Boolean
    ): AiNutritionResult {
        val query = "${rawName.lowercase()} ${rawDescription.lowercase()}".trim()
        val weight = rawWeight ?: when {
            query.contains("салат") -> 220
            query.contains("стейк") || query.contains("мясо") -> 280
            query.contains("суп") || query.contains("борщ") -> 350
            query.contains("пицца") -> 350
            query.contains("смузи") || query.contains("шейк") -> 300
            else -> 300
        }

        // Плотность нутриентов на 100 грамм по паттернам блюд
        data class MacroDensity(val kcal: Float, val p: Float, val f: Float, val c: Float)

        val density = when {
            query.contains("лосось") || query.contains("форель") || query.contains("рыб") ->
                MacroDensity(175f, 19f, 9f, 4f)
            query.contains("куриц") || query.contains("индейк") || query.contains("филе") || query.contains("грудк") ->
                MacroDensity(145f, 24f, 4f, 2f)
            query.contains("говядин") || query.contains("стейк") || query.contains("мясо") ->
                MacroDensity(215f, 22f, 13f, 1f)
            query.contains("паста") || query.contains("макарон") || query.contains("спагетти") ->
                MacroDensity(165f, 6f, 5f, 24f)
            query.contains("рис") || query.contains("гречк") || query.contains("киноа") ->
                MacroDensity(130f, 4.5f, 2f, 24f)
            query.contains("овсянк") || query.contains("каша") || query.contains("геркулес") ->
                MacroDensity(110f, 4f, 2.5f, 18f)
            query.contains("яйц") || query.contains("омлет") || query.contains("яичниц") ->
                MacroDensity(155f, 13f, 11f, 1.2f)
            query.contains("творог") || query.contains("сырник") ->
                MacroDensity(140f, 16f, 5f, 6f)
            query.contains("протеин") || query.contains("изолят") || query.contains("шейк") ->
                MacroDensity(120f, 24f, 1.5f, 2.5f)
            query.contains("пицц") || query.contains("бургер") || query.contains("шаурм") ->
                MacroDensity(245f, 11f, 12f, 24f)
            query.contains("салат") ->
                MacroDensity(75f, 2.5f, 5f, 5f)
            query.contains("суп") || query.contains("борщ") ->
                MacroDensity(60f, 3.5f, 2.5f, 6f)
            else ->
                MacroDensity(140f, 8f, 5f, 16f)
        }

        val factor = weight.toFloat() / 100f
        val calories = (density.kcal * factor).toInt()
        val protein = ((density.p * factor) * 10).toInt() / 10f
        val fat = ((density.f * factor) * 10).toInt() / 10f
        val carbs = ((density.c * factor) * 10).toInt() / 10f

        val finalName = if (rawName.isBlank()) "Биометрический ланч" else rawName
        val finalDesc = if (rawDescription.isNotBlank()) rawDescription else "Энергетический комплекс нутриентов с расчетом по био-матрице"

        val status = if (hadApiKeyError) {
            "Biocode Heuristics (сеть/ключ недоступен)"
        } else {
            "Biocode Biochemical Engine (оффлайн режим)"
        }

        return AiNutritionResult(
            name = finalName,
            description = finalDesc,
            weightGrams = weight,
            calories = calories,
            protein = protein,
            fat = fat,
            carbs = carbs,
            isRealAi = false,
            statusMessage = status
        )
    }

    /**
     * Сжатие изображения и конвертация в Base64 для передачи в Gemini API
     */
    private fun encodeImageUriToBase64(context: Context, uri: Uri): String? {
        return try {
            val inputStream: InputStream = context.contentResolver.openInputStream(uri) ?: return null
            val original = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            if (original == null) return null

            val maxDimension = 1024
            val scaled = if (original.width > maxDimension || original.height > maxDimension) {
                val ratio = minOf(maxDimension.toFloat() / original.width, maxDimension.toFloat() / original.height)
                Bitmap.createScaledBitmap(original, (original.width * ratio).toInt(), (original.height * ratio).toInt(), true)
            } else {
                original
            }

            val byteStream = ByteArrayOutputStream()
            scaled.compress(Bitmap.CompressFormat.JPEG, 85, byteStream)
            Base64.encodeToString(byteStream.toByteArray(), Base64.NO_WRAP)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to encode photo uri $uri: ${e.message}")
            null
        }
    }

    /**
     * Расчет биометрического профиля и калоража покоя (BMR) через ИИ Gemini
     */
    suspend fun calculateBiometricsAI(
        context: Context,
        gender: com.biocode.engine.Gender,
        age: Int,
        weightKg: Float,
        heightCm: Float,
        mode: com.biocode.engine.TargetMetabolicMode
    ): com.biocode.engine.BiometricsProfile = withContext(Dispatchers.IO) {
        val apiKey = getApiKey(context)
        val defaultProfile = com.biocode.engine.BiometricsProfile.computeDefaultProfile(gender, weightKg, heightCm, age, mode)

        if (apiKey.isBlank()) {
            return@withContext defaultProfile
        }

        try {
            val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/$GEMINI_MODEL:generateContent?key=$apiKey"
            val prompt = buildString {
                appendLine("Ты — биохимический физиолог и спортивный метаболограф Biocode.")
                appendLine("Рассчитай научно обоснованный метаболический профиль человека:")
                appendLine("- Пол: ${gender.label}")
                appendLine("- Возраст: $age лет")
                appendLine("- Вес: $weightKg кг")
                appendLine("- Рост: $heightCm см")
                appendLine("- Режим: ${mode.label} (${mode.desc})")
                appendLine("КРИТИЧЕСКИ ВАЖНО рассчитать:")
                appendLine("1. bmrCalories: Базовый метаболизм в состоянии абсолютного покоя (будто человек просто лежит целый день и ничего не делает).")
                appendLine("2. targetDailyCalories: целевой суточный калораж для выбранного режима.")
                appendLine("3. targetBurnCalories: рекомендуемый активный расход энергии на день.")
                appendLine("4. protein, fat, carbs: распределение макронутриентов в граммах.")
                appendLine("Ответь СТРОГО в формате JSON без markdown:")
                appendLine("{\"bmrCalories\": 1800, \"targetDailyCalories\": 2400, \"targetBurnCalories\": 600, \"protein\": 150, \"fat\": 75, \"carbs\": 280}")
            }

            val reqJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", prompt) })
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("response_mime_type", "application_json")
                    put("temperature", 0.2)
                })
            }

            val conn = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                doOutput = true
                connectTimeout = 8000
                readTimeout = 10000
            }

            OutputStreamWriter(conn.outputStream, "UTF-8").use { it.write(reqJson.toString()) }

            if (conn.responseCode == 200) {
                val respText = conn.inputStream.bufferedReader().use { it.readText() }
                val root = JSONObject(respText)
                val cand = root.getJSONArray("candidates").getJSONObject(0)
                val content = cand.getJSONObject("content")
                val text = content.getJSONArray("parts").getJSONObject(0).getString("text")
                val json = JSONObject(text)

                return@withContext com.biocode.engine.BiometricsProfile(
                    gender = gender,
                    age = age,
                    weightKg = weightKg,
                    heightCm = heightCm,
                    targetMode = mode,
                    bmrCalories = json.optInt("bmrCalories", defaultProfile.bmrCalories),
                    targetDailyCalories = json.optInt("targetDailyCalories", defaultProfile.targetDailyCalories),
                    targetBurnCalories = json.optInt("targetBurnCalories", defaultProfile.targetBurnCalories),
                    targetProteinGrams = json.optDouble("protein", defaultProfile.targetProteinGrams.toDouble()).toFloat(),
                    targetFatGrams = json.optDouble("fat", defaultProfile.targetFatGrams.toDouble()).toFloat(),
                    targetCarbsGrams = json.optDouble("carbs", defaultProfile.targetCarbsGrams.toDouble()).toFloat()
                )
            } else {
                defaultProfile
            }
        } catch (e: Exception) {
            Log.e(TAG, "Biometrics AI error: ${e.message}")
            defaultProfile
        }
    }

    /**
     * Расчет сожженных калорий в упражнении через ИИ Gemini с учетом биометрии
     */
    suspend fun calculateExerciseBurnAI(
        context: Context,
        exerciseName: String,
        description: String,
        feelingsRpe: String,
        sets: Int,
        repsOrTime: String,
        durationMinutes: Int,
        weightKg: Float
    ): Int = withContext(Dispatchers.IO) {
        val apiKey = getApiKey(context)

        // Оффлайн fallback по формуле MET
        val isCardio = exerciseName.contains("бег", true) || exerciseName.contains("кардио", true) ||
                exerciseName.contains("гребл", true) || exerciseName.contains("велик", true) ||
                exerciseName.contains("эргометр", true) || exerciseName.contains("hiit", true)
        val met = if (isCardio) 8.5f else 5.5f
        val fallbackBurn = (met * weightKg * (durationMinutes / 60f)).toInt().coerceIn(30, 1200)

        if (apiKey.isBlank()) {
            return@withContext fallbackBurn
        }

        try {
            val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/$GEMINI_MODEL:generateContent?key=$apiKey"
            val prompt = buildString {
                appendLine("Ты — физиолог физических нагрузок Biocode Kinetic Engine.")
                appendLine("Рассчитай расход энергии (ккал) для тренировочного упражнения с учетом биометрии спортсмена:")
                appendLine("- Вес спортсмена: $weightKg кг")
                appendLine("- Упражнение: $exerciseName")
                appendLine("- Описание и биомеханика: $description")
                appendLine("- Субъективные ощущения/RPE: $feelingsRpe")
                appendLine("- Подходы: $sets, повторения/время: $repsOrTime")
                appendLine("- Время выполнения: $durationMinutes минут")
                appendLine("Ответь СТРОГО в формате JSON без markdown:")
                appendLine("{\"caloriesBurned\": 120}")
            }

            val reqJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", prompt) })
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("response_mime_type", "application_json")
                    put("temperature", 0.2)
                })
            }

            val conn = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                doOutput = true
                connectTimeout = 8000
                readTimeout = 10000
            }

            OutputStreamWriter(conn.outputStream, "UTF-8").use { it.write(reqJson.toString()) }

            if (conn.responseCode == 200) {
                val respText = conn.inputStream.bufferedReader().use { it.readText() }
                val root = JSONObject(respText)
                val cand = root.getJSONArray("candidates").getJSONObject(0)
                val content = cand.getJSONObject("content")
                val text = content.getJSONArray("parts").getJSONObject(0).getString("text")
                val json = JSONObject(text)
                json.optInt("caloriesBurned", fallbackBurn)
            } else {
                fallbackBurn
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exercise AI error: ${e.message}")
            fallbackBurn
        }
    }
}

