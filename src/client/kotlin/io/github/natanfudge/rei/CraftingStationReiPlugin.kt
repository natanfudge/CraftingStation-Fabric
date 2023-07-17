package io.github.natanfudge.rei

import io.github.natanfudge.CraftingStationBlock
import me.shedaniel.rei.api.client.plugins.REIClientPlugin
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry
import me.shedaniel.rei.api.client.registry.transfer.TransferHandler
import me.shedaniel.rei.api.client.registry.transfer.TransferHandlerRegistry
import me.shedaniel.rei.api.common.util.EntryStacks
import me.shedaniel.rei.plugin.common.BuiltinPlugin

class CraftingStationReiPlugin : REIClientPlugin {
    override fun registerTransferHandlers(registry: TransferHandlerRegistry) {
        registry.register(CraftingStationTransferHandler)
    }

    override fun registerCategories(registry: CategoryRegistry) {
        registry.addWorkstations(BuiltinPlugin.CRAFTING, EntryStacks.of(CraftingStationBlock.item))
    }
}

object CraftingStationTransferHandler : TransferHandler {
    override fun handle(context: TransferHandler.Context): TransferHandler.Result {
        //TODO: fuck REI. make a JEI plugin instead.
//        return TransferHandler.Result.
//        return TransferHandler.Result.createNotApplicable()
//        TODO("Not yet implemented")
    }

}