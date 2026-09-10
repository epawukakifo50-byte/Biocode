package com.biocode.engine

import android.graphics.Paint as AndroidPaint
import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.sin

/**
 * ==============================================================================
 * BIOCODE BIOCYCLE WAVEFORM ENGINE (AGSL & PROCEDURAL METABALL LUMEN)
 * ==============================================================================
 *
 * Высокопроизводительный шейдер AGSL (Android Graphics Shading Language) для
 * симуляции горизонтального перистальтического био-сосуда пищеварительного
 * биоцикла (Biocycle Waveform Bar) с аппаратным рендерингом на GPU (Android 13+).
 *
 * ФИЗИЧЕСКАЯ И ОПТИЧЕСКАЯ МОДЕЛЬ:
 * 1. Цепочка из 5 органических камер/пузырей переменного калибра, соединенных
 *    через полиномиальный гладкий минимум smin(a, b, k) в единый флюидный тракт.
 * 2. Бегущая перистальтическая волна сокращений: sin(u_time * 2.0 - p.x * 6.0).
 * 3. Оптический разрез био-сосуда:
 *    - Темное непрозрачное питательное ядро в глубине камер (u_darkCore).
 *    - Интенсивная неоновая биолюминесценция на верхнем и нижнем контурах (Fresnel rim)
 *      и в сужениях соединительных шеек (u_accentColor, u_rimGlow).
 *    - Активный метаболический болюс (энергетический сгусток) на позиции u_phaseProgress.
 *    - Внутренние микро-токи и световой ореол (aura) вокруг сосуда.
 */

// language=AGSL
const val BIOCYCLE_WAVE_AGSL = """
    uniform float2 u_resolution;
    uniform float u_time;
    uniform layout(color) half4 u_accentColor;
    uniform layout(color) half4 u_darkCore;
    uniform layout(color) half4 u_rimGlow;
    uniform float u_phaseProgress; // 0.0 to 1.0

    // Полиномиальный гладкий минимум Иниго Квилеса (IQ polynomial smin)
    float smin(float a, float b, float k) {
        float h = clamp(0.5 + 0.5 * (b - a) / k, 0.0, 1.0);
        return mix(b, a, h) - k * h * (1.0 - h);
    }

    half4 main(float2 fragCoord) {
        float w = u_resolution.x;
        float h = u_resolution.y;
        if (w <= 0.0 || h <= 0.0) {
            return half4(0.0);
        }

        // Нормализованные координаты
        float nx = fragCoord.x / w;

        // Биоморфная волна вертикальной оси (органическое дыхание сосуда)
        float cy = h * (0.5 + 0.022 * sin(nx * 6.28318 - u_time * 1.5));
        float dy = fragCoord.y - cy;

        // X-координаты 5 центров камер вдоль оси биоцикла
        float x1 = w * 0.12;
        float x2 = w * 0.32;
        float x3 = w * 0.51;
        float x4 = w * 0.70;
        float x5 = w * 0.88;

        // Бегущие перистальтические гармоники для каждого узла
        float pWave1 = sin(u_time * 2.2 - (x1 / w) * 9.42);
        float pWave2 = sin(u_time * 2.2 - (x2 / w) * 9.42);
        float pWave3 = sin(u_time * 2.2 - (x3 / w) * 9.42);
        float pWave4 = sin(u_time * 2.2 - (x4 / w) * 9.42);
        float pWave5 = sin(u_time * 2.2 - (x5 / w) * 9.42);

        // Калибры камер по референсу:
        // Узел 1: средний, Узел 2: крупный, Узел 3: малый перешеек, Узел 4: крупный, Узел 5: концевой сужающийся хвост
        float r1 = h * 0.27 * (1.0 + 0.08 * pWave1);
        float r2 = h * 0.38 * (1.0 + 0.09 * pWave2);
        float r3 = h * 0.21 * (1.0 + 0.12 * pWave3);
        float r4 = h * 0.36 * (1.0 + 0.09 * pWave4);
        float r5 = h * 0.23 * (1.0 + 0.08 * pWave5);

        // Дистанционные поля (SDF) эллиптических везикул
        float d1 = length(float2((fragCoord.x - x1) * 0.88, dy)) - r1;
        float d2 = length(float2((fragCoord.x - x2) * 0.90, dy)) - r2;
        float d3 = length(float2((fragCoord.x - x3) * 0.85, dy)) - r3;
        float d4 = length(float2((fragCoord.x - x4) * 0.90, dy)) - r4;
        float d5 = length(float2((fragCoord.x - x5) * 0.88, dy)) - r5;

        // Непрерывный внутренний просвет (люмен), соединяющий крайние камеры
        float tSeg = clamp((fragCoord.x - x1) / (x5 - x1), 0.0, 1.0);
        float xSeg = mix(x1, x5, tSeg);
        float rChannel = h * 0.12 * (1.0 + 0.14 * sin(u_time * 2.0 - nx * 12.0));
        float dChannel = length(float2(fragCoord.x - xSeg, dy)) - rChannel;

        // Слияние метаболов камер и связующего канала через гладкий минимум
        float k = h * 0.22;
        float d = d1;
        d = smin(d, d2, k);
        d = smin(d, d3, k);
        d = smin(d, d4, k);
        d = smin(d, d5, k);
        d = smin(d, dChannel, h * 0.15);

        // Активный пищевой болюс (энергетический пакет биоцикла)
        float progressClamped = clamp(u_phaseProgress, 0.0, 1.0);
        float bolusX = mix(x1, x5, progressClamped);
        float distBolus = length(float2((fragCoord.x - bolusX) * 0.70, dy));
        float bolusGlow = exp(-distBolus / (h * 0.28));
        float bolusPulse = 0.25 * (sin(u_time * 4.5) * 0.5 + 0.5);
        bolusGlow *= (1.0 + bolusPulse);

        // Параметры биолюминесцентного контура
        float edgeLine = smoothstep(1.8, 0.0, abs(d));
        float depth = -d;
        float normDepth = clamp(depth / (h * 0.32), 0.0, 1.0);

        // Свечение кромок (Fresnel rim): верхний/нижний край и тонкие шейки светятся ярко
        float rimGlowVal = pow(1.0 - normDepth, 2.2);
        float verticalRim = smoothstep(0.18, 0.92, abs(dy) / (h * 0.34));
        rimGlowVal = mix(rimGlowVal, 1.0, verticalRim * 0.65);

        // Плотное темное питательное ядро в глубине широких камер
        float coreMix = smoothstep(0.20, 0.85, normDepth);

        // Продольные микро-струи питательного потока
        float flow = sin((fragCoord.x - bolusX) * 0.10 - u_time * 3.0) * 0.5 + 0.5;
        float internalStream = flow * (1.0 - coreMix * 0.7) * 0.18;

        // Внешний диффузный световой ореол (Aura)
        float halo = exp(-max(d, 0.0) / (h * 0.065)) * 0.60;

        half4 col;
        if (d <= 0.0) {
            // Внутри био-сосуда
            half4 baseCore = mix(u_accentColor, u_darkCore, half(coreMix));
            col = mix(baseCore, u_rimGlow, half(rimGlowVal * 0.82));
            col += u_rimGlow * half(edgeLine * 0.75);
            col += u_rimGlow * half(bolusGlow * 0.65);
            col += u_accentColor * half(internalStream);
            col.a = half(clamp(0.92 + edgeLine * 0.08, 0.0, 1.0));
        } else {
            // Снаружи: ореол биолюминесцентного свечения
            col = u_accentColor * half(halo);
            col += u_rimGlow * half(edgeLine * 0.65);
            col += u_rimGlow * half(bolusGlow * halo * 0.85);
            col.a = half(clamp(halo * 0.90 + edgeLine * 0.65, 0.0, 1.0));
        }

        // Premultiplied alpha для корректного Compose блендинга
        col.rgb *= col.a;
        return col;
    }
"""

/**
 * БАЗОВЫЙ ПЕРИСТАЛЬТИЧЕСКИЙ БИО-БАР (ШЕЙДЕР + CANVAS FALLBACK)
 *
 * @param modifier Модификатор размера и расположения
 * @param phaseProgress Текущий прогресс биоцикла от 0.0f до 1.0f
 * @param accentColor Главный акцентный неон темы (по умолчанию BioLime)
 * @param darkCore Темное флюидное ядро (по умолчанию DarkMoss)
 * @param rimGlow Неоновый краевой оттенок свечения кромок
 */
@Composable
fun BiocodeBiocycleWaveBar(
    modifier: Modifier = Modifier,
    phaseProgress: Float = 0.45f,
    accentColor: Color = LocalBiocodeColors.current.bioLime,
    darkCore: Color = LocalBiocodeColors.current.darkMoss,
    rimGlow: Color = lerp(accentColor, Color.White, 0.35f)
) {
    // Высокоточный генератор непрерывного времени (секунды) без рывков
    val timeSeconds by produceState(initialValue = 0f) {
        val startNanos = System.nanoTime()
        while (true) {
            withFrameNanos { frameNanos ->
                value = (frameNanos - startNanos) / 1_000_000_000f
            }
        }
    }

    // Аппаратный шейдер AGSL для Android 13+ (API 33)
    val runtimeShader = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            try {
                RuntimeShader(BIOCYCLE_WAVE_AGSL)
            } catch (e: Throwable) {
                null
            }
        } else {
            null
        }
    }

    val paint = remember {
        AndroidPaint().apply {
            isAntiAlias = true
        }
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && runtimeShader != null) {
            // Настройка униформов AGSL
            runtimeShader.setFloatUniform("u_resolution", size.width, size.height)
            runtimeShader.setFloatUniform("u_time", timeSeconds)
            runtimeShader.setColorUniform("u_accentColor", accentColor.toArgb())
            runtimeShader.setColorUniform("u_darkCore", darkCore.toArgb())
            runtimeShader.setColorUniform("u_rimGlow", rimGlow.toArgb())
            runtimeShader.setFloatUniform("u_phaseProgress", phaseProgress.coerceIn(0f, 1f))

            paint.shader = runtimeShader
            drawIntoCanvas { canvas ->
                canvas.nativeCanvas.drawRect(0f, 0f, size.width, size.height, paint)
            }
        } else {
            // Векторный Canvas Fallback для API < 33
            drawBiocycleWaveFallback(
                time = timeSeconds,
                phaseProgress = phaseProgress.coerceIn(0f, 1f),
                accentColor = accentColor,
                darkCore = darkCore,
                rimGlow = rimGlow
            )
        }
    }
}

/**
 * ВЕКТОРНЫЙ CANVAS FALLBACK (ДЛЯ УСТРОЙСТВ СТАРШЕ ANDROID 13)
 *
 * Точно воспроизводит органическую форму 5-камерного перистальтического тракта
 * с помощью кривых Безье, многослойных градиентов и неонового ореола.
 */
private fun DrawScope.drawBiocycleWaveFallback(
    time: Float,
    phaseProgress: Float,
    accentColor: Color,
    darkCore: Color,
    rimGlow: Color
) {
    val w = size.width
    val h = size.height
    if (w <= 0f || h <= 0f) return

    val cy = h * (0.5f + 0.022f * sin(time * 1.5f))

    val x1 = w * 0.12f
    val x2 = w * 0.32f
    val x3 = w * 0.51f
    val x4 = w * 0.70f
    val x5 = w * 0.88f

    val pWave1 = sin(time * 2.2f - (x1 / w) * 9.42f)
    val pWave2 = sin(time * 2.2f - (x2 / w) * 9.42f)
    val pWave3 = sin(time * 2.2f - (x3 / w) * 9.42f)
    val pWave4 = sin(time * 2.2f - (x4 / w) * 9.42f)
    val pWave5 = sin(time * 2.2f - (x5 / w) * 9.42f)

    val r1 = h * 0.27f * (1f + 0.08f * pWave1)
    val r2 = h * 0.38f * (1f + 0.09f * pWave2)
    val r3 = h * 0.21f * (1f + 0.12f * pWave3)
    val r4 = h * 0.36f * (1f + 0.09f * pWave4)
    val r5 = h * 0.23f * (1f + 0.08f * pWave5)

    val neckR = h * 0.12f * (1f + 0.14f * sin(time * 2.0f))

    // Построение замкнутого силуэта тракта с органическими перетяжками
    val vesselPath = Path().apply {
        // Верхний контур слева направо
        moveTo(x1 - r1 * 1.05f, cy)
        cubicTo(x1 - r1 * 1.05f, cy - r1, x1 - r1 * 0.5f, cy - r1, x1, cy - r1)

        // Перешеек 1 -> 2
        val midX12 = (x1 + x2) * 0.5f
        cubicTo(x1 + r1 * 0.6f, cy - r1 * 0.9f, midX12 - neckR, cy - neckR, midX12, cy - neckR)
        cubicTo(midX12 + neckR, cy - neckR, x2 - r2 * 0.6f, cy - r2 * 0.9f, x2, cy - r2)

        // Перешеек 2 -> 3
        val midX23 = (x2 + x3) * 0.5f
        cubicTo(x2 + r2 * 0.6f, cy - r2 * 0.9f, midX23 - neckR, cy - neckR, midX23, cy - neckR)
        cubicTo(midX23 + neckR, cy - neckR, x3 - r3 * 0.6f, cy - r3 * 0.9f, x3, cy - r3)

        // Перешеек 3 -> 4
        val midX34 = (x3 + x4) * 0.5f
        cubicTo(x3 + r3 * 0.6f, cy - r3 * 0.9f, midX34 - neckR, cy - neckR, midX34, cy - neckR)
        cubicTo(midX34 + neckR, cy - neckR, x4 - r4 * 0.6f, cy - r4 * 0.9f, x4, cy - r4)

        // Перешеек 4 -> 5
        val midX45 = (x4 + x5) * 0.5f
        cubicTo(x4 + r4 * 0.6f, cy - r4 * 0.9f, midX45 - neckR, cy - neckR, midX45, cy - neckR)
        cubicTo(midX45 + neckR, cy - neckR, x5 - r5 * 0.6f, cy - r5 * 0.9f, x5, cy - r5)

        // Правый купол камеры 5
        cubicTo(x5 + r5 * 0.5f, cy - r5, x5 + r5 * 1.05f, cy - r5, x5 + r5 * 1.05f, cy)

        // Нижний контур справа налево
        cubicTo(x5 + r5 * 1.05f, cy + r5, x5 + r5 * 0.5f, cy + r5, x5, cy + r5)

        // Нижний перешеек 5 -> 4
        cubicTo(x5 - r5 * 0.6f, cy + r5 * 0.9f, midX45 + neckR, cy + neckR, midX45, cy + neckR)
        cubicTo(midX45 - neckR, cy + neckR, x4 + r4 * 0.6f, cy + r4 * 0.9f, x4, cy + r4)

        // Нижний перешеек 4 -> 3
        cubicTo(x4 - r4 * 0.6f, cy + r4 * 0.9f, midX34 + neckR, cy + neckR, midX34, cy + neckR)
        cubicTo(midX34 - neckR, cy + neckR, x3 + r3 * 0.6f, cy + r3 * 0.9f, x3, cy + r3)

        // Нижний перешеек 3 -> 2
        cubicTo(x3 - r3 * 0.6f, cy + r3 * 0.9f, midX23 + neckR, cy + neckR, midX23, cy + neckR)
        cubicTo(midX23 - neckR, cy + neckR, x2 + r2 * 0.6f, cy + r2 * 0.9f, x2, cy + r2)

        // Нижний перешеек 2 -> 1
        cubicTo(x2 - r2 * 0.6f, cy + r2 * 0.9f, midX12 + neckR, cy + neckR, midX12, cy + neckR)
        cubicTo(midX12 - neckR, cy + neckR, x1 + r1 * 0.6f, cy + r1 * 0.9f, x1, cy + r1)

        // Левый купол камеры 1
        cubicTo(x1 - r1 * 0.5f, cy + r1, x1 - r1 * 1.05f, cy + r1, x1 - r1 * 1.05f, cy)
        close()
    }

    // 1. Внешний неоновый ореол (Aura glow)
    drawPath(
        path = vesselPath,
        color = accentColor.copy(alpha = 0.12f),
        style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
    )
    drawPath(
        path = vesselPath,
        color = accentColor.copy(alpha = 0.28f),
        style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
    )

    // 2. Внутреннее заполнение с вертикальным оптическим градиентом
    val bodyGradient = Brush.verticalGradient(
        colors = listOf(
            accentColor.copy(alpha = 0.70f),
            darkCore.copy(alpha = 0.92f),
            darkCore.copy(alpha = 0.98f),
            darkCore.copy(alpha = 0.92f),
            accentColor.copy(alpha = 0.70f)
        ),
        startY = cy - h * 0.45f,
        endY = cy + h * 0.45f
    )
    drawPath(path = vesselPath, brush = bodyGradient)

    // 3. Ядра плотной питательной плазмы внутри узлов
    val nodes = listOf(
        Pair(Offset(x1, cy), r1 * 0.65f),
        Pair(Offset(x2, cy), r2 * 0.72f),
        Pair(Offset(x3, cy), r3 * 0.55f),
        Pair(Offset(x4, cy), r4 * 0.70f),
        Pair(Offset(x5, cy), r5 * 0.58f)
    )
    nodes.forEach { (center, radius) ->
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    darkCore,
                    darkCore.copy(alpha = 0.85f),
                    Color.Transparent
                ),
                center = center,
                radius = radius
            ),
            radius = radius,
            center = center
        )
    }

    // 4. Тонкая резкая люминесцентная мембрана по контуру
    drawPath(
        path = vesselPath,
        brush = Brush.verticalGradient(
            colors = listOf(
                rimGlow.copy(alpha = 0.95f),
                accentColor.copy(alpha = 0.40f),
                rimGlow.copy(alpha = 0.95f)
            ),
            startY = cy - h * 0.4f,
            endY = cy + h * 0.4f
        ),
        style = Stroke(width = 1.6.dp.toPx())
    )

    // 5. Активный метаболический болюс (пищевой сгусток)
    val bolusX = x1 + (x5 - x1) * phaseProgress
    val bolusCenter = Offset(bolusX, cy)
    val bolusPulse = 1f + 0.18f * sin(time * 4.5f)

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                rimGlow.copy(alpha = 0.85f),
                accentColor.copy(alpha = 0.50f),
                Color.Transparent
            ),
            center = bolusCenter,
            radius = 20.dp.toPx() * bolusPulse
        ),
        radius = 20.dp.toPx() * bolusPulse,
        center = bolusCenter
    )

    drawCircle(
        color = rimGlow,
        radius = 3.5.dp.toPx() * bolusPulse,
        center = bolusCenter
    )
}

/**
 * ТЕХНИЧЕСКИЙ БРЕКЕТ-ТАЙМЛАЙН БИОЦИКЛА
 *
 * Отрисовывает инженерную HUD-шкалу со скобами ┌ ┐, делениями фаз
 * и динамическим курсором текущего прогресса.
 */
@Composable
fun BiocycleBracketTimeline(
    modifier: Modifier = Modifier,
    phaseProgress: Float = 0.45f,
    accentColor: Color = LocalBiocodeColors.current.bioLime,
    deckBorderColor: Color = LocalBiocodeColors.current.deckBorder,
    tealColor: Color = LocalBiocodeColors.current.pineTeal,
    creamColor: Color = LocalBiocodeColors.current.noguchiCream
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(20.dp)
    ) {
        val w = size.width
        val h = size.height
        if (w <= 0f || h <= 0f) return@Canvas

        val startX = w * 0.08f
        val endX = w * 0.92f
        val bracketSpan = endX - startX
        val trackY = h * 0.55f
        val cornerDrop = 5.dp.toPx()

        // 1. Базовая линия с концевыми скобами ┌ ┐
        val bracketPath = Path().apply {
            moveTo(startX, trackY + cornerDrop)
            lineTo(startX, trackY)
            lineTo(endX, trackY)
            lineTo(endX, trackY + cornerDrop)
        }

        drawPath(
            path = bracketPath,
            color = deckBorderColor.copy(alpha = 0.75f),
            style = Stroke(width = 1.2.dp.toPx(), cap = StrokeCap.Square)
        )

        // 2. Деления фаз (4 фазы / 5 узлов)
        val tickCount = 16
        for (i in 0..tickCount) {
            val t = i.toFloat() / tickCount
            val tx = startX + bracketSpan * t
            val isMajor = (i % 4 == 0)
            val tickHeight = if (isMajor) 6.dp.toPx() else 3.dp.toPx()
            val isPassed = t <= phaseProgress

            val tickColor = when {
                isPassed && isMajor -> accentColor.copy(alpha = 0.95f)
                isPassed -> accentColor.copy(alpha = 0.55f)
                isMajor -> tealColor.copy(alpha = 0.85f)
                else -> tealColor.copy(alpha = 0.35f)
            }

            drawLine(
                color = tickColor,
                start = Offset(tx, trackY),
                end = Offset(tx, trackY - tickHeight),
                strokeWidth = if (isMajor) 1.5.dp.toPx() else 1.dp.toPx()
            )
        }

        // 3. Индикатор-курсор фазы (неоновый треугольник со световодом)
        val cursorX = startX + bracketSpan * phaseProgress.coerceIn(0f, 1f)

        // Вертикальный световод вниз к перистальтическому бару
        drawLine(
            brush = Brush.verticalGradient(
                colors = listOf(accentColor, Color.Transparent),
                startY = trackY,
                endY = h
            ),
            start = Offset(cursorX, trackY),
            end = Offset(cursorX, h),
            strokeWidth = 1.dp.toPx()
        )

        // Неоновый курсор (указатель)
        val cursorPath = Path().apply {
            moveTo(cursorX, trackY + 2.dp.toPx())
            lineTo(cursorX - 3.5.dp.toPx(), trackY - 5.dp.toPx())
            lineTo(cursorX + 3.5.dp.toPx(), trackY - 5.dp.toPx())
            close()
        }
        drawPath(path = cursorPath, color = accentColor)
    }
}

/**
 * ПОЛНЫЙ МОДУЛЬ БИОЦИКЛА BIOCODE (BENTO CONTAINER + HUD + WAVE BAR)
 *
 * Интегрирует:
 * - Технический заголовок HUD: "ПРИЕМЫ ПИЩИ // БИОЦИКЛ", "ФАЗА 02"
 * - Инженерную верхнюю скобочную шкалу (Bracket Timeline)
 * - Перистальтический флюидный био-бар (AGSL на Android 13+ / Canvas на Android < 13)
 * - Нижние маркеры био-фаз
 *
 * @param modifier Модификатор внешнего контейнера
 * @param currentPhaseText Название активной фазы (например, "ФАЗА 02")
 * @param categoryText Категория телеметрии (например, "ПРИЕМЫ ПИЩИ // БИОЦИКЛ")
 * @param phaseProgress Прогресс прохождения текущей фазы / суточного цикла (0.0f..1.0f)
 */
@Composable
fun BiocodeBiocycleWaveModule(
    modifier: Modifier = Modifier,
    currentPhaseText: String = "ФАЗА 02",
    categoryText: String = "ПРИЕМЫ ПИЩИ // БИОЦИКЛ",
    phaseProgress: Float = 0.45f
) {
    val colors = LocalBiocodeColors.current

    // Плавная анимация индикатора прогресса
    val animatedProgress by animateFloatAsState(
        targetValue = phaseProgress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "PhaseProgressAnim"
    )

    // Пульсация статусного LED индикатора
    val infiniteTransition = rememberInfiniteTransition(label = "LedBlink")
    val ledAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "LedAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(colors.spruceDeck)
            .border(1.dp, colors.deckBorder, RoundedCornerShape(18.dp))
            .padding(top = 14.dp, bottom = 12.dp, start = 14.dp, end = 14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // 1. ВЕРХНЯЯ СТРОКА ТЕЛЕМЕТРИИ HUD
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Левая группа: Статусный LED + Название категории
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(colors.bioLime.copy(alpha = ledAlpha))
                    )
                    Text(
                        text = categoryText.uppercase(),
                        style = BiocodeTypography.TelemetryLabel.copy(
                            color = colors.noguchiCream.copy(alpha = 0.72f),
                            letterSpacing = 1.4.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                // Правая группа: Бейдж активной фазы
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(colors.pillActiveBg)
                        .border(1.dp, colors.bioLime.copy(alpha = 0.65f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = currentPhaseText.uppercase(),
                        style = BiocodeTypography.TabLabel.copy(
                            color = colors.bioLime,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            letterSpacing = 1.sp
                        )
                    )
                }
            }

            // 2. ИНЖЕНЕРНЫЙ БРЕКЕТ-ТАЙМЛАЙН СО СКОБАМИ
            BiocycleBracketTimeline(
                modifier = Modifier.padding(top = 2.dp),
                phaseProgress = animatedProgress,
                accentColor = colors.bioLime,
                deckBorderColor = colors.deckBorder,
                tealColor = colors.pineTeal,
                creamColor = colors.noguchiCream
            )

            // 3. ПЕРИСТАЛЬТИЧЕСКИЙ ВОЛНОВОЙ БИО-БАР (AGSL / FALLBACK)
            BiocodeBiocycleWaveBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp),
                phaseProgress = animatedProgress,
                accentColor = colors.bioLime,
                darkCore = colors.darkMoss,
                rimGlow = lerp(colors.bioLime, Color.White, 0.35f)
            )

            // 4. ТЕЛЕМЕТРИЧЕСКАЯ ШКАЛА УЗЛОВ ФАЗ
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf(
                    "01 // УТРО",
                    "02 // ДЕНЬ",
                    "03 // ВЕЧЕР",
                    "04 // РЕГЕНЕРАЦИЯ"
                ).forEachIndexed { index, label ->
                    val phaseThreshold = (index + 1) * 0.25f
                    val isActive = animatedProgress >= index * 0.25f && animatedProgress < phaseThreshold
                    Text(
                        text = label,
                        style = BiocodeTypography.TelemetryLabel.copy(
                            color = if (isActive) colors.bioLime else colors.noguchiCream.copy(alpha = 0.35f),
                            fontSize = 8.sp,
                            letterSpacing = 0.8.sp,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium
                        )
                    )
                }
            }
        }
    }
}

/**
 * ИНТЕРАКТИВНЫЙ ДЕМО-КОМПОНЕНТ ДЛЯ ТЕСТИРОВАНИЯ И ПРЕВЬЮ
 */
@Composable
fun BiocodeBiocycleWaveBarDemo() {
    var phaseProgress by remember { mutableFloatStateOf(0.45f) }
    var currentPhaseName by remember { mutableStateOf("ФАЗА 02") }
    var currentThemeMode by remember { mutableStateOf(BiocodeThemeMode.NOGUCHI_GREEN) }

    val currentColors = remember(currentThemeMode) {
        getBiocodeThemeColors(currentThemeMode)
    }

    // Локальное переопределение цветов для превью
    BiocodePalette.current = currentColors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(currentColors.deepPineBg)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Заголовок демо
        Text(
            text = "BIOCODE // BIOCYCLE WAVE ENGINE",
            style = BiocodeTypography.HeaderBrand.copy(
                fontSize = 20.sp,
                color = currentColors.noguchiCream
            )
        )

        Text(
            text = "Аппаратный перистальтический био-сосуд на AGSL Shaders (Android 13+) с автоматическим Canvas Fallback.",
            style = BiocodeTypography.TelemetryLabel.copy(
                color = currentColors.noguchiCream.copy(alpha = 0.6f),
                lineHeight = 14.sp
            )
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Основной модуль
        BiocodeBiocycleWaveModule(
            modifier = Modifier.fillMaxWidth(),
            currentPhaseText = currentPhaseName,
            categoryText = "ПРИЕМЫ ПИЩИ // БИОЦИКЛ",
            phaseProgress = phaseProgress
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Панель интерактивного управления
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(currentColors.spruceDeck.copy(alpha = 0.6f))
                .border(1.dp, currentColors.deckBorderSubtle, RoundedCornerShape(14.dp))
                .padding(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ИНТЕРАКТИВНЫЙ БОЛЮС:",
                        style = BiocodeTypography.TelemetryLabel.copy(
                            color = currentColors.noguchiCream.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "${(phaseProgress * 100).toInt()}%",
                        style = BiocodeTypography.ValueNumber.copy(
                            color = currentColors.bioLime
                        )
                    )
                }

                Slider(
                    value = phaseProgress,
                    onValueChange = {
                        phaseProgress = it
                        currentPhaseName = when {
                            it < 0.25f -> "ФАЗА 01"
                            it < 0.50f -> "ФАЗА 02"
                            it < 0.75f -> "ФАЗА 03"
                            else -> "ФАЗА 04"
                        }
                    },
                    colors = SliderDefaults.colors(
                        thumbColor = currentColors.bioLime,
                        activeTrackColor = currentColors.bioLime,
                        inactiveTrackColor = currentColors.deckBorder
                    )
                )

                // Кнопки быстрого переключения фаз
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        "01" to 0.12f,
                        "02" to 0.38f,
                        "03" to 0.62f,
                        "04" to 0.88f
                    ).forEach { (label, progressVal) ->
                        val isSelected = currentPhaseName == "ФАЗА $label"
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) currentColors.pillActiveBg else currentColors.pillInactiveBg)
                                .border(
                                    1.dp,
                                    if (isSelected) currentColors.bioLime else currentColors.deckBorderSubtle,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    phaseProgress = progressVal
                                    currentPhaseName = "ФАЗА $label"
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Ф-$label",
                                style = BiocodeTypography.TabLabel.copy(
                                    color = if (isSelected) currentColors.bioLime else currentColors.noguchiCream.copy(alpha = 0.5f),
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Кнопка смены цветовой темы Biocode
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(currentColors.pillActiveBg)
                        .border(1.dp, currentColors.limeBorder, RoundedCornerShape(8.dp))
                        .clickable {
                            currentThemeMode = currentThemeMode.next()
                        }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "СМЕНИТЬ ТЕМУ: ${currentThemeMode.title}",
                        style = BiocodeTypography.TabLabel.copy(
                            color = currentColors.bioLime,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }
    }
}
