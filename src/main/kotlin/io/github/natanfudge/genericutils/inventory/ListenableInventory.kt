package io.github.natanfudge.genericutils.inventory

import net.minecraft.inventory.Inventory
import net.minecraft.inventory.SidedInventory
import net.minecraft.item.ItemStack

/**
 * This should only be used when the inventory is created. You can't just copyToListenable() wherever you want and expect listening to changes to work.
 */
fun Inventory.asListenable(): ListenableInventory = ListenableInventoryImpl(this)
fun SidedInventory.asListenable(): ListenableSidedInventory = ListenableSidedInventoryImpl(this)


class ListenableSidedInventoryImpl(private val inventory: SidedInventory, private val listenable: Listenable = inventory.asListenable()) :
    ListenableSidedInventory, SidedInventory by inventory, Listenable by listenable

interface ListenableInventory : Inventory, Listenable
interface ListenableSidedInventory : SidedInventory, ListenableInventory

interface Listenable {
    fun onChange(listenerId: String, listener: () -> Unit): SubscriptionHandle
    fun unsubscribe(listenerId: String)
}

/**
 * Use [FixedSlotInventory] to implement this (more implementations to come)
 */
class ListenableInventoryImpl(private val inventory: Inventory) : Inventory by inventory, ListenableInventory {
    // Providing it as a constructor parameter is not useful since the target BlockEntity is not available when the inventory is created
    private var onChange: MutableMap<String, () -> Unit> = mutableMapOf()

    /**
     * Returns a SubscriptionHandle that must call unsubscribe() when this onChange callback is no longer required.
     * You may also opt to not use the SubscriptionHandle and just call unsubscribe() with the same ID.
     */
    override fun onChange(listenerId: String, listener: () -> Unit): SubscriptionHandle {
        onChange[listenerId] = listener
        return SubscriptionHandle(this, listenerId)
    }

    override fun unsubscribe(listenerId: String) {
        onChange.remove(listenerId)
    }

    private fun notifyListeners() = onChange.forEach { it.value() }

    private inline fun trackRemovalChange(changingCode: () -> ItemStack): ItemStack {
        val removed = changingCode()
        if (!removed.isEmpty) notifyListeners()
        return removed
    }

    override fun clear() {
        val sizeBefore = size()
        inventory.clear()
        if (sizeBefore > 0) notifyListeners()
    }

    override fun removeStack(slot: Int, amount: Int): ItemStack = trackRemovalChange {
        inventory.removeStack(slot, amount)
    }

    override fun removeStack(slot: Int): ItemStack = trackRemovalChange {
        inventory.removeStack(slot)
    }

    override fun setStack(slot: Int, stack: ItemStack) {
        val old = inventory.getStack(slot)
        inventory.setStack(slot, stack)
        if (!ItemStack.areEqual(old, stack)) notifyListeners()
    }
}


class SubscriptionHandle(private val inventory: Listenable, private val id: String) {
    fun unsubscribe() = inventory.unsubscribe(id)
}


