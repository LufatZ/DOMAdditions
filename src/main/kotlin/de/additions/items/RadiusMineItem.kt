package de.additions.items

import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.component.DataComponentTypes
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.MiningToolItem
import net.minecraft.item.ToolMaterial
import net.minecraft.item.tooltip.TooltipType
import net.minecraft.registry.tag.TagKey
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World

class RadiusMineItem (material: ToolMaterial, val effectiveBlocks: TagKey<Block>, attackDamage: Float, attackSpeed: Float, settings: Settings) : MiningToolItem(material, effectiveBlocks, attackDamage, attackSpeed, settings) {

    val tootipKey: String = "tooltip.additions.radius_mine"
    val tooltipDescription: String = "This tool can mine in a radius of 2 blocks"

    fun getTooltip(): Map<String, String> {
        return mapOf(tootipKey to tooltipDescription)
    }

    override fun appendTooltip(stack: ItemStack, context: TooltipContext, tooltip: MutableList<Text>, type: TooltipType) {
        tooltip.add(Text.translatable(tootipKey).formatted(Formatting.DARK_GREEN))
    }

    // This tool only works in survival mode. if anyone knows how to make it work in creative mode, please let me know or just take a commit
    override fun postMine(stack: ItemStack, world: World, state: BlockState,pos: BlockPos, miner: LivingEntity): Boolean {
        //first call the super method to get the default behavior
        val result = super.postMine(stack, world, state, pos, miner)
        val toolComponent = stack.get(DataComponentTypes.TOOL)
        //On client side or if the block has a hardness of 0.0f (like air), we don't want to do anything
        if (world.isClient || state.getHardness(world, pos) == 0.0f || toolComponent == null) {
            return result
        }
        // The radius of the block to be mined. The circle will have a diameter of 3 blocks at default
        val radius = 2
        // Take use of the facing of the player to determine the direction of the circle
        // If the player is looking horizontally, so we will mine in a vertical circle else in a horizontal circle
        // related to the player's facing
        when (miner.facing) {
            Direction.UP, Direction.DOWN -> {
                for (dx in -radius..radius) {
                    for (dz in -radius..radius) {
                        if (dx * dx + dz * dz <= radius * radius){
                            val targetPos = pos.add(dx, 0, dz)
                            if (targetPos == pos) {
                                continue
                            }
                            val targetState = world.getBlockState(targetPos)
                            if (!targetState.isAir && targetState.getHardness(world, targetPos) != 0.0f && targetState.isIn(effectiveBlocks) && toolComponent.damagePerBlock > 0) {
                                world.breakBlock(targetPos, true, miner)
                                stack.damage(toolComponent.damagePerBlock, miner, EquipmentSlot.MAINHAND)
                            }
                        }
                    }
                }
            }
            Direction.NORTH, Direction.SOUTH -> {
                for (dx in -radius..radius) {
                    for (dy in -1..1) {
                        if (dx * dx + dy * dy <= radius * radius){
                            val targetPos = pos.add(dx, dy, 0)
                            if (targetPos == pos) {
                                continue
                            }
                            val targetState = world.getBlockState(targetPos)
                            if (!targetState.isAir && targetState.getHardness(world, targetPos) != 0.0f && targetState.isIn(effectiveBlocks) && toolComponent.damagePerBlock > 0) {
                                world.breakBlock(targetPos, true, miner)
                                stack.damage(toolComponent.damagePerBlock, miner, EquipmentSlot.MAINHAND)
                            }
                        }
                    }
                }
            }
            Direction.EAST, Direction.WEST -> {
                for (dz in -radius..radius) {
                    for (dy in -1..1) {
                        if (dz * dz + dy * dy <= radius * radius){
                            val targetPos = pos.add(0, dy, dz)
                            if (targetPos == pos) {
                                continue
                            }
                            val targetState = world.getBlockState(targetPos)
                            if (!targetState.isAir && targetState.getHardness(world, targetPos) != 0.0f && targetState.isIn(effectiveBlocks) && toolComponent.damagePerBlock > 0) {
                                world.breakBlock(targetPos, true, miner)
                                stack.damage(toolComponent.damagePerBlock, miner, EquipmentSlot.MAINHAND)
                            }
                        }
                    }
                }
            }
            // If the player is looking in any other direction, we don't want to do anything
            else -> Unit
        }
        return result
    }
}