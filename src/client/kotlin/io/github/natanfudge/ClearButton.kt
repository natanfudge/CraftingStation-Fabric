package io.github.natanfudge

import io.github.natanfudge.genericutils.Textures
import io.github.natanfudge.genericutils.Tooltips
import io.github.natanfudge.genericutils.customTexture
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.Text

class ClearButton(x: Int, y: Int, screen: Screen, onPress: PressAction) :
    ButtonWidget(x, y, Size, Size, Text.empty(), onPress, Tooltips.ofText(Text.translatable("text.crafting_station.clear"), screen)) {
    override fun renderButton(stack: MatrixStack, mouseX: Int, mouseY: Int, partialTicks: Float) = with(stack) {
        Clear.draw(x, y)
        if (isHovered) {
            renderTooltip(stack, mouseX, mouseY)
        }
    }

    companion object : Textures {
        const val X = 85
        const val Y = 16
        const val Size = 9
        val Clear = customTexture("textures/gui/clear.png", height = Size, width = Size)
    }
}

