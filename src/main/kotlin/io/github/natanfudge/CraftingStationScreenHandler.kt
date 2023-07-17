package io.github.natanfudge

import io.github.natanfudge.genericutils.*
import io.github.natanfudge.genericutils.superclasses.KScreenHandler
import io.github.natanfudge.injection.ImmutableItemStack
import io.github.natanfudge.utils.inventory.*
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.CraftingInventory
import net.minecraft.inventory.CraftingResultInventory
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.recipe.Recipe
import net.minecraft.recipe.RecipeType
import net.minecraft.screen.NamedScreenHandlerFactory
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.screen.slot.CraftingResultSlot
import net.minecraft.screen.slot.Slot
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World

class CraftingStationScreenHandler(
    id: Int,
    inv: PlayerInventory,
    craftingStationPos: BlockPos,
) : KScreenHandler(Type, id) {
    companion object {
        val Type: ScreenHandlerType<CraftingStationScreenHandler> = ExtendedScreenHandlerType { syncId, playerInventory, buf ->
            CraftingStationScreenHandler(syncId, playerInventory, buf.readBlockPos()/*, ArrayPropertyDelegate(1)*/)
        }
    }

    val adjacentInventories: MultiInventoryManager = manageAdjacentInventories(craftingStationPos, inv)
    private val player = inv.player
    private val world: World = player.world
    private val blockEntity: CraftingStationBlockEntity = inv.player.world.getSpecificBlockEntity(craftingStationPos)
    private val inputInventory = InventoryBackedCraftingInventory(this, blockEntity)
    private val outputInventory = CraftingResultInventory()
    private val combinedSlots = buildCombinedSlots()

    private var lastRecipe: Recipe<CraftingInventory>? = null
    private var lastLastRecipe: Recipe<CraftingInventory>? = null

    init {
        combinedSlots.forEachSlot { slot, _ -> addSlot(slot) }
        // Another player may change the contents of the table and we will need a new recipe when it is opened
        updateResult()
    }

    val hasAdjacentInventories = !adjacentInventories.isEmpty

    fun setTopRow(offset: Int) {
        adjacentInventories.scroll(offset)
    }

    fun selectInventory(inventoryIndex: Int) {
        adjacentInventories.select(inventoryIndex)
    }

    fun updateLastRecipeFromServer(recipe: Recipe<CraftingInventory>?) {
        lastRecipe = recipe
        // if no recipe, set to empty to prevent ghost outputs when another player grabs the result
        outputInventory.setStack(if (recipe != null) recipe.craft(inputInventory) else ItemStack.EMPTY)
    }


    val selectedAdjacentInventoryRows: Int
        get() = adjacentInventories.selected?.rows ?: 0

    override fun onContentChanged(inventory: Inventory) {
        updateResult()
    }

    override fun canUse(player: PlayerEntity): Boolean {
        return true
    }

    override fun transferSlotImmutable(player: PlayerEntity, index: Int, slotStack: ImmutableItemStack): ImmutableItemStack {
        return combinedSlots.quickTransfer(index, slotStack)
    }

    override fun insertItemImmutable(stack: ImmutableItemStack, startIndex: Int, endIndex: Int, fromLast: Boolean): ImmutableItemStack {
        // We don't care about fromLast because no one calls insertItem on other screenHandlers anyway...
        return combinedSlots.insert(stack, startIndex, endIndex)
    }

    override fun canInsertIntoSlot(stack: ItemStack, slot: Slot): Boolean {
        return slot.inventory !== outputInventory && super.canInsertIntoSlot(stack, slot)
    }

    private fun manageAdjacentInventories(craftingStationPos: BlockPos, playerInventory: PlayerInventory): MultiInventoryManager {
        val player = playerInventory.player
        with(player.world) {
            val existingInventoryPos = Direction.entries.map { craftingStationPos.offset(it) }.filter { adjacentInventoryUsable(it, player) }
            val inventories = existingInventoryPos.mapIndexed { index, blockPos -> getAdjacentInventory(index, blockPos) }
            return MultiInventoryManager(inventories/*, syncedCurrentSelectedInventory*/)
        }
    }

    private fun buildCombinedSlots() = CombinedSlots.of(this) {
        val (result, input, adjacent, player) = parts(
            listOf(getCraftingResultSlot()),
            getCraftingInputSlots(),
            adjacentInventories.slots,
            getPlayerSlots()
        )
        result.movesTo(player, adjacent)
        input.movesTo(player, adjacent)
        adjacent.movesTo(player, input)
        player.movesTo(input, adjacent)
    }

    // apparently vanilla CraftingResultSlot are slow because they search all recipes whenever you make a change to the items
    // We could consider optimizing it.
    private fun getCraftingResultSlot() = CraftingResultSlot(player, inputInventory, outputInventory, 0, CraftingResultSlotX, CraftingResultSlotY)

    private fun getCraftingInputSlots() = squareGrid(CraftingTableSize).minecraftCells { x, y, i ->
        Slot(inputInventory, i, CraftingMatrixStartX + x, CraftingMatrixStartY + y)
    }

    private fun getPlayerSlots(): List<Slot> = fullGrid(rows = 3, columns = 9).minecraftCells { x, y, i ->
        Slot(player.inventory, 9 + i, PlayerInventoryXStart + x, PlayerInventoryYStart + y)
    } + fullGrid(rows = 1, columns = 9).minecraftCells { x, _, i ->
        Slot(player.inventory, i, PlayerToolbarXStart + x, PlayerToolbarYStart)
    }


    private fun updateResult() {
        // if the recipe is no longer valid, update it
        if (lastRecipe == null || !lastRecipe!!.matches(inputInventory, world)) {
            lastRecipe = findRecipe(inputInventory, world)
        }

        val newResult = if (lastRecipe != null) lastRecipe!!.craft(inputInventory) else ItemStack.EMPTY

        // set the slot on both sides, client is for display/so the client knows about the recipe
        outputInventory.setStack(newResult)

        // update recipe on server
        world.inServer {
            syncResultToOtherPlayers(newResult)
        }
    }

    private fun findRecipe(inv: CraftingInventory, world: World): Recipe<CraftingInventory>? {
        return world.recipeManager.getFirstMatch(RecipeType.CRAFTING, inv, world).stream().findFirst().orElse(null)
    }

    context(ServerWorld)
    private fun syncResultToOtherPlayers(newResult: ItemStack) {
        // we need to sync to all players currently in the inventory
        val relevantPlayers = players.filter { player.hasThisStationOpened() }

        // sync result to all serverside inventories to prevent duplications/recipes being blocked
        // need to do this every time as otherwise taking items of the result causes desync
        relevantPlayers.forEach { otherPlayer: ServerPlayerEntity ->
            // safe cast since hasSameContainerOpen does class checks
            val handler = otherPlayer.currentScreenHandler as CraftingStationScreenHandler
            handler.setResultStack(newResult, revision)

            // if the recipe changed, update clients last recipe
            // this also updates the client side display when the recipe is added
            if (lastLastRecipe != lastRecipe) {
                handler.lastRecipe = lastRecipe
                Packets.SyncRecipe.send(lastRecipe?.id, otherPlayer)
            }

        }
        if (lastLastRecipe != lastRecipe) {
            lastLastRecipe = lastRecipe
        }
    }

    private fun setResultStack(stack: ItemStack, revision: Int) {
        this.outputInventory.setStack(stack)
        this.revision = revision
    }

    private fun PlayerEntity.hasThisStationOpened(): Boolean {
        val stationHandler = currentScreenHandler as? CraftingStationScreenHandler ?: return false
        return stationHandler.blockEntity == this@CraftingStationScreenHandler.blockEntity
    }
}


context(World)
private fun adjacentInventoryUsable(pos: BlockPos, byPlayer: PlayerEntity): Boolean {
    val blockEntity = getBlockEntity(pos)
    // Don't access other tables from this table
    if (blockEntity == null || blockEntity is CraftingStationBlockEntity) return false
    val inventory = getCompleteInventoryAt(pos)
    return !(inventory == null || !inventory.canPlayerUse(byPlayer))
}

context(World)
private fun getAdjacentInventory(i: Int, adjacentPos: BlockPos): ManagedInventory {
    val blockEntity = getBlockEntity(adjacentPos)!!
    val inventory = getCompleteInventoryAt(adjacentPos)!!
    val inventoryName = if (blockEntity is NamedScreenHandlerFactory) blockEntity.displayName else blockEntity.cachedState.block.name
    val block = getBlockState(adjacentPos)
    val slots = adjacentInventorySlots(inventory, isFirstInventory = i == 0)
    return ManagedInventory(
        inventoryName, inventory, block, slots,
        columns = AdjacentInventoryColumns, maxVisibleRows = AdjacentInventoryMaxVisibleRows, cellsYStart = AdjacentInventoryCellsYStart
    )
}

private fun adjacentInventorySlots(inventory: Inventory, isFirstInventory: Boolean): List<HideableSlot> {
    val hasScrollbar = inventory.size() > AdjacentInventoryMaxVisibleCells
    val startX = if (hasScrollbar) AdjacentSlotsXStartWithScrollbar else AdjacentSlotsXStartNoScrollbar
    return grid(inventory.size(), columns = AdjacentInventoryColumns).minecraftCells { x, y, index ->
        val slotY = y + AdjacentSlotsYStart
        val visible = y < AdjacentInventoryMaxVisibleRows * MinecraftConstants.InventoryCellSize && isFirstInventory
        HideableSlot(inventory, index, x + startX, slotY, visible = visible)
    }
}

private const val CraftingResultSlotX = 124
private const val CraftingResultSlotY = 35
private const val CraftingMatrixStartX = 30
private const val CraftingMatrixStartY = 17
private const val CraftingTableSize = 3
private const val AdjacentSlotsXStartWithScrollbar = -125
private const val AdjacentSlotsXStartNoScrollbar = -117
private const val AdjacentSlotsYStart = 17
private const val PlayerInventoryXStart = 8
private const val PlayerInventoryYStart = 84
private const val PlayerToolbarXStart = PlayerInventoryXStart
private const val PlayerToolbarYStart = 142
private const val AdjacentInventoryCellsYStart = 17
private const val AdjacentInventoryColumns = 6
 const val AdjacentInventoryMaxVisibleRows = 9
private const val AdjacentInventoryMaxVisibleCells = AdjacentInventoryColumns * AdjacentInventoryMaxVisibleRows