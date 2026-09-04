package com.biocode.engine

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Состояние и геометрические параметры отдельного модуля интерфейса.
 */
data class BiocodeModuleState(
    val id: String,
    val xOffset: Dp,
    val yOffset: Dp,
    val width: Dp,
    val height: Dp,
    val cornerRadius: Dp = 24.dp,
    val backgroundColor: Color = Color(0xFF0A2620)
)

/**
 * Двухслойный конструктор интерфейса BIOCODE.
 *
 * Архитектура:
 * 1. Слой силуэтов (Background Gooey Layer):
 *    Solid-шейпы модулей отрисовываются под действием gooeyBackground().
 *    При сближении их границы сплавляются в органические галтели на GPU.
 *
 * 2. Слой контента (Crisp Foreground Layer):
 *    Тексты, LED-матрицы, векторные иконки позиционируются в точности по тем же
 *    координатам, но без размытия (со 100% резкостью).
 */
@Composable
fun BiocodeEngine(
    modules: List<BiocodeModuleState>,
    modifier: Modifier = Modifier,
    blurRadius: Float = 48f,
    contentPadding: PaddingValues = PaddingValues(12.dp),
    moduleContent: @Composable BoxScope.(BiocodeModuleState) -> Unit
) {
    Box(modifier = modifier) {
        // СЛОЙ 1: СИЛУЭТЫ ПОД СЛИЯНИЕ
        Box(
            modifier = Modifier
                .matchParentSize()
                .gooeyBackground(blurRadius = blurRadius)
        ) {
            modules.forEach { module ->
                Box(
                    modifier = Modifier
                        .offset(x = module.xOffset, y = module.yOffset)
                        .size(width = module.width, height = module.height)
                        .background(
                            color = module.backgroundColor,
                            shape = RoundedCornerShape(module.cornerRadius)
                        )
                )
            }
        }

        // СЛОЙ 2: ЧЕТКИЙ КОНТЕНТ
        Box(
            modifier = Modifier.matchParentSize()
        ) {
            modules.forEach { module ->
                Box(
                    modifier = Modifier
                        .offset(x = module.xOffset, y = module.yOffset)
                        .size(width = module.width, height = module.height)
                        .padding(contentPadding)
                ) {
                    moduleContent(module)
                }
            }
        }
    }
}

/**
 * Упрощенный хост слияния произвольных Composable подконтейнеров.
 */
@Composable
fun BiocodeGooeyHost(
    modifier: Modifier = Modifier,
    blurRadius: Float = 40f,
    silhouettes: @Composable BoxScope.() -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    Box(modifier = modifier) {
        // Слой 1: Слияние силуэтов
        Box(
            modifier = Modifier
                .matchParentSize()
                .gooeyBackground(blurRadius = blurRadius),
            content = silhouettes
        )
        // Слой 2: Четкий контент
        Box(
            modifier = Modifier.matchParentSize(),
            content = content
        )
    }
}
