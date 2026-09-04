package com.biocode.engine

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * МОДУЛЬНЫЙ БЕНТО-КОНТЕЙНЕР BIOCODE (РЕФЕРЕНСЫ 3 И 4)
 *
 * Поддерживает:
 * - Прямоугольные и L-образные формы с вогнутыми галтелями (concave fillets)
 * - Инженерную маркировку и бейджи телеметрии
 * - Палитру Noguchi (#272c1a, #446158, #9fd700, #fffeef)
 * - Четкий 1px контур и аппаратное ускорение
 */
@Composable
fun BiocodeBentoCard(
    modifier: Modifier = Modifier,
    title: String? = null,
    badgeText: String? = null,
    badgeColor: Color = BiocodePalette.BioLime,
    backgroundColor: Color = BiocodePalette.DarkMoss,
    borderColor: Color = BiocodePalette.NoguchiBorder,
    borderWidth: Dp = 1.dp,
    shape: Shape = RoundedCornerShape(22.dp),
    contentPadding: PaddingValues = PaddingValues(16.dp),
    showCrosshairs: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(backgroundColor)
            .border(borderWidth, borderColor, shape)
    ) {
        // Угловые инженерные маркеры (перекрестия '+')
        if (showCrosshairs) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val arm = 4.dp.toPx()
                val pad = 12.dp.toPx()
                val markColor = borderColor.copy(alpha = 0.5f)

                // Top-Left Cross
                drawLine(markColor, Offset(pad - arm, pad), Offset(pad + arm, pad), 1.2f)
                drawLine(markColor, Offset(pad, pad - arm), Offset(pad, pad + arm), 1.2f)

                // Top-Right Cross
                drawLine(markColor, Offset(size.width - pad - arm, pad), Offset(size.width - pad + arm, pad), 1.2f)
                drawLine(markColor, Offset(size.width - pad, pad - arm), Offset(size.width - pad, pad + arm), 1.2f)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding)
        ) {
            // Заголовок карточки с бейджем телеметрии
            if (title != null || badgeText != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (title != null) {
                        Text(
                            text = title.uppercase(),
                            style = BiocodeTypography.TabLabel,
                            color = BiocodePalette.NoguchiCream.copy(alpha = 0.85f)
                        )
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    if (badgeText != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(badgeColor.copy(alpha = 0.15f))
                                .border(1.dp, badgeColor.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = badgeText.uppercase(),
                                style = BiocodeTypography.TelemetryLabel,
                                color = badgeColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Основное тело карточки
            Box(
                modifier = Modifier.fillMaxWidth(),
                content = content
            )
        }
    }
}

/**
 * ФЛЮИДНЫЙ НАВИГАЦИОННЫЙ ДОК (РЕФЕРЕНС 1)
 * Верхняя или нижняя панель с плашками и сопряженными кнопками.
 */
@Composable
fun BiocodeFluidNavBar(
    title: String,
    modifier: Modifier = Modifier,
    activeSubtitle: String? = null,
    onSearchClick: () -> Unit = {},
    onActionClick: () -> Unit = {},
    onCloseClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Левый стыковочный узел с логотипом-матрицей
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(BiocodePalette.DarkMoss)
                .border(1.dp, BiocodePalette.NoguchiBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            VectorDopamineStar(
                modifier = Modifier.size(20.dp),
                tint = BiocodePalette.BioLime
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Центральная пилюля с названием раздела
        Box(
            modifier = Modifier
                .weight(1f)
                .height(46.dp)
                .clip(RoundedCornerShape(23.dp))
                .background(BiocodePalette.DarkMoss)
                .border(1.dp, BiocodePalette.NoguchiBorder, RoundedCornerShape(23.dp))
                .padding(horizontal = 18.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = BiocodeTypography.BentoHeader.copy(fontSize = 13.sp),
                    color = BiocodePalette.NoguchiCream,
                    maxLines = 1
                )
                if (activeSubtitle != null) {
                    Text(
                        text = activeSubtitle,
                        style = BiocodeTypography.TelemetryLabel.copy(fontSize = 8.sp),
                        color = BiocodePalette.BioLime,
                        maxLines = 1
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Правый кластер сопряженных круглых кнопок (Search, Share, Close)
        Row(
            modifier = Modifier
                .height(46.dp)
                .clip(RoundedCornerShape(23.dp))
                .background(BiocodePalette.DarkMoss)
                .border(1.dp, BiocodePalette.NoguchiBorder, RoundedCornerShape(23.dp))
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Кнопка Search
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(BiocodePalette.PineTeal.copy(alpha = 0.5f))
                    .clickable { onSearchClick() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "⌕",
                    fontSize = 18.sp,
                    color = BiocodePalette.NoguchiCream,
                    fontWeight = FontWeight.Bold
                )
            }

            // Кнопка Действие / Добавить
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(BiocodePalette.BioLime)
                    .clickable { onActionClick() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+",
                    fontSize = 20.sp,
                    color = BiocodePalette.DarkMoss,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
