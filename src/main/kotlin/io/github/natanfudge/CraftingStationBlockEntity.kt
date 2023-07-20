package io.github.natanfudge

import io.github.natanfudge.genericutils.inventory.FixedSlotInventory
import io.github.natanfudge.genericutils.inventory.ListenableInventory
import io.github.natanfudge.genericutils.inventory.asListenable
import io.github.natanfudge.genericutils.superclasses.KBlockEntity
import io.github.natanfudge.genericutils.superclasses.KBlockEntityType
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory
import net.minecraft.block.BlockState
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.CraftingResultInventory
import net.minecraft.inventory.Inventory
import net.minecraft.network.PacketByteBuf
import net.minecraft.screen.ScreenHandler
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos


class CraftingStationBlockEntity(
    pos: BlockPos,
    state: BlockState,
    val inventory: ListenableInventory = FixedSlotInventory(9).asListenable(),
    val resultInventory: CraftingResultInventory = CraftingResultInventory(),
) :
    KBlockEntity(Type, pos, state, inventory = inventory), Inventory by inventory,
    ExtendedScreenHandlerFactory {


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
