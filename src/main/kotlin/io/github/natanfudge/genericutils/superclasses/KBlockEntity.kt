package io.github.natanfudge.genericutils.superclasses

import io.github.natanfudge.genericutils.inventory.ListenableInventory
import io.github.natanfudge.genericutils.inventory.readItemsNbt
import io.github.natanfudge.genericutils.inventory.writeItemsNbt
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.Packet
import net.minecraft.network.listener.ClientPlayPacketListener
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket
import net.minecraft.util.math.BlockPos

abstract class KBlockEntity(
    private val type: KBlockEntityType<*>, pos: BlockPos, state: BlockState,
    /** KBlockEntity will save the inventory for you */
    private val inventory: ListenableInventory? = null,
) : BlockEntity(type, pos, state) {

    // clientRequiresNbt implementation

    override fun toInitialChunkDataNbt(): NbtCompound {
        return if (type.clientRequiresNbt) createNbt() else super.toInitialChunkDataNbt()
    }

    override fun toUpdatePacket(): Packet<ClientPlayPacketListener>? {
        return if(type.clientRequiresNbt) return BlockEntityUpdateS2CPacket.create(this) else super.toUpdatePacket()
    }


    ///// Inventory implementation
    init {
        inventory?.onChange { markDirty() }
    }

    override fun writeNbt(nbt: NbtCompound) {
        inventory?.writeItemsNbt(nbt)
        super.writeNbt(nbt)
    }

    override fun readNbt(nbt: NbtCompound) {
        inventory?.readItemsNbt(nbt)
        super.readNbt(nbt)
    }



}