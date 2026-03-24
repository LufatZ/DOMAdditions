package de.additions.items

import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.component.Tool
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.tags.BlockTags
import net.minecraft.tags.TagKey
import net.minecraft.sounds.SoundSource
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.InteractionResult
import net.minecraft.world.InteractionHand
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.item.HoneycombItem
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.ToolMaterial
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.DropExperienceBlock
import net.minecraft.world.level.block.RotatedPillarBlock
import net.minecraft.world.level.block.WeatheringCopper
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.gameevent.GameEvent

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
    settings: Properties,
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
    override fun useOn(context: UseOnContext): InteractionResult {
        // Only perform AoE stripping/scraping if the tool is an axe.
        if (effectiveBlocks != BlockTags.MINEABLE_WITH_AXE) return super.useOn(context)

        val world = context.level
        val player = context.player ?: return InteractionResult.PASS
        val pos = context.clickedPos
        val stack = context.itemInHand

        // Prevent action if certain offhand conditions are met.
        if (shouldCancelStripAttempt(context)) return InteractionResult.PASS

        val facing = player.nearestViewDirection

        /**
         * Helper function to attempt stripping/scraping a single block at the target position.
         * @param targetPos The position of the block to interact with.
         */
        fun handleBlockAt(targetPos: BlockPos) {
            tryStrip(world, targetPos, player, world.getBlockState(targetPos))?.let { newState ->
                world.setBlock(targetPos, newState, 11) // Set block state with updates
                world.gameEvent(GameEvent.BLOCK_CHANGE, targetPos, GameEvent.Context.of(player, newState))
                stack.hurtAndBreak(1, player, context.hand)
            }
        }

        when (facing) {
            Direction.UP, Direction.DOWN -> {
                for (dx in -RADIUS..RADIUS) {
                    for (dz in -RADIUS..RADIUS) {
                        if ((dx != 0 || dz != 0) && dx * dx + dz * dz <= RADIUS * RADIUS) {
                            handleBlockAt(pos.offset(dx, 0, dz))
                        }
                    }
                }
            }
            Direction.NORTH, Direction.SOUTH -> {
                for (dx in -RADIUS..RADIUS) {
                    for (dy in -RADIUS..RADIUS) {
                        if ((dx != 0 || dy != 0) && dx * dx + dy * dy <= RADIUS * RADIUS) {
                            handleBlockAt(pos.offset(dx, dy, 0))
                        }
                    }
                }
            }
            Direction.EAST, Direction.WEST -> {
                for (dz in -RADIUS..RADIUS) {
                    for (dy in -RADIUS..RADIUS) {
                        if ((dz != 0 || dy != 0) && dz * dz + dy * dy <= RADIUS * RADIUS) {
                            handleBlockAt(pos.offset(0, dy, dz))
                        }
                    }
                }
            }
            else -> Unit
        }

        // Also apply the action to the center block that was initially clicked.
        tryStrip(world, pos, player, world.getBlockState(pos))?.let { newState ->
            world.setBlock(pos, newState, 11)
            world.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, newState))
            stack.hurtAndBreak(1, player, context.hand)
        }

        return InteractionResult.SUCCESS
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
    private fun tryStrip(world: Level, pos: BlockPos, player: Player?, state: BlockState): BlockState? {
        // 1. Attempt to strip log/wood
        getStrippedState(state)?.let { newState ->
            world.playSound(player, pos, SoundEvents.AXE_STRIP, SoundSource.BLOCKS, 1f, 1f)
            return newState
        }

        // 2. Attempt to scrape oxidizable blocks (copper)
        WeatheringCopper.getPrevious(state).orElse(null)?.let { newState ->
            world.playSound(player, pos, SoundEvents.AXE_SCRAPE, SoundSource.BLOCKS, 1f, 1f)
            world.levelEvent(player, 3005, pos, 0) // Visual effect for scraping
            return newState
        }

        // 3. Attempt to dewax waxed blocks
        HoneycombItem.WAX_OFF_BY_BLOCK.get()[state.block]
            ?.withPropertiesOf(state)
            ?.let { newState ->
                world.playSound(player, pos, SoundEvents.AXE_WAX_OFF, SoundSource.BLOCKS, 1f, 1f)
                world.levelEvent(player, 3004, pos, 0) // Visual effect for dewaxing
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
        STRIPPED_BLOCKS[state.block]?.defaultBlockState()
            ?.setValue(RotatedPillarBlock.AXIS, state.getValue(RotatedPillarBlock.AXIS))

    /**
     * Checks if the stripping action should be cancelled, e.g., if an offhand item action takes precedence.
     * This is to prevent interference with actions like blocking with a shield.
     *
     * @param context The context of the item usage.
     * @return `true` if the stripping attempt should be cancelled, `false` otherwise.
     */
    private fun shouldCancelStripAttempt(context: UseOnContext): Boolean {
        val player = context.player ?: return false
        // This logic mirrors vanilla checks to prevent right-click actions when the offhand action is prioritized.
        return context.hand == InteractionHand.MAIN_HAND &&
                player.offhandItem.has(DataComponents.BLOCKS_ATTACKS) &&
                !player.isSecondaryUseActive
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
    override fun mineBlock(
        stack: ItemStack,
        world: Level,
        state: BlockState,
        pos: BlockPos, // position of the first block
        miner: LivingEntity,
    ): Boolean {
        // First, call the superclass method to handle default tool damage and stats.
        val initialResult = super.mineBlock(stack, world, state, pos, miner)

        val toolData = stack.get(DataComponents.TOOL)

        val isCreative = miner is Player && miner.isCreative

        if (world.isClientSide || toolData == null) return initialResult

        if (!isCreative && state.getDestroySpeed(world, pos) <= 0.0f) return initialResult

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
        world: Level,
        startPos: BlockPos,
        targetBlock: Block,
        miner: LivingEntity,
        stack: ItemStack,
        toolData: Tool
    ) {
        val visitedPositions = mutableSetOf<BlockPos>()
        val positionsToCheck = ArrayDeque<BlockPos>() // Use ArrayDeque as a queue for efficiency

        // Start the search from the direct neighbors of the initial block.
        NEIGHBOR_OFFSETS.forEach { offset ->
            positionsToCheck.add(startPos.offset(offset))
        }
        visitedPositions.add(startPos)

        var blocksMinedCount = 0

        // Process positions from the queue until it's empty or the max vein size is reached.
        while (positionsToCheck.isNotEmpty() && blocksMinedCount < MAX_VEIN_SIZE) {
            val currentPos = positionsToCheck.removeFirst()

            // Skip positions we have already processed and blocks that are not vein mineable.
            if (!visitedPositions.add(currentPos) ||
                (world.getBlockState(currentPos).block !is DropExperienceBlock && effectiveBlocks == BlockTags.MINEABLE_WITH_PICKAXE)) {
                continue
            }

            // Check if the block at this position is a valid part of the vein.
            if (isValidVeinBlock(world, currentPos, targetBlock, miner, toolData)) {
                // Mine the block and increment the counter.
                tryBreakBlock(currentPos, world, miner, stack, toolData)
                blocksMinedCount++

                // Add all neighbors of the newly mined block to the queue to continue the search.
                NEIGHBOR_OFFSETS.forEach { offset ->
                    val neighborPos = currentPos.offset(offset)
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
        world: Level,
        pos: BlockPos,
        targetBlock: Block,
        miner: LivingEntity,
        toolData: Tool
    ): Boolean {
        val blockState = world.getBlockState(pos)

        if (blockState.block != targetBlock) return false

        if (miner is Player && miner.isCreative) return true

        return isSuitableForMining(blockState, world, pos, toolData)
    }
}