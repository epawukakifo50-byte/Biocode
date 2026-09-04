package com.biocode.engine

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer

/**
 * BIOCODE GOOEY ENGINE (АППАРАТНЫЙ МЕТАБОЛЛ НА GPU)
 *
 * Создает эффект расплавленной био-жидкости (MetaBall), объединяя сильное
 * гауссово размытие и цветовую матрицу пороговой альфы (Alpha Thresholding).
 *
 * Преимущество перед обычными шейдерами:
 * - Сохраняет оригинальные RGB-цвета независимых модулей (ColorMatrix ColorFilter).
 * - Аппаратное ускорение через Skia RenderEffect (60/120 FPS).
 * - Плавные сопряжения (вогнутые галтели) в местах сближения модулей.
 */
@RequiresApi(Build.VERSION_CODES.S)
fun getGooeyRenderEffect(
    blurRadius: Float = 48f,
    alphaMultiplier: Float = 60f,
    alphaSubtract: Float = -5000f
): RenderEffect {
    // 1. Аппаратное размытие силуэтов на GPU
    val blurEffect = RenderEffect.createBlurEffect(blurRadius, blurRadius, Shader.TileMode.CLAMP)

    // 2. Цветовая матрица сжатия альфа-канала до резкой границы
    val gooeyMatrix = ColorMatrix(
        floatArrayOf(
            1f, 0f, 0f, 0f, 0f,    // Red
            0f, 1f, 0f, 0f, 0f,    // Green
            0f, 0f, 1f, 0f, 0f,    // Blue
            0f, 0f, 0f, alphaMultiplier, alphaSubtract // Резкая отсечка порога альфы
        )
    )

    val alphaMatrix = RenderEffect.createColorFilterEffect(ColorMatrixColorFilter(gooeyMatrix.array))

    // 3. Цепочка эффектов: размытие -> пороговая матрица
    return RenderEffect.createChainEffect(alphaMatrix, blurEffect)
}

/**
 * Модификатор для контейнера слияния.
 * Любые solid-элементы, нарисованные внутри этого контейнера,
 * при сближении физически сплавляются в единый метаболл.
 */
fun Modifier.gooeyBackground(
    blurRadius: Float = 48f,
    alphaMultiplier: Float = 60f,
    alphaSubtract: Float = -5000f
): Modifier {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        this.graphicsLayer {
            renderEffect = getGooeyRenderEffect(
                blurRadius = blurRadius,
                alphaMultiplier = alphaMultiplier,
                alphaSubtract = alphaSubtract
            ).asComposeRenderEffect()
        }
    } else {
        this
    }
}
