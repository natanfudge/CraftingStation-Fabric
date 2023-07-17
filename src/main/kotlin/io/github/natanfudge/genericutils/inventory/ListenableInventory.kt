package io.github.natanfudge.genericutils.inventory

import io.github.natanfudge.genericutils.forEachNonEmptyStackIndexed
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventories
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtList
import net.minecraft.util.collection.DefaultedList


/**
 * Use [FixedSlotInventory] to implement this (more implementations to come)
 */
interface ListenableInventory : Inventory {
    fun onChange(listener: () -> Unit)
}


/**
 * Easy ListenableInventory implementation
 */
class FixedSlotInventory(slots: Int) : ListenableInventory {
    private val inventory = DefaultedList.ofSize(slots, ItemStack.EMPTY)

    // Providing it as a constructor parameter is not useful since the target BlockEntity is not available when the inventory is created
    private var onChange: MutableList<() -> Unit> = mutableListOf()

    override fun onChange(listener: () -> Unit) {
        onChange.add(listener)
    }

    private fun notifyListeners() = onChange.forEach {it()}

    private inline fun trackRemovalChange(changingCode: () -> ItemStack): ItemStack {
        val removed = changingCode()
        if (!removed.isEmpty) notifyListeners()
        return removed
    }

    override fun clear() {
        val sizeBefore = inventory.size
        inventory.clear()
        if (sizeBefore > 0) notifyListeners()
    }

    override fun size(): Int {
        return inventory.size
    }

    override fun isEmpty(): Boolean {
        for (i in 0 until size()) {
            val stack = getStack(i)
            if (!stack.isEmpty) {
                return false
            }
        }
        return true
    }

    override fun getStack(slot: Int): ItemStack {
        return inventory[slot]
    }

    override fun removeStack(slot: Int, amount: Int): ItemStack = trackRemovalChange {
         Inventories.splitStack(inventory, slot, amount)
    }

    override fun removeStack(slot: Int): ItemStack = trackRemovalChange {
         Inventories.removeStack(inventory, slot);
    }

    override fun setStack(slot: Int, stack: ItemStack) {
        val old = inventory[slot]
        inventory[slot] = stack;
        if (stack.count > stack.maxCount) {
            stack.count = stack.maxCount;
        }
        if (!ItemStack.areEqual(old, stack)) notifyListeners()
    }

    override fun markDirty() {
    }

    override fun canPlayerUse(player: PlayerEntity?): Boolean {
        return true
    }

}

private const val ItemStackListSlotKey = "Slot"
private const val ItemStackListKey = "Items"

fun Inventory.writeItemsNbt(nbt: NbtCompound) {
    val nbtList = NbtList()
    forEachNonEmptyStackIndexed { i, stack ->
        val compound = NbtCompound()
        compound.putByte(ItemStackListSlotKey, i.toByte())
        stack.writeNbt(compound)
        nbtList.add(compound)
    }
    nbt.put(ItemStackListKey, nbtList)
}

fun Inventory.readItemsNbt(nbt: NbtCompound) {
    val nbtList = nbt.getList(ItemStackListKey, NbtElement.COMPOUND_TYPE.toInt())

    for (i in nbtList.indices) {
        val nbtCompound = nbtList.getCompound(i)
        val j = nbtCompound.getByte(ItemStackListSlotKey).toInt() and 255
        if (j >= 0 && j < size()) {
            setStack(j, ItemStack.fromNbt(nbtCompound))
        }
    }
}
