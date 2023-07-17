package io.github.natanfudge.genericutils

import io.github.natanfudge.CraftingStation
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.entity.HopperBlockEntity
import net.minecraft.inventory.CraftingResultInventory
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Identifier
import net.minecraft.util.ItemScatterer
import net.minecraft.util.math.BlockPos
import net.minecraft.util.shape.VoxelShape
import net.minecraft.world.World


fun csId(path: String) = Identifier(CraftingStation.ModId, path)

object Voxels {
    fun createBlockCuboid(minX: Int, minY: Int, minZ: Int, maxX: Int, maxY: Int, maxZ: Int): VoxelShape {
        return Block.createCuboidShape(minX.toDouble(), minY.toDouble(), minZ.toDouble(), maxX.toDouble(), maxY.toDouble(), maxZ.toDouble())
    }
}

val World.isServer get() = !isClient

inline fun <reified T> World.whenBlockEntityIs(pos: BlockPos, action: (T) -> Unit) {
    val entity = getBlockEntity(pos)
    if (entity is T) action(entity)
}

inline fun <reified T> World.getSpecificBlockEntity(pos: BlockPos): T = getBlockEntity(pos) as T


inline fun Inventory.forEachNonEmptyStack(action: (ItemStack) -> Unit) = forEachNonEmptyStackIndexed { _, stack -> action(stack) }
inline fun Inventory.forEachNonEmptyStackIndexed(action: (Int, ItemStack) -> Unit) {
    for (i in 0 until size()) {
        val stack = getStack(i)
        if (!stack.isEmpty) action(i, stack)
    }
}

fun World.scatterItemStack(stack: ItemStack, pos: BlockPos) = ItemScatterer.spawn(
    this,
    pos.x.toDouble(),
    pos.y.toDouble(),
    pos.z.toDouble(),
    stack
)

// HopperBlockEntity has a complete implementation of getting the correct inventory object at a position
fun World.getCompleteInventoryAt(pos: BlockPos) = HopperBlockEntity.getInventoryAt(this, pos)

fun BlockState.toItemStack() = ItemStack(block)

fun CraftingResultInventory.setStack(stack: ItemStack) = setStack(0, stack)

inline fun World.inServer(action: context(ServerWorld)() -> Unit) {
    if(isServer) action(this as ServerWorld)
}

fun String.truncate(truncate: Int) = if (length > truncate) {
    substring(0, truncate) + "..."
} else this

inline fun Int.inBounds(start: Int, end: Int) = this >= start && this <= end
inline fun Double.inBounds(start: Int, end: Int) = this >= start && this <= end
inline fun Double.inBoundsExclusive(start: Int, end: Int) = this > start && this < end

