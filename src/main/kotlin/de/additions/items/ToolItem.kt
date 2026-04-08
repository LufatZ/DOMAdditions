package de.additions.items

import net.fabricmc.fabric.api.event.player.AttackBlockCallback
import net.minecraft.core.BlockPos
import net.minecraft.tags.BlockTags
import net.minecraft.tags.TagKey
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.ToolMaterial
import net.minecraft.world.item.component.Tool
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState

/**
 * Base implementation of an item that functions as a tool, supporting specialized mining mechanics and durability management.
 */
open class ToolItem(
    val material: ToolMaterial,
    val effectiveBlocks: TagKey<Block>,
    settings: Properties,
) : Item(settings) {

    /**
     * Companion object for [ToolItem] containing utility constants and custom tool materials.
     *
     * This object handles the registration of block mining callbacks for players in Creative mode
     * to ensure compatibility with custom mining effects, and provides a collection of enhanced
     * [ToolMaterial] instances with increased durability.
     */
    companion object {
        init {
            /**
             * Registers a callback to handle block mining in Creative mode.
             * This ensures that custom AoE/Vein mining effects trigger even when
             * vanilla durability constraints are bypassed.
             */
            AttackBlockCallback.EVENT.register { player, world, _, pos, _ ->
                if (world.isClientSide || !player.isCreative) {
                    return@register InteractionResult.PASS
                }

                val stack = player.mainHandItem
                if (stack.item is ToolItem) {
                    val state = world.getBlockState(pos)
                    (stack.item as ToolItem).mineBlock(stack, world, state, pos, player)
                }

                InteractionResult.PASS
            }
        }

        /**
         * The radius defining the area of effect for block breaking operations.
         */
        const val RADIUS = 1


        /**
         * A constant multiplier applied to durability calculations during block mining operations.
         */
        private const val DURABILITY_MULTIPLIER = 10.0f

        /**
         * Specialized tool materials, featuring modified durability based on a predefined multiplier
         * while retaining other base properties.
         */
        val C_WOOD = ToolMaterial(
            BlockTags.INCORRECT_FOR_WOODEN_TOOL,
            (ToolMaterial.WOOD.durability() * DURABILITY_MULTIPLIER).toInt(),
            ToolMaterial.WOOD.speed(),
            ToolMaterial.WOOD.attackDamageBonus(),
            ToolMaterial.WOOD.enchantmentValue(),
            ToolMaterial.WOOD.repairItems()
        )
        val C_STONE = ToolMaterial(
            BlockTags.INCORRECT_FOR_STONE_TOOL,
            (ToolMaterial.STONE.durability() * DURABILITY_MULTIPLIER).toInt(),
            ToolMaterial.STONE.speed(),
            ToolMaterial.STONE.attackDamageBonus(),
            ToolMaterial.STONE.enchantmentValue(),
            ToolMaterial.STONE.repairItems()
        )
        val C_IRON = ToolMaterial(
            BlockTags.INCORRECT_FOR_IRON_TOOL,
            (ToolMaterial.IRON.durability() * DURABILITY_MULTIPLIER).toInt(),
            ToolMaterial.IRON.speed(),
            ToolMaterial.IRON.attackDamageBonus(),
            ToolMaterial.IRON.enchantmentValue(),
            ToolMaterial.IRON.repairItems()
        )
        val C_DIAMOND = ToolMaterial(
            BlockTags.INCORRECT_FOR_DIAMOND_TOOL,
            (ToolMaterial.DIAMOND.durability() * DURABILITY_MULTIPLIER).toInt(),
            ToolMaterial.DIAMOND.speed(),
            ToolMaterial.DIAMOND.attackDamageBonus,
            ToolMaterial.DIAMOND.enchantmentValue(),
            ToolMaterial.DIAMOND.repairItems()
        )
        val C_GOLD = ToolMaterial(
            BlockTags.INCORRECT_FOR_GOLD_TOOL,
            (ToolMaterial.GOLD.durability() * DURABILITY_MULTIPLIER).toInt(),
            ToolMaterial.GOLD.speed(),
            ToolMaterial.GOLD.attackDamageBonus(),
            ToolMaterial.GOLD.enchantmentValue(),
            ToolMaterial.GOLD.repairItems()
        )
        val C_NETHERITE = ToolMaterial(
            BlockTags.INCORRECT_FOR_NETHERITE_TOOL,
            (ToolMaterial.NETHERITE.durability() * DURABILITY_MULTIPLIER).toInt(),
            ToolMaterial.NETHERITE.speed(),
            ToolMaterial.NETHERITE.attackDamageBonus(),
            ToolMaterial.NETHERITE.enchantmentValue(),
            ToolMaterial.NETHERITE.repairItems()
        )

        /**
         * A mapping of material type names to their corresponding [ToolMaterial] definitions.
         */
        val materials: Map<String, ToolMaterial> = mapOf(
            "wood" to C_WOOD,
            "stone" to C_STONE,
            "iron" to C_IRON,
            "diamond" to C_DIAMOND,
            "gold" to C_GOLD,
            "netherite" to C_NETHERITE
        )

        /**
         * Fallback error message used when an unidentified material is encountered within a ToolItem.
         */
        internal const val UNKNOWN_MATERIAL_MSG = "Fallback -> Unknown material used in ToolItem"
    }

    /**
     * Attempts to break a block at a specific position using the provided tool data.
     * Checks if the target block is suitable for mining and applies durability damage
     * to the item stack upon successful destruction, provided the miner is not in creative mode.
     *
     * @param targetPos The position of the block to be broken.
     * @param world The level where the block destruction takes place.
     * @param miner The entity attempting to break the block.
     * @param stack The item stack being used, which may lose durability.
     * @param toolData The properties and damage characteristics of the tool.
     */
    internal fun tryBreakBlock(
        targetPos: BlockPos,
        world: Level,
        miner: LivingEntity,
        stack: ItemStack,
        toolData: Tool,
    ) {
        val targetState = world.getBlockState(targetPos)
        val isCreative = (miner is Player) && miner.isCreative

        if (isSuitableForMining(targetState, world, targetPos, toolData)) {
            val blockBroken = world.destroyBlock(targetPos, !isCreative, miner)

            if (blockBroken && !isCreative) {
                stack.hurtWithoutBreaking(toolData.damagePerBlock(), miner as Player)
            }
        }
    }

    /**
     * Checks whether a specific block state is suitable to be mined using the provided tool data.
     *
     * @param state The state of the block being evaluated.
     * @param world The level in which the block is located.
     * @param targetPos The position of the block.
     * @param toolData The properties and capabilities of the tool.
     * @return True if the block has a destruction speed greater than zero, the tool deals damage to blocks, and the tool is appropriate for the block's drops; false otherwise.
     */
    internal fun isSuitableForMining(
        state: BlockState,
        world: Level,
        targetPos: BlockPos,
        toolData: Tool,
    ): Boolean {
        return state.getDestroySpeed(world, targetPos) > 0.0f &&
                toolData.damagePerBlock() > 0 &&
                toolData.isCorrectForDrops(state)
    }
}
