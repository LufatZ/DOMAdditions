package de.additions.items

import de.additions.blocks.BlockRegistry
import de.additions.blocks.BlockRegistry.DIRT_PATH_SLAB
import de.additions.blocks.BlockRegistry.DIRT_PATH_STAIR
import de.additions.datagen.BlockTagGenerator
import de.additions.items.RadiusMineItem.Companion.PATH_CREATION_COOLDOWN
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.component.DataComponents
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.tags.BlockTags
import net.minecraft.tags.TagKey
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.ToolMaterial
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.SlabBlock
import net.minecraft.world.level.block.StairBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.gameevent.GameEvent
import java.util.*

/**
 * A custom mining tool that breaks blocks in a square (2*RADIUS+1)² area around the initially
 * mined block, in a plane relative to the miner's look direction (AoE mining).
 * Also provides AoE shovel functionality for creating and reverting dirt paths.
 *
 * Inherits from [ToolItem]. The [Item.Properties] passed at registration must already carry the
 * appropriate [DataComponents.TOOL] and [DataComponents.ATTRIBUTE_MODIFIERS] components
 * (use the `.pickaxe()`, `.shovel()`, etc. helpers on [Item.Properties]).
 *
 * @property material   The [ToolMaterial] used for helper lookups (crafting ingredient, name, …).
 * @property effectiveBlocks  The block tag whose members receive the AoE mining treatment.
 *                            Should match the tag baked into the [DataComponents.TOOL] component.
 * @param settings  Pre-configured [Item.Properties]; must include TOOL + ATTRIBUTE_MODIFIERS.
 */
class RadiusMineItem(
    material: ToolMaterial,
    effectiveBlocks: TagKey<Block>,
    settings: Properties,
) : ToolItem(material, effectiveBlocks, settings) {

    companion object {
        /** Cooldown in ticks before the path-creation ability can fire again (20 ticks = 1 s). */
        private const val PATH_CREATION_COOLDOWN = 2

        /** Per-player timestamp of the last successful path-creation use. */
        private val lastPathCreationTime = mutableMapOf<UUID, Long>()
    }

    /**
     * Called after a block is successfully mined.
     * Breaks every block in a square (2*RADIUS+1)² plane around [pos], oriented perpendicular
     * to the miner's look direction. The center block (already broken by vanilla) is skipped.
     *
     * Only runs server-side and only for blocks with a positive destroy speed (skips insta-break
     * blocks like air/grass to avoid wasting durability).
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

        if (world.isClientSide || state.getDestroySpeed(world, pos) <= 0.0f || toolData == null) {
            return initialResult
        }

        when (miner.nearestViewDirection) {
            // Looking up/down → square in the XZ plane
            Direction.UP, Direction.DOWN -> {
                for (dx in -RADIUS..RADIUS) {
                    for (dz in -RADIUS..RADIUS) {
                        if (dx == 0 && dz == 0) continue // centre already broken
                        tryBreakBlock(pos.offset(dx, 0, dz), world, miner, stack, toolData)
                    }
                }
            }
            // Looking north/south → square in the XY plane
            Direction.NORTH, Direction.SOUTH -> {
                for (dx in -RADIUS..RADIUS) {
                    for (dy in -RADIUS..RADIUS) {
                        if (dx == 0 && dy == 0) continue
                        tryBreakBlock(pos.offset(dx, dy, 0), world, miner, stack, toolData)
                    }
                }
            }
            // Looking east/west → square in the YZ plane
            Direction.EAST, Direction.WEST -> {
                for (dz in -RADIUS..RADIUS) {
                    for (dy in -RADIUS..RADIUS) {
                        if (dz == 0 && dy == 0) continue
                        tryBreakBlock(pos.offset(0, dy, dz), world, miner, stack, toolData)
                    }
                }
            }
        }

        return true
    }

    /**
     * Right-click handler. Converts suitable blocks within a square (2*RADIUS+1)² XZ area around
     * the clicked position into dirt paths (normal use) or reverts path variants back to dirt
     * (sneak-use). The area is always horizontal regardless of look direction.
     *
     * Respects a short [PATH_CREATION_COOLDOWN] to prevent sound/event spam on rapid clicks.
     */
    override fun useOn(context: UseOnContext): InteractionResult {
        val world = context.level
        val initialPos = context.clickedPos
        val player = context.player ?: return InteractionResult.PASS
        val stack = context.itemInHand ?: return InteractionResult.PASS

        // --- Cooldown check ---
        val playerUuid = player.uuid
        val currentTime = world.gameTime
        if (currentTime - (lastPathCreationTime[playerUuid] ?: 0L) < PATH_CREATION_COOLDOWN) {
            return InteractionResult.PASS
        }

        val pathableFullBlocks = BlockTags.DIRT
        val pathableCustomBlocks = BlockTagGenerator.DirtLikeBlockTag
        val dirtConvertableBlocks = BlockTagGenerator.DirtPathVariantTag
        var changedSomething = false

        fun isPathable(state: BlockState) =
            state.`is`(pathableFullBlocks) || state.`is`(pathableCustomBlocks)

        fun isDirtConvertable(state: BlockState) =
            state.`is`(dirtConvertableBlocks)

        for (dx in -RADIUS..RADIUS) {
            for (dz in -RADIUS..RADIUS) {
                val currentPos = initialPos.offset(dx, 0, dz)
                val targetState = world.getBlockState(currentPos)
                val blockAboveState = world.getBlockState(currentPos.above())

                if (isPathable(targetState) && blockAboveState.isAir && !player.isShiftKeyDown) {
                    // --- Normal use: convert to dirt path ---
                    val pathState: BlockState? = when (targetState.block) {
                        Blocks.DIRT_PATH -> null   // already a path
                        DIRT_PATH_STAIR  -> null
                        DIRT_PATH_SLAB   -> null
                        is StairBlock    -> DIRT_PATH_STAIR.withPropertiesOf(targetState)
                        is SlabBlock     -> DIRT_PATH_SLAB.withPropertiesOf(targetState)
                        else             -> Blocks.DIRT_PATH.defaultBlockState()
                    }

                    if (pathState != null) {
                        world.playSound(player, currentPos, SoundEvents.SHOVEL_FLATTEN, SoundSource.BLOCKS)

                        if (!world.isClientSide) {
                            world.setBlock(currentPos, pathState, Block.UPDATE_CLIENTS or Block.UPDATE_KNOWN_SHAPE)
                            world.gameEvent(GameEvent.BLOCK_CHANGE, currentPos, GameEvent.Context.of(player, pathState))
                            stack.hurtWithoutBreaking(1, player)
                            changedSomething = true
                        }
                    }

                } else if (isDirtConvertable(targetState) && player.isShiftKeyDown) {
                    // --- Sneak-use: revert path variants back to dirt ---
                    val newState = when (targetState.block) {
                        is StairBlock -> BlockRegistry.DIRT_STAIR.withPropertiesOf(targetState)
                        is SlabBlock  -> BlockRegistry.DIRT_SLAB.withPropertiesOf(targetState)
                        else          -> Blocks.DIRT.withPropertiesOf(targetState)
                    }

                    world.playSound(player, currentPos, SoundEvents.SHOVEL_FLATTEN, SoundSource.BLOCKS)

                    if (!world.isClientSide) {
                        // Push the player up slightly when the block directly below them is replaced
                        if (currentPos == player.blockPosition()) {
                            player.randomTeleport(player.x, player.y + 0.5, player.z, false)
                        }

                        world.setBlock(currentPos, newState, Block.UPDATE_CLIENTS or Block.UPDATE_KNOWN_SHAPE)
                        world.gameEvent(GameEvent.BLOCK_CHANGE, currentPos, GameEvent.Context.of(player, newState))
                        stack.hurtWithoutBreaking(1, player)
                        changedSomething = true
                    }
                }
            }
        }

        return if (changedSomething) {
            if (!world.isClientSide) lastPathCreationTime[playerUuid] = currentTime
            InteractionResult.SUCCESS
        } else {
            InteractionResult.PASS
        }
    }
}