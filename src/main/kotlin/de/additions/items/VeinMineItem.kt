package de.additions.items

import de.additions.Additions.logger
import de.additions.datagen.BlockTagGenerator
import net.minecraft.core.BlockPos
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.Identifier
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.tags.BlockTags
import net.minecraft.tags.TagKey
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.HoneycombItem
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.ToolMaterial
import net.minecraft.world.item.component.Tool
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.RotatedPillarBlock
import net.minecraft.world.level.block.WeatheringCopper
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.gameevent.GameEvent

/**
 * A specialized tool item that implements vein mining mechanics, allowing a single block
 * break to trigger the destruction of connected blocks of the same type.
 *
 * This item supports both axes-like functionality (for stripping logs or removing wax)
 * and pickaxe-like functionality (for mining ore veins). It uses Breadth-First Search
 * (BFS) to traverse adjacent blocks within a defined radius based on the tool's material.
 *
 * @param material The tool material defining durability and effectiveness.
 * @param effectiveBlocks A tag key specifying which blocks this tool can interact with.
 * @param settings The base properties for the item.
 */
class VeinMineItem(
    material: ToolMaterial,
    effectiveBlocks: TagKey<Block>,
    settings: Properties,
) : ToolItem(material, effectiveBlocks, settings) {


    /**
     * Holds configuration constants and spatial coordinate offsets used to identify
     * connected blocks during the vein mining process.
     */
    companion object {
        /**
         * The maximum number of connected blocks that can be processed in a single vein mining operation.
         */
        private const val MAX_VEIN_SIZE = 64

        /**
         * An array of relative positions representing the 26 adjacent blocks surrounding a central coordinate,
         * including all orthogonal and diagonal neighbors within a 3x3x3 grid excluding the origin.
         */
        private val NEIGHBORS_26: Array<BlockPos> = buildList {
            for (dx in -1..1) for (dy in -1..1) for (dz in -1..1) {
                if (dx == 0 && dy == 0 && dz == 0) continue
                add(BlockPos(dx, dy, dz))
            }
        }.toTypedArray()

        /**
         * An array of relative [BlockPos] offsets representing a 5x5x5 neighborhood around a central point,
         * excluding the origin (0, 0, 0).
         */
        private val NEIGHBORS_124: Array<BlockPos> = buildList {
            for (dx in -2..2) for (dy in -2..2) for (dz in -2..2) {
                if (dx == 0 && dy == 0 && dz == 0) continue
                add(BlockPos(dx, dy, dz))
            }
        }.toTypedArray()
    }


    /**
     * Executes a Breadth-First Search algorithm to traverse connected blocks within a level.
     *
     * @param world The level where the search is performed.
     * @param startPos The initial position to begin the traversal.
     * @param neighbors An array of offsets used to identify adjacent blocks for exploration.
     * @param matchFn A predicate function used to determine if a block at a specific position and state meets the criteria for processing.
     * @param actionFn A function that performs an action on matching blocks; returning false will terminate the entire search.
     * @param skipVisit If true, non-matching blocks are removed from the visited set to allow them to be re-evaluated during traversal.
     */
    private fun runBfs(
        world: Level,
        startPos: BlockPos,
        neighbors: Array<BlockPos>,
        matchFn: (BlockPos, BlockState) -> Boolean,
        actionFn: (BlockPos, BlockState) -> Boolean,
        skipVisit: Boolean = false,
    ) {
        val visited = mutableSetOf(startPos)
        val queue = ArrayDeque<BlockPos>()
        NEIGHBORS_26.forEach { queue.add(startPos.offset(it)) }

        var count = 0

        while (queue.isNotEmpty() && count < MAX_VEIN_SIZE) {
            val pos = queue.removeFirst()
            if (!visited.add(pos)) continue

            val state = world.getBlockState(pos)

            if (!matchFn(pos, state)) {
                if (skipVisit) visited.remove(pos)
                continue
            }

            if (!actionFn(pos, state)) return
            count++

            neighbors.forEach { offset ->
                val n = pos.offset(offset)
                if (n !in visited) queue.addLast(n)
            }
        }
    }

    /**
     * Performs an axe-based interaction on the targeted block and its connected neighbors of the same type.
     *
     * This method checks if the clicked block is mineable with an axe. If so, it attempts to apply
     * an axe action to the block and then uses a breadth-first search to find and apply the same
     * action to all adjacent blocks of the same type. The item's durability is reduced for every
     * block modified.
     *
     * @param context The interaction context containing information about the player, world, position, and held item.
     * @return An [InteractionResult] representing the outcome of the interaction.
     */
    override fun useOn(context: UseOnContext): InteractionResult {
        if (effectiveBlocks != BlockTags.MINEABLE_WITH_AXE) return super.useOn(context)

        val world = context.level
        val player = context.player ?: return InteractionResult.PASS
        val pos = context.clickedPos
        val stack = context.itemInHand
        val clickedState = world.getBlockState(pos)

        if (shouldCancelStripAttempt(context)) return InteractionResult.PASS

        val firstNewState = tryAxeAction(world, pos, player, clickedState)
            ?: return super.useOn(context)

        world.setBlock(pos, firstNewState, 11)
        world.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, firstNewState))
        stack.hurtAndBreak(1, player, context.hand)

        val targetBlock = clickedState.block

        runBfs(
            world, pos, NEIGHBORS_26,
            matchFn = { _, state -> state.block == targetBlock },
            actionFn = { currentPos, currentState ->
                val newState = tryAxeAction(world, currentPos, player, currentState) ?: return@runBfs true
                world.setBlock(currentPos, newState, 11)
                world.gameEvent(GameEvent.BLOCK_CHANGE, currentPos, GameEvent.Context.of(player, newState))
                stack.hurtAndBreak(1, player, context.hand)
                true
            }
        )

        return InteractionResult.SUCCESS
    }

    /**
     * Attempts to perform an axe-related action on a block, such as stripping logs,
     * scraping weathered copper, or removing wax from a block state.
     *
     * @param world The level where the action is occurring.
     * @param pos The position of the block being acted upon.
     * @param player The player performing the action, or null if no player is involved.
     * @param state The current block state at the given position.
     * @return The new block state if an action was successfully applied, or null if no action could be performed.
     */
    private fun tryAxeAction(world: Level, pos: BlockPos, player: Player?, state: BlockState): BlockState? {
        getStrippedState(state)?.let { newState ->
            world.playSound(player, pos, SoundEvents.AXE_STRIP, SoundSource.BLOCKS, 1f, 1f)
            return newState
        }
        WeatheringCopper.getPrevious(state).orElse(null)?.let { newState ->
            world.playSound(player, pos, SoundEvents.AXE_SCRAPE, SoundSource.BLOCKS, 1f, 1f)
            world.levelEvent(player, 3005, pos, 0)
            return newState
        }
        HoneycombItem.WAX_OFF_BY_BLOCK.get()[state.block]
            ?.withPropertiesOf(state)
            ?.let { newState ->
                world.playSound(player, pos, SoundEvents.AXE_WAX_OFF, SoundSource.BLOCKS, 1f, 1f)
                world.levelEvent(player, 3004, pos, 0)
                return newState
            }
        return null
    }

    /**
     * Attempts to find and return a stripped version of the provided block state, maintaining
     * the same axis property if the resulting block supports it.
     *
     * @param state The original block state to be converted to its stripped variant.
     * @return The stripped block state with the preserved axis, or null if no stripped version
     * is registered or the transformation cannot be completed.
     */
    private fun getStrippedState(state: BlockState): BlockState? {
        val key = BuiltInRegistries.BLOCK.getKey(state.block)
        val strippedKey = Identifier.fromNamespaceAndPath(key.namespace, "stripped_${key.path}")
        val strippedBlock = BuiltInRegistries.BLOCK.getValue(strippedKey)

        return try {
            strippedBlock.defaultBlockState()
                .setValue(RotatedPillarBlock.AXIS, state.getValue(RotatedPillarBlock.AXIS))
        } catch (_: IllegalArgumentException) {
            null
        }
    }

    /**
     * Determines whether the attempt to strip a block should be cancelled based on the current interaction context.
     *
     * @param context The interaction context containing the player and hand information.
     * @return True if the strip attempt should be cancelled; false otherwise.
     */
    private fun shouldCancelStripAttempt(context: UseOnContext): Boolean {
        val player = context.player ?: return false
        return context.hand == InteractionHand.MAIN_HAND
                && player.offhandItem.has(DataComponents.BLOCKS_ATTACKS)
                && !player.isSecondaryUseActive
    }


    /**
     * Overrides the standard block mining process to implement vein mining functionality.
     *
     * This method executes the base mining logic and then checks if the tool stack contains
     * valid tool data. If the operation is on the server side and the criteria for vein
     * mining are met, it triggers the vein mining effect.
     *
     * @param stack The item stack being used to mine the block.
     * @param world The level in which the mining occurs.
     * @param state The state of the block being mined.
     * @param pos The position of the block being mined.
     * @param miner The entity performing the mining action.
     * @return True if vein mining was triggered, otherwise the result from the super implementation.
     */
    override fun mineBlock(
        stack: ItemStack,
        world: Level,
        state: BlockState,
        pos: BlockPos,
        miner: LivingEntity,
    ): Boolean {
        val result = super.mineBlock(stack, world, state, pos, miner)

        val toolData = stack.get(DataComponents.TOOL) ?: return result
        if (world.isClientSide) return result
        if (miner !is Player && state.getDestroySpeed(world, pos) <= 0f) return result

        performVeinMining(world, pos, state.block, miner, stack, toolData)
        return true
    }

    /**
     * Executes a vein mining operation starting from a specific position, expanding to connected blocks
     * of the same type using a Breadth-First Search algorithm. The process considers tool efficiency,
     * block suitability, and handles block destruction and tool durability degradation.
     *
     * @param world The level where the mining operation takes place.
     * @param startPos The initial position to begin the vein mining search.
     * @param targetBlock The specific block type to look for and mine.
     * @param miner The entity performing the mining action.
     * @param stack The item stack being used as a tool, which will be damaged during the process.
     * @param toolData The data component of the tool containing damage and mining properties.
     */
    private fun performVeinMining(
        world: Level,
        startPos: BlockPos,
        targetBlock: Block,
        miner: LivingEntity,
        stack: ItemStack,
        toolData: Tool,
    ) {
        val isCreative = miner is Player && miner.isCreative
        val isPickaxe = effectiveBlocks == BlockTags.MINEABLE_WITH_PICKAXE
        val neighbors = if (isPickaxe) NEIGHBORS_124 else NEIGHBORS_26

        runBfs(
            world, startPos, neighbors,
            matchFn = { pos, state ->
                if (isPickaxe && !isOreBlock(state)) return@runBfs false
                state.block == targetBlock && isSuitableForMining(state, world, pos, toolData)
            },
            actionFn = { pos, _ ->
                val broken = world.destroyBlock(pos, !isCreative, miner)
                if (broken && !isCreative) {
                    stack.hurtWithoutBreaking(toolData.damagePerBlock(), miner as Player)
                }
                !stack.isEmpty // stop if the tool just broke
            },
            skipVisit = isPickaxe,
        )
    }

    /**
     * A set containing the identifiers of fallback ore types that have already been processed,
     * used to prevent redundant logging or duplicate processing during mining operations.
     */
    private val loggedFallbackOres = mutableSetOf<String>()

    /**
     * Determines whether a given block state qualifies as an ore block.
     *
     * The check identifies ores by verifying if the block belongs to the predefined OresTag
     * or if its registry key path ends with the "_ore" suffix.
     *
     * @param state The block state to evaluate.
     * @return True if the block is identified as an ore via tags or naming convention, false otherwise.
     */
    private fun isOreBlock(state: BlockState): Boolean {
        if (state.`is`(BlockTagGenerator.OresTag)) return true

        val key = BuiltInRegistries.BLOCK.getKey(state.block) ?: return false
        if (key.path.endsWith("_ore")) {
            if (loggedFallbackOres.add(key.toString())) {
                logger.debug(
                    "VeinMineItem: '{}' matched via naming convention. " +
                            "Add it to 'additions:ores' for cleaner compatibility.", key
                )
            }
            return true
        }
        return false
    }
}