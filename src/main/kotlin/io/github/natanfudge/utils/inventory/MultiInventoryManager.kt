package io.github.natanfudge.utils.inventory

import io.github.natanfudge.genericutils.MinecraftConstants
import net.minecraft.block.BlockState
import net.minecraft.inventory.Inventory
import net.minecraft.text.Text
import kotlin.math.ceil


class MultiInventoryManager(
    val inventories: List<ManagedInventory>,
) {
    private var _selected = 0

    val selectedIndex get() = _selected

    val selected: ManagedInventory? get() = inventories.getOrNull(selectedIndex)


    val isEmpty = inventories.isEmpty()
    val slots = inventories.flatMap { it.slots }

    fun scroll(offset: Int) {
        val inventory = inventories[_selected];
        inventory.slots.forEachIndexed { i, slot ->
            val newRow = i / inventory.columns - offset
            // If the new row of the slot is not in the visible range of the inventory, hide it
            if (newRow < 0 || newRow >= inventory.maxVisibleRows) slot.hide()
            else slot.show(y = inventory.cellsYStart + MinecraftConstants.InventoryCellSize * newRow)
        }
    }

    fun selectedInventoryName(): Text {
        return inventories[_selected].name
    }


    fun select(newInventoryIndex: Int) {
        if (newInventoryIndex == _selected || newInventoryIndex >= inventories.size) return
        val oldInventory = inventories[_selected]
        for (slot in oldInventory.slots) {
            hideSlot(slot)
        }

        _selected = newInventoryIndex
        scroll(0)
    }

    private fun hideSlot(slot: HideableSlot) {
        slot.hide()
    }

}

data class ManagedInventory(
    val name: Text,
    val inventory: Inventory,
    val holdingBlock: BlockState,
    val slots: List<HideableSlot>,
    val columns: Int,
    val maxVisibleRows: Int,
    val cellsYStart: Int
) {
    val rows = ceil(slots.size.toDouble() / columns).toInt()
}
