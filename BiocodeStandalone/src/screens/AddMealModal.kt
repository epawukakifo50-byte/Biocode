package com.biocode.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.biocode.engine.BiocodePalette
import com.biocode.engine.BiocodeTypography
import com.biocode.engine.LucidePlus
import com.biocode.engine.LucideSparkles
import com.biocode.engine.LucideX
import com.biocode.engine.MealEntry
import com.biocode.engine.MealType

@Composable
fun AddMealModal(
    onDismiss: () -> Unit,
    onSaveMeal: (MealEntry) -> Unit
) {
    var mealName by remember { mutableStateOf("") }
    var caloriesStr by remember { mutableStateOf("") }
    var proteinStr by remember { mutableStateOf("") }
    var fatStr by remember { mutableStateOf("") }
    var carbsStr by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(MealType.LUNCH) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(26.dp))
                .background(BiocodePalette.DarkMoss)
                .border(1.5.dp, BiocodePalette.PineTeal, RoundedCornerShape(26.dp))
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Заголовок модала
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
                            LucideSparkles(modifier = Modifier.size(16.dp), tint = BiocodePalette.BioLime)
                        }
                        Column {
                            Text(
                                text = "НОВЫЙ ПРИЕМ ПИЩИ",
                                style = BiocodeTypography.TabLabel,
                                color = BiocodePalette.NoguchiCream,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "БИО-ЛОГИРОВАНИЕ МАКРОСОВ",
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
                            .clickable { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        LucideX(modifier = Modifier.size(16.dp), tint = BiocodePalette.NoguchiCream)
                    }
                }

                // Тип приема пищи
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val types = listOf(
                        MealType.BREAKFAST to "ЗАВТРАК",
                        MealType.LUNCH to "ОБЕД",
                        MealType.DINNER to "УЖИН",
                        MealType.SNACK to "СНЕК"
                    )

                    types.forEach { (type, label) ->
                        val isSel = selectedType == type
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSel) BiocodePalette.BioLime else BiocodePalette.PineTeal.copy(alpha = 0.4f))
                                .clickable { selectedType = type }
                                .padding(vertical = 8.dp),
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

                // Название блюда
                OutlinedTextField(
                    value = mealName,
                    onValueChange = { mealName = it },
                    label = { Text("Название блюда", style = BiocodeTypography.TelemetryLabel) },
                    placeholder = { Text("Например: Боул с форелью", color = BiocodePalette.NoguchiCream.copy(alpha = 0.3f)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BiocodePalette.BioLime,
                        unfocusedBorderColor = BiocodePalette.DeckBorder,
                        focusedTextColor = BiocodePalette.NoguchiCream,
                        unfocusedTextColor = BiocodePalette.NoguchiCream,
                        focusedLabelColor = BiocodePalette.BioLime,
                        unfocusedLabelColor = BiocodePalette.NoguchiCream.copy(alpha = 0.6f),
                        cursorColor = BiocodePalette.BioLime
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Калории и БЖУ
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = caloriesStr,
                        onValueChange = { caloriesStr = it.filter { c -> c.isDigit() } },
                        label = { Text("Ккал", style = BiocodeTypography.TelemetryLabel) },
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
                        modifier = Modifier.weight(1.2f)
                    )

                    OutlinedTextField(
                        value = proteinStr,
                        onValueChange = { proteinStr = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Белки (г)", style = BiocodeTypography.TelemetryLabel) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BiocodePalette.BioLime,
                            unfocusedBorderColor = BiocodePalette.DeckBorder,
                            focusedTextColor = BiocodePalette.BioLime,
                            unfocusedTextColor = BiocodePalette.NoguchiCream,
                            focusedLabelColor = BiocodePalette.BioLime,
                            cursorColor = BiocodePalette.BioLime
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = fatStr,
                        onValueChange = { fatStr = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Жиры (г)", style = BiocodeTypography.TelemetryLabel) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BiocodePalette.MacroFat,
                            unfocusedBorderColor = BiocodePalette.DeckBorder,
                            focusedTextColor = BiocodePalette.MacroFat,
                            unfocusedTextColor = BiocodePalette.NoguchiCream,
                            focusedLabelColor = BiocodePalette.MacroFat,
                            cursorColor = BiocodePalette.BioLime
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = carbsStr,
                        onValueChange = { carbsStr = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Углеводы", style = BiocodeTypography.TelemetryLabel) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BiocodePalette.NoguchiCream,
                            unfocusedBorderColor = BiocodePalette.DeckBorder,
                            focusedTextColor = BiocodePalette.NoguchiCream,
                            unfocusedTextColor = BiocodePalette.NoguchiCream,
                            focusedLabelColor = BiocodePalette.NoguchiCream,
                            cursorColor = BiocodePalette.BioLime
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Быстрые био-пресеты
                Text(
                    text = "БЫСТРЫЕ ШАБЛОНЫ",
                    style = BiocodeTypography.TelemetryLabel.copy(fontSize = 8.5.sp),
                    color = BiocodePalette.NoguchiCream.copy(alpha = 0.6f)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val presets = listOf(
                        Triple("Шейк изолята", 140, Triple(30f, 1.5f, 3f)),
                        Triple("Тост с яйцом", 280, Triple(14f, 12f, 26f)),
                        Triple("Курица и гречка", 490, Triple(45f, 9f, 55f))
                    )

                    presets.forEach { (name, kcal, macros) ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(BiocodePalette.SpruceDeck)
                                .border(1.dp, BiocodePalette.DeckBorder, RoundedCornerShape(10.dp))
                                .clickable {
                                    mealName = name
                                    caloriesStr = kcal.toString()
                                    proteinStr = macros.first.toInt().toString()
                                    fatStr = macros.second.toInt().toString()
                                    carbsStr = macros.third.toInt().toString()
                                }
                                .padding(horizontal = 6.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = name,
                                    style = BiocodeTypography.TelemetryLabel.copy(fontSize = 8.sp),
                                    color = BiocodePalette.NoguchiCream,
                                    maxLines = 1
                                )
                                Text(
                                    text = "$kcal ккал",
                                    style = BiocodeTypography.TelemetryLabel.copy(fontSize = 7.5.sp),
                                    color = BiocodePalette.BioLime
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Кнопка сохранения
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(BiocodePalette.BioLime)
                        .clickable {
                            val cals = caloriesStr.toIntOrNull() ?: 200
                            val prot = proteinStr.toFloatOrNull() ?: 15f
                            val fat = fatStr.toFloatOrNull() ?: 5f
                            val carb = carbsStr.toFloatOrNull() ?: 20f
                            val title = mealName.ifBlank { "Прием пищи" }

                            val entry = MealEntry(
                                id = System.currentTimeMillis().toString(),
                                name = title,
                                type = selectedType,
                                calories = cals,
                                protein = prot,
                                fat = fat,
                                carbs = carb,
                                time = "14:30"
                            )
                            onSaveMeal(entry)
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
                            text = "ДОБАВИТЬ В ДНЕВНИК",
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
