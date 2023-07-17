package io.github.natanfudge.genericutils.superclasses

import net.minecraft.block.BlockEntityProvider
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos

/**
 * Can be implemented by delegating to a KBlockEntityType
 */
interface KBlockEntityProvider : BlockEntityProvider {
    val blockEntityType: BlockEntityType<*>
}
