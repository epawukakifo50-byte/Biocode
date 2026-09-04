package com.biocode.engine

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.min
import kotlin.math.sqrt

// =============================================================================
// 1. БИО-КАПСУЛА С ПЕРЕТЯЖКОЙ (WAIST PILL ПО ЗОЛОТОМУ СЕЧЕНИЮ — РЕФЕРЕНС 3)
// =============================================================================

object BiocodeGeometry {
    const val GOLDEN_RATIO_INV = 0.6180339887f // 1 / Phi

    /**
     * Создает контур био-капсулы с вогнутой перетяжкой:
     * - Левый круглый узел (Head): радиус R1
     * - Вогнутая талия (Waist): перемычка строго R1 * 0.618
     * - Тело капсулы (Torso): полукруг радиуса R2 справа
     */
    fun createWaistPillPath(
        width: Float,
        height: Float,
        nodeDiameter: Float = height,
        waistRatio: Float = GOLDEN_RATIO_INV
    ): Path {
        val path = Path()
        val r1 = nodeDiameter / 2f
        val r2 = height / 2f
        val x1 = r1
        val x2 = width - r2
        val yc = height / 2f
        val dCenters = x2 - x1

        if (dCenters <= (r1 + r2) * 0.5f) {
            path.addRoundRect(
                androidx.compose.ui.geometry.RoundRect(
                    rect = Rect(0f, 0f, width, height),
                    radiusX = r2,
                    radiusY = r2
                )
            )
            return path
        }

        val halfWaistThickness = height * (waistRatio / 2f)
        val xw = x1 + dCenters * 0.44f
        val ywTop = 0f
        val ywBot = height
        val rWaist = (height - halfWaistThickness * 2f).coerceAtLeast(4f)

        path.moveTo(x2, yc - r2)
        path.lineTo(xw + rWaist, yc - halfWaistThickness)
        path.quadraticTo(xw, yc - halfWaistThickness, xw - rWaist, yc - r2)
        path.lineTo(x1, yc - r1)
        path.arcTo(
            rect = Rect(0f, 0f, nodeDiameter, nodeDiameter),
            startAngleDegrees = 270f,
            sweepAngleDegrees = -180f,
            forceMoveTo = false
        )
        path.lineTo(xw - rWaist, yc + r2)
        path.quadraticTo(xw, yc + halfWaistThickness, xw + rWaist, yc + halfWaistThickness)
        path.lineTo(x2, yc + r2)
        path.arcTo(
            rect = Rect(width - 2f * r2, 0f, width, height),
            startAngleDegrees = 90f,
            sweepAngleDegrees = -180f,
            forceMoveTo = false
        )
        path.close()
        return path
    }
}

class BiocodeWaistPillShape(
    val nodeDiameterDp: Dp? = null,
    val waistRatio: Float = BiocodeGeometry.GOLDEN_RATIO_INV
) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val dNodePx = if (nodeDiameterDp != null) with(density) { nodeDiameterDp.toPx() } else size.height
        val path = BiocodeGeometry.createWaistPillPath(size.width, size.height, dNodePx, waistRatio)
        return Outline.Generic(path)
    }
}

@Composable
fun BiocodeWaistPill(
    modifier: Modifier = Modifier,
    height: Dp = 48.dp,
    backgroundColor: Color = Color(0xFF0A2620),
    borderColor: Color = Color(0xFF1E5448),
    nodeContent: @Composable BoxScope.() -> Unit,
    bodyContent: @Composable BoxScope.() -> Unit
) {
    val shape = remember(height) { BiocodeWaistPillShape() }
    Box(
        modifier = modifier
            .height(height)
            .clip(shape)
            .background(backgroundColor)
            .border(1.dp, borderColor, shape)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(height),
                contentAlignment = Alignment.Center,
                content = nodeContent
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 6.dp, end = 14.dp),
                contentAlignment = Alignment.CenterStart,
                content = bodyContent
            )
        }
    }
}

// =============================================================================
// 2. ОРБИТАЛЬНЫЙ СТЫКОВОЧНЫЙ УЗЕЛ (РЕФЕРЕНС 4)
// =============================================================================

@Composable
fun BiocodeDockingPort(
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    ringColor: Color = Color(0xFF4AE3B5),
    socketColor: Color = Color(0xFF071916),
    plungerColor: Color = Color(0xFF0B241F),
    onClick: (() -> Unit)? = null,
    icon: @Composable BoxScope.() -> Unit
) {
    val density = LocalDensity.current
    val infiniteTransition = rememberInfiniteTransition(label = "orbit_spin")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbit_cw"
    )

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .clickable(enabled = onClick != null) { onClick?.invoke() },
        contentAlignment = Alignment.Center
    ) {
        // Концентрические орбиты на Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val outerRadius = (min(this.size.width, this.size.height) / 2f) - with(density) { 2.dp.toPx() }

            // Вращающиеся сегменты
            rotate(degrees = rotationAngle, pivot = center) {
                for (i in 0 until 4) {
                    drawArc(
                        color = ringColor.copy(alpha = 0.8f),
                        startAngle = i * 90f + 12f,
                        sweepAngle = 66f,
                        useCenter = false,
                        topLeft = Offset(center.x - outerRadius, center.y - outerRadius),
                        size = Size(outerRadius * 2f, outerRadius * 2f),
                        style = Stroke(width = with(density) { 1.5.dp.toPx() }, cap = StrokeCap.Round)
                    )
                }
            }

            // 90° Keyway вырез
            val midRadius = outerRadius * 0.74f
            drawArc(
                color = ringColor.copy(alpha = 0.45f),
                startAngle = 45f,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = Offset(center.x - midRadius, center.y - midRadius),
                size = Size(midRadius * 2f, midRadius * 2f),
                style = Stroke(width = with(density) { 1.dp.toPx() })
            )
        }

        // Внутреннее гнездо плунжера
        Box(
            modifier = Modifier
                .size(size * 0.62f)
                .clip(CircleShape)
                .background(socketColor)
                .border(1.dp, ringColor.copy(alpha = 0.6f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(size * 0.48f)
                    .clip(CircleShape)
                    .background(plungerColor),
                contentAlignment = Alignment.Center,
                content = icon
            )
        }
    }
}

// =============================================================================
// 3. КНОПКА «ЖИДКИЙ ВЫБРОС» (FLUID DROPLET BUTTON)
// =============================================================================

@Composable
fun BiocodeFluidDropletButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 66.dp,
    dropletColor: Color = Color(0xFF0A2620),
    accentColor: Color = Color(0xFF4AE3B5),
    icon: @Composable BoxScope.() -> Unit
) {
    val view = LocalView.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Пружинящее натяжение жидкости (squish & stretch)
    val dropElevation by animateDpAsState(
        targetValue = if (isPressed) (-18).dp else (-10).dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "elevation"
    )
    val scaleY by animateFloatAsState(
        targetValue = if (isPressed) 1.15f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale_y"
    )
    val scaleX by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale_x"
    )

    Box(
        modifier = modifier
            .offset(y = dropElevation)
            .size(size)
            .graphicsLayer {
                this.scaleX = scaleX
                this.scaleY = scaleY
            }
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        // Силуэт капли
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(dropletColor)
        )

        // Внутренний плунжер
        Box(
            modifier = Modifier
                .size(size * 0.72f)
                .clip(CircleShape)
                .background(Color(0xFF071916))
                .border(1.5.dp, if (isPressed) accentColor else Color(0xFF1E5448), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(size * 0.54f)
                    .clip(CircleShape)
                    .background(if (isPressed) accentColor else Color(0xFF123830)),
                contentAlignment = Alignment.Center,
                content = icon
            )
        }
    }
}
