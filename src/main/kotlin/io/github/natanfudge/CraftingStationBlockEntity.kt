package io.github.natanfudge

import io.github.natanfudge.genericutils.superclasses.KBlockEntity
import io.github.natanfudge.genericutils.superclasses.KBlockEntityType
import io.github.natanfudge.genericutils.inventory.FixedSlotInventory
import io.github.natanfudge.genericutils.inventory.ListenableInventory
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory
import net.minecraft.block.BlockState
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.network.PacketByteBuf
import net.minecraft.screen.ScreenHandler
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos

//TODO: make I/O better - sided insertion and extraction of recipe output


class CraftingStationBlockEntity(pos: BlockPos, state: BlockState, val inventory: ListenableInventory = FixedSlotInventory(9)) :
    KBlockEntity(Type, pos, state, inventory = inventory), ListenableInventory by inventory,
    ExtendedScreenHandlerFactory {
//    private var currentContainer = 0
//
//    val data = object : PropertyDelegate {
//        override fun get(index: Int): Int {
//            return currentContainer
//        }
//
//        override fun set(index: Int, value: Int) {
//            currentContainer = value
//        }
//
//        override fun size(): Int {
//            return 1
//        }
//
//    }

    companion object {
        val Type = KBlockEntityType.ofBlock({ CraftingStationBlock }, ::CraftingStationBlockEntity, clientRequiresNbt = true)
    }

    override fun createMenu(syncId: Int, inv: PlayerInventory, player: PlayerEntity): ScreenHandler {
        return CraftingStationScreenHandler(syncId, inv, pos/*, data*/)
    }

    override fun getDisplayName(): Text {
        return Text.translatable(cachedState.block.translationKey);
    }

    override fun writeScreenOpeningData(player: ServerPlayerEntity, buf: PacketByteBuf) {
        buf.writeBlockPos(pos)
    }

    override fun markDirty() {
        super.markDirty()
    }
}
