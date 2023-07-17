package io.github.natanfudge.genericutils.superclasses

import io.github.natanfudge.genericutils.inventory.copyToMutable
import io.github.natanfudge.genericutils.inventory.onQuickTransfer
import io.github.natanfudge.genericutils.inventory.onTakeItem
import io.github.natanfudge.injection.ImmutableItemStack
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.screen.PropertyDelegate
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerType

abstract class KScreenHandler(type: ScreenHandlerType<*>, syncId: Int, properties: PropertyDelegate? = null) : ScreenHandler(type, syncId) {
    init {
        if (properties != null) addProperties(properties)
    }

    // Make intelliJ shut up about calling non-final method
    final override fun addProperties(propertyDelegate: PropertyDelegate) {
        super.addProperties(propertyDelegate)
    }

    final override fun insertItem(stack: ItemStack, startIndex: Int, endIndex: Int, fromLast: Boolean): Boolean {
        val newStack = insertItemImmutable(stack, startIndex, endIndex, fromLast)
        val changed = newStack.count != stack.count
        stack.count = newStack.count
        return changed
    }

    /**
     * Transforms [stack] by inserting to slots from [startIndex] to [endIndex] - 1 (both inclusive) until the entire stack is used.
     *
     * <p>If [fromLast] is true, this attempts the insertion in reverse
     * order; i.e. [endIndex]- 1 to [startIndex] (both inclusive).
     *
     * @return the new [stack]
     */
    abstract fun insertItemImmutable(stack: ImmutableItemStack, startIndex: Int, endIndex: Int, fromLast: Boolean): ImmutableItemStack
    final override fun transferSlot(player: PlayerEntity, index: Int): ItemStack {
        val slot = slots[index]
        val oldStack = slot.stack
        val newStack = transferSlotImmutable(player, index, oldStack)
        if (oldStack.count != newStack.count) {
            slot.onQuickTransfer(newStack, oldStack)
            slot.onTakeItem(player, newStack)
            slot.stack = newStack.copyToMutable()
            return oldStack
        }

        return ItemStack.EMPTY
    }

    /**
     * Transfers (or "quick-moves") the stack [slotStack] at slot [index] to other
     * slots of the screen handler that belong to a different inventory.
     *
     * @return the transformed stack after it has been transferred
     *
     * @see insertItemImmutable
     */
    abstract fun transferSlotImmutable(player: PlayerEntity, index: Int, slotStack: ImmutableItemStack): ImmutableItemStack
}