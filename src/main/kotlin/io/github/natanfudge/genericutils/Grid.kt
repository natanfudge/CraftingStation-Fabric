package io.github.natanfudge.genericutils

import net.minecraft.screen.slot.Slot
import kotlin.math.ceil

typealias SlotMapping<T> = (x: Int, y: Int, i: Int) -> T
class GridIndices(val size: Int, val columns: Int) {
    val rows: Int = ceil(size / columns.toFloat()).toInt()
    inline fun forEachCell(iterator: (x: Int, y: Int, i: Int) -> Unit) {
        repeat(size) {
            val row = it / columns
            val column = it % columns
            iterator(column, row, it)
        }
    }
    inline fun forEachCellOfSize(size: Double, iterator: (x: Double, y: Double, i: Int) -> Unit) {
        repeat(this.size) {
            val row = it / columns
            val column = it % columns
            iterator(column * size, row * size, it)
        }
    }

    fun <T> mapWithCellSize(height: Int, width: Int, map: SlotMapping<T>): List<T> = buildList {
        forEachCell { x, y, i ->
            add(map(x * width, y * height, i))
        }
    }
    fun <T> mapWithCellSize(size: Int, map: SlotMapping<T>): List<T> = mapWithCellSize(size,size,map)
    fun <T : Slot> minecraftCells(map: SlotMapping<T>): List<T> = mapWithCellSize(size = MinecraftConstants.InventoryCellSize, map)
}

fun fullGrid(rows: Int, columns: Int) = GridIndices(rows * columns, columns)
fun squareGrid(size: Int) = fullGrid(size, size)
fun grid(size: Int, columns: Int) = GridIndices(size, columns)
