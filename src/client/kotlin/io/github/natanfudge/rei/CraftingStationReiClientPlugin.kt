package io.github.natanfudge.rei

import io.github.natanfudge.CraftingStationBlock
import io.github.natanfudge.CraftingStationScreen
import me.shedaniel.math.Rectangle
import me.shedaniel.rei.api.client.ClientHelper
import me.shedaniel.rei.api.client.plugins.REIClientPlugin
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry
import me.shedaniel.rei.api.client.registry.transfer.TransferHandler
import me.shedaniel.rei.api.client.registry.transfer.TransferHandlerRegistry
import me.shedaniel.rei.api.common.display.SimpleGridMenuDisplay
import me.shedaniel.rei.api.common.util.EntryStacks
import me.shedaniel.rei.plugin.common.BuiltinPlugin
import me.shedaniel.rei.plugin.common.displays.crafting.DefaultShapedDisplay
import net.minecraft.recipe.ShapedRecipe

class CraftingStationReiClientPlugin : REIClientPlugin {
    override fun registerCategories(registry: CategoryRegistry) {
        registry.addWorkstations(BuiltinPlugin.CRAFTING, EntryStacks.of(CraftingStationBlock.item))
    }
}
