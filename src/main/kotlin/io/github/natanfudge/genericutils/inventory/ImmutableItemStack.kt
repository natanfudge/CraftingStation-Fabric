package io.github.natanfudge.genericutils.inventory

import io.github.natanfudge.injection.ImmutableItemStack
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.slot.Slot


fun ImmutableItemStack.canCombineWith(other: ImmutableItemStack) = ItemStack.canCombine(this as ItemStack, other as ItemStack)
fun ImmutableItemStack.asMutable() = this as ItemStack
fun ImmutableItemStack.copyToMutable() = this as ItemStack
fun ImmutableItemStack.withCount(count: Int) = if (this.count == count) this else copyWithCount(count)
fun ImmutableItemStack.copyWithCount(count: Int) = ItemStack(item, count).also { it.nbt = nbt }

// These functions are not expected to mutate the stack so it's acceptable to pass ImmutableItemStack into them
fun Slot.canInsert(stack: ImmutableItemStack) = canInsert(stack.asMutable())
fun ScreenHandler.canInsertIntoSlot(stack: ImmutableItemStack, slot: Slot) = canInsertIntoSlot(stack.asMutable(), slot)
fun Slot.getMaxItemCount(stack: ImmutableItemStack) = getMaxItemCount(stack.asMutable())
fun Slot.onQuickTransfer(newStack: ImmutableItemStack, oldStack: ImmutableItemStack): Unit = onQuickTransfer(newStack.asMutable(), oldStack.asMutable())
fun Slot.onTakeItem(player: PlayerEntity, stack: ImmutableItemStack): Unit = onTakeItem(player, stack.asMutable())