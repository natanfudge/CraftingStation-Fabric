package io.github.natanfudge.utils.inventory

import io.github.natanfudge.genericutils.inventory.*
import io.github.natanfudge.genericutils.superclasses.KRecipeScreenHandler
import io.github.natanfudge.genericutils.superclasses.KScreenHandler
import io.github.natanfudge.injection.ImmutableItemStack
import net.minecraft.inventory.Inventory
import net.minecraft.screen.slot.Slot
import kotlin.math.min

class CombinedSlotsBuilder<I: Inventory>(private val handler: KRecipeScreenHandler<I>) {
    private val partMap = mutableMapOf<InventoryPart, List<InventoryPart>>()
    fun parts(vararg slots: List<Slot>): List<InventoryPart> {
        return slots.map { InventoryPart(it) }
    }

    fun InventoryPart.movesTo(vararg other: InventoryPart) {
        partMap[this] = other.toList()
    }

    fun build(): CombinedSlots<I> {
        return CombinedSlots(handler, partMap.keys.toList(), partMap)
    }
}

class CombinedSlots<I : Inventory> internal constructor(
    private val handler: KRecipeScreenHandler<I>, @PublishedApi internal val parts: List<InventoryPart>,
    /**
     * When shift-clicking on an item in an inventory part, it will only move to values specified as the value,
     * and in the order specified in the list.
     */
    private val allowedMovements: Map<InventoryPart, List<InventoryPart>>
) {
    companion object {
        fun<I: Inventory> of(handler: KRecipeScreenHandler<I>, builder: CombinedSlotsBuilder<I>.() -> Unit) = CombinedSlotsBuilder(handler).apply(builder).build()
    }

    val size = parts.sumOf { it.slots.size }

    private fun getInventoryPart(index: Int): InventoryPart {
        var passedInventoriesSizeSum = 0
        for (part in parts) {
            val partSize = part.slots.size
            if (index < passedInventoriesSizeSum + partSize) {
                // If index is within the bounds of this part
                return part
            } else {
                passedInventoriesSizeSum += partSize
            }
        }
        throw IndexOutOfBoundsException("Index $index is out of bounds of the combined inventory size of ${parts.sumOf { it.slots.size }}")
    }

    inline fun forEachSlot(action: (Slot, i: Int) -> Unit) {
        var i = 0
        for (part in parts) {
            for (slot in part.slots) action(slot, i++)
        }
    }


    /**
     * Returns how many of [itemStack] are left.
     */
    fun insert(itemStack: ImmutableItemStack, startIndex: Int, endIndex: Int): Int {
        return mergeAndThenSpreadStacks(stackInventoryPart = null, stack = itemStack, startIndex = startIndex, endIndex = endIndex)
    }

    /**
     * Returns how many of [slotStack] are left.
     */
    fun quickTransfer(index: Int, slotStack: ImmutableItemStack): Int {
        val inventoryPart = getInventoryPart(index)
        return mergeAndThenSpreadStacks(inventoryPart, slotStack, startIndex = 0, endIndex = size - 1)
    }

    private fun mergeAndThenSpreadStacks(
        stackInventoryPart: InventoryPart?,
        stack: ImmutableItemStack,
        startIndex: Int,
        endIndex: Int
    ): Int {
        // Prioritize merging over moving into an empty slot
        val afterMerge = spreadStacks(stackInventoryPart, stack, amountLeft = stack.count, mergeOnly = true, startIndex = startIndex, endIndex = endIndex)
        return spreadStacks(stackInventoryPart, stack, amountLeft = afterMerge, mergeOnly = false, startIndex = startIndex, endIndex = endIndex)
    }

    private fun spreadStacks(
        stackInventoryPart: InventoryPart?,
        stackToSpread: ImmutableItemStack,
        amountLeft: Int,
        mergeOnly: Boolean,
        startIndex: Int,
        endIndex: Int
    ): Int {
        var remainingCount = amountLeft
        forEachSlotPartMovesTo(stackInventoryPart) { slot, i ->
            remainingCount -= trySlot(stackToSpread, remainingCount, slot, i, startIndex, endIndex, mergeOnly)
            if (remainingCount == 0) return 0
        }
        return remainingCount
    }

    private inline fun forEachSlotPartMovesTo(part: InventoryPart?, action: (Slot, i: Int) -> Unit) {
        if (part == null) forEachSlot(action)
        else {
            var i = 0
            for (movesToPart in allowedMovements.getValue(part)) {
                for (slot in movesToPart.slots) action(slot, i++)
            }
        }
    }

    /**
     * Returns the amount taken from the slot
     */
    private fun trySlot(
        stackToSpread: ImmutableItemStack,
        remainingCount: Int,
        slot: Slot,
        i: Int,
        startIndex: Int,
        endIndex: Int,
        mergeOnly: Boolean
    ): Int {
        // Respect startIndex and endIndex
        if (i < startIndex || i > endIndex) return 0

        val merging = slot.hasStack()
        val slotStack = slot.stack

        // If stack is empty and we only want to merge , ignore slot
        if (mergeOnly && !merging) return 0
        if (merging && !slotStack.canCombineWith(stackToSpread)) return 0

        // Check slot and handler allow this transfer
        if (!slot.canInsert(stackToSpread) || !handler.canInsertIntoSlot(stackToSpread, slot)) return 0

        val totalCount = remainingCount + slotStack.count
        val limitInSlot = slot.getMaxItemCount(stackToSpread)
        val previousAmountInSlot = slotStack.count
        val resultAmountInSlot = min(limitInSlot, totalCount)
        val amountMoved = resultAmountInSlot - previousAmountInSlot

        val slotChanged = resultAmountInSlot != slotStack.count
        if (previousAmountInSlot == 0) {
            // Nothing in the slot - put in a new stack
            slot.stack = stackToSpread.copyWithCount(resultAmountInSlot)
        } else {
            // There is already a stack in the slot - increase its count
            slotStack.count = resultAmountInSlot
        }
        if (slotChanged) slot.markDirty()

        return amountMoved
    }

}

class InventoryPart(val slots: List<Slot>)
