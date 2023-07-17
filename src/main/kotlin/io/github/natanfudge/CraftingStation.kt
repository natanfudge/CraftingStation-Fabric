package io.github.natanfudge

import io.github.natanfudge.genericutils.CommonInit
import io.github.natanfudge.genericutils.register
import net.fabricmc.api.ModInitializer
import org.slf4j.LoggerFactory

object CraftingStation : ModInitializer {
    const val ModId = "crafting_station"
    private val logger = LoggerFactory.getLogger("crafting_station")

    override fun onInitialize()  = with(CommonInit) {
        register(CraftingStationBlock)
        register(CraftingStationScreenHandler.Type, "crafting_station")
        Packets.registerMessages()
    }
}