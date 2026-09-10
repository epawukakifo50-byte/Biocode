package com.biocode.app.screens

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.biocode.engine.BiocodeDotMatrixText
import com.biocode.engine.BiocodePalette
import com.biocode.engine.BiocodeTypography
import com.biocode.engine.LucideApple
import com.biocode.engine.LucideEdit
import com.biocode.engine.LucideMoon
import com.biocode.engine.LucideSun
import com.biocode.engine.LucideTrash2
import com.biocode.engine.LucideUtensils
import com.biocode.engine.LucideX
import com.biocode.engine.MealEntry
import com.biocode.engine.MealType

@Composable
fun BiocodeMealDetailModal(
    meal: MealEntry,
    onDismiss: () -> Unit,
    onDeleteMeal: (String) -> Unit,
    onEditMeal: (MealEntry) -> Unit = {}
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Загрузка фото из локального URI если прикреплено
    val mealBitmap = remember(meal.photoUri) {
        if (!meal.photoUri.isNullOrBlank()) {
            try {
                val uri = Uri.parse(meal.photoUri)
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream)
                }
            } catch (e: Exception) {
                null
            }
        } else null
    }

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
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. ШАПКА КАРТОЧКИ: ТИП ПРИЕМА ПИЩИ, ВРЕМЯ И КНОПКА ЗАКРЫТИЯ
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
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(BiocodePalette.SpruceDeck)
                                .border(1.dp, BiocodePalette.DeckBorder, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            when (meal.type) {
                                MealType.BREAKFAST -> LucideSun(modifier = Modifier.size(18.dp), tint = BiocodePalette.BioLime)
                                MealType.LUNCH -> LucideUtensils(modifier = Modifier.size(18.dp), tint = BiocodePalette.BioLime)
                                MealType.DINNER -> LucideMoon(modifier = Modifier.size(18.dp), tint = BiocodePalette.BioLime)
                                MealType.SNACK -> LucideApple(modifier = Modifier.size(18.dp), tint = BiocodePalette.BioLime)
                            }
                        }

                        Column {
                            val typeTitle = when (meal.type) {
                                MealType.BREAKFAST -> "УТРЕННИЙ ПРИЕМ // ЗАВТРАК"
                                MealType.LUNCH -> "ДНЕВНОЙ ПРИЕМ // ОБЕД"
                                MealType.DINNER -> "ВЕЧЕРНИЙ ПРИЕМ // УЖИН"
                                MealType.SNACK -> "МЕТАБОЛИЧЕСКИЙ ПЕРЕКУС"
                            }
                            Text(
                                text = typeTitle,
                                style = BiocodeTypography.TelemetryLabel.copy(fontSize = 9.sp),
                                color = BiocodePalette.BioLime,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "ТАКТ ФИКСАЦИИ: ${meal.time}",
                                style = BiocodeTypography.TelemetryLabel.copy(fontSize = 7.5.sp),
                                color = BiocodePalette.NoguchiCream.copy(alpha = 0.6f)
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

                // 2. НАЗВАНИЕ БЛЮДА И ГРАММОВКА
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = meal.name,
                        style = BiocodeTypography.TabLabel.copy(fontSize = 19.sp, lineHeight = 23.sp),
                        color = BiocodePalette.NoguchiCream,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (meal.weightGrams != null && meal.weightGrams > 0) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(BiocodePalette.BioLime.copy(alpha = 0.15f))
                                    .border(1.dp, BiocodePalette.BioLime.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "ПОРЦИЯ: ~${meal.weightGrams} Г",
                                    style = BiocodeTypography.TelemetryLabel.copy(fontSize = 8.5.sp),
                                    color = BiocodePalette.BioLime,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(BiocodePalette.SpruceDeck)
                                .border(1.dp, BiocodePalette.DeckBorder.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "BIOCODE VERIFIED",
                                style = BiocodeTypography.TelemetryLabel.copy(fontSize = 8.sp),
                                color = BiocodePalette.NoguchiCream.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                // 3. ФОТО БЛЮДА (ВЫСОКОЧЕТКИЙ БИОМОРФНЫЙ ВИЗИР)
                if (mealBitmap != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(210.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .border(1.5.dp, BiocodePalette.BioLime.copy(alpha = 0.7f), RoundedCornerShape(18.dp))
                    ) {
                        Image(
                            bitmap = mealBitmap.asImageBitmap(),
                            contentDescription = meal.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        // HUD оверлей
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(10.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(BiocodePalette.DarkMoss.copy(alpha = 0.90f))
                                .border(1.dp, BiocodePalette.BioLime.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "BIO-SCAN // 1.0X UHD",
                                style = BiocodeTypography.TelemetryLabel.copy(fontSize = 8.sp),
                                color = BiocodePalette.BioLime
                            )
                        }
                    }
                }

                // 4. БЕНТО-МОДУЛЬ ЭНЕРГИИ И БЖУ
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(BiocodePalette.SpruceDeck)
                        .border(1.2.dp, BiocodePalette.DeckBorder, RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Калории
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "ЭНЕРГЕТИЧЕСКИЙ ПУЛ",
                                    style = BiocodeTypography.TelemetryLabel.copy(fontSize = 8.sp),
                                    color = BiocodePalette.NoguchiCream.copy(alpha = 0.6f)
                                )
                                Text(
                                    text = "КАЛОРИЙНОСТЬ ПОРЦИИ",
                                    style = BiocodeTypography.TabLabel.copy(fontSize = 11.sp),
                                    color = BiocodePalette.NoguchiCream
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                BiocodeDotMatrixText(
                                    text = "${meal.calories}",
                                    dotSize = 3.2.dp,
                                    dotSpacing = 1.1.dp,
                                    activeColor = BiocodePalette.BioLime,
                                    inactiveColor = BiocodePalette.PineTeal.copy(alpha = 0.5f)
                                )
                                Text(
                                    text = "KCAL",
                                    style = BiocodeTypography.TelemetryLabel.copy(fontSize = 9.sp),
                                    color = BiocodePalette.BioLime,
                                    modifier = Modifier.padding(bottom = 2.dp)
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(BiocodePalette.DeckBorder.copy(alpha = 0.4f))
                        )

                        // 3 Ячейки БЖУ
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Белки
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(BiocodePalette.DarkMoss)
                                    .border(1.dp, BiocodePalette.BioLime.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                                    .padding(8.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "БЕЛКИ",
                                        style = BiocodeTypography.TelemetryLabel.copy(fontSize = 8.sp),
                                        color = BiocodePalette.BioLime
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${meal.protein.toInt()} г",
                                        style = BiocodeTypography.TabLabel.copy(fontSize = 14.sp),
                                        color = BiocodePalette.NoguchiCream,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Жиры
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(BiocodePalette.DarkMoss)
                                    .border(1.dp, BiocodePalette.MacroFat.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                                    .padding(8.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "ЖИРЫ",
                                        style = BiocodeTypography.TelemetryLabel.copy(fontSize = 8.sp),
                                        color = BiocodePalette.MacroFat
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${meal.fat.toInt()} г",
                                        style = BiocodeTypography.TabLabel.copy(fontSize = 14.sp),
                                        color = BiocodePalette.NoguchiCream,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Углеводы
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(BiocodePalette.DarkMoss)
                                    .border(1.dp, BiocodePalette.NoguchiCream.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                    .padding(8.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "УГЛЕВОДЫ",
                                        style = BiocodeTypography.TelemetryLabel.copy(fontSize = 8.sp),
                                        color = BiocodePalette.NoguchiCream
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${meal.carbs.toInt()} г",
                                        style = BiocodeTypography.TabLabel.copy(fontSize = 14.sp),
                                        color = BiocodePalette.NoguchiCream,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // 5. ОПИСАНИЕ И СОСТАВ БЛЮДА
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(BiocodePalette.SpruceDeck)
                        .border(1.dp, BiocodePalette.DeckBorder, RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "СОСТАВ И БИОХИМИЧЕСКИЙ ПРОФИЛЬ",
                            style = BiocodeTypography.TelemetryLabel.copy(fontSize = 8.5.sp),
                            color = BiocodePalette.BioLime,
                            fontWeight = FontWeight.Bold
                        )

                        val desc = if (meal.description.isNotBlank()) {
                            meal.description
                        } else {
                            "Биометрический профиль нутриентов сформирован в дневнике. Расширенные заметки по ингредиентам отсутствуют."
                        }

                        Text(
                            text = desc,
                            style = BiocodeTypography.TelemetryLabel.copy(fontSize = 10.5.sp, lineHeight = 14.sp),
                            color = BiocodePalette.NoguchiCream.copy(alpha = 0.9f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // 6. КНОПКА «ИЗМЕНИТЬ ПРИЕМ ПИЩИ»
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .clip(RoundedCornerShape(23.dp))
                        .background(BiocodePalette.BioLime)
                        .clickable {
                            onEditMeal(meal)
                            onDismiss()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LucideEdit(modifier = Modifier.size(16.dp), tint = BiocodePalette.DarkMoss)
                        Text(
                            text = "ИЗМЕНИТЬ ПРИЕМ ПИЩИ",
                            style = BiocodeTypography.TabLabel,
                            color = BiocodePalette.DarkMoss,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // 7. КНОПКИ ДЕЙСТВИЙ: УДАЛИТЬ И ЗАКРЫТЬ
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Удалить
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .clip(RoundedCornerShape(23.dp))
                            .background(BiocodePalette.LipidAmber.copy(alpha = 0.12f))
                            .border(1.2.dp, BiocodePalette.LipidAmber.copy(alpha = 0.6f), RoundedCornerShape(23.dp))
                            .clickable {
                                onDeleteMeal(meal.id)
                                onDismiss()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            LucideTrash2(modifier = Modifier.size(15.dp), tint = BiocodePalette.LipidAmber)
                            Text(
                                text = "УДАЛИТЬ",
                                style = BiocodeTypography.TelemetryLabel.copy(fontSize = 9.sp),
                                color = BiocodePalette.LipidAmber,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Закрыть
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .clip(RoundedCornerShape(23.dp))
                            .background(BiocodePalette.PineTeal)
                            .border(1.dp, BiocodePalette.DeckBorder, RoundedCornerShape(23.dp))
                            .clickable { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "ЗАКРЫТЬ",
                            style = BiocodeTypography.TabLabel.copy(fontSize = 10.5.sp),
                            color = BiocodePalette.NoguchiCream,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
