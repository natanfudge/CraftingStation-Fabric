package io.github.natanfudge.utils.inventory

import net.minecraft.inventory.Inventory
import net.minecraft.screen.slot.Slot

class HideableSlot(inventory: Inventory, index: Int, xPosition: Int, yPosition: Int, var visible: Boolean) :
    Slot(inventory, index, xPosition, if (visible) yPosition else Int.MAX_VALUE) {
    private var visibleYPosition = yPosition

    fun show(y: Int? = null) {
        visible = true
        if (y != null) visibleYPosition = y
        this.y = visibleYPosition
    }

    fun hide() {
        visible = false
        this.y = Int.MAX_VALUE
    }
}