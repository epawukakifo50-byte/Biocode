package com.biocode.app.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.biocode.ai.GeminiNutritionService
import com.biocode.engine.BiocodeBentoCard
import com.biocode.engine.BiocodeBlueprintCanvas
import com.biocode.engine.BiocodeDotMatrixText
import com.biocode.engine.BiocodePalette
import com.biocode.engine.BiocodeTypography
import com.biocode.engine.BiometricsProfile
import com.biocode.engine.DailyNutritionState
import com.biocode.engine.Gender
import com.biocode.engine.LucideCheck
import com.biocode.engine.LucideSparkles
import com.biocode.engine.LucideZap
import com.biocode.engine.TargetMetabolicMode
import kotlinx.coroutines.launch

/**
 * 🧮 4. ЭКРАН «РАСЧЕТ» (БИОМЕТРИЧЕСКИЙ ПРОФАЙЛЕР & ИИ-АНАЛИЗАТОР BMR / TDEE)
 */
@Composable
fun CalculatorScreen(
    state: DailyNutritionState,
    currentProfile: BiometricsProfile = BiometricsProfile(),
    onUpdateProfile: (BiometricsProfile) -> Unit = {},
    onUpdateTargets: (calories: Int, protein: Float, fat: Float, carbs: Float) -> Unit = { _, _, _, _ -> },
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // Биометрические поля ввода
    var selectedGender by remember { mutableStateOf(currentProfile.gender) }
    var ageStr by remember { mutableStateOf(currentProfile.age.toString()) }
    var weightStr by remember { mutableStateOf(currentProfile.weightKg.toString()) }
    var heightStr by remember { mutableStateOf(currentProfile.heightCm.toInt().toString()) }
    var selectedMode by remember { mutableStateOf(currentProfile.targetMode) }

    // Рассчитанный профиль
    var calculatedProfile by remember { mutableStateOf(currentProfile) }
    var isCalculating by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var hasAppliedTargets by remember { mutableStateOf(false) }

    // Пульсация кнопки ИИ
    val infiniteTransition = rememberInfiniteTransition(label = "calc_ai_glow")
    val aiGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BiocodePalette.DarkMoss)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                focusManager.clearFocus()
                keyboardController?.hide()
            }
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
            // Заголовок BIOCODE
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
                text = "БИОМЕТРИЧЕСКИЙ ПРОФАЙЛЕР // BMR ПОКОЯ & СУТОЧНЫЙ БАЛАНС",
                style = BiocodeTypography.TelemetryLabel,
                color = BiocodePalette.BioLime,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. БЛОК БИОМЕТРИИ ЧЕЛОВЕКА
                BiocodeBentoCard(
                    title = "ФИЗИОЛОГИЧЕСКИЕ ПАРАМЕТРЫ",
                    badgeText = "АНТРОПОМЕТРИЯ",
                    badgeColor = BiocodePalette.BioLime,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Пол: Мужской / Женский
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(Gender.MALE to "МУЖСКОЙ", Gender.FEMALE to "ЖЕНСКИЙ").forEach { (g, label) ->
                                val isSel = selectedGender == g
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSel) BiocodePalette.BioLime else BiocodePalette.DarkMoss)
                                        .border(
                                            1.dp,
                                            if (isSel) BiocodePalette.BioLime else BiocodePalette.DeckBorder,
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable {
                                            selectedGender = g
                                            focusManager.clearFocus()
                                            keyboardController?.hide()
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        style = BiocodeTypography.TelemetryLabel.copy(fontSize = 9.sp),
                                        color = if (isSel) BiocodePalette.DarkMoss else BiocodePalette.NoguchiCream,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Возраст, Вес, Рост
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = ageStr,
                                onValueChange = { ageStr = it.filter { c -> c.isDigit() }.take(3) },
                                label = { Text("Возраст", style = BiocodeTypography.TelemetryLabel) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BiocodePalette.BioLime,
                                    unfocusedBorderColor = BiocodePalette.DeckBorder,
                                    focusedTextColor = BiocodePalette.NoguchiCream,
                                    unfocusedTextColor = BiocodePalette.NoguchiCream,
                                    focusedLabelColor = BiocodePalette.BioLime,
                                    cursorColor = BiocodePalette.BioLime
                                ),
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = weightStr,
                                onValueChange = { weightStr = it.filter { c -> c.isDigit() || c == '.' }.take(5) },
                                label = { Text("Вес (кг)", style = BiocodeTypography.TelemetryLabel) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BiocodePalette.BioLime,
                                    unfocusedBorderColor = BiocodePalette.DeckBorder,
                                    focusedTextColor = BiocodePalette.NoguchiCream,
                                    unfocusedTextColor = BiocodePalette.NoguchiCream,
                                    focusedLabelColor = BiocodePalette.BioLime,
                                    cursorColor = BiocodePalette.BioLime
                                ),
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = heightStr,
                                onValueChange = { heightStr = it.filter { c -> c.isDigit() }.take(3) },
                                label = { Text("Рост (см)", style = BiocodeTypography.TelemetryLabel) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BiocodePalette.BioLime,
                                    unfocusedBorderColor = BiocodePalette.DeckBorder,
                                    focusedTextColor = BiocodePalette.NoguchiCream,
                                    unfocusedTextColor = BiocodePalette.NoguchiCream,
                                    focusedLabelColor = BiocodePalette.BioLime,
                                    cursorColor = BiocodePalette.BioLime
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // 2. ВЫБОР РЕЖИМА (ДЕФИЦИТ, ГОМЕОСТАЗ, ПРОФИЦИТ)
                BiocodeBentoCard(
                    title = "МЕТАБОЛИЧЕСКИЙ РЕЖИМ",
                    badgeText = "ЦЕЛЕВОЙ ВЕКТОР",
                    badgeColor = BiocodePalette.BioLime,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            TargetMetabolicMode.values().forEach { mode ->
                                val isSel = selectedMode == mode
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSel) BiocodePalette.BioLime else BiocodePalette.DarkMoss)
                                        .border(
                                            1.dp,
                                            if (isSel) BiocodePalette.BioLime else BiocodePalette.DeckBorder,
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable {
                                            selectedMode = mode
                                            focusManager.clearFocus()
                                            keyboardController?.hide()
                                        }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = mode.label,
                                            style = BiocodeTypography.TelemetryLabel.copy(fontSize = 8.sp),
                                            color = if (isSel) BiocodePalette.DarkMoss else BiocodePalette.NoguchiCream,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        Text(
                            text = "• ${selectedMode.desc}",
                            style = BiocodeTypography.TelemetryLabel.copy(fontSize = 8.5.sp),
                            color = BiocodePalette.BioLime
                        )
                    }
                }

                // 3. АКЦЕНТНАЯ КНОПКА «РАССЧИТАТЬ С ПОМОЩЬЮ ИИ»
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(BiocodePalette.BioLime.copy(alpha = if (isCalculating) 0.6f else aiGlowAlpha))
                        .border(1.5.dp, BiocodePalette.BioLime, RoundedCornerShape(24.dp))
                        .clickable(enabled = !isCalculating) {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                            val age = ageStr.toIntOrNull() ?: 28
                            val weight = weightStr.toFloatOrNull() ?: 78f
                            val height = heightStr.toFloatOrNull() ?: 182f

                            coroutineScope.launch {
                                isCalculating = true
                                statusMessage = "ИИ ВЫЧИСЛЯЕТ МЕТАБОЛИЧЕСКИЙ РАСХОД ПОКОЯ..."
                                val profile = GeminiNutritionService.calculateBiometricsAI(
                                    context = context,
                                    gender = selectedGender,
                                    age = age,
                                    weightKg = weight,
                                    heightCm = height,
                                    mode = selectedMode
                                )
                                calculatedProfile = profile
                                isCalculating = false
                                hasAppliedTargets = false
                                statusMessage = "РАСЧЕТ ЗАВЕРШЕН: BMR ${profile.bmrCalories} KCAL // ЦЕЛЬ ${profile.targetDailyCalories} KCAL"
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (isCalculating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = BiocodePalette.DarkMoss,
                                strokeWidth = 2.dp
                            )
                            Text(
                                text = "ИИ-РАСЧЕТ МЕТАБОЛИЗМА...",
                                style = BiocodeTypography.TabLabel.copy(fontSize = 11.sp),
                                color = BiocodePalette.DarkMoss,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            LucideSparkles(modifier = Modifier.size(17.dp), tint = BiocodePalette.DarkMoss)
                            Text(
                                text = "РАССЧИТАТЬ С ПОМОЩЬЮ ИИ",
                                style = BiocodeTypography.TabLabel.copy(fontSize = 11.sp),
                                color = BiocodePalette.DarkMoss,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Статус-сообщение ИИ
                AnimatedVisibility(
                    visible = statusMessage != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(BiocodePalette.SpruceDeck)
                            .border(1.dp, BiocodePalette.BioLime.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "⚡ ${statusMessage ?: ""}",
                            style = BiocodeTypography.TelemetryLabel.copy(fontSize = 8.sp),
                            color = BiocodePalette.BioLime
                        )
                    }
                }

                // 4. ГЛАВНЫЙ БЛОК: КАЛОРАЖ ПОКОЯ (BMR)
                BiocodeBentoCard(
                    title = "КАЛОРАЖ ПОКОЯ (BMR)",
                    badgeText = "СОСТОЯНИЕ ПОКОЯ // BASAL RATE",
                    badgeColor = BiocodePalette.BioLime,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "ЭНЕРГИЯ БАЗОВОГО ОБМЕНА",
                                    style = BiocodeTypography.TelemetryLabel.copy(fontSize = 8.5.sp),
                                    color = BiocodePalette.NoguchiCream.copy(alpha = 0.8f)
                                )
                                Text(
                                    text = "Тратится лежа без движения",
                                    style = BiocodeTypography.TelemetryLabel.copy(fontSize = 8.sp),
                                    color = BiocodePalette.BioLime
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                BiocodeDotMatrixText(
                                    text = "${calculatedProfile.bmrCalories}",
                                    dotSize = 3.0.dp,
                                    activeColor = BiocodePalette.BioLime
                                )
                                Text(
                                    text = "KCAL",
                                    style = BiocodeTypography.MonospaceTitle.copy(fontSize = 12.sp),
                                    color = BiocodePalette.BioLime,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // 5. РЕКОМЕНДОВАННЫЙ СУТОЧНЫЙ ПРИХОД И РАСХОД
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Приход (питание)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(BiocodePalette.SpruceDeck)
                            .border(1.dp, BiocodePalette.DeckBorder, RoundedCornerShape(16.dp))
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "ЦЕЛЕВОЙ ПРИХОД",
                                style = BiocodeTypography.TelemetryLabel.copy(fontSize = 7.5.sp),
                                color = BiocodePalette.BioLime
                            )
                            Text(
                                text = "${calculatedProfile.targetDailyCalories}",
                                style = BiocodeTypography.MonospaceTitle.copy(fontSize = 20.sp),
                                color = BiocodePalette.NoguchiCream
                            )
                            Text(
                                text = "Ккал / сутки (${selectedMode.label})",
                                style = BiocodeTypography.TelemetryLabel.copy(fontSize = 7.5.sp),
                                color = BiocodePalette.NoguchiCream.copy(alpha = 0.6f)
                            )
                        }
                    }

                    // Активный расход (тренировки)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(BiocodePalette.SpruceDeck)
                            .border(1.dp, BiocodePalette.DeckBorder, RoundedCornerShape(16.dp))
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "ЖЕЛАТЕЛЬНЫЙ РАСХОД",
                                style = BiocodeTypography.TelemetryLabel.copy(fontSize = 7.5.sp),
                                color = BiocodePalette.LipidAmber
                            )
                            Text(
                                text = "${calculatedProfile.targetBurnCalories}",
                                style = BiocodeTypography.MonospaceTitle.copy(fontSize = 20.sp),
                                color = BiocodePalette.LipidAmber
                            )
                            Text(
                                text = "Ккал активности / сутки",
                                style = BiocodeTypography.TelemetryLabel.copy(fontSize = 7.5.sp),
                                color = BiocodePalette.NoguchiCream.copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                // 6. РАСПРЕДЕЛЕНИЕ МАКРОНУТРИЕНТОВ
                BiocodeBentoCard(
                    title = "ЦЕЛЕВЫЕ МАКРОНУТРИЕНТЫ",
                    badgeText = "БИО-БАЛАНС",
                    badgeColor = BiocodePalette.BioLime,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("БЕЛКИ", style = BiocodeTypography.TelemetryLabel.copy(fontSize = 8.sp), color = BiocodePalette.BioLime)
                            Text("${calculatedProfile.targetProteinGrams.toInt()} г", style = BiocodeTypography.MonospaceTitle.copy(fontSize = 15.sp), color = BiocodePalette.BioLime)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("ЖИРЫ", style = BiocodeTypography.TelemetryLabel.copy(fontSize = 8.sp), color = BiocodePalette.MacroFat)
                            Text("${calculatedProfile.targetFatGrams.toInt()} г", style = BiocodeTypography.MonospaceTitle.copy(fontSize = 15.sp), color = BiocodePalette.MacroFat)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("УГЛЕВОДЫ", style = BiocodeTypography.TelemetryLabel.copy(fontSize = 8.sp), color = BiocodePalette.NoguchiCream)
                            Text("${calculatedProfile.targetCarbsGrams.toInt()} г", style = BiocodeTypography.MonospaceTitle.copy(fontSize = 15.sp), color = BiocodePalette.NoguchiCream)
                        }
                    }
                }

                // 7. КНОПКА ПРИМЕНЕНИЯ ЦЕЛЕЙ К МЕТАБОЛИЧЕСКОМУ ХАБУ
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(if (hasAppliedTargets) BiocodePalette.PineTeal else BiocodePalette.BioLime)
                        .clickable {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                            onUpdateProfile(calculatedProfile)
                            onUpdateTargets(
                                calculatedProfile.targetDailyCalories,
                                calculatedProfile.targetProteinGrams,
                                calculatedProfile.targetFatGrams,
                                calculatedProfile.targetCarbsGrams
                            )
                            hasAppliedTargets = true
                            statusMessage = "ЦЕЛИ УСПЕШНО ПРИМЕНЕНЫ К МЕТАБОЛИЧЕСКОМУ ХАБУ!"
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (hasAppliedTargets) {
                            LucideCheck(modifier = Modifier.size(16.dp), tint = BiocodePalette.BioLime)
                            Text(
                                text = "ЦЕЛИ СИНХРОНИЗИРОВАНЫ",
                                style = BiocodeTypography.TabLabel,
                                color = BiocodePalette.BioLime,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            LucideZap(modifier = Modifier.size(16.dp), tint = BiocodePalette.DarkMoss)
                            Text(
                                text = "ПРИМЕНИТЬ К МЕТАБОЛИЧЕСКОМУ ХАБУ",
                                style = BiocodeTypography.TabLabel,
                                color = BiocodePalette.DarkMoss,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(86.dp))
            }
        }
    }
}
