package io.github.natanfudge.genericutils

import io.github.natanfudge.genericutils.inventory.ListenableInventory
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.CraftingInventory
import net.minecraft.item.ItemStack
import net.minecraft.screen.ScreenHandler

// TODO: test this mod in multiplayer:
//  1. SyncRecipe packet
//  2. Recipe updating when another player has station open
//  3. Inventory updating when another player has station open
//  4. Being able to switch between different adjacent inventories together with another player


/**Usually, CraftingInventory's only emulate an inventory for the purpose of GUIs. Here, we want the  CraftingInventory to store its stuff in a real Inventory.**/
class InventoryBackedCraftingInventory(private val eventHandler: ScreenHandler, private val inv: ListenableInventory) :
    CraftingInventory(eventHandler, 3, 3) {

    init {
        inv.onChange { eventHandler.onContentChanged(this); }
    }


    // We explicitly implement everything because delegation doesn't work well here
    override fun getStack(slot: Int): ItemStack = inv.getStack(slot)

    override fun removeStack(slot: Int, count: Int): ItemStack = inv.removeStack(slot, count)

    override fun setStack(slot: Int, stack: ItemStack) = inv.setStack(slot, stack)

    override fun removeStack(index: Int): ItemStack = inv.removeStack(index)

    override fun isEmpty(): Boolean = inv.isEmpty

    override fun clear() = inv.clear()

    override fun size(): Int = inv.size()

    override fun canPlayerUse(player: PlayerEntity): Boolean = inv.canPlayerUse(player)

    override fun markDirty() = inv.markDirty()
}