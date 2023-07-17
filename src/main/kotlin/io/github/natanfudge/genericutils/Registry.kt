package io.github.natanfudge.genericutils

import io.github.natanfudge.genericutils.superclasses.KBlock
import io.github.natanfudge.genericutils.superclasses.KBlockEntityProvider
import net.minecraft.item.BlockItem
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.util.registry.Registry

/**
 * Will register the block and associated BlockItem and BlockEntityType with the KBlock's ID
 */
context(CommonInit)
fun register(block: KBlock) = with(block) {
    Registry.register(Registry.BLOCK, id, this)
    if (itemSettings != null) Registry.register(Registry.ITEM, id, block.item)
    if (this is KBlockEntityProvider) Registry.register(Registry.BLOCK_ENTITY_TYPE, id, blockEntityType)
}
context(CommonInit)
fun register(type: ScreenHandlerType<*>, idPath: String) {
    Registry.register(Registry.SCREEN_HANDLER, csId(idPath), type)
}

