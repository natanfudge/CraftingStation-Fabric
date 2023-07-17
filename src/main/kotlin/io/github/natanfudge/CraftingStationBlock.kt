
package io.github.natanfudge

import io.github.natanfudge.genericutils.superclasses.KBlock
import io.github.natanfudge.genericutils.superclasses.KBlockEntityProvider
import io.github.natanfudge.genericutils.Voxels.createBlockCuboid
import net.minecraft.block.*
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.util.shape.VoxelShapes

object CraftingStationBlock : KBlock(
    "crafting_station",
    Settings.of(Material.WOOD),
    Item.Settings().group(ItemGroup.DECORATIONS),
    shape = VoxelShapes.union(
        createBlockCuboid(0, 12, 0, 16, 16, 16),
        createBlockCuboid(0, 0, 0, 4, 12, 4),
        createBlockCuboid(12, 0, 0, 16, 12, 4),
        createBlockCuboid(0, 0, 12, 4, 12, 16),
        createBlockCuboid(12, 0, 12, 16, 12, 16)
    ).simplify(),
    hasScreen = true,
    dropInventoryOnDestroyed = true
), Waterloggable,
    KBlockEntityProvider by CraftingStationBlockEntity.Type
