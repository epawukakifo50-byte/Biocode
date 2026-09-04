package com.biocode.engine

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * ТОЧЕЧНО-МАТРИЧНЫЙ LED-ДИСПЛЕЙ (BIOCODE 5x7 DOT MATRIX)
 * Отрисовывает дискретные светодиодные цифры и символы.
 */
object BiocodeLedFont {
    // 5 колонок x 7 строк для каждого символа
    val GLYPHS: Map<Char, LongArray> = mapOf(
        '0' to longArrayOf(0x3E, 0x51, 0x49, 0x45, 0x3E),
        '1' to longArrayOf(0x00, 0x42, 0x7F, 0x40, 0x00),
        '2' to longArrayOf(0x42, 0x61, 0x51, 0x49, 0x46),
        '3' to longArrayOf(0x21, 0x41, 0x45, 0x4B, 0x31),
        '4' to longArrayOf(0x18, 0x14, 0x12, 0x7F, 0x10),
        '5' to longArrayOf(0x27, 0x45, 0x45, 0x45, 0x39),
        '6' to longArrayOf(0x3C, 0x4A, 0x49, 0x49, 0x30),
        '7' to longArrayOf(0x01, 0x71, 0x09, 0x05, 0x03),
        '8' to longArrayOf(0x36, 0x49, 0x49, 0x49, 0x36),
        '9' to longArrayOf(0x06, 0x49, 0x49, 0x29, 0x1E),
        '/' to longArrayOf(0x20, 0x10, 0x08, 0x04, 0x02),
        '-' to longArrayOf(0x08, 0x08, 0x08, 0x08, 0x08),
        '%' to longArrayOf(0x62, 0x64, 0x08, 0x13, 0x23),
        ' ' to longArrayOf(0x00, 0x00, 0x00, 0x00, 0x00)
    )
}

@Composable
fun BiocodeDotMatrixGlyph(
    char: Char,
    modifier: Modifier = Modifier,
    dotSize: Dp = 2.5.dp,
    dotSpacing: Dp = 1.dp,
    activeColor: Color = Color(0xFF4AE3B5),
    inactiveColor: Color = Color(0xFF0F362C)
) {
    val glyph = BiocodeLedFont.GLYPHS[char] ?: BiocodeLedFont.GLYPHS[' ']!!

    Canvas(
        modifier = modifier.size(
            width = dotSize * 5 + dotSpacing * 4,
            height = dotSize * 7 + dotSpacing * 6
        )
    ) {
        val dPx = dotSize.toPx()
        val sPx = dotSpacing.toPx()

        for (col in 0 until 5) {
            val colBits = glyph[col]
            for (row in 0 until 7) {
                val isLit = ((colBits shr row) and 1L) == 1L
                val x = col * (dPx + sPx)
                val y = row * (dPx + sPx)

                drawRoundRect(
                    color = if (isLit) activeColor else inactiveColor,
                    topLeft = Offset(x, y),
                    size = Size(dPx, dPx),
                    cornerRadius = CornerRadius(dPx * 0.35f, dPx * 0.35f)
                )
            }
        }
    }
}

@Composable
fun BiocodeDotMatrixText(
    text: String,
    modifier: Modifier = Modifier,
    dotSize: Dp = 2.5.dp,
    dotSpacing: Dp = 1.dp,
    charSpacing: Dp = 3.dp,
    activeColor: Color = Color(0xFF4AE3B5),
    inactiveColor: Color = Color(0xFF0F362C)
) {
    Row(modifier = modifier) {
        text.forEachIndexed { index, char ->
            BiocodeDotMatrixGlyph(
                char = char,
                dotSize = dotSize,
                dotSpacing = dotSpacing,
                activeColor = activeColor,
                inactiveColor = inactiveColor
            )
            if (index < text.length - 1) {
                Spacer(modifier = Modifier.width(charSpacing))
            }
        }
    }
}
