package io.github.natanfudge.genericutils.inventory

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack

class CombinedInventory(private val inv1: Inventory, private val inv2: Inventory) : Inventory {
    private val inv2Start = inv1.size()
    override fun clear() {
        inv1.clear()
        inv2.clear()
    }

    override fun size(): Int = inv1.size() + inv2.size()

    override fun isEmpty(): Boolean {
        return inv1.isEmpty && inv2.isEmpty
    }

    override fun getStack(slot: Int): ItemStack {
        if (slot < inv2Start) return inv1.getStack(slot)
        else return inv2.getStack(slot - inv2Start)
    }

    override fun removeStack(slot: Int, amount: Int): ItemStack {
        if (slot < inv2Start) return inv1.removeStack(slot, amount)
        else return inv2.removeStack(slot - inv2Start, amount)
    }

    override fun removeStack(slot: Int): ItemStack {
        if (slot < inv2Start) return inv1.removeStack(slot)
        else return inv2.removeStack(slot - inv2Start)
    }

    override fun setStack(slot: Int, stack: ItemStack) {
        if (slot < inv2Start) return inv1.setStack(slot, stack)
        else return inv2.setStack(slot - inv2Start, stack)
    }

    override fun markDirty() {
        inv1.markDirty()
        inv2.markDirty()
    }

    override fun canPlayerUse(player: PlayerEntity): Boolean {
        return inv1.canPlayerUse(player) && inv2.canPlayerUse(player)
    }
}