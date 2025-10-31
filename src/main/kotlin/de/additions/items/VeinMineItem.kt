package de.additions.items

import net.minecraft.block.*
import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.ToolComponent
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.*
import net.minecraft.registry.tag.BlockTags
import net.minecraft.registry.tag.TagKey
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World
import net.minecraft.world.event.GameEvent

/**
 * Represents a custom mining tool that breaks a vein of connected blocks of the same type.
 * When a block is mined, it recursively searches for and breaks adjacent blocks of the same type, up to a certain limit.
 * This item also includes Area of Effect (AoE) functionality for right-click actions like stripping logs if it's an axe.
 *
 * Inherits from [ToolItem] and relies on the [ToolComponent] being correctly configured via [Item.Settings]
 * during item registration (e.g., using `.pickaxe()`, `.axe()`, or `.tool()` helpers).
 *
 * @property material The [ToolMaterial] defining base properties.
 * @property effectiveBlocks A [TagKey]<[Block]> specifying which blocks this tool is effective against.
 * The vein mining effect will only trigger on these blocks.
 * @param settings The base [Item.Settings] for this item. MUST be pre-configured with appropriate components
 * (like [DataComponentTypes.TOOL]) for base tool functionality.
 */
class VeinMineItem(
    material: ToolMaterial,
    effectiveBlocks: TagKey<Block>,
    settings: Settings,
) : ToolItem(material, effectiveBlocks, settings) {

    companion object {
        /** The maximum number of blocks that can be mined in a single vein mining operation. */
        private const val MAX_VEIN_SIZE = 64

        /**
         * An array of [BlockPos] offsets representing all 26 neighboring positions in a 3x3x3 cube
         * surrounding a central block. Used to find adjacent blocks for the vein mining algorithm.
         */
        private val NEIGHBOR_OFFSETS: Array<BlockPos> = buildList {
            for (dx in -1..1) {
                for (dy in -1..1) {
                    for (dz in -1..1) {
                        if (dx == 0 && dy == 0 && dz == 0) continue // Skip the center block itself
                        add(BlockPos(dx, dy, dz))
                    }
                }
            }
        }.toTypedArray()

        /** A map that associates strippable blocks (logs, wood) with their stripped counterparts. */
        private val STRIPPED_BLOCKS: Map<Block, Block> = mapOf(
            Blocks.OAK_WOOD to Blocks.STRIPPED_OAK_WOOD,
            Blocks.OAK_LOG to Blocks.STRIPPED_OAK_LOG,
            Blocks.DARK_OAK_WOOD to Blocks.STRIPPED_DARK_OAK_WOOD,
            Blocks.DARK_OAK_LOG to Blocks.STRIPPED_DARK_OAK_LOG,
            Blocks.PALE_OAK_WOOD to Blocks.STRIPPED_PALE_OAK_WOOD,
            Blocks.PALE_OAK_LOG to Blocks.STRIPPED_PALE_OAK_LOG,
            Blocks.ACACIA_WOOD to Blocks.STRIPPED_ACACIA_WOOD,
            Blocks.ACACIA_LOG to Blocks.STRIPPED_ACACIA_LOG,
            Blocks.CHERRY_WOOD to Blocks.STRIPPED_CHERRY_WOOD,
            Blocks.CHERRY_LOG to Blocks.STRIPPED_CHERRY_LOG,
            Blocks.BIRCH_WOOD to Blocks.STRIPPED_BIRCH_WOOD,
            Blocks.BIRCH_LOG to Blocks.STRIPPED_BIRCH_LOG,
            Blocks.JUNGLE_WOOD to Blocks.STRIPPED_JUNGLE_WOOD,
            Blocks.JUNGLE_LOG to Blocks.STRIPPED_JUNGLE_LOG,
            Blocks.SPRUCE_WOOD to Blocks.STRIPPED_SPRUCE_WOOD,
            Blocks.SPRUCE_LOG to Blocks.STRIPPED_SPRUCE_LOG,
            Blocks.WARPED_STEM to Blocks.STRIPPED_WARPED_STEM,
            Blocks.WARPED_HYPHAE to Blocks.STRIPPED_WARPED_HYPHAE,
            Blocks.CRIMSON_STEM to Blocks.STRIPPED_CRIMSON_STEM,
            Blocks.CRIMSON_HYPHAE to Blocks.STRIPPED_CRIMSON_HYPHAE,
            Blocks.MANGROVE_WOOD to Blocks.STRIPPED_MANGROVE_WOOD,
            Blocks.MANGROVE_LOG to Blocks.STRIPPED_MANGROVE_LOG,
            Blocks.BAMBOO_BLOCK to Blocks.STRIPPED_BAMBOO_BLOCK
        )
    }

    /**
     * Overrides the default right-click behavior to apply axe actions (stripping, scraping, de-waxing)
     * in an Area of Effect (AoE) if this tool is an axe.
     * The AoE is a circular area on the plane perpendicular to the player's facing direction.
     *
     * @param context The context in which the item was used.
     * @return [ActionResult.SUCCESS] if the AoE action was performed, otherwise delegates to the parent implementation.
     */
    override fun useOnBlock(context: ItemUsageContext): ActionResult {
        // Only perform AoE stripping/scraping if the tool is an axe.
        if (effectiveBlocks != BlockTags.AXE_MINEABLE) return super.useOnBlock(context)

        val world = context.world
        val player = context.player ?: return ActionResult.PASS
        val pos = context.blockPos
        val stack = context.stack

        // Prevent action if certain offhand conditions are met.
        if (shouldCancelStripAttempt(context)) return ActionResult.PASS

        val facing = player.facing

        /**
         * Helper function to attempt stripping/scraping a single block at the target position.
         * @param targetPos The position of the block to interact with.
         */
        fun handleBlockAt(targetPos: BlockPos) {
            tryStrip(world, targetPos, player, world.getBlockState(targetPos))?.let { newState ->
                world.setBlockState(targetPos, newState, 11) // Set block state with updates
                world.emitGameEvent(GameEvent.BLOCK_CHANGE, targetPos, GameEvent.Emitter.of(player, newState))
                stack.damage(1, player, context.hand)
            }
        }

        when (facing) {
            Direction.UP, Direction.DOWN -> {
                for (dx in -RADIUS..RADIUS) {
                    for (dz in -RADIUS..RADIUS) {
                        if ((dx != 0 || dz != 0) && dx * dx + dz * dz <= RADIUS * RADIUS) {
                            handleBlockAt(pos.add(dx, 0, dz))
                        }
                    }
                }
            }
            Direction.NORTH, Direction.SOUTH -> {
                for (dx in -RADIUS..RADIUS) {
                    for (dy in -RADIUS..RADIUS) {
                        if ((dx != 0 || dy != 0) && dx * dx + dy * dy <= RADIUS * RADIUS) {
                            handleBlockAt(pos.add(dx, dy, 0))
                        }
                    }
                }
            }
            Direction.EAST, Direction.WEST -> {
                for (dz in -RADIUS..RADIUS) {
                    for (dy in -RADIUS..RADIUS) {
                        if ((dz != 0 || dy != 0) && dz * dz + dy * dy <= RADIUS * RADIUS) {
                            handleBlockAt(pos.add(0, dy, dz))
                        }
                    }
                }
            }
            else -> Unit
        }

        // Also apply the action to the center block that was initially clicked.
        tryStrip(world, pos, player, world.getBlockState(pos))?.let { newState ->
            world.setBlockState(pos, newState, 11)
            world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(player, newState))
            stack.damage(1, player, context.hand)
        }

        return ActionResult.SUCCESS
    }

    /**
     * Attempts to perform a right-click axe action on a block (strip, scrape, or wax off).
     * Plays the appropriate sound effect on success.
     *
     * @param world The world where the action takes place.
     * @param pos The position of the block.
     * @param player The player performing the action.
     * @param state The current state of the block.
     * @return The new [BlockState] if an action was successful, otherwise `null`.
     */
    private fun tryStrip(world: World, pos: BlockPos, player: PlayerEntity?, state: BlockState): BlockState? {
        // 1. Attempt to strip log/wood
        getStrippedState(state)?.let { newState ->
            world.playSound(player, pos, SoundEvents.ITEM_AXE_STRIP, SoundCategory.BLOCKS, 1f, 1f)
            return newState
        }

        // 2. Attempt to scrape oxidizable blocks (copper)
        Oxidizable.getDecreasedOxidationState(state).orElse(null)?.let { newState ->
            world.playSound(player, pos, SoundEvents.ITEM_AXE_SCRAPE, SoundCategory.BLOCKS, 1f, 1f)
            world.syncWorldEvent(player, 3005, pos, 0) // Visual effect for scraping
            return newState
        }

        // 3. Attempt to dewax waxed blocks
        HoneycombItem.WAXED_TO_UNWAXED_BLOCKS.get()[state.block]
            ?.getStateWithProperties(state)
            ?.let { newState ->
                world.playSound(player, pos, SoundEvents.ITEM_AXE_WAX_OFF, SoundCategory.BLOCKS, 1f, 1f)
                world.syncWorldEvent(player, 3004, pos, 0) // Visual effect for dewaxing
                return newState
            }

        return null
    }

    /**
     * Determines the stripped equivalent of a given block state, preserving its axis property.
     *
     * @param state The block state to check.
     * @return The stripped [BlockState] if one exists, otherwise `null`.
     */
    private fun getStrippedState(state: BlockState): BlockState? =
        STRIPPED_BLOCKS[state.block]?.defaultState
            ?.with(PillarBlock.AXIS, state[PillarBlock.AXIS])

    /**
     * Checks if the stripping action should be cancelled, e.g., if an offhand item action takes precedence.
     * This is to prevent interference with actions like blocking with a shield.
     *
     * @param context The context of the item usage.
     * @return `true` if the stripping attempt should be cancelled, `false` otherwise.
     */
    private fun shouldCancelStripAttempt(context: ItemUsageContext): Boolean {
        val player = context.player ?: return false
        // This logic mirrors vanilla checks to prevent right-click actions when the offhand action is prioritized.
        return context.hand == Hand.MAIN_HAND &&
                player.offHandStack.contains(DataComponentTypes.BLOCKS_ATTACKS) &&
                !player.shouldCancelInteraction()
    }

    /**
     * Called after a block is successfully mined. This serves as the entry point for the vein mining logic.
     *
     * @param stack The [ItemStack] used for mining.
     * @param world The world the block was mined in.
     * @param state The state of the block that was mined.
     * @param pos The position of the mined block.
     * @param miner The entity that mined the block.
     * @return Always returns `true` after attempting the vein mine to indicate the action was handled.
     */
    override fun postMine(
        stack: ItemStack,
        world: World,
        state: BlockState,
        pos: BlockPos, // position of the first block
        miner: LivingEntity,
    ): Boolean {
        // First, call the superclass method to handle default tool damage and stats.
        val initialResult = super.postMine(stack, world, state, pos, miner)

        val toolData = stack.get(DataComponentTypes.TOOL)

        // Execute vein mining only on the server, if the item has tool data, and the initial block wasn't instantly breakable.
        if (world.isClient || state.getHardness(world, pos) <= 0.0f || toolData == null) {
            return initialResult // Don't process AoE on client or for trivial blocks
        }

        // Trigger the main vein mining logic.
        performVeinMining(world, pos, state.block, miner, stack, toolData)

        return true
    }

    /**
     * Performs vein mining using a breadth-first search (BFS) algorithm to find and mine connected blocks of the same type.
     * The search starts from the neighbors of the initially broken block.
     *
     * @param world The world where mining takes place.
     * @param startPos The position of the first block that was broken by the player.
     * @param targetBlock The type of block to search for in the vein.
     * @param miner The entity that mined the block.
     * @param stack The [ItemStack] being used.
     * @param toolData The tool component data from the item stack.
     */
    private fun performVeinMining(
        world: World,
        startPos: BlockPos,
        targetBlock: Block,
        miner: LivingEntity,
        stack: ItemStack,
        toolData: ToolComponent
    ) {
        val visitedPositions = mutableSetOf<BlockPos>()
        val positionsToCheck = ArrayDeque<BlockPos>() // Use ArrayDeque as a queue for efficiency

        // Start the search from the direct neighbors of the initial block.
        NEIGHBOR_OFFSETS.forEach { offset ->
            positionsToCheck.add(startPos.add(offset))
        }
        visitedPositions.add(startPos)

        var blocksMinedCount = 0

        // Process positions from the queue until it's empty or the max vein size is reached.
        while (positionsToCheck.isNotEmpty() && blocksMinedCount < MAX_VEIN_SIZE) {
            val currentPos = positionsToCheck.removeFirst()

            // Skip positions we have already processed and blocks that are not vein mineable.
            if (!visitedPositions.add(currentPos) ||
                (world.getBlockState(currentPos).block !is ExperienceDroppingBlock && effectiveBlocks == BlockTags.PICKAXE_MINEABLE)) {
                continue
            }

            // Check if the block at this position is a valid part of the vein.
            if (isValidVeinBlock(world, currentPos, targetBlock, toolData)) {
                // Mine the block and increment the counter.
                tryBreakBlock(currentPos, world, miner, stack, toolData)
                blocksMinedCount++

                // Add all neighbors of the newly mined block to the queue to continue the search.
                NEIGHBOR_OFFSETS.forEach { offset ->
                    val neighborPos = currentPos.add(offset)
                    if (neighborPos !in visitedPositions) {
                        positionsToCheck.addLast(neighborPos)
                    }
                }
            }
        }
    }

    /**
     * Checks if a block at a given position is a valid target for the vein mining operation.
     * A block is valid if it's the same type as the original and the tool is suitable for mining it.
     *
     * @param world The current world.
     * @param pos The position of the block to check.
     * @param targetBlock The block type of the original vein.
     * @param toolData The tool component for checking mining suitability.
     * @return `true` if the block can be vein-mined, `false` otherwise.
     */
    private fun isValidVeinBlock(
        world: World,
        pos: BlockPos,
        targetBlock: Block,
        toolData: ToolComponent
    ): Boolean {
        val blockState = world.getBlockState(pos)

        // The block must be the same type as the one originally mined.
        if (blockState.block != targetBlock) {
            return false
        }

        // Delegate to the superclass for general suitability checks (e.g., tool level, hardness).
        return isSuitableForMining(blockState, world, pos, toolData)
    }
}