package com.biocode.engine

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.biocode.app.screens.TodayScreen

/**
 * Оставлено для обратной совместимости с движком.
 * Делегирует рендеринг в экран TodayScreen.
 */
@Composable
fun BiocodeNutritionDashboard(
    modifier: Modifier = Modifier
) {
    var diaryState by remember { mutableStateOf(DailyNutritionState()) }
    TodayScreen(
        state = diaryState,
        onStateUpdate = { diaryState = it },
        modifier = modifier
    )
}
