package io.github.natanfudge.genericutils.superclasses

import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.util.math.BlockPos

//TODO: cleanup and doc this

/**
 * Make sure that the BlockEntityType is unique per block
 */
class KBlockEntityType<T : BlockEntity>(
    private val factory: BlockEntityFactory<out T>,
    blocks: Set<Block>,
    /**
     * Set to true if the BlockEntity displays something that requires its data that needs to be seen when looking at the block
     */
    val clientRequiresNbt: Boolean
) :
    BlockEntityType<T>(factory, blocks, null), KBlockEntityProvider
{

    override val blockEntityType: BlockEntityType<*>
        get() = this

    companion object {
        fun <T : BlockEntity> ofBlock(
            // Lazy because the BlockEntityType must be initialized before the Block
            block: () -> KBlock,
            constructor: BlockEntityFactory<T>,
            clientRequiresNbt: Boolean = false
        ): KBlockEntityType<T> =
            KBlockEntityType(constructor, LazyOneElementSet(block),clientRequiresNbt, /*, screenHandler*/)
    }

    override fun createBlockEntity(pos: BlockPos, state: BlockState): BlockEntity {
        return factory.create(pos, state)
    }

}


// I'm gonna need to expand it to more than one element at some point
private class LazyOneElementSet<T>(private val element: () -> T) : Set<T> {
    override val size: Int = 1

    override fun isEmpty(): Boolean {
        return false
    }

    override fun iterator(): Iterator<T> {
        return Iterator(element)
    }

    override fun containsAll(elements: Collection<T>): Boolean {
        return elements.all { contains(it) }
    }

    override fun contains(element: T): Boolean {
        val el = this.element()
        val eq = element == this.element()
        return element == this.element()
    }

    class Iterator<T>(val element: () -> T) : kotlin.collections.Iterator<T> {
        private var returnedElement = false
        override fun hasNext(): Boolean {
            return !returnedElement
        }

        override fun next(): T {
            returnedElement = true
            return element()
        }

    }
}
