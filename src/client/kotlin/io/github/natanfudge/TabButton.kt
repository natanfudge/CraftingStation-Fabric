package io.github.natanfudge

import com.mojang.blaze3d.systems.RenderSystem
import io.github.natanfudge.genericutils.Textures
import io.github.natanfudge.genericutils.Tooltips
import io.github.natanfudge.genericutils.customTexture
import io.github.natanfudge.genericutils.getClient
import io.github.natanfudge.utils.*
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.item.ItemStack
import net.minecraft.text.Text

class TabButton(
    screen: Screen,
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    private val index: Int,
    private val representativeStack: ItemStack,
    onPress: PressAction,
) : ButtonWidget(x, y, width, height, Text.empty(), onPress, Tooltips.ofItemStack(representativeStack, screen)) {
    override fun renderButton(matrices: MatrixStack, mouseX: Int, mouseY: Int, partialTicks: Float) = with(matrices){
        RenderSystem.enableBlend()
        RenderSystem.defaultBlendFunc()
        RenderSystem.enableDepthTest()
        val screenHandler = getClient().player!!.currentScreenHandler as CraftingStationScreenHandler
        if (screenHandler.adjacentInventories.selectedIndex == index) {
            SelectedTab.draw(x,y)
        } else {
            UnselectedTab.draw(x, y)
        }
        if (!representativeStack.isEmpty) {
            getClient().itemRenderer.renderInGui(representativeStack, x + 3, y + 3)
        }
        if (isHovered) {
            renderTooltip(matrices, mouseX, mouseY)
        }
    }

    companion object: Textures {
        val SelectedTab = customTexture("textures/gui/selected_tab.png", width = TabButtonWidth, height = TabButtonHeight)
        val UnselectedTab = customTexture("textures/gui/unselected_tab.png", width = TabButtonWidth, height = TabButtonHeight)
    }
}
