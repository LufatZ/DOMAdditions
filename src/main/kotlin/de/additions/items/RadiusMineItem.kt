package de.additions.items

import de.additions.Additions.logger
import de.additions.datagen.ItemTagGenerator
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.component.DataComponentTypes
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.LivingEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.item.MiningToolItem
import net.minecraft.item.ToolMaterial
import net.minecraft.item.tooltip.TooltipType
import net.minecraft.recipe.Ingredient
import net.minecraft.registry.RegistryEntryLookup
import net.minecraft.registry.tag.ItemTags
import net.minecraft.registry.tag.TagKey
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World

/**
 * A custom mining tool that mines blocks in a radius around the target block.
 *
 * @property material The tool material defining durability and properties
 * @property effectiveBlocks Tag specifying which blocks can be mined effectively
 * @param attackDamage Base attack damage
 * @param attackSpeed Attack speed modifier
 * @param settings Item settings
 */
class RadiusMineItem(
    val material: ToolMaterial,
    val effectiveBlocks: TagKey<Block>,
    attackDamage: Float,
    attackSpeed: Float,
    settings: Settings
) : MiningToolItem(material, effectiveBlocks, attackDamage, attackSpeed, settings) {

    companion object {
        /** Multiplier for tool durability values */
        private const val DMULTI = 10.0f

        // Custom tool materials with multiplied durability
        val C_WOOD = ToolMaterial(
            ToolMaterial.WOOD.incorrectBlocksForDrops,
            (ToolMaterial.WOOD.durability * DMULTI).toInt(),
            ToolMaterial.WOOD.speed,
            ToolMaterial.WOOD.attackDamageBonus,
            ToolMaterial.WOOD.enchantmentValue,
            ToolMaterial.WOOD.repairItems
        )

        val C_STONE = ToolMaterial(
            ToolMaterial.STONE.incorrectBlocksForDrops,
            (ToolMaterial.STONE.durability * DMULTI).toInt(),
            ToolMaterial.STONE.speed,
            ToolMaterial.STONE.attackDamageBonus,
            ToolMaterial.STONE.enchantmentValue,
            ToolMaterial.STONE.repairItems
        )

        val C_IRON = ToolMaterial(
            ToolMaterial.IRON.incorrectBlocksForDrops,
            (ToolMaterial.IRON.durability * DMULTI).toInt(),
            ToolMaterial.IRON.speed,
            ToolMaterial.IRON.attackDamageBonus,
            ToolMaterial.IRON.enchantmentValue,
            ToolMaterial.IRON.repairItems
        )

        val C_DIAMOND = ToolMaterial(
            ToolMaterial.DIAMOND.incorrectBlocksForDrops,
            (ToolMaterial.DIAMOND.durability * DMULTI).toInt(),
            ToolMaterial.DIAMOND.speed,
            ToolMaterial.DIAMOND.attackDamageBonus,
            ToolMaterial.DIAMOND.enchantmentValue,
            ToolMaterial.DIAMOND.repairItems
        )

        val C_GOLD = ToolMaterial(
            ToolMaterial.GOLD.incorrectBlocksForDrops,
            (ToolMaterial.GOLD.durability * DMULTI).toInt(),
            ToolMaterial.GOLD.speed,
            ToolMaterial.GOLD.attackDamageBonus,
            ToolMaterial.GOLD.enchantmentValue,
            ToolMaterial.GOLD.repairItems
        )

        val C_NETHERITE = ToolMaterial(
            ToolMaterial.NETHERITE.incorrectBlocksForDrops,
            (ToolMaterial.NETHERITE.durability * DMULTI).toInt(),
            ToolMaterial.NETHERITE.speed,
            ToolMaterial.NETHERITE.attackDamageBonus,
            ToolMaterial.NETHERITE.enchantmentValue,
            ToolMaterial.NETHERITE.repairItems
        )

        /** Map of material names to their corresponding ToolMaterial instances */
        val materials = mapOf(
            "wood" to C_WOOD,
            "stone" to C_STONE,
            "iron" to C_IRON,
            "diamond" to C_DIAMOND,
            "gold" to C_GOLD,
            "netherite" to C_NETHERITE
        )
    }

    // Translation keys and tooltip configuration
    private val tooltipKey: String = "tooltip.additions.radius_mine"
    private val tooltipDescription: String = "This tool can mine in a radius of 2 blocks"
    private val unknownMaterial = "Fallback -> Unknown material $material"

    /**
     * @return Map containing the tooltip translation key and description
     */
    fun getTooltip(): Map<String, String> {
        return mapOf(tooltipKey to tooltipDescription)
    }

    /**
     * Adds the tooltip to the item stack
     */
    override fun appendTooltip(
        stack: ItemStack,
        context: TooltipContext,
        tooltip: MutableList<Text>,
        type: TooltipType
    ) {
        tooltip.add(Text.translatable(tooltipKey).formatted(Formatting.DARK_GREEN))
    }

    /**
     * Gets the crafting components for this tool's material
     * @return Pair containing either a tag key or specific item
     */
    fun getCraftingTagOrItem(): Pair<TagKey<Item>?, Item?> {
        return when (material) {
            C_WOOD -> Pair(ItemTags.PLANKS, null)
            C_STONE -> Pair(ItemTagGenerator.stonesTag, null)
            C_IRON -> Pair(null, Items.IRON_INGOT)
            C_DIAMOND -> Pair(null, Items.DIAMOND)
            C_GOLD -> Pair(null, Items.GOLD_INGOT)
            C_NETHERITE -> Pair(null, Items.NETHERITE_INGOT)
            else -> {
                logger.warn("$unknownMaterial (from getCraftingTagOrItem)")
                Pair(ItemTags.PLANKS, null)
            }
        }
    }

    /**
     * Creates an ingredient for crafting recipes
     * @param registryLookup Registry lookup for tag resolution
     * @return Ingredient based on tool material
     */
    fun getMaterialIngredient(registryLookup: RegistryEntryLookup<Item>): Ingredient {
        val (tag, item) = getCraftingTagOrItem()
        return when {
            tag != null -> Ingredient.fromTag(registryLookup.getOrThrow(tag))
            item != null -> Ingredient.ofItem(item)
            else -> {
                logger.warn("$unknownMaterial (from getMaterialIngredient)")
                Ingredient.fromTag(registryLookup.getOrThrow(ItemTags.PLANKS))
            }
        }
    }

    /**
     * Gets the representative block for the tool material
     * @return Block associated with the tool material
     */
    fun getMaterialBlock(): Block {
        return when (material) {
            C_WOOD -> Blocks.STRIPPED_DARK_OAK_LOG
            C_STONE -> Blocks.STONE
            C_IRON -> Blocks.IRON_BLOCK
            C_DIAMOND -> Blocks.DIAMOND_BLOCK
            C_GOLD -> Blocks.GOLD_BLOCK
            C_NETHERITE -> Blocks.NETHERITE_BLOCK
            else -> {
                logger.warn("$unknownMaterial (from getMaterialBlock)")
                Blocks.OAK_PLANKS
            }
        }
    }

    /**
     * Gets the name of the tool material
     * @return Material name or "unknown" if not found
     */
    fun getMaterialName(): String {
        return materials.entries.firstOrNull { it.value == material }?.key ?: run {
            logger.warn("$unknownMaterial (from getMaterialName)")
            "unknown"
        }
    }

    /**
     * Handles the block breaking logic with radius mining
     * @param radius The mining radius (diameter = radius*2 + 1)
     */
    override fun postMine(
        stack: ItemStack,
        world: World,
        state: BlockState,
        pos: BlockPos,
        miner: LivingEntity
    ): Boolean {
        val result = super.postMine(stack, world, state, pos, miner)
        val toolComponent = stack.get(DataComponentTypes.TOOL)

        if (world.isClient || state.getHardness(world, pos) == 0.0f || toolComponent == null) {
            return result
        }

        val radius = 2

        when (miner.facing) {
            Direction.UP, Direction.DOWN -> {
                // Mine in horizontal plane (XZ)
                for (dx in -radius..radius) {
                    for (dz in -radius..radius) {
                        if (dx * dx + dz * dz <= radius * radius) {
                            tryBreakBlock(pos.add(dx, 0, dz), world, miner, stack, toolComponent)
                        }
                    }
                }
            }
            Direction.NORTH, Direction.SOUTH -> {
                // Mine in vertical plane (XY)
                for (dx in -radius..radius) {
                    for (dy in -1..1) {
                        if (dx * dx + dy * dy <= radius * radius) {
                            tryBreakBlock(pos.add(dx, dy, 0), world, miner, stack, toolComponent)
                        }
                    }
                }
            }
            Direction.EAST, Direction.WEST -> {
                // Mine in vertical plane (ZY)
                for (dz in -radius..radius) {
                    for (dy in -1..1) {
                        if (dz * dz + dy * dy <= radius * radius) {
                            tryBreakBlock(pos.add(0, dy, dz), world, miner, stack, toolComponent)
                        }
                    }
                }
            }
            else -> Unit
        }
        return result
    }

    /**
     * Attempts to break a block at the target position
     * @param targetPos Position to break
     * @param damagePerBlock Damage to apply per broken block
     */
    private fun tryBreakBlock(
        targetPos: BlockPos,
        world: World,
        miner: LivingEntity,
        stack: ItemStack,
        toolComponent: net.minecraft.component.type.ToolComponent
    ) {
        if (targetPos == miner.blockPos) return

        val targetState = world.getBlockState(targetPos)
        if (!targetState.isAir &&
            targetState.getHardness(world, targetPos) != 0.0f &&
            targetState.isIn(effectiveBlocks) &&
            toolComponent.damagePerBlock > 0
        ) {
            world.breakBlock(targetPos, true, miner)
            stack.damage(toolComponent.damagePerBlock, miner, EquipmentSlot.MAINHAND)
        }
    }
}