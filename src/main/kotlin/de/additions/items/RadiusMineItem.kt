@file:Suppress("ktlint:standard:no-wildcard-imports")

package de.additions.items

import de.additions.Additions.logger
import de.additions.blocks.BlockRegistry.DIRT_PATH_SLAB
import de.additions.blocks.BlockRegistry.DIRT_PATH_STAIR
import de.additions.blocks.SnowyStairsBlock
import de.additions.datagen.BlockTagGenerator
import de.additions.datagen.ItemTagGenerator
import de.additions.items.RadiusMineItem.Companion.RADIUS
import de.additions.items.RadiusMineItem.Companion.materials
import net.minecraft.block.*
import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.ToolComponent
import net.minecraft.component.type.TooltipDisplayComponent
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.*
import net.minecraft.item.tooltip.TooltipType
import net.minecraft.recipe.Ingredient
import net.minecraft.registry.RegistryEntryLookup
import net.minecraft.registry.tag.BlockTags
import net.minecraft.registry.tag.ItemTags
import net.minecraft.registry.tag.TagKey
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.state.property.Properties
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.Formatting
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World
import net.minecraft.world.event.GameEvent
import java.util.*
import java.util.function.Consumer

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
class RadiusMineItem(
    val material: ToolMaterial,
    val effectiveBlocks: TagKey<Block>,
    settings: Settings,
) : Item(settings) {
    companion object {
        /** Multiplier applied to vanilla tool durability values. Applied when defining the ToolMaterial instance. */
        private const val DURABILITY_MULTIPLIER = 10.0f

        /** The radius for the Area of Effect (AoE) mining and path creation (0=1x1, 1=3x3, 2=5x5 -> 5x5 area). */
        private const val RADIUS = 2

        /** Cooldown in Ticks (20 Ticks = 1 Second) for the path creation ability. */
        private const val PATH_CREATION_COOLDOWN = 2

        /** Map to store the last usage time of the path creation ability per player UUID. */
        private val lastPathCreationTime = mutableMapOf<UUID, Long>()

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
    }

    // --- Tooltip Configuration ---
    private val tooltipKeyDesc1: String = "tooltip.additions.radius_mine_1"
    private val tooltipKeyDesc2: String = "tooltip.additions.radius_mine_2"
    private val tooltipKeyArea: String = "tooltip.additions.radius_mine_area"
    private val tooltipKeyEffective: String = "tooltip.additions.radius_blocks"

    // Provide a more descriptive tooltip text using the actual diameter.
    private val tooltipDescriptionFirst: String = "Mines blocks in a below defined area."
    private val tooltipDescriptionSecond: String = "Also can create paths in this area."
    private val tooltipArea: String = "Mine Area:"
    private val tooltipEffective: String = "Effective Blocks:"
    private val unknownMaterialErrorMsg = "Fallback -> Unknown material used in RadiusMineItem: $material"

    /** Provides tooltip data, useful for registration/data generation. */
    fun getTooltipData(): Map<String, String> =
        mapOf(
            tooltipKeyDesc1 to tooltipDescriptionFirst,
            tooltipKeyDesc2 to tooltipDescriptionSecond,
            tooltipKeyArea to tooltipArea,
            tooltipKeyEffective to tooltipEffective,
        )

    // TODO: Migrate tooltip handling to DataComponentTypes.LORE or a custom component for modern approach.
    @Deprecated("Uses older tooltip system. Modern approach uses components.", ReplaceWith("Modern component-based tooltips"))
    override fun appendTooltip(
        stack: ItemStack,
        context: TooltipContext,
        display: TooltipDisplayComponent,
        consumer: Consumer<Text>,
        type: TooltipType,
    ) {
        consumer.accept(Text.translatable("")) // placeholder for spacing
        consumer.accept(Text.translatable(tooltipKeyDesc1).formatted(Formatting.WHITE))
        consumer.accept(Text.translatable(tooltipKeyDesc2).formatted(Formatting.WHITE))
        consumer.accept(Text.translatable(tooltipKeyArea).formatted(Formatting.AQUA))
        consumer.accept(Text.literal(" -> ${2 * RADIUS + 1}x${2 * RADIUS + 1}"))
        consumer.accept(Text.translatable(tooltipKeyEffective).formatted(Formatting.AQUA))
        consumer.accept(Text.translatable(" -> %s", effectiveBlocks.name))
        super.appendTooltip(stack, context, display, consumer, type)
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
                logger.warn("$unknownMaterialErrorMsg (from getCraftingTagOrItem)")
                Pair(ItemTags.PLANKS, null) // Fallback to Planks tag
            }
        }

    /**
     * Creates a crafting [Ingredient] based on the tool's material using the result from [getCraftingTagOrItem].
     * Requires a [RegistryEntryLookup] for resolving tags. Logs a warning for unknown materials.
     * @param registryLookup A lookup provider for resolving registry entries like Item Tags.
     * @return The corresponding crafting [Ingredient].
     */
    fun getMaterialIngredient(registryLookup: RegistryEntryLookup<Item>): Ingredient {
        val (tag, item) = getCraftingTagOrItem()
        return when {
            tag != null -> Ingredient.fromTag(registryLookup.getOrThrow(tag))
            item != null -> Ingredient.ofItems(item) // Use ofItems for clarity with single item
            else -> {
                logger.warn("$unknownMaterialErrorMsg (from getMaterialIngredient - fallback used)")
                Ingredient.fromTag(registryLookup.getOrThrow(ItemTags.PLANKS)) // Fallback ingredient
            }
        }
    }

    /**
     * Gets a representative block associated with the tool's material.
     * Useful for visual elements in recipes or GUIs. Logs a warning for unknown materials.
     * @return The representative [Block].
     */
    fun getMaterialBlock(): Block =
        when (material) {
            C_WOOD -> Blocks.OAK_LOG // Keep example consistent
            C_STONE -> Blocks.STONE
            C_IRON -> Blocks.IRON_BLOCK
            C_DIAMOND -> Blocks.DIAMOND_BLOCK
            C_GOLD -> Blocks.GOLD_BLOCK
            C_NETHERITE -> Blocks.NETHERITE_BLOCK
            else -> {
                logger.warn("$unknownMaterialErrorMsg (from getMaterialBlock)")
                Blocks.OAK_PLANKS // Fallback block
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
                logger.warn("$unknownMaterialErrorMsg (from getMaterialName)")
                "unknown" // Fallback name
            }

    // --- Core Functionality ---

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
        val hand = context.hand

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
        var changedSomething = false

        fun isInPathable(state: BlockState): Boolean = state.isIn(pathableFullBlocks) || state.isIn(pathableCustomBlocks)

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
                    if (isInPathable(targetState) && blockAboveState.isAir) {
                        val targetBlock = targetState.block

                        // Determine the desired path state
                        val pathState: BlockState? =
                            when (targetBlock) {
                                Blocks.DIRT_PATH -> null // Already a path block, skip
                                // Check if target is already a custom path stair/slab
                                DIRT_PATH_STAIR -> null
                                DIRT_PATH_SLAB -> null
                                is StairsBlock -> tryCreatePathStairState(targetState) // Use helper
                                is SlabBlock -> tryCreatePathSlabState(targetState) // Use helper
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
     * @param miner The [LivingEntity] mining (potential for future checks).
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

    // --- Helper functions for path creation state conversion ---
    // (Keep these as previously refactored for clarity)

    /**
     * Attempts to create a path stair state, preserving original properties if possible.
     * Logs a warning and returns null if conversion fails or the target path stair block is not registered.
     */
    private fun tryCreatePathStairState(originalState: BlockState): BlockState? {
        val pathStairBlock = DIRT_PATH_STAIR // Ensure path stair block exists

        return try {
            var newState = pathStairBlock.defaultState
            // Copy common stair properties safely checking if they exist on the target
            if (newState.contains(Properties.HORIZONTAL_FACING) && originalState.contains(Properties.HORIZONTAL_FACING)) {
                newState = newState.with(Properties.HORIZONTAL_FACING, originalState.get(Properties.HORIZONTAL_FACING))
            }
            if (newState.contains(Properties.BLOCK_HALF) && originalState.contains(Properties.BLOCK_HALF)) {
                newState = newState.with(Properties.BLOCK_HALF, originalState.get(Properties.BLOCK_HALF))
            }
            if (newState.contains(Properties.STAIR_SHAPE) && originalState.contains(Properties.STAIR_SHAPE)) {
                newState = newState.with(Properties.STAIR_SHAPE, originalState.get(Properties.STAIR_SHAPE))
            }
            if (newState.contains(Properties.WATERLOGGED) && originalState.contains(Properties.WATERLOGGED)) {
                newState = newState.with(Properties.WATERLOGGED, originalState.get(Properties.WATERLOGGED))
            }

            // Copy custom 'SNOWY' property if both blocks have it
            if (originalState.contains(SnowyStairsBlock.SNOWY) && newState.contains(SnowyStairsBlock.SNOWY)) {
                newState = newState.with(SnowyStairsBlock.SNOWY, originalState.get(SnowyStairsBlock.SNOWY))
            }
            newState // Return the configured state
        } catch (e: IllegalArgumentException) {
            // Catch cases where a property might exist on original but not target
            logger.warn(
                "[RadiusMineItem] Failed to copy stair properties during path conversion for state $originalState. Target: $pathStairBlock",
                e,
            )
            null // Indicate failure
        }
    }

    /**
     * Attempts to create a path slab state, preserving original properties if possible.
     * Logs a warning and returns null if conversion fails or the target path slab block is not registered.
     */
    private fun tryCreatePathSlabState(originalState: BlockState): BlockState? {
        val pathSlabBlock = DIRT_PATH_SLAB

        return try {
            var newState = pathSlabBlock.defaultState
            // Copy common slab properties safely
            if (newState.contains(Properties.SLAB_TYPE) && originalState.contains(Properties.SLAB_TYPE)) {
                newState = newState.with(Properties.SLAB_TYPE, originalState.get(Properties.SLAB_TYPE))
            }
            if (newState.contains(Properties.WATERLOGGED) && originalState.contains(Properties.WATERLOGGED)) {
                newState = newState.with(Properties.WATERLOGGED, originalState.get(Properties.WATERLOGGED))
            }
            newState // Return the configured state
        } catch (e: IllegalArgumentException) {
            logger.warn(
                "[RadiusMineItem] Failed to copy slab properties during path conversion for state $originalState. Target: $pathSlabBlock",
                e,
            )
            null // Indicate failure
        }
    }
}
