package io.github.natanfudge.genericutils.inventory

import net.minecraft.inventory.Inventory
import net.minecraft.inventory.SidedInventory
import net.minecraft.item.ItemStack
import net.minecraft.util.math.Direction



fun Inventory.withSides(inputs: SideMap, outputs: SideMap): SidedInventory {
    return SidedInventoryImpl(this, inputs, outputs)
}

private class SidedInventoryImpl(private val inventory: Inventory, private val inputs: SideMap, private val outputs: SideMap) : SidedInventory,
    Inventory by inventory {
    private val combinedSides = Direction.entries.associateWith { (inputs.getValue(it) + outputs.getValue(it)).toIntArray() }
    override fun getAvailableSlots(side: Direction): IntArray {
        return combinedSides.getValue(side)
    }

    override fun canInsert(slot: Int, stack: ItemStack, dir: Direction?): Boolean {
        if (dir == null) return false
        return inputs.getValue(dir).contains(slot)
    }

    override fun canExtract(slot: Int, stack: ItemStack, dir: Direction?): Boolean {
        if (dir == null) return false
        return outputs.getValue(dir).contains(slot)
    }

}

typealias SideMap = Map<Direction, Set<Int>>

