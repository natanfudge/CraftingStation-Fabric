package io.github.natanfudge.rei

import io.github.natanfudge.CraftingStationScreenHandler
import me.shedaniel.rei.api.common.plugins.REIServerPlugin
import me.shedaniel.rei.api.common.transfer.info.MenuInfoRegistry
import me.shedaniel.rei.api.common.transfer.info.simple.RecipeBookGridMenuInfo
import me.shedaniel.rei.api.common.transfer.info.simple.SimpleMenuInfoProvider
import me.shedaniel.rei.plugin.common.BuiltinPlugin

class CraftingStationReiServerPlugin: REIServerPlugin {
    override fun registerMenuInfo(registry: MenuInfoRegistry) {
        registry.register(BuiltinPlugin.CRAFTING, CraftingStationScreenHandler::class.java, SimpleMenuInfoProvider.of(::RecipeBookGridMenuInfo))
    }
}