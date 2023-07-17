package io.github.natanfudge.genericutils

import io.github.natanfudge.TabButton
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.ButtonWidget.TooltipSupplier
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.client.world.ClientWorld
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos

inline fun <reified T : Screen> whenCurrentScreenIs(action: T.(ClientWorld?) -> Unit) {
    val client = getClient()
    val screen = client.currentScreen
    if (screen is T) {
        action(screen, client.world)
    }
}

fun getClient(): MinecraftClient = MinecraftClient.getInstance()

object Tooltips {
    fun ofItemStack( stack: ItemStack, screen: Screen): TooltipSupplier = TooltipSupplier { _, poseStack, x, y ->
        screen.renderTooltip(poseStack, stack, x, y)
    }
    fun ofText(text: Text, screen: Screen) = TooltipSupplier { _, matrices, mouseX, mouseY: Int ->
        screen.renderTooltip(matrices,text,mouseX,mouseY)
    }
}

inline fun MatrixStack.use(usage: MatrixStack.() -> Unit) {
    push()
    usage()
    pop()
}