package io.github.natanfudge

import io.github.natanfudge.genericutils.squareGrid
import io.github.natanfudge.genericutils.use
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.WorldRenderer
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory
import net.minecraft.client.render.model.json.ModelTransformation
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.Vec3f

@Environment(EnvType.CLIENT)
class CraftingStationBlockEntityRenderer(context: BlockEntityRendererFactory.Context) : BlockEntityRenderer<CraftingStationBlockEntity> {
    private val itemRenderer = context.itemRenderer
    private val grid = squareGrid(3)
    override fun render(
        blockEntity: CraftingStationBlockEntity,
        tickDelta: Float,
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int,
        overlay: Int
    ) {
        if (blockEntity.inventory.isEmpty) return

        matrices.translate(0.0, ItemHeight, 0.0)
        grid.forEachCellOfSize(SpacingBetweenSlots) { x, y, i ->
            val item = blockEntity.inventory.getStack(i)
            if (item.isEmpty) return@forEachCellOfSize

            matrices.use {
                translate(x + SlotPadding, 0.0, y + SlotPadding)
                multiply(Vec3f.POSITIVE_Y.getRadialQuaternion(0f))
                scale(ItemScale, ItemScale, ItemScale)
                val model = itemRenderer.getModel(item, blockEntity.world, null, 0)
                val lightAbove = WorldRenderer.getLightmapCoordinates(blockEntity.world, blockEntity.pos.up())
                itemRenderer.renderItem(
                    item, ModelTransformation.Mode.FIXED,
                    false, matrices, vertexConsumers, lightAbove, OverlayTexture.DEFAULT_UV, model
                )
            }
        }
    }
}

private const val SpacingBetweenSlots = 0.189
private const val SlotPadding = 0.31
private const val ItemHeight = 1.0625
private const val ItemScale = 0.25f