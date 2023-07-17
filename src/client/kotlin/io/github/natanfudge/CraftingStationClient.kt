package io.github.natanfudge

import io.github.natanfudge.genericutils.ClientInit
import io.github.natanfudge.genericutils.network.register
import io.github.natanfudge.genericutils.whenCurrentScreenIs
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry
import net.minecraft.client.gui.screen.ingame.HandledScreens
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories
import net.minecraft.inventory.CraftingInventory
import net.minecraft.recipe.Recipe

object CraftingStationClient : ClientModInitializer {
    override fun onInitializeClient() = with(ClientInit) {
        HandledScreens.register(
            CraftingStationScreenHandler.Type
        ) { handler, playerInventory, title -> CraftingStationScreen(handler, playerInventory, title) }

        BlockEntityRendererFactories.register(CraftingStationBlockEntity.Type, ::CraftingStationBlockEntityRenderer)

        Packets.SyncRecipe.register { _, _, content, _ ->
            whenCurrentScreenIs<CraftingStationScreen> { world ->
                if (world != null) {
                    val recipe = if (content == null) null else world.recipeManager.get(content).orElse(null)
                    screenHandler.updateLastRecipeFromServer(recipe as Recipe<CraftingInventory>?)
                }
            }
        }
    }
}