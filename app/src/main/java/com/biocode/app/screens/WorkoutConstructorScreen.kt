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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.biocode.engine.BiocodeBentoCard
import com.biocode.engine.BiocodeBlueprintCanvas
import com.biocode.engine.BiocodePalette
import com.biocode.engine.BiocodeTypography
import com.biocode.engine.DefaultKineticData
import com.biocode.engine.LucideCheck
import com.biocode.engine.LucideDumbbell
import com.biocode.engine.LucidePlus
import com.biocode.engine.LucideSparkles
import com.biocode.engine.LucideTrendingUp
import com.biocode.engine.WorkoutPlanDay
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 🛠 2. ВТОРОЕ МЕНЮ KINETIC HUB: «КОНСТРУКТОР ТРЕНИРОВОК»
 *
 * Сборка цельных тренировок, расстановка очередности дней (сплит) и ИИ-оптимизация очередности.
 */
@Composable
fun WorkoutConstructorScreen(
    onAddCustomWorkout: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    var planDays by remember { mutableStateOf(DefaultKineticData.createInitialPlan()) }
    var isAiOptimizing by remember { mutableStateOf(false) }
    var aiAdviceBanner by remember { mutableStateOf<String?>(null) }
    var selectedDayIndex by remember { mutableStateOf(0) }

    // Пульсация неоновой кнопки
    val infiniteTransition = rememberInfiniteTransition(label = "constructor_glow")
    val aiGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "c_glow"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BiocodePalette.DarkMoss)
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
                text = "КОНСТРУКТОР ТРЕНИРОВОК // БИОМЕХАНИКА СПЛИТА",
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
                // 1. ИИ-АССИСТЕНТ ОЧЕРЕДНОСТИ СПЛИТА
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(BiocodePalette.BioLime.copy(alpha = if (isAiOptimizing) 0.6f else aiGlowAlpha))
                        .border(1.5.dp, BiocodePalette.BioLime, RoundedCornerShape(24.dp))
                        .clickable(enabled = !isAiOptimizing) {
                            coroutineScope.launch {
                                isAiOptimizing = true
                                aiAdviceBanner = "ИИ АНАЛИЗИРУЕТ ФАЗЫ СУПЕРКОМПЕНСАЦИИ И БИОМЕХАНИКУ..."
                                delay(1200)
                                isAiOptimizing = false
                                aiAdviceBanner = "ОЧЕРЕДНОСТЬ ОПТИМИЗИРОВАНА: Кардио разнесено с днем тяжелых приседаний, грудные мышцы изолированы от трицепса для максимального восстановления."
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (isAiOptimizing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = BiocodePalette.DarkMoss,
                                strokeWidth = 2.dp
                            )
                            Text(
                                text = "ИИ ОПТИМИЗИРУЕТ СПЛИТ...",
                                style = BiocodeTypography.TabLabel.copy(fontSize = 11.sp),
                                color = BiocodePalette.DarkMoss,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            LucideSparkles(modifier = Modifier.size(17.dp), tint = BiocodePalette.DarkMoss)
                            Text(
                                text = "ОПТИМИЗИРОВАТЬ СПЛИТ С ИИ",
                                style = BiocodeTypography.TabLabel.copy(fontSize = 11.sp),
                                color = BiocodePalette.DarkMoss,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Статус-сообщение ИИ
                AnimatedVisibility(
                    visible = aiAdviceBanner != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(BiocodePalette.SpruceDeck)
                            .border(1.dp, BiocodePalette.BioLime.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "⚡ ${aiAdviceBanner ?: ""}",
                            style = BiocodeTypography.TelemetryLabel.copy(fontSize = 8.5.sp, lineHeight = 12.sp),
                            color = BiocodePalette.BioLime
                        )
                    }
                }

                // 2. ЦЕПОЧКА ТРЕНИРОВОЧНЫХ ДНЕЙ (НЕДЕЛЬНЫЙ ЦИКЛ)
                BiocodeBentoCard(
                    title = "СТРУКТУРА НЕДЕЛЬНОГО СПЛИТА",
                    badgeText = "7-ДНЕВНЫЙ ЦИКЛ",
                    badgeColor = BiocodePalette.BioLime,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        planDays.forEachIndexed { idx, day ->
                            val isSelected = selectedDayIndex == idx
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) BiocodePalette.PineTeal else BiocodePalette.DarkMoss)
                                    .border(
                                        1.dp,
                                        if (isSelected) BiocodePalette.BioLime else BiocodePalette.DeckBorder,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable { selectedDayIndex = idx }
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(if (day.isRestDay) BiocodePalette.SpruceDeck else BiocodePalette.BioLime),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "0${day.dayNumber}",
                                                style = BiocodeTypography.MonospaceTitle.copy(fontSize = 11.sp),
                                                color = if (day.isRestDay) BiocodePalette.NoguchiCream else BiocodePalette.DarkMoss,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Column {
                                            Text(
                                                text = day.title,
                                                style = BiocodeTypography.TabLabel.copy(fontSize = 12.sp),
                                                color = if (isSelected) BiocodePalette.BioLime else BiocodePalette.NoguchiCream,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = if (day.isRestDay) "БИО-ВОССТАНОВЛЕНИЕ // ФАСЦИИ" else "СИЛОВОЙ ВЕКТОР // ТРЕНИРОВКА",
                                                style = BiocodeTypography.TelemetryLabel.copy(fontSize = 8.sp),
                                                color = if (day.isRestDay) BiocodePalette.LipidAmber else BiocodePalette.BioLime.copy(alpha = 0.8f)
                                            )
                                        }
                                    }

                                    // Кнопки быстрой перестановки
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        if (idx > 0) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(BiocodePalette.SpruceDeck)
                                                    .clickable {
                                                        val mutable = planDays.toMutableList()
                                                        val temp = mutable[idx]
                                                        mutable[idx] = mutable[idx - 1]
                                                        mutable[idx - 1] = temp
                                                        planDays = mutable
                                                    }
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text("▲", fontSize = 9.sp, color = BiocodePalette.BioLime)
                                            }
                                        }

                                        if (idx < planDays.size - 1) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(BiocodePalette.SpruceDeck)
                                                    .clickable {
                                                        val mutable = planDays.toMutableList()
                                                        val temp = mutable[idx]
                                                        mutable[idx] = mutable[idx + 1]
                                                        mutable[idx + 1] = temp
                                                        planDays = mutable
                                                    }
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text("▼", fontSize = 9.sp, color = BiocodePalette.BioLime)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 3. ДЕТАЛИ ВЫБРАННОГО ТРЕНИРОВОЧНОГО ДНЯ
                val currentDay = planDays.getOrNull(selectedDayIndex) ?: planDays.first()
                BiocodeBentoCard(
                    title = "ДЕТАЛИ // ${currentDay.title}",
                    badgeText = "БИОМЕХАНИКА",
                    badgeColor = BiocodePalette.BioLime,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "РЕКОМЕНДАЦИЯ ИИ:",
                            style = BiocodeTypography.TelemetryLabel.copy(fontSize = 8.sp),
                            color = BiocodePalette.BioLime
                        )
                        Text(
                            text = currentDay.aiRecommendation,
                            style = BiocodeTypography.TelemetryLabel.copy(fontSize = 9.5.sp, lineHeight = 13.sp),
                            color = BiocodePalette.NoguchiCream.copy(alpha = 0.9f)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp)
                                .clip(RoundedCornerShape(21.dp))
                                .background(BiocodePalette.PineTeal)
                                .border(1.dp, BiocodePalette.DeckBorder, RoundedCornerShape(21.dp))
                                .clickable { onAddCustomWorkout() },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                LucidePlus(modifier = Modifier.size(15.dp), tint = BiocodePalette.BioLime)
                                Text(
                                    text = "ДОБАВИТЬ УПРАЖНЕНИЕ В ДЕНЬ",
                                    style = BiocodeTypography.TelemetryLabel.copy(fontSize = 9.sp),
                                    color = BiocodePalette.NoguchiCream,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(86.dp))
            }
        }
    }
}
