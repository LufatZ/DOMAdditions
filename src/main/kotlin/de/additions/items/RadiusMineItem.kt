package de.additions.items

import de.additions.datagen.BlockTagGenerator
import de.additions.helper.PathConversionHelper
import de.additions.helper.PathConversionHelper.isPathable
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.component.DataComponents
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.tags.TagKey
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.ToolMaterial
import net.minecraft.world.item.component.Tool
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.gameevent.GameEvent

/**
 * A specialized tool implementation that performs area-of-effect mining and terrain modification.
 *
 * This item extends the functionality of the [ToolItem] by applying effects to adjacent blocks
 * based on the direction the miner is facing when breaking blocks, or by modifying path blocks
 * within a radius when interacting with the world.
 *
 * @param material The [ToolMaterial] defining the tool's durability and strength.
 * @param effectiveBlocks A tag key representing the set of blocks this tool can mine.
 * @param settings The properties for the item instance.
 */
class RadiusMineItem(
    material: ToolMaterial,
    effectiveBlocks: TagKey<Block>,
    settings: Properties,
) : ToolItem(material, effectiveBlocks, settings) {

    /**
     * Performs block mining with an area-of-effect (AoE) expansion based on the direction the miner is facing.
     * This method triggers a secondary breaking process in a specified radius around the target block,
     * depending on whether the miner is looking up/down, north/south, or east/west.
     *
     * @param stack The [ItemStack] being used to mine the block.
     * @param world The [Level] where the mining occurs.
     * @param state The [BlockState] of the block being mined.
     * @param pos The [BlockPos] of the block being mined.
     * @param miner The [LivingEntity] performing the mining action.
     * @return True if the mining operation was successful, regardless of whether an expansion occurred.
     */
    override fun mineBlock(stack: ItemStack, world: Level, state: BlockState, pos: BlockPos, miner: LivingEntity): Boolean {
        val initialResult = super.mineBlock(stack, world, state, pos, miner)

        val toolData = stack.get(DataComponents.TOOL) ?: return initialResult
        when (miner.nearestViewDirection) {
            Direction.UP, Direction.DOWN ->
                expandAndBreak(pos, world, miner, stack, toolData, dx = -RADIUS..RADIUS, dy = 0..0, dz = -RADIUS..RADIUS)

            Direction.NORTH, Direction.SOUTH ->
                expandAndBreak(pos, world, miner, stack, toolData, dx = -RADIUS..RADIUS, dy = -RADIUS..RADIUS, dz = 0..0)

            else ->
                expandAndBreak(pos, world, miner, stack, toolData, dx = 0..0, dy = -RADIUS..RADIUS, dz = -RADIUS..RADIUS)
        }
        return true
    }

    /**
     * Iterates through a specified 3D range around the center position and attempts to break blocks at each offset, skipping the center block itself.
     *
     * @param center The central position of the mining area.
     * @param world The level where the mining occurs.
     * @param miner The entity performing the action.
     * @param stack The item stack being used.
     * @param toolData The data containing tool-specific properties and damage values.
     * @param dx The range of X-axis offsets to apply.
     * @param dy The range of Y-axis offsets to apply.
     * @param dz The range of Z-axis offsets to apply.
     */
    private fun expandAndBreak(
        center: BlockPos,
        world: Level,
        miner: LivingEntity,
        stack: ItemStack,
        toolData: Tool,
        dx: IntRange,
        dy: IntRange,
        dz: IntRange
    ) {
        for (x in dx) {
            for (y in dy) {
                for (z in dz) {
                    if (x == 0 && y == 0 && z == 0) continue // Skip the block already broken by vanilla
                    tryBreakBlock(center.offset(x, y, z), world, miner, stack, toolData)
                }
            }
        }
    }

    /**
     * Handles the interaction logic when using the item on a block, allowing for the radial conversion
     * of pathable blocks to dirt paths or reverting dirt paths back to dirt.
     *
     * When the player is sneaking, the method attempts to revert path variants within a specified radius
     * to their base dirt state. When not sneaking, it attempts to convert pathable blocks into
     * dirt path variants if the space above the target block is air. The operation requires the
     * clicked block to be pathable or for the player to be sneaking.
     *
     * @param context The context of the interaction, containing the level, player, item in hand, and clicked position.
     * @return An [InteractionResult] indicating whether the interaction was a success or was passed.
     */
    override fun useOn(context: UseOnContext): InteractionResult {
        val world = context.level
        val player = context.player ?: return InteractionResult.PASS
        val stack = context.itemInHand
        val pos = context.clickedPos
        var changed = false

        if (!isPathable(world.getBlockState(pos)) && !player.isShiftKeyDown) return InteractionResult.PASS
        for (dx in -RADIUS..RADIUS) {
            for (dz in -RADIUS..RADIUS) {
                val targetPos = pos.offset(dx, 0, dz)
                val state = world.getBlockState(targetPos)

                if (player.isShiftKeyDown) {
                    if (state.`is`(BlockTagGenerator.DirtPathVariantTag)) {
                        val newState = PathConversionHelper.revertToDirtState(state)
                        applyChange(world, targetPos, newState, player, stack)
                        changed = true
                    }
                } else {
                    if (isPathable(state) && world.getBlockState(targetPos.above()).isAir) {
                        val newState = PathConversionHelper.getPathTargetState(state)
                        applyChange(world, targetPos, newState, player, stack)
                        changed = true
                    }
                }
            }
        }

        return if (changed) {
            world.playSound(player, pos, SoundEvents.SHOVEL_FLATTEN, SoundSource.BLOCKS, 1.0f, 1.0f)
            InteractionResult.SUCCESS
        } else InteractionResult.PASS
    }

    /**
     * Applies a block change at the specified position on the server side, adjusts entity positions
     * to prevent clipping with the new state, triggers a block change game event, and reduces
     * the durability of the item stack.
     *
     * @param world The level where the block change is being performed.
     * @param pos The location of the block being updated.
     * @param state The new block state to be set at the position.
     * @param player The entity performing the action, used for event context and durability reduction.
     * @param stack The item stack being used, which will lose durability during the operation.
     */
    private fun applyChange(world: Level, pos: BlockPos, state: BlockState, player: LivingEntity, stack: ItemStack) {
        if (!world.isClientSide) {
            world.setBlock(pos, state, 11)
            PathConversionHelper.applyEntityFix(world, pos, state)
            world.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, state))
            stack.hurtAndBreak(1, player, InteractionHand.MAIN_HAND)
        }
    }
}
