@file:Suppress("OVERRIDE_DEPRECATION")

package io.github.natanfudge.genericutils.superclasses

import io.github.natanfudge.genericutils.*
import net.minecraft.block.Block
import net.minecraft.block.BlockEntityProvider
import net.minecraft.block.BlockState
import net.minecraft.block.ShapeContext
import net.minecraft.block.Waterloggable
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.fluid.FluidState
import net.minecraft.fluid.Fluids
import net.minecraft.inventory.Inventory
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.item.ItemPlacementContext
import net.minecraft.screen.NamedScreenHandlerFactory
import net.minecraft.state.StateManager
import net.minecraft.state.property.Properties
import net.minecraft.state.property.Property
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.shape.VoxelShape
import net.minecraft.world.BlockView
import net.minecraft.world.World
import net.minecraft.world.WorldAccess
import java.lang.IllegalArgumentException

/**
 * Shorthands for assigning values to BlockStates
 */
data class PropertyKeyValue<T : Comparable<T>>(val key: Property<T>, val defaultValue: T, val placementValue: ItemPlacementContext.() -> T)

fun <T : Comparable<T>> Property<T>.default(value: T, placementValue: ItemPlacementContext.() -> T) = PropertyKeyValue(this, value, placementValue)


/**
 * Block States can be added by overriding [defaultProperties].
 * Make sure that when you override [defaultProperties] you add the super() to the list.
 */
abstract class KBlock(
    /**
     * The same ID will be used for registering the block and potential items and block entities
     */
    id: String,
    blockSettings: Settings,
    /**
     * If specified, a BlockItem will be generated for the block
     */
    val itemSettings: Item.Settings? = null,
    private val shape: VoxelShape? = null,
    /**
     * If true, the NamedScreenHandlerFactory at the block's position will be activated when the block is right-clicked
     */
    private val hasScreen: Boolean = false,
    /**
     * If true, the Inventory at the block's position will be scattered on the ground when the block is destroyed
     */
    private val dropInventoryOnDestroyed: Boolean = false
) : Block(blockSettings) {
    init {
        // Validation
        if (this is BlockEntityProvider && this !is KBlockEntityProvider) {
            throw IllegalArgumentException("KBlocks that have block entities should implement KBlockEntityProvider and not BlockEntityProvider.")
        }
    }

    val id = csId(id)
    val item = BlockItem(this, itemSettings)

    //////////// Inventory implementation /////////

    override fun onStateReplaced(state: BlockState, world: World, pos: BlockPos, newState: BlockState, moved: Boolean) {
        if (dropInventoryOnDestroyed) {
            if (state.block != newState.block) {
                world.whenBlockEntityIs<Inventory>(pos) { inventory ->
                    inventory.forEachNonEmptyStack {
                        world.scatterItemStack(it, pos)
                    }
                    world.updateComparators(pos, this)
                }
                super.onStateReplaced(state, world, pos, newState, moved)
            }
        } else super.onStateReplaced(state, world, pos, newState, moved)
    }

    //////////// Screen implementation /////////////

    override fun onUse(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, hit: BlockHitResult): ActionResult {
        if (hasScreen) {
            if (world.isServer) {
                world.whenBlockEntityIs<NamedScreenHandlerFactory>(pos) {
                    player.openHandledScreen(it)
                }
            }
            return ActionResult.SUCCESS
        }
        return super.onUse(state, world, pos, player, hand, hit)
    }

    override fun createScreenHandlerFactory(state: BlockState, world: World, pos: BlockPos): NamedScreenHandlerFactory? {
        if (hasScreen) {
            world.whenBlockEntityIs<NamedScreenHandlerFactory>(pos) {
                return it
            }
            return null
        }
        return super.createScreenHandlerFactory(state, world, pos)
    }

    /////////////// Shape implementation

    final override fun getOutlineShape(state: BlockState?, world: BlockView?, pos: BlockPos?, context: ShapeContext?): VoxelShape {
        return shape ?: super.getOutlineShape(state, world, pos, context)
    }

    //////////////// WaterLoggable implementation ///////////////

    // This can't be a constructor parameter because the data is needed too early (during the parent constructor, before the child constructor)
    private fun isWaterLoggable() = this is Waterloggable

    final override fun getFluidState(state: BlockState): FluidState {
        return if (isWaterLoggable() && state[Properties.WATERLOGGED]) Fluids.WATER.getStill(false) else super.getFluidState(state)
    }

    final override fun getStateForNeighborUpdate(
        state: BlockState,
        direction: Direction,
        neighborState: BlockState,
        world: WorldAccess,
        pos: BlockPos,
        neighborPos: BlockPos
    ): BlockState? {
        if (isWaterLoggable() && state.get(Properties.WATERLOGGED)) {
            world.createAndScheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world))
        }
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos)
    }


    ////////// BlockState implementation ////////////////

    init {
        // Bad minecraft code design leaking into mine
        @Suppress("LeakingThis")
        for (keyValue in defaultProperties()) {
            // Hack, this cast is safe because key type is equal to value type always.
            @Suppress("UNCHECKED_CAST")
            keyValue as PropertyKeyValue<Comparable<Any>>
            defaultState = defaultState.with(keyValue.key, keyValue.defaultValue)
        }
    }

    // Minecraft uses this super early so unfortunately it can't be a constructor parameter.
    /**
     * Make sure that when you override [defaultProperties] you add the super() to the list.
     */
    open fun defaultProperties(): List<PropertyKeyValue<*>> {
        return if (isWaterLoggable()) listOf(Properties.WATERLOGGED.default(false) { world.getFluidState(blockPos).fluid == Fluids.WATER }) else listOf()
    }


    final override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        builder.add(*defaultProperties().map { it.key }.toTypedArray())
    }

    // Cannot be used inside appendProperties()
    // Bad minecraft code design leaking into mine
    @Suppress("LeakingThis")
    private val savedDefaultProperties = defaultProperties()

    final override fun getPlacementState(ctx: ItemPlacementContext): BlockState {
        var state = defaultState
        for (property in savedDefaultProperties) {
            // Hack, this cast is safe because key type is equal to value type always.
            @Suppress("UNCHECKED_CAST")
            property as PropertyKeyValue<Comparable<Any>>
            state = state.with(property.key, property.placementValue(ctx))
        }
        return state
    }
}