package de.additions.items

import de.additions.Additions.logger
import de.additions.datagen.ItemTagGenerator
import de.additions.items.ToolItem.Companion.materials
import net.fabricmc.fabric.api.event.player.AttackBlockCallback
import net.minecraft.core.BlockPos
import net.minecraft.core.HolderGetter
import net.minecraft.tags.BlockTags
import net.minecraft.tags.ItemTags
import net.minecraft.tags.TagKey
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.ToolMaterial
import net.minecraft.world.item.component.Tool
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState

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
open class ToolItem(
    val material: ToolMaterial,
    val effectiveBlocks: TagKey<Block>,
    settings: Properties,
) : Item(settings) {
    companion object {
        init {
            // Register the AttackBlockCallback to handle creative mode AoE mining.
            AttackBlockCallback.EVENT.register { player, world, _, pos, _ ->
                // Only run on the server and in creative mode.
                // Survival is handled by postMine.
                if (world.isClientSide || !player.isCreative) {
                    return@register InteractionResult.PASS
                }

                val stack = player.mainHandItem
                val item = stack.item

                // Check if the player is holding a RadiusMineItem.
                if (item is ToolItem) {
                    val state = world.getBlockState(pos)

                    item.mineBlock(stack, world, state, pos, player)
                    // Let the original block be broken by the vanilla mechanic regardless.
                    return@register InteractionResult.PASS
                }

                // If not our tool, let the default action proceed.
                InteractionResult.PASS
            }
        }

        /** The radius for the Area of Effect (AoE) mining and path creation (0=1x1, 1=3x3, 2=5x5 -> 5x5 area). */
        const val RADIUS = 1

        internal enum class ToolTypes {
            PICKAXE,AXE,SHOVEL,HOE
        }
        internal enum class MiningTypes {
            RADIUS_MINING, VEIN_MINING
        }

        /** Multiplier applied to vanilla tool durability values. Applied when defining the ToolMaterial instance. */
        private const val DURABILITY_MULTIPLIER = 10.0f

        // --- Custom Tool Materials with Multiplied Durability ---
        val C_WOOD =
            ToolMaterial(
                BlockTags.INCORRECT_FOR_WOODEN_TOOL, // Use vanilla tag directly
                (ToolMaterial.WOOD.durability() * DURABILITY_MULTIPLIER).toInt(), // Use vanilla instance and method
                ToolMaterial.WOOD.speed(),
                ToolMaterial.WOOD.attackDamageBonus(),
                ToolMaterial.WOOD.enchantmentValue(),
                ToolMaterial.WOOD.repairItems(), // Use vanilla tag directly
            )

        val C_STONE =
            ToolMaterial(
                BlockTags.INCORRECT_FOR_STONE_TOOL,
                (ToolMaterial.STONE.durability() * DURABILITY_MULTIPLIER).toInt(),
                ToolMaterial.STONE.speed(),
                ToolMaterial.STONE.attackDamageBonus(),
                ToolMaterial.STONE.enchantmentValue(),
                ToolMaterial.STONE.repairItems(),
            )

        val C_IRON =
            ToolMaterial(
                BlockTags.INCORRECT_FOR_IRON_TOOL,
                (ToolMaterial.IRON.durability() * DURABILITY_MULTIPLIER).toInt(),
                ToolMaterial.IRON.speed(),
                ToolMaterial.IRON.attackDamageBonus(),
                ToolMaterial.IRON.enchantmentValue(),
                ToolMaterial.IRON.repairItems(),
            )

        val C_DIAMOND =
            ToolMaterial(
                BlockTags.INCORRECT_FOR_DIAMOND_TOOL,
                (ToolMaterial.DIAMOND.durability() * DURABILITY_MULTIPLIER).toInt(),
                ToolMaterial.DIAMOND.speed(),
                ToolMaterial.DIAMOND.attackDamageBonus(),
                ToolMaterial.DIAMOND.enchantmentValue(),
                ToolMaterial.DIAMOND.repairItems(),
            )

        val C_GOLD =
            ToolMaterial(
                BlockTags.INCORRECT_FOR_GOLD_TOOL,
                (ToolMaterial.GOLD.durability() * DURABILITY_MULTIPLIER).toInt(),
                ToolMaterial.GOLD.speed(),
                ToolMaterial.GOLD.attackDamageBonus(),
                ToolMaterial.GOLD.enchantmentValue(),
                ToolMaterial.GOLD.repairItems(),
            )

        val C_NETHERITE =
            ToolMaterial(
                BlockTags.INCORRECT_FOR_NETHERITE_TOOL,
                (ToolMaterial.NETHERITE.durability() * DURABILITY_MULTIPLIER).toInt(),
                ToolMaterial.NETHERITE.speed(),
                ToolMaterial.NETHERITE.attackDamageBonus(),
                ToolMaterial.NETHERITE.enchantmentValue(),
                ToolMaterial.NETHERITE.repairItems(),
            )

        /** Map associating string identifiers with their corresponding custom [ToolMaterial] instances. Useful for registration or data generation. */
        val materials: Map<String, ToolMaterial> =
            mapOf(
                "wood" to C_WOOD,
                "stone" to C_STONE,
                "iron" to C_IRON,
                "diamond" to C_DIAMOND,
                "gold" to C_GOLD,
                "netherite" to C_NETHERITE,
            )
        internal const val UNKNOWN_MATERIAL_MSG = "Fallback -> Unknown material used in RadiusMineItem"
    }
    // --- Material Helper Functions ---

    /**
     * Determines the appropriate crafting ingredient (tag or specific item) based on the tool's material.
     * Logs a warning if the material is unrecognized.
     * @return A [Pair] where the first element is a [TagKey]<[Item]>? and the second is an [Item]?. One should be non-null.
     */
    fun getCraftingTagOrItem(): Pair<TagKey<Item>?, Item?> =
        when (material) {
            C_WOOD -> Pair(ItemTags.PLANKS, null)
            C_STONE -> Pair(ItemTagGenerator.stonesTag, null) // Used in ItemTagGenerator
            C_IRON -> Pair(null, Items.IRON_INGOT)
            C_DIAMOND -> Pair(null, Items.DIAMOND)
            C_GOLD -> Pair(null, Items.GOLD_INGOT)
            C_NETHERITE -> Pair(null, Items.NETHERITE_INGOT)
            else -> {
                logger.warn("$UNKNOWN_MATERIAL_MSG (from getCraftingTagOrItem: ${material})")
                Pair(ItemTags.PLANKS, null) // Fallback to Planks tag
            }
        }

    /**
     * Creates a crafting [Ingredient] based on the tool's material using the result from [getCraftingTagOrItem].
     * Requires a [RegistryEntryLookup] for resolving tags. Logs a warning for unknown materials.
     * @param registryLookup A lookup provider for resolving registry entries like Item Tags.
     * @return The corresponding crafting [Ingredient].
     */
    fun getMaterialIngredient(registryLookup: HolderGetter<Item>): Ingredient {
        val (tag, item) = getCraftingTagOrItem()
        return when {
            tag != null -> Ingredient.of(registryLookup.getOrThrow(tag))
            item != null -> Ingredient.of(item) // Use ofItems for clarity with single item
            else -> {
                logger.warn("$UNKNOWN_MATERIAL_MSG (from getMaterialIngredient: ${material})")
                Ingredient.of(registryLookup.getOrThrow(ItemTags.PLANKS)) // Fallback ingredient
            }
        }
    }

    /**
     * Gets the string identifier (e.g., "wood", "stone") for the tool's material based on the [materials] map.
     * Logs a warning if the material is not found in the map.
     * @return The material name as a [String], or "unknown" if not found.
     */
    fun getMaterialName(): String =
        materials.entries.firstOrNull { it.value == material }?.key
            ?: run {
                logger.warn("$UNKNOWN_MATERIAL_MSG (from getMaterialName: ${material})")
                "unknown" // Fallback name
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
    internal fun tryBreakBlock(
        targetPos: BlockPos,
        world: Level,
        miner: LivingEntity,
        stack: ItemStack,
        toolData: Tool,
    ) {
        val targetState = world.getBlockState(targetPos)

        val isCreative = miner is Player && miner.isCreative
        val canMine = isSuitableForMining(targetState, world, targetPos, toolData)

        if (canMine) {
            val blockBroken = world.destroyBlock(targetPos, !isCreative, miner)

            if (blockBroken && !isCreative) {
                stack.hurtWithoutBreaking(toolData.damagePerBlock(), miner as Player)
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
    internal fun isSuitableForMining(
        state: BlockState,
        world: Level,
        targetPos: BlockPos,
        toolData: Tool,
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
                C_IRON -> !state.`is`(BlockTags.NEEDS_DIAMOND_TOOL)
                C_STONE ->
                    !state.`is`(BlockTags.NEEDS_IRON_TOOL) &&
                            !state.`is`(BlockTags.NEEDS_DIAMOND_TOOL)
                C_WOOD, C_GOLD ->
                    !state.`is`(BlockTags.NEEDS_STONE_TOOL) &&
                            !state.`is`(BlockTags.NEEDS_IRON_TOOL) &&
                            !state.`is`(BlockTags.NEEDS_DIAMOND_TOOL)
                else -> false // Unknown material cannot mine
            }

        // 3. Check other conditions: Not air, has hardness > 0, tool deals damage, and meets material level.
        return !state.isAir &&
                state.getDestroySpeed(world, targetPos) > 0.0f &&
                toolData.damagePerBlock() > 0 &&
                sufficientMiningLevel
    }
}
