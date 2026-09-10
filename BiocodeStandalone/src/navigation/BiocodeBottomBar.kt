package com.biocode.app.navigation

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.biocode.engine.BiocodePalette
import com.biocode.engine.BiocodeTypography
import com.biocode.engine.LucideBookOpen
import com.biocode.engine.LucideCalculator
import com.biocode.engine.LucideCalendar
import com.biocode.engine.LucideSparkles
import com.biocode.engine.LucideUtensils

enum class BiocodeTab(val title: String) {
    TODAY("Сегодня"),
    MEAL_BUILDER("Что поесть"),
    CALCULATOR("Расчет"),
    DIARY("Дневник")
}

/**
 * 5-КНОПОЧНЫЙ НАВИГАЦИОННЫЙ БАР BIOCODE (РЕФЕРЕНСЫ 1, 3, 5)
 *
 * Содержит 5 элементов:
 * 1. Сегодня (LucideCalendar)
 * 2. Что поесть (LucideUtensils)
 * 3. ЦЕНТРАЛЬНАЯ АКЦЕНТНАЯ КНОПКА (со звездочкой LucideSparkles) с физикой squish & stretch
 * 4. Расчет (LucideCalculator)
 * 5. Дневник (LucideBookOpen)
 */
@Composable
fun BiocodeBottomBar(
    selectedTab: BiocodeTab,
    onTabSelected: (BiocodeTab) -> Unit,
    onAddMealClick: () -> Unit,
    hubMode: com.biocode.engine.HubMode = com.biocode.engine.HubMode.METABOLIC,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val addInteractionSource = remember { MutableInteractionSource() }
    val isAddPressed by addInteractionSource.collectIsPressedAsState()

    // Пружинящее вытягивание центральной звезды (Squish & Stretch)
    val dropElevation by animateDpAsState(
        targetValue = if (isAddPressed) (-16).dp else (-10).dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "add_elevation"
    )
    val dropScaleY by animateFloatAsState(
        targetValue = if (isAddPressed) 1.15f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "add_scale_y"
    )
    val dropScaleX by animateFloatAsState(
        targetValue = if (isAddPressed) 0.90f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "add_scale_x"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(78.dp)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        // Фоновая бенто-капсула консоли
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(BiocodePalette.DarkMoss)
                .border(1.2.dp, BiocodePalette.NoguchiBorder, RoundedCornerShape(32.dp))
                .padding(horizontal = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 1. «Сегодня»
                NavTabItem(
                    title = BiocodeTab.TODAY.title,
                    isSelected = selectedTab == BiocodeTab.TODAY,
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                        onTabSelected(BiocodeTab.TODAY)
                    },
                    modifier = Modifier.weight(1f)
                ) { tint ->
                    LucideCalendar(modifier = Modifier.size(20.dp), tint = tint)
                }

                // 2. «Что поесть» // «Конструктор»
                NavTabItem(
                    title = if (hubMode == com.biocode.engine.HubMode.KINETIC) "Конструктор" else BiocodeTab.MEAL_BUILDER.title,
                    isSelected = selectedTab == BiocodeTab.MEAL_BUILDER,
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                        onTabSelected(BiocodeTab.MEAL_BUILDER)
                    },
                    modifier = Modifier.weight(1f)
                ) { tint ->
                    if (hubMode == com.biocode.engine.HubMode.KINETIC) {
                        com.biocode.engine.LucideDumbbell(modifier = Modifier.size(20.dp), tint = tint)
                    } else {
                        LucideUtensils(modifier = Modifier.size(20.dp), tint = tint)
                    }
                }

                // Пространство под центральную акцентную кнопку
                Spacer(modifier = Modifier.size(60.dp))

                // 4. «Расчет»
                NavTabItem(
                    title = BiocodeTab.CALCULATOR.title,
                    isSelected = selectedTab == BiocodeTab.CALCULATOR,
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                        onTabSelected(BiocodeTab.CALCULATOR)
                    },
                    modifier = Modifier.weight(1f)
                ) { tint ->
                    LucideCalculator(modifier = Modifier.size(20.dp), tint = tint)
                }

                // 5. «Дневник»
                NavTabItem(
                    title = BiocodeTab.DIARY.title,
                    isSelected = selectedTab == BiocodeTab.DIARY,
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                        onTabSelected(BiocodeTab.DIARY)
                    },
                    modifier = Modifier.weight(1f)
                ) { tint ->
                    LucideBookOpen(modifier = Modifier.size(20.dp), tint = tint)
                }
            }
        }

        // 3. ЦЕНТРАЛЬНАЯ АКЦЕНТНАЯ КНОПКА ДОБАВЛЕНИЯ ПРИЕМА ПИЩИ
        Box(
            modifier = Modifier
                .offset(y = dropElevation)
                .size(60.dp)
                .graphicsLayer {
                    this.scaleX = dropScaleX
                    this.scaleY = dropScaleY
                }
                .clip(CircleShape)
                .background(BiocodePalette.PineTeal)
                .border(2.dp, BiocodePalette.BioLime, CircleShape)
                .clickable(
                    interactionSource = addInteractionSource,
                    indication = null
                ) {
                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                    onAddMealClick()
                },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(BiocodePalette.BioLime),
                contentAlignment = Alignment.Center
            ) {
                LucideSparkles(modifier = Modifier.size(22.dp), tint = BiocodePalette.DarkMoss)
            }
        }
    }
}

@Composable
private fun NavTabItem(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable (Color) -> Unit
) {
    val tint = if (isSelected) BiocodePalette.BioLime else BiocodePalette.NoguchiCream.copy(alpha = 0.5f)

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        icon(tint)
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = title,
            style = BiocodeTypography.TelemetryLabel.copy(
                fontSize = 8.5.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            ),
            color = tint,
            maxLines = 1
        )
    }
}
