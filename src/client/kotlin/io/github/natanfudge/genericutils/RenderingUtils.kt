package io.github.natanfudge.genericutils

import com.mojang.blaze3d.systems.RenderSystem
import io.github.natanfudge.genericutils.csId
import net.minecraft.client.gui.DrawableHelper
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier

/**
 * Classes that use textures should declare an object and make it implement [Textures].
 * Then textures should be declared as properties of the object and initialized using the [Textures] extension methods below.
 * Then the textures can be used in rendering code.
 */
interface Textures

fun Textures.vanillaTexturePart(path: String, u: Int = 0, v: Int = 0, width: Int, height: Int, textureWidth: Int = 256, textureHeight: Int = 256) =
    TexturePart(Identifier(path), u, v, width, height, textureWidth, textureHeight)

fun Textures.vanillaBackgroundTexture(path: String) = vanillaTexturePart(path, width = 176, height = 166)

fun Textures.customTexture(path: String, width: Int, height: Int) =
    TexturePart(csId(path), u = 0, v = 0, width, height, textureWidth = width, textureHeight = height)

data class TexturePart(
    val texture: Identifier,
    val u: Int,
    val v: Int,
    val width: Int,
    val height: Int,
    val textureWidth: Int,
    val textureHeight: Int
) {
    context(DrawableHelper, MatrixStack)
    fun draw(x: Int, y: Int) {
        RenderSystem.setShaderTexture(0, texture)
        DrawableHelper.drawTexture(this@MatrixStack, x, y, zOffset, u.toFloat(), v.toFloat(), width, height, textureWidth, textureHeight)
    }
}

