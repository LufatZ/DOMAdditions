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
 * Represents a custom mining tool that breaks a vein of connected blocks of the same type.
 * When a block is mined, it recursively searches for and breaks adjacent blocks of the same type, up to a certain limit.
 * This item also includes vein-style functionality for right-click actions like stripping logs if it's an axe.
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
        /** The maximum number of blocks that can be processed in a single vein operation (mining or stripping). */
        private const val MAX_VEIN_SIZE = 64

        /**
         * An array of [BlockPos] offsets representing all 26 neighboring positions in a 3x3x3 cube
         * surrounding a central block. Used to find adjacent blocks for the vein algorithm.
         */
        private val NEIGHBOR_OFFSETS: Array<BlockPos> = buildList {
            for (dx in -1..1) {
                for (dy in -1..1) {
                    for (dz in -1..1) {
                        if (dx == 0 && dy == 0 && dz == 0) continue
                        add(BlockPos(dx, dy, dz))
                    }
                }
            }
        }.toTypedArray()
    }

    /**
     * Dynamically determines the stripped variant of a block based on naming conventions.
     * This approach improves mod compatibility by not requiring hardcoded mappings for all wood types.
     *
     * @param block The original block to check for a stripped variant.
     * @return The stripped [Block] if found, otherwise `null`.
     */
    private fun getStrippedVariant(block: Block): Block? {
        val key = BuiltInRegistries.BLOCK.getKey(block) ?: return null
        val strippedKey = Identifier.fromNamespaceAndPath(key.namespace, "stripped_${key.path}")
        return BuiltInRegistries.BLOCK.getValue(strippedKey)
    }

    /**
     * Overrides the default right-click behavior to apply axe actions (stripping, scraping, de-waxing)
     * using the same BFS vein algorithm as block mining, instead of a radius-based AoE.
     * Only processes connected blocks of the exact same type as the clicked block.
     *
     * @param context The context in which the item was used.
     * @return [InteractionResult.SUCCESS] if any vein action was performed, otherwise delegates to the parent.
     */
    override fun useOn(context: UseOnContext): InteractionResult {
        if (effectiveBlocks != BlockTags.MINEABLE_WITH_AXE) return super.useOn(context)

        val world = context.level
        val player = context.player ?: return InteractionResult.PASS
        val pos = context.clickedPos
        val stack = context.itemInHand
        val clickedState = world.getBlockState(pos)

        if (shouldCancelStripAttempt(context)) return InteractionResult.PASS

        // Only proceed if the clicked block itself can actually be stripped/scraped/dewaxed.
        // Log a warning for mod compatibility if a stripped variant exists in the registry but
        // doesn't support the AXIS property — this indicates a misconfigured or incompatible block.
        if (tryStrip(world, pos, player, clickedState) == null) {
            val key = BuiltInRegistries.BLOCK.getKey(clickedState.block)
            val strippedKey = Identifier.fromNamespaceAndPath(key.namespace, "stripped_${key.path}")
            logger.warn(
                "VeinMineItem: Found stripped variant '$strippedKey' for block '$key' " +
                "but could not apply strip action (missing AXIS property or unsupported block type). " +
                "This may indicate a mod compatibility issue."
            )
            return super.useOn(context)
        }

        // Apply the action to the clicked block first.
        tryStrip(world, pos, player, clickedState)?.let { newState ->
            world.setBlock(pos, newState, 11)
            world.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, newState))
            stack.hurtAndBreak(1, player, context.hand)
        }

        // BFS over connected blocks of the exact same type.
        val targetBlock = clickedState.block
        val visitedPositions = mutableSetOf<BlockPos>()
        val positionsToCheck = ArrayDeque<BlockPos>()

        NEIGHBOR_OFFSETS.forEach { offset -> positionsToCheck.add(pos.offset(offset)) }
        visitedPositions.add(pos)

        var blocksStrippedCount = 0

        while (positionsToCheck.isNotEmpty() && blocksStrippedCount < MAX_VEIN_SIZE) {
            val currentPos = positionsToCheck.removeFirst()

            if (!visitedPositions.add(currentPos)) continue

            val currentState = world.getBlockState(currentPos)

            // Only process blocks of the exact same type as the originally clicked block.
            if (currentState.block != targetBlock) continue

            tryStrip(world, currentPos, player, currentState)?.let { newState ->
                world.setBlock(currentPos, newState, 11)
                world.gameEvent(GameEvent.BLOCK_CHANGE, currentPos, GameEvent.Context.of(player, newState))
                stack.hurtAndBreak(1, player, context.hand)
                blocksStrippedCount++

                NEIGHBOR_OFFSETS.forEach { offset ->
                    val neighborPos = currentPos.offset(offset)
                    if (neighborPos !in visitedPositions) {
                        positionsToCheck.addLast(neighborPos)
                    }
                }
            }
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
            world.levelEvent(player, 3005, pos, 0)
            return newState
        }

        // 3. Attempt to dewax waxed blocks
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
     * Determines the stripped equivalent of a given block state, preserving its axis property.
     * Uses dynamic registry lookup for improved mod compatibility.
     * Only works on blocks that have the ROTATED_PILLAR property (logs, wood, stems).
     * The [pos] parameter was removed as it was only used for logging, which caused log spam
     * when called proactively during BFS neighbor checks.
     *
     * @param state The block state to check.
     * @return The stripped [BlockState] if one exists and supports axis, otherwise `null`.
     */
    private fun getStrippedState(state: BlockState): BlockState? {
        val strippedBlock = getStrippedVariant(state.block) ?: return null

        return try {
            strippedBlock.defaultBlockState()
                .setValue(RotatedPillarBlock.AXIS, state.getValue(RotatedPillarBlock.AXIS))
        } catch (e: IllegalArgumentException) {
            // Block doesn't support the AXIS property — not a strippable pillar block.
            null
        }
    }

    /**
     * Checks if the stripping action should be cancelled, e.g., if an offhand item action takes precedence.
     *
     * @param context The context of the item usage.
     * @return `true` if the stripping attempt should be cancelled, `false` otherwise.
     */
    private fun shouldCancelStripAttempt(context: UseOnContext): Boolean {
        val player = context.player ?: return false
        return context.hand == InteractionHand.MAIN_HAND &&
                player.offhandItem.has(DataComponents.BLOCKS_ATTACKS) &&
                !player.isSecondaryUseActive
    }

    /**
     * Called after a block is successfully mined. Entry point for the vein mining logic.
     *
     * @param stack The [ItemStack] used for mining.
     * @param world The world the block was mined in.
     * @param state The state of the block that was mined.
     * @param pos The position of the mined block.
     * @param miner The entity that mined the block.
     * @return Always returns `true` after attempting the vein mine.
     */
    override fun mineBlock(
        stack: ItemStack,
        world: Level,
        state: BlockState,
        pos: BlockPos,
        miner: LivingEntity,
    ): Boolean {
        val initialResult = super.mineBlock(stack, world, state, pos, miner)

        val toolData = stack.get(DataComponents.TOOL)
        val isCreative = miner is Player && miner.isCreative

        if (world.isClientSide || toolData == null) return initialResult
        if (!isCreative && state.getDestroySpeed(world, pos) <= 0.0f) return initialResult

        performVeinMining(world, pos, state.block, miner, stack, toolData)

        return true
    }

    /**
     * Checks whether a block qualifies as an ore for vein mining purposes.
     *
     * Uses a two-stage approach for maximum mod compatibility:
     * 1. Primary: checks against [BlockTagGenerator.OresTag] (`additions:ores`), which bundles all
     *    vanilla ore tags and common cross-mod convention tags. Other mods can extend this tag
     *    in their own data to be automatically supported.
     * 2. Fallback: checks if the block's registry name ends with `_ore`, catching mod-added ores
     *    that are not yet tagged (e.g. from mods that don't follow tag conventions).
     *    Logs a debug message when the fallback triggers, to aid in identifying untagged ores.
     *
     * @param state The [BlockState] to check.
     * @param world The current world, used for tag lookups.
     * @return `true` if the block should be considered an ore for vein mining.
     */
    private fun isOreBlock(state: BlockState, world: Level): Boolean {
        if (state.`is`(BlockTagGenerator.OresTag)) return true

        // Fallback: naming convention check for untagged mod ores.
        val key = BuiltInRegistries.BLOCK.getKey(state.block)
        if (key.path.endsWith("_ore")) {
            logger.debug(
                "VeinMineItem: Block '${key}' matched ore fallback via naming convention. " +
                "Consider adding it to the 'additions:ores' tag or the '${key.namespace}:ores' tag for better compatibility."
            )
            return true
        }

        return false
    }

    /**
     * Performs vein mining using a BFS algorithm to find and mine connected blocks of the same type.
     *
     * For pickaxe tools, a gap of up to 2 blocks between ore blocks is tolerated to account for
     * Minecraft's blob-style ore generation, where individual ores can be separated by stone.
     * Non-ore blocks within the gap are skipped without being added to visitedPositions, so they
     * can still be reached via a different path from a neighbouring ore block.
     * For other tools, only directly adjacent blocks (1 block gap) are considered.
     *
     * @param world The world where mining takes place.
     * @param startPos The position of the first block broken by the player.
     * @param targetBlock The block type to search for in the vein.
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
        toolData: Tool,
    ) {
        val isPickaxe = effectiveBlocks == BlockTags.MINEABLE_WITH_PICKAXE
        val visitedPositions = mutableSetOf<BlockPos>()
        val positionsToCheck = ArrayDeque<BlockPos>()

        NEIGHBOR_OFFSETS.forEach { offset -> positionsToCheck.add(startPos.offset(offset)) }
        visitedPositions.add(startPos)

        var blocksMinedCount = 0

        while (positionsToCheck.isNotEmpty() && blocksMinedCount < MAX_VEIN_SIZE) {
            val currentPos = positionsToCheck.removeFirst()

            if (!visitedPositions.add(currentPos)) continue

            // For pickaxe, skip non-ore blocks without marking them visited, so they can still be
            // reached via a different neighbouring ore block during BFS expansion.
            if (isPickaxe && !isOreBlock(world.getBlockState(currentPos), world)) {
                visitedPositions.remove(currentPos)
                continue
            }

            if (isValidVeinBlock(world, currentPos, targetBlock, miner, toolData)) {
                tryBreakBlock(currentPos, world, miner, stack, toolData)
                blocksMinedCount++

                // For pickaxes, enqueue neighbors within a 5x5x5 area (2 block gap tolerance)
                // to account for blob-style ore generation where ores can be separated by stone.
                // For other tools, only enqueue the 26 direct neighbors (1 block gap).
                if (isPickaxe) {
                    for (dx in -2..2) {
                        for (dy in -2..2) {
                            for (dz in -2..2) {
                                if (dx == 0 && dy == 0 && dz == 0) continue
                                val neighborPos = currentPos.offset(dx, dy, dz)
                                if (neighborPos !in visitedPositions) {
                                    positionsToCheck.addLast(neighborPos)
                                }
                            }
                        }
                    }
                } else {
                    NEIGHBOR_OFFSETS.forEach { offset ->
                        val neighborPos = currentPos.offset(offset)
                        if (neighborPos !in visitedPositions) {
                            positionsToCheck.addLast(neighborPos)
                        }
                    }
                }
            }
        }
    }

    /**
     * Checks if a block at a given position is a valid target for the vein mining operation.
     * A block is valid if it is the exact same type as the original and the tool is suitable.
     *
     * @param world The current world.
     * @param pos The position of the block to check.
     * @param targetBlock The block type of the original vein.
     * @param miner The entity performing the mining.
     * @param toolData The tool component for checking mining suitability.
     * @return `true` if the block can be vein-mined, `false` otherwise.
     */
    private fun isValidVeinBlock(
        world: Level,
        pos: BlockPos,
        targetBlock: Block,
        miner: LivingEntity,
        toolData: Tool,
    ): Boolean {
        val blockState = world.getBlockState(pos)

        if (blockState.block != targetBlock) return false
        //if (miner is Player && miner.isCreative) return true

        return isSuitableForMining(blockState, world, pos, toolData)
    }
}