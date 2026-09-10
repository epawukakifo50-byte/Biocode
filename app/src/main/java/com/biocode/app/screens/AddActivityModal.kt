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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.biocode.ai.GeminiNutritionService
import com.biocode.engine.BiocodePalette
import com.biocode.engine.BiocodeTypography
import com.biocode.engine.BiometricsProfile
import com.biocode.engine.ExerciseCategory
import com.biocode.engine.ExerciseItem
import com.biocode.engine.LucideCheck
import com.biocode.engine.LucideDumbbell
import com.biocode.engine.LucidePlus
import com.biocode.engine.LucideSparkles
import com.biocode.engine.LucideX
import kotlinx.coroutines.launch

/**
 * 🏋️ МОДАЛЬНОЕ ОКНО «ДОБАВИТЬ АКТИВНОСТЬ» (KINETIC ACTIVITY & AI LOAD CALCULATOR)
 */
@Composable
fun AddActivityModal(
    userProfile: BiometricsProfile,
    onDismiss: () -> Unit,
    onSaveExercise: (ExerciseItem, ExerciseCategory) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // Поля ввода активности
    var exerciseName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var feelingsRpe by remember { mutableStateOf("RPE 8 / Оптимально") }
    var setsStr by remember { mutableStateOf("4") }
    var repsStr by remember { mutableStateOf("10-12 повт") }
    var durationMinutesStr by remember { mutableStateOf("15") }
    var weightKgStr by remember { mutableStateOf("60") }
    var caloriesBurnedStr by remember { mutableStateOf("120") }
    var selectedCategory by remember { mutableStateOf(ExerciseCategory.STRENGTH) }

    var isAiCalculating by remember { mutableStateOf(false) }
    var aiStatusBanner by remember { mutableStateOf<String?>(null) }

    // Пульсация неоновой кнопки
    val infiniteTransition = rememberInfiniteTransition(label = "activity_ai_glow")
    val aiGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "act_glow"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(26.dp))
                .background(BiocodePalette.DarkMoss)
                .border(1.5.dp, BiocodePalette.PineTeal, RoundedCornerShape(26.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                }
                .padding(horizontal = 18.dp, vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. ШАПКА МОДАЛА
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(BiocodePalette.PineTeal),
                            contentAlignment = Alignment.Center
                        ) {
                            LucideDumbbell(modifier = Modifier.size(16.dp), tint = BiocodePalette.BioLime)
                        }
                        Column {
                            Text(
                                text = "НОВАЯ АКТИВНОСТЬ",
                                style = BiocodeTypography.TabLabel,
                                color = BiocodePalette.NoguchiCream,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "KINETIC HUB // БИОМЕХАНИКА",
                                style = BiocodeTypography.TelemetryLabel.copy(fontSize = 8.sp),
                                color = BiocodePalette.BioLime
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(BiocodePalette.SpruceDeck)
                            .border(1.dp, BiocodePalette.DeckBorder.copy(alpha = 0.5f), CircleShape)
                            .clickable { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        LucideX(modifier = Modifier.size(16.dp), tint = BiocodePalette.NoguchiCream)
                    }
                }

                // 2. ВЫБОР КАТЕГОРИИ НАГРУЗКИ
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ExerciseCategory.values().forEach { cat ->
                        val isSel = selectedCategory == cat
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSel) BiocodePalette.BioLime else BiocodePalette.PineTeal.copy(alpha = 0.4f))
                                .border(
                                    1.dp,
                                    if (isSel) BiocodePalette.BioLime else BiocodePalette.DeckBorder.copy(alpha = 0.4f),
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    selectedCategory = cat
                                    focusManager.clearFocus()
                                    keyboardController?.hide()
                                }
                                .padding(vertical = 7.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = cat.label.split(" ").first().uppercase(),
                                style = BiocodeTypography.TelemetryLabel.copy(fontSize = 8.5.sp),
                                color = if (isSel) BiocodePalette.DarkMoss else BiocodePalette.NoguchiCream,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                    }
                }

                // 3. НАЗВАНИЕ УПРАЖНЕНИЯ
                OutlinedTextField(
                    value = exerciseName,
                    onValueChange = { exerciseName = it },
                    label = { Text("Название упражнения / активности", style = BiocodeTypography.TelemetryLabel) },
                    placeholder = { Text("Например: Жим гантелей, Спринт, Приседания...", style = BiocodeTypography.TelemetryLabel.copy(color = Color.Gray)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BiocodePalette.BioLime,
                        unfocusedBorderColor = BiocodePalette.DeckBorder,
                        focusedTextColor = BiocodePalette.NoguchiCream,
                        unfocusedTextColor = BiocodePalette.NoguchiCream,
                        focusedLabelColor = BiocodePalette.BioLime,
                        cursorColor = BiocodePalette.BioLime
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // 4. ОПИСАНИЕ И ОЩУЩЕНИЯ
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Краткое описание / биомеханика", style = BiocodeTypography.TelemetryLabel) },
                    placeholder = { Text("Акцент на верхний пучок, пауза 1 сек в нижней точке...", style = BiocodeTypography.TelemetryLabel.copy(color = Color.Gray)) },
                    singleLine = false,
                    maxLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BiocodePalette.BioLime,
                        unfocusedBorderColor = BiocodePalette.DeckBorder,
                        focusedTextColor = BiocodePalette.NoguchiCream,
                        unfocusedTextColor = BiocodePalette.NoguchiCream,
                        focusedLabelColor = BiocodePalette.BioLime,
                        cursorColor = BiocodePalette.BioLime
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // 5. СУБЪЕКТИВНЫЕ ОЩУЩЕНИЯ / RPE
                OutlinedTextField(
                    value = feelingsRpe,
                    onValueChange = { feelingsRpe = it },
                    label = { Text("Ощущения / Уровень напряжения (RPE)", style = BiocodeTypography.TelemetryLabel) },
                    placeholder = { Text("Например: RPE 8.5 / Мощный памп, жжение", style = BiocodeTypography.TelemetryLabel.copy(color = Color.Gray)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BiocodePalette.BioLime,
                        unfocusedBorderColor = BiocodePalette.DeckBorder,
                        focusedTextColor = BiocodePalette.NoguchiCream,
                        unfocusedTextColor = BiocodePalette.NoguchiCream,
                        focusedLabelColor = BiocodePalette.BioLime,
                        cursorColor = BiocodePalette.BioLime
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // 6. ПОДХОДЫ, ПОВТОРЕНИЯ И ВРЕМЯ
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedTextField(
                        value = setsStr,
                        onValueChange = { setsStr = it.filter { c -> c.isDigit() } },
                        label = { Text("Подходы", style = BiocodeTypography.TelemetryLabel) },
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
                        value = repsStr,
                        onValueChange = { repsStr = it },
                        label = { Text("Повторы/время", style = BiocodeTypography.TelemetryLabel) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BiocodePalette.BioLime,
                            unfocusedBorderColor = BiocodePalette.DeckBorder,
                            focusedTextColor = BiocodePalette.NoguchiCream,
                            unfocusedTextColor = BiocodePalette.NoguchiCream,
                            focusedLabelColor = BiocodePalette.BioLime,
                            cursorColor = BiocodePalette.BioLime
                        ),
                        modifier = Modifier.weight(1.3f)
                    )

                    OutlinedTextField(
                        value = durationMinutesStr,
                        onValueChange = { durationMinutesStr = it.filter { c -> c.isDigit() } },
                        label = { Text("Мин", style = BiocodeTypography.TelemetryLabel) },
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
                        modifier = Modifier.weight(0.9f)
                    )
                }

                // Быстрые чипсы минут
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("10 мин" to "10", "15 мин" to "15", "30 мин" to "30", "45 мин" to "45").forEach { (label, minVal) ->
                        val isSel = durationMinutesStr == minVal
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) BiocodePalette.BioLime else BiocodePalette.SpruceDeck)
                                .border(1.dp, if (isSel) BiocodePalette.BioLime else BiocodePalette.DeckBorder, RoundedCornerShape(8.dp))
                                .clickable {
                                    durationMinutesStr = minVal
                                    focusManager.clearFocus()
                                    keyboardController?.hide()
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = BiocodeTypography.TelemetryLabel.copy(fontSize = 8.5.sp),
                                color = if (isSel) BiocodePalette.DarkMoss else BiocodePalette.NoguchiCream,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // 7. АКЦЕНТНАЯ КНОПКА «РАССЧИТАТЬ НАГРУЗКУ (ИИ)»
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(BiocodePalette.BioLime.copy(alpha = if (isAiCalculating) 0.6f else aiGlowAlpha))
                        .border(1.5.dp, BiocodePalette.BioLime, RoundedCornerShape(24.dp))
                        .clickable(enabled = !isAiCalculating) {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                            val duration = durationMinutesStr.toIntOrNull() ?: 15
                            val sets = setsStr.toIntOrNull() ?: 4

                            coroutineScope.launch {
                                isAiCalculating = true
                                aiStatusBanner = "ИИ ВЫЧИСЛЯЕТ ЭНЕРГОРАСХОД С УЧЕТОМ ВЕСА ${userProfile.weightKg} КГ..."
                                val burn = GeminiNutritionService.calculateExerciseBurnAI(
                                    context = context,
                                    exerciseName = exerciseName.ifBlank { "Силовое упражнение" },
                                    description = description,
                                    feelingsRpe = feelingsRpe,
                                    sets = sets,
                                    repsOrTime = repsStr,
                                    durationMinutes = duration,
                                    weightKg = userProfile.weightKg
                                )
                                caloriesBurnedStr = burn.toString()
                                isAiCalculating = false
                                aiStatusBanner = "РАСЧЕТ ВЫПОЛНЕН: РАСХОД ~$burn KCAL С УЧЕТОМ БИОМЕТРИИ"
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (isAiCalculating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = BiocodePalette.DarkMoss,
                                strokeWidth = 2.dp
                            )
                            Text(
                                text = "АНАЛИЗ БИОМЕХАНИКИ НАГРУЗКИ...",
                                style = BiocodeTypography.TabLabel.copy(fontSize = 11.sp),
                                color = BiocodePalette.DarkMoss,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            LucideSparkles(modifier = Modifier.size(17.dp), tint = BiocodePalette.DarkMoss)
                            Text(
                                text = "РАССЧИТАТЬ НАГРУЗКУ (ИИ)",
                                style = BiocodeTypography.TabLabel.copy(fontSize = 11.sp),
                                color = BiocodePalette.DarkMoss,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Статус-сообщение ИИ
                AnimatedVisibility(
                    visible = aiStatusBanner != null,
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
                            text = "⚡ ${aiStatusBanner ?: ""}",
                            style = BiocodeTypography.TelemetryLabel.copy(fontSize = 8.sp),
                            color = BiocodePalette.BioLime
                        )
                    }
                }

                // 8. РАСЧИТАННЫЕ СОЖЖЕННЫЕ КАЛОРИИ
                OutlinedTextField(
                    value = caloriesBurnedStr,
                    onValueChange = { caloriesBurnedStr = it.filter { c -> c.isDigit() } },
                    label = { Text("Сожжено энергии (Ккал)", style = BiocodeTypography.TelemetryLabel) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BiocodePalette.LipidAmber,
                        unfocusedBorderColor = BiocodePalette.DeckBorder,
                        focusedTextColor = BiocodePalette.LipidAmber,
                        unfocusedTextColor = BiocodePalette.NoguchiCream,
                        focusedLabelColor = BiocodePalette.LipidAmber,
                        cursorColor = BiocodePalette.BioLime
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(2.dp))

                // 9. КНОПКА «ЗАФИКСИРОВАТЬ АКТИВНОСТЬ»
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(BiocodePalette.BioLime)
                        .clickable {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                            val burn = caloriesBurnedStr.toIntOrNull() ?: 120
                            val sets = setsStr.toIntOrNull() ?: 4
                            val duration = durationMinutesStr.toIntOrNull() ?: 15
                            val weight = weightKgStr.toFloatOrNull()
                            val name = exerciseName.ifBlank { "Тренировочное упражнение" }

                            val exercise = ExerciseItem(
                                id = System.currentTimeMillis().toString(),
                                name = name,
                                description = description,
                                feelingsRpe = feelingsRpe,
                                sets = sets,
                                repsOrTime = repsStr,
                                weightKg = weight,
                                durationMinutes = duration,
                                caloriesBurned = burn
                            )
                            onSaveExercise(exercise, selectedCategory)
                            onDismiss()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LucidePlus(modifier = Modifier.size(16.dp), tint = BiocodePalette.DarkMoss)
                        Text(
                            text = "ЗАФИКСИРОВАТЬ АКТИВНОСТЬ",
                            style = BiocodeTypography.TabLabel,
                            color = BiocodePalette.DarkMoss,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
