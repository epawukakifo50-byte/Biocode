package com.biocode.engine

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Клетка дискретной сетки Biocode.
 */
data class GridCell(val col: Int, val row: Int)

/**
 * Позиция угла для L-образных вырезов и стыковок.
 */
enum class CornerPosition {
    TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
}

/**
 * Спецификация глобальной сетки Biocode.
 */
data class BiocodeGridSpec(
    val columns: Int = 4,
    val rows: Int = 6,
    val gutter: Dp = 8.dp,
    val margin: Dp = 12.dp
) {
    /**
     * Вычисляет ширину одной ячейки исходя из доступной ширины контейнера.
     */
    fun calculateCellWidth(totalWidthDp: Dp): Dp {
        val availableWidth = totalWidthDp - (margin * 2) - (gutter * (columns - 1))
        return (availableWidth / columns).coerceAtLeast(1.dp)
    }

    /**
     * Вычисляет высоту ячейки исходя из доступной высоты или пропорции.
     */
    fun calculateCellHeight(totalHeightDp: Dp): Dp {
        val availableHeight = totalHeightDp - (margin * 2) - (gutter * (rows - 1))
        return (availableHeight / rows).coerceAtLeast(1.dp)
    }
}

/**
 * Модульный полимино-контейнер Biocode, состоящий из набора ячеек сетки.
 */
data class BiocodePolyomino(
    val id: String,
    val cells: Set<GridCell>
) {
    init {
        require(cells.isNotEmpty()) { "Полимино-модуль должен содержать как минимум одну ячейку!" }
    }

    val minCol: Int = cells.minOf { it.col }
    val maxCol: Int = cells.maxOf { it.col }
    val minRow: Int = cells.minOf { it.row }
    val maxRow: Int = cells.maxOf { it.row }

    val widthInCells: Int = maxCol - minCol + 1
    val heightInCells: Int = maxRow - minRow + 1

    companion object {
        /**
         * Прямоугольный контейнер (NxM ячеек)
         */
        fun rect(id: String, col: Int, row: Int, width: Int, height: Int): BiocodePolyomino {
            val cellSet = mutableSetOf<GridCell>()
            for (c in col until col + width) {
                for (r in row until row + height) {
                    cellSet.add(GridCell(c, r))
                }
            }
            return BiocodePolyomino(id, cellSet)
        }

        /**
         * L-образный контейнер с вырезом под соседний виджет (как на референсах 3 и 4).
         * @param notchCorner в каком углу находится вырез
         * @param notchWidth ширина выреза в ячейках
         * @param notchHeight высота выреза в ячейках
         */
        fun lShape(
            id: String,
            col: Int,
            row: Int,
            width: Int,
            height: Int,
            notchWidth: Int = 1,
            notchHeight: Int = 1,
            notchCorner: CornerPosition = CornerPosition.BOTTOM_RIGHT
        ): BiocodePolyomino {
            val cellSet = mutableSetOf<GridCell>()
            for (c in col until col + width) {
                for (r in row until row + height) {
                    val inNotch = when (notchCorner) {
                        CornerPosition.TOP_LEFT -> c < col + notchWidth && r < row + notchHeight
                        CornerPosition.TOP_RIGHT -> c >= col + width - notchWidth && r < row + notchHeight
                        CornerPosition.BOTTOM_LEFT -> c < col + notchWidth && r >= row + height - notchHeight
                        CornerPosition.BOTTOM_RIGHT -> c >= col + width - notchWidth && r >= row + height - notchHeight
                    }
                    if (!inNotch) {
                        cellSet.add(GridCell(c, r))
                    }
                }
            }
            return BiocodePolyomino(id, cellSet)
        }

        /**
         * U-образный контейнер (огибающая рамка вокруг центрального блока).
         */
        fun uShape(
            id: String,
            col: Int,
            row: Int,
            width: Int,
            height: Int,
            openSide: CornerPosition = CornerPosition.BOTTOM_LEFT
        ): BiocodePolyomino {
            val cellSet = mutableSetOf<GridCell>()
            for (c in col until col + width) {
                for (r in row until row + height) {
                    val isCore = (c in (col + 1) until (col + width - 1)) &&
                            (r in (row + 1) until (row + height))
                    if (!isCore) {
                        cellSet.add(GridCell(c, r))
                    }
                }
            }
            return BiocodePolyomino(id, cellSet)
        }
    }
}
