package com.biocode.engine

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.withInfiniteAnimationFrameMillis
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * BIOCODE BIOMORPHIC FLUID MEMBRANE SHADER (AGSL / SkSL)
 * ═══════════════════════════════════════════════════════════════════════════════
 *
 * Математическая модель живой цитоплазматической амебы / метаболла:
 * 1. Центральное органическое ядро на базе скругленного прямоугольника.
 * 2. 5 гармонически осциллирующих лопастей/псевдоподий, сливающихся через smin(a, b, k).
 * 3. 2 орбитальных микро-капли (сателлита) с динамическим поверхностным натяжением.
 * 4. Высокочастотная краевая ондуляция клеточной мембраны (u_time).
 * 5. Желатиновый цитоплазматический обод с 2.5D нормалями и Fresnel-свечением.
 * 6. Глубокая темная центральная каверна (d < -rimWidth) для 100% читаемости текста.
 * 7. Быстро затухающая внешняя аура рассеянного люминесцентного ореола (d > 0).
 */
const val BIOMORPHIC_MEMBRANE_AGSL = """
uniform float2 u_resolution;
uniform float u_time;
uniform layout(color) half4 u_accentColor;
uniform layout(color) half4 u_darkBg;
uniform layout(color) half4 u_rimGlow;

// Полиномиальный гладкий минимум Inigo Quilez
float smin(float a, float b, float k) {
    float h = clamp(0.5 + 0.5 * (b - a) / k, 0.0, 1.0);
    return mix(b, a, h) - k * h * (1.0 - h);
}

// Знакопеременная функция расстояния (SDF) скругленного прямоугольника
float sdRoundedBox(float2 p, float2 b, float r) {
    float2 q = abs(p) - b + float2(r, r);
    return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - r;
}

// Расчет единого поля расстояний метаболической мембраны
float evaluateMembraneSDF(float2 p, float t, float2 res) {
    float margin = clamp(min(res.x, res.y) * 0.12, 18.0, 32.0);
    float2 coreHalf = res * 0.5 - float2(margin, margin);
    float cornerR = clamp(min(coreHalf.x, coreHalf.y) * 0.35, 16.0, 30.0);

    // 1. Базовое органическое ядро
    float d = sdRoundedBox(p, coreHalf, cornerR);

    // 2. 5 гармонических псевдоподий (метаболлов)
    // Лопасть 1: Верхний правый сектор (пульсация и покачивание)
    float2 c1 = float2(
        coreHalf.x * (0.60 + 0.18 * sin(t * 0.70 + 1.20)),
        -coreHalf.y * (0.50 + 0.22 * cos(t * 0.90 + 0.40))
    );
    float r1 = margin * (1.10 + 0.30 * sin(t * 1.10));
    float d1 = length(p - c1) - r1;

    // Лопасть 2: Нижний левый сектор (противофазное дыхание)
    float2 c2 = float2(
        -coreHalf.x * (0.58 + 0.20 * cos(t * 0.80 + 2.10)),
        coreHalf.y * (0.52 + 0.20 * sin(t * 1.00 + 1.70))
    );
    float r2 = margin * (1.15 + 0.28 * cos(t * 0.85 + 0.50));
    float d2 = length(p - c2) - r2;

    // Лопасть 3: Верхний выступ
    float2 c3 = float2(
        coreHalf.x * (-0.20 + 0.30 * sin(t * 0.60 + 4.00)),
        -coreHalf.y * (0.75 + 0.15 * sin(t * 1.30))
    );
    float r3 = margin * (0.95 + 0.25 * sin(t * 1.20 + 2.00));
    float d3 = length(p - c3) - r3;

    // Лопасть 4: Нижний гребень
    float2 c4 = float2(
        coreHalf.x * (0.18 + 0.26 * cos(t * 0.75 + 3.1415)),
        coreHalf.y * (0.72 + 0.16 * cos(t * 1.10 + 1.00))
    );
    float r4 = margin * (1.00 + 0.25 * cos(t * 0.95));
    float d4 = length(p - c4) - r4;

    // Лопасть 5: Боковой латеральный узел
    float2 c5 = float2(
        -coreHalf.x * (0.80 + 0.12 * sin(t * 0.50 + 0.80)),
        coreHalf.y * (-0.12 + 0.24 * cos(t * 0.70 + 2.50))
    );
    float r5 = margin * (0.90 + 0.22 * sin(t * 1.40 + 3.00));
    float d5 = length(p - c5) - r5;

    // Плавное гидродинамическое слияние ядер
    float k = margin * 1.20;
    d = smin(d, d1, k);
    d = smin(d, d2, k);
    d = smin(d, d3, k);
    d = smin(d, d4, k);
    d = smin(d, d5, k);

    // 3. Две орбитальные капли-спутника
    // Сателлит 1: Прямая медленная орбита
    float a1 = t * 0.45;
    float2 orbit1 = coreHalf + float2(margin * 0.55, margin * 0.50);
    float2 sat1 = float2(cos(a1) * orbit1.x, sin(a1) * orbit1.y);
    float satR1 = margin * 0.28;
    float dSat1 = length(p - sat1) - satR1;

    // Сателлит 2: Встречная эксцентрическая орбита
    float a2 = -t * 0.38 + 2.80;
    float2 orbit2 = coreHalf + float2(margin * 0.65, margin * 0.45);
    float2 sat2 = float2(cos(a2) * orbit2.x, sin(a2) * orbit2.y);
    float satR2 = margin * 0.22;
    float dSat2 = length(p - sat2) - satR2;

    // Упругое отсекание/мостикование капель
    float kSat = margin * 0.45;
    d = smin(d, dSat1, kSat);
    d = smin(d, dSat2, kSat);

    // 4. Тонкая гармоническая ондуляция мембраны
    float angle = atan(p.y, p.x + 0.0001);
    float undulation = sin(angle * 4.0 + t * 2.0) * 1.20
                     + cos(angle * 6.0 - t * 1.5) * 0.80
                     + sin(p.x * 0.035 + t) * cos(p.y * 0.035 - t * 0.80) * 0.90;
    d += undulation * 0.70;

    return d;
}

half4 main(float2 fragCoord) {
    float2 center = u_resolution * 0.5;
    float2 p = fragCoord - center;
    float t = u_time * 1.5;

    float d = evaluateMembraneSDF(p, t, u_resolution);
    float rimWidth = clamp(min(u_resolution.x, u_resolution.y) * 0.08, 10.0, 16.0);

    // Численный градиент (центральная разность) для получения точной нормали контура
    float eps = 1.0;
    float dx = evaluateMembraneSDF(p + float2(eps, 0.0), t, u_resolution) - evaluateMembraneSDF(p - float2(eps, 0.0), t, u_resolution);
    float dy = evaluateMembraneSDF(p + float2(0.0, eps), t, u_resolution) - evaluateMembraneSDF(p - float2(0.0, eps), t, u_resolution);
    float2 grad2D = float2(dx, dy);
    float gradLen = length(grad2D);
    float2 n2D = gradLen > 0.0001 ? grad2D / gradLen : float2(0.0, 1.0);

    // 2.5D нормаль полусферического купола желатиновой мембраны
    // u: 0.0 у внутреннего края (каверна), 1.0 у наружной границы
    float u = clamp((d + rimWidth) / rimWidth, 0.0, 1.0);
    float angleDome = u * 3.14159265;
    float nZ = sin(angleDome);
    float nRadial = cos(angleDome);
    float3 N = normalize(float3(n2D * nRadial, nZ * 1.40));

    // Оптическая модель освещения: Вектор взгляда и Fresnel-эффект
    float3 V = float3(0.0, 0.0, 1.0);
    float NdotV = clamp(dot(N, V), 0.0, 1.0);
    float fresnel = pow(1.0 - NdotV, 2.60);

    // Виртуальный источник света для сочного глянцевого блика
    float3 L = normalize(float3(0.35, -0.65, 0.70));
    float diffuse = max(dot(N, L), 0.0);
    float3 H = normalize(L + V);
    float specular = pow(max(dot(N, H), 0.0), 36.0);

    // Цветовая композиция желатинового обода
    half3 rimBase = mix(u_accentColor.rgb * 0.70, u_rimGlow.rgb, half(fresnel * 0.85));
    rimBase += half3(u_accentColor.rgb * half(diffuse * 0.35));
    rimBase += half3(u_rimGlow.rgb * half(specular * 0.95));

    // Биолюминесцентная волна подповерхностного свечения
    float bioPulse = 0.5 + 0.5 * sin(t * 2.20 + p.x * 0.02 + p.y * 0.02);
    rimBase += half3(u_accentColor.rgb * half(bioPulse * 0.12));

    // Глубокий монолитный фон центральной каверны (для идеальной читаемости метрик)
    half3 interiorColor = mix(u_darkBg.rgb, u_accentColor.rgb * 0.08, 0.12);

    // Плавный переход от каверны к ободу
    float rimFactor = smoothstep(-rimWidth, -rimWidth * 0.65, d);
    half3 surfaceColor = mix(interiorColor, rimBase, half(rimFactor));

    // Сглаживание внешнего контура мембраны (Anti-Aliasing)
    float bodyAlpha = smoothstep(0.8, -0.8, d);

    // Внешний рассеянный люминесцентный ореол (Aura)
    float aura = exp(-max(d, 0.0) * 0.22) * smoothstep(22.0, 0.0, d);
    half3 auraColor = mix(u_accentColor.rgb, u_rimGlow.rgb, 0.30);

    // Итоговое смешивание с прозрачным фоном и Premultiplied Alpha
    half3 finalRgb = mix(auraColor, surfaceColor, half(bodyAlpha));
    half finalAlpha = half(clamp(bodyAlpha * 0.97 + (1.0 - bodyAlpha) * aura * 0.60, 0.0, 1.0));

    return half4(finalRgb * finalAlpha, finalAlpha);
}
"""

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * BIOCODE FLUID MEMBRANE CARD (КОМПОНЕНТ ЖИВОЙ МЕМБРАНЫ)
 * ═══════════════════════════════════════════════════════════════════════════════
 *
 * Органическая амебоидная карточка приема пищи с аппаратным рендерингом AGSL
 * на Android 13+ (API 33+) и адаптивным графическим Canvas-фоллбэком на ранних версиях.
 *
 * @param modifier Модификатор разметки карточки
 * @param title Название блюда или заголовок карточки
 * @param macros Форматированная строка БЖУ (например, "Б:38г  Ж:14г  У:62г")
 * @param calories Калорийность блюда (например, "540")
 * @param icon Векторная иконка времени суток или типа приема пищи
 * @param accentColor Акцентный цвет мембраны (по умолчанию из LocalBiocodeColors)
 * @param darkBgColor Цвет глубокой центральной каверны (по умолчанию из LocalBiocodeColors)
 * @param rimGlowColor Цвет свечения краевого блика (по умолчанию из LocalBiocodeColors)
 * @param onClick Опциональный обработчик клика
 * @param content Опциональный пользовательский слот содержимого каверны
 */
@Composable
fun BiocodeFluidMembraneCard(
    modifier: Modifier = Modifier,
    title: String = "Овсянка с миндалем и изолятом",
    macros: String = "Б:38г  Ж:14г  У:62г",
    calories: String = "540",
    icon: ImageVector = Icons.Default.WbSunny,
    accentColor: Color = LocalBiocodeColors.current.bioLime,
    darkBgColor: Color = LocalBiocodeColors.current.darkMoss,
    rimGlowColor: Color = LocalBiocodeColors.current.noguchiCream,
    onClick: (() -> Unit)? = null,
    content: (@Composable BoxScope.() -> Unit)? = null
) {
    // 60-120 FPS непрерывная временная развертка VSYNC без скачков
    val timeSeconds by produceState(initialValue = 0f) {
        while (true) {
            withInfiniteAnimationFrameMillis { frameTimeMillis ->
                value = (frameTimeMillis / 1000f) % 10000f
            }
        }
    }

    // Инициализация AGSL RuntimeShader на Android 13+ (API 33+) с защитой от сбоев
    val runtimeShader = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            try {
                RuntimeShader(BIOMORPHIC_MEMBRANE_AGSL)
            } catch (_: Throwable) {
                null
            }
        } else {
            null
        }
    }

    val clickableModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null
        ) { onClick() }
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .then(clickableModifier)
            .height(118.dp)
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        // Фоновый рендеринг: Аппаратный AGSL шейдер либо Canvas-фоллбэк
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && runtimeShader != null) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                if (size.width > 0f && size.height > 0f) {
                    runtimeShader.setFloatUniform("u_resolution", size.width, size.height)
                    runtimeShader.setFloatUniform("u_time", timeSeconds)
                    runtimeShader.setColorUniform("u_accentColor", accentColor.toArgb())
                    runtimeShader.setColorUniform("u_darkBg", darkBgColor.toArgb())
                    runtimeShader.setColorUniform("u_rimGlow", rimGlowColor.toArgb())

                    drawRect(brush = ShaderBrush(runtimeShader))
                }
            }
        } else {
            // Превосходный процедурный органический фоллбэк для Android 8 - 12
            BiocodeFluidMembraneCanvasFallback(
                timeSeconds = timeSeconds,
                accentColor = accentColor,
                darkBgColor = darkBgColor,
                rimGlowColor = rimGlowColor,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Содержимое каверны: изолировано внутренними отступами от волнообразного обода
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 18.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            if (content != null) {
                content()
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // 1. Левый био-узел: Круглый контейнер иконки времени суток
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(LocalBiocodeColors.current.spruceDeck.copy(alpha = 0.85f))
                            .border(1.2.dp, accentColor.copy(alpha = 0.35f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = accentColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    // 2. Центральная информационная колонка
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = title,
                            style = BiocodeTypography.MonospaceTitle.copy(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = LocalBiocodeColors.current.noguchiCream,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = macros,
                            style = BiocodeTypography.TelemetryLabel.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 1.1.sp
                            ),
                            color = LocalBiocodeColors.current.noguchiCream.copy(alpha = 0.70f)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // 3. Правый модуль калорийности в фирменном стиле BIOCODE
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = calories,
                            style = BiocodeTypography.ValueNumber.copy(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            ),
                            color = accentColor
                        )
                        Text(
                            text = "KCAL",
                            style = BiocodeTypography.TelemetryLabel.copy(
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.4.sp
                            ),
                            color = LocalBiocodeColors.current.noguchiCream.copy(alpha = 0.50f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Высокопроизводительный процедурный Canvas-рендерер для Android API < 33.
 * Воссоздает органическую деформацию мембраны, Fresnel-обод и орбитальные капли.
 */
@Composable
private fun BiocodeFluidMembraneCanvasFallback(
    timeSeconds: Float,
    accentColor: Color,
    darkBgColor: Color,
    rimGlowColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        if (w <= 0f || h <= 0f) return@Canvas

        val margin = (minOf(w, h) * 0.12f).coerceIn(18f, 32f)
        val cx = w * 0.5f
        val cy = h * 0.5f
        val coreW = (w - margin * 2f).coerceAtLeast(10f)
        val coreH = (h - margin * 2f).coerceAtLeast(10f)
        val cornerR = (minOf(coreW, coreH) * 0.35f).coerceIn(16f, 30f)

        // 1. Внешняя люминесцентная аура рассеивания
        drawRoundRect(
            color = accentColor.copy(alpha = 0.12f),
            topLeft = Offset(margin * 0.5f, margin * 0.5f),
            size = Size(w - margin, h - margin),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerR * 1.3f)
        )

        // 2. Генерация волнообразного контура амебоидной мембраны
        val path = Path()
        val segments = 32
        for (i in 0..segments) {
            val theta = (i.toFloat() / segments.toFloat()) * (2f * PI.toFloat())
            val baseRx = coreW * 0.5f
            val baseRy = coreH * 0.5f

            // Формирование скругленной коробки через суперэллиптическую аппроксимацию
            val cosT = cos(theta)
            val sinT = sin(theta)
            val n = 4.0f
            val sCos = kotlin.math.sign(cosT) * kotlin.math.abs(cosT).toDouble().let { Math.pow(it, 2.0 / n) }.toFloat()
            val sSin = kotlin.math.sign(sinT) * kotlin.math.abs(sinT).toDouble().let { Math.pow(it, 2.0 / n) }.toFloat()

            // Наложение 5 органических псевдоподий
            val l1 = sin(theta * 2f + timeSeconds * 1.5f) * 6f
            val l2 = cos(theta * 3f - timeSeconds * 1.2f) * 4f
            val l3 = sin(theta * 5f + timeSeconds * 2.1f) * 2.5f

            val rX = baseRx + l1 + l3
            val rY = baseRy + l2 + l3

            val px = cx + sCos * rX
            val py = cy + sSin * rY

            if (i == 0) {
                path.moveTo(px, py)
            } else {
                path.lineTo(px, py)
            }
        }
        path.close()

        // 3. Заполнение глубокой центральной каверны
        drawPath(path = path, color = darkBgColor)

        // 4. Отрисовка желатинового цитоплазматического обода
        val rimBrush = Brush.sweepGradient(
            colors = listOf(
                accentColor.copy(alpha = 0.85f),
                rimGlowColor.copy(alpha = 0.95f),
                accentColor.copy(alpha = 0.65f),
                rimGlowColor.copy(alpha = 0.90f),
                accentColor.copy(alpha = 0.85f)
            ),
            center = Offset(cx, cy)
        )
        drawPath(
            path = path,
            brush = rimBrush,
            style = Stroke(
                width = 8.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // 5. Внутренняя световая кайма каверны
        drawPath(
            path = path,
            color = rimGlowColor.copy(alpha = 0.25f),
            style = Stroke(width = 1.5.dp.toPx())
        )

        // 6. Две орбитальные капли-сателлита
        val t = timeSeconds * 1.5f
        val a1 = t * 0.45f
        val orbit1X = coreW * 0.5f + margin * 0.45f
        val orbit1Y = coreH * 0.5f + margin * 0.40f
        val sat1 = Offset(cx + cos(a1) * orbit1X, cy + sin(a1) * orbit1Y)

        val a2 = -t * 0.38f + 2.80f
        val orbit2X = coreW * 0.5f + margin * 0.55f
        val orbit2Y = coreH * 0.5f + margin * 0.35f
        val sat2 = Offset(cx + cos(a2) * orbit2X, cy + sin(a2) * orbit2Y)

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(rimGlowColor, accentColor),
                center = sat1,
                radius = 5.dp.toPx()
            ),
            radius = 5.dp.toPx(),
            center = sat1
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(rimGlowColor, accentColor),
                center = sat2,
                radius = 4.dp.toPx()
            ),
            radius = 4.dp.toPx(),
            center = sat2
        )
    }
}

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * ДЕМОНСТРАЦИОННЫЙ ЭКРАН (BIOCODE FLUID MEMBRANE DEMO)
 * ═══════════════════════════════════════════════════════════════════════════════
 *
 * Интерактивная витрина для визуализации и тестирования живой мембраны:
 * - Основная карточка с завтраком
 * - Вторая карточка с обедом
 * - Инженерная карточка с кастомным слотом содержимого (телеметрия AGSL)
 * - Переключатель тем оформления BIOCODE
 */
@Preview(showBackground = true, backgroundColor = 0xFF061512)
@Composable
fun BiocodeFluidMembraneCardDemo() {
    var activeThemeMode by remember { mutableStateOf(BiocodeThemeMode.NOGUCHI_GREEN) }
    val themeColors = remember(activeThemeMode) { getBiocodeThemeColors(activeThemeMode) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(themeColors.deepPineBg)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Верхний бренд-хедер
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "BIOCODE ENGINE",
                    style = BiocodeTypography.HeaderBrand.copy(fontSize = 24.sp),
                    color = themeColors.noguchiCream
                )
                Text(
                    text = "AGSL BIOMORPHIC MEMBRANE // 60 FPS",
                    style = BiocodeTypography.TelemetryLabel,
                    color = themeColors.bioLime
                )
            }

            // Кнопка циклической смены темы
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(themeColors.spruceDeck)
                    .border(1.dp, themeColors.bioLime.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .clickable { activeThemeMode = activeThemeMode.next() }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = activeThemeMode.name,
                    style = BiocodeTypography.TelemetryLabel.copy(
                        fontWeight = FontWeight.Bold,
                        color = themeColors.bioLime
                    )
                )
            }
        }

        // 1. Стандартная карточка (Завтрак)
        BiocodeFluidMembraneCard(
            title = "Овсянка с миндалем и изолятом",
            macros = "Б:38г  Ж:14г  У:62г",
            calories = "540",
            icon = Icons.Default.WbSunny,
            accentColor = themeColors.bioLime,
            darkBgColor = themeColors.darkMoss,
            rimGlowColor = themeColors.noguchiCream
        )

        // 2. Вторая карточка (Обед)
        BiocodeFluidMembraneCard(
            title = "Боул с лососем и киноа",
            macros = "Б:44г  Ж:22г  У:58г",
            calories = "620",
            icon = Icons.Default.Restaurant,
            accentColor = themeColors.bioLime,
            darkBgColor = themeColors.darkMoss,
            rimGlowColor = themeColors.noguchiCream
        )

        // 3. Карточка с тренировочным шейком
        BiocodeFluidMembraneCard(
            title = "Сывороточный изолят + BCAA",
            macros = "Б:32г  Ж:2г   У:4г",
            calories = "165",
            icon = Icons.Default.FitnessCenter,
            accentColor = themeColors.bioLime,
            darkBgColor = themeColors.darkMoss,
            rimGlowColor = themeColors.noguchiCream
        )

        // 4. Карточка с кастомным слотом (AGSL телеметрия)
        BiocodeFluidMembraneCard(
            accentColor = themeColors.bioLime,
            darkBgColor = themeColors.darkMoss,
            rimGlowColor = themeColors.noguchiCream
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "AGSL SHADER CORE",
                        style = BiocodeTypography.BentoHeader.copy(
                            fontSize = 14.sp,
                            color = themeColors.noguchiCream
                        )
                    )
                    Text(
                        text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            "STATUS: HARDWARE RUNTIME SHADER ACTIVE"
                        } else {
                            "STATUS: PROCEDURAL CANVAS FALLBACK"
                        },
                        style = BiocodeTypography.TelemetryLabel.copy(
                            fontSize = 10.sp,
                            color = themeColors.bioLime
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(themeColors.pillActiveBg)
                        .border(1.dp, themeColors.deckBorder, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "LIVE GPU",
                        style = BiocodeTypography.TelemetryLabel.copy(
                            color = themeColors.bioLime,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}
