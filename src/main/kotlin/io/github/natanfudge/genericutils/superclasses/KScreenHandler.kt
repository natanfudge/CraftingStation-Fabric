package io.github.natanfudge.genericutils.superclasses

import io.github.natanfudge.genericutils.inventory.copyWithCount
import io.github.natanfudge.injection.ImmutableItemStack
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.screen.AbstractRecipeScreenHandler
import net.minecraft.screen.PropertyDelegate
import net.minecraft.screen.ScreenHandlerType

typealias KScreenHandler = KRecipeScreenHandler<Inventory>

/**
 * We have a problem with separating RecipeScreenHandler vs ScreenHandler. Hopefully we don't need ScreenHandler directly.
 */
abstract class KRecipeScreenHandler<in I : Inventory>(type: ScreenHandlerType<*>, syncId: Int, properties: PropertyDelegate? = null) :
    AbstractRecipeScreenHandler<@UnsafeVariance I>(type, syncId) {
    init {
        if (properties != null) addProperties(properties)
    }

    // Make intelliJ shut up about calling non-final method
    final override fun addProperties(propertyDelegate: PropertyDelegate) {
        super.addProperties(propertyDelegate)
    }

    final override fun insertItem(stack: ItemStack, startIndex: Int, endIndex: Int, fromLast: Boolean): Boolean {
        val newStackCount = insertItemImmutable(stack, startIndex, endIndex, fromLast)
        val changed = newStackCount != stack.count
        stack.count = newStackCount
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
    abstract fun insertItemImmutable(stack: ImmutableItemStack, startIndex: Int, endIndex: Int, fromLast: Boolean): Int
    final override fun transferSlot(player: PlayerEntity, index: Int): ItemStack {
        val slot = slots[index]
        val oldStack = slot.stack
        val newStackCount = transferSlotImmutable(player, index, oldStack)
        if (oldStack.count != newStackCount) {
            val newItem = oldStack.copyWithCount(newStackCount)
            slot.onQuickTransfer(newItem, oldStack)
            slot.onTakeItem(player, newItem)
            slot.stack = newItem

            onContentChanged(null)

            return oldStack
        }

        return ItemStack.EMPTY
    }

    // Assert that inventory is nullable
    override fun onContentChanged(inventory: Inventory?) {
        super.onContentChanged(inventory)
    }

    /**
     * Transfers (or "quick-moves") the stack [slotStack] at slot [index] to other
     * slots of the screen handler that belong to a different inventory.
     *
     * @return the transformed stack after it has been transferred
     *
     * @see insertItemImmutable
     */
    abstract fun transferSlotImmutable(player: PlayerEntity, index: Int, slotStack: ImmutableItemStack): Int
}