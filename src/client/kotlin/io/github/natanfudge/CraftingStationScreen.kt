package io.github.natanfudge

import com.mojang.blaze3d.systems.RenderSystem
import io.github.natanfudge.genericutils.*
import io.github.natanfudge.genericutils.MinecraftConstants.InventoryCellSize
import io.github.natanfudge.genericutils.network.send
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.text.Text
import kotlin.math.roundToInt

private const val TabButtonsY = -22
private const val TabButtonsXStart = -128
 const val TabButtonWidth = 22
 const val TabButtonHeight = 28



private const val AdjacentInventoryBackgroundWidth = 131
private const val AdjacentInventoryBackgroundHeight = 183
private const val AdjacentInventoryBackgroundX = -AdjacentInventoryBackgroundWidth + 1
private const val SelectedInventoryTitleX = -122f
private const val SelectedInventoryTitleY = 6f
private const val GrayColor = 0x404040

private const val ScrollBarX = -17
private const val ScrollBarYRelativeToAdjacentInventory = 16
private const val ScrollHandleYRelativeToAdjacentInventory = ScrollBarYRelativeToAdjacentInventory + 1
private const val ScrollHandleX = ScrollBarX + 1
private const val ScrollHandleHeight = 15


class CraftingStationScreen(handler: CraftingStationScreenHandler, inventory: PlayerInventory, title: Text) :
    HandledScreen<CraftingStationScreenHandler>(handler, inventory, title) {
    private var currentScroll = 0.0
        set(scroll) {
            val actualScroll = scroll.coerceIn(0.0, 1.0)
            field = actualScroll
            handler.setTopRow((scrollableRows * actualScroll).roundToInt())
        }
    private var isScrolling = false
    override fun init() {
        super.init()
        handler.adjacentInventories.inventories.forEachIndexed { i, inventory ->
            addDrawableChild(
                TabButton(
                    screen = this,
                    x = x + TabButtonsXStart + (TabButtonWidth - 1) * i, y = y + TabButtonsY,
                    width = TabButtonWidth, height = TabButtonHeight,
                    i, representativeStack = inventory.holdingBlock.toItemStack()
                ) { handler.selectInventory(i) }
            )
        }

        addDrawableChild(
            ClearButton(x = x + ClearButton.X, y = y + ClearButton.Y, screen = this) {
                Packets.ClearScreen.send(Unit)
            }
        )
    }

    /**
     * Determines at what bounds items should be thrown when dragged on a position
     */
    override fun isClickOutsideBounds(mouseX: Double, mouseY: Double, guiLeftIn: Int, guiTopIn: Int, mouseButton: Int): Boolean {
        // Include the secondary background as the bounds as well
        if (handler.hasAdjacentInventories && isPointWithinBounds(
                AdjacentInventoryBackgroundX, TabButtonsY, AdjacentInventoryBackgroundWidth,
                AdjacentInventoryBackgroundHeight + InventoryCellSize,
                mouseX, mouseY
            )
        ) {
            return false
        }
        return super.isClickOutsideBounds(mouseX, mouseY, guiLeftIn, guiTopIn, mouseButton)
    }

    override fun render(stack: MatrixStack, mouseX: Int, mouseY: Int, partialTicks: Float) {
        renderBackground(stack)
        super.render(stack, mouseX, mouseY, partialTicks)
        drawMouseoverTooltip(stack, mouseX, mouseY)
    }

    override fun drawForeground(stack: MatrixStack, mouseX: Int, mouseY: Int) {
        super.drawForeground(stack, mouseX, mouseY)
        if (handler.hasAdjacentInventories) {
            val selectedInventoryTitle = handler.adjacentInventories.selectedInventoryName().string.truncate(23)
            textRenderer.draw(stack, selectedInventoryTitle, SelectedInventoryTitleX, SelectedInventoryTitleY, GrayColor)
        }
    }

    override fun drawBackground(stack: MatrixStack, partialTicks: Float, mouseX: Int, mouseY: Int) = with(stack) {
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f)
        CraftingTableBackground.draw(x, y)
        val selectedInventory = handler.adjacentInventories.selected
        if (selectedInventory != null) {
            val adjacentInventoryBackgroundY = (height - backgroundHeight) / 2
            SecondaryInventoryBackground.draw(x = x + AdjacentInventoryBackgroundX, y = adjacentInventoryBackgroundY)
            selectedInventory.slots.forEach {
                // For some reason we need to offset x and y by -1
                if (it.visible) InventorySlot.draw(x = x + it.x - 1, y = y + it.y - 1)
            }
            if (hasScrollbar()) {
                val scrollHandleX = x + ScrollHandleX
                Scrollbar.draw(x = x + ScrollBarX, y = adjacentInventoryBackgroundY + ScrollBarYRelativeToAdjacentInventory)
                val scrollHandleBaseY = adjacentInventoryBackgroundY + ScrollHandleYRelativeToAdjacentInventory
                // Minus 2 pixels for the border of the scrollbar
                val scrollDistance = (ScrollbarRangeOfMovement * currentScroll).toInt()
                val scrollHandleY = scrollHandleBaseY + scrollDistance

                if (isScrolling) ScrollHandleScrolling.draw(x = scrollHandleX, y = scrollHandleY)
                else ScrollHandle.draw(x = scrollHandleX, y = scrollHandleY)
            }

        }
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, scroll: Int): Boolean {
        if (hasScrollbar()) {
            val scrollHandleX = x + ScrollHandleX
            val scrollHandleEndX = scrollHandleX + ScrollHandle.width
            isScrolling = mouseX.inBounds(scrollHandleX, scrollHandleEndX)
        }
        return super.mouseClicked(mouseX, mouseY, scroll)
    }

    override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, deltaX: Double, deltaY: Double): Boolean {
        if (isScrolling) {
            currentScroll += (deltaY / ScrollbarRangeOfMovement)
        }

        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, scroll: Int): Boolean {
        isScrolling = false
        return super.mouseReleased(mouseX, mouseY, scroll)
    }

    private fun hasScrollbar(): Boolean {
        return handler.selectedAdjacentInventoryRows > AdjacentInventoryMaxVisibleRows
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, scrollDelta: Double): Boolean {
        if (hasScrollbar() && mouseX.inBoundsExclusive(x - InventoryCellSize * 7, x)) {
            currentScroll -= scrollDelta / scrollableRows
            return true
        }
        return false
    }


    private val scrollableRows get() = handler.selectedAdjacentInventoryRows - AdjacentInventoryMaxVisibleRows


    companion object : Textures {
        private val CraftingTableBackground = vanillaBackgroundTexture("textures/gui/container/crafting_table.png")
        private val InventorySlot = vanillaTexturePart(
            "textures/gui/container/creative_inventory/tab_items.png",
            u = 8,
            v = 17,
            width = InventoryCellSize,
            height = InventoryCellSize
        )
        private val Scrollbar = customTexture("textures/gui/long_scrollbar.png", width = 14, height = 162)
        private val ScrollHandleScrolling = vanillaTexturePart(
            "textures/gui/container/creative_inventory/tabs.png", u = 232,
            v = 0,
            width = 12,
            height = ScrollHandleHeight
        )
        private val ScrollHandle = vanillaTexturePart(
            "textures/gui/container/creative_inventory/tabs.png", u = 244,
            v = 0,
            width = 12,
            height = ScrollHandleHeight
        )
        private val SecondaryInventoryBackground = customTexture("textures/gui/secondary_inventory_background.png", width = 131, height = 183)

        private val ScrollbarRangeOfMovement = Scrollbar.height - 2 - ScrollHandleHeight

    }
}

