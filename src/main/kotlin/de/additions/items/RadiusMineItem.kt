@file:Suppress("ktlint:standard:no-wildcard-imports")

package de.additions.items

import de.additions.blocks.BlockRegistry
import de.additions.blocks.BlockRegistry.DIRT_PATH_SLAB
import de.additions.blocks.BlockRegistry.DIRT_PATH_STAIR
import de.additions.datagen.BlockTagGenerator
import de.additions.items.RadiusMineItem.Companion.RADIUS
import net.minecraft.block.*
import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.ToolComponent
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUsageContext
import net.minecraft.item.ToolMaterial
import net.minecraft.registry.tag.BlockTags
import net.minecraft.registry.tag.TagKey
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.util.ActionResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World
import net.minecraft.world.event.GameEvent
import java.util.*

/**
 * Represents a custom mining tool that breaks blocks in a defined radius (AoE) around the initially mined block.
 * It also includes AoE functionality similar to a shovel for creating paths.
 *
 * Inherits from [Item] and relies on the [ToolComponent] (and potentially others like AttributeModifiers)
 * being correctly configured via [Item.Settings] during item registration (e.g., using `.pickaxe()`, `.shovel()`, or `.tool()` helpers).
 *
 * @property material The [ToolMaterial] defining base properties. Used here for helpers and potentially passed to Settings during registration.
 * @property effectiveBlocks A [TagKey]<[Block]> specifying which blocks the AoE mining effect applies to.
 * Should generally match the tag used when configuring the [ToolComponent] (e.g., [BlockTags.PICKAXE_MINEABLE]).
 * @param settings The base [Item.Settings] for this item. MUST be pre-configured with appropriate components
 * (like [DataComponentTypes.TOOL], [DataComponentTypes.ATTRIBUTE_MODIFIERS]) for base tool functionality.
 */
class RadiusMineItem(material: ToolMaterial,
    effectiveBlocks: TagKey<Block>,
    settings: Settings,
) : ToolItem(material, effectiveBlocks, settings) {
companion object {
    /** The radius for the Area of Effect (AoE) mining and path creation (0=1x1, 1=3x3, 2=5x5 -> 5x5 area). */
    const val RADIUS = 2

    /** Cooldown in Ticks (20 Ticks = 1 Second) for the path creation ability. */
    private const val PATH_CREATION_COOLDOWN = 2

    /** Map to store the last usage time of the path creation ability per player UUID. */
    private val lastPathCreationTime = mutableMapOf<UUID, Long>()
}
    /**
     * Called after a block is successfully mined. Implements the radius mining logic.
     * Breaks additional blocks within the defined [RADIUS] around the original block, in a plane relative to the miner's facing direction.
     *
     * @param stack The [ItemStack] used for mining.
     * @param world The [World] where mining occurred.
     * @param state The [BlockState] of the initially mined block.
     * @param pos The [BlockPos] of the initially mined block.
     * @param miner The [LivingEntity] who mined the block.
     * @return Boolean indicating if the mining action should proceed (super call handles initial damage).
     */
    override fun postMine(
        stack: ItemStack,
        world: World,
        state: BlockState,
        pos: BlockPos, // Position of the *first* block broken
        miner: LivingEntity,
    ): Boolean {
        // super.postMine() handles damage for the *initial* block based on ToolComponent.damagePerBlock
        // It returns true if the block was successfully mined and damage was applied (or would be in survival).
        val initialResult = super.postMine(stack, world, state, pos, miner)

        val toolData = stack.get(DataComponentTypes.TOOL)

        // Execute AoE only on the server, if the item has tool data, and the initial block wasn't instantly breakable.
        if (world.isClient || state.getHardness(world, pos) <= 0.0f || toolData == null) {
            return initialResult // Don't process AoE on client or for trivial blocks
        }

        // Determine the AoE plane based on the direction the player is facing.
        when (miner.facing) {
            // Looking Up/Down: Mine in a horizontal (XZ) plane around the target block.
            Direction.UP, Direction.DOWN -> {
                for (dx in -RADIUS..RADIUS) {
                    for (dz in -RADIUS..RADIUS) {
                        // Skip the center block (dx=0, dz=0) - already mined
                        // Check if within the circular radius in the XZ plane
                        if ((dx != 0 || dz != 0) && dx * dx + dz * dz <= RADIUS * RADIUS) {
                            tryBreakBlock(pos.add(dx, 0, dz), world, miner, stack, toolData)
                        }
                    }
                }
            }
            // Looking North/South: Mine in a vertical (XY) plane around the target block.
            Direction.NORTH, Direction.SOUTH -> {
                for (dx in -RADIUS..RADIUS) {
                    for (dy in -RADIUS..RADIUS) {
                        // Skip the center block (dx=0, dy=0)
                        // Check if within the circular radius in the XY plane
                        if ((dx != 0 || dy != 0) && dx * dx + dy * dy <= RADIUS * RADIUS) {
                            tryBreakBlock(pos.add(dx, dy, 0), world, miner, stack, toolData)
                        }
                    }
                }
            }
            // Looking East/West: Mine in a vertical (YZ) plane around the target block.
            Direction.EAST, Direction.WEST -> {
                for (dz in -RADIUS..RADIUS) {
                    for (dy in -RADIUS..RADIUS) {
                        // Skip the center block (dz=0, dy=0)
                        // Check if within the circular radius in the YZ plane
                        if ((dz != 0 || dy != 0) && dz * dz + dy * dy <= RADIUS * RADIUS) {
                            tryBreakBlock(pos.add(0, dy, dz), world, miner, stack, toolData)
                        }
                    }
                }
            }
            // Should not happen with standard facing directions
            else -> Unit
        }

        // Return true because the item was used for mining (even if only the initial block was affected by super.postMine)
        // This ensures vanilla mechanics like stat tracking might count the usage.
        return true
    }

    /**
     * Called when the item is used on a block (right-click). Implements the radius path creation logic.
     * Turns suitable blocks (like dirt, grass) into dirt paths within the defined [RADIUS].
     *
     * @param context Provides context about the usage action (world, position, player, etc.).
     * @return [ActionResult] indicating whether the action was successful.
     */
    override fun useOnBlock(context: ItemUsageContext): ActionResult {
        val world = context.world
        val initialPos = context.blockPos
        val player = context.player ?: return ActionResult.PASS
        val stack = context.stack ?: return ActionResult.PASS // Need stack for damage

        // --- Cooldown Check ---
        val playerUuid = player.uuid
        val currentTime = world.time
        val lastUsage = lastPathCreationTime[playerUuid] ?: 0L
        if (currentTime - lastUsage < PATH_CREATION_COOLDOWN) {
            return ActionResult.PASS // Still in cooldown
        }

        // Define which blocks can be turned into paths
        val pathableFullBlocks = BlockTags.DIRT // Using the DIRT tag (includes grass, dirt, podzol, etc.)
        val pathableCustomBlocks = BlockTagGenerator.DirtLikeBlockTag
        val dirtCovertableBlocks = BlockTagGenerator.DirtPathVariantTag
        var changedSomething = false

        fun isInPathable(state: BlockState): Boolean = state.isIn(pathableFullBlocks) || state.isIn(pathableCustomBlocks)

        fun isInDirtCovertable(state: BlockState): Boolean = state.isIn(dirtCovertableBlocks)

        // Iterate through the horizontal plane defined by the RADIUS around the clicked block
        for (dx in -RADIUS..RADIUS) {
            for (dz in -RADIUS..RADIUS) {
                // Check if the position is within the circular radius in the XZ plane
                if (dx * dx + dz * dz <= RADIUS * RADIUS) {
                    val currentPos = initialPos.add(dx, 0, dz)
                    val targetState = world.getBlockState(currentPos)
                    val blockAboveState = world.getBlockState(currentPos.up())

                    // Conditions for creating a path:
                    // 1. Target block is pathable (e.g., in DIRT tag).
                    // 2. Space above is air.
                    if (isInPathable(targetState) && blockAboveState.isAir && !player.isSneaking) {
                        val targetBlock = targetState.block

                        // Determine the desired path state
                        val pathState: BlockState? =
                            when (targetBlock) {
                                // Check if target is already a custom path stair/slab
                                // this should not be necessary, but just in case
                                Blocks.DIRT_PATH -> null
                                DIRT_PATH_STAIR -> null
                                DIRT_PATH_SLAB -> null
                                // copy states from the target block
                                is StairsBlock -> DIRT_PATH_STAIR.getStateWithProperties(targetState)
                                is SlabBlock -> DIRT_PATH_SLAB.getStateWithProperties(targetState)
                                else -> Blocks.DIRT_PATH.defaultState // Default to full path block
                            }

                        // If a valid path state was determined and it's different
                        if (pathState != null) {
                            // Play sound before changing state
                            world.playSound(
                                player, // Play sound near the player triggering it
                                currentPos,
                                SoundEvents.ITEM_SHOVEL_FLATTEN,
                                SoundCategory.BLOCKS,
                            )

                            // Perform changes only on the server
                            if (!world.isClient) {
                                // Set the block state
                                world.setBlockState(currentPos, pathState, Block.NOTIFY_LISTENERS or Block.FORCE_STATE)

                                // Emit game event for observers (like sculk)
                                world.emitGameEvent(
                                    GameEvent.BLOCK_CHANGE,
                                    currentPos,
                                    GameEvent.Emitter.of(player, pathState), // Use player context
                                )

                                // Apply durability damage
                                stack.damage(1, player)
                                changedSomething = true
                            }
                        }
                    } else if (isInDirtCovertable(targetState) && player.isSneaking) {
                        // Check if the target block is a dirt-like block that can be converted
                        val newState =
                            when (targetState.block) {
                                is StairsBlock -> BlockRegistry.DIRT_STAIR.getStateWithProperties(targetState)
                                is SlabBlock -> BlockRegistry.DIRT_SLAB.getStateWithProperties(targetState)
                                else -> Blocks.DIRT.getStateWithProperties(targetState)
                            }

                        // Play sound before changing state
                        world.playSound(
                            player,
                            currentPos,
                            SoundEvents.ITEM_SHOVEL_FLATTEN,
                            SoundCategory.BLOCKS,
                        )

                        // Perform changes only on the server
                        if (!world.isClient) {
                            if (currentPos == player.blockPos) {
                                // push up the player if the replaced block is below them
                                player.teleport(player.x, player.y + 0.5, player.z, false)
                            }
                            // Set the block state
                            world.setBlockState(currentPos, newState, Block.NOTIFY_LISTENERS or Block.FORCE_STATE)

                            // Emit game event for observers (like sculk)
                            world.emitGameEvent(
                                GameEvent.BLOCK_CHANGE,
                                currentPos,
                                GameEvent.Emitter.of(player, newState), // Use player context
                            )

                            // Apply durability damage
                            stack.damage(1, player)
                            changedSomething = true
                        }
                    }
                }
            }
        }

        // If any block was changed server-side, update cooldown and return SUCCESS
        return if (changedSomething) {
            if (!world.isClient) { // Only update cooldown on server
                lastPathCreationTime[playerUuid] = currentTime
            }
            ActionResult.SUCCESS
        } else {
            ActionResult.PASS
        }
    }

    /**
     * Helper function to attempt breaking a block at a specific position during the AoE mining process.
     * Checks if the block is suitable and applies damage to the tool.
     *
     * @param targetPos The [BlockPos] of the block to potentially break.
     * @param world The current [World].
     * @param miner The [LivingEntity] performing the mining action.
     * @param stack The [ItemStack] being used.
     * @param toolData The [ToolComponent] containing tool properties like damage per block.
     */
    private fun tryBreakBlock(
        targetPos: BlockPos,
        world: World,
        miner: LivingEntity,
        stack: ItemStack,
        toolData: ToolComponent,
    ) {
        val targetState = world.getBlockState(targetPos)

        // Check if the block is suitable for AoE mining with this tool
        // Pass 'miner' to isSuitableForMining in case future checks need player abilities etc.
        if (isSuitableForMining(targetState, world, targetPos, toolData)) {
            // Break the block, triggering drops and effects (true = drops enabled).
            val blockBroken = world.breakBlock(targetPos, true, miner)

            // If the block was successfully broken, apply durability damage.
            if (blockBroken) {
                // Use the ToolComponent's damagePerBlock value.
                stack.damage(toolData.damagePerBlock(), miner as PlayerEntity?)
            }
        }
    }

    /**
     * Checks if a given block state is suitable for being mined by this tool in an AoE fashion.
     * Considers tool material tier, block tags, hardness, and tool configuration.
     *
     * @param state The [BlockState] to check.
     * @param world The current [World].
     * @param targetPos The [BlockPos] of the block.
     * @param toolData The [ToolComponent] of the item stack.
     * @return True if the block can be mined by this tool's AoE effect, false otherwise.
     */
    private fun isSuitableForMining(
        state: BlockState,
        world: World,
        targetPos: BlockPos,
        // Keep miner parameter
        toolData: ToolComponent,
    ): Boolean {
        // 1. Check if the tool is configured to mine this block type via the effectiveBlocks tag.
        if (!toolData.isCorrectForDrops(state)) { // Use ToolComponent's check which considers the effectiveBlocks tag
            // Or if effectiveBlocks tag is different from toolData rules: if (!state.isIn(effectiveBlocks)) return false
            return false
        }

        // 2. Check vanilla tool material tier requirements.
        // This logic correctly reflects standard Minecraft tool progression.
        val sufficientMiningLevel =
            when (material) {
                // Using the constants defined in this class's companion object
                C_NETHERITE, C_DIAMOND -> true
                C_IRON -> !state.isIn(BlockTags.NEEDS_DIAMOND_TOOL)
                C_STONE ->
                    !state.isIn(BlockTags.NEEDS_IRON_TOOL) &&
                        !state.isIn(BlockTags.NEEDS_DIAMOND_TOOL)
                C_WOOD, C_GOLD ->
                    !state.isIn(BlockTags.NEEDS_STONE_TOOL) &&
                        !state.isIn(BlockTags.NEEDS_IRON_TOOL) &&
                        !state.isIn(BlockTags.NEEDS_DIAMOND_TOOL)
                else -> false // Unknown material cannot mine
            }

        // 3. Check other conditions: Not air, has hardness > 0, tool deals damage, and meets material level.
        return !state.isAir &&
            state.getHardness(world, targetPos) > 0.0f &&
            toolData.damagePerBlock() > 0 &&
            // Ensure the tool component defines damage
            sufficientMiningLevel
    }
}
