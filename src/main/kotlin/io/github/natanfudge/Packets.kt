package io.github.natanfudge

import io.github.natanfudge.genericutils.CommonInit
import io.github.natanfudge.genericutils.network.c2sPacket
import io.github.natanfudge.genericutils.network.s2cPacket
import net.minecraft.util.Identifier

object Packets {
    val ClearScreen = c2sPacket<Unit>("clear")
    val SyncRecipe = s2cPacket<Identifier?>("sync_recipe")
    context(CommonInit)
    fun registerMessages() {
        ClearScreen.register { _, player, _, _, _ ->
            val container = player.currentScreenHandler;
            if (container is CraftingStationScreenHandler) {
                container.clearCraftingSlots()
            }
        }
    }
}